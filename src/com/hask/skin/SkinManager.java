package com.hask.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkinManager {

    private static final Map<UUID, SkinData> skins = new HashMap<>();
    private static File file;
    private static YamlConfiguration config;

    public static void load() {
        file = new File(HaskSkin.get().getDataFolder(), "skins.yml");
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String value = config.getString(key + ".value");
            String signature = config.getString(key + ".signature");
            if (value != null && signature != null) {
                skins.put(uuid, new SkinData(value, signature));
            }
        }
        HaskSkin.get().getLogger().info("Skins carregadas: " + skins.size());
    }

    public static void save() {
        config = new YamlConfiguration();
        for (Map.Entry<UUID, SkinData> entry : skins.entrySet()) {
            String path = entry.getKey().toString();
            config.set(path + ".value", entry.getValue().value);
            config.set(path + ".signature", entry.getValue().signature);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSkin(UUID uuid, String value, String signature) {
        skins.put(uuid, new SkinData(value, signature));
        save();
    }

    public static SkinData getSkin(UUID uuid) {
        return skins.get(uuid);
    }

    public static boolean hasSkin(UUID uuid) {
        return skins.containsKey(uuid);
    }

    public static void applySkin(Player player) {
        SkinData data = skins.get(player.getUniqueId());
        if (data == null) return;
        applyNMS(player, data.value, data.signature);
    }

    public static void applyNMS(final Player player, final String value, final String signature) {
        EntityPlayer self = ((CraftPlayer) player).getHandle();
        // Modifica o GameProfile in-place
        GameProfile profile = self.getProfile();
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", value, signature));

        refreshForOthers(player);
    }

    // Notifica outros jogadores para renderizarem a nova skin (sem respawn — evita bug de chunk)
    private static void refreshForOthers(final Player player) {
        final EntityPlayer self = ((CraftPlayer) player).getHandle();
        final int entityId = self.getId();

        PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, self);
        PacketPlayOutPlayerInfo addPacket    = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, self);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            CraftPlayer cp = (CraftPlayer) online;
            cp.getHandle().playerConnection.sendPacket(removePacket);
            cp.getHandle().playerConnection.sendPacket(addPacket);
            cp.getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entityId));
            cp.getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(self));
        }
    }

    public static class SkinData {
        public final String value;
        public final String signature;
        public SkinData(String value, String signature) {
            this.value = value;
            this.signature = signature;
        }
    }
}
