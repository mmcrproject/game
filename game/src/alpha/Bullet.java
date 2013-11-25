package alpha;

import java.awt.Color;
import java.awt.Graphics;

public class Bullet {

	private int posx, posy, adjust;
	private String bullet = "^"; //TODO size is 5x5 adjust shooting
	private int speed;
	private int distanceMoved = 0;

	public Bullet(int x, int y, int magnitude) {
		speed = magnitude;
		posx = x;
		posy = y;
	}

	synchronized public void add(Bullet b) {
		this.posx = b.posx;
		this.posy = b.posy;
	}
	
	synchronized public void draw(Graphics g) {
		g.setColor(new Color(100, 75, 200));
		if (posx <= 700) {
			g.drawString(bullet, posx, posy);
		}

	} // end of draw()

	public void move(int direction) {
		posy -= speed;
		distanceMoved += speed;
	}
	
	
	public boolean canShoot(Bullet b, int dist, boolean autoFire, boolean shootReset) {
		if (b.distanceMoved > dist) {
			if(autoFire || shootReset){
				distanceMoved=0;
				return true;
			}
		}
		return false;
	}

	public int[] getLoc() {
		int[] array = new int[2];
		array[0] = posx;
		array[1] = posy;
		return array;
	}

} // end of Bullet class
