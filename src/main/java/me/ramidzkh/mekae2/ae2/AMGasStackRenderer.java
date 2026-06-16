package me.ramidzkh.mekae2.ae2;

import java.util.List;

import ae2.api.client.AEKeyRenderHandler;
import ae2.api.client.AEKeyRendering;
import ae2.util.Platform;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class AMGasStackRenderer implements AEKeyRenderHandler<MekanismKey> {

    private static boolean initialized;

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        AEKeyRendering.register(MekanismKeyType.TYPE, MekanismKey.class, new AMGasStackRenderer());
        initialized = true;
    }

    @Override
    public void drawInGui(Minecraft minecraft, int x, int y, MekanismKey what) {
        GasStack stack = what.toStack(1);
        TextureAtlasSprite sprite = getSprite(what.getGas());
        RenderState renderState = RenderState.capture();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            drawTexturedQuad(x, y, 16, 16, sprite, stack);
        } finally {
            renderState.restore();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void drawOnBlockFace(MekanismKey what, float scale, int combinedLight, World level) {
        GasStack stack = what.toStack(1);
        TextureAtlasSprite sprite = getSprite(what.getGas());
        float x0 = -scale / 2.0f;
        float y0 = -scale / 2.0f;
        float x1 = scale / 2.0f;
        float y1 = scale / 2.0f;
        RenderState renderState = RenderState.capture();

        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            MekanismRenderer.color(stack);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(x0, y0, 0.0001f).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
            buffer.pos(x0, y1, 0.0001f).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
            buffer.pos(x1, y1, 0.0001f).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
            buffer.pos(x1, y0, 0.0001f).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
            tessellator.draw();
        } finally {
            renderState.restore();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public ITextComponent getDisplayName(MekanismKey stack) {
        return stack.getDisplayName();
    }

    @Override
    public List<ITextComponent> getTooltip(MekanismKey stack) {
        List<ITextComponent> tooltip = new ObjectArrayList<>(2);
        tooltip.add(getDisplayName(stack));
        tooltip.add(new TextComponentString(Platform.getModName(stack.getModId())));
        return tooltip;
    }

    private static TextureAtlasSprite getSprite(Gas gas) {
        return getSpriteOrMissing(gas, Minecraft.getMinecraft().getTextureMapBlocks());
    }

    static TextureAtlasSprite getSpriteOrMissing(Gas gas, TextureMap textureMap) {
        var icon = gas.getIcon();
        if (icon == null) {
            return textureMap.getMissingSprite();
        }
        return textureMap.getAtlasSprite(icon.toString());
    }

    static String getSpriteName(Gas gas) {
        var icon = gas.getIcon();
        return icon == null ? TextureMap.LOCATION_MISSING_TEXTURE.toString() : icon.toString();
    }

    @SuppressWarnings("SameParameterValue")
    private static void drawTexturedQuad(int x, int y, int width, int height, TextureAtlasSprite sprite,
                                         GasStack stack) {
        MekanismRenderer.color(stack);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        buffer.pos(x + width, y + height, 0).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        buffer.pos(x + width, y, 0).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        buffer.pos(x, y, 0).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        tessellator.draw();
    }

    private record RenderState(boolean blendEnabled, boolean alphaEnabled, boolean lightingEnabled,
                               boolean cullEnabled) {
        private static RenderState capture() {
            return new RenderState(
                GL11.glIsEnabled(GL11.GL_BLEND),
                GL11.glIsEnabled(GL11.GL_ALPHA_TEST),
                GL11.glIsEnabled(GL11.GL_LIGHTING),
                GL11.glIsEnabled(GL11.GL_CULL_FACE));
        }

        private void restore() {
            setBlend(blendEnabled);
            setAlpha(alphaEnabled);
            setLighting(lightingEnabled);
            setCull(cullEnabled);
        }

        private static void setBlend(boolean enabled) {
            if (enabled) {
                GlStateManager.enableBlend();
            } else {
                GlStateManager.disableBlend();
            }
        }

        private static void setAlpha(boolean enabled) {
            if (enabled) {
                GlStateManager.enableAlpha();
            } else {
                GlStateManager.disableAlpha();
            }
        }

        private static void setLighting(boolean enabled) {
            if (enabled) {
                GlStateManager.enableLighting();
            } else {
                GlStateManager.disableLighting();
            }
        }

        private static void setCull(boolean enabled) {
            if (enabled) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
        }
    }
}
