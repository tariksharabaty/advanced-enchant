package com.oidioomstudio.advancedenchant.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.oidioomstudio.advancedenchant.client.renderer.blockentity.state.InfuserRenderState;
import com.oidioomstudio.advancedenchant.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class InfuserRenderer implements BlockEntityRenderer<InfuserBlockEntity, InfuserRenderState> {
    public static final SpriteId BOOK_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.apply(
            Identifier.fromNamespaceAndPath("advancedenchant", "enchantment/enchanting_infuser_book")
    );

    private final ItemModelResolver itemModelResolver;
    private final SpriteGetter sprites;
    private final BookModel bookModel;

    public InfuserRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public InfuserRenderState createRenderState() {
        return new InfuserRenderState();
    }

    @Override
    public void extractRenderState(InfuserBlockEntity blockEntity, InfuserRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.flip = Mth.lerp(partialTick, blockEntity.oFlip, blockEntity.flip);
        renderState.open = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
        renderState.time = (float) blockEntity.time + partialTick;
        float rotDiff = blockEntity.rot - blockEntity.oRot;
        while (rotDiff >= (float) Math.PI) {
            rotDiff -= (float) (Math.PI * 2);
        }
        while (rotDiff < -(float) Math.PI) {
            rotDiff += (float) (Math.PI * 2);
        }
        renderState.yRot = blockEntity.oRot + rotDiff * partialTick;

        renderState.item.clear();
        this.itemModelResolver.updateForTopItem(renderState.item, blockEntity.getItem(0), ItemDisplayContext.GROUND, blockEntity.getLevel(), null, 0);
    }

    @Override
    public void submit(InfuserRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        this.submitBook(renderState, poseStack, submitNodeCollector);
        this.submitItem(renderState, poseStack, submitNodeCollector);
    }

    private void submitBook(EnchantTableRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.75f, 0.5f);
        poseStack.translate(0.0f, 0.1f + Mth.sin(state.time * 0.1f) * 0.01f, 0.0f);
        float yRot = state.yRot;
        poseStack.rotate(Axis.YP.rotation(-yRot));
        poseStack.rotate(Axis.ZP.rotationDegrees(80.0f));
        float ff1 = Mth.frac(state.flip + 0.25f) * 1.6f - 0.3f;
        float ff2 = Mth.frac(state.flip + 0.75f) * 1.6f - 0.3f;
        BookModel.State bookState = BookModel.State.forAnimation(state.time, Mth.clamp(ff1, 0.0f, 1.0f), Mth.clamp(ff2, 0.0f, 1.0f), state.open);
        submitNodeCollector.submitModel(this.bookModel, bookState, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, BOOK_TEXTURE, this.sprites, 0);
        if (state.breakProgress != null) {
            submitNodeCollector.order(1).submitCrumblingOverlay(this.bookModel, bookState, poseStack, BOOK_TEXTURE.renderType(this.bookModel.renderType()), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
        }
        poseStack.popPose();
    }

    private void submitItem(InfuserRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        if (renderState.open > 0.0f) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 1.0f, 0.5f);
            float hoverOffset = Mth.sin(renderState.time / 10.0f) * 0.1f + 0.1f;
            AABB aABB = renderState.item.getModelBoundingBox();
            float modelYScale = -((float) aABB.minY) + 0.0625f;
            poseStack.translate(0.0, hoverOffset + modelYScale * renderState.open - 0.15f * (1.0f - renderState.open), 0.0);
            float scale = renderState.open * 0.8f + 0.2f;
            poseStack.scale(scale, scale, scale);
            poseStack.rotate(Axis.YP.rotation(renderState.time / 20.0f));
            renderState.item.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
