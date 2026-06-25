package com.etherforge.mod.client.render;

import com.etherforge.mod.entities.EntityMechGolem;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderMechGolem extends RenderBiped<EntityMechGolem> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/entity/mech_golem.png");

    public RenderMechGolem(RenderManager manager) {
        super(manager, new ModelBiped(0.0f, 0.0f, 64, 64), 0.4f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMechGolem entity) {
        return TEXTURE;
    }
}