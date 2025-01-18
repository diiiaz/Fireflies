package io.github.diiiaz.fireflies.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModel;

@Environment(EnvType.CLIENT)
public class FireflyEntityModel extends EntityModel<FireflyEntityRenderState> {

	private final ModelPart body_bone;

	public FireflyEntityModel(ModelPart root) {
		super(root, RenderLayer::getEntityCutout);
		this.body_bone = root.getChild("body_bone");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("body_bone", ModelPartBuilder.create().uv(0, -2).cuboid(0.0F, -1.5F, -1.0F, 0.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 23.5F, 0.0F));
		return TexturedModelData.of(modelData, 4, 1);
	}
}