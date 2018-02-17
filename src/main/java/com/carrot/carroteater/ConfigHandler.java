package com.carrot.carroteater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ConfigHandler
{
	private static File configFile;
	private static ConfigurationLoader<CommentedConfigurationNode> configManager;
	private static CommentedConfigurationNode config;
	
	private static List<String> whitelist;

	public static void init(File rootDir)
	{
		configFile = new File(rootDir, "config.conf");
		configManager = HoconConfigurationLoader.builder().setPath(configFile.toPath()).build();
		
		whitelist = new ArrayList<>();
	}

	public static void load()
	{
		load(null);
	}

	public static void load(CommandSource src)
	{
		// load file
		try
		{
			if (!configFile.exists())
			{
				configFile.getParentFile().mkdirs();
				configFile.createNewFile();
				config = configManager.load();
				configManager.save(config);
			}
			config = configManager.load();
		}
		catch (IOException e)
		{
			CarrotEater.getLogger().error("Error loading config");
			e.printStackTrace();
			if (src != null)
			{
				src.sendMessage(Text.of(TextColors.RED, "Error loading config"));
			}
		}

		// check integrity
		if (!config.getNode("config", "etrigger").hasListChildren()) {
			config.getNode("config", "etrigger").setComment("Number of entities that should trigger a forced entity clear, 0 to disable");
		}
		if (!config.getNode("config", "mtrigger").hasListChildren()) {
			config.getNode("config", "mtrigger").setComment("Number of monsters that should trigger a forced monster clear, 0 to disable");
		}
		if (!config.getNode("config", "timer").hasListChildren()) {
			config.getNode("config", "timer").setComment("Number of minutes between each forced clearlag, 0 to disable");
		}

		Utils.ensurePositiveNumber(config.getNode("config", "etrigger"), 399);
		Utils.ensurePositiveNumber(config.getNode("config", "mtrigger"), 0);
		Utils.ensurePositiveNumber(config.getNode("config", "timer"), 15);

		if (!config.getNode("whitelist").hasListChildren()) {
			config.getNode("whitelist").setComment("Item you want clearlag to ignore");
			config.getNode("whitelist").getAppendedNode().setValue("appliedenergistics2:crystal_seed");
		}
		
		for (ConfigurationNode item : config.getNode("whitelist").getChildrenList()) {
			whitelist.add(item.getString());
		}
		
		save();
		if (src != null)
		{
			src.sendMessage(Text.of(TextColors.GREEN, "Config reloaded"));
		}
	}

	public static void save()
	{
		try
		{
			configManager.save(config);
		}
		catch (IOException e)
		{
			CarrotEater.getLogger().error("Could not save config file !");
		}
	}
	
	public static boolean isWhitelisted(Item item) {
		return whitelist.contains(item.getItemType().getBlock().map(type -> type.getDefaultState().getId()).orElseGet(() -> item.getItemType().getId()));
	}

	public static CommentedConfigurationNode getNode(String... path)
	{
		return config.getNode((Object[]) path);
	}

	public static class Utils
	{
		public static void ensureString(CommentedConfigurationNode node, String def)
		{
			if (node.getString() == null)
			{
				node.setValue(def);
			}
		}

		public static void ensurePositiveNumber(CommentedConfigurationNode node, Number def)
		{
			if (!(node.getValue() instanceof Number) || node.getDouble(-1) < 0)
			{
				node.setValue(def);
			}
		}

		public static void ensureBoolean(CommentedConfigurationNode node, boolean def)
		{
			if (!(node.getValue() instanceof Boolean))
			{
				node.setValue(def);
			}
		}
	}
}
