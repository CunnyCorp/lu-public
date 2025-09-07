package pictures.cunny.loli_utils.utility.render;

import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.core.BlockPos;

public class RenderWrap {
    private final BlockPos blockPos;
    private final int maxTime;
    private final Color originalColor;
    private final RenderStyle renderStyle;

    // Time-based variables
    private int currentTime;
    private int breath;

    // Animation state
    private final long startTime;
    private double animationOffset;
    private boolean isAnimating;

    // Performance tracking
    private double lastDistance;
    private boolean distanceDirty = true;

    // Render style enum
    public enum RenderStyle {
        STATIC,
        FADE,
        SHRINK,
        PULSE,
        BREATH,
        STROKE
    }

    // Legacy constructors for compatibility
    public RenderWrap(int fadeTime, int breath) {
        this(BlockPos.ZERO, 20, fadeTime, breath, new Color(255, 255, 255, 255), RenderStyle.FADE);
    }

    public RenderWrap(int fadeTime, int breath, Color color) {
        this(BlockPos.ZERO, 20, fadeTime, breath, color, RenderStyle.FADE);
    }

    public RenderWrap(BlockPos blockPos, int maxTime, int currentTime, int breath, Color color) {
        this(blockPos, maxTime, currentTime, breath, color, RenderStyle.FADE);
    }

    // New enhanced constructor
    public RenderWrap(BlockPos blockPos, int maxTime, int currentTime, int breath, Color color, RenderStyle style) {
        this.blockPos = blockPos;
        this.maxTime = Math.max(1, maxTime);
        this.currentTime = Math.max(0, currentTime);
        this.breath = breath;
        this.originalColor = color;
        this.renderStyle = style != null ? style : RenderStyle.FADE;
        this.startTime = System.currentTimeMillis();
        this.animationOffset = 0.0;
        this.isAnimating = true;
        this.lastDistance = -1.0;
    }

    // Convenience constructors for different render styles
    public static RenderWrap createFading(BlockPos pos, int fadeTime, Color color) {
        return new RenderWrap(pos, fadeTime, fadeTime, 0, color, RenderStyle.FADE);
    }

    public static RenderWrap createStatic(BlockPos pos, Color color) {
        return new RenderWrap(pos, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, color, RenderStyle.STATIC);
    }

    public static RenderWrap createPulsing(BlockPos pos, int duration, Color color) {
        return new RenderWrap(pos, duration, duration, 0, color, RenderStyle.PULSE);
    }

    public static RenderWrap createShrinking(BlockPos pos, int shrinkTime, Color color) {
        return new RenderWrap(pos, shrinkTime, shrinkTime, 0, color, RenderStyle.SHRINK);
    }

    public static RenderWrap createBreathing(BlockPos pos, int breathTime, int breathIntensity, Color color) {
        return new RenderWrap(pos, breathTime, breathTime, breathIntensity, color, RenderStyle.BREATH);
    }

    // Getters with legacy compatibility
    public int maxFadeTime() {
        return maxTime;
    }

    public int fadeTime() {
        return currentTime;
    }

    public int maxTime() {
        return maxTime;
    }

    public int currentTime() {
        return currentTime;
    }

    public int breath() {
        return breath;
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public Color color() {
        return originalColor;
    }

    public Color originalColor() {
        return originalColor;
    }

    public RenderStyle renderStyle() {
        return renderStyle;
    }

    public long startTime() {
        return startTime;
    }

    public double animationOffset() {
        return animationOffset;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    // Setters
    public void fadeTime(int fadeTime) {
        this.currentTime = Math.max(0, fadeTime);
    }

    public void currentTime(int time) {
        this.currentTime = Math.max(0, time);
    }

    public void breath(int breath) {
        this.breath = breath;
    }

    public void animationOffset(double offset) {
        this.animationOffset = offset;
    }

    public void setAnimating(boolean animating) {
        this.isAnimating = animating;
    }

    // Progress calculations
    public double getProgress() {
        if (maxTime <= 0) return 1.0;
        return Math.max(0.0, Math.min(1.0, (double) currentTime / maxTime));
    }

    public double getFadeProgress() {
        return 1.0 - getProgress();
    }

    public double getAge() {
        return (System.currentTimeMillis() - startTime) / 1000.0;
    }

    // Animation calculations
    public double getShrinkFactor() {
        if (renderStyle != RenderStyle.SHRINK) return 1.0;
        return Math.max(0.1, getProgress());
    }

    public double getPulseFactor() {
        if (renderStyle != RenderStyle.PULSE) return 1.0;
        double time = getAge() * 4; // 4 pulses per second
        return 1.0 + 0.1 * Math.sin(time * Math.PI);
    }

    public double getBreathFactor() {
        if (renderStyle != RenderStyle.BREATH) return 0.0;
        return breath * Math.sin(getAge() * Math.PI * 0.5);
    }

    // Color calculations
    public Color getCurrentColor() {
        return getCurrentColor(1.0);
    }

    public Color getCurrentColor(double globalAlpha) {
        Color base = originalColor;
        double alpha = base.a / 255.0;

        switch (renderStyle) {
            case FADE -> {
                alpha *= getProgress();
            }
            case SHRINK, PULSE -> {
                // Maintain original alpha
            }
            case BREATH -> {
                alpha *= Math.max(0.3, getProgress()); // Don't fade completely
            }
            case STROKE -> {
                alpha *= Math.max(0.5, getProgress()); // Maintain visibility
            }
            case STATIC -> {
                // No alpha modification
            }
        }

        alpha *= globalAlpha;
        alpha = Math.max(0.0, Math.min(1.0, alpha));

        return new Color(base.r, base.g, base.b, (int) (alpha * 255));
    }

    // Utility methods
    public boolean shouldSkip() {
        return currentTime <= 0 ||
                (renderStyle == RenderStyle.FADE && getFadeProgress() >= 1.0) ||
                (renderStyle == RenderStyle.SHRINK && getShrinkFactor() <= 0.1);
    }

    public boolean shouldRemove() {
        return shouldSkip() ||
                (renderStyle != RenderStyle.STATIC && currentTime <= 0);
    }

    public void tick() {
        if (currentTime > 0 && renderStyle != RenderStyle.STATIC) {
            currentTime--;
        }

        // Update animation state
        updateAnimation();

        // Mark distance as dirty for recalculation
        distanceDirty = true;
    }

    private void updateAnimation() {
        double age = getAge();

        switch (renderStyle) {
            case PULSE -> {
                animationOffset = 0.05 * Math.sin(age * Math.PI * 4);
            }
            case BREATH -> {
                animationOffset = getBreathFactor();
            }
            case STROKE -> {
                animationOffset = 0.02 * Math.sin(age * Math.PI * 2);
            }
            default -> {
                animationOffset = 0.0;
            }
        }
    }

    // Distance caching for performance
    public double getDistanceToPlayer(double playerX, double playerY, double playerZ) {
        if (distanceDirty || lastDistance < 0) {
            double dx = blockPos.getX() + 0.5 - playerX;
            double dy = blockPos.getY() + 0.5 - playerY;
            double dz = blockPos.getZ() + 0.5 - playerZ;
            lastDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            distanceDirty = false;
        }
        return lastDistance;
    }

    public void invalidateDistance() {
        distanceDirty = true;
    }

    // Render dimensions calculations
    public double getRenderSize() {
        return getRenderSize(1.0);
    }

    public double getRenderSize(double baseSize) {
        double size = baseSize;

        switch (renderStyle) {
            case SHRINK -> {
                size *= getShrinkFactor();
            }
            case PULSE -> {
                size *= getPulseFactor();
            }
            case BREATH -> {
                size += Math.abs(getBreathFactor()) * 0.1;
            }
        }

        return Math.max(0.01, size);
    }

    public double[] getRenderBounds() {
        double size = getRenderSize();
        double half = size * 0.5;
        double offset = animationOffset;

        return new double[]{
                blockPos.getX() + 0.5 - half,
                blockPos.getY() + 0.5 - half + offset,
                blockPos.getZ() + 0.5 - half,
                blockPos.getX() + 0.5 + half,
                blockPos.getY() + 0.5 + half + offset,
                blockPos.getZ() + 0.5 + half
        };
    }

    // Advanced render calculations
    public boolean isInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double[] bounds = getRenderBounds();
        return !(bounds[3] < minX || bounds[0] > maxX ||
                bounds[4] < minY || bounds[1] > maxY ||
                bounds[5] < minZ || bounds[2] > maxZ);
    }

    public int getLOD(double distance) {
        if (distance < 32) return 0;      // Full detail
        if (distance < 64) return 1;      // Medium detail
        if (distance < 128) return 2;     // Low detail
        return 3;                         // Minimal detail
    }

    // Debug information
    @Override
    public String toString() {
        return String.format("RenderWrap{pos=%s, time=%d/%d, style=%s, progress=%.2f}",
                blockPos, currentTime, maxTime, renderStyle, getProgress());
    }

    // Equals and hashCode for collections
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RenderWrap other)) return false;
        return blockPos.equals(other.blockPos) &&
                startTime == other.startTime &&
                renderStyle == other.renderStyle;
    }

    @Override
    public int hashCode() {
        return blockPos.hashCode() ^ Long.hashCode(startTime) ^ renderStyle.hashCode();
    }

    // Factory methods for common use cases
    public static RenderWrap forScaffold(BlockPos pos, int fadeTime, Color color, RenderStyle style) {
        return new RenderWrap(pos, fadeTime, fadeTime, 10, color, style);
    }

    public static RenderWrap forCombat(BlockPos pos, Color color) {
        return new RenderWrap(pos, 60, 60, 0, color, RenderStyle.PULSE);
    }

    public static RenderWrap forUtility(BlockPos pos, Color color) {
        return new RenderWrap(pos, 100, 100, 0, color, RenderStyle.FADE);
    }

    // Performance optimization methods
    public void optimizeForDistance(double distance) {
        if (distance > 100) {
            // Reduce update frequency for distant blocks
            if (System.currentTimeMillis() % 3 != 0) {
                return;
            }
        }
        tick();
    }

    public boolean shouldUpdateThisFrame(double distance, int frameCount) {
        // Skip updates for very distant blocks on alternate frames
        if (distance > 64 && frameCount % 2 == 0) {
            return false;
        }
        if (distance > 128 && frameCount % 4 != 0) {
            return false;
        }
        return true;
    }
}