package games;

abstract public class ObsNondetBase extends ObserverBase implements StateObsNondeterministic {
    public ObsNondetBase() {
        super();
    }

    public ObsNondetBase(ObsNondetBase other) {
        super(other);
    }

    /**
     * Default implementation for perfect-information games: the partial state that the player-to-move observes is
     * identical to {@code this}. <br>
     * Games with imperfect information have to override this method.
     *
     * @return the partial information state (here: identical to {@code this})
     */
    public StateObsNondeterministic partialState() { return this; }


}
