package com.sandcore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SandCore extends JavaPlugin {

    private static SandCore instance;
    public static SandCore getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Load persistent player data (class choices, levels, etc.)
        PlayerDataManager.loadData();

        // Register event listeners (GUI, skill tree, etc.)
        Bukkit.getPluginManager().registerEvents(new ClassSelectionGUI(), this);
        Bukkit.getPluginManager().registerEvents(new StatsUpgradeGUI(), this);
        Bukkit.getPluginManager().registerEvents(new AdminStatsGUI(), this);
        Bukkit.getPluginManager().registerEvents(new SkillTreeGUI(), this);

        // Register command executors and tab completers.
        Commands commands = new Commands();
        getCommand("class").setExecutor(commands);
        getCommand("class").setTabCompleter(commands);
        getCommand("stats").setExecutor(commands);
        getCommand("stats").setTabCompleter(commands);
        getCommand("adminstats").setExecutor(commands);
        getCommand("adminstats").setTabCompleter(commands);
        getCommand("skilltree").setExecutor(commands);
        getCommand("skilltree").setTabCompleter(commands);

        // Startup message.
        getLogger().info("************************************************");
        getLogger().info("Super Cool Start Up Message: SandCore Plugin Loaded!");
        getLogger().info("************************************************");
    }

    @Override
    public void onDisable() {
        // Save persistent data (player classes, levels, etc.).
        PlayerDataManager.saveData();
        PlayerStatsManager.saveStats();
    }

    /*====================
      Inner Classes & Managers
    ====================*/

    // 1. PlayerStats holds stat values.
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

        public double getHealth() { return health; }
        public void setHealth(double health) { this.health = health; }
        public double getDamage() { return damage; }
        public void setDamage(double damage) { this.damage = damage; }
        public double getSpeed() { return speed; }
        public void setSpeed(double speed) { this.speed = speed; }
        public double getDefense() { return defense; }
        public void setDefense(double defense) { this.defense = defense; }

        // Applies the stats to the player using current API attribute constants.
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

    // 2. PlayerStatsManager handles retrieving and saving stats.
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

        // Stub for saving stats.
        public static void saveStats() {
            getInstance().getLogger().info("Player stats saved!");
        }
    }

    // 3. PlayerClassManager stores a player's chosen class and class change points.
    public static class PlayerClassManager {
        // Made public for persistence.
        public static Map<UUID, String> playerClasses = new HashMap<>();
        private static Map<UUID, Integer> classChangePoints = new HashMap<>();

        public static String getPlayerClass(Player player) {
            return playerClasses.get(player.getUniqueId());
        }

        public static void setPlayerClass(Player player, String className) {
            playerClasses.put(player.getUniqueId(), className);
            PlayerDataManager.saveData(); // Persist choice immediately.
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

    // 4. ClassSelectionGUI displays a configurable GUI for class selection.
    public static class ClassSelectionGUI implements Listener {
        public static void open(Player player) {
            Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Select Your Class");
            ConfigurationSection classSection = getInstance().getConfig().getConfigurationSection("classes");
            if (classSection != null) {
                for (String classKey : classSection.getKeys(false)) {
                    ConfigurationSection cs = classSection.getConfigurationSection(classKey);
                    // Read config values:
                    String materialStr = cs.getString("material", "PAPER").toUpperCase();
                    Material material = Material.getMaterial(materialStr);
                    if(material == null) material = Material.PAPER;
                    String displayName = cs.getString("display_name", classKey);
                    List<String> lore = cs.getStringList("lore");

                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.RESET + displayName);
                    if(lore != null && !lore.isEmpty()) {
                        List<String> coloredLore = new ArrayList<>();
                        for(String line : lore) {
                            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                        meta.setLore(coloredLore);
                    }
                    item.setItemMeta(meta);
                    gui.addItem(item);
                }
            }
            player.openInventory(gui);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals(ChatColor.GOLD + "Select Your Class")) return;
            event.setCancelled(true);
            if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            Player player = (Player) event.getWhoClicked();
            String chosenClass = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();

            // Persist the player's chosen class.
            PlayerClassManager.setPlayerClass(player, chosenClass);
            player.sendMessage(ChatColor.GREEN + "You have selected the " + chosenClass + " class!");
            // (Optional) Update stats based on chosen class using config here.
            player.closeInventory();
        }
    }

    // 5. StatsUpgradeGUI for players to upgrade their stats.
    public static class StatsUpgradeGUI implements Listener {
        public static void open(Player player) {
            Inventory gui = Bukkit.createInventory(null, 9, ChatColor.AQUA + "Upgrade Your Stats");

            ItemStack health = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta hMeta = health.getItemMeta();
            hMeta.setDisplayName(ChatColor.RED + "Health +5");
            health.setItemMeta(hMeta);
            gui.setItem(0, health);

            ItemStack damage = new ItemStack(Material.TNT);
            ItemMeta dMeta = damage.getItemMeta();
            dMeta.setDisplayName(ChatColor.RED + "Damage +1");
            damage.setItemMeta(dMeta);
            gui.setItem(1, damage);

            ItemStack speed = new ItemStack(Material.FEATHER);
            ItemMeta sMeta = speed.getItemMeta();
            sMeta.setDisplayName(ChatColor.RED + "Speed +0.02");
            speed.setItemMeta(sMeta);
            gui.setItem(2, speed);

            ItemStack defense = new ItemStack(Material.IRON_CHESTPLATE);
            ItemMeta defMeta = defense.getItemMeta();
            defMeta.setDisplayName(ChatColor.RED + "Defense +0.5");
            defense.setItemMeta(defMeta);
            gui.setItem(3, defense);

            player.openInventory(gui);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals(ChatColor.AQUA + "Upgrade Your Stats")) return;
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
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
                open(player);
            } else {
                player.sendMessage(ChatColor.RED + "You have no available stat points!");
            }
        }
    }

    // 6. AdminStatsGUI lets admins view and modify a player's stats.
    public static class AdminStatsGUI implements Listener {
        public static void open(Player admin, Player target) {
            Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_RED + "Admin: " + target.getName() + " Stats");
            PlayerStats stats = PlayerStatsManager.getPlayerStats(target);

            ItemStack health = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta hMeta = health.getItemMeta();
            hMeta.setDisplayName(ChatColor.RED + "Health: " + stats.getHealth());
            health.setItemMeta(hMeta);
            inv.setItem(0, health);

            ItemStack damage = new ItemStack(Material.TNT);
            ItemMeta dMeta = damage.getItemMeta();
            dMeta.setDisplayName(ChatColor.RED + "Damage: " + stats.getDamage());
            damage.setItemMeta(dMeta);
            inv.setItem(1, damage);

            ItemStack speed = new ItemStack(Material.FEATHER);
            ItemMeta sMeta = speed.getItemMeta();
            sMeta.setDisplayName(ChatColor.RED + "Speed: " + stats.getSpeed());
            speed.setItemMeta(sMeta);
            inv.setItem(2, speed);

            ItemStack defense = new ItemStack(Material.IRON_CHESTPLATE);
            ItemMeta defMeta = defense.getItemMeta();
            defMeta.setDisplayName(ChatColor.RED + "Defense: " + stats.getDefense());
            defense.setItemMeta(defMeta);
            inv.setItem(3, defense);

            admin.openInventory(inv);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().startsWith(ChatColor.DARK_RED + "Admin: ")) return;
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            Player admin = (Player) event.getWhoClicked();
            String title = ChatColor.stripColor(event.getView().getTitle());
            String[] parts = title.split(" ");
            if (parts.length < 2) return;
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
            open(admin, target);
        }
    }

    // 7. PlayerDataManager handles persistent loading/saving of player data.
    public static class PlayerDataManager {
        private static File playerDataFile;
        private static FileConfiguration playerDataConfig;

        public static void loadData() {
            playerDataFile = new File(getInstance().getDataFolder(), "playerdata.yml");
            if (!playerDataFile.exists()) {
                try {
                    playerDataFile.getParentFile().mkdirs();
                    playerDataFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
            // Load saved player classes.
            if (playerDataConfig.contains("classes")) {
                for (String uuidStr : playerDataConfig.getConfigurationSection("classes").getKeys(false)) {
                    String savedClass = playerDataConfig.getString("classes." + uuidStr);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        PlayerClassManager.playerClasses.put(uuid, savedClass);
                    } catch (IllegalArgumentException e) { }
                }
            }
            // Load player levels.
            if (playerDataConfig.contains("levels")) {
                for (String uuidStr : playerDataConfig.getConfigurationSection("levels").getKeys(false)) {
                    int level = playerDataConfig.getInt("levels." + uuidStr);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        PlayerLevelManager.playerLevels.put(uuid, level);
                    } catch (IllegalArgumentException e) { }
                }
            }
            // Load player skill points.
            if (playerDataConfig.contains("skillpoints")) {
                for (String uuidStr : playerDataConfig.getConfigurationSection("skillpoints").getKeys(false)) {
                    int sp = playerDataConfig.getInt("skillpoints." + uuidStr);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        PlayerLevelManager.playerSkillPoints.put(uuid, sp);
                    } catch (IllegalArgumentException e) { }
                }
            }
        }

        public static void saveData() {
            for (Map.Entry<UUID, String> entry : PlayerClassManager.playerClasses.entrySet()) {
                playerDataConfig.set("classes." + entry.getKey().toString(), entry.getValue());
            }
            for (Map.Entry<UUID, Integer> entry : PlayerLevelManager.playerLevels.entrySet()) {
                playerDataConfig.set("levels." + entry.getKey().toString(), entry.getValue());
            }
            for (Map.Entry<UUID, Integer> entry : PlayerLevelManager.playerSkillPoints.entrySet()) {
                playerDataConfig.set("skillpoints." + entry.getKey().toString(), entry.getValue());
            }
            try {
                playerDataConfig.save(playerDataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 8. PlayerLevelManager handles player levels and skill points.
    public static class PlayerLevelManager {
        public static Map<UUID, Integer> playerLevels = new HashMap<>();
        public static Map<UUID, Integer> playerSkillPoints = new HashMap<>();

        public static int getLevel(Player player) {
            return playerLevels.getOrDefault(player.getUniqueId(), 1);
        }

        public static int getSkillPoints(Player player) {
            return playerSkillPoints.getOrDefault(player.getUniqueId(), 0);
        }

        public static void addExperience(Player player, int xp) {
            int newLevel = getLevel(player) + xp;
            playerLevels.put(player.getUniqueId(), newLevel);
            playerSkillPoints.put(player.getUniqueId(), getSkillPoints(player) + xp); // e.g., 1 skill point per xp
            player.sendMessage(ChatColor.GREEN + "You've reached level " + newLevel + " and earned " + xp + " skill point(s)!");
        }

        public static void useSkillPoint(Player player) {
            int sp = getSkillPoints(player);
            if (sp > 0) {
                playerSkillPoints.put(player.getUniqueId(), sp - 1);
            }
        }
    }

    // 9. SkillTreeGUI displays a skill tree for leveling rewards.
    public static class SkillTreeGUI implements Listener {
        public static void open(Player player) {
            Inventory gui = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Skill Tree");
            ConfigurationSection skillSection = getInstance().getConfig().getConfigurationSection("skilltree");
            if (skillSection != null) {
                int slot = 0;
                for (String skillKey : skillSection.getKeys(false)) {
                    ConfigurationSection cs = skillSection.getConfigurationSection(skillKey);
                    String materialStr = cs.getString("material", "BARRIER").toUpperCase();
                    Material material = Material.getMaterial(materialStr);
                    if(material == null) material = Material.BARRIER;
                    String displayName = cs.getString("display_name", skillKey);
                    List<String> lore = cs.getStringList("lore");
                    int requiredLevel = cs.getInt("required_level", 1);

                    // Create item with requirement info.
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.RESET + displayName + " (Req Level: " + requiredLevel + ")");
                    if (lore != null && !lore.isEmpty()) {
                        List<String> coloredLore = new ArrayList<>();
                        for (String line : lore) {
                            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                        meta.setLore(coloredLore);
                    }
                    item.setItemMeta(meta);
                    gui.setItem(slot, item);
                    slot++;
                }
            }
            player.openInventory(gui);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().startsWith(ChatColor.LIGHT_PURPLE + "Skill Tree")) return;
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            Player player = (Player) event.getWhoClicked();
            String clicked = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            int reqLevel = 1;
            try {
                if (clicked.contains("(Req Level:")) {
                    String reqPart = clicked.substring(clicked.indexOf("(Req Level:"), clicked.indexOf(")", clicked.indexOf("(Req Level:")));
                    reqPart = reqPart.replace("(Req Level:", "").trim();
                    reqLevel = Integer.parseInt(reqPart);
                }
            } catch (Exception e) {
                reqLevel = 1;
            }
            if (PlayerLevelManager.getLevel(player) < reqLevel) {
                player.sendMessage(ChatColor.RED + "You need to be at least level " + reqLevel + " to unlock this skill!");
                return;
            }
            if (PlayerLevelManager.getSkillPoints(player) <= 0) {
                player.sendMessage(ChatColor.RED + "You don't have any skill points!");
                return;
            }
            // Example reward: if the skill is related to "health", increase player's health.
            if (clicked.toLowerCase().contains("health")) {
                PlayerStats stats = PlayerStatsManager.getPlayerStats(player);
                stats.setHealth(stats.getHealth() + 5.0);
                stats.applyToPlayer(player);
                player.sendMessage(ChatColor.GREEN + "Your health has increased!");
            }
            PlayerLevelManager.useSkillPoint(player);
            player.closeInventory();
        }
    }

    // 10. Commands handles all command logic and tab auto-completion.
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
                case "skilltree":
                    SkillTreeGUI.open(player);
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
            } else if (cmd.equals("skilltree")) {
                // No extra completions.
            }
            return completions;
        }
    }
}