package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;

import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.utility.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.LevelAccessor;

public class SBBContraptionManager extends ContraptionRenderingWorld<ContraptionRenderInfo> {
	public static final SuperByteBufferCache.Compartment<Pair<Contraption, RenderType>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();

	public SBBContraptionManager(LevelAccessor world) {
		super(world);
	}

	@Override
	public void renderLayer(RenderLayerEvent event) {
		super.renderLayer(event);
		RenderType type = event.getType();
		VertexConsumer consumer = event.buffers.bufferSource()
				.getBuffer(type);
		visible.forEach(info -> renderContraptionLayerSBB(info, type, consumer));

		event.buffers.bufferSource().endBatch(type);
	}

	@Override
	public boolean invalidate(Contraption contraption) {
		for (RenderType chunkBufferLayer : RenderType.chunkBufferLayers()) {
			SuperByteBufferCache.getInstance().invalidate(CONTRAPTION, Pair.of(contraption, chunkBufferLayer));
		}
		return super.invalidate(contraption);
	}

	@Override
	protected ContraptionRenderInfo create(Contraption c) {
		VirtualRenderWorld renderWorld = ContraptionRenderDispatcher.setupRenderWorld(world, c);
		return new ContraptionRenderInfo(c, renderWorld);
	}

	private void renderContraptionLayerSBB(ContraptionRenderInfo renderInfo, RenderType layer, VertexConsumer consumer) {
		if (!renderInfo.isVisible()) return;

		SuperByteBuffer superBuffer = SuperByteBufferCache.getInstance().get(CONTRAPTION, Pair.of(renderInfo.contraption, layer), () -> ContraptionRenderDispatcher.buildStructureBuffer(renderInfo.renderWorld, renderInfo.contraption, layer));

		if (superBuffer.isEmpty())
				return;

		ContraptionMatrices matrices = renderInfo.getMatrices();

		superBuffer.transform(matrices.getModel())
				.light(matrices.getWorld())
				.hybridLight()
				.renderInto(matrices.getViewProjection(), consumer);
	}
}
