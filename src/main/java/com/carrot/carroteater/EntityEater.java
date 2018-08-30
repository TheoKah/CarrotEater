package com.carrot.carroteater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EnderCrystal;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.complex.EnderDragon;
import org.spongepowered.api.entity.living.complex.EnderDragonPart;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class EntityEater {
	public enum Type {
		ITEM,
		XP,
		MONSTER,
		ANIMAL
	}

	static private Map<Type, Predicate> filters;
	static private HashSet<UUID> marked = new HashSet<>();

	static void init() {
		filters = new HashMap<>();
		Predicate playerPredicate = e -> !(e instanceof Player);
		Predicate dragonPredicate = e -> !(e instanceof EnderDragon);
		Predicate dragon2Predicate = e -> !(e instanceof EnderDragonPart);
		Predicate crystalPredicate = e -> !(e instanceof EnderCrystal);
		Predicate mainPredicate = playerPredicate.and(dragonPredicate).and(dragon2Predicate).and(crystalPredicate);
		Predicate<Item> whitelistPredicate = item -> !ConfigHandler.isWhitelisted(item);
		Predicate<Item> itemPredicate = item -> item.get(Keys.REPRESENTED_ITEM).isPresent();

		filters.put(Type.ITEM, mainPredicate.and(e -> e instanceof Item).and(itemPredicate).and(whitelistPredicate));
		filters.put(Type.XP, mainPredicate.and(e -> e instanceof ExperienceOrb));
		filters.put(Type.MONSTER, mainPredicate.and(e -> e instanceof Monster));
		filters.put(Type.ANIMAL, mainPredicate.and(e -> e instanceof Animal));

	}

	static Text countEntitiesWorld(String world) {
		Optional<World> w = Sponge.getServer().getWorld(world);

		if (w.isPresent())
			return countEntitiesWorld(w.get());
		return Text.of(TextColors.YELLOW, world, TextColors.GRAY, ": ",TextColors.LIGHT_PURPLE, "world not found");
	}

	static Text countEntitiesWorld(World world) {
		int all = world.getEntities().size();
		int items = countItems(world);
		int xp = countXP(world);
		int monsters = countMonsters(world);
		int animals = countAnimals(world);

		return Text.of(TextColors.YELLOW, world.getName(), TextColors.GRAY, ": ",
				TextColors.DARK_RED, items, TextColors.LIGHT_PURPLE, " items on the ground", TextColors.GRAY, ", ",
				TextColors.DARK_RED, xp, TextColors.LIGHT_PURPLE, " xp orbs", TextColors.GRAY, ", ",
				TextColors.DARK_RED, monsters, TextColors.LIGHT_PURPLE, " monsters", TextColors.GRAY, ", ",
				TextColors.DARK_RED, animals, TextColors.LIGHT_PURPLE, " animals", TextColors.GRAY, ", ",
				TextColors.DARK_RED, all, TextColors.LIGHT_PURPLE, " total", TextColors.GRAY, ", ");
	}

	static List<Text> countEntities() {
		List<Text> out = new ArrayList<>();
		for (World world : Sponge.getServer().getWorlds())
			out.add(countEntitiesWorld(world));
		return out;
	}

	static int countItems(World world) {
		return world.getEntities(filters.get(Type.ITEM)).size();
	}

	static int countXP(World world) {
		return world.getEntities(filters.get(Type.XP)).size();
	}

	static int countMonsters(World world) {
		return world.getEntities(filters.get(Type.MONSTER)).size();
	}

	static int countAnimals(World world) {
		return world.getEntities(filters.get(Type.ANIMAL)).size();
	}

	static int countItems() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += countItems(world);
		return count;
	}

	static int countXP() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += countXP(world);
		return count;
	}

	static int countMonsters() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += countMonsters(world);
		return count;
	}

	static int countAnimals() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += countAnimals(world);
		return count;
	}
	
	static void unmark() {
		marked.clear();
	}
	
	static int mark(Collection<Entity> entities) {
		int count = entities.size();
		entities.forEach(e -> {
			marked.add(e.getUniqueId());
		});
		return count;
	}

	static int eat(Collection<Entity> entities) {
		int count = entities.size();
		entities.forEach(e -> {
			if (marked.contains(e.getUniqueId())) {
				SecondChance.saveItem(e);
				e.remove();
			}
		});
		return count;
	}

	static int eatItems(World world) {
		return eat(world.getEntities(filters.get(Type.ITEM)));
	}

	static int eatXP(World world) {
		return eat(world.getEntities(filters.get(Type.XP)));
	}

	static int eatMonsters(World world) {
		return eat(world.getEntities(filters.get(Type.MONSTER)));
	}

	static int eatAnimals(World world) {
		return eat(world.getEntities(filters.get(Type.ANIMAL)));
	}

	static int eatItems() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += eatItems(world);
		return count;
	}

	static int eatXP() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += eatXP(world);
		return count;
	}

	static int eatMonsters() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += eatMonsters(world);
		return count;
	}

	static int eatAnimals() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += eatAnimals(world);
		return count;
	}
	
	static int markItems(World world) {
		return mark(world.getEntities(filters.get(Type.ITEM)));
	}

	static int markXP(World world) {
		return mark(world.getEntities(filters.get(Type.XP)));
	}

	static int markMonsters(World world) {
		return mark(world.getEntities(filters.get(Type.MONSTER)));
	}

	static int markAnimals(World world) {
		return mark(world.getEntities(filters.get(Type.ANIMAL)));
	}

	static int markItems() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += markItems(world);
		return count;
	}

	static int markXP() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += markXP(world);
		return count;
	}

	static int markMonsters() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += markMonsters(world);
		return count;
	}

	static int markAnimals() {
		int count = 0;
		for (World world : Sponge.getServer().getWorlds())
			count += markAnimals(world);
		return count;
	}
}
