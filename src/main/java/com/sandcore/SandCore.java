package com.sandcore.mmo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SandCore extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register event listeners.
        getServer().getPluginManager().registerEvents(new ClassSelectionGUI(), this);
        getServer().getPluginManager().registerEvents(new StatsUpgradeGUI(), this);
        // Register command executors.
        getCommand("class").setExecutor(new Commands());
        getCommand("stats").setExecutor(new Commands());
    }

    // ================== PlayerStats Class ==================
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

        public void applyToPlayer(Player player) {
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

    // ================== PlayerClassManager ==================
    public static class PlayerClassManager {
        private static HashMap<UUID, String> playerClasses = new HashMap<>();
        private static HashMap<UUID, Integer> classChangePoints = new HashMap<>();

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
            if (current > 0) {
                classChangePoints.put(player.getUniqueId(), current - 1);
            }
        }
    }

    // ================== PlayerStatPointsManager ==================
    public static class PlayerStatPointsManager {
        private static HashMap<UUID, Integer> statPoints = new HashMap<>();

        public static int getStatPoints(Player player) {
            return statPoints.getOrDefault(player.getUniqueId(), 0);
        }

        public static void addStatPoints(Player player, int points) {
            statPoints.put(player.getUniqueId(), getStatPoints(player) + points);
        }

        public static boolean deductStatPoint(Player player) {
            int current = getStatPoints(player);
            if (current > 0) {
                statPoints.put(player.getUniqueId(), current - 1);
                return true;
            }
            return false;
        }
    }

    // ================== PlayerStatsManager ==================
    public static class PlayerStatsManager {
        private static Map<UUID, PlayerStats> playerStats = new HashMap<>();

        public static PlayerStats getPlayerStats(Player player) {
            return playerStats.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerStats());
        }

        public static void setPlayerStats(Player player, PlayerStats stats) {
            playerStats.put(player.getUniqueId(), stats);
        }

        public static void updateStatsForClass(Player player, String className) {
            PlayerStats stats;
            switch (className.toLowerCase()) {
                case "warrior":
                    stats = new PlayerStats(30.0, 5.0, 0.1, 3.0);
                    break;
                case "mage":
                    stats = new PlayerStats(20.0, 7.0, 0.1, 1.5);
                    break;
                case "archer":
                    stats = new PlayerStats(25.0, 4.0, 0.12, 2.0);
                    break;
                default:
                    stats = new PlayerStats();
                    break;
            }
            stats.applyToPlayer(player);
            setPlayerStats(player, stats);
        }
    }

    // ================== ClassSelectionGUI ==================
    public static class ClassSelectionGUI implements Listener {

        public static void open(Player player) {
            Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Select Your Class");

            // Create class items.
            ItemStack warrior = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta warriorMeta = warrior.getItemMeta();
            warriorMeta.setDisplayName(ChatColor.RED + "Warrior");
            warrior.setItemMeta(warriorMeta);

            ItemStack mage = new ItemStack(Material.BLAZE_ROD);
            ItemMeta mageMeta = mage.getItemMeta();
            mageMeta.setDisplayName(ChatColor.BLUE + "Mage");
            mage.setItemMeta(mageMeta);

            ItemStack archer = new ItemStack(Material.BOW);
            ItemMeta archerMeta = archer.getItemMeta();
            archerMeta.setDisplayName(ChatColor.GREEN + "Archer");
            archer.setItemMeta(archerMeta);

            // Set items in designated slots.
            gui.setItem(2, warrior);
            gui.setItem(4, mage);
            gui.setItem(6, archer);

            player.openInventory(gui);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Select Your Class"))
                return;
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
                return;
            Player player = (Player) event.getWhoClicked();
            String className = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            // Require a class change point if changing classes.
            if (PlayerClassManager.getClassChangePoints(player) <= 0) {
                player.sendMessage(ChatColor.RED + "You don't have any class change points!");
                player.closeInventory();
                return;
            }

            // Set the player's class, deduct a change point, and update stats.
            PlayerClassManager.setPlayerClass(player, className);
            PlayerClassManager.deductClassChangePoint(player);
            PlayerStatsManager.updateStatsForClass(player, className);

            player.sendMessage(ChatColor.GREEN + "Your class has been updated to " + className + "!");
            player.closeInventory();
        }
    }

    // ================== StatsUpgradeGUI ==================
    public static class StatsUpgradeGUI implements Listener {

        public static void open(Player player) {
            Inventory inv = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Upgrade Your Stats");

            // Items for stat upgrades.
            ItemStack healthItem = new ItemStack(Material.REDSTONE);
            ItemMeta healthMeta = healthItem.getItemMeta();
            healthMeta.setDisplayName(ChatColor.RED + "Upgrade Health");
            healthItem.setItemMeta(healthMeta);

            ItemStack damageItem = new ItemStack(Material.BLAZE_POWDER);
            ItemMeta damageMeta = damageItem.getItemMeta();
            damageMeta.setDisplayName(ChatColor.DARK_RED + "Upgrade Damage");
            damageItem.setItemMeta(damageMeta);

            ItemStack speedItem = new ItemStack(Material.FEATHER);
            ItemMeta speedMeta = speedItem.getItemMeta();
            speedMeta.setDisplayName(ChatColor.AQUA + "Upgrade Speed");
            speedItem.setItemMeta(speedMeta);

            ItemStack defenseItem = new ItemStack(Material.SHIELD);
            ItemMeta defenseMeta = defenseItem.getItemMeta();
            defenseMeta.setDisplayName(ChatColor.GRAY + "Upgrade Defense");
            defenseItem.setItemMeta(defenseMeta);

            // Show available stat points.
            int points = PlayerStatPointsManager.getStatPoints(player);
            ItemStack pointsItem = new ItemStack(Material.EMERALD);
            ItemMeta pointsMeta = pointsItem.getItemMeta();
            pointsMeta.setDisplayName(ChatColor.GREEN + "Available Stat Points: " + points);
            pointsItem.setItemMeta(pointsMeta);

            inv.setItem(1, healthItem);
            inv.setItem(3, damageItem);
            inv.setItem(5, speedItem);
            inv.setItem(7, defenseItem);
            inv.setItem(4, pointsItem);

            player.openInventory(inv);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Upgrade Your Stats"))
                return;
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
                return;
            Player player = (Player) event.getWhoClicked();
            String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            PlayerStats stats = PlayerStatsManager.getPlayerStats(player);
            boolean upgraded = false;
            switch (displayName.toLowerCase()) {
                case "upgrade health":
                    if (PlayerStatPointsManager.deductStatPoint(player)) {
                        stats.setHealth(stats.getHealth() + 5.0);
                        upgraded = true;
                    }
                    break;
                case "upgrade damage":
                    if (PlayerStatPointsManager.deductStatPoint(player)) {
                        stats.setDamage(stats.getDamage() + 1.0);
                        upgraded = true;
                    }
                    break;
                case "upgrade speed":
                    if (PlayerStatPointsManager.deductStatPoint(player)) {
                        stats.setSpeed(stats.getSpeed() + 0.02);
                        upgraded = true;
                    }
                    break;
                case "upgrade defense":
                    if (PlayerStatPointsManager.deductStatPoint(player)) {
                        stats.setDefense(stats.getDefense() + 0.5);
                        upgraded = true;
                    }
                    break;
                default:
                    break;
            }
            if (upgraded) {
                stats.applyToPlayer(player);
                player.sendMessage(ChatColor.GREEN + "Stat upgraded!");
                open(player);
            } else {
                player.sendMessage(ChatColor.RED + "You have no stat points available!");
            }
        }
    }

    // ================== Commands Handler ==================
    public static class Commands implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use these commands.");
                return true;
            }
            Player player = (Player) sender;
            String cmdName = command.getName().toLowerCase();

            if (cmdName.equals("class")) {
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
                    player.sendMessage(ChatColor.AQUA + "Your class: " + (currentClass == null ? "None" : currentClass));
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
                            player.sendMessage(ChatColor.RED + "You don't have permission.");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Amount must be a number.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Unknown subcommand.");
                }
            } else if (cmdName.equals("stats")) {
                if (args.length == 0) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /stats <upgrade|add>");
                    return true;
                }
                String subCmd = args[0].toLowerCase();
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
                            PlayerStatPointsManager.addStatPoints(player, amount);
                            player.sendMessage(ChatColor.GREEN + "Added " + amount + " stat point(s).");
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't have permission.");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Amount must be a number.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Unknown subcommand.");
                }
            }
            return true;
        }
    }
}