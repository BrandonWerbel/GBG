package controllers.ReplayBuffer.Transition;

import controllers.TD.ntuple4.NextState4;
import games.StateObservation;
import tools.ScoreTuple;

public interface ITransition {

 public void setPlayer(int player);
 public int getPlayer();

 public void setNextState(NextState4 ns);
 public NextState4 getNextState4();

 public void setSLast(StateObservation sLast);
 public StateObservation getSLast();

 public void setRLast(ScoreTuple rLast);
 public ScoreTuple getRLast();
 public double getPlayerRLast();

 public void setR(ScoreTuple R);
 public ScoreTuple getR();

 public void setIsFinalTransition(int b);
 public int isFinalTransition();


}

