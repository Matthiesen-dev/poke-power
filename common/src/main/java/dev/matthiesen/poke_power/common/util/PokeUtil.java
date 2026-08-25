package dev.matthiesen.poke_power.common.util;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.PokemonStats;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import dev.matthiesen.matthiesen_core.common.utility.item.ItemBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private List<MutableComponent> getMovesComponents() {
        String moveOne = !pokemon.getMoveSet().getMoves().isEmpty() ?
                Objects.requireNonNull(pokemon.getMoveSet().get(0)).getDisplayName().getString() : "Empty";
        String moveTwo = pokemon.getMoveSet().getMoves().size() >= 2 ?
                Objects.requireNonNull(pokemon.getMoveSet().get(1)).getDisplayName().getString() : "Empty";
        String moveThree = pokemon.getMoveSet().getMoves().size() >= 3 ?
                Objects.requireNonNull(pokemon.getMoveSet().get(2)).getDisplayName().getString() : "Empty";
        String moveFour = pokemon.getMoveSet().getMoves().size() >= 4 ?
                Objects.requireNonNull(pokemon.getMoveSet().get(3)).getDisplayName().getString() : "Empty";
        return List.of(
                Component.literal("Moves: ").withStyle(ChatFormatting.DARK_GREEN),
                Component.literal(" ").append(Component.literal(moveOne).withStyle(ChatFormatting.WHITE)),
                Component.literal(" ").append(Component.literal(moveTwo).withStyle(ChatFormatting.WHITE)),
                Component.literal(" ").append(Component.literal(moveThree).withStyle(ChatFormatting.WHITE)),
                Component.literal(" ").append(Component.literal(moveFour).withStyle(ChatFormatting.WHITE))
        );
    }

    private ChatFormatting getStatColor(Stats stat) {
        return switch (stat) {
            case HP -> ChatFormatting.RED;
            case ATTACK -> ChatFormatting.BLUE;
            case DEFENCE -> ChatFormatting.GRAY;
            case SPECIAL_ATTACK -> ChatFormatting.AQUA;
            case SPECIAL_DEFENCE -> ChatFormatting.YELLOW;
            case SPEED -> ChatFormatting.GREEN;
            case EVASION -> ChatFormatting.DARK_GREEN;
            case ACCURACY -> ChatFormatting.DARK_RED;
        };
    }

    private String getStatLabel(Stats stat) {
        return switch (stat) {
            case HP -> "HP";
            case ATTACK -> "Atk";
            case DEFENCE -> "Def";
            case SPECIAL_ATTACK -> "SpAtk";
            case SPECIAL_DEFENCE -> "SpDef";
            case SPEED -> "Spd";
            case EVASION -> "Evasion";
            case ACCURACY -> "Accuracy";
        };
    }

    private List<MutableComponent> getStatComponents(PokemonStats stats, String label, ChatFormatting labelColor) {
        List<MutableComponent> components = new ArrayList<>();

        components.add(Component.literal(label + ": ").withStyle(labelColor));

        MutableComponent inProgress = Component.empty();
        int currentProgress = 0;
        int maxPerLine = 3;

        for (Stats stat : Stats.values()) {
            if (stat == Stats.ACCURACY || stat == Stats.EVASION) {
                continue; // Skip Accuracy and Evasion stats
            }

            int statValue = stats.getOrDefault(stat);
            ChatFormatting color = getStatColor(stat);
            String statLabel = getStatLabel(stat);
            inProgress.append(Component.literal("  " + statLabel + ": ").withStyle(color)
                    .append(Component.literal(String.valueOf(statValue)).withStyle(ChatFormatting.WHITE)));
            currentProgress++;

            if (currentProgress >= maxPerLine) {
                components.add(inProgress);
                inProgress = Component.empty();
                currentProgress = 0;
            }
        }

        if (currentProgress > 0) {
            components.add(inProgress);
        }

        return components;
    }

    private Component[] buildComponentList(List<Component> components) {
        return components.toArray(new Component[0]);
    }

    private Component[] getLore() {
        String pokeball = pokemon.getCaughtBall().item().getDefaultInstance().getDisplayName().getString();
        String level = String.valueOf(pokemon.getLevel());
        String nickname = pokemon.getNickname() != null ? pokemon.getNickname().getString() : "No nickname";
        String heldItem = pokemon.heldItem().isEmpty() ? "No held item" : pokemon.heldItem().getDisplayName().getString();
        String OT = pokemon.getOriginalTrainerName() != null ? pokemon.getOriginalTrainerName() : "Unknown";
        MutableComponent nature = LocalizationUtilsKt.lang(pokemon.getNature().getDisplayName().replace("cobblemon.", ""));
        MutableComponent ability = LocalizationUtilsKt.lang(pokemon.getAbility().getDisplayName().replace("cobblemon.", ""));
        var ivs = getStatComponents(pokemon.getIvs(), "IVs", ChatFormatting.LIGHT_PURPLE);
        var evs = getStatComponents(pokemon.getEvs(), "EVs", ChatFormatting.DARK_AQUA);
        var moves = getMovesComponents();
        String form = pokemon.getForm().getName();

        List<Component> pokeLore = new ArrayList<>();

        pokeLore.add(Component.literal(pokeball).setStyle(Style.EMPTY.withItalic(true)
                .withColor(ChatFormatting.DARK_GRAY)));
        pokeLore.add(Component.literal("Level: ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(level).withStyle(ChatFormatting.WHITE)));
        pokeLore.add(Component.literal("Nickname: ").withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal(nickname).withStyle(ChatFormatting.WHITE)));
        pokeLore.add(Component.literal("Held Item: ").withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal(heldItem).withStyle(ChatFormatting.WHITE)));
        pokeLore.add(Component.literal("OT: ").withStyle(ChatFormatting.DARK_BLUE)
                .append(Component.literal(OT).withStyle(ChatFormatting.WHITE)));
        pokeLore.add(Component.literal("Nature: ").withStyle(ChatFormatting.YELLOW)
                .append(nature.withStyle(ChatFormatting.WHITE)));
        pokeLore.add(Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                .append(ability.withStyle(ChatFormatting.WHITE)));
        pokeLore.addAll(ivs);
        pokeLore.addAll(evs);
        pokeLore.addAll(moves);
        pokeLore.add(Component.literal("Form: ").withStyle(ChatFormatting.GOLD).append(Component.literal(form)));

        return buildComponentList(pokeLore);
    }
}
