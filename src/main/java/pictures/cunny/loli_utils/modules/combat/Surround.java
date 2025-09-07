package pictures.cunny.loli_utils.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.modules.LoliModule;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.CrystalUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;
import pictures.cunny.loli_utils.utility.render.LoliRendering;

import java.util.*;

public class Surround extends LoliModule {
    private final Map<BlockPos, VisualType> renderMap = Collections.synchronizedMap(new HashMap<>());
    private final List<BlockPos> savedBlocks = Collections.synchronizedList(new ArrayList<>());

    // Basic
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Double> placeDistance =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("place-distance")
                            .description("The max distance to place blocks.")
                            .defaultValue(3.75)
                            .sliderRange(3.2, 5.0)
                            .build());
    public final Setting<Integer> blocksPerTick = sgDefault.add(new IntSetting.Builder()
            .name("blocks-per-tick")
            .description("How many blocks to place per tick.")
            .sliderRange(1, 8)
            .defaultValue(2)
            .build());
    public final Setting<Integer> tickDelay = sgDefault.add(new IntSetting.Builder()
            .name("tick-delay")
            .description("Delay in ticks per cycle.")
            .sliderRange(0, 10)
            .defaultValue(0)
            .build());
    private final Setting<List<Item>> fallback = sgDefault.add(new ItemListSetting.Builder()
            .name("fallback-blocks")
            .description("The blocks to fall back to if you run out of obsidian.")
            .defaultValue(List.of(Items.NETHERITE_BLOCK))
            .filter(CrystalUtils.FALLBACK_BLOCKS::contains)
            .build());


    // Protection
    private final SettingGroup sgProtect = settings.createGroup("Protection");
    public final Setting<Boolean> protectUnder = sgProtect.add(new BoolSetting.Builder()
            .name("protect-under")
            .description("Places obsidian under the primary blocks if they are vulnerable.")
            .defaultValue(true)
            .build());
    public final Setting<Boolean> noConflict = sgProtect.add(new BoolSetting.Builder()
            .name("no-conflict")
            .description("Breaks crystals that could prevent the block from being replaced.")
            .defaultValue(true)
            .build());
    public final Setting<Boolean> autoReplace = sgProtect.add(new BoolSetting.Builder()
            .name("auto-replace")
            .description("Attempts to replace the crystal with obsidian.")
            .defaultValue(true)
            .visible(noConflict::get)
            .build());
    public final Setting<Boolean> saveReplaced = sgProtect.add(new BoolSetting.Builder()
            .name("save-replaced")
            .description("Saves the position and makes sure they're not broken.")
            .defaultValue(true)
            .visible(() -> autoReplace.get() && autoReplace.isVisible())
            .build());
    public final Setting<Boolean> markConflicted = sgProtect.add(new BoolSetting.Builder()
            .name("mark-as-saved")
            .description("Saves the position and makes sure they're not broken.")
            .defaultValue(true)
            .visible(() -> autoReplace.get() && autoReplace.isVisible() && saveReplaced.isVisible())
            .build());
    public final Setting<Integer> crystalTickDelay = sgProtect.add(new IntSetting.Builder()
            .name("break-delay")
            .description("The ticks between breaking crystals.")
            .sliderRange(0, 40)
            .defaultValue(3)
            .visible(noConflict::get)
            .build());

    // Toggles
    private final SettingGroup sgToggles = settings.createGroup("Toggles");
    public final Setting<Boolean> onGround = sgToggles.add(new BoolSetting.Builder()
            .name("on-ground")
            .description("Only place blocks on the ground.")
            .defaultValue(true)
            .build());
    public final Setting<Boolean> useItem = sgToggles.add(new BoolSetting.Builder()
            .name("use-item")
            .description("Pauses when using an item.")
            .defaultValue(true)
            .build());


    // Render
    private final SettingGroup sgRender = settings.createGroup("Render");
    public final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
            .name("render")
            .description("Render block placements.")
            .defaultValue(true)
            .build());
    public final Setting<SettingColor> placedColor = sgRender.add(new ColorSetting.Builder()
            .name("placed")
            .defaultValue(new Color(0, 128, 0, 60))
            .build());
    public final Setting<SettingColor> waitingColor = sgRender.add(new ColorSetting.Builder()
            .name("waiting")
            .defaultValue(new Color(255, 165, 0, 60))
            .build());
    public final Setting<SettingColor> conflictingColor = sgRender.add(new ColorSetting.Builder()
            .name("conflicting")
            .defaultValue(new Color(255, 0, 0, 60))
            .build());
    public final Setting<SettingColor> failColor = sgRender.add(new ColorSetting.Builder()
            .name("fail")
            .defaultValue(new Color(255, 0, 0, 60))
            .build());
    private BlockPos currentHole = BlockPos.ZERO;
    private int updateTicks = 0;
    private int breakTicks = 0;

    public Surround() {
        super(LoliUtilsMeteor.CATEGORY, "loli-surround", "Surrounds your feet with blocks.");
        this.timedTicks = 2;
    }

    @Override
    public void safeOnActivate() {
        this.updateTicks = 0;
        this.renderMap.clear();
    }

    @Override
    public void update() {
        if (updateTicks > 0) {
            updateTicks--;
            return;
        }

        if (breakTicks > 0) breakTicks--;
        placeBlocks();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get()) return;
        try {
            synchronized (renderMap) {
                for (Map.Entry<BlockPos, VisualType> entry : renderMap.entrySet()) {

                    Color color = switch (entry.getValue()) {
                        case PLACED -> placedColor.get();
                        case WAITING -> waitingColor.get();
                        case CONFLICTING -> conflictingColor.get();
                        case FAIL -> failColor.get();
                    };

                    LoliRendering.renderBlock(event.renderer, entry.getKey(), color);
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }
    }

    public void placeBlocks() {
        if (mc.player == null) return;
        if (onGround.get() && !mc.player.onGround()) return;
        if (useItem.get() && mc.player.isUsingItem()) return;

        var ref = new Object() {
            int blocks = 0;
        };
        updateTicks = tickDelay.get();

        BlockPos original = mc.player.blockPosition();
        if (!currentHole.equals(original)) {
            currentHole = original;
            savedBlocks.clear();
        }

        try {
            synchronized (renderMap) {
                renderMap.clear();
            }
        } catch (ConcurrentModificationException ignored) {
        }

        boolean isNoConflict = noConflict.get();
        boolean isMarkConflicted = markConflicted.isVisible() && markConflicted.get();
        boolean isAutoReplace = autoReplace.get();


        for (Direction direction : BlockUtils.HORIZONTALS) {
            BlockPos offset = original.relative(direction);
            if (isNoConflict) {
                List<EndCrystal> imposedCrystals = BlockUtils.imposedCrystals(offset);
                while (!imposedCrystals.isEmpty()) {
                    EndCrystal crystal = imposedCrystals.removeFirst();

                    BlockPos pos = crystal.blockPosition();

                    if (isMarkConflicted) {
                        if (!savedBlocks.contains(pos))
                            savedBlocks.add(pos);
                    }

                    if (breakTicks <= 0) {
                        PacketUtils.send(ServerboundInteractPacket.createAttackPacket(crystal, false));
                        PacketUtils.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                        breakTicks = crystalTickDelay.get();
                    }

                    if (isAutoReplace) {
                        FindItemResult itemResult = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == Items.OBSIDIAN);
                        if (!itemResult.found())
                            itemResult = InvUtils.findInHotbar(stack -> fallback.get().contains(stack.getItem()));
                        if (itemResult.found()) {
                            if (saveReplaced.get() && !savedBlocks.contains(pos))
                                savedBlocks.add(pos);
                            if (BlockUtils.placeBlock(itemResult, crystal.blockPosition(), placeDistance.get()))
                                ref.blocks++;
                        }
                    }
                }
            }

            if (!BlockUtils.canExplode(offset)) {
                renderMap.put(offset, VisualType.PLACED);
                continue;
            }

            if (ref.blocks >= blocksPerTick.get()) {
                renderMap.put(offset, VisualType.WAITING);
                continue;
            }

            if (BlockUtils.isReplaceable(mc.player.blockPosition().relative(Direction.DOWN))) {
                if (place(mc.player.blockPosition().relative(Direction.DOWN))) ref.blocks++;
            }

            if (!CrystalUtils.isSurroundMissing()) continue;

            if (ref.blocks >= blocksPerTick.get()) {
                break;
            }

            if (place(offset)) ref.blocks++;

            if (protectUnder.get()) {
                BlockPos underPos = offset.relative(Direction.DOWN);
                if (place(underPos)) {
                    if (saveReplaced.get() && !savedBlocks.contains(underPos)) {
                        savedBlocks.add(underPos);
                    }
                    ref.blocks++;
                }
            }
        }

        if (saveReplaced.get() && ref.blocks < blocksPerTick.get()) {
            synchronized (savedBlocks) {
                for (BlockPos pos : savedBlocks) {
                    if (!BlockUtils.canExplode(pos)) {
                        renderMap.put(pos, VisualType.PLACED);
                        continue;
                    }

                    if (ref.blocks >= blocksPerTick.get()) {
                        renderMap.put(pos, VisualType.WAITING);
                        continue;
                    }

                    if (place(pos)) ref.blocks++;
                }
            }
        }
    }

    public boolean place(BlockPos pos) {
        if (BlockUtils.canPlace(pos)) {
            FindItemResult itemResult = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == Items.OBSIDIAN);
            if (!itemResult.found())
                itemResult = InvUtils.findInHotbar(itemStack -> fallback.get().contains(itemStack.getItem()));
            if (!itemResult.found()) {
                renderMap.put(pos, VisualType.FAIL);
                return false;
            }

            if (BlockUtils.placeBlock(itemResult, pos, placeDistance.get())) {
                renderMap.put(pos, VisualType.PLACED);
                return true;
            } else {
                if (BlockUtils.hasEntitiesInside(pos)) {
                    renderMap.put(pos, VisualType.CONFLICTING);
                } else {
                    renderMap.put(pos, VisualType.FAIL);
                }
            }
        } else {
            if (BlockUtils.hasEntitiesInside(pos)) {
                renderMap.put(pos, VisualType.CONFLICTING);
            } else {
                renderMap.put(pos, VisualType.FAIL);
            }
        }
        return false;
    }

    public enum VisualType {
        PLACED,
        WAITING,
        CONFLICTING,
        FAIL
    }
}
