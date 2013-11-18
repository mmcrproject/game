package alpha;

import java.awt.Color;
import java.awt.Graphics;

public class Bullet {

	private int posx, posy;
	private String bullet = "^";
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
			g.drawString(bullet, posx, posy+speed);
		}

	} // end of draw()

	public void move(int direction) {
		posy -= speed;
		distanceMoved += speed;
	}
	
	
	public int canShoot(Bullet b, int dist) {
		if (b.distanceMoved > dist) {
			distanceMoved=0;
			return 1;
		} else {
			return 0;
		}
	}

	public int[] getLoc() {
		int[] array = new int[2];
		array[0] = posx;
		array[1] = posy;
		return array;
	}

} // end of Bubble class
