package pictures.cunny.loli_utils.utility;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

public record Dependency(String dep) {
    public boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(dep);
    }

    public Optional<ModContainer> get() {
        return FabricLoader.getInstance().getModContainer(dep);
    }
}
