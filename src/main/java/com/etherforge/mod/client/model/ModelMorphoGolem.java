package com.etherforge.mod.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelMorphoGolem extends ModelEtherGolem {

    private ModelRenderer spikeLeft;
    private ModelRenderer spikeRight;
    private ModelRenderer tail;

    public ModelMorphoGolem() {
        super();

        // ── Выросты на голове — дочерние к head ─
        spikeLeft = new ModelRenderer(this, 48, 0);
        spikeLeft.setRotationPoint(0.0f, 0.0f, 0.0f);
        // Относительно rotationPoint головы (0,4,0)
        // Левый вырост: от правого края головы (X=3)
        spikeLeft.addBox(3.0f, -5.5f, -0.5f, 3, 2, 1);
        head.addChild(spikeLeft);

        spikeRight = new ModelRenderer(this, 48, 4);
        spikeRight.setRotationPoint(0.0f, 0.0f, 0.0f);
        spikeRight.addBox(-6.0f, -5.5f, -0.5f, 3, 2, 1);
        head.addChild(spikeRight);

        // ── Хвост — дочерний к телу ─────────────
        tail = new ModelRenderer(this, 48, 8);
        tail.setRotationPoint(0.0f, 0.0f, 0.0f);
        // Сзади тела: Z=2 (тело Z: -2..2)
        tail.addBox(-1.5f, 3.0f, 2.0f, 3, 4, 3);
        body.addChild(tail);
    }

    @Override
    public void setRotationAngles(float limbSwing,
                                  float limbSwingAmount,
                                  float ageInTicks,
                                  float netHeadYaw,
                                  float headPitch,
                                  float scaleFactor,
                                  Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch,
                scaleFactor, entity);

        // Хвост качается независимо от ходьбы
        // tail — child тела, поэтому rotateAngle
        // суммируется с углом тела
        tail.rotateAngleX = MathHelper.sin(
                ageInTicks * 0.1f) * 0.2f;
    }

    @Override
    public void render(Entity entity, float limbSwing,
                       float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch,
                       float scale) {
        super.render(entity, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, scale);
    }
}