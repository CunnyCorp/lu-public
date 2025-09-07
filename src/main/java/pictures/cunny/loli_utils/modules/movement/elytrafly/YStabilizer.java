package pictures.cunny.loli_utils.modules.movement.elytrafly;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

public class YStabilizer extends Module {
    private final SettingGroup sgDefault = this.settings.getDefaultGroup();
    private final Setting<Double> owLevel =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("OW-level")
                            .description("Start speed settings in bps")
                            .defaultValue(333)
                            .range(5, 500)
                            .sliderRange(200, 389)
                            .build());
    private final Setting<Double> netherLevel =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("nether-level")
                            .description("Start speed settings in bps")
                            .defaultValue(121.5)
                            .range(-10, 1000)
                            .sliderRange(120, 128)
                            .build());
    public final Setting<Double> constantPitch =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("constant-pitch")
                            .description("Pitch for staying steady.")
                            .defaultValue(-1.9)
                            .decimalPlaces(1)
                            .sliderRange(-7, -0.2)
                            .build());
    public final Setting<Double> correctionPitch =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("correction-pitch")
                            .description("Pitch for correcting courses.")
                            .defaultValue(-2.7)
                            .decimalPlaces(1)
                            .sliderRange(-7, -0.2)
                            .build());
    public final Setting<Double> dramaticPitchStep =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("dramatic-pitch-step")
                            .description("The step for urgent pitch changes.")
                            .defaultValue(0.12)
                            .decimalPlaces(1)
                            .sliderRange(0.1, 3)
                            .build());
    public final Setting<Double> subtlePitchStep =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("subtle-pitch-step")
                            .description("The step for subtle pitch changes.")
                            .defaultValue(0.05)
                            .decimalPlaces(1)
                            .sliderRange(0.01, 3)
                            .build());

    public YStabilizer() {
        super(LoliUtilsMeteor.CATEGORY, "y-stabilizer", "Keep a consistent Y level.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.player.isFallFlying()) {
            if (mc.player.getY() > getDimY()) {
                mc.player.setXRot((float) Math.min(constantPitch.get(), mc.player.getXRot() + subtlePitchStep.get()));
            } else if (mc.player.getY() < getDimY()) {
                float nextPitch = mc.player.getXRot();
                if (nextPitch > correctionPitch.get()) {
                    nextPitch -= dramaticPitchStep.get();
                } else if (nextPitch < correctionPitch.get()) {
                    nextPitch += subtlePitchStep.get();
                }
                mc.player.setXRot((float) Math.max(correctionPitch.get(), nextPitch));
            }
        }
    }

    public double getDimY() {
        if (mc.player == null) {
            return 320;
        }

        return mc.player.clientLevel.dimensionType().respawnAnchorWorks() ? netherLevel.get() : owLevel.get();
    }
}
