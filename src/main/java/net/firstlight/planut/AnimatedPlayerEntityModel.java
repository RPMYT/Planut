package net.firstlight.planut;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AnimatedPlayerEntityModel extends AnimatedGeoModel<AnimatedPlayerEntity> {

	@Override
	public ResourceLocation getAnimationFileLocation(AnimatedPlayerEntity animatable) {
		return new ResourceLocation("planut", "animation/planut.animation.json");
	}

	@Override
	public ResourceLocation getModelLocation(AnimatedPlayerEntity animatable) {
		return new ResourceLocation("planut", "geo/planut.geo.json");
	}

	@Override
	public ResourceLocation getTextureLocation(AnimatedPlayerEntity animatable) {
		return new ResourceLocation("minecraft", "textures/entity/steve.png");
	}
}
