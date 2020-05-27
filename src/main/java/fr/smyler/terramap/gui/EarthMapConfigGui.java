/**
 * This is stolen from terra121, lightly modified
 * 
 * @author orangeadam3, shejan0
 */

package fr.smyler.terramap.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import fr.smyler.terramap.TerramapServer;
import io.github.terra121.EarthGeneratorSettings;
import io.github.terra121.TerraMod;
import io.github.terra121.control.DynamicOptions;
import io.github.terra121.control.DynamicOptions.Element;
import io.github.terra121.projection.GeographicProjection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class EarthMapConfigGui extends GuiScreen implements DynamicOptions.Handler {

	ResourceLocation map = null;
	ResourceLocation rightmap = null;
	BufferedImage base;
	GeographicProjection projection;
	DynamicOptions settings;
	private DynamicOptions.Element[] settingElems;
	private GuiButton done, cancel;

	private int mapsize;

	private EarthGeneratorSettings cfg;

	GuiTiledMap parent;

	public EarthMapConfigGui(GuiTiledMap parent, Minecraft mc) {

		EarthGeneratorSettings parentCfg = TerramapServer.getServer().getGeneratorSettings();
		if(parentCfg == null) cfg = new EarthGeneratorSettings("");
		else cfg = new EarthGeneratorSettings(parentCfg.toString());

		this.mc = mc;
		this.parent = parent;

		@SuppressWarnings("resource")
		InputStream is = getClass().getClassLoader().getResourceAsStream("assets/terra121/data/map.png");
		try {
			base = ImageIO.read(is);
		} catch (IOException e) {
			base = new BufferedImage(512,256,0);
		} finally {
			IOUtils.closeQuietly(is);
		}

		String[] projs = (String[])GeographicProjection.projections.keySet().toArray(new String[GeographicProjection.projections.size()]);

		settingElems = new DynamicOptions.Element[] {
				cycleButton(6969, "projection", projs, e -> {projectMap(true); return I18n.format("terra121.gui.projection")+": "+I18n.format("terra121.projection."+e);}),
				cycleButton(6968, "orentation", GeographicProjection.Orientation.values(), e -> {projectMap(true); return I18n.format("terra121.gui.orientation")+": "+I18n.format("terra121.orientation."+e.toString());}),
		};
		projectMap(false);
	}

	private <E> DynamicOptions.CycleButtonElement<E> cycleButton(int id, String field, E[] list, Function<E, String> tostring) {
		try {
			return new DynamicOptions.CycleButtonElement<E>(id, list, EarthGeneratorSettings.JsonSettings.class.getField(field), cfg.settings, tostring);
		} catch (NoSuchFieldException | SecurityException e) {
			TerraMod.LOGGER.error("This should never happen, but find field reflection error");
			e.printStackTrace();
		}
		return null;
	}

	private void projectMap(boolean change) {

		projection = cfg.getNormalizedProjection();

		cfg.settings.scaleX = cfg.settings.scaleY = projection.metersPerUnit();

		if(map!=null)
			mc.renderEngine.deleteTexture(map);
		else if(rightmap!=null)
			mc.renderEngine.deleteTexture(rightmap);

		BufferedImage img = new BufferedImage(1024,1024,BufferedImage.TYPE_INT_ARGB);

		//scale should be able to fit whole earth inside texture
		double[] bounds = projection.bounds();
		double scale = Math.max(Math.abs(bounds[2]-bounds[0]), Math.abs(bounds[3]-bounds[1]));

		int w = img.getWidth();
		int h = img.getHeight();

		for(int x=0;x<w;x++) {
			for(int y=0;y<h;y++) {
				//image coords to projection coords
				double X = (x/(double)w)*scale+bounds[0];
				double Y = (y/(double)h)*scale+bounds[1];

				//not out of bounds
				if(bounds[0]<=X&&X<=bounds[2]&&bounds[1]<=Y&&Y<=bounds[3]) {

					double proj[] = projection.toGeo(X, Y); //projection coords to lon lat

					//lat lon to reference image coords
					int lon = (int)((proj[0]/360 + 0.5)*base.getWidth());
					int lat = (int)((0.5 + proj[1]/180)*base.getHeight());

					//get pixel from reference image if possible
					if(lon>=0 && lat>=0 && lat < base.getHeight() && lon < base.getWidth()) {
						img.setRGB(x, y, base.getRGB(lon, base.getHeight()-lat-1));
					}
				}
			}
		}

		map = this.mc.renderEngine.getDynamicTextureLocation("mapdemo", new DynamicTexture(img));
	}

	@Override
	public void initGui() {
		mapsize = height-64;
		if(width-mapsize<200)
			mapsize = width-200;
		if(mapsize<32)
			mapsize = 0;

		settings = new DynamicOptions(mc, width-mapsize, height-32, 32, height-32, 32, this, settingElems);
		done = new GuiButton(69, width-106, height-26, 100, 20, "Done");
		cancel = new GuiButton(69, 6, height-26, 100, 20, "Cancel");
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawBackground(0xff);

		settings.drawScreen(mouseX, mouseY, partialTicks);

		//render map texture
		this.mc.renderEngine.bindTexture(map);
		drawScaledCustomSizeModalRect(width-mapsize, (height-mapsize)/2, 0, 0, 1024, 1024, mapsize, mapsize, 1024, 1024);

		this.mc.renderEngine.bindTexture(Gui.OPTIONS_BACKGROUND);
		drawScaledCustomSizeModalRect(0, height-32, 0, 0, width, 32, width, 32, 32, 32); //footer
		drawScaledCustomSizeModalRect(0, 0, 0, 0, width, 32, width, 32, 32, 32); //header

		done.drawButton(mc, mouseX, mouseY, partialTicks);
		cancel.drawButton(mc, mouseX, mouseY, partialTicks);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseEvent) {
		if(done.mousePressed(mc, mouseX, mouseY)) {
			TerramapServer.getServer().setGeneratorSettings(this.cfg);
			TerramapServer.getServer().saveSettings();
			this.mc.displayGuiScreen(this.parent); ///exit
			return;

		} else if(cancel.mousePressed(mc, mouseX, mouseY)) {
			this.mc.displayGuiScreen(this.parent); //exit without saving
			return;
		}

		settings.mouseClicked(mouseX, mouseY, mouseEvent);
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		settings.handleMouseInput();
	}

	@Override
	public void onDynOptClick(Element elem) {
	}
}