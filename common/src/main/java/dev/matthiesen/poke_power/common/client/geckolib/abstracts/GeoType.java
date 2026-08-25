package dev.matthiesen.poke_power.common.client.geckolib.abstracts;

public enum GeoType {
    BLOCK("block"),
    ITEM("item");

    private final String name;

    GeoType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
