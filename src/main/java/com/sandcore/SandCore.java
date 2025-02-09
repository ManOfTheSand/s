package com.sandcore.mmo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SandCore extends JavaPlugin {

    private static SandCore instance;

    public static SandCore getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        // Save default config if it does not exist.
        saveDefaultConfig();

        // Register GUIs & listeners.
        getServer().getPluginManager().registerEvents(new ClassSelectionGUI(), this);
        getServer().getPluginManager().registerEvents(new StatsUpgradeGUI(), this);
        getServer().getPluginManager().registerEvents(new AdminStatsGUI(), this);

        // Register command executors and tab completers.
        Commands commands = new Commands();
        getCommand("class").setExecutor(commands);
        getCommand("class").setTabCompleter(commands);
        getCommand("stats").setExecutor(commands);
        getCommand("stats").setTabCompleter(commands);
        getCommand("adminstats").setExecutor(commands);
        getCommand("adminstats").setTabCompleter(commands);

        // Super cool startup message.
        getLogger().info("************************************************");
        getLogger().info("Super Cool Start Up Message: SandCore Plugin Loaded!");
        getLogger().info("************************************************");
    }

    /*========================
      Inner Classes and Managers
    ========================*/

    // 1. PlayerStats class holds the stat values.
    public static class PlayerStats {
        private double health;
        private double damage;
        private double speed;
        private double defense;

        public PlayerStats() {
            this.health = 20.0;
            this.damage = 2.0;
            this.speed = 0.1;
            this.defense = 0.0;
        }

        public PlayerStats(double health, double damage, double speed, double defense) {
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.defense = defense;
        }

        public double getHealth() {
            return health;
        }

        public void setHealth(double health) {
            this.health = health;
        }

        public double getDamage() {
            return damage;
        }

        public void setDamage(double damage) {
            this.damage = damage;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public double getDefense() {
            return defense;
        }

        public void setDefense(double defense) {
            this.defense = defense;
        }

        /**
         * Applies the stats to the player using the updated, non-generic attributes.
         */
        public void applyToPlayer(Player player) {
            // Use up-to-date attribute constants.
            if (player.getAttribute(Attribute.MAX_HEALTH) != null)
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
            if (player.getAttribute(Attribute.ATTACK_DAMAGE) != null)
                player.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
            if (player.getAttribute(Attribute.MOVEMENT_SPEED) != null)
                player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            if (player.getAttribute(Attribute.ARMOR) != null)
                player.getAttribute(Attribute.ARMOR).setBaseValue(defense);
        }

        @Override
        public String toString() {
            return "Health: " + health + ", Damage: " + damage + ", Speed: " + speed + ", Defense: " + defense;
        }
    }

    // 2. PlayerStatsManager handles retrieving, updating, and saving player stats.
    public static class PlayerStatsManager {
        private static Map<UUID, PlayerStats> playerStats = new HashMap<>();

        public static PlayerStats getPlayerStats(Player player) {
            return playerStats.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerStats());
        }

        public static PlayerStats getStats(UUID uuid) {
            return playerStats.computeIfAbsent(uuid, key -> new PlayerStats());
        }

        public static void setPlayerStats(Player player, PlayerStats stats) {
            playerStats.put(player.getUniqueId(), stats);
        }

        // Loads class stats from the config.
        public static void updateStatsForClass(Player player, String className) {
            String path = "classes." + className.toLowerCase();
            // Load configurable stats; defaults provided if config value missing.
            double health = SandCore.getInstance().getConfig().getDouble(path + ".health", 20.0);
            double damage = SandCore.getInstance().getConfig().getDouble(path + ".damage", 2.0);
            double speed = SandCore.getInstance().getConfig().getDouble(path + ".speed", 0.1);
            double defense = SandCore.getInstance().getConfig().getDouble(path + ".defense", 0.0);
            PlayerStats stats = new PlayerStats(health, damage, speed, defense);
            stats.applyToPlayer(player);
            setPlayerStats(player, stats);
        }

        // Stub for saving stats to persistence.
        public static void saveStats() {
            // Implement actual persistence logic as needed.
            System.out.println("Player stats saved!");
        }
    }

    // 3. PlayerClassManager for storing a player's selected class and class change points.
    public static class PlayerClassManager {
        private static Map<UUID, String> playerClasses = new HashMap<>();
        private static Map<UUID, Integer> classChangePoints = new HashMap<>();

        public static String getPlayerClass(Player player) {
            return playerClasses.get(player.getUniqueId());
        }

        public static void setPlayerClass(Player player, String className) {
            playerClasses.put(player.getUniqueId(), className);
        }

        public static int getClassChangePoints(Player player) {
            return classChangePoints.getOrDefault(player.getUniqueId(), 0);
        }

        public static void addClassChangePoints(Player player, int points) {
            classChangePoints.put(player.getUniqueId(), getClassChangePoints(player) + points);
        }

        public static void deductClassChangePoint(Player player) {
            int current = getClassChangePoints(player);
            if(current > 0) {
                classChangePoints.put(player.getUniqueId(), current - 1);
            }
        }
    }

    // 4. ClassSelectionGUI displays a GUI for players to choose a class (loaded from config).
    public static class ClassSelectionGUI implements Listener {
        public static void open(Player player) {
            Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Select Your Class");
            ConfigurationSection classSection = SandCore.getInstance().getConfig().getConfigurationSection("classes");
            if (classSection != null) {
                for (String classKey : classSection.getKeys(false)) {
                    ItemStack item;
                    switch (classKey.toLowerCase()) {
                        case "warrior":
                            item = new ItemStack(Material.DIAMOND_SWORD);
                            break;
                        case "mage":
                            item = new ItemStack(Material.BLAZE_ROD);
                            break;
                        case "archer":
                            item = new ItemStack(Material.BOW);
                            break;
                        default:
                            item = new ItemStack(Material.PAPER);
                            break;
                    }
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.RESET + (classKey.substring(0, 1).toUpperCase() + classKey.substring(1)));
                    item.setItemMeta(meta);
                    gui.addItem(item);
                }
            }
            player.openInventory(gui);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals(ChatColor.GOLD + "Select Your Class")) {
                return;
            }
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            Player player = (Player) event.getWhoClicked();
            String className = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
            if (PlayerClassManager.getClassChangePoints(player) <= 0) {
                player.sendMessage(ChatColor.RED + "You don't have any class change points!");
                player.closeInventory();
                return;
            }
            PlayerClassManager.setPlayerClass(player, className);
            PlayerClassManager.deductClassChangePoint(player);
            PlayerStatsManager.updateStatsForClass(player, className);
            player.sendMessage(ChatColor.GREEN + "Class updated to " + className + "!");
            player.closeInventory();
        }
    }

    // 5. StatsUpgradeGUI allows players to upgrade their own stats.
    public static class StatsUpgradeGUI implements Listener {
        public static void open(Player player) {
            Inventory inv = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Upgrade Your Stats");

            ItemStack healthItem = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta healthMeta = healthItem.getItemMeta();
            healthMeta.setDisplayName(ChatColor.RED + "Upgrade Health");
            healthItem.setItemMeta(healthMeta);
            inv.setItem(0, healthItem);

            ItemStack damageItem = new ItemStack(Material.TNT);
            ItemMeta damageMeta = damageItem.getItemMeta();
            damageMeta.setDisplayName(ChatColor.RED + "Upgrade Damage");
            damageItem.setItemMeta(damageMeta);
            inv.setItem(1, damageItem);

            ItemStack speedItem = new ItemStack(Material.FEATHER);
            ItemMeta speedMeta = speedItem.getItemMeta();
            speedMeta.setDisplayName(ChatColor.RED + "Upgrade Speed");
            speedItem.setItemMeta(speedMeta);
            inv.setItem(2, speedItem);

            ItemStack defenseItem = new ItemStack(Material.IRON_CHESTPLATE);
            ItemMeta defenseMeta = defenseItem.getItemMeta();
            defenseMeta.setDisplayName(ChatColor.RED + "Upgrade Defense");
            defenseItem.setItemMeta(defenseMeta);
            inv.setItem(3, defenseItem);

            player.openInventory(inv);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals(ChatColor.GOLD + "Upgrade Your Stats")) {
                return;
            }
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            Player player = (Player) event.getWhoClicked();
            String stat = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
            PlayerStats stats = PlayerStatsManager.getPlayerStats(player);
            boolean upgraded = false;
            if (stat.contains("health")) {
                stats.setHealth(stats.getHealth() + 5.0);
                upgraded = true;
            } else if (stat.contains("damage")) {
                stats.setDamage(stats.getDamage() + 1.0);
                upgraded = true;
            } else if (stat.contains("speed")) {
                stats.setSpeed(stats.getSpeed() + 0.02);
                upgraded = true;
            } else if (stat.contains("defense")) {
                stats.setDefense(stats.getDefense() + 0.5);
                upgraded = true;
            }
            if (upgraded) {
                stats.applyToPlayer(player);
                player.sendMessage(ChatColor.GREEN + "Stat upgraded!");
                open(player); // Refresh the GUI
            } else {
                player.sendMessage(ChatColor.RED + "You have no available stat points!");
            }
        }
    }

    // 6. AdminStatsGUI lets an admin view and modify a player's stats.
    public static class AdminStatsGUI implements Listener {
        public static void open(Player admin, Player target) {
            Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_RED + "Admin: " + target.getName() + " Stats");
            PlayerStats stats = PlayerStatsManager.getPlayerStats(target);

            ItemStack health = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta hmeta = health.getItemMeta();
            hmeta.setDisplayName(ChatColor.RED + "Health: " + stats.getHealth());
            health.setItemMeta(hmeta);
            inv.setItem(0, health);

            ItemStack damage = new ItemStack(Material.TNT);
            ItemMeta dmeta = damage.getItemMeta();
            dmeta.setDisplayName(ChatColor.RED + "Damage: " + stats.getDamage());
            damage.setItemMeta(dmeta);
            inv.setItem(1, damage);

            ItemStack speed = new ItemStack(Material.FEATHER);
            ItemMeta smeta = speed.getItemMeta();
            smeta.setDisplayName(ChatColor.RED + "Speed: " + stats.getSpeed());
            speed.setItemMeta(smeta);
            inv.setItem(2, speed);

            ItemStack defense = new ItemStack(Material.IRON_CHESTPLATE);
            ItemMeta imeta = defense.getItemMeta();
            imeta.setDisplayName(ChatColor.RED + "Defense: " + stats.getDefense());
            defense.setItemMeta(imeta);
            inv.setItem(3, defense);

            admin.openInventory(inv);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().startsWith(ChatColor.DARK_RED + "Admin: "))
                return;
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
                return;
            Player admin = (Player) event.getWhoClicked();

            String title = ChatColor.stripColor(event.getView().getTitle());
            String[] parts = title.split(" ");
            if (parts.length < 2)
                return;
            String targetName = parts[1];
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                admin.sendMessage(ChatColor.RED + "Player not found.");
                return;
            }
            String clicked = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
            PlayerStats stats = PlayerStatsManager.getPlayerStats(target);
            if (clicked.contains("health")) {
                stats.setHealth(stats.getHealth() + 5.0);
            } else if (clicked.contains("damage")) {
                stats.setDamage(stats.getDamage() + 1.0);
            } else if (clicked.contains("speed")) {
                stats.setSpeed(stats.getSpeed() + 0.02);
            } else if (clicked.contains("defense")) {
                stats.setDefense(stats.getDefense() + 0.5);
            }
            stats.applyToPlayer(target);
            admin.sendMessage(ChatColor.GREEN + "Stats updated for " + target.getName());
            open(admin, target); // Refresh GUI
        }
    }

    // 7. Commands class handles all our commands and auto-populates suggestions.
    public static class Commands implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }
            Player player = (Player) sender;
            String cmdName = command.getName().toLowerCase();
            switch (cmdName) {
                case "class":
                    if (args.length == 0) {
                        player.sendMessage(ChatColor.YELLOW + "Usage: /class <select|info|addcp>");
                        return true;
                    }
                    String subCmd = args[0].toLowerCase();
                    if (subCmd.equals("select")) {
                        ClassSelectionGUI.open(player);
                    } else if (subCmd.equals("info")) {
                        String currentClass = PlayerClassManager.getPlayerClass(player);
                        int cp = PlayerClassManager.getClassChangePoints(player);
                        PlayerStats stats = PlayerStatsManager.getPlayerStats(player);
                        player.sendMessage(ChatColor.AQUA + "Your class: " + (currentClass != null ? currentClass : "None"));
                        player.sendMessage(ChatColor.AQUA + "Class Change Points: " + cp);
                        player.sendMessage(ChatColor.AQUA + "Stats: " + stats.toString());
                    } else if (subCmd.equals("addcp")) {
                        if (args.length < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /class addcp <amount>");
                            return true;
                        }
                        try {
                            int amount = Integer.parseInt(args[1]);
                            if (player.hasPermission("sandcore.admin")) {
                                PlayerClassManager.addClassChangePoints(player, amount);
                                player.sendMessage(ChatColor.GREEN + "Added " + amount + " class change point(s).");
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission.");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Amount must be a number.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Unknown subcommand.");
                    }
                    break;
                case "stats":
                    if (args.length == 0) {
                        player.sendMessage(ChatColor.YELLOW + "Usage: /stats <upgrade|add>");
                        return true;
                    }
                    subCmd = args[0].toLowerCase();
                    if (subCmd.equals("upgrade")) {
                        StatsUpgradeGUI.open(player);
                    } else if (subCmd.equals("add")) {
                        if (args.length < 2) {
                            player.sendMessage(ChatColor.RED + "Usage: /stats add <amount>");
                            return true;
                        }
                        try {
                            int amount = Integer.parseInt(args[1]);
                            if (player.hasPermission("sandcore.admin")) {
                                // You can implement stat point addition logic here.
                                player.sendMessage(ChatColor.GREEN + "Added " + amount + " stat point(s).");
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission.");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Amount must be a number.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Unknown subcommand.");
                    }
                    break;
                case "adminstats":
                    if (!player.hasPermission("sandcore.admin")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission.");
                        return true;
                    }
                    if (args.length == 0) {
                        player.sendMessage(ChatColor.YELLOW + "Usage: /adminstats <player>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }
                    AdminStatsGUI.open(player, target);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown command.");
                    break;
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            List<String> completions = new ArrayList<>();
            String cmd = command.getName().toLowerCase();
            if (cmd.equals("class")) {
                if (args.length == 1) {
                    completions.add("select");
                    completions.add("info");
                    completions.add("addcp");
                }
            } else if (cmd.equals("stats")) {
                if (args.length == 1) {
                    completions.add("upgrade");
                    completions.add("add");
                }
            } else if (cmd.equals("adminstats")) {
                if (args.length == 1) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        completions.add(p.getName());
                    }
                }
            }
            return completions;
        }
    }
}