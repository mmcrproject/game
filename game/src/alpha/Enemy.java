package alpha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;

public class Enemy {

	private String mook0 = "defau";
	private String mook1 = "{.m.}"; //TODO size is 25x12 adjust bullet
	private String mook2 = "}.w.{";
	private String mook3 = "\\..^../";
	private String mookDamage = "X.x.X";
	private String mook;
	private int mookType;
	
	private boolean destroyed = false;
	private int posx;
	private int posy;
	Random g = new Random();
	private int movedX, movedY;
	private int health;
	private int numSteps = 60;
	private int direction = 0;
	private int delta;

	public int getX() {
		return posx;
	}

	public int getY() {
		return posy;
	}
	
	public int getMaxSteps(){
		return numSteps;
	}
	
	public int getDirection(){
		return direction;
	}

	public boolean gotHit(Bullet b) {
		int[] array;
		array = b.getLoc();
		int bX = array[0];
		int bY = array[1];
		if (bX >= posx && bX <= posx + 25) {
			if (bY >= posy && bY <= posy + 12) {
				return true;
			}
		}
		return false;
	}

	public Enemy(int initPosx, int initPosy, int mookTypeIn, int speed, int gameLevel) {
		posx = initPosx;
		posy = initPosy;
		delta = speed;
		direction = 1;
		health = ((gameLevel/10)+mookTypeIn);
		mookType = mookTypeIn;
		
		switch (mookType) {
			case 1: mook = mook1;
				break;
			case 2: mook = mook2;
				break;
			case 3: mook = mook3;
				break;
			default: mook = mook0;
				break;
		}
	} // end of badGuy()

	public void move() {
		if ((movedX+delta) > numSteps || movedX < 0) {
			if(direction > 0){
				posy = posy+10;
				posx -= delta;
				movedX -= delta;
			} else {
				posy = posy-10;
				posx += delta;
				movedX += delta;
			}
			direction = 0-direction;
		} else if(direction > 0){
			movedX += delta;
			posx += delta;
		} else if(direction < 0){
			movedX -= delta;
			posx -= delta;
		}
	} // end of move()

	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.setFont(new Font("Sans Serif", 1, 12));
		g.drawString(mook, posx, posy);
	} // end of draw()

	public void damage() {
		health--;
		if(health == 1) {
			mook = mookDamage;
		}
	}

	public int getHealth() {
		return health;
	}
	
	public boolean isDestroyed(){
		return destroyed;
	}

	public int[] getSteps() {
		int[] array = new int[2];
		array[0] = movedX;
		array[1] = movedY;
		return array;
	}

	public int getType() {
		return mookType;
	}
}