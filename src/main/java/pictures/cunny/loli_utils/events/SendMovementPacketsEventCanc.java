package pictures.cunny.loli_utils.events;

import meteordevelopment.meteorclient.events.Cancellable;

// This one can also be canceled, woohoo
public class SendMovementPacketsEventCanc extends Cancellable {
    public static final SendMovementPacketsEventCanc INSTANCE = new SendMovementPacketsEventCanc();

    public SendMovementPacketsEventCanc() {
        this.setCancelled(false);
    }

    public static SendMovementPacketsEventCanc get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
