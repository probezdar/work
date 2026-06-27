package com.etherforge.mod.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMechGolem extends ModelEtherGolem {

    // Рёбра — дочерние к телу
    private ModelRenderer ribLeft;
    private ModelRenderer ribRight;

    // Кулаки — дочерние к рукам
    private ModelRenderer fistLeft;
    private ModelRenderer fistRight;

    public ModelMechGolem() {
        super();

        // ── Рёбра жёсткости ─────────────────────
        // Дочерние к body — координаты относительно
        // rotationPoint тела (0, 4, 0)
        ribLeft = new ModelRenderer(this, 48, 20);
        ribLeft.setRotationPoint(0.0f, 0.0f, 0.0f);
        // Правый борт тела (X=4) + рёбро шириной 1
        ribLeft.addBox(4.0f, 1.0f, -1.5f, 1, 6, 3);
        body.addChild(ribLeft);

        ribRight = new ModelRenderer(this, 48, 20);
        ribRight.setRotationPoint(0.0f, 0.0f, 0.0f);
        // Левый борт (X=-5)
        ribRight.addBox(-5.0f, 1.0f, -1.5f, 1, 6, 3);
        body.addChild(ribRight);

        // ── Кулаки ──────────────────────────────
        // Дочерние к armLeft/armRight
        // Координаты относительно rotationPoint руки
        fistLeft = new ModelRenderer(this, 48, 30);
        fistLeft.setRotationPoint(0.0f, 0.0f, 0.0f);
        // Конец левой руки: Y=5 (рука 6 высотой, от -1 до 5)
        fistLeft.addBox(-0.5f, 5.0f, -2.0f, 4, 3, 4);
        armLeft.addChild(fistLeft);

        fistRight = new ModelRenderer(this, 48, 30);
        fistRight.setRotationPoint(0.0f, 0.0f, 0.0f);
        // Для правой руки box смещён влево (-3.5f)
        fistRight.addBox(-3.5f, 5.0f, -2.0f, 4, 3, 4);
        armRight.addChild(fistRight);
    }

    @Override
    public void render(Entity entity, float limbSwing,
                       float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch,
                       float scale) {
        // Все child-детали рендерятся автоматически
        // вместе с родителем
        super.render(entity, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, scale);
    }
}