package dev.ftb.packcompanion.features.reciperemover;

import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.packcompanion.core.Feature;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeRemover extends Feature.Common {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeRemover.class);
    private static final Set<Identifier> EMPTY_SET = Set.of();

    private static Set<Identifier> removedRecipes;

    private static final Path REMOVED_RECIPES_PATH = Path.of("config", "ftbpc", "removals");

    public RecipeRemover(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        try {
            Files.createDirectories(REMOVED_RECIPES_PATH);
        } catch (Exception e) {
            LOGGER.error("Failed to create directory {}: {}", REMOVED_RECIPES_PATH, e.getMessage());
        }
    }

    @Override
    public List<LiteralArgumentBuilder<CommandSourceStack>> commands(CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        return List.of(Commands.literal("recipe-remover").then(Commands.literal("reload")
                .executes(this::reload)
        ));
    }

    public static Set<Identifier> getRemovedRecipes() {
        if (removedRecipes == null) {
            removedRecipes = loadRemovedRecipes();
        }

        return removedRecipes;
    }

    private static Set<Identifier> loadRemovedRecipes() {
        if (!Files.exists(REMOVED_RECIPES_PATH)) {
            return EMPTY_SET;
        }

        List<Path> jsonFiles;
        try (var files = Files.list(REMOVED_RECIPES_PATH)) {
            jsonFiles = files
                    .filter(Files::isRegularFile)
                    .filter(e -> e.toString().endsWith(".json"))
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to list files in {}: {}", REMOVED_RECIPES_PATH, e.getMessage());
            return EMPTY_SET;
        }

        Set<Identifier> removedRecipesSet = new HashSet<>();
        for (Path jsonFile : jsonFiles) {
            try {
                var namespace = jsonFile.getFileName().toString().replace(".json", "");
                var jsonContent = Files.readString(jsonFile);
                var jsonArray = JsonParser.parseString(jsonContent);
                if (!jsonArray.isJsonArray()) {
                    LOGGER.error("Expected a JSON array in {}, but found: {}", jsonFile, jsonArray);
                    continue;
                }

                var recipes = jsonArray.getAsJsonArray();
                for (var recipeElement : recipes) {
                    if (!recipeElement.isJsonPrimitive() || !recipeElement.getAsJsonPrimitive().isString()) {
                        LOGGER.error("Expected a string in JSON array in {}, but found: {}", jsonFile, recipeElement);
                        continue;
                    }

                    var recipeName = recipeElement.getAsString();
                    var resourceLocation = Identifier.fromNamespaceAndPath(namespace, recipeName);
                    removedRecipesSet.add(resourceLocation);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse removed recipes from {}: {}", jsonFile, e.getMessage());
            }
        }

        return removedRecipesSet;
    }

    private int reload(CommandContext<CommandSourceStack> context) {
        removedRecipes = null;

        context.getSource().sendSuccess(() -> Component.literal("Recipe remover reloaded, run ").append(
                Component.literal("/reload").withStyle(style -> style.withColor(0xFFAA00).withClickEvent(new ClickEvent.RunCommand("/reload")))
        ).append(Component.literal(" to see changes.")), false);
        return Command.SINGLE_SUCCESS;
    }
}
