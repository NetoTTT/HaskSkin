package com.hask.skin;

import com.mojang.authlib.properties.Property;
import com.hask.skin.SkinManager.SkinData;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import com.mojang.authlib.GameProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SkinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (!SkinManager.hasSkin(p.getUniqueId())) return;

        SkinData data = SkinManager.getSkin(p.getUniqueId());

        // Modifica o GameProfile imediatamente (antes do ADD_PLAYER ser enviado aos outros)
        GameProfile profile = ((CraftPlayer) p).getHandle().getProfile();
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", new Property("textures", data.value, data.signature));

        // Após 60 ticks (3s): notifica outros caso SkinsRestorer tenha sobrescrito
        Bukkit.getScheduler().runTaskLater(HaskSkin.get(), new Runnable() {
            public void run() {
                if (p.isOnline()) SkinManager.applySkin(p);
            }
        }, 60L);
    }
}
