package com.bewitchment.client.gui;


import com.bewitchment.client.ResourceLocations;
import com.bewitchment.client.gui.container.ContainerThreadSpinner;
import com.bewitchment.common.tile.TileEntityThreadSpinner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public class GuiThreadSpinner extends GuiContainer {

	public GuiThreadSpinner(Container inventorySlotsIn, TileEntityThreadSpinner te) {
		super(inventorySlotsIn);
		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawDefaultBackground();
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceLocations.THREAD_SPINNER_GUI);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		ContainerThreadSpinner c = (ContainerThreadSpinner) this.inventorySlots;
		double progress = (double) c.data_a[0] / (TileEntityThreadSpinner.MAX_TICKS - 10);
		drawTexturedModalRect(guiLeft + 85, guiTop + 33, 176, 0, (int) (22 * progress), 17);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		this.renderHoveredToolTip(mouseX - guiLeft, mouseY - guiTop);
	}

}
