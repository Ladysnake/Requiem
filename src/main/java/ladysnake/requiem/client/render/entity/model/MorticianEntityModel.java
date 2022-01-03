/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
// Made with Blockbench 3.9.2
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

package ladysnake.requiem.client.render.entity.model;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.entity.MorticianEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.util.math.MathHelper;

public class MorticianEntityModel<T extends Entity> extends SinglePartEntityModel<T> implements ModelWithHead {
    public static final EntityModelLayer MODEL_LAYER = new EntityModelLayer(Requiem.id("mortician"), "main");
    public static final String SLEEVES_PART_NAME = "sleeves";

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart arms;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public MorticianEntityModel(ModelPart root) {
        super(RenderLayer::getEntityTranslucent);
        this.root = root;
        this.head = root.getChild(EntityModelPartNames.HEAD);
        this.arms = root.getChild(EntityModelPartNames.ARMS);
        this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
        this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
        this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
        this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
                .mirrored()
                .uv(0, 0).cuboid(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, true)
                .uv(31, 1).cuboid(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F)
                .uv(2, 0).cuboid(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F)
                .uv(2, 4).cuboid(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F),
            ModelTransform.NONE);
        root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create()
                .mirrored()
                .uv(16, 16).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                .uv(0, 34).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, new Dilation(0.5F)),
            ModelTransform.NONE);
        root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create()
                .uv(0, 18).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, true),
            ModelTransform.pivot(-2.0F, 12.0F, 0.0F));
        root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create()
                .uv(0, 18).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, false),
            ModelTransform.pivot(2.0F, 12.0F, 0.0F));
        ModelPartData arms = root.addChild(EntityModelPartNames.ARMS, ModelPartBuilder.create()
                .uv(40, 30).cuboid(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, true)
                .uv(34, 38).cuboid(-6.0F, 2.5F, -1.5F, 12.0F, 3.0F, 3.0F, false)
                .uv(44, 18).cuboid(4.0F, -2.0F, -2.0F, 3.0F, 8.0F, 4.0F, true)
                .uv(44, 18).cuboid(-7.0F, -2.0F, -2.0F, 3.0F, 8.0F, 4.0F, false),
            ModelTransform.of(0.0F, 2.0F, 0.0F, -0.9163F, 0.0F, 0.0F));
        arms.addChild(SLEEVES_PART_NAME, ModelPartBuilder.create()
                .uv(42, 0).cuboid(-6.999F, -0.2F, -0.4F, 5.0F, 3.0F, 6.0F, true)
                .uv(42, 0).cuboid(1.999F, -0.2F, -0.4F, 5.0F, 3.0F, 6.0F, false),
            ModelTransform.of(0.0F, 4.0F, 0.0F, -0.6545F, 0.0F, 0.0F));
        root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create()
                .uv(28, 43).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 9.0F, 4.0F, true)
                .uv(42, 43).cuboid(-0.5F, -1.0F, -1.5F, 2.0F, 11.0F, 3.0F, true),
            ModelTransform.pivot(5.0F, 2.0F, 0.0F)
        );
        root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create()
                .uv(28, 43).cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 9.0F, 4.0F, false)
                .uv(42, 43).cuboid(-1.5F, -1.0F, -1.5F, 2.0F, 11.0F, 3.0F, false),
            ModelTransform.pivot(-5.0F, 2.0F, 0.0F)
        );
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        boolean rollingHead = entity instanceof MerchantEntity merchant && merchant.getHeadRollingTimeLeft() > 0;

        this.head.yaw = headYaw * (float) (Math.PI / 180.0);
        this.head.pitch = headPitch * (float) (Math.PI / 180.0);

        if (rollingHead) {
            this.head.roll = 0.3F * MathHelper.sin(0.45F * animationProgress);
            this.head.pitch = 0.4F;
        } else {
            this.head.roll = 0.0F;
        }

        this.rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance * 0.5F;
        this.leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 1.4F * limbDistance * 0.5F;
        this.rightLeg.yaw = 0.0F;
        this.leftLeg.yaw = 0.0F;

        if (entity instanceof MorticianEntity mortician && mortician.isSpellcasting()) {
            this.rightArm.pivotZ = 0.0F;
            this.rightArm.pivotX = -5.0F;
            this.leftArm.pivotZ = 0.0F;
            this.leftArm.pivotX = 5.0F;
            this.rightArm.pitch = MathHelper.cos(animationProgress * 0.6662F) * 0.25F;
            this.leftArm.pitch = MathHelper.cos(animationProgress * 0.6662F) * 0.25F;
            this.rightArm.roll = (float) (Math.PI * 3.0 / 4.0);
            this.leftArm.roll = (float) (-Math.PI * 3.0 / 4.0);
            this.rightArm.yaw = 0.0F;
            this.leftArm.yaw = 0.0F;
            this.arms.visible = false;
            this.rightArm.visible = true;
            this.leftArm.visible = true;
        } else {
            this.arms.visible = true;
            this.rightArm.visible = false;
            this.leftArm.visible = false;
        }
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }
}
