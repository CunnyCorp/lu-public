package pictures.cunny.loli_utils.modules.rendering;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.modules.LoliModule;

import java.util.*;

public class CumDripping extends LoliModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgEmission = settings.createGroup("Emission");
    private final SettingGroup sgPhysics = settings.createGroup("Physics");
    private final SettingGroup sgVisual = settings.createGroup("Visual");
    private final SettingGroup sgWhitelist = settings.createGroup("Whitelist");

    // General Settings
    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("max-distance")
            .description("Maximum distance to render liquid.")
            .defaultValue(64.0)
            .min(1.0)
            .max(512.0)
            .sliderMax(128.0)
            .build()
    );

    private final Setting<Boolean> renderSelf = sgGeneral.add(new BoolSetting.Builder()
            .name("render-self")
            .description("Render liquid on yourself.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> renderOthers = sgGeneral.add(new BoolSetting.Builder()
            .name("render-others")
            .description("Render liquid on other players.")
            .defaultValue(true)
            .build()
    );

    // Emission Settings
    private final Setting<EmissionType> emissionType = sgEmission.add(new EnumSetting.Builder<EmissionType>()
            .name("emission-type")
            .description("Where the cum emits from.")
            .defaultValue(EmissionType.BETWEEN_LEGS)
            .build()
    );

    private final Setting<Double> emissionRate = sgEmission.add(new DoubleSetting.Builder()
            .name("emission-rate")
            .description("Rate of cum emission.")
            .defaultValue(0.5)
            .min(0.1)
            .max(5.0)
            .build()
    );

    private final Setting<CumShape> cumShape = sgEmission.add(new EnumSetting.Builder<CumShape>()
            .name("cum-shape")
            .description("Shape of the cum.")
            .defaultValue(CumShape.DROPLETS)
            .build()
    );

    private final Setting<Double> strandThickness = sgEmission.add(new DoubleSetting.Builder()
            .name("strand-thickness")
            .description("Thickness of cum strands.")
            .defaultValue(0.03)
            .min(0.01)
            .max(0.1)
            .visible(() -> cumShape.get() == CumShape.STRANDS)
            .build()
    );

    private final Setting<Double> streamWidth = sgEmission.add(new DoubleSetting.Builder()
            .name("stream-width")
            .description("Width of cum streams.")
            .defaultValue(0.05)
            .min(0.02)
            .max(0.2)
            .visible(() -> cumShape.get() == CumShape.STREAMS)
            .build()
    );

    // Physics Settings
    private final Setting<Double> massMultiplier = sgPhysics.add(new DoubleSetting.Builder()
            .name("mass-multiplier")
            .description("Multiplier for cum mass.")
            .defaultValue(1.0)
            .min(0.1)
            .max(5.0)
            .build()
    );

    private final Setting<Double> gravity = sgPhysics.add(new DoubleSetting.Builder()
            .name("gravity")
            .description("Gravity effect on cum.")
            .defaultValue(0.03)
            .min(0.0)
            .max(0.1)
            .build()
    );

    private final Setting<Double> airResistance = sgPhysics.add(new DoubleSetting.Builder()
            .name("air-resistance")
            .description("Air resistance on cum.")
            .defaultValue(0.98)
            .min(0.9)
            .max(1.0)
            .build()
    );

    private final Setting<Double> surfaceTension = sgPhysics.add(new DoubleSetting.Builder()
            .name("surface-tension")
            .description("Surface tension effect.")
            .defaultValue(0.2)
            .min(0.0)
            .max(1.0)
            .build()
    );

    // Visual Settings
    private final Setting<SettingColor> cumColor = sgVisual.add(new ColorSetting.Builder()
            .name("cum-color")
            .description("Color of the cum.")
            .defaultValue(new SettingColor(255, 255, 255, 200))
            .build()
    );

    private final Setting<Double> transparency = sgVisual.add(new DoubleSetting.Builder()
            .name("transparency")
            .description("Transparency of the cum.")
            .defaultValue(0.7)
            .min(0.1)
            .max(1.0)
            .build()
    );

    private final Setting<Integer> maxCum = sgVisual.add(new IntSetting.Builder()
            .name("max-cum")
            .description("Maximum number of cum particles.")
            .defaultValue(100)
            .min(10)
            .max(500)
            .build()
    );

    // Whitelist Settings
    private final Setting<List<String>> whitelist = sgWhitelist.add(new StringListSetting.Builder()
            .name("whitelist")
            .description("Players to show cum on.")
            .defaultValue(Collections.emptyList())
            .build()
    );

    private final Setting<Boolean> useWhitelist = sgWhitelist.add(new BoolSetting.Builder()
            .name("use-whitelist")
            .description("Only show liquid cum whitelisted players.")
            .defaultValue(false)
            .build()
    );

    private final Map<UUID, List<LiquidParticle>> cumDropsMap = new HashMap<>();
    private final Random random = new Random();

    public CumDripping() {
        super(LoliUtilsMeteor.CATEGORY, "cum-dripping", "Renders cum dripping on players.");
    }

    private enum EmissionType {
        MOUTH("Mouth"),
        BETWEEN_LEGS("Between Legs"),
        CHEST("Chest");

        private final String name;

        EmissionType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private enum CumShape {
        DROPLETS("Droplets"),
        STRANDS("Strands"),
        STREAMS("Streams"),
        SPLASHES("Splashes");

        private final String name;

        CumShape(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class LiquidParticle {
        Vec3 position;
        Vec3 velocity;
        double size;
        double mass;
        double life;
        double maxLife;
        int shapeVariant;

        LiquidParticle(Vec3 position, Vec3 velocity, double size, double mass, double maxLife, int shapeVariant) {
            this.position = position;
            this.velocity = velocity;
            this.size = size;
            this.mass = mass;
            this.life = 0;
            this.maxLife = maxLife;
            this.shapeVariant = shapeVariant;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.level == null || mc.player == null) return;

        for (Player player : mc.level.players()) {
            if (shouldNotRenderPlayer(player)) continue;

            UUID uuid = player.getUUID();
            List<LiquidParticle> particles = cumDropsMap.computeIfAbsent(uuid, k -> new ArrayList<>());

            particles.removeIf(particle -> particle.life >= particle.maxLife);

            if (particles.size() < maxCum.get()) {
                emitNewParticles(player, particles);
            }

            updateParticles(player, particles);
        }
    }

    private void emitNewParticles(Player player, List<LiquidParticle> particles) {
        int particlesToEmit = (int) (emissionRate.get() * (1 + random.nextDouble()));

        for (int i = 0; i < particlesToEmit && particles.size() < maxCum.get(); i++) {
            Vec3 emissionPoint = getEmissionPoint(player);
            Vec3 initialVelocity = getInitialVelocity();
            double size = 0.02 + random.nextDouble() * 0.04;
            double mass = size * massMultiplier.get() * (0.8 + random.nextDouble() * 0.4);
            double maxLife = 2.0 + random.nextDouble() * 3.0;
            int shapeVariant = random.nextInt(3);

            particles.add(new LiquidParticle(
                    emissionPoint,
                    initialVelocity,
                    size,
                    mass,
                    maxLife,
                    shapeVariant
            ));
        }
    }

    private Vec3 getEmissionPoint(Player player) {
        Vec3 pos = player.getPosition(0f);

        return switch (emissionType.get()) {
            case MOUTH -> pos.add(0, player.getEyeHeight() - 0.2, 0);
            case BETWEEN_LEGS -> pos.add(0, 0.4, 0);
            case CHEST -> pos.add(0, player.getEyeHeight() * 0.7, 0);
        };
    }

    private Vec3 getInitialVelocity() {

        return switch (emissionType.get()) {
            case MOUTH -> new Vec3(
                    (random.nextDouble() - 0.5) * 0.02,
                    -0.01 - random.nextDouble() * 0.02,
                    (random.nextDouble() - 0.5) * 0.02
            );
            case BETWEEN_LEGS -> new Vec3(
                    (random.nextDouble() - 0.5) * 0.01,
                    -0.02 - random.nextDouble() * 0.03,
                    (random.nextDouble() - 0.5) * 0.01
            );
            case CHEST -> new Vec3(
                    (random.nextDouble() - 0.5) * 0.015,
                    -0.015 - random.nextDouble() * 0.025,
                    (random.nextDouble() - 0.5) * 0.015
            );
        };
    }

    private void updateParticles(Player player, List<LiquidParticle> particles) {
        for (LiquidParticle particle : particles) {
            particle.velocity = particle.velocity.add(0, -gravity.get() * particle.mass, 0);
            particle.velocity = particle.velocity.scale(airResistance.get());
            particle.position = particle.position.add(particle.velocity);

            if (cumShape.get() == CumShape.DROPLETS) {
                Vec3 center = getEmissionPoint(player);
                Vec3 toCenter = center.subtract(particle.position).normalize();
                particle.velocity = particle.velocity.add(toCenter.scale(surfaceTension.get() * 0.001));
            }

            particle.life += 0.05;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        for (Player player : mc.level.players()) {
            if (shouldNotRenderPlayer(player)) continue;

            List<LiquidParticle> particles = cumDropsMap.get(player.getUUID());
            if (particles == null) continue;

            for (LiquidParticle particle : particles) {
                if (particle.life < particle.maxLife) {
                    renderLiquidParticle(event, particle);
                }
            }
        }
    }

    private boolean shouldNotRenderPlayer(Player player) {
        if (player == mc.player && !renderSelf.get()) return true;
        if (player != mc.player && !renderOthers.get()) return true;
        assert mc.player != null;
        if (player.distanceTo(mc.player) > maxDistance.get()) return true;
        if (useWhitelist.get() && !whitelist.get().contains(player.getName().getString())) return true;
        return player.isInvisible();
    }

    private void renderLiquidParticle(Render3DEvent event, LiquidParticle particle) {
        Color color = new Color(cumColor.get());
        color.a = (int) (color.a * transparency.get() * (1 - particle.life / particle.maxLife));

        switch (cumShape.get()) {
            case DROPLETS:
                renderDroplet(event, particle, color);
                break;
            case STRANDS:
                renderStrand(event, particle, color);
                break;
            case STREAMS:
                renderStream(event, particle, color);
                break;
            case SPLASHES:
                renderSplash(event, particle, color);
                break;
        }
    }

    private void renderDroplet(Render3DEvent event, LiquidParticle particle, Color color) {
        int segments = 6;
        Vec3 center = particle.position;
        double size = particle.size * (1 - particle.life / particle.maxLife * 0.5);

        for (int i = 0; i < segments / 2; i++) {
            double phi1 = Math.PI * i / ((double) segments / 2);
            double phi2 = Math.PI * (i + 1) / ((double) segments / 2);

            for (int j = 0; j < segments; j++) {
                double theta1 = 2 * Math.PI * j / segments;
                double theta2 = 2 * Math.PI * (j + 1) / segments;

                Vec3 p11 = getSpherePoint(center, size, phi1, theta1);
                Vec3 p12 = getSpherePoint(center, size, phi1, theta2);
                Vec3 p21 = getSpherePoint(center, size, phi2, theta1);
                Vec3 p22 = getSpherePoint(center, size, phi2, theta2);

                event.renderer.triangles.ensureQuadCapacity();
                event.renderer.triangles.quad(
                        event.renderer.triangles.vec3(p11.x, p11.y, p11.z).color(color).next(),
                        event.renderer.triangles.vec3(p12.x, p12.y, p12.z).color(color).next(),
                        event.renderer.triangles.vec3(p22.x, p22.y, p22.z).color(color).next(),
                        event.renderer.triangles.vec3(p21.x, p21.y, p21.z).color(color).next()
                );
            }
        }
    }

    private void renderStrand(Render3DEvent event, LiquidParticle particle, Color color) {
        Vec3 start = particle.position;
        Vec3 end = particle.position.add(particle.velocity.scale(2));
        double thickness = strandThickness.get() * particle.size;

        renderTaperedCylinder(event, start, end, thickness, thickness * 0.8, color, 6);
    }

    private void renderStream(Render3DEvent event, LiquidParticle particle, Color color) {
        Vec3 start = particle.position;
        Vec3 end = particle.position.add(particle.velocity.scale(3));
        double startWidth = streamWidth.get() * particle.size;
        double endWidth = startWidth * 0.6;

        renderTaperedCylinder(event, start, end, startWidth, endWidth, color, 8);
    }

    private void renderSplash(Render3DEvent event, LiquidParticle particle, Color color) {
        int points = 4 + particle.shapeVariant;
        Vec3 center = particle.position;
        double size = particle.size * (1 + particle.life / particle.maxLife);

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double length = size * (0.5 + random.nextDouble() * 0.5);

            Vec3 direction = new Vec3(Math.cos(angle), Math.sin(angle) * 0.5, Math.sin(angle));
            Vec3 end = center.add(direction.normalize().scale(length));

            renderTaperedCylinder(event, center, end, size * 0.2, size * 0.1, color, 4);
        }
    }

    private Vec3 getSpherePoint(Vec3 center, double radius, double phi, double theta) {
        double x = center.x + radius * Math.sin(phi) * Math.cos(theta);
        double y = center.y + radius * Math.cos(phi);
        double z = center.z + radius * Math.sin(phi) * Math.sin(theta);
        return new Vec3(x, y, z);
    }

    private void renderTaperedCylinder(Render3DEvent event, Vec3 start, Vec3 end, double startRadius, double endRadius, Color color, int segments) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 perpendicular = direction.cross(new Vec3(0, 1, 0)).normalize();
        if (perpendicular.length() < 0.1) {
            perpendicular = direction.cross(new Vec3(1, 0, 0)).normalize();
        }
        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();

        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            Vec3 s1 = start.add(perpendicular.scale(startRadius * Math.cos(angle1)))
                    .add(perpendicular2.scale(startRadius * Math.sin(angle1)));
            Vec3 s2 = start.add(perpendicular.scale(startRadius * Math.cos(angle2)))
                    .add(perpendicular2.scale(startRadius * Math.sin(angle2)));
            Vec3 e1 = end.add(perpendicular.scale(endRadius * Math.cos(angle1)))
                    .add(perpendicular2.scale(endRadius * Math.sin(angle1)));
            Vec3 e2 = end.add(perpendicular.scale(endRadius * Math.cos(angle2)))
                    .add(perpendicular2.scale(endRadius * Math.sin(angle2)));

            event.renderer.triangles.ensureQuadCapacity();
            event.renderer.triangles.quad(
                    event.renderer.triangles.vec3(s1.x, s1.y, s1.z).color(color).next(),
                    event.renderer.triangles.vec3(s2.x, s2.y, s2.z).color(color).next(),
                    event.renderer.triangles.vec3(e2.x, e2.y, e2.z).color(color).next(),
                    event.renderer.triangles.vec3(e1.x, e1.y, e1.z).color(color).next()
            );
        }
    }
}