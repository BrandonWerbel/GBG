package controllers.MCTSWrapper.passStates;

import controllers.MCTSWrapper.utils.StateObservationExtensions;
import games.StateObservation;
import tools.Types;

/**
 * Represents a real action that is applyable to a game state.
 * For this use case an action of the type Types.ACTIONS is wrapped.
 */
public final class RegularAction implements ApplyableAction {
    private final Types.ACTIONS action;

    public RegularAction(final Types.ACTIONS action) {
        this.action = action;
    }

    /**
     * Applies a regular action to a passed StateObservation.
     * <p>
     * Since the GBG framework skips game states in which a
     * player has to pass, this method reverses the skip and
     * returns the state in which the player has to pass.
     *
     * @param so StateObservation instance to which the action should be applied.
     * @return A new StateObservation instance which results from applying
     * the action to the given StateObservation.
     * So the given StateObservation instance remains unchanged.
     */
    @Override
    public StateObservation applyTo(final StateObservation so) {
        final var stateCopy = so.copy();
        stateCopy.advance(action);

        return stateCopy.getPlayer() == so.getPlayer() // If it is still the same player's turn after the action has been performed,
                                                       // a passing situation has occurred that has been skipped.
            ? StateObservationExtensions.swapCurrentPlayer(stateCopy)
            : stateCopy;
    }

    /**
     * An action is identified by the board position it addresses.
     */
    @Override
    public int getId() {
        return action.toInt();
    }
}
