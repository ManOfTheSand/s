package com.sandcore;

import com.sandcore.utils.ChatColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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

        // Set default values for UI if not provided.
        FileConfiguration config = getConfig();
        config.addDefault("ui.header", "&#00FF7F&lSANDCORE SKILLS");
        config.addDefault("ui.reload-message", "&#FFD700Config reloaded! &#32CD32(v%version%)");
        config.options().copyDefaults(true);
        saveConfig();

        // Ensure the plugin data folder and classes.yml exist.
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File classesFile = new File(getDataFolder(), "classes.yml");
        if (!classesFile.exists()) {
            saveResource("classes.yml", false);
        }

        // Load persistent player data.
        PlayerDataManager.loadData();

        // Load custom level data.
        LevelManager.loadData();

        // Register event listeners.
        Bukkit.getPluginManager().registerEvents(new ClassSelectionGUI(), this);
        Bukkit.getPluginManager().registerEvents(new StatsUpgradeGUI(), this);
        Bukkit.getPluginManager().registerEvents(new AdminStatsGUI(), this);
        Bukkit.getPluginManager().registerEvents(new StatsGUI(), this);
        Bukkit.getPluginManager().registerEvents(new LevelListener(), this);

        // Register command executors and tab completers.
        Commands commands = new Commands();
        getCommand("class").setExecutor(commands);
        getCommand("class").setTabCompleter(commands);
        getCommand("stats").setExecutor(commands);
        getCommand("stats").setTabCompleter(commands);
        getCommand("adminstats").setExecutor(commands);
        getCommand("adminstats").setTabCompleter(commands);
        // The "skilltree" command and associated functionality have been removed.
        getCommand("screload").setExecutor(new ReloadCommand());
        getCommand("xp").setExecutor(commands);

        String header = ChatColorUtil.translateHexColorCodes(
                getConfig().getString("ui.header", "&#00FF7F&lSANDCORE PLUGIN LOADED!")
        );
        getLogger().info(header);
        getLogger().info("************************************************");
        getLogger().info("SandCore Plugin Loaded!");
        getLogger().info("************************************************");
    }

    @Override
    public void onDisable() {
        PlayerDataManager.saveData();
        PlayerStatsManager.saveStats();
        LevelManager.saveData();
        // Remove any active boss bars.
        for (Player player : Bukkit.getOnlinePlayers()) {
            LevelBarManager.removeLevelBar(player);
        }
    }

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

        public static void saveStats() {
            getInstance().getLogger().info("Player stats saved!");
        }
    }

    // 3. PlayerClassManager stores a player's chosen class and class change points.
    public static class PlayerClassManager {
        public static Map<UUID, String> playerClasses = new HashMap<>();
        private static Map<UUID, Integer> classChangePoints = new HashMap<>();

        public static String getPlayerClass(Player player) {
            return playerClasses.get(player.getUniqueId());
        }

        public static void setPlayerClass(Player player, String className) {
            playerClasses.put(player.getUniqueId(), className);
            PlayerDataManager.saveData();
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

    // 4. ClassSelectionGUI displays a configurable 27-slot GUI for class selection.
    public static class ClassSelectionGUI implements Listener {
        public static void open(Player player) {
            int size = 27;
            String title = ChatColor.GOLD + "Select Your Class";
            Inventory gui = Bukkit.createInventory(null, size, title);

            // Optionally fill unused slots with a filler item.
            for (int i = 0; i < size; i++) {
                gui.setItem(i, new ItemStack(Material.AIR));
            }

            // Load classes from classes.yml.
            File classesFile = new File(getInstance().getDataFolder(), "classes.yml");
            YamlConfiguration classesConfig = YamlConfiguration.loadConfiguration(classesFile);
            ConfigurationSection classesSection = classesConfig.getConfigurationSection("classes");
            if (classesSection == null) {
                getInstance().getLogger().warning("No classes found in classes.yml!");
                player.sendMessage(ChatColor.RED + "No classes configured!");
                return;
            }

            // Retrieve icon slots from config (fallback if not specified).
            FileConfiguration config = getInstance().getConfig();
            ConfigurationSection guiConfig = config.getConfigurationSection("guis.class_selection");
            List<Integer> iconSlots;
            if (guiConfig != null && guiConfig.contains("icon_slots")) {
                iconSlots = guiConfig.getIntegerList("icon_slots");
            } else {
                iconSlots = Arrays.asList(10, 11, 12, 13, 14);
            }

            Set<String> classKeys = classesSection.getKeys(false);
            int slotIndex = 0;
            for (String key : classKeys) {
                ConfigurationSection classSection = classesSection.getConfigurationSection(key);
                String materialStr = classSection.getString("material", "STONE").toUpperCase();
                Material mat = Material.getMaterial(materialStr);
                if (mat == null) {
                    mat = Material.STONE;
                }
                ItemStack classItem = new ItemStack(mat);
                ItemMeta meta = classItem.getItemMeta();
                String displayName = ChatColorUtil.translateHexColorCodes(classSection.getString("display_name", key));
                meta.setDisplayName(displayName);
                List<String> lore = classSection.getStringList("lore");
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColorUtil.translateHexColorCodes(line));
                }
                meta.setLore(coloredLore);
                classItem.setItemMeta(meta);

                int slot;
                if (slotIndex < iconSlots.size()) {
                    slot = iconSlots.get(slotIndex);
                } else {
                    slot = slotIndex;
                    if (slot >= size) break;
                }
                gui.setItem(slot, classItem);
                slotIndex++;
            }
            player.openInventory(gui);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals(ChatColor.GOLD + "Select Your Class")) return;
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            Player player = (Player) event.getWhoClicked();
            String chosenClass = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
            PlayerClassManager.setPlayerClass(player, chosenClass);
            player.sendMessage(ChatColor.GREEN + "You have selected the " + chosenClass + " class!");
            player.closeInventory();
        }
    }

    // 5. StatsUpgradeGUI for players to upgrade their stats (old 9-slot GUI).
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
                player.sendMessage(ChatColor.GREEN + "Your Health has been increased by 5.0!");
                upgraded = true;
            } else if (stat.contains("damage")) {
                stats.setDamage(stats.getDamage() + 1.0);
                player.sendMessage(ChatColor.GREEN + "Your Damage has been increased by 1.0!");
                upgraded = true;
            } else if (stat.contains("speed")) {
                stats.setSpeed(stats.getSpeed() + 0.02);
                player.sendMessage(ChatColor.GREEN + "Your Speed has been increased by 0.02!");
                upgraded = true;
            } else if (stat.contains("defense")) {
                stats.setDefense(stats.getDefense() + 0.5);
                player.sendMessage(ChatColor.GREEN + "Your Defense has been increased by 0.5!");
                upgraded = true;
            }
            if (upgraded) {
                stats.applyToPlayer(player);
                // Refresh the GUI with updated stats.
                open(player);
            } else {
                player.sendMessage(ChatColor.RED + "You have no available stat points!");
            }
        }
    }

    // 6. StatsGUI – a new, detailed 27-slot GUI that shows your current stats and allows upgrades with clear messages.
    public static class StatsGUI implements Listener {
        public static void open(Player player) {
            Inventory gui = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&aYour Stats (Click to upgrade)"));

            PlayerStats stats = PlayerStatsManager.getPlayerStats(player);

            // Health item.
            ItemStack healthItem = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta healthMeta = healthItem.getItemMeta();
            String healthName = ChatColor.translateAlternateColorCodes('&', "&cHealth: " + stats.getHealth());
            healthMeta.setDisplayName(healthName);
            List<String> healthLore = new ArrayList<>();
            healthLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to increase Health by 5"));
            healthMeta.setLore(healthLore);
            healthItem.setItemMeta(healthMeta);
            gui.setItem(10, healthItem);

            // Damage item.
            ItemStack damageItem = new ItemStack(Material.TNT);
            ItemMeta damageMeta = damageItem.getItemMeta();
            String damageName = ChatColor.translateAlternateColorCodes('&', "&cDamage: " + stats.getDamage());
            damageMeta.setDisplayName(damageName);
            List<String> damageLore = new ArrayList<>();
            damageLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to increase Damage by 1"));
            damageMeta.setLore(damageLore);
            damageItem.setItemMeta(damageMeta);
            gui.setItem(11, damageItem);

            // Speed item.
            ItemStack speedItem = new ItemStack(Material.FEATHER);
            ItemMeta speedMeta = speedItem.getItemMeta();
            String speedName = ChatColor.translateAlternateColorCodes('&', "&cSpeed: " + stats.getSpeed());
            speedMeta.setDisplayName(speedName);
            List<String> speedLore = new ArrayList<>();
            speedLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to increase Speed by 0.02"));
            speedMeta.setLore(speedLore);
            speedItem.setItemMeta(speedMeta);
            gui.setItem(12, speedItem);

            // Defense item.
            ItemStack defenseItem = new ItemStack(Material.IRON_CHESTPLATE);
            ItemMeta defenseMeta = defenseItem.getItemMeta();
            String defenseName = ChatColor.translateAlternateColorCodes('&', "&cDefense: " + stats.getDefense());
            defenseMeta.setDisplayName(defenseName);
            List<String> defenseLore = new ArrayList<>();
            defenseLore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to increase Defense by 0.5"));
            defenseMeta.setLore(defenseLore);
            defenseItem.setItemMeta(defenseMeta);
            gui.setItem(13, defenseItem);

            player.openInventory(gui);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().contains("Your Stats")) return;
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            Player player = (Player) event.getWhoClicked();
            String clickedItem = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            PlayerStats stats = PlayerStatsManager.getPlayerStats(player);

            if(clickedItem.contains("Health")){
                stats.setHealth(stats.getHealth() + 5.0);
                stats.applyToPlayer(player);
                player.sendMessage(ChatColor.GREEN + "Your Health increased by 5.0!");
            } else if(clickedItem.contains("Damage")){
                stats.setDamage(stats.getDamage() + 1.0);
                stats.applyToPlayer(player);
                player.sendMessage(ChatColor.GREEN + "Your Damage increased by 1.0!");
            } else if(clickedItem.contains("Speed")){
                stats.setSpeed(stats.getSpeed() + 0.02);
                stats.applyToPlayer(player);
                player.sendMessage(ChatColor.GREEN + "Your Speed increased by 0.02!");
            } else if(clickedItem.contains("Defense")){
                stats.setDefense(stats.getDefense() + 0.5);
                stats.applyToPlayer(player);
                player.sendMessage(ChatColor.GREEN + "Your Defense increased by 0.5!");
            }

            // Refresh the GUI with updated stats.
            open(player);
        }
    }

    // 7. AdminStatsGUI lets admins view and modify a player's stats in an expanded (27-slot) GUI.
    public static class AdminStatsGUI implements Listener {
        public static void open(Player admin, Player target) {
            int size = 27;
            Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_RED + "Admin: " + target.getName() + " Stats");
            PlayerStats stats = PlayerStatsManager.getPlayerStats(target);

            // Place stat items in chosen slots.
            ItemStack health = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta hMeta = health.getItemMeta();
            hMeta.setDisplayName(ChatColor.RED + "Health: " + stats.getHealth());
            health.setItemMeta(hMeta);
            inv.setItem(10, health);

            ItemStack damage = new ItemStack(Material.TNT);
            ItemMeta dMeta = damage.getItemMeta();
            dMeta.setDisplayName(ChatColor.RED + "Damage: " + stats.getDamage());
            damage.setItemMeta(dMeta);
            inv.setItem(11, damage);

            ItemStack speed = new ItemStack(Material.FEATHER);
            ItemMeta sMeta = speed.getItemMeta();
            sMeta.setDisplayName(ChatColor.RED + "Speed: " + stats.getSpeed());
            speed.setItemMeta(sMeta);
            inv.setItem(12, speed);

            ItemStack defense = new ItemStack(Material.IRON_CHESTPLATE);
            ItemMeta defMeta = defense.getItemMeta();
            defMeta.setDisplayName(ChatColor.RED + "Defense: " + stats.getDefense());
            defense.setItemMeta(defMeta);
            inv.setItem(13, defense);

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

    // 8. PlayerDataManager handles persistent loading/saving of player data.
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
                ConfigurationSection classesSection = playerDataConfig.getConfigurationSection("classes");
                for (String uuidStr : classesSection.getKeys(false)) {
                    String savedClass = classesSection.getString(uuidStr);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        PlayerClassManager.playerClasses.put(uuid, savedClass);
                    } catch (IllegalArgumentException e) { }
                }
            }
            // Load player levels (legacy, if any).
            if (playerDataConfig.contains("levels")) {
                ConfigurationSection levelsSection = playerDataConfig.getConfigurationSection("levels");
                for (String uuidStr : levelsSection.getKeys(false)) {
                    int level = levelsSection.getInt(uuidStr);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        PlayerLevelManager.playerLevels.put(uuid, level);
                    } catch (IllegalArgumentException e) { }
                }
            }
            // Load player skill points.
            if (playerDataConfig.contains("skillpoints")) {
                ConfigurationSection spSection = playerDataConfig.getConfigurationSection("skillpoints");
                for (String uuidStr : spSection.getKeys(false)) {
                    int sp = spSection.getInt(uuidStr);
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

    // 9. PlayerLevelManager handles basic level and skill point storage (legacy).
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
            playerSkillPoints.put(player.getUniqueId(), getSkillPoints(player) + xp);
            player.sendMessage(ChatColor.GREEN + "You've reached level " + newLevel + " and earned " + xp + " skill point(s)!");
        }

        public static void useSkillPoint(Player player) {
            int sp = getSkillPoints(player);
            if (sp > 0) {
                playerSkillPoints.put(player.getUniqueId(), sp - 1);
            }
        }
    }

    // 10. Commands handles all command logic, tab auto-completion, and now the new xp command.
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
                        player.sendMessage(ChatColor.YELLOW + "Usage: /stats <upgrade|add|view>");
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
                    } else if (subCmd.equals("view")) {
                        StatsGUI.open(player);
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
                case "xp":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.YELLOW + "Usage: /xp <combat|lumber|mining|fishing> <amount>");
                        return true;
                    }
                    String skillStr = args[0].toLowerCase();
                    int xpToAdd;
                    try {
                        xpToAdd = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Amount must be a number.");
                        return true;
                    }
                    LevelManager.SkillType skillType;
                    switch (skillStr) {
                        case "combat":
                            skillType = LevelManager.SkillType.COMBAT;
                            break;
                        case "lumber":
                            skillType = LevelManager.SkillType.LUMBER;
                            break;
                        case "mining":
                            skillType = LevelManager.SkillType.MINING;
                            break;
                        case "fishing":
                            skillType = LevelManager.SkillType.FISHING;
                            break;
                        default:
                            player.sendMessage(ChatColor.RED + "Unknown skill type. Use combat, lumber, mining, or fishing.");
                            return true;
                    }
                    LevelManager.addExperience(player, skillType, xpToAdd);
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
                    completions.add("view");
                }
            } else if (cmd.equals("adminstats")) {
                if (args.length == 1) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        completions.add(p.getName());
                    }
                }
            } else if (cmd.equals("xp")) {
                if (args.length == 1) {
                    completions.add("combat");
                    completions.add("lumber");
                    completions.add("mining");
                    completions.add("fishing");
                }
            }
            return completions;
        }
    }

    // 11. ReloadCommand handles /screload to reload the configuration.
    public class ReloadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("sandcore.reload")) {
                sender.sendMessage(ChatColorUtil.translateHexColorCodes("&#FF0000⛔ You don't have permission to reload the plugin!"));
                return true;
            }
            reloadConfig();
            String version = getDescription().getVersion();
            String msg = getConfig().getString("ui.reload-message", "&#FFD700Config reloaded!").replace("%version%", version);
            sender.sendMessage(ChatColorUtil.translateHexColorCodes(msg));
            return true;
        }
    }

    // ===============================
    // New Leveling System and BossBar
    // ===============================

    // A. PlayerLevelData holds detailed leveling data.
    public static class PlayerLevelData {
        private int combatLevel;
        private double combatXP;
        private int lumberLevel;
        private double lumberXP;
        private int miningLevel;
        private double miningXP;
        private int fishingLevel;
        private double fishingXP;

        public PlayerLevelData() {
            this.combatLevel = 1;
            this.combatXP = 0.0;
            this.lumberLevel = 1;
            this.lumberXP = 0.0;
            this.miningLevel = 1;
            this.miningXP = 0.0;
            this.fishingLevel = 1;
            this.fishingXP = 0.0;
        }

        public int getCombatLevel() { return combatLevel; }
        public void setCombatLevel(int combatLevel) { this.combatLevel = combatLevel; }
        public double getCombatXP() { return combatXP; }
        public void setCombatXP(double combatXP) { this.combatXP = combatXP; }
        public int getLumberLevel() { return lumberLevel; }
        public void setLumberLevel(int lumberLevel) { this.lumberLevel = lumberLevel; }
        public double getLumberXP() { return lumberXP; }
        public void setLumberXP(double lumberXP) { this.lumberXP = lumberXP; }
        public int getMiningLevel() { return miningLevel; }
        public void setMiningLevel(int miningLevel) { this.miningLevel = miningLevel; }
        public double getMiningXP() { return miningXP; }
        public void setMiningXP(double miningXP) { this.miningXP = miningXP; }
        public int getFishingLevel() { return fishingLevel; }
        public void setFishingLevel(int fishingLevel) { this.fishingLevel = fishingLevel; }
        public double getFishingXP() { return fishingXP; }
        public void setFishingXP(double fishingXP) { this.fishingXP = fishingXP; }
    }

    // B. LevelManager manages XP/levels and persistent storage in leveldata.yml.
    public static class LevelManager {
        public enum SkillType {
            COMBAT, LUMBER, MINING, FISHING
        }
        private static Map<UUID, PlayerLevelData> levelData = new HashMap<>();
        private static File levelDataFile;
        private static FileConfiguration levelConfig;

        public static void loadData() {
            levelDataFile = new File(getInstance().getDataFolder(), "leveldata.yml");
            if (!levelDataFile.exists()) {
                try {
                    levelDataFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            levelConfig = YamlConfiguration.loadConfiguration(levelDataFile);
            for (String key : levelConfig.getKeys(false)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(key);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                PlayerLevelData data = new PlayerLevelData();
                data.setCombatLevel(levelConfig.getInt(key + ".combatLevel", 1));
                data.setCombatXP(levelConfig.getDouble(key + ".combatXP", 0.0));
                data.setLumberLevel(levelConfig.getInt(key + ".lumberLevel", 1));
                data.setLumberXP(levelConfig.getDouble(key + ".lumberXP", 0.0));
                data.setMiningLevel(levelConfig.getInt(key + ".miningLevel", 1));
                data.setMiningXP(levelConfig.getDouble(key + ".miningXP", 0.0));
                data.setFishingLevel(levelConfig.getInt(key + ".fishingLevel", 1));
                data.setFishingXP(levelConfig.getDouble(key + ".fishingXP", 0.0));
                levelData.put(uuid, data);
            }
        }

        public static void saveData() {
            for (Map.Entry<UUID, PlayerLevelData> entry : levelData.entrySet()) {
                String key = entry.getKey().toString();
                PlayerLevelData data = entry.getValue();
                levelConfig.set(key + ".combatLevel", data.getCombatLevel());
                levelConfig.set(key + ".combatXP", data.getCombatXP());
                levelConfig.set(key + ".lumberLevel", data.getLumberLevel());
                levelConfig.set(key + ".lumberXP", data.getLumberXP());
                levelConfig.set(key + ".miningLevel", data.getMiningLevel());
                levelConfig.set(key + ".miningXP", data.getMiningXP());
                levelConfig.set(key + ".fishingLevel", data.getFishingLevel());
                levelConfig.set(key + ".fishingXP", data.getFishingXP());
            }
            try {
                levelConfig.save(levelDataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static PlayerLevelData getPlayerLevelData(Player player) {
            return levelData.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerLevelData());
        }

        public static void addExperience(Player player, SkillType skill, int xpToAdd) {
            PlayerLevelData data = getPlayerLevelData(player);
            double currentXP = 0.0;
            int currentLevel = 0;
            double xpPerLevel = 0.0;
            FileConfiguration config = getInstance().getConfig();
            switch (skill) {
                case COMBAT:
                    currentXP = data.getCombatXP();
                    currentLevel = data.getCombatLevel();
                    xpPerLevel = config.getDouble("leveling.combat.xp_per_level", 1000);
                    break;
                case LUMBER:
                    currentXP = data.getLumberXP();
                    currentLevel = data.getLumberLevel();
                    xpPerLevel = config.getDouble("leveling.skills.lumber.xp_per_level", 500);
                    break;
                case MINING:
                    currentXP = data.getMiningXP();
                    currentLevel = data.getMiningLevel();
                    xpPerLevel = config.getDouble("leveling.skills.mining.xp_per_level", 500);
                    break;
                case FISHING:
                    currentXP = data.getFishingXP();
                    currentLevel = data.getFishingLevel();
                    xpPerLevel = config.getDouble("leveling.skills.fishing.xp_per_level", 300);
                    break;
            }
            currentXP += xpToAdd;
            boolean leveledUp = false;
            while (currentXP >= xpPerLevel) {
                currentXP -= xpPerLevel;
                currentLevel++;
                leveledUp = true;
            }
            switch (skill) {
                case COMBAT:
                    data.setCombatXP(currentXP);
                    data.setCombatLevel(currentLevel);
                    break;
                case LUMBER:
                    data.setLumberXP(currentXP);
                    data.setLumberLevel(currentLevel);
                    break;
                case MINING:
                    data.setMiningXP(currentXP);
                    data.setMiningLevel(currentLevel);
                    break;
                case FISHING:
                    data.setFishingXP(currentXP);
                    data.setFishingLevel(currentLevel);
                    break;
            }
            if (leveledUp) {
                player.sendMessage(ChatColor.GOLD + "Congratulations! Your " + skill.name().toLowerCase() + " skill leveled up to " + currentLevel + "!");
            } else {
                player.sendMessage(ChatColor.BLUE + "You gained " + xpToAdd + " XP in " + skill.name().toLowerCase() + " (" + currentXP + "/" + xpPerLevel + " XP)");
            }
            if (skill == SkillType.COMBAT) {
                LevelBarManager.updateLevelBar(player, Math.min(Math.max(currentXP / xpPerLevel, 0.0), 1.0), currentLevel);
            }
            // Save asynchronously.
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    saveData();
                }
            }.runTaskAsynchronously(getInstance());
        }
    }

    // C. LevelBarManager displays a BossBar for combat progression.
    public static class LevelBarManager {
        private static Map<UUID, BossBar> bossBars = new HashMap<>();

        public static void updateLevelBar(Player player, double progress, int level) {
            BossBar bar = bossBars.get(player.getUniqueId());
            if (bar == null) {
                bar = Bukkit.createBossBar(ChatColor.GREEN + "Combat Level: " + level, BarColor.BLUE, BarStyle.SOLID);
                bossBars.put(player.getUniqueId(), bar);
                bar.addPlayer(player);
            } else {
                bar.setTitle(ChatColor.GREEN + "Combat Level: " + level);
            }
            bar.setProgress(Math.min(Math.max(progress, 0.0), 1.0));
        }

        public static void removeLevelBar(Player player) {
            BossBar bar = bossBars.remove(player.getUniqueId());
            if (bar != null) {
                bar.removeAll();
            }
        }
    }

    // D. LevelListener hides vanilla XP and sets up the BossBar on join.
    public static class LevelListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            // Hide vanilla XP.
            player.setLevel(0);
            player.setExp(0);
            PlayerLevelData data = LevelManager.getPlayerLevelData(player);
            double xpPerLevel = getInstance().getConfig().getDouble("leveling.combat.xp_per_level", 1000);
            double progress = data.getCombatXP() / xpPerLevel;
            LevelBarManager.updateLevelBar(player, Math.min(Math.max(progress, 0.0), 1.0), data.getCombatLevel());
        }
    }
}