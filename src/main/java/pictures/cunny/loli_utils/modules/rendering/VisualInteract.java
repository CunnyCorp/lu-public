package pictures.cunny.loli_utils.modules.rendering;

import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.render.LoliRendering;
import pictures.cunny.loli_utils.utility.render.RenderWrap;
import pictures.cunny.loli_utils.utility.render.VIRenderWrap;

import java.util.ArrayList;
import java.util.List;

public class VisualInteract extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Boolean> storageInteract =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("storage")
                            .description("Render a box on storage blocks when interacted with.")
                            .defaultValue(false)
                            .build());
    public final Setting<List<BlockEntityType<?>>> storageFilter =
            sgDefault.add(
                    new StorageBlockListSetting.Builder()
                            .name("storage-types")
                            .description("Only render the storage selected.")
                            .visible(storageInteract::get)
                            .defaultValue(StorageBlockListSetting.STORAGE_BLOCKS)
                            .build());
    private final Setting<SettingColor> storageColor =
            sgDefault.add(
                    new ColorSetting.Builder()
                            .name("storage-color")
                            .visible(storageInteract::get)
                            .defaultValue(new Color(114, 216, 232, 67))
                            .build());
    private final Setting<Integer> storageFadeTime =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("storage-fade-time")
                            .description("How fast to fade storage out.")
                            .visible(storageInteract::get)
                            .defaultValue(160)
                            .sliderRange(20, 1000)
                            .build());
    private final Setting<Integer> persistentStorage =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("persistent-storage")
                            .description("How many storage blocks to keep persistently rendered, 0 for none.")
                            .visible(storageInteract::get)
                            .defaultValue(2)
                            .sliderRange(0, 16)
                            .build());
    public final Setting<Boolean> breakInteract =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("break")
                            .description("Render a box when you start breaking a block.")
                            .defaultValue(false)
                            .build());
    private final Setting<SettingColor> breakColor =
            sgDefault.add(
                    new ColorSetting.Builder()
                            .name("break-color")
                            .visible(breakInteract::get)
                            .defaultValue(new Color(114, 216, 232, 67))
                            .build());
    private final Setting<Integer> breakFadeTime =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("break-fade-time")
                            .description("How fast to fade break blocks out.")
                            .visible(breakInteract::get)
                            .defaultValue(160)
                            .sliderRange(20, 1000)
                            .build());

    private final List<RenderWrap> lastInteractedContainers = new ArrayList<>();
    private final List<RenderWrap> lastBreakingBlocks = new ArrayList<>();
    private final List<RenderWrap> persistentInteractedContainers = new ArrayList<>();

    public VisualInteract() {
        super(LoliUtilsMeteor.CATEGORY, "visual-interact", "Visualizes certain interactions.");
    }

    @EventHandler
    public void onInteractBlock(InteractBlockEvent event) {
        if (event.result.getType() == HitResult.Type.BLOCK) {
            if (storageInteract.get()) {
                if (mc.level.getBlockEntity(event.result.getBlockPos()) instanceof BlockEntity) {
                    LoliRendering.removeAnyDupes(lastInteractedContainers, event.result.getBlockPos());
                    LoliRendering.removeAnyDupes(lastBreakingBlocks, event.result.getBlockPos());
                    LoliRendering.removeAnyDupes(persistentInteractedContainers, event.result.getBlockPos());

                    if (persistentStorage.get() > 0) {
                        if (persistentInteractedContainers.size() >= persistentStorage.get()) {
                            lastInteractedContainers.add(persistentInteractedContainers.getFirst());
                            persistentInteractedContainers.removeFirst();
                        }

                        persistentInteractedContainers.add(new VIRenderWrap(event.result.getBlockPos(), storageFadeTime.get(), 0, 0, storageColor.get()));
                    } else {
                        lastInteractedContainers.add(new VIRenderWrap(event.result.getBlockPos(), storageFadeTime.get(), 0, 0, storageColor.get()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundPlayerActionPacket packet) {
            switch (packet.getAction()) {
                case ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK, START_DESTROY_BLOCK -> {
                    if (breakInteract.get()) {
                        LoliRendering.removeAnyDupes(lastInteractedContainers, packet.getPos());
                        LoliRendering.removeAnyDupes(lastBreakingBlocks, packet.getPos());
                        LoliRendering.removeAnyDupes(persistentInteractedContainers, packet.getPos());

                        lastBreakingBlocks.add(new RenderWrap(packet.getPos(), breakFadeTime.get(), 0, 0, breakColor.get()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTickPre(TickEvent.Pre event) {
        assert mc.player != null;

        for (RenderWrap wrap : new ArrayList<>(persistentInteractedContainers)) {
            if (!BlockUtils.isNotAir(wrap.blockPos()) && wrap.blockPos().closerThan(mc.player.getOnPos(), 32)) {
                LoliRendering.removeAnyDupes(lastInteractedContainers, wrap.blockPos());
                LoliRendering.removeAnyDupes(lastBreakingBlocks, wrap.blockPos());
                LoliRendering.removeAnyDupes(persistentInteractedContainers, wrap.blockPos());
            }
        }

        LoliRendering.tickFadeTime(lastInteractedContainers);

        LoliRendering.tickFadeTime(lastBreakingBlocks);
    }

    @EventHandler
    public void render3DEvent(Render3DEvent event) {
        assert mc.player != null;

        if (!lastInteractedContainers.isEmpty()) {
            for (RenderWrap wrap : lastInteractedContainers) {
                LoliRendering.renderRetractingCube(event.renderer, wrap);
            }
        }

        if (!persistentInteractedContainers.isEmpty()) {
            for (RenderWrap wrap : persistentInteractedContainers) {
                LoliRendering.renderRetractingCube(event.renderer, wrap);
            }
        }

        if (!lastBreakingBlocks.isEmpty()) {
            for (RenderWrap wrap : lastBreakingBlocks) {
                LoliRendering.renderRetractingCube(event.renderer, wrap);
            }
        }
    }
}
