package com.etherforge.mod.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelEtherealGolem extends ModelEtherGolem {

    // Кристаллы — дочерние к телу
    private ModelRenderer crystalLeft;
    private ModelRenderer crystalRight;
    private ModelRenderer crystalCenter;

    // Ореол — дочерний к телу
    private ModelRenderer haloFront;
    private ModelRenderer haloBack;

    // Для анимации вращения кристаллов
    // храним угол отдельно
    private float crystalAngle = 0f;

    public ModelEtherealGolem() {
        super();

        // ── Кристаллы вместо ног ────────────────
        // Дочерние к body, координаты относительно
        // rotationPoint тела (0, 4, 0)
        // Тело: Y 0..8, кристаллы начинаются от Y=8

        crystalLeft = new ModelRenderer(this, 32, 36);
        crystalLeft.setRotationPoint(0.0f, 0.0f, 0.0f);
        crystalLeft.addBox(1.0f, 8.0f, -1.0f, 2, 5, 2);
        body.addChild(crystalLeft);

        crystalRight = new ModelRenderer(this, 40, 36);
        crystalRight.setRotationPoint(0.0f, 0.0f, 0.0f);
        crystalRight.addBox(-3.0f, 8.0f, -1.0f, 2, 5, 2);
        body.addChild(crystalRight);

        crystalCenter = new ModelRenderer(this, 48, 36);
        crystalCenter.setRotationPoint(0.0f, 0.0f, 0.0f);
        crystalCenter.addBox(-1.0f, 9.0f, -0.5f, 2, 6, 1);
        body.addChild(crystalCenter);

        // ── Ореол ───────────────────────────────
        // Тонкие пластины вокруг тела
        haloFront = new ModelRenderer(this, 0, 50);
        haloFront.setRotationPoint(0.0f, 0.0f, 0.0f);
        // Впереди тела: Z=-3 (тело Z: -2..2)
        haloFront.addBox(-5.0f, 1.0f, -3.0f, 10, 6, 1);
        body.addChild(haloFront);

        haloBack = new ModelRenderer(this, 0, 50);
        haloBack.setRotationPoint(0.0f, 0.0f, 0.0f);
        // Сзади: Z=2
        haloBack.addBox(-5.0f, 1.0f, 2.0f, 10, 6, 1);
        body.addChild(haloBack);

        // Убираем обычные ноги — скрываем их
        // (они всё ещё в базовом классе, просто
        //  не рендерим в override)
    }

    @Override
    public void setRotationAngles(float limbSwing,
                                  float limbSwingAmount,
                                  float ageInTicks,
                                  float netHeadYaw,
                                  float headPitch,
                                  float scaleFactor,
                                  Entity entity) {
        // Голова следит за взглядом
        head.rotateAngleY = netHeadYaw * 0.017453292f;
        head.rotateAngleX = headPitch  * 0.017453292f;

        // Руки плавно покачиваются (парение)
        armLeft.rotateAngleX = MathHelper.sin(
                ageInTicks * 0.07f) * 0.25f;
        armRight.rotateAngleX = MathHelper.sin(
                ageInTicks * 0.07f + (float) Math.PI) * 0.25f;

        // Тело медленно покачивается
        body.rotateAngleX = MathHelper.sin(
                ageInTicks * 0.04f) * 0.06f;
        body.rotateAngleY = MathHelper.sin(
                ageInTicks * 0.03f) * 0.04f;

        // Кристаллы вращаются вокруг Y
        // (child к телу — RotateAngle суммируется)
        crystalAngle = ageInTicks * 0.05f;
        crystalLeft.rotateAngleY   =  crystalAngle;
        crystalRight.rotateAngleY  = -crystalAngle;
        crystalCenter.rotateAngleY =  crystalAngle * 0.7f;

        // Ореол слегка следит за телом
        // (уже child — дополнительно не нужно)
    }

    @Override
    public void render(Entity entity, float limbSwing,
                       float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch,
                       float scale) {

        setRotationAngles(limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, scale, entity);

        head.render(scale);
        body.render(scale);   // кристаллы и ореол — child, рендерятся сами
        armLeft.render(scale);
        armRight.render(scale);
        // legLeft и legRight НЕ рендерим — эфирный голем без ног
    }
}