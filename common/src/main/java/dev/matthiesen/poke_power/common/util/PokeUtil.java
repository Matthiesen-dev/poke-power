package dev.matthiesen.poke_power.common.util;

import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemBuilder;
import dev.matthiesen.poke_power.common.config.PokePowerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class PokeUtil {
    private final Pokemon pokemon;
    private final Gender gender;

    public PokeUtil(@NotNull Pokemon pokemon) {
        this.pokemon = pokemon;
        this.gender = pokemon.getGender();
    }

    public ItemStack toItem() {
        return new ItemBuilder(PokemonItem.from(pokemon, 1))
                .hideAdditional()
                .addLore(getLore())
                .setCustomName(customNameBuilder())
                .build();
    }

    private MutableComponent customNameBuilder() {
        MutableComponent component = pokemon.getSpecies().getTranslatedName().copy().withStyle(ChatFormatting.GRAY);
        if (pokemon.getShiny()) {
            component.append(Component.literal(" ★").withStyle(ChatFormatting.GOLD));
        }

        // Append the gender symbol to the name if the Pokemon has a gender
        if (gender != Gender.GENDERLESS) {
            component.append(Component.literal(" " + parseShowdownGender())
                    .withStyle(getGenderColor()));
        }

        return component;
    }

    private String parseShowdownGender() {
        return switch (gender) {
            case MALE -> "♂";
            case FEMALE -> "♀";
            case GENDERLESS -> "○";
        };
    }

    private ChatFormatting getGenderColor() {
        return switch (gender) {
            case MALE -> ChatFormatting.BLUE;
            case FEMALE -> ChatFormatting.LIGHT_PURPLE;
            case GENDERLESS -> ChatFormatting.GRAY;
        };
    }

    private Component[] buildComponentList(List<Component> components) {
        return components.toArray(new Component[0]);
    }

    private String getPowerPerTick() {
        String format = "%s FE/t";
        int powerPerPokeLevel = PokePowerConfig.SERVER_CONFIG.powerPerPokeLevel.getAsInt();
        int power = pokemon.getLevel() * powerPerPokeLevel;
        return String.format(format, power);
    }

    private Component[] getLore() {
        String pokeball = pokemon.getCaughtBall().item().getDefaultInstance().getDisplayName().getString();
        String level = String.valueOf(pokemon.getLevel());
        String nickname = pokemon.getNickname() != null ? pokemon.getNickname().getString() : "No nickname";

        List<Component> pokeLore = new ArrayList<>();

        pokeLore.add(Component.literal(pokeball).setStyle(Style.EMPTY.withItalic(true)
                .withColor(ChatFormatting.DARK_GRAY)));
        pokeLore.add(Component.literal("Level: ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(level).withStyle(ChatFormatting.WHITE)));
        pokeLore.add(Component.literal("Nickname: ").withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal(nickname).withStyle(ChatFormatting.WHITE)));

        pokeLore.add(Component.empty());

        pokeLore.add(Component.literal("Power Generation: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(getPowerPerTick()).withStyle(ChatFormatting.WHITE)));

        return buildComponentList(pokeLore);
    }
}
