package pictures.cunny.loli_utils.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.modules.misc.LoliCrypt;

@Mixin(value = ChatComponent.class)
public class ChatComponentMixin {
    @Inject(method = "logChatMessage", at = @At("HEAD"), cancellable = true)
    public void logChatMessages(GuiMessage guiMessage, CallbackInfo ci) {
        if (Modules.get().isActive(LoliCrypt.class) && Modules.get().get(LoliCrypt.class).logging.get() == LoliCrypt.Logging.None) {
            ci.cancel();
        }
    }
}
