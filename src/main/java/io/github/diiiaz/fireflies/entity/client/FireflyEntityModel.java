package io.github.diiiaz.fireflies.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.TintableCompositeModel;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class FireflyEntityModel<T extends Entity> extends TintableCompositeModel<T> {

	private final ModelPart root;

    public FireflyEntityModel(ModelPart root) {
		this.root = root;
		root.getChild("body_bone");
    }

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("body_bone", ModelPartBuilder.create().uv(0, -2).cuboid(0.0F, -1.5F, -1.0F, 0.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 23.5F, 0.0F));
		return TexturedModelData.of(modelData, 4, 1);
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}
}