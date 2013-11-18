package alpha;

import java.awt.Color;
import java.awt.Graphics;

public class Alpha {

	private String ship;
	private Boolean upgraded = false;
	private int shield; //current shield
	private int shieldMax; //max shield
	private final int shieldReset;
	private final int shieldMaxReset;

	private int width = 15, height = 12; //hitbox size
	private int posX, posY; //ship position
	
	private int leftMax;
	private int rightMax;
	
	private Boolean isDestroyed;
	
	public Alpha(int initialX, int initialY, int maxLeft, int maxRight, int shieldSet, int shieldMaxSet) {
		setX(initialX);
		setY(initialY);
		leftMax = maxLeft;
		rightMax = maxRight;
		shield = shieldSet;
		shieldReset = shieldSet;
		shieldMax = shieldMaxSet;
		shieldMaxReset = shieldMaxSet;
		reset();
		isDestroyed = false;
	}
	
	public int getX() {
		return posX;
	}

	public int getY() {
		return posY;
	}
	
	private void setX(int x) {
		posX = x;
	}
	
	private void setY(int y) {
		posY = y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void draw(Graphics g, int deltaVector, Boolean moving) {
		g.setColor(Color.black);
		
		if(moving){
			posX += deltaVector; //only adjust position if ship is moving
			
			if(posX > rightMax){ //if ship would go beyond bounds, hold at edge
				posX = rightMax;
			} else if(posX < leftMax){
				posX = leftMax;
			}
		}
		
		if (upgraded) {
			ship = "\\^/";
		} else {
			ship = "/^\\";
		}
		
	g.drawString(ship, posX-(width/2), posY); //Adjust X to account for ship's width
	g.drawRect(posX-(width/2)+5, posY-height+3, width, height); //test hitbox

	} // end of draw()

	public void addShield() {
		if (shield < shieldMax) {
			shield++;
		}
	}
	
	public void addShieldMax() {
		shieldMax++;
	}
	
	public float shieldPercent() {
		return shield/shieldMax;
	}
	
	public void resetShieldMax(int x) {
		shieldMax=x;
		shield=x;
	}

	public void damage() {
		if(shield > 0) {
			shield--;
		}
		if(shield == 0) {
			ship = "xXx";
			isDestroyed = true;
		}
	}

	public int getShield() {
		return shield;
	}
	
	public int getShieldMax() {
		return shieldMax;
	}
	
	public void upgrade() {
		ship = "\\^/";
	}

	public void reset() {
		ship = "/^\\";
		shield = shieldReset;
		shieldMax = shieldMaxReset;
	}
} // end of Kirby class