package net.firstlight.planut;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.tuple.Pair;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

public class AnimatedPlayerEntity implements IAnimatable {
	private static final HashMap<String, Integer> CONTROLLER_METADATA = new HashMap<>();
	private static final HashMap<Predicate<Pair<EntityPlayer, AnimationController<AnimatedPlayerEntity>>>, AnimationBuilder> ANIMATIONS = new HashMap<>();

	private final UUID uuid;
	private final AnimationFactory factory = new AnimationFactory(this);

	public AnimatedPlayerEntity(UUID uuid) {
		this.uuid = uuid;
	}
	public static void addController(String name, int transitionLengthTicks) {
		CONTROLLER_METADATA.put(name, transitionLengthTicks);
	}

	public static void addAnimation(Predicate<Pair<EntityPlayer, AnimationController<AnimatedPlayerEntity>>> predicate, String... animations) {
		AnimationBuilder builder = new AnimationBuilder();
		for (String animation : animations) {
			builder.addAnimation(animation);
		}
		ANIMATIONS.put(predicate, builder);
	}

	@Override
	public void registerControllers(AnimationData data) {
		CONTROLLER_METADATA.forEach((name, transitionLength) -> data.addAnimationController(new AnimationController<>(this, name, transitionLength, this::predicate)));
	}

	@Override
	public AnimationFactory getFactory() {
		return factory;
	}

	@SuppressWarnings("unchecked")
	public PlayState predicate(AnimationEvent<AnimatedPlayerEntity> event) {
		EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(this.uuid);
		if (player != null) {
			ANIMATIONS.forEach((predicate, animation) -> {
				if (predicate.test(Pair.of(player, event.getController()))) {
					event.getController().setAnimation(animation);
				}
			});
		}
		return PlayState.CONTINUE;
	}
}