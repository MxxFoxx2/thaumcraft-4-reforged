package thaumcraft.entity.golem;

public enum GolemType {
    STRAW(10, 0, 0.38F, false, 1, 1, 75, 0, 0),
    WOOD(20, 6, 0.35F, false, 1, 4, 75, 1, 0),
    TALLOW(20, 9, 0.33F, false, 2, 8, 75, 2, 0),
    CLAY(25, 9, 0.33F, true, 1, 8, 100, 2, 0),
    FLESH(15, 6, 0.35F, false, 2, 4, 40, 1, 0),
    STONE(30, 12, 0.32F, true, 1, 16, 100, 3, 0),
    IRON(35, 15, 0.31F, true, 1, 32, 125, 4, 0),
    THAUMIUM(40, 15, 0.32F, true, 2, 32, 100, 4, 0),
    GRASS(10, 0, 0.38F, false, 2, 1, 75, 0, 750),
    DIRT(10, 0, 0.38F, false, 1, 1, 75, 0, 500),
    PLANT(10, 0, 0.4F, false, 2, 1, 75, 0, 750),
    ROCK(30, 12, 0.32F, true, 2, 16, 100, 3, 1000),
    METAL(35, 15, 0.31F, true, 2, 32, 125, 4, 1250),
    CLOTH(10, 0, 0.4F, false, 2, 1, 75, 0, 750),
    SAND(10, 0, 0.4F, false, 1, 1, 75, 0, 500),
    REDSTONE(20, 9, 0.35F, true, 2, 8, 45, 2, 1000),
    TNT(10, 0, 0.38F, false, 1, 1, 75, 0, 750),
    ICE(20, 6, 0.35F, false, 2, 4, 75, 1, 750),
    CACTUS(20, 6, 0.35F, false, 2, 4, 75, 1, 1000),
    CAKE(20, 6, 0.35F, false, 2, 4, 25, 1, 750),
    WEB(15, 6, 0.35F, false, 2, 4, 40, 1, 1000),
    VOID(25, 15, 0.4F, true, 2, 8, 20, 3, 0);

    public final int health;
    public final int armor;
    public final float speed;
    public final boolean fireResist;
    public final int upgrades;
    public final int carry;
    public final int regenDelay;
    public final int strength;
    public final int visCost;

    GolemType(int health, int armor, float speed, boolean fireResist, int upgrades, int carry, int regenDelay, int strength, int visCost) {
        this.health = health;
        this.armor = armor;
        this.speed = speed;
        this.fireResist = fireResist;
        this.upgrades = upgrades;
        this.carry = carry;
        this.regenDelay = regenDelay;
        this.strength = strength;
        this.visCost = visCost;
    }

    public static GolemType byId(int id) {
        return values()[Math.clamp(id, 0, values().length - 1)];
    }

    public String itemId() {
        return name().toLowerCase(java.util.Locale.ROOT) + "_golem";
    }
}
