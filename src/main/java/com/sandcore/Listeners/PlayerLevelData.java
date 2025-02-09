package com.sandcore;

public class PlayerLevelData {
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
        this.combatXP = 0;
        this.lumberLevel = 1;
        this.lumberXP = 0;
        this.miningLevel = 1;
        this.miningXP = 0;
        this.fishingLevel = 1;
        this.fishingXP = 0;
    }

    // Getters and setters (combat)
    public int getCombatLevel() { return combatLevel; }
    public void setCombatLevel(int combatLevel) { this.combatLevel = combatLevel; }
    public double getCombatXP() { return combatXP; }
    public void setCombatXP(double combatXP) { this.combatXP = combatXP; }

    // Getters and setters (lumber)
    public int getLumberLevel() { return lumberLevel; }
    public void setLumberLevel(int lumberLevel) { this.lumberLevel = lumberLevel; }
    public double getLumberXP() { return lumberXP; }
    public void setLumberXP(double lumberXP) { this.lumberXP = lumberXP; }

    // Getters and setters (mining)
    public int getMiningLevel() { return miningLevel; }
    public void setMiningLevel(int miningLevel) { this.miningLevel = miningLevel; }
    public double getMiningXP() { return miningXP; }
    public void setMiningXP(double miningXP) { this.miningXP = miningXP; }

    // Getters and setters (fishing)
    public int getFishingLevel() { return fishingLevel; }
    public void setFishingLevel(int fishingLevel) { this.fishingLevel = fishingLevel; }
    public double getFishingXP() { return fishingXP; }
    public void setFishingXP(double fishingXP) { this.fishingXP = fishingXP; }
}