package com.etherforge.mod.gui;

import com.etherforge.mod.tileentity.TileEntityEtherWorkbench;
import com.etherforge.mod.util.Reference;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiEtherWorkbench extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Reference.MOD_ID, "textures/gui/ether_workbench.png"
    );

    private final TileEntityEtherWorkbench workbench;

    public GuiEtherWorkbench(InventoryPlayer playerInv,
                             TileEntityEtherWorkbench workbench) {
        super(new ContainerEtherWorkbench(playerInv, workbench));
        this.workbench = workbench;
        this.xSize = 176;
        this.ySize = 184;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks,
                                                   int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(TEXTURE);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = "Ether Workbench";
        fontRenderer.drawString(title,
                xSize / 2 - fontRenderer.getStringWidth(title) / 2,
                6, 0x404040);
        fontRenderer.drawString("Inventory", 8, 90, 0x404040);
    }
}