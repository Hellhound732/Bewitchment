package com.bewitchment.common.core.event;

import net.minecraftforge.common.MinecraftForge;

/**
 * This class was created by Arekkuusu on 14/06/2017.
 * It's distributed as part of Bewitchment under
 * the MIT license.
 */
public final class ModEvents {

	private ModEvents() {
	}

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new EnergyEvents());
		MinecraftForge.EVENT_BUS.register(new HarvestEvent());
		MinecraftForge.EVENT_BUS.register(new DivinationEvents());
		MinecraftForge.EVENT_BUS.register(new TransformationEvents());
		MinecraftForge.EVENT_BUS.register(new VampireAbilityHandler());
		MinecraftForge.EVENT_BUS.register(new WerewolfAbilityHandler());
	}


}
