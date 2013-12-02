package alpha;

import java.awt.Color;
import java.awt.Graphics;

public class BadBullets {

	private int posx, posy, direction, delta, leftMax, rightMax;
	private String bullet1 = "|";
	private String bullet2 = "!";
	private String bullet9 = ";";
	private String bullet;
	
	public BadBullets(int x, int y, int direct, int speed, int type, int leftMx, int rightMx) {
		posx = x;
		posy = y;
		leftMax = leftMx;
		rightMax = rightMx;
		direction = direct;
		delta = speed;
		switch(type) {
			case 1: bullet = bullet1;
				break;
			case 2: bullet = bullet2;
				break;
			case 9: bullet = bullet9;
				break;
			default: bullet = bullet9;
				break;
		}
	}

	synchronized public void add(BadBullets b) {
		this.posx = b.posx;
		this.posy = b.posy;
	}

	synchronized public void draw(Graphics g) {
		g.setColor(new Color(200, 0, 200));
		if (posx <= 700) {
			g.drawString(bullet, posx, posy);
		}

	} // end of draw()

	public void move() {
		if (direction > 0 || direction < 0) {
			posx += (direction*delta);
			if(posx <= leftMax || posx >= rightMax){
				direction = 0-direction;
				if(posx <= leftMax){
					posx = leftMax;
				}
				if(posx >= rightMax){
					posx = rightMax;
				}
			}
		}
		posy += delta;
	}

	public int[] getLoc() {
		int[] array = new int[2];
		array[0] = posx;
		array[1] = posy;
		return array;
	}

} // end of Bubble class