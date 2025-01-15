package io.github.diiiaz.fireflies.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModel;

@Environment(EnvType.CLIENT)
public class FireflyModel extends EntityModel<FireflyEntityRenderState> {

	private final ModelPart bb_main;

	public FireflyModel(ModelPart root) {
		super(root, RenderLayer::getEntityCutout);
        this.bb_main = root.getChild("bb_main");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, -4).cuboid(0.0F, -3.0F, -2.0F, 0.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 16, 16);
	}

	public void setAngles(FireflyEntityRenderState entity) {
		bb_main.traverse().forEach(ModelPart::resetTransform);
	}

}