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

        // ══════════════════════════════════════
        //  ГОЛОВА
        //  rotationPoint = верх тела (Y=4 от
        //  начала координат модели)
        //  Box: -3..3, -6..0, -3..3  (6x6x6)
        // ══════════════════════════════════════
        head = new ModelRenderer(this, 0, 0);
        head.setRotationPoint(0.0f, 4.0f, 0.0f);
        head.addBox(-3.0f, -6.0f, -3.0f, 6, 6, 6);

        // Кристалл сверху — дочерний к голове
        headDetail = new ModelRenderer(this, 24, 0);
        headDetail.setRotationPoint(0.0f, 0.0f, 0.0f);
        headDetail.addBox(-1.0f, -8.0f, -1.0f, 2, 2, 2);
        // Добавляем как child — следует за головой автоматически
        head.addChild(headDetail);

        // ══════════════════════════════════════
        //  ТЕЛО
        //  rotationPoint = (0, 4, 0)
        //  Box: -4..4, 0..8, -2..2  (8x8x4)
        // ══════════════════════════════════════
        body = new ModelRenderer(this, 16, 16);
        body.setRotationPoint(0.0f, 4.0f, 0.0f);
        body.addBox(-4.0f, 0.0f, -2.0f, 8, 8, 4);

        // Панель на груди — дочерняя к телу
        bodyPanel = new ModelRenderer(this, 40, 16);
        bodyPanel.setRotationPoint(0.0f, 0.0f, 0.0f);
        // Чуть выступает вперёд (-2.5f по Z вместо -2.0f)
        bodyPanel.addBox(-2.0f, 1.5f, -2.5f, 4, 3, 1);
        body.addChild(bodyPanel);

        // ══════════════════════════════════════
        //  РУКИ
        //
        //  КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ:
        //  rotationPoint должна быть на плече —
        //  т.е. вплотную к телу по X
        //
        //  Тело занимает X: -4..4
        //  Левая рука начинается от X=4 (правый
        //  борт тела если смотреть спереди —
        //  это левая рука голема)
        //
        //  rotationPoint.X = +5  (левая)
        //  rotationPoint.X = -5  (правая)
        //  rotationPoint.Y = 5   (уровень плеча)
        //
        //  Box рисуется ОТНОСИТЕЛЬНО rotationPoint:
        //  от 0 до +3 по X (рука идёт наружу)
        // ══════════════════════════════════════

        // Левая рука (с точки зрения голема — его левая)
        armLeft = new ModelRenderer(this, 0, 16);
        armLeft.setRotationPoint(4.0f, 5.0f, 0.0f);
        // Box: 0..3, -1..5, -1.5..1.5
        armLeft.addBox(0.0f, -1.0f, -1.5f, 3, 6, 3);

        // Наплечник — дочерний к armLeft
        // Позиция относительно точки вращения руки
        armLeftPad = new ModelRenderer(this, 0, 28);
        armLeftPad.setRotationPoint(0.0f, 0.0f, 0.0f);
        armLeftPad.addBox(-0.5f, -2.5f, -2.0f, 4, 2, 4);
        armLeft.addChild(armLeftPad);

        // Правая рука
        armRight = new ModelRenderer(this, 12, 16);
        armRight.setRotationPoint(-4.0f, 5.0f, 0.0f);
        // Box: -3..0, -1..5, -1.5..1.5
        armRight.addBox(-3.0f, -1.0f, -1.5f, 3, 6, 3);

        // Наплечник правый — дочерний к armRight
        armRightPad = new ModelRenderer(this, 8, 28);
        armRightPad.setRotationPoint(0.0f, 0.0f, 0.0f);
        armRightPad.addBox(-3.5f, -2.5f, -2.0f, 4, 2, 4);
        armRight.addChild(armRightPad);

        // ══════════════════════════════════════
        //  НОГИ
        //  rotationPoint = (±2, 12, 0)
        //  Y=12 = конец тела (4 + 8)
        //  Box: -2..2, 0..5, -2..2  (4x5x4)
        // ══════════════════════════════════════
        legLeft = new ModelRenderer(this, 0, 36);
        legLeft.setRotationPoint(2.0f, 12.0f, 0.0f);
        legLeft.addBox(-2.0f, 0.0f, -2.0f, 4, 5, 4);

        legRight = new ModelRenderer(this, 16, 36);
        legRight.setRotationPoint(-2.0f, 12.0f, 0.0f);
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