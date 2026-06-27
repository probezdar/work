// client/render/RenderMechGolem.java
package com.etherforge.mod.client.render;

import com.etherforge.mod.client.model.ModelMechGolem;
import com.etherforge.mod.entities.EntityMechGolem;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderMechGolem extends RenderLiving<EntityMechGolem> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/entity/mech_golem.png");

    public RenderMechGolem(RenderManager manager) {
        super(manager, new ModelMechGolem(), 0.4f);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMechGolem entity) {
        return TEXTURE;
    }
}