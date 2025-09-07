package pictures.cunny.loli_utils.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import pictures.cunny.loli_utils.config.LoliConfig;
import pictures.cunny.loli_utils.utility.render.PlayerEffects;
import pictures.cunny.loli_utils.utility.render.SpecialEffects;

public class ChangePlayerEffects extends Command {
    public ChangePlayerEffects() {
        super("set-effect", "Sets individual player name effects.");
    }

    @Override
    public void build(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {

        builder.then(
                argument("player", PlayerListEntryArgumentType.create()).then(

                                literal("color").then(argument("r", IntegerArgumentType.integer(0, 255))
                                        .then(
                                                argument("g", IntegerArgumentType.integer(0, 255))
                                                        .then(
                                                                argument("b", IntegerArgumentType.integer(0, 255))
                                                                        .executes(
                                                                                context -> {
                                                                                    PlayerInfo player = context.getArgument("player", PlayerInfo.class);

                                                                                    int r = context.getArgument("r", Integer.class);
                                                                                    int g = context.getArgument("g", Integer.class);
                                                                                    int b = context.getArgument("b", Integer.class);

                                                                                    Color color = new Color(r, g, b);

                                                                                    SpecialEffects.ensureProfileExists(player.getProfile().getId().toString());

                                                                                    LoliConfig.get().playerEffects.get(player.getProfile().getId().toString()).color = color;

                                                                                    MutableComponent message = Component.empty();

                                                                                    message.setStyle(message.getStyle().withColor(Color.CYAN.toTextColor()));
                                                                                    message.append("Setting ");

                                                                                    message.append(player.getProfile().getName());

                                                                                    message.append(" to ");

                                                                                    MutableComponent colorText = Component.literal(color.toTextColor().serialize());

                                                                                    colorText.setStyle(message.getStyle().withBold(true).withUnderlined(true).withColor(color.toTextColor()));

                                                                                    message.append(colorText);

                                                                                    info(message);

                                                                                    return SINGLE_SUCCESS;
                                                                                })))))
                        .then(literal("clear").executes(context -> {
                            PlayerInfo player = context.getArgument("player", PlayerInfo.class);

                            MutableComponent message = Component.empty();

                            if (LoliConfig.get().playerEffects.containsKey(player.getProfile().getId().toString())) {
                                PlayerEffects effects = LoliConfig.get().playerEffects.get(player.getProfile().getId().toString());
                                message.setStyle(message.getStyle().withColor(Color.CYAN.toTextColor()));
                                message.append("Removing ");

                                message.append(player.getProfile().getName());
                                message.append("'s ");

                                MutableComponent colorText = Component.literal("color");

                                colorText.setStyle(message.getStyle().withBold(true).withUnderlined(true).withColor(effects.color.toTextColor()));

                                message.append(colorText);
                                message.append(".");
                            } else {
                                message.append(player.getProfile().getName());
                                message.append(" doesn't have a custom color.");
                            }

                            info(message);

                            LoliConfig.get().playerEffects.remove(player.getProfile().getId().toString());
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("effect")
                                .then(literal("bold").then(argument("bold", BoolArgumentType.bool())
                                        .executes(context -> {
                                            PlayerInfo player = context.getArgument("player", PlayerInfo.class);

                                            MutableComponent message = Component.empty();
                                            message.append("Setting ");

                                            SpecialEffects.ensureProfileExists(player.getProfile().getId().toString());

                                            LoliConfig.get().playerEffects.get(player.getProfile().getId().toString()).bold = context.getArgument("bold", Boolean.class);

                                            message.append(player.getProfile().getName());
                                            message.append("'s text to bold.");

                                            info(message);
                                            return SINGLE_SUCCESS;
                                        }))).then(literal("underline").then(argument("underline", BoolArgumentType.bool())
                                        .executes(context -> {
                                            PlayerInfo player = context.getArgument("player", PlayerInfo.class);

                                            MutableComponent message = Component.empty();
                                            message.append("Setting ");

                                            SpecialEffects.ensureProfileExists(player.getProfile().getId().toString());

                                            LoliConfig.get().playerEffects.get(player.getProfile().getId().toString()).underline = context.getArgument("underline", Boolean.class);

                                            message.append(player.getProfile().getName());
                                            message.append("'s text to underlined.");

                                            info(message);
                                            return SINGLE_SUCCESS;
                                        }))).then(literal("italic").then(argument("italic", BoolArgumentType.bool())
                                        .executes(context -> {
                                            PlayerInfo player = context.getArgument("player", PlayerInfo.class);

                                            MutableComponent message = Component.empty();
                                            message.append("Setting ");

                                            SpecialEffects.ensureProfileExists(player.getProfile().getId().toString());

                                            LoliConfig.get().playerEffects.get(player.getProfile().getId().toString()).italic = context.getArgument("italic", Boolean.class);

                                            message.append(player.getProfile().getName());
                                            message.append("'s text to italic.");

                                            info(message);
                                            return SINGLE_SUCCESS;
                                        })))));

    }

}
