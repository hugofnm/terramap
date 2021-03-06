package fr.thesmyler.smylibgui.widgets;

import javax.annotation.Nullable;

import fr.thesmyler.smylibgui.Animation;
import fr.thesmyler.smylibgui.Animation.AnimationState;
import fr.thesmyler.smylibgui.screen.Screen;
import net.minecraft.client.gui.GuiScreen;

public class SlidingPanelWidget extends Screen {

	protected int showX, hiddenX, showY, hiddenY;
	protected int bgColor = 0xA0000000;
	protected Animation mainAnimation;
	protected boolean closeOnClickOther = false;
	protected boolean visible = true;

	public SlidingPanelWidget(int showX, int hiddenX, int showY, int hiddenY, int z, int width, int height, long delay) {
		super(hiddenX, hiddenY, z, width, height, BackgroundType.NONE);
		this.showX = showX;
		this.showY = showY;
		this.hiddenX = hiddenX;
		this.hiddenY = hiddenY;
		this.mainAnimation = new Animation(delay);
	}

	public SlidingPanelWidget(int z, long delay) {
		this(0, 0, 0, 0, z, 50, 50, delay);
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, boolean hovered, boolean focused, @Nullable Screen parent){
		GuiScreen.drawRect(x, y, x + this.width, y + this.height, this.bgColor);
		super.draw(x, y, mouseX, mouseY, hovered, focused, parent);
		this.mainAnimation.update();
	}
	
	@Override
	public void onUpdate(Screen parent) {
		this.mainAnimation.update();
		super.onUpdate(parent);
	}

	public PanelTarget getTarget() {
		switch(this.mainAnimation.getState()) {
		case LEAVE:
			return PanelTarget.CLOSED;
		case ENTER:
			return PanelTarget.OPENED;
		default:
			return this.mainAnimation.getProgress() < 0.5 ? PanelTarget.CLOSED: PanelTarget.OPENED;
		}
	}

	@Override
	public boolean onParentClick(int mouseX, int mouseY, int mouseButton, @Nullable Screen parent) {
		if(this.closeOnClickOther && !this.getTarget().equals(PanelTarget.CLOSED)) {
			this.close();
			return false;
		}
		return true;
	}

	public void open() {
		this.mainAnimation.start(AnimationState.ENTER);
	}

	public void close() {
		this.mainAnimation.start(AnimationState.LEAVE);
	}

	public SlidingPanelWidget setStateNoAnimation(boolean opened) {
		this.mainAnimation.start(opened? AnimationState.LEAVE: AnimationState.ENTER);
		this.mainAnimation.stop();
		return this;
	}

	public int getOpenX() {
		return this.showX;
	}

	public SlidingPanelWidget setOpenX(int x) {
		this.showX = x;
		return this;
	}

	public int getClosedX() {
		return this.hiddenX;
	}

	public SlidingPanelWidget setClosedX(int x) {
		this.hiddenX = x;
		return this;
	}

	public int getOpenY() {
		return this.showY;
	}

	public SlidingPanelWidget setOpenY(int y) {
		this.showY = y;
		return this;
	}

	public int getClosedY() {
		return this.hiddenY;
	}

	public SlidingPanelWidget setClosedY(int y) {
		this.hiddenY = y;
		return this;
	}

	public SlidingPanelWidget setWidth(int width) {
		this.width = width;
		return this;
	}

	public SlidingPanelWidget setHeight(int height) {
		this.height = height;
		return this;
	}

	public boolean closesOnClickOther() {
		return this.closeOnClickOther;
	}

	public SlidingPanelWidget setCloseOnClickOther(boolean yesNo) {
		this.closeOnClickOther = yesNo;
		return this;
	}

	@Override
	public int getX() {
		return this.mainAnimation.blend(this.showX, this.hiddenX);
	}

	@Override
	public int getY() {
		return this.mainAnimation.blend(this.showY, this.hiddenY);
	}

	public int getBackroundColor() {
		return this.bgColor;
	}

	public SlidingPanelWidget setBackgroundColor(int color) {
		this.bgColor = color;
		return this;
	}
	
	@Override
	public boolean isVisible(Screen parent) {
		return this.visible;
	}
	
	public SlidingPanelWidget setVisibility(boolean yesNo) {
		this.visible = yesNo;
		return this;
	}
	
	public SlidingPanelWidget show() {
		return this.setVisibility(true);
	}
	
	public SlidingPanelWidget hide() {
		return this.setVisibility(false);
	}

	public enum PanelTarget {
		OPENED, CLOSED;
	}

}
