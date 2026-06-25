package com.etherforge.mod.client.render;

import com.etherforge.mod.entities.EntityMorphoGolem;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderMorphoGolem extends RenderBiped<EntityMorphoGolem> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/entity/morpho_golem.png");

    public RenderMorphoGolem(RenderManager manager) {
        super(manager, new ModelBiped(0.0f, 0.0f, 64, 64), 0.4f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMorphoGolem entity) {
        return TEXTURE;
    }
}