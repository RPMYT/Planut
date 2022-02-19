package net.firstlight.planut;

import net.firstlight.planut.utils.MatrixUtils;
import net.firstlight.planut.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.geo.render.built.*;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.MatrixStack;
import software.bernie.shadowed.eliotlash.mclib.utils.Interpolations;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

@SuppressWarnings("rawtypes")
@SideOnly(Side.CLIENT)
public class AnimatedPlayerEntityRenderer<T extends AnimatedPlayerEntity & IAnimatable> implements IGeoRenderer<T> {

	public static MatrixStack MATRIX_STACK = new MatrixStack();

	// ARGB COLORS VARIABLES
	private float r;
	private float g;
	private float b;
	private float a;

	private final Vector3f normal = new Vector3f();
	private final Vector4f vertex = new Vector4f();

	private final AnimatedGeoModel modelProvider;

	public AnimatedPlayerEntityRenderer(AnimatedGeoModel modelProvider) {
		this.modelProvider = modelProvider;
	}

	public void setColor(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnimatedGeoModel<T> getGeoModelProvider() {
		return this.modelProvider;
	}

	@Override
	public ResourceLocation getTextureLocation(T instance) {
		return this.getGeoModelProvider().getTextureLocation(instance);
	}

	public void renderPlayer(GeoModel model, T animatable, RenderPlayerEvent e) {

		if (e.getEntityPlayer().isInvisibleToPlayer(Minecraft.getMinecraft().player))
			return;

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();

		boolean captured = MatrixUtils.captureMatrix();

		// If you don't want the model to rotate the head with the movement of the mouse
		// simply remove this line.
		PlayerUtils.checkHeadRotations(e);

		GlStateManager.scale(1, 1, 1);
		float renderYawOffset = Interpolations.lerp(e.getEntityPlayer().prevRenderYawOffset,
				e.getEntityPlayer().renderYawOffset, e.getPartialRenderTick());

		// If you don't want the model to rotate when the player is using elytra simply
		// remove this line.
		PlayerUtils.isElytraFlying(e.getEntityPlayer().isElytraFlying(), e, renderYawOffset);

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableBlend();

		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();

		getGeoModelProvider().setLivingAnimations(animatable, e.getEntityPlayer().getPersistentID().hashCode());
		setColor(1F, 1F, 1F, 1F);

		GlStateManager.disableCull();
		GlStateManager.enableRescaleNormal();

		BufferBuilder builder = Tessellator.getInstance().getBuffer();

		// Render all top level bones.
		for (GeoBone bone : model.topLevelBones) {
			renderRecursively(animatable, builder, MATRIX_STACK, bone, e);
		}

		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();

		if (captured) {
			MatrixUtils.releaseMatrix();
		}
		GlStateManager.popMatrix();

		// Draw the player name
		e.getRenderer().renderName((AbstractClientPlayer) e.getEntityPlayer(), e.getX(), e.getY(), e.getZ());
	}

	public void renderRecursively(T animatable, BufferBuilder builder, MatrixStack stack, GeoBone bone,
			RenderPlayerEvent e) {

		stack.push();
		renderItemInHand(bone, e, "bipedRightArm", "bipedLeftArm");
		stack.pop();

		stack.push();

		stack.translate(bone);
		stack.moveToPivot(bone);
		stack.rotate(bone);
		stack.scale(bone);
		stack.moveBackFromPivot(bone);

		if (!bone.isHidden) {
			for (GeoCube cube : bone.childCubes) {
				Minecraft.getMinecraft().getTextureManager()
						.bindTexture(getGeoModelProvider().getTextureLocation(animatable));
				renderCube(builder, stack, cube);

			}

			for (GeoBone childBone : bone.childBones) {
				renderRecursively(animatable, builder, stack, childBone, e);
			}
		}

		stack.pop();
	}

	private void renderCube(BufferBuilder builder, MatrixStack stack, GeoCube cube) {

		GlStateManager.pushMatrix();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

		stack.push();
		stack.moveToPivot(cube);
		stack.rotate(cube);
		stack.moveBackFromPivot(cube);

		for (GeoQuad quad : cube.quads) {
			if (quad != null) {
				this.normal.set(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
				stack.getNormalMatrix().transform(this.normal);

				if (this.normal.getX() < 0 && (cube.size.y == 0 || cube.size.z == 0))
					this.normal.x *= -1;
				if (this.normal.getY() < 0 && (cube.size.x == 0 || cube.size.z == 0))
					this.normal.y *= -1;
				if (this.normal.getZ() < 0 && (cube.size.x == 0 || cube.size.y == 0))
					this.normal.z *= -1;

				for (GeoVertex vertex : quad.vertices) {
					this.vertex.set(vertex.position);
					this.vertex.w = 1;
					stack.getModelMatrix().transform(this.vertex);

					builder.pos(this.vertex.getX(), this.vertex.getY(), this.vertex.getZ())
							.tex(vertex.textureU, vertex.textureV).color(this.r, this.g, this.b, this.a)
							.normal(this.normal.getX(), this.normal.getY(), this.normal.getZ()).endVertex();
				}
			}

		}

		Tessellator.getInstance().draw();
		stack.pop();
		GlStateManager.popMatrix();
	}

	public void renderItemInHand(GeoBone itemBone, RenderPlayerEvent e,
								 String mainItemBoneName, String secondaryItemBoneName) {

		ItemStack mainItem = e.getEntityPlayer().getHeldItemMainhand();
		ItemStack secondaryItem = e.getEntityPlayer().getHeldItemOffhand();

		// Right arm
		if (mainItem != ItemStack.EMPTY) {
			if (itemBone.getName().equals(mainItemBoneName)) {
				GlStateManager.pushMatrix();
				MatrixUtils.multiplyMatrix(MATRIX_STACK, itemBone);
				if (mainItem.getItem() instanceof ItemBlock) {
					GlStateManager.scale(0.3, 0.3, 0.3);
					GlStateManager.translate(0.1, -1, -1);
				}
				Minecraft.getMinecraft().getItemRenderer().renderItem(e.getEntityPlayer(), mainItem,
						TransformType.NONE);
				GlStateManager.popMatrix();
			}
		}

		// Left arm
		if (secondaryItem != ItemStack.EMPTY) {
			if (itemBone.getName().equals(secondaryItemBoneName)) {
				GlStateManager.pushMatrix();
				MatrixUtils.multiplyMatrix(MATRIX_STACK, itemBone);
				if (secondaryItem.getItem() instanceof ItemBlock) {
					GlStateManager.scale(0.3, 0.3, 0.3);
					GlStateManager.translate(0.1, -1, -1);
				}
				Minecraft.getMinecraft().getItemRenderer().renderItem(e.getEntityPlayer(), secondaryItem,
						TransformType.NONE);
				GlStateManager.popMatrix();
			}
		}
	}
}