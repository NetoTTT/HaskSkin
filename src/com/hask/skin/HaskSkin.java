package com.hask.skin;

import com.hask.skin.command.CmdSkin;
import org.bukkit.plugin.java.JavaPlugin;

public class HaskSkin extends JavaPlugin {

    private static HaskSkin instance;

    @Override
    public void onEnable() {
        instance = this;
        getDataFolder().mkdirs();
        SkinManager.load();
        getCommand("hskin").setExecutor(new CmdSkin());
        getServer().getPluginManager().registerEvents(new SkinListener(), this);
        getLogger().info("HaskSkin ativado.");
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static HaskSkin get() { return instance; }
}
