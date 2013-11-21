package alpha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;

public class Boss {

	private String boss0 = " _,_";
	private String boss1 = "/ \\ _ / \\";  
	private String boss2 = "{} ^ {}";
	private String damage = "X.x.X";
	
	private boolean destroyed = false;
	private boolean doChange = false;
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
	
	public void change(){
		if (doChange) {
			delta = 0-delta;
			doChange = false;
		}
	
	}

	public Boss(int initPosx, int initPosy, int speed, int life) {
		posx = initPosx;
		posy = initPosy;
		delta = speed;
		direction = 1;
		health = life;
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
		} else {
			System.out.println("unmoving");
		}
	} // end of move()

	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.setFont(new Font("Sans Serif", 1, 12));
		g.drawString(boss0, posx+5, posy-10);
		g.drawString(boss1, posx, posy);
		g.drawString(boss2, posx, posy+10);
	} // end of draw()

	public void damage() {
		health--;
		boss2 = damage;
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
}