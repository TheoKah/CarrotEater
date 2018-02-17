package com.carrot.carroteater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import com.google.inject.Inject;

@Plugin(id = "carroteater", name = "CarrotEater", authors={"Carrot"}, url="https://github.com/TheoKah/CarrotEater")
public class CarrotEater {
	private File rootDir;

	private static CarrotEater plugin;

	@Inject
	private Logger logger;

	@Inject
	@ConfigDir(sharedRoot = true)
	private File defaultConfigDir;

	@Listener
	public void onInit(GameInitializationEvent event) throws IOException
	{
		plugin = this;

		rootDir = new File(defaultConfigDir, "carroteater");

		ConfigHandler.init(rootDir);
		EntityEater.init();
	}

	@Listener
	public void onStart(GameStartedServerEvent event)
	{

		ConfigHandler.load();

		CommandSpec countEntity = CommandSpec.builder()
				.description(Text.of("Get entity count"))
				.permission("carroteater.admin.count")
				.executor(new CommandExecutor() {

					@Override
					public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
						Optional<WorldProperties> dim = args.<WorldProperties>getOne(Text.of("world"));
						List<Text> contents = new ArrayList<>();

						if (dim.isPresent()) {
							contents.add(EntityEater.countEntitiesWorld(dim.get().getWorldName()));
						} else {
							contents.addAll(EntityEater.countEntities());
						}

						PaginationList.builder()
						.title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, "Entities Count", TextColors.GOLD, " }"))
						.contents(contents)
						.padding(Text.of("-"))
						.sendTo(src);
						return CommandResult.success();
					}
				})
				.arguments(GenericArguments.optional(GenericArguments.world(Text.of("world"))))
				.build();

		CommandSpec laggyChunk = CommandSpec.builder()
				.description(Text.of("List of chunks sorted by entity count"))
				.permission("carroteater.admin.chunks.entity")
				.executor(new CommandExecutor() {

					@Override
					public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
						Optional<WorldProperties> dim = args.<WorldProperties>getOne(Text.of("world"));
						List<Text> contents = new ArrayList<>();
						String title = "Chunks";
						TreeMap<Chunk, Integer> sortedChunks = new TreeMap<>((o1, o2) -> Integer.compare(o2.getEntities().size(), o1.getEntities().size()));

						if (dim.isPresent()) {
							Optional<World> w = Sponge.getServer().getWorld(dim.get().getUniqueId());
							if (w.isPresent()) {
								title = "Chunks in " + w.get().getName();
								for (Chunk chunk : w.get().getLoadedChunks()) {
									if (chunk.getEntities().size() > 0)
										sortedChunks.put(chunk, chunk.getEntities().size());
								}
							} else {
								title = "World not found";
							}
						} else {
							for (World world : Sponge.getServer().getWorlds()) {
								for (Chunk chunk : world.getLoadedChunks()) {
									if (chunk.getEntities().size() > 0)
										sortedChunks.put(chunk, chunk.getEntities().size());
								}
							}
						}

						sortedChunks.forEach(((chunk, count) -> contents.add(
								Text.builder().append(Text.of(TextColors.YELLOW, (dim.isPresent() ? chunk.getWorld().getName() + " " : ""), chunk.getPosition().getX() + "," + chunk.getPosition().getZ() + " contains " + count + " entities."))
								.onClick(TextActions.executeCallback(player -> {
									((Player) player).setLocation(new Location<World>(chunk.getWorld(), chunk.getPosition().getX() * 16 + 8, 150, chunk.getPosition().getZ() * 16 + 8));
								})).build())));

						PaginationList.builder()
						.title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, title, TextColors.GOLD, " }"))
						.contents(contents)
						.padding(Text.of("-"))
						.sendTo(src);
						return CommandResult.success();
					}
				})
				.arguments(GenericArguments.optional(GenericArguments.world(Text.of("world"))))
				.build();

		CommandSpec laggyChunkTile = CommandSpec.builder()
				.description(Text.of("List of chunks sorted by tile count"))
				.permission("carroteater.admin.chunks.tile")
				.executor(new CommandExecutor() {

					@Override
					public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
						Optional<WorldProperties> dim = args.<WorldProperties>getOne(Text.of("world"));
						List<Text> contents = new ArrayList<>();
						String title = "Chunks";
						TreeMap<Chunk, Integer> sortedChunks = new TreeMap<>((o1, o2) -> Integer.compare(o2.getTileEntities().size(), o1.getTileEntities().size()));

						if (dim.isPresent()) {
							Optional<World> w = Sponge.getServer().getWorld(dim.get().getUniqueId());
							if (w.isPresent()) {
								title = "Chunks in " + w.get().getName();
								for (Chunk chunk : w.get().getLoadedChunks()) {
									if (chunk.getTileEntities().size() > 0)
										sortedChunks.put(chunk, chunk.getTileEntities().size());
								}
							} else {
								title = "World not found";
							}
						} else {
							for (World world : Sponge.getServer().getWorlds()) {
								for (Chunk chunk : world.getLoadedChunks()) {
									if (chunk.getTileEntities().size() > 0)
										sortedChunks.put(chunk, chunk.getTileEntities().size());
								}
							}
						}

						sortedChunks.forEach(((chunk, count) -> contents.add(
								Text.builder().append(Text.of(TextColors.YELLOW, (dim.isPresent() ? chunk.getWorld().getName() + " " : ""), chunk.getPosition().getX() + "," + chunk.getPosition().getZ() + " contains " + count + " tiles."))
								.onClick(TextActions.executeCallback(player -> {
									((Player) player).setLocation(new Location<World>(chunk.getWorld(), chunk.getPosition().getX() * 16 + 8, 150, chunk.getPosition().getZ() * 16 + 8));
								})).build())));

						PaginationList.builder()
						.title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, title, TextColors.GOLD, " }"))
						.contents(contents)
						.padding(Text.of("-"))
						.sendTo(src);
						return CommandResult.success();
					}
				})
				.arguments(GenericArguments.optional(GenericArguments.world(Text.of("world"))))
				.build();

		CommandSpec chunks = CommandSpec.builder()
				.description(Text.of("Get chunk list"))
				.permission("carroteater.admin.chunks")
				.child(laggyChunk, "entity", "entities", "e")
				.child(laggyChunkTile, "tile", "tiles", "t")
				.build();
		
		CommandSpec eat = CommandSpec.builder()
				.description(Text.of("Force remove items and entities"))
				.permission("carroteater.admin.eat")
				.executor(new CommandExecutor() {
					
					@Override
					public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
						new ClearItems().run();
						return CommandResult.success();
					}
				})
				.build();

		CommandSpec main = CommandSpec.builder()
				.description(Text.of("Main CarrotEater command"))
				.executor(new CommandExecutor() {

					@Override
					public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
						List<Text> contents = new ArrayList<>();

						contents.add(Text.of(TextColors.GOLD, "/ce count [world]", TextColors.GRAY, " - ", TextColors.YELLOW, "Count entities"));
						contents.add(Text.of(TextColors.GOLD, "/ce chunks <entity|tile> [world]", TextColors.GRAY, " - ", TextColors.YELLOW, "List chunks by entities or tile count"));
						contents.add(Text.of(TextColors.GOLD, "/ce eat", TextColors.GRAY, " - ", TextColors.YELLOW, "Force clear of Items and entities"));

						PaginationList.builder()
						.title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, "CarrotEater", TextColors.GOLD, " }"))
						.contents(contents)
						.padding(Text.of("-"))
						.sendTo(src);
						return CommandResult.success();
					}
				})
				.child(countEntity, "count")
				.child(chunks, "chunks")
				.child(eat, "eat")
				.build();

		Sponge.getCommandManager().register(plugin, main, "carroteater", "ce", "clag", "clearlag", "clagg");

		int delay = ConfigHandler.getNode("config", "timer").getInt(0);

		if (delay > 0) {
			Sponge.getScheduler()
			.createTaskBuilder()
			.execute(new ClearItems())
			.delay(delay, TimeUnit.MINUTES)
			.interval(delay, TimeUnit.MINUTES)
			.async()
			.submit(this);
		}
	}

	public static CarrotEater getInstance()
	{
		return plugin;
	}

	public static Logger getLogger()
	{
		return getInstance().logger;
	}

	public static Cause getCause()
	{
		return Cause.source(Sponge.getPluginManager().fromInstance(CarrotEater.getInstance()).get()).build();
	}
}
