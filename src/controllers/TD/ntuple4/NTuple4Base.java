package controllers.TD.ntuple4;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Arena;
import games.StateObsWithBoardVector;
import games.StateObservation;
import games.RubiksCube.CubeConfig;
import params.*;

/**
 *  Abstract superclass for {@link Sarsa4Agt} and {@link TDNTuple4Agt}.
 *
 *	@see Sarsa4Agt
 *	@see TDNTuple4Agt
 */
abstract public class NTuple4Base extends AgentBase implements NTuple4Agt {
	public Random rand; // generate random Numbers
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	@Serial
	private static final long  serialVersionUID = 13L;

	/**
	 * Controls the amount of explorative moves in
	 * {@link PlayAgent#getNextAction2(StateObservation, boolean, boolean, boolean)}
	 * during training. <br>
	 * m_epsilon = 0.0: no random moves, <br>
	 * m_epsilon = 0.1 (def.): 10% of the moves are random, and so forth
	 * m_epsilon undergoes a linear change from {@code tdPar.getEpsilon()} 
	 * to {@code tdPar.getEpsilonFinal()}. 
	 * This is realized in {@link #finishUpdateWeights()}.
	 */
	protected double m_epsilon = 0.1;
	
	/**
	 * m_EpsilonChangeDelta is the epsilon change per episode.
	 */
	protected double m_EpsilonChangeDelta = 0.001;
	
//	protected boolean TC; 				//obsolete, use now m_Net.getTc()
//	protected boolean tcImm=true;		//obsolete, use now m_Net.getTcImm()
	protected int tcIn; 	// obsolete (since tcImm always true); was: temporal coherence interval: 
							// if (!tcImm), then after tcIn games tcFactor will be updates
	
	// Value function of the agent.
	protected NTuple4ValueFunc m_Net;

	/**
	 * Members {@code #m_tdPar}, {@link #m_ntPar}, {@link AgentBase#m_oPar} are needed for
	 * saving and loading the agent (to restore the agent with all its parameter settings)
	 */
	protected ParTD m_tdPar;

	protected ParNT m_ntPar;
	
	protected boolean PRINTTABLES = false;	// /WK/ control the printout of tableA, tableN, epsilon
	
	//
	// variables needed in various train methods
	//
	protected int m_counter = 0;				// episode move counter (trainAgent)
	protected boolean m_finished = false;		// whether a training game is finished
	protected boolean epiLengthStop = false;	// whether a training game finished due to epiLength condition
	protected RandomAgent randomAgent = new RandomAgent("Random");
	
	// info only:
	protected int tieCounter = 0;
	protected int winXCounter = 0;
	protected int winOCounter = 0;

	/**
	 * Old debug switches from TDNTuple2Agt, may be occasionally useful for NTuple4ValueFunc
	 */
	// debug printout in updateWeightsNew, ZValueMulti, NextState:
	public static boolean DBG_REWARD=false;
	// debug: repeat always the same sequence in one episode (to check a trivial convergence)
	public static boolean DBG2_FIXEDSEQUENCE=false;
	// debug VER_3P=true, MODE_3P=0: m_Net3 is updated in parallel to m_Net (compare 2P and 3P),
	// extra printout v_old,v_new in NTuple4ValueFunc::updateWeightsNew,updateWeightsNewTerminal
	public static boolean DBG_OLD_3P=false;
	// debug VER_3P=true, MODE_3P=1:
	public static boolean DBG_NEW_3P=false;
	// debug printout in updateWeightsNewTerminal:
	public static boolean DBGF_TARGET=false;

	public NTuple4Base() {
		super();
	}

	public NTuple4Base(String name) {
		super(name);
	}

	public NTuple4Base(String name, ParOther oPar, ParRB rbPar, ParWrapper wrPar) {
		super(name,oPar,rbPar,wrPar);
	}

	public boolean instantiateAfterLoading() {
		super.instantiateAfterLoading();
		this.instantiateParTD();
		return true;
	}

	/**
	 * Instantiate parameters stepReward and rewardPositive.
	 * If they are 0.0, i.e. not present in older agents, instantiate them to
	 * those values that were effective before 2022-09-29 (see {@link CubeConfig})
	 */
	public void instantiateParTD() {
		if (m_tdPar.getStepReward()==0.0) m_tdPar.setStepReward(-0.04);
		if (m_tdPar.getRewardPositive()==0.0) m_tdPar.setRewardPositive(1.0);
		//setRewardParsFromParTD();
	}

	// --- is obsolete now, we call GameBoardCube.updateParams to achieve the same ---
//	public void setRewardParsFromParTD() {
//		// These two parameter stepReward and rewardPositive are currently only needed by RubiksCube.
//		// It is a bit awkward that we use them to set the global CubeConfig.stepReward and CubeConfig.REWARD_POSITIVE.
//		// This is because at the point where we need them (e.g. StateObservation.getReward() or getGameScore()),
//		// the agent and its m_tdPar is not available.
//		CubeConfig.stepReward = m_tdPar.getStepReward();
//		CubeConfig.REWARD_POSITIVE = m_tdPar.getRewardPositive();
//	}

	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 *
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 *
	 * @see Arena#loadAgent
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) {
		super.fillParamTabsAfterLoading(n, m_arena);
		m_arena.m_xab.setTdParFrom(n, this.getParTD() );
		m_arena.m_xab.setNtParFrom(n, this.getParNT() );
	}
	
	/**
	 * 
	 * @param score the game score to normalize
	 * @param so	needed for accessing getMinGameScore(), getMaxGameScore()
	 * @return normalized score to [-1,+1] (the appropriate range for tanh-sigmoid) if 
	 * 		switch {@link #m_tdPar}{@code .getNormalize()} is set.
	 */
	protected double normalize2(double score, StateObservation so) {
		if (m_tdPar.getNormalize()) {
			// since we have - in contrast to TDAgent - here only one sigmoid
			// choice, namely tanh, we can take fixed [min,max] = [-1,+1]. 
			// If we would later extend to several sigmoids, we would have to 
			// adapt normalize2 accordingly:		
			score = normalize(score,so.getMinGameScore(),
							   		so.getMaxGameScore(),-1.0,+1.0);
		}
		return score;
	}
	
	/**
	 * Adjust {@code ALPHA} and adjust {@code m_epsilon}.
	 */
	public void finishUpdateWeights() {

		m_Net.finishUpdateWeights(); // adjust learn param ALPHA

		// linear decrease of m_epsilon (re-activated 08/2017)
		m_epsilon = m_epsilon - m_EpsilonChangeDelta;

		if (PRINTTABLES) {
			try {
				print(m_epsilon);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void print(double m_epsilon2) throws IOException {
		PrintWriter epsilon = new PrintWriter(new FileWriter("epsilon",true));
		epsilon.println("" +m_epsilon2);
		epsilon.close();
	}
	
	public String printTrainStatus() {
		DecimalFormat frm = new DecimalFormat("#0.0000");
		DecimalFormat frme;
		frme = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
		frme.applyPattern("0.0E00");  

		String cs = ""; //getClass().getName() + ": ";   // optional class name
		String str = cs + "alpha="+frm.format(m_Net.getAlpha()) 
				   + ", epsilon="+frm.format(getEpsilon())
				   //+ ", lambda:" + m_Net.getLambda()
				   + ", "+getGameNum() + " games"
				   + " ("+frme.format(getNumLrnActions()) + " learn actions)";
		if (this.m_Net.getNumPlayers()==2) 
			str = str + ", (winX/tie/winO)=("+winXCounter+"/"+tieCounter+"/"+winOCounter+")";
		winXCounter=tieCounter=winOCounter=0;
		return str;
	}

	/**
	 * @return a short description of the n-tuple configuration
	 */
	protected String stringDescrNTuple() {
		if (m_ntPar.getRandomness()) {
			return "random "+m_ntPar.getNtupleNumber() +" "+m_ntPar.getNtupleMax()+"-tuple";
		} else {
			int mode = m_ntPar.getFixedNtupleMode();
			return "fixed n-tuple, mode="+mode;
		}
	}
	


	public void setTDParams(ParTD tdPar, int maxGameNum) {
		double alpha = tdPar.getAlpha();
		double alphaFinal = tdPar.getAlphaFinal();
		double alphaChangeRatio = Math
				.pow(alphaFinal / alpha, 1.0 / maxGameNum);
		m_Net.setAlpha(tdPar.getAlpha());
		m_Net.setAlphaChangeRatio(alphaChangeRatio);

//		NORMALIZE=tdPar.getNormalize();
		m_epsilon = tdPar.getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdPar.getEpsilonFinal()) / maxGameNum;

	}

	public void setNTParams(ParNT ntPar) {
//		TC=ntPar.getTc();					// obsolete, use now m_Net.getTc()
//		tcImm=ntPar.getTcImm();				// obsolete, use now m_Net.getTcImm()
		tcIn=ntPar.getTcInterval();			// obsolete (since m_Net.getTcImm() always true)
		
		m_Net.setTdAgt(this);				// needed when loading an older agent
	}

	public void setAlpha(double alpha) {
		m_Net.setAlpha(alpha);
	}

	public void setFinished(boolean m) {
		this.m_finished=m;
	}

	public void setEpiLengthStop(boolean m) {
		this.epiLengthStop=m;
	}

	public double getAlpha() {
		return m_Net.getAlpha();
	}

	public double getEpsilon() {
		return m_epsilon;
	}
	
	public double getGamma() {
		return m_tdPar.getGamma();
	}
	
	/**
	 * the number of calls to {@link NTuple4ValueFunc#update(StateObsWithBoardVector, int, int, double, double, boolean, boolean)}
	 */
	@Override
	public long getNumLrnActions() {
		return m_Net.getNumLearnActions();
	}

	public void resetNumLearnActions() {
		m_Net.resetNumLearnActions();
	}
	
	@Override
	public int getMoveCounter() {
		return m_counter;
	}

	public NTuple4ValueFunc getNTupleValueFunc() {
		return m_Net;
	}
	
	@Override
	public boolean isTrainable() { return true; }

	public ParTD getParTD() {
		return m_tdPar;
	}
	public ParNT getParNT() { return m_ntPar; }
	// getParOther() is in AgentBase

	public void setParTD(ParTD m_tdPar) {
		this.m_tdPar = new ParTD(m_tdPar);
	}
	public void setParNT(ParNT m_ntPar) {
		this.m_ntPar = new ParNT(m_ntPar);
	}

	public boolean getAFTERSTATE() {
		return m_ntPar.getAFTERSTATE();
	}
//	public boolean getLearnFromRM() {
//		return m_oPar.useLearnFromRM();
//	}

	public void incrementMoveCounter() {
		m_counter++;
	}
	
	/**
	 * *** This method currently works only for N &le; 2 players! ***
	 * <p>
	 * @param reward = r(s_{t+1}|p_{t+1}) with s_{t+1} = ns.nextSO
	 */
	@Override
	public void incrementWinCounters(double reward, NextState4 ns) {
		if (reward==0.0) tieCounter++;
		if (reward==-1.0 & ns.refer.getPlayer()==0) winXCounter++;		// ns.refer = s_{t}
		if (reward==-1.0 & ns.refer.getPlayer()==1) winOCounter++;
		if (reward== 1.0 & ns.refer.getPlayer()==0) winOCounter++;
		if (reward== 1.0 & ns.refer.getPlayer()==1) winXCounter++;
	}
	
	/**
	 * see {@link NTuple4ValueFunc#weightAnalysis(double[])}
	 */
	public double[][] weightAnalysis(double[] per) {
		System.out.println("Training duration: "+(double)getDurationTrainingMs()/1000+" sec");
		System.out.println("Eval for training: "+(double)getDurationEvaluationMs()/1000+" sec");

		return m_Net.weightAnalysis(per);
	}

}
