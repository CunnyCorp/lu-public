package pictures.cunny.loli_utils.events;

import meteordevelopment.meteorclient.events.Cancellable;

// Not to be confused with meteors event, this can be cancelled, yay!
public class TickPlayerMovementEvent extends Cancellable {
    public static final TickPlayerMovementEvent INSTANCE = new TickPlayerMovementEvent();

    public TickPlayerMovementEvent() {
        this.setCancelled(false);
    }

    public static TickPlayerMovementEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
