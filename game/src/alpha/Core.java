package alpha;

import java.awt.Color;
import java.awt.Graphics;

public class Core {
	
	//Cores always drop in the same manner, so direction and speed can be set w/o input
	private int posx, posy;
	private int delta = 1;
	private String core = "¤";
	private int worth;
	
	public Core(int x, int y, int type) {
		posx = x;
		posy = y;
		worth = type;
	}

	synchronized public void add(Core b) {
		this.posx = b.posx;
		this.posy = b.posy;
	}

	synchronized public void draw(Graphics g) {
		g.setColor(Color.green);
		if (posx <= 700) {
			g.drawString(core, posx, posy);
		}

	} // end of draw()

	public void move() {
		posy += delta;
	}

	public int[] getLoc() {
		int[] array = new int[2];
		array[0] = posx;
		array[1] = posy;
		return array;
	}
	
	public int getValue() {
		return worth;
	}

} // end of Core class