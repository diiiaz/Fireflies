package io.github.diiiaz.fireflies.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;

@Environment(EnvType.CLIENT)
public class FireflyEntityModel extends EntityModel<FireflyEntityRenderState> {

	private final ModelPart body;

	public FireflyEntityModel(ModelPart root) {
        super(root, RenderLayer::getEntityCutout);
        this.body = root.getChild(EntityModelPartNames.BODY);
	}

		public static TexturedModelData getTexturedModelData() {
			ModelData modelData = new ModelData();
			ModelPartData modelPartData = modelData.getRoot();
			ModelPartData main = modelPartData.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(1, 1).cuboid(-0.5F, -0.5F, 0.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, 21.5F, -1.5F, -0.0873F, 0.0F, 0.0F));

			ModelPartData cube_r1 = main.addChild("cube_r1", ModelPartBuilder.create().uv(0, 2).cuboid(0.0F, -0.5F, -0.5F, 0.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.1F, 0.1309F, 0.0F, 0.0F));
			return TexturedModelData.of(modelData, 16, 16);
			}

	public void setAngles(FireflyEntityRenderState entity) {
		body.traverse().forEach(ModelPart::resetTransform);
	}

}




//	private final ModelPart body;
//
//	public FireflyEntityModel(ModelPart root) {
//		super(root, RenderLayer::getEntityCutout);
//		this.body = root.getChild(EntityModelPartNames.BODY);
//	}
//
//	public static TexturedModelData getTexturedModelData() {
//		ModelData modelData = new ModelData();
//		ModelPartData modelPartData = modelData.getRoot();
//		modelPartData.addChild(EntityModelPartNames.BODY,
//				ModelPartBuilder.create()
//						.uv(0, -4)
//						.cuboid(0.0F, -3.0F, -2.0F, 0.0F, 2.0F, 4.0F,
//								new Dilation(0.0F)),
//				ModelTransform.pivot(0.0F, 24.0F, 0.0F));
//		return TexturedModelData.of(modelData, 16, 16);
//	}
//
//	public void setAngles(FireflyEntityRenderState entity) {
//		body.traverse().forEach(ModelPart::resetTransform);
//	}
