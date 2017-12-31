package games;

import games.StateObservation;
import games.Arena;

/**
 * Each class implementing interface GameBoard has the board game GUI. 
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * <p>
 * {@link GameBoard} has an internal object derived from {@link StateObservation} which represents the 
 * current game state. It can be retrieved with {@link #getStateObs()}, it can be reset and 
 * retrieved with {@link #getDefaultStartState()}, or a random start state can be retrieved 
 * with {@link #chooseStartState01()}.  
 * 
 * @author Wolfgang Konen, TH K�ln, Nov'16
 *
 */
public interface GameBoard {
	public void clearBoard(boolean boardClear, boolean vClear);
	//public void updateBoard();
	//public void updateBoard(StateObservation so);
	public void updateBoard(StateObservation so, boolean enableOccupiedCells
			, boolean showValueOnGameboard);
	public void showGameBoard(Arena arena,boolean alignToMain);
	public void toFront();
	/**
	 * Is an action requested from Arena (i.e. was human interaction done)?
	 * @return true if action from Arena is requested 
	 */
	public boolean isActionReq();
	public void setActionReq(boolean actionReq);
	public void enableInteraction(boolean enable);
	public StateObservation getStateObs();
	
	/**
	 * If logs and agents should be placed in a subdirectory (e.g. Hex: BoardSize), then
	 * this method returns a suitable string. If it returns {@code null}, then logs and 
	 * agents are placed in the {@code gameName} directory directly. 
	 * @return subdir string
	 */
	public String getSubDir();
	
	public Arena getArena();
	
	/**
	 * @return the 'empty-board' start state
	 */
	public StateObservation getDefaultStartState();
	/**
	 * @return a start state which is with probability 0.5 the empty board 
	 * 		start state and with probability 0.5 one of the possible one-ply 
	 * 		successors
	 */
	public StateObservation chooseStartState01();
}
