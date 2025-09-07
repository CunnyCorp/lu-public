package pictures.cunny.loli_utils.modules.rendering;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.events.ChunkLoadEvent;
import pictures.cunny.loli_utils.utility.MathUtils;
import pictures.cunny.loli_utils.utility.modules.DimensionCheck;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Highlighter extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Boolean> spawnerRender =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("activated-spawners")
                            .description("Render activated spawners.")
                            .defaultValue(false)
                            .build());
    public final Setting<Boolean> spawnerMessage =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("spawner-message")
                            .description("Send a chat message when a activated mob spawner is loaded.")
                            .visible(spawnerRender::get)
                            .defaultValue(true)
                            .build());
    public final Setting<Boolean> spawnerBeam =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("spawner-beam")
                            .description("Renders a beam for activated spawners.")
                            .visible(spawnerRender::get)
                            .defaultValue(false)
                            .build());
    private final Setting<Double> spawnerBeamSize =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("beam-size")
                            .description("How big to make beams")
                            .visible(() -> spawnerRender.get() && spawnerBeam.get())
                            .defaultValue(0.45)
                            .sliderRange(0.001, 1.5)
                            .build());
    private final Setting<SettingColor> spawnerColor =
            sgDefault.add(
                    new ColorSetting.Builder()
                            .name("spawner-color")
                            .visible(spawnerRender::get)
                            .defaultValue(new Color(114, 216, 232, 255))
                            .build());
    private final Setting<SettingColor> spawnerColorSec =
            sgDefault.add(
                    new ColorSetting.Builder()
                            .name("spawner-color-2")
                            .visible(spawnerRender::get)
                            .defaultValue(new Color(100, 200, 240, 255))
                            .build());
    private final Setting<Integer> spawnerSidesAlpha =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("spawner-sides-alpha")
                            .description("What to change the alpha to for the sides.")
                            .visible(spawnerRender::get)
                            .defaultValue(60)
                            .sliderRange(12, 255)
                            .build());
    public final Setting<Boolean> mapZoneRender =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("map-zones")
                            .description("Render zones of map boundaries.")
                            .defaultValue(false)
                            .build());
    public final Setting<DimensionCheck> mapZoneDimension =
            sgDefault.add(
                    new EnumSetting.Builder<DimensionCheck>()
                            .name("map-zone-dimension")
                            .description("Render map zones only in set dimensions.")
                            .visible(mapZoneRender::get)
                            .defaultValue(DimensionCheck.OW)
                            .build());
    private final Setting<Integer> mapZoneRange =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("map-zone-range")
                            .description("Amount of map zones around the player to highlight.")
                            .visible(mapZoneRender::get)
                            .defaultValue(4)
                            .sliderRange(1, 10)
                            .build());
    private final Setting<Integer> mapZoneYStart =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("map-zone-y-start")
                            .description("Minimum Y level to render the zone at.")
                            .visible(mapZoneRender::get)
                            .defaultValue(63)
                            .sliderRange(-64, 319)
                            .build());
    private final Setting<Integer> mapZoneYOffset =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("map-zone-y-offset")
                            .description("How far to offset off of the start.")
                            .visible(mapZoneRender::get)
                            .defaultValue(2)
                            .sliderRange(0, 16)
                            .build());
    private final Setting<SettingColor> mapColor =
            sgDefault.add(
                    new ColorSetting.Builder()
                            .name("map-color")
                            .visible(mapZoneRender::get)
                            .defaultValue(new Color(187, 115, 236, 255))
                            .build());
    private final Setting<Integer> mapZoneSidesAlpha =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("map-zone-sides-alpha")
                            .description("What to change the alpha to for the sides.")
                            .visible(mapZoneRender::get)
                            .defaultValue(60)
                            .sliderRange(12, 255)
                            .build());
    private final Setting<Integer> refreshDelay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("refresh-delay")
                            .description("How often to refresh lists, in ticks.")
                            .defaultValue(4)
                            .sliderRange(0, 10)
                            .build());
    private final List<double[]> mapQuads = new ArrayList<>();
    private final List<double[]> spawners = new ArrayList<>();
    private int ticks = 0;
    private double beamRot = 1;
    private boolean flipBeam = false;

    public Highlighter() {
        super(LoliUtilsMeteor.CATEGORY, "highlighter", "Highlights certain things in the world based on configuration.");
    }

    @EventHandler
    public void onChunkEvent(ChunkLoadEvent event) {
        Map<BlockPos, BlockEntity> blockEntities = mc.level.getChunk(event.x, event.z).getBlockEntities();

        for (var entry : blockEntities.entrySet()) {
            if (spawnerRender.get() && isSpawnerActivated(entry.getValue())) {
                spawners.add(new double[]{entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()});
                if (spawnerMessage.get()) {
                    info("Activated spawner at: %s", entry.getKey().toShortString());
                    mc.getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.literal(title), Component.literal("Found an activated spawner at: " + entry.getKey().toShortString())));
                }
            }
        }
    }

    @EventHandler
    public void onTickPre(TickEvent.Pre event) {
        assert mc.player != null;

        if (beamRot < -1) {
            flipBeam = true;
        } else if (beamRot > 1) {
            flipBeam = false;
        }

        if (flipBeam) {
            beamRot += 0.022;
        } else {
            beamRot -= 0.022;
        }

        if (ticks < refreshDelay.get()) {
            ticks++;
            return;
        }

        if (spawnerRender.get()) {
            for (var pos : new ArrayList<>(spawners)) {
                assert mc.level != null;
                if (!isSpawnerActivated(mc.level.getBlockEntity(new BlockPos((int) pos[0], (int) pos[1], (int) pos[2])))) {
                    spawners.remove(pos);
                }
            }
        }

        ticks = 0;

        if (!mapQuads.isEmpty()) {
            mapQuads.clear();
        }

        try {
            if (mapZoneRender.get() && mapZoneDimension.get().check.call()) {
                double currentMapQuadX = MathUtils.toMapQuad(mc.player.getX());
                double currentMapQuadZ = MathUtils.toMapQuad(mc.player.getZ());

                for (int x = -mapZoneRange.get(); x <= mapZoneRange.get(); x++) {
                    for (int z = -mapZoneRange.get(); z <= mapZoneRange.get(); z++) {
                        mapQuads.add(new double[]{(currentMapQuadX + x) * 128, (currentMapQuadZ + z) * 128});
                    }
                }
            }
        } catch (Exception e) {
            LoliUtilsMeteor.LOGGER.info("Ran into an exception while updating the map quadrant list.", e);
        }
    }

    @EventHandler
    public void render3DEvent(Render3DEvent event) {
        assert mc.player != null;

        if (mapZoneRender.get()) {
            for (double[] quad : mapQuads) {
                renderMapQuad(event.renderer, quad[0], quad[1]);
            }
        }

        if (spawnerRender.get()) {
            for (double[] pos : spawners) {
                renderSpawner(event.renderer, pos[0], pos[1], pos[2]);
            }
        }
    }

    public boolean isSpawnerActivated(BlockEntity blockEntity) {
        if (mc.level == null) {
            return true;
        }

        if (!(blockEntity instanceof SpawnerBlockEntity mobSpawner)) {
            return false;
        }

        if (mobSpawner.getSpawner().spawnDelay == 20) {
            LoliUtilsMeteor.LOGGER.info("Spawn Delay is 20 for {}", blockEntity.getBlockPos().toShortString());
            return false;
        }

        return true;
    }

    public void renderMapQuad(Renderer3D renderer, double x, double z) {
        int origAlpha = mapColor.get().a;

        renderer.boxLines(x - 64, mapZoneYStart.get(), z - 64, x + 64, mapZoneYStart.get() + mapZoneYOffset.get(), z + 64, mapColor.get(), 0);
        mapColor.get().a(mapZoneSidesAlpha.get());
        renderer.boxSides(x - 64, mapZoneYStart.get(), z - 64, x + 64, mapZoneYStart.get() + mapZoneYOffset.get(), z + 64, mapColor.get(), 0);
        mapColor.get().a(origAlpha);
    }

    public void renderSpawner(Renderer3D renderer, double x, double y, double z) {
        int origAlpha = spawnerColor.get().a;

        List<Integer> lineMeshes = new ArrayList<>();

        if (spawnerBeam.get()) {
            spawnerColor.get().a(spawnerSidesAlpha.get());

            x += 0.5;
            z += 0.5;

            var lastX = -5;
            var lastZ = -5;
            for (int lineX = -5; lineX < 5; lineX++) {
                for (int lineZ = -5; lineZ < 5; lineZ++) {
                    lineMeshes.add(renderer.triangles.vec3(x + (spawnerBeamSize.get() / lineX) - (0.1 * beamRot), y, z - (spawnerBeamSize.get() / lineZ) + (0.1 * beamRot)).color(spawnerColor.get()).next());
                    lineMeshes.add(renderer.triangles.vec3(x - (spawnerBeamSize.get() / lastX) + (0.1 * beamRot), y, z + (spawnerBeamSize.get() / lastZ) - (0.1 * beamRot)).color(spawnerColorSec.get()).next());
                    lineMeshes.add(renderer.triangles.vec3(x - (spawnerBeamSize.get() / lastX) + (0.1 * beamRot), y + 256, z + (spawnerBeamSize.get() / lastZ) - (0.1 * beamRot)).color(spawnerColorSec.get()).next());
                    lineMeshes.add(renderer.triangles.vec3(x + (spawnerBeamSize.get() / lineX) - (0.1 * beamRot), y + 256, z - (spawnerBeamSize.get() / lineZ) + (0.1 * beamRot)).color(spawnerColor.get()).next());

                    lastX = lineX;
                    lastZ = lineZ;
                }
            }

            Iterator<Integer> iterator = lineMeshes.iterator();

            while (iterator.hasNext()) {
                renderer.triangles.quad(iterator.next(), iterator.next(), iterator.next(), iterator.next());
            }

            spawnerColor.get().a(origAlpha);
        } else {
            renderer.boxLines(x, y, z, x + 1, y + 1, z + 1, spawnerColor.get(), 0);
            spawnerColor.get().a(spawnerSidesAlpha.get());
            renderer.boxSides(x, y, z, x + 1, y + 1, z + 1, spawnerColor.get(), 0);
            spawnerColor.get().a(origAlpha);
        }
    }
}
