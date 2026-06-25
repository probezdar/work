// client/render/RenderEtherealGolem.java
package com.etherforge.mod.client.render;

import com.etherforge.mod.entities.EntityEtherealGolem;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderEtherealGolem extends RenderBiped<EntityEtherealGolem> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/entity/ethereal_golem.png");

    // Эфирный голем полупрозрачный
    public RenderEtherealGolem(RenderManager manager) {
        super(manager, new ModelBiped(), 0.5f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEtherealGolem entity) {
        return TEXTURE;
    }

    // Полупрозрачный рендер
    @Override
    public void doRender(EntityEtherealGolem entity,
                         double x, double y, double z,
                         float entityYaw, float partialTicks) {
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.blendFunc(
                net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA,
                net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        net.minecraft.client.renderer.GlStateManager.color(
                1.0f, 1.0f, 1.0f, 0.75f); // 75% непрозрачность
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        net.minecraft.client.renderer.GlStateManager.disableBlend();
        net.minecraft.client.renderer.GlStateManager.color(
                1.0f, 1.0f, 1.0f, 1.0f);
    }
}