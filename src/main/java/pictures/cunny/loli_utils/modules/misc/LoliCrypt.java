package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.StringUtil;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class LoliCrypt extends Module {
    private static final String FIX1 = String.valueOf((char) 10);
    private static final String FIX2 = String.valueOf((char) 13);
    public static ArrayList<String> decoratedText = new ArrayList<>();
    private static String SECRET_KEY;
    private static String SALT;
    private static TextColor TEXT_COLOR;

    private final Random random = new Random();
    private final IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
    private final SettingGroup sgBasic = settings.createGroup("Basic");
    public final Setting<Boolean> noRemoving = sgBasic.add(new BoolSetting.Builder()
            .name("no-server-remove")
            .description("Prevents the server from removing messages.")
            .build());
    public final Setting<Logging> logging = sgBasic.add(new EnumSetting.Builder<Logging>()
            .name("logging")
            .description("The logging mode for chat.")
            .defaultValue(Logging.Normal)
            .build());

    private final SettingGroup sgEncryption = settings.createGroup("Encryption");
    public final Setting<SettingColor> colorSetting = sgEncryption.add(new ColorSetting.Builder()
            .name("text-color")
            .defaultValue(new Color(255, 140, 0, 255))
            .onChanged(settingColor -> TEXT_COLOR = settingColor.toTextColor())
            .build());
    private final Setting<Boolean> oldMode = sgEncryption.add(new BoolSetting.Builder()
            .name("old")
            .description("Reverts something for old.")
            .build());
    private final Setting<Boolean> send = sgEncryption.add(new BoolSetting.Builder()
            .name("send")
            .description("Whether or not to send encrypted messages.")
            .build());
    private final Setting<Keybind> sendKeybind = sgEncryption.add(new KeybindSetting.Builder()
            .name("send-keybind")
            .defaultValue(Keybind.none())
            .description("A keybind to toggle sending")
            .action(() -> {
                if (mc.screen != null) return;
                send.set(!send.get());
                ChatUtils.infoPrefix("LoliCrypt", send.get() ? "Patented LoliTechnology of minecraft encryption." : "Our LoliScouts say we're getting spied on by italians.");
            })
            .build());
    private final Setting<Boolean> trashData = sgEncryption.add(new BoolSetting.Builder()
            .name("trash-data")
            .description("Add trash data to messages.")
            .build());
    private final Setting<Integer> trashMax = sgEncryption.add(new IntSetting.Builder()
            .name("max-trash")
            .description("Maximum amount of trash to add.")
            .sliderRange(4, 14)
            .build());
    private SecretKeySpec key;
    private final Setting<String> saltKey = sgEncryption.add(new StringSetting.Builder()
            .name("salt")
            .description("The salt key.")
            .defaultValue("salt")
            .onChanged(s -> {
                SALT = s;
                generateKey();
            })
            .build());
    private final Setting<String> secretKey = sgEncryption.add(new StringSetting.Builder()
            .name("secret")
            .description("The secret key.")
            .defaultValue("secret")
            .onChanged(s -> {
                SECRET_KEY = s;
                generateKey();
            })
            .build());

    public LoliCrypt() {
        super(LoliUtilsMeteor.CATEGORY, "LoliCrypt", "Not even minecraft is safe with these italians.");
    }

    public static boolean shouldCancel(Component text) {
        return decoratedText.contains(text.getString());
    }

    @Override
    public void onActivate() {
        generateKey();
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (noRemoving.get() && event.packet instanceof ClientboundDeleteChatPacket) {
            event.cancel();
        }

        if (event.packet instanceof ClientboundDisguisedChatPacket(
                Component message, net.minecraft.network.chat.ChatType.Bound chatType
        )) {
            if (logging.get() == Logging.Extra)
                LoliUtilsMeteor.LOGGER.info("<PROFILELESS MESSAGE> {} - NAME: {} - TARGET NAME: {} - CHAT TYPE: {}", message.getString(), chatType.name().getString(), chatType.targetName().isPresent() ? chatType.targetName().get().getString() : "NO TARGET", chatType.chatType().getRegisteredName());
        }

        if (event.packet instanceof ServerboundChatPacket packet) {
            if (logging.get() == Logging.Extra)
                LoliUtilsMeteor.LOGGER.info("UNSIGNED: {}", packet.message());

        }

        if (logging.get() == Logging.Extra) {
            if (event.packet instanceof ClientboundSystemChatPacket(Component content, boolean overlay)) {
                LoliUtilsMeteor.LOGGER.info("<GAME MESSAGE> {} - OVERLAY: {}", content.getString(), overlay);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST - 1000)
    private void onMessageSend(SendMessageEvent event) {
        if (!send.get() || event.message.startsWith(Config.get().prefix.get()) || event.message.startsWith("/") || event.isCancelled())
            return;
        String encrypted =
                encryptTillItWorks(event.message);

        if (encrypted == null) {
            event.setCancelled(true);
            ChatUtils.infoPrefix("LoliCrypt", "The Lolis forgot how to encrypt...");
            return;
        }

        encrypted = encrypted.replaceAll(FIX1, "'1");
        encrypted = encrypted.replaceAll(FIX2, "'2");

        if (encrypted.length() >= 256) {
            event.setCancelled(true);
            ChatUtils.infoPrefix("LoliCrypt", "Based on the LoliPipe Infrastructure, when encrypted it'd have been too long.");
            return;
        }

        if (ServerGamePacketListenerImpl.isChatMessageIllegal(encrypted)) {
            event.setCancelled(true);
            ChatUtils.infoPrefix("LoliCrypt", "The LoliScientist are guessing the mime encoder dislikes us.");

            List<String> badChars = new ArrayList<>();

            for (int i = 0; i < encrypted.length(); i++) {
                if (!StringUtil.isAllowedChatCharacter(encrypted.charAt(i))) {
                    badChars.add("C:" + encrypted.codePointAt(i));
                }
            }

            LoliUtilsMeteor.LOGGER.info("Encoded Message: {}, [{}]", encrypted, String.join(",", badChars));
            return;
        }

        event.message = ":" + encrypted;
    }

    public String encryptTillItWorks(String message) {
        String encrypted =
                encrypt((oldMode.get() ? "NEKO " : "LOLI ") + message + (trashData.get() ? "| " + StringUtils.randomText(random.nextInt(3,
                                trashMax.get()),
                        false) : ""));
        if (encrypted != null) {
            return encrypted;
        }

        return encryptTillItWorks(message);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onMessageReceived(ReceiveMessageEvent event) {
        // Chat Encryption
        if (event.getMessage().getString().endsWith("<DECRYPT>")) {
            return;
        }

        String prettyStr = event.getMessage().getString().strip();

        for (String str : prettyStr.split(" ")) {
            String colorless = str.replaceAll("§[4c6e2ab319d5f780rlonmkz]", "");
            if (colorless.contains(":")) {
                colorless = colorless.replaceAll("'1", FIX1);
                colorless = colorless.replaceAll("'2", FIX2);
                String decrypted = decrypt(colorless);
                if (decrypted != null && decrypted.startsWith(oldMode.get() ? "NEKO " : "LOLI ")) {
                    decrypted = decrypted.replaceFirst((oldMode.get() ? "NEKO " : "LOLI "), "");
                    String[] csplit = decrypted.split("\\|");
                    if (csplit.length > 1) decrypted = decrypted.replace("|" + csplit[csplit.length - 1], "");
                    event.setMessage(Component.literal(prettyStr.replace(str, decrypted)).setStyle(Style.EMPTY.withBold(true).withColor(TEXT_COLOR)));
                }
            }
        }
    }

    private String decrypt(String input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            return new String(cipher.doFinal(Base64.getMimeDecoder().decode(input)));
        } catch (Exception ignored) {
        }
        return null;
    }

    private String encrypt(String input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            return Base64.getMimeEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void generateKey() {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            this.key = new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (Exception ignored) {
        }
    }

    public enum Logging {
        Extra,
        Normal,
        None
    }
}
