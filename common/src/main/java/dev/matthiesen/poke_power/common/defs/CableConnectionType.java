package dev.matthiesen.poke_power.common.defs;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum CableConnectionType implements StringRepresentable {
    NONE("none"),
    CABLE("cable"),
    MACHINE("machine");

    private final String serializedName;

    CableConnectionType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }
}
