package pictures.cunny.loli_utils.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.modules.PacketPlace;
import pictures.cunny.loli_utils.modules.misc.SimpleNuker;
import pictures.cunny.loli_utils.modules.misc.UnSequenced;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow
    protected abstract void startPrediction(ClientLevel clientLevel, PredictiveAction predictiveAction);

    @Shadow
    public abstract boolean destroyBlock(BlockPos blockPos);

    @Inject(method = "handleInventoryMouseClick", at = @At("TAIL"))
    public void handleInventoryMouseClick(int i, int j, int k, ClickType clickType, Player player, CallbackInfo ci) {
        LoliUtilsMeteor.LOGGER.info("Clicked slot: {} - {}", j, player.containerMenu.slots.size());
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void interactBlock(
            LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!Modules.get().isActive(PacketPlace.class)) return;
        assert mc.getConnection() != null;
        mc.getConnection().send(new ServerboundSwingPacket(interactionHand));
        mc.getConnection().send(new ServerboundUseItemOnPacket(interactionHand, blockHitResult, 0));
        cir.setReturnValue(InteractionResult.PASS);
    }

    @Redirect(method = "startPrediction", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/prediction/BlockStatePredictionHandler;currentSequence()I"))
    public int interactBlock(
            BlockStatePredictionHandler instance) {
        if (Modules.get().isActive(UnSequenced.class)) {
            return 0;
        }

        return instance.currentSequence();
    }

    @Redirect(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", ordinal = 1))
    public void interactBlock(
            MultiPlayerGameMode instance, ClientLevel clientLevel, PredictiveAction predictiveAction) {
        if (Modules.get().isActive(SimpleNuker.class)) {
            SimpleNuker module = Modules.get().get(SimpleNuker.class);

            PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, module.lastTargetedBlock.atY(1948), Direction.DOWN));
            destroyBlock(module.lastTargetedBlock);
            PacketUtils.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, module.lastTargetedBlock, Direction.UP, 0));
            module.skippedBlocks.add(new SimpleNuker.SkippedBlock(System.currentTimeMillis(), module.lastTargetedBlock.immutable()));
            return;
        }

        startPrediction(clientLevel, predictiveAction);
    }
}
