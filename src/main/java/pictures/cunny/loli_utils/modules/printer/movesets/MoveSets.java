package pictures.cunny.loli_utils.modules.printer.movesets;

public enum MoveSets {
    VANILLA(new VanillaMove()),
    ADVANCED(new AdvancedMove()),
    BARITONE(new BaritoneMove()),
    CONST_EFLY(new ConstElytraMove());

    public final DefaultMove movement;

    MoveSets(DefaultMove movement) {
        this.movement = movement;
    }
}
