package pictures.cunny.loli_utils.commands;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class MapartCopy extends Command {
    private final Path mapPath = Path.of(MeteorClient.FOLDER.getPath()).resolve("maps");
    private NativeImage texture;

    public MapartCopy() {
        super("copy-map", "Copies a map as an image.");

        try {
            Files.createDirectories(mapPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
        builder.then(
                literal("maps")
                        .then(
                                argument("x", IntegerArgumentType.integer())
                                        .then(
                                                argument("y", IntegerArgumentType.integer())
                                                        .executes(
                                                                context -> {
                                                                    texture =
                                                                            new NativeImage(
                                                                                    (context.getArgument("x", Integer.class) + 1) * 128,
                                                                                    (context.getArgument("y", Integer.class) + 1) * 128,
                                                                                    true);
                                                                    return SINGLE_SUCCESS;
                                                                }))));

        builder.then(
                argument("x", IntegerArgumentType.integer())
                        .then(
                                argument("y", IntegerArgumentType.integer())
                                        .executes(
                                                context -> {
                                                    if (texture == null) return 0;
                                                    int x = context.getArgument("x", Integer.class),
                                                            y = context.getArgument("y", Integer.class);
                                                    if (mc.player != null
                                                            && mc.player.getMainHandItem().getItem() == Items.FILLED_MAP) {
                                                        MapItemSavedData mapState =
                                                                MapItem.getSavedData(mc.player.getMainHandItem(), mc.level);
                                                        for (int i = 0; i < 128; ++i) {
                                                            for (int j = 0; j < 128; ++j) {
                                                                int k = j + i * 128;
                                                                Objects.requireNonNull(this.texture)
                                                                        .setPixel(
                                                                                j + (x * 128),
                                                                                i + (y * 128),
                                                                                MapColor.getColorFromPackedId(
                                                                                        Objects.requireNonNull(mapState).colors[k]));
                                                            }
                                                        }
                                                    }
                                                    return SINGLE_SUCCESS;
                                                })));

        builder.then(
                literal("save")
                        .executes(
                                context -> {
                                    if (mc.player != null && texture != null) {
                                        saveMap(Objects.requireNonNull(texture).hashCode());
                                    }
                                    return SINGLE_SUCCESS;
                                }));

        builder.then(
                literal("single")
                        .executes(
                                context -> {
                                    if (mc.player != null
                                            && mc.player.getMainHandItem().getItem() == Items.FILLED_MAP) {
                                        texture =
                                                new NativeImage(
                                                        128,
                                                        128,
                                                        true);
                                        int mapId = mc.player.getMainHandItem().get(DataComponents.MAP_ID).id();
                                        MapItemSavedData mapState =
                                                MapItem.getSavedData(mc.player.getMainHandItem(), mc.level);
                                        for (int i = 0; i < 128; ++i) {
                                            for (int j = 0; j < 128; ++j) {
                                                int k = j + i * 128;
                                                Objects.requireNonNull(this.texture)
                                                        .setPixel(
                                                                j,
                                                                i,
                                                                MapColor.getColorFromPackedId(
                                                                        Objects.requireNonNull(mapState).colors[k]));
                                            }
                                        }

                                        saveMap(mapId);
                                    }
                                    return SINGLE_SUCCESS;
                                }));
    }

    public void saveMap(int id) {
        try {
            Objects.requireNonNull(texture)
                    .writeToFile(
                            mapPath.resolve(
                                    id
                                            + "_"
                                            + (mc.getCurrentServer() != null && !mc.isSingleplayer()
                                            ? mc.getCurrentServer().ip
                                            : "OFFLINE")
                                            + ".png"));

            this.texture.close();

            this.texture = null;
        } catch (IOException ignored) {
        }
    }
}
