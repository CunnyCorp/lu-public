package pictures.cunny.loli_utils.utility.packets;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.Dependencies;

import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PacketUtils {
    public static boolean IS_VFP_LOADED = false;
    protected static boolean modifyCurrentTickRots = false;
    protected static long currentTick = 0;
    protected static Map<Long, List<Packet<?>>> queuedPackets = new HashMap<>();
    protected static Map<Long, List<BlockPlacement>> queuedBlockPlacement = new HashMap<>();
    protected static boolean wasModified = false;
    protected static float modifiedYaw = 0f;
    protected static float modifiedPitch = 0f;

    @PreInit
    public static void init() {
        IS_VFP_LOADED = Dependencies.VFP.isLoaded();

        if (IS_VFP_LOADED) {
            LoliUtilsMeteor.LOGGER.info("[LOLI] VFP is loaded, automatically changing how packets are sent from LoliUtils!");
        }

        // For rotations.
        MeteorClient.EVENT_BUS.subscribe(new PacketUtils());
    }

    @EventHandler
    public void onTickPost(TickEvent.Post event) {
        if (!wasModified && modifyCurrentTickRots) {
            assert mc.player != null;
            LoliUtilsMeteor.LOGGER.info("Server wasn't updated, updating them now.");
            send(new ServerboundMovePlayerPacket.Rot(modifiedYaw, modifiedPitch, mc.player.onGround(), mc.player.horizontalCollision));
            wasModified = false;
        }

        modifyCurrentTickRots = false;
    }

    @EventHandler(priority = 1)
    public void onTickPre(TickEvent.Pre event) {
        currentTick++;

        if (queuedPackets.containsKey(currentTick)) {
            for (Packet<?> packet : queuedPackets.get(currentTick)) {
                send(packet);
            }

            queuedPackets.remove(currentTick);
        }

        if (queuedBlockPlacement.containsKey(currentTick)) {
            for (BlockPlacement placement : queuedBlockPlacement.get(currentTick)) {
                placement.place();
            }

            queuedBlockPlacement.remove(currentTick);
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        assert mc.player != null;
        if (modifyCurrentTickRots) {
            if (modifiedYaw == mc.player.getYRot() && modifiedPitch == mc.player.getXRot()) {
                return;
            }

            if (event.packet instanceof ServerboundMovePlayerPacket.Rot packet) {
                send(new ServerboundMovePlayerPacket.Rot(modifiedYaw, modifiedPitch, packet.isOnGround(), packet.horizontalCollision()));
                wasModified = true;
                event.cancel();
            } else if (event.packet instanceof ServerboundMovePlayerPacket.PosRot packet) {
                send(new ServerboundMovePlayerPacket.PosRot(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ()), modifiedYaw, modifiedPitch, packet.isOnGround(), packet.horizontalCollision()));
                wasModified = true;
                event.cancel();
            } else if (event.packet instanceof ServerboundMovePlayerPacket.StatusOnly packet) {
                send(new ServerboundMovePlayerPacket.Rot(modifiedYaw, modifiedPitch, packet.isOnGround(), packet.horizontalCollision()));
                wasModified = true;
                event.cancel();
            } else if (event.packet instanceof ServerboundMovePlayerPacket.Pos packet) {
                send(new ServerboundMovePlayerPacket.PosRot(packet.getX(mc.player.getX()), packet.getY(mc.player.getY()), packet.getZ(mc.player.getZ()), modifiedYaw, modifiedPitch, mc.player.onGround(), mc.player.horizontalCollision));
                wasModified = true;
                event.cancel();
            }
        }
    }

    public static void updatePosition(double x, double y, double z) {
        Objects.requireNonNull(mc.player).setPos(x, y, z);
        send(new ServerboundMovePlayerPacket.Pos(x, y, z, mc.player.onGround(), mc.player.horizontalCollision));
    }

    public static void updatePosition(Vec3 pos) {
        updatePosition(pos.x, pos.y, pos.z);
    }

    public static void rotate(float pitch, float yaw, boolean update) {
        assert mc.player != null;

        if (update) {
            modifyCurrentTickRots = true;
            modifiedYaw = yaw;
            modifiedPitch = pitch;
        } else {
            mc.player.setXRot(pitch);
            mc.player.setYRot(yaw);
        }
    }

    public static void rotate(float pitch, float yaw) {
        rotate(pitch, yaw, false);
    }

    public static void queuePacketForNextTick(Packet<?> packet) {
        if (!queuedPackets.containsKey(currentTick + 1)) {
            queuedPackets.put(currentTick + 1, new ArrayList<>());
        }

        queuedPackets.get(currentTick + 1).add(packet);
    }

    public static void queuePlacementForNextTick(BlockPlacement placement) {
        if (!queuedBlockPlacement.containsKey(currentTick + 1)) {
            queuedBlockPlacement.put(currentTick + 1, new ArrayList<>());
        }

        queuedBlockPlacement.get(currentTick + 1).add(placement);
    }

    public static void send(Packet<?> packet) {
        if (mc.getConnection() == null) return;

        send(mc.getConnection().getConnection(), packet);
    }

    public static void send(Connection connection, Packet<?> packet) {
        if (connection == null) {
            LoliUtilsMeteor.LOGGER.error("Connection is null");
            return;
        }

        // Cancel here too!
        if (LoliUtilsMeteor.NO_SWING && packet instanceof ServerboundSwingPacket) {
            return;
        }

        if (IS_VFP_LOADED) {
            // Same as the standalone Client, will be compatible.
            connection.send(packet, null, true);
        } else {
            connection.channel.writeAndFlush(packet);
        }
    }

    public static void chat(String string, boolean command) {
        if (command) command(string);
        else chat(string);
    }

    public static void chat(String string) {
        if (ServerGamePacketListenerImpl.isChatMessageIllegal(string) || mc.getConnection() == null) {
            return;
        }

        if (string.startsWith("/")) {
            command(string.replaceFirst("/", ""));
        } else {
            mc.getConnection().sendChat(string);
        }
    }

    public static void command(String string) {
        if (ServerGamePacketListenerImpl.isChatMessageIllegal(string) || mc.getConnection() == null) {
            return;
        }

        mc.getConnection().sendCommand(string);
    }
}
