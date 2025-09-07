package pictures.cunny.loli_utils.mixin.litematica;

import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pictures.cunny.loli_utils.modules.printer.PrinterUtils;

@Mixin(value = SchematicPlacementManager.class, remap = false)
public class SchematicPlacementManagerMixin {
    @Redirect(method = "onClientChunkUnload",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/config/options/ConfigBoolean;getBooleanValue()Z"))
    public boolean chunkUnloadPatcher(ConfigBoolean instance) {
        return PrinterUtils.PRINTER.schematicStayLoadedPatch.get();
    }
}
