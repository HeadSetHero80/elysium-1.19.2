package com.williambl.elysium.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.williambl.elysium.Elysium;
import ladysnake.satin.api.event.EntitiesPreRenderCallback;
import ladysnake.satin.api.event.PostWorldRenderCallbackV2;
import ladysnake.satin.api.managed.ManagedFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.util.RenderLayerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

public final class GlowEffectManager implements EntitiesPreRenderCallback, PostWorldRenderCallbackV2 {
    public static final GlowEffectManager INSTANCE = new GlowEffectManager();
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ManagedShaderEffect auraPostShader = ShaderEffectManager.getInstance().manage(Elysium.id("shaders/post/glow.json"));
    private final ManagedFramebuffer auraFramebuffer = this.auraPostShader.getTarget("glows");
    private boolean auraBufferCleared;

    public void init() {
        EntitiesPreRenderCallback.EVENT.register(this);
        PostWorldRenderCallbackV2.EVENT.register(this);
    }

    public void beforeEntitiesRender(@NotNull Camera camera, @NotNull Frustum frustum, float tickDelta) {
        this.auraBufferCleared = false;
    }

    public void onWorldRendered(@NotNull MatrixStack matrices, @NotNull Camera camera, float tickDelta, long nanoTime) {
        if (this.auraBufferCleared) {
            this.auraPostShader.render(tickDelta);
            this.client.getFramebuffer().beginWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            this.auraFramebuffer.draw();
            RenderSystem.disableBlend();
        }
    }

    public void beginGlowFramebufferUse() {
        Framebuffer auraFramebuffer = this.auraFramebuffer.getFramebuffer();
        if (auraFramebuffer != null) {
            auraFramebuffer.beginWrite(false);
            if (!this.auraBufferCleared) {
                // Define the clear color (RGBA values)
                float[] clearColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f}; // Transparent black
                RenderSystem.clearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
                RenderSystem.clear(16384, MinecraftClient.IS_SYSTEM_MAC); // Clear the color buffer
                this.auraFramebuffer.copyDepthFrom(this.client.getFramebuffer());
                auraFramebuffer.beginWrite(false);
                this.auraBufferCleared = true;
            }
        }
    }

    private void endGlowFramebufferUse() {
        this.client.getFramebuffer().beginWrite(false);
    }

    public static RenderLayer getRenderType(RenderLayer base) {
        return GlowEffectManager.GlowRenderTypes.getRenderType(base);
    }

    private static final class GlowRenderTypes extends RenderLayer {
        private static final RenderPhase.Target GLOW_TARGET = new RenderPhase.Target("elysium:glow_target", GlowEffectManager.INSTANCE::beginGlowFramebufferUse, GlowEffectManager.INSTANCE::endGlowFramebufferUse);

        private GlowRenderTypes(String string, VertexFormat vertexFormat, VertexFormat.DrawMode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
            super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
        }

        public static RenderLayer getRenderType(RenderLayer base) {
            return RenderLayerHelper.copy(base, "elysium:glow", (builder) -> builder.target(GLOW_TARGET));
        }
    }
}