package alpha;

import java.awt.Color;
import java.awt.Graphics;

public class Bullet {

	private int posx, posy;
	private String bullet;
	private String bulletD = "^";
	private String bulletB = "*";
	private String bulletL = "|";
	private String bulletN = "0";
	private int speed;
	private int distanceMoved = 0;
	private int direction;
	private int leftMax = 50;
	private int rightMax = 250;
	
	public Bullet(int x, int y, int magnitude, int direct) {
		speed = magnitude;
		posx = x;
		posy = y;
		direction = direct;
		bullet = bulletD;
	}
	
	public Bullet(int x, int y, int magnitude, int direct, String type) {
		speed = magnitude;
		posx = x;
		posy = y;
		direction = direct;
		if(type == "B"){
			bullet = bulletB;
		} else if(type == "L"){
			bullet = bulletL;
		} else {
			bullet = bulletN;
		}
	}

	synchronized public void add(Bullet b) {
		this.posx = b.posx;
		this.posy = b.posy;
		this.speed = b.speed;
		this.direction = b.direction;
	}
	
	synchronized public void draw(Graphics g) {
		g.setColor(new Color(100, 75, 200));
		if (posx <= 700) {
			g.drawString(bullet, posx, posy);
		}

	} // end of draw()

	public void move() {
		posy -= speed;
		distanceMoved += speed;
		if(direction > 0 || direction < 0){
			posx += (speed*direction);
			if(posx < leftMax){
				posx = leftMax;
				direction = 0 - direction;
			}else if(posx > rightMax){
				posx = rightMax;
				direction = 0 - direction;
			}
		}
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
