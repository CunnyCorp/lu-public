package pictures.cunny.loli_utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.elements.MeteorTextHud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.meteordev.starscript.Starscript;
import org.meteordev.starscript.value.Value;
import org.meteordev.starscript.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pictures.cunny.loli_utils.commands.ChangePlayerEffects;
import pictures.cunny.loli_utils.commands.MapartCopy;
import pictures.cunny.loli_utils.events.TimedEvent;
import pictures.cunny.loli_utils.modules.FunnyX;
import pictures.cunny.loli_utils.modules.HoldPackets;
import pictures.cunny.loli_utils.modules.PacketPlace;
import pictures.cunny.loli_utils.modules.combat.Surround;
import pictures.cunny.loli_utils.modules.misc.*;
import pictures.cunny.loli_utils.modules.movement.*;
import pictures.cunny.loli_utils.modules.movement.elytrafly.*;
import pictures.cunny.loli_utils.modules.misc.SimpleNuker;
import pictures.cunny.loli_utils.modules.printer.Printer;
import pictures.cunny.loli_utils.modules.printer.PrinterUtils;
import pictures.cunny.loli_utils.modules.printer.movesets.MoveSets;
import pictures.cunny.loli_utils.modules.rendering.*;
import pictures.cunny.loli_utils.deepseek.DeepSeek;
import pictures.cunny.loli_utils.utility.Dependencies;
import pictures.cunny.loli_utils.utility.MathUtils;
import pictures.cunny.loli_utils.utility.SecretHelper;
import pictures.cunny.loli_utils.utility.modules.McDataCache;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;
import pictures.cunny.loli_utils.utility.render.SpecialEffects;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class LoliUtilsMeteor extends MeteorAddon {
    public static boolean NO_SWING = false;
    public static final Random RANDOM = new Random();
    public static final Logger LOGGER = LoggerFactory.getLogger("Loli Utils");
    public static final Category CATEGORY = new Category("Loli Utils", Items.MILK_BUCKET.getDefaultInstance());
    // You shouldn't touch this at all me.
    public static SecretHelper SECRET_HELPER = new SecretHelper();
    public static final TimerTask TIMED_EVENT_TASK = new TimerTask() {
        @Override
        public void run() {
            try {
                MeteorClient.EVENT_BUS.post(TimedEvent.INSTANCE);
            } catch (RuntimeException e) {
                LOGGER.error("LoliUtils - Timed Event Exception.", e);
            }
        }
    };
    public static DeepSeek deepSeek;


    public static void postInit() {
        mc = Minecraft.getInstance();
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Loli Utils loading :3");

        Timer timer = new Timer();
        // You have to handles timing in the modules!!!!
        timer.scheduleAtFixedRate(LoliUtilsMeteor.TIMED_EVENT_TASK, 10, 10);

        Modules.get().add(new CumDripping());
        Modules.get().add(new BoxESP());
        Modules.get().add(new PenisEsp());

        Modules.get().add(new Ripples());

        Modules.get().add(new Surround());
        Modules.get().add(new BookModifier());

        // Modules
        Modules.get().add(new AutoAnimal());
        Modules.get().add(new BooksPlus());
        Modules.get().add(new ElytraSwap());
        Modules.get().add(new FunnyX());
        Modules.get().add(new HoldPackets());
        Modules.get().add(new PacketPlace());


        // 0 Movement 0 \\
        Modules.get().add(new BaritoneTweaks());
        Modules.get().add(new ControlFly());
        Modules.get().add(new FastMove());
        Modules.get().add(new GrimLagBack());
        Modules.get().add(new OnlyControl());
        Modules.get().add(new NoInputs());
        Modules.get().add(new NoJumpDelay());
        Modules.get().add(new Scaffold());
        Modules.get().add(new YStabilizer());

        // 0 Misc 0 \\
        Modules.get().add(new CaseSpoof());
        Modules.get().add(new UnSequenced());
        Modules.get().add(new AutoWither());
        Modules.get().add(new MapFiller());
        Modules.get().add(new AutoDrop());
        Modules.get().add(new AutoRename());
        Modules.get().add(new ExampleModule());
        Modules.get().add(new LoliCrypt());

        // 0 Stash Moving 0 \\
        Modules.get().add(new AutoDump());
        Modules.get().add(new ChestAura());
        Modules.get().add(new EChestAura());
        Modules.get().add(new InventoryFull());
        Modules.get().add(new StashMover());

        // 0 Rendering 0 \\
        Modules.get().add(new Highlighter());
        Modules.get().add(new VisualInteract());
        Modules.get().add(new HeadUpdater());

        // 0 Dependant 0 \\
        if (Dependencies.LITEMATICA.isLoaded()) {
            Modules.get().add(new Printer());
            Modules.get().add(new SimpleNuker());
        }

        // Commands
        Commands.add(new MapartCopy());
        Commands.add(new ChangePlayerEffects());

        MeteorStarscript.ss.set(
                "map_pos",
                new ValueMap()
                        .set(
                                "x",
                                () ->
                                        Value.number(
                                                mc.player == null ? 0 : MathUtils.toMapQuad(mc.player.getBlockX())))
                        .set(
                                "z",
                                () ->
                                        Value.number(
                                                mc.player == null ? 0 : MathUtils.toMapQuad(mc.player.getBlockZ()))));

        MeteorStarscript.ss.set(
                "is_day",
                (starscript, i) ->
                        Value.bool(
                                mc.level != null
                                        && Math.floor(((double) mc.level.getTimeOfDay(0f) / 12000L) % 2) == 0));

        MeteorStarscript.ss.set("get_used", LoliUtilsMeteor::getUsed);

        MeteorStarscript.ss.set(
                "printer",
                new ValueMap()
                        .set(
                                "rp",
                                () ->
                                        Value.number(
                                                PrinterUtils.PRINTER == null ? -1 : PrinterUtils.PRINTER.interestPoint
                                        )
                        )
        );

        MeteorTextHud.INFO.addPreset(
                "Map Quad",
                textHud -> {
                    textHud.text.set("#1{map_pos.x}, {map_pos.z}");
                    textHud.updateDelay.set(0);
                });

        MeteorTextHud.INFO.addPreset(
                "Printer Interest Point",
                textHud -> {
                    textHud.text.set("I.P: #1{printer.rp}");
                    textHud.updateDelay.set(0);
                });

        SpecialEffects.init();

        // Don't remove this.
        if (Dependencies.LITEMATICA.isLoaded()) {
            MeteorClient.EVENT_BUS.unsubscribe(MoveSets.CONST_EFLY.movement);
        }
    }

    private static Value getUsed(Starscript ss, int argCount) {
        if (argCount != 1) {
            ss.error("Only 1 arg is needed.");
        }

        if (mc.player == null) return Value.number(0);

        String str = ss.popString("First arg needs to be a string.");

        if (str.equals("First arg needs to be a string.")) {
            return Value.number(0);
        }

        Item item = McDataCache.getItem(str);

        PacketUtils.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));

        if (!Stats.ITEM_USED.contains(item)) ss.error("That item does not exist.");

        return Value.number(mc.player.getStats().getValue(Stats.ITEM_USED.get(item)));
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "pictures.cunny.loli_utils";
    }
}
