package com.sandcore.mmo;

import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import com.sandcore.mmo.command.MMOCoreCommandExecutor;
import com.sandcore.mmo.util.ServiceRegistry;

public final class MMOCoreMain extends JavaPlugin {

    private static MMOCoreMain instance;
    private MiniMessage miniMessage;
    private BukkitAudiences audiences;

    @Override
    public void onEnable() {
        instance = this;
        miniMessage = MiniMessage.builder().build();
        audiences = BukkitAudiences.create(this);
        
        // Register the /mmocore command if defined in plugin.yml
        if (getCommand("mmocore") != null) {
            getCommand("mmocore").setExecutor(new MMOCoreCommandExecutor());
        }
        
        // Initialize managers and other services.
        ServiceRegistry.registerServices();
        
        getLogger().info("SandCore Plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (audiences != null) {
            audiences.close();
        }
        getLogger().info("SandCore Plugin disabled!");
    }

    public static MMOCoreMain getInstance() {
        return instance;
    }
    
    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
    
    public BukkitAudiences getAudiences() {
        return audiences;
    }
} 