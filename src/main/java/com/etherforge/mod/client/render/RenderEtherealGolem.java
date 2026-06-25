package com.etherforge.mod.client.render;

import com.etherforge.mod.entities.EntityEtherealGolem;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderEtherealGolem extends RenderBiped<EntityEtherealGolem> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/entity/ethereal_golem.png");

    public RenderEtherealGolem(RenderManager manager) {
        super(manager, new ModelBiped(0.0f, 0.0f, 64, 64), 0.4f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEtherealGolem entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(EntityEtherealGolem entity,
                         double x, double y, double z,
                         float entityYaw, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.75f);

        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
}