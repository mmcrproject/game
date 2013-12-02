package alpha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;

public class MiniBoss {

	private String miniBoss0 = " _,_";
	private String miniBoss1 = "{d b}";
	private String miniBoss2 = "\\/^\\/";
	private String damage = "X.x.X";
	
	private boolean destroyed = false;
	private boolean doChange = false;
	private int posx;
	private int posy;
	Random g = new Random();
	private int movedX, movedY;
	private int health;
	private int direction = 0;
	private int delta;
	private int leftMax, rightMax;

	public int getX() {
		return posx;
	}

	public int getY() {
		return posy;
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
	
	public void change(){
		if (doChange) {
			delta = 0-delta;
			doChange = false;
		}
	
	}

	public MiniBoss(int initPosx, int initPosy, int speed, int life, int lMax, int rMax) {
		posx = initPosx;
		posy = initPosy;
		delta = speed;
		direction = 1;
		health = life;
		leftMax = lMax;
		rightMax = rMax;
	} // end of badGuy()

	public void move() {
		if(direction > 0){
			if(posx+delta > rightMax){
				posx = rightMax;
				posy = posy-10;
				direction = 0-direction;
			} else {
				posx += delta;
			}
		} else if(direction < 0) {
			if(posx-delta < leftMax){
				posx = leftMax;
				posy = posy+10;
				direction = 0-direction;
			} else {
				posx -= delta;
			}
		}
	} // end of move()

	public void draw(Graphics g) {
		if(health == 1){
			miniBoss1 = damage;
		}
		g.setColor(Color.black);
		g.setFont(new Font("Sans Serif", 1, 12));
		g.drawString(miniBoss0, posx+1, posy-10);
		g.drawString(miniBoss1, posx, posy);
		g.drawString(miniBoss2, posx+2, posy+10);
	} // end of draw()

	public void damage() {
		health--;
	} //end of damage()

	public int getHealth() {
		return health;
	} //end of getHealth()
	
	public boolean isDestroyed(){
		return destroyed;
	} //end of isDestroyed()

	public int[] getSteps() {
		int[] array = new int[2];
		array[0] = movedX;
		array[1] = movedY;
		return array;
	} //end of getSteps()
}