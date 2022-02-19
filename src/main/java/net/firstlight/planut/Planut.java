package net.firstlight.planut;

import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import software.bernie.geckolib3.GeckoLib;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(Side.CLIENT)
@Mod(modid = "planut", clientSideOnly = true, dependencies = "required-after:geckolib3", name = "Planut (Player Animation Utility)", version = "1.0")
public class Planut {
	public static HashMap<UUID, AnimatedPlayerEntityRenderer<AnimatedPlayerEntity>> renderers = new HashMap<>();

	public Planut() {
        GeckoLib.initialize();
	}

	@SubscribeEvent
	public static void renderCustomPlayerModel(RenderPlayerEvent.Pre event) {
		UUID uuid = event.getEntityPlayer().getPersistentID();

		renderers.putIfAbsent(uuid, new AnimatedPlayerEntityRenderer<>(new AnimatedPlayerEntityModel()));

		AnimatedPlayerEntityRenderer<AnimatedPlayerEntity> renderer = renderers.get(uuid);
		AnimatedPlayerEntity player = new AnimatedPlayerEntity(uuid);

		event.setCanceled(true);
		renderer.renderPlayer(renderer.getGeoModelProvider().getModel(renderer.getGeoModelProvider().getModelLocation(player)), player, event);
	}
}
