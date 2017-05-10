package controllers.MC;


import java.util.Random;

import games.StateObservation;

/**
 * Created by Johannes on 07.11.2016.
 */
public class RandomSearch{
    private Random random = new Random();
    private int rolloutDepth = MCAgentConfig.ROLLOUTDEPTH;

    public void startAgent(StateObservation sob, int depth) {

        for(int i = 0; i < depth; i++) {
            if (sob.getNumAvailableActions() > 0) {
                sob.advance(sob.getAvailableActions().get(random.nextInt(sob.getNumAvailableActions())));
            }
            else {
                rolloutDepth = i+1;
                break;
            }
        }
    }

    public int getRolloutDepth() {
        return  rolloutDepth;
    }
}
