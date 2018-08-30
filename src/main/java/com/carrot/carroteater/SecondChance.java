package com.carrot.carroteater;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.InvalidDataFormatException;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;


public class SecondChance {
	private static Hashtable<UUID, Hashtable<Vector3i, ArrayList<Tuple<Location<World>, String>>>> mem = new Hashtable<>();

	public static void saveItem(Entity e) {
		Optional<ItemStackSnapshot> item = e.get(Keys.REPRESENTED_ITEM);
		if (item.isPresent()) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				DataFormats.NBT.writeTo(os, item.get().toContainer());
				os.flush();
				if (!mem.containsKey(e.getLocation().getExtent().getUniqueId()))
					mem.put(e.getLocation().getExtent().getUniqueId(), new Hashtable<>());
				if (!mem.get(e.getLocation().getExtent().getUniqueId()).containsKey(e.getLocation().getChunkPosition()))
					mem.get(e.getLocation().getExtent().getUniqueId()).put(e.getLocation().getChunkPosition(), new ArrayList<>());
				mem.get(e.getLocation().getExtent().getUniqueId()).get(e.getLocation().getChunkPosition()).add(new Tuple<Location<World>, String>(e.getLocation(), Base64.getEncoder().encodeToString(os.toByteArray())));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static int popItems(UUID world, Vector3i chunk) {
		if (!mem.containsKey(world))
			return 0;
		if (!mem.get(world).containsKey(chunk))
			return 0;
		ArrayList<Tuple<Location<World>, String>> hand = mem.get(world).get(chunk);
		mem.get(world).remove(chunk);
		if (mem.get(world).isEmpty())
			mem.remove(world);
		int total = 0;
		for (Tuple<Location<World>, String> tuple : hand) {
			try {
				Optional<ItemStack> item = ItemStack.builder().build(DataFormats.NBT.readFrom(new ByteArrayInputStream(Base64.getDecoder().decode(tuple.getSecond()))));
				if (item.isPresent()) {
					total += item.get().getQuantity();
					Entity newItem = tuple.getFirst().getExtent().createEntity(EntityTypes.ITEM, tuple.getFirst().getPosition());
					newItem.offer(Keys.REPRESENTED_ITEM, item.get().createSnapshot());
					tuple.getFirst().getExtent().spawnEntity(newItem, Cause.source(EntitySpawnCause.builder()
							.entity(newItem).type(SpawnTypes.PLUGIN).build()).build());
				}
			} catch (InvalidDataException | InvalidDataFormatException | IOException e) {
				e.printStackTrace();
			}
		}
		return total;
	}
	
	public static void cleanup() {
//		for (UUID chunk : mem.keySet()) {
//			for (Vector3i item : mem.get(chunk).keySet()) {
//				mem.get(chunk).get(item).clear();
//			}
//			mem.get(chunk).clear();
//		}
		mem.clear();
	}
}
