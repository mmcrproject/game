package alpha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;

public class Enemy {

	private String mook0 = "defau";
	private String mook1 = "{.m.}";
	private String mook2 = "}.w.{";
	private String mook3 = "\\.^./";
	private String mookDamage = "X.x.X";
	private String mook;
	
	private boolean doChange = false;
	private int posx = 60, posy = 200;
	Random g = new Random();
	private boolean doOnce = true;
	private int high = 1, low = -1;
	private int x = 0, y = 0;
	private int movedX, movedY;
	private int health = 1;
	private int numSteps = 100;
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
		int ballX = array[0];
		int ballY = array[1];
		if (ballX >= posx && ballX <= posx + 30) {
			if (ballY >= posy && ballY <= posy + 14) {
				return true;
			}
		}
		return false;
	}
	
	public void change(){
		if (doChange) {
			delta = 0-delta;
			doChange = false;
		}
	
	}

	public Enemy(int initPosx, int initPosy, int mookType, int speed) {
		posx = initPosx;
		posy = initPosy;
		delta = speed;
		
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

	public int move() {

		if (x == 0 && y == 0) {
			doOnce = true;
		}
		if (doOnce) {
			x = g.nextInt(high - low + 1) + low;
			y = g.nextInt(high - low + 1) + low;
			doOnce = false;
		}
		movedX += x;
		movedY += y;
		if (movedX > numSteps || movedY > numSteps || movedX < -numSteps
				|| movedY < -numSteps) {
			x = g.nextInt(high - low + 1) + low;
			y = g.nextInt(high - low + 1) + low;
			movedX = 0;
			movedY = 0;
		}
		posx += x;
		if (posx > 665) {
			posx = 10;
		} else if (posx < 10) {
			posx = 664;
		}

		posy += y;
		if (posy > 235) {
			posy = 20;
		} else if (posy < 20) {
			posy = 234;
		}

		if (x>0){
			direction=1;
		}else{
			direction=-1;
		}
		return 0;
	} // end of move()

	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.setFont(new Font("Sans Serif", 3, 14));
		g.drawString(mook, posx, posy);

	} // end of draw()

	public void damage() {
		health--;
//		System.out.println(health);
		mook = mookDamage;
	}

	public int getHealth() {
		return health;
	}

	public int[] getSteps() {
		int[] array = new int[2];
		array[0] = movedX;
		array[1] = movedY;
		return array;
	}
}