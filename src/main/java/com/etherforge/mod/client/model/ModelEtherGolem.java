package com.etherforge.mod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelEtherGolem extends ModelBase {

    // ═══════════════════════════════════════════
    //  Части модели — protected чтобы подклассы
    //  могли добавлять дочерние детали
    // ═══════════════════════════════════════════
    protected ModelRenderer head;
    protected ModelRenderer headDetail;

    protected ModelRenderer body;
    protected ModelRenderer bodyPanel;

    // Руки — точка вращения на плече
    protected ModelRenderer armLeft;
    protected ModelRenderer armRight;

    // Наплечники — дочерние к руке (child)
    protected ModelRenderer armLeftPad;
    protected ModelRenderer armRightPad;

    protected ModelRenderer legLeft;
    protected ModelRenderer legRight;

    public ModelEtherGolem() {
        textureWidth  = 64;
        textureHeight = 64;

        // СДВИГ всех частей вниз на +7 по Y
        // чтобы низ ног (был Y=17) стал Y=24 = земля

        // ══════════════════════════════════════
        //  ГОЛОВА  (было Y=4 → стало Y=11)
        // ══════════════════════════════════════
        head = new ModelRenderer(this, 0, 0);
        head.setRotationPoint(0.0f, 11.0f, 0.0f);   // было 4.0f
        head.addBox(-3.0f, -6.0f, -3.0f, 6, 6, 6);

        headDetail = new ModelRenderer(this, 24, 0);
        headDetail.setRotationPoint(0.0f, 0.0f, 0.0f);
        headDetail.addBox(-1.0f, -8.0f, -1.0f, 2, 2, 2);
        head.addChild(headDetail);

        // ══════════════════════════════════════
        //  ТЕЛО  (было Y=4 → стало Y=11)
        // ══════════════════════════════════════
        body = new ModelRenderer(this, 16, 16);
        body.setRotationPoint(0.0f, 11.0f, 0.0f);   // было 4.0f
        body.addBox(-4.0f, 0.0f, -2.0f, 8, 8, 4);

        bodyPanel = new ModelRenderer(this, 40, 16);
        bodyPanel.setRotationPoint(0.0f, 0.0f, 0.0f);
        bodyPanel.addBox(-2.0f, 1.5f, -2.5f, 4, 3, 1);
        body.addChild(bodyPanel);

        // ══════════════════════════════════════
        //  РУКИ  (было Y=5 → стало Y=12)
        // ══════════════════════════════════════
        armLeft = new ModelRenderer(this, 0, 16);
        armLeft.setRotationPoint(4.0f, 12.0f, 0.0f);  // было 5.0f
        armLeft.addBox(0.0f, -1.0f, -1.5f, 3, 6, 3);

        armLeftPad = new ModelRenderer(this, 0, 28);
        armLeftPad.setRotationPoint(0.0f, 0.0f, 0.0f);
        armLeftPad.addBox(-0.5f, -2.5f, -2.0f, 4, 2, 4);
        armLeft.addChild(armLeftPad);

        armRight = new ModelRenderer(this, 12, 16);
        armRight.setRotationPoint(-4.0f, 12.0f, 0.0f); // было 5.0f
        armRight.addBox(-3.0f, -1.0f, -1.5f, 3, 6, 3);

        armRightPad = new ModelRenderer(this, 8, 28);
        armRightPad.setRotationPoint(0.0f, 0.0f, 0.0f);
        armRightPad.addBox(-3.5f, -2.5f, -2.0f, 4, 2, 4);
        armRight.addChild(armRightPad);

        // ══════════════════════════════════════
        //  НОГИ  (было Y=12 → стало Y=19)
        //  Низ ног: 19 + 5 = 24 = земля ✓
        // ══════════════════════════════════════
        legLeft = new ModelRenderer(this, 0, 36);
        legLeft.setRotationPoint(2.0f, 19.0f, 0.0f);  // было 12.0f
        legLeft.addBox(-2.0f, 0.0f, -2.0f, 4, 5, 4);

        legRight = new ModelRenderer(this, 16, 36);
        legRight.setRotationPoint(-2.0f, 19.0f, 0.0f); // было 12.0f
        legRight.addBox(-2.0f, 0.0f, -2.0f, 4, 5, 4);
    }

    // ═══════════════════════════════════════════
    //  Рендер
    // ═══════════════════════════════════════════
    @Override
    public void render(Entity entity, float limbSwing,
                       float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch,
                       float scale) {

        setRotationAngles(limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, scale, entity);

        head.render(scale);      // headDetail рендерится автоматически (child)
        body.render(scale);      // bodyPanel рендерится автоматически (child)
        armLeft.render(scale);   // armLeftPad — child
        armRight.render(scale);  // armRightPad — child
        legLeft.render(scale);
        legRight.render(scale);
    }

    // ═══════════════════════════════════════════
    //  Анимация
    // ═══════════════════════════════════════════
    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount,
                                  float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor,
                                  Entity entity) {

        // Голова следит за взглядом
        head.rotateAngleY = netHeadYaw * 0.017453292f;
        head.rotateAngleX = headPitch  * 0.017453292f;

        // Качание рук при ходьбе
        armLeft.rotateAngleX = MathHelper.cos(
                limbSwing * 0.6662f + (float) Math.PI)
                * 1.4f * limbSwingAmount;

        armRight.rotateAngleX = MathHelper.cos(
                limbSwing * 0.6662f)
                * 1.4f * limbSwingAmount;

        // Качание ног при ходьбе
        legLeft.rotateAngleX = MathHelper.cos(
                limbSwing * 0.6662f)
                * 1.2f * limbSwingAmount;

        legRight.rotateAngleX = MathHelper.cos(
                limbSwing * 0.6662f + (float) Math.PI)
                * 1.2f * limbSwingAmount;

        // Лёгкое покачивание тела в покое
        body.rotateAngleY = MathHelper.sin(
                ageInTicks * 0.05f) * 0.02f;
    }
}