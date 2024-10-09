package params;

import tools.Types;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Panel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import games.Arena;

import javax.swing.JCheckBox;
import javax.swing.JFrame;

/**
 * 
 * Competition Options
 *
 */
public class OptionsComp extends JFrame {
	private static final long serialVersionUID = 1L;
	private JLabel lNumGames;
	private JLabel lSeed;
	private JLabel lVerbose;
	//private JLabel lPlayUntil;
	//private JLabel lOpponents;
	//private JLabel lFirstPlayer;
	//private JLabel lSecondPlayer;

	private JCheckBox cbUseSeed;
	private JCheckBox cbUseCurBoard;
	private JCheckBox cbLogValues;
	private JCheckBox cbswapPlayers;

	private JTextField tNumGames;
	private JTextField tSeed;
	private JTextField tVerbose;
	
	private Choice cFirstPlayer;
	private Choice cSecondPlayer;

	public OptionsComp(int competeNumber) {
		super("Competition Options");

		// now set in showOptionsComp to Types.GUI_WINCOMP_WIDTH,Types.GUI_WINCOMP_HEIGHT
//		setSize(320, 500);
//		setBounds(0, 0, 320, 500);

		setLayout(new BorderLayout(10, 10));
		add(new JLabel(" "), BorderLayout.SOUTH); 

		lNumGames = new JLabel("# games/competition");
		tNumGames = new JTextField(""+competeNumber, 3);

		cbUseSeed = new JCheckBox("Use seed");
		cbUseSeed.setSelected(false);

		lSeed = new JLabel("seed");
		tSeed = new JTextField(""+42, 1);
		//lPlayUntil = new JLabel("Stop Game after x Moves: ");

		lVerbose = new JLabel("verbose (0,1,2,3)");
		tVerbose = new JTextField(""+0, 3);

		cbUseCurBoard = new JCheckBox("Use current board");
		cbUseCurBoard.setSelected(true);

		cbLogValues = new JCheckBox("Log value tables of opponents");
		cbLogValues.setSelected(true);
		
		cbswapPlayers = new JCheckBox("Swap players (only MULTI)");
		cbswapPlayers.setSelected(false);

		cbUseSeed.addActionListener( e -> enableSeedPart() );

//		lOpponents = new JLabel("Opponents");
//		lFirstPlayer = new JLabel("First player");
//		lSecondPlayer = new JLabel("Second player");
//		cFirstPlayer = new Choice();
//		cSecondPlayer = new Choice();
//		cFirstPlayer.add("Agent X");
//		cFirstPlayer.add("Agent O");
//		//cFirstPlayer.add("Agent Eval");
//		cSecondPlayer.add("Agent X");
//		cSecondPlayer.add("Agent O");
//		//cSecondPlayer.add("Agent Eval");
//		cFirstPlayer.select(0);
//		cSecondPlayer.select(1);
		

		Panel p = new Panel();
		p.setLayout(new GridLayout2(0, 1, 5, 5));

//		p.add(lOpponents);
//		p.add(new Canvas());
//		
//		p.add(lFirstPlayer);
//		p.add(lSecondPlayer);
//		
//		p.add(cFirstPlayer);
//		p.add(cSecondPlayer);
		
		p.add(lNumGames);
		p.add(tNumGames);

		p.add(cbUseSeed);
		p.add(lSeed);
		p.add(tSeed);

		p.add(lVerbose);
		p.add(tVerbose);

		p.add(cbUseCurBoard);

		//p.add(lPlayUntil);

		p.add(cbLogValues);
		
		add(p);

		enableSeedPart();
		cbUseCurBoard.setEnabled(false);	// currently not used
		cbLogValues.setEnabled(false);		// currently not used
		pack();

		setVisible(false);
	}
	
	public void showOptionsComp(Arena arena,boolean isVisible) {
		if (arena.hasGUI()) {
			// place window winCompOptions on the right side of the XArenaTabs window
			this.setVisible(isVisible);
			int x = arena.m_xab.getX() + arena.m_xab.getWidth() + 8;
			int y = arena.m_xab.getLocation().y;
			if (arena.m_ArenaFrame!=null) {
				x = arena.m_ArenaFrame.getX() + arena.m_ArenaFrame.getWidth() + 1;
				y = arena.m_ArenaFrame.getY();
			}
			if (arena.m_tabs!=null) x += arena.m_tabs.getX() + 1;
			arena.m_xab.winCompOptions.setLocation(x,y);
			arena.m_xab.winCompOptions.setSize(Types.GUI_WINCOMP_WIDTH,Types.GUI_WINCOMP_HEIGHT);	
		}
	}

	public boolean swapPlayers() {
		return cbswapPlayers.isSelected();
	}

	// currently unused
	public boolean useCurBoard() {
		return cbUseCurBoard.isSelected();
	}

	// currently unused
	public boolean logValues() {
		return cbLogValues.isSelected();
	}

	public int getNumGames() {
		return Integer.parseInt(tNumGames.getText());
	}

	public long getSeed() { return Long.parseLong(tSeed.getText()); }

	public int getVerbose() {
		return Integer.parseInt(tVerbose.getText());
	}

	public boolean useSeed() { return cbUseSeed.isSelected(); }
	
	public void setNumGames(int competeNumber) {
		tNumGames.setText(""+competeNumber);
	}
	
	// currently unused
	public int getFirstPlayer() {
		return cFirstPlayer.getSelectedIndex();
	}
	
	// currently unused
	public int getSecondPlayer() {
		return cSecondPlayer.getSelectedIndex();
	}

	private void enableSeedPart() {
		if (useSeed()==false){
			lSeed.setEnabled(false);
			tSeed.setEnabled(false);
		}else{
			lSeed.setEnabled(true);
			tSeed.setEnabled(true);
		}
	}

}
