package pictures.cunny.loli_utils.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;

// Not to be confused with meteors event, this can be cancelled, yay!
public class KeyboardInputEvent extends Cancellable {
    public static final KeyboardInputEvent INSTANCE = new KeyboardInputEvent();
    public Input input;
    public ClientInput clientInput;

    public KeyboardInputEvent() {
        this.setCancelled(false);
    }

    public static KeyboardInputEvent get(ClientInput clientInput) {
        INSTANCE.clientInput = clientInput;
        INSTANCE.input = null;
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
