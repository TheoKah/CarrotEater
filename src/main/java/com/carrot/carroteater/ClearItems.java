package com.carrot.carroteater;

import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

public class ClearItems implements Runnable {

	@Override
	public void run() {
		MessageChannel.TO_ALL.send(Text.of(TextColors.GREEN, "[CarrotEater] ", TextColors.YELLOW, "Items on the ground and XP orbs will be deleted in ", TextColors.DARK_RED, "ONE MINUTE"));
		
		Sponge.getScheduler()
		.createTaskBuilder()
		.execute(new Runnable() {
			
			@Override
			public void run() {
				MessageChannel.TO_ALL.send(Text.of(TextColors.GREEN, "[CarrotEater] ", TextColors.YELLOW, "Items on the ground and XP orbs will be deleted in ", TextColors.DARK_RED, "30 SECONDS"));
			}
		})
		.delay(30, TimeUnit.SECONDS)
		.async()
		.submit(CarrotEater.getInstance());
		
		Sponge.getScheduler()
		.createTaskBuilder()
		.execute(new Runnable() {
			
			@Override
			public void run() {
				int count = EntityEater.eatItems() + EntityEater.eatXP();
				MessageChannel.TO_ALL.send(Text.of(TextColors.GREEN, "[CarrotEater] ", TextColors.YELLOW, "Items on the ground and XP orbs were deleted: ", TextColors.LIGHT_PURPLE, count, TextColors.YELLOW, " entities"));
				
			}
		})
		.delay(1, TimeUnit.MINUTES)
		.submit(CarrotEater.getInstance());
	}

}
