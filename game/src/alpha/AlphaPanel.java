package alpha;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;



//import kirby.BadBubbles;
//import kirby.BadGuy;
//import kirby.Bubble;
import alpha.Alpha;

@SuppressWarnings("serial")
public class AlphaPanel extends JPanel implements Runnable, KeyListener {

	private static final int PWIDTH = 400; // size of default panel
	private static final int PHEIGHT = 400;
	private static final int leftMax = 50; // usable are inside panel, left-right
	private static final int rightMax = PWIDTH - 50;
	private static final int startX = PWIDTH / 2; 
	private static final int startY = PHEIGHT - 30;
	
	private ArrayList<Bullet> bullets;
	private ArrayList<Enemy> mooks;
	
	private static final int NO_DELAYS_PER_YIELD = 16;
	/*
	 * Number of frames with a delay of 0 ms before the animation thread yields
	 * to other running threads.
	 */

	private static int MAX_FRAME_SKIPS = 5; // was 2;
	// no. of frames that can be skipped in any one animation loop
	// i.e the games state is updated but not rendered

	private long gameStartTime;
	boolean pauseOnce = true;

	private long framesSkipped = 0L;
	private Thread animator; // the thread that performs the animation
	private boolean running = false; // used to stop the animation thread
	private boolean isPaused = true;

	private long period; // period between drawing in _nanosecs_

	// used at game termination
	private boolean gameOver = false;

	// off screen rendering
	private Graphics dbg;
	private Image dbImage = null;

		// key code savers
	private boolean keyS = false; //Left
	private boolean keyD = false; //Right
	private boolean keyK = false; //Kills
	
	private long startTime;
	private long secondTime;
	private int seconds;
	private int delta = 4; //magnitude of ship speed, begins at one unit
	private int deltaVector; //magnitude delta and +/- sign
	
	private Alpha alpha; //character model
	
	private int score; //total points
	private int cores; //money for upgrades
	
	//Allows rules for when keys pressed together
	/*private Boolean movingLeft = false;
	private Boolean movingRight = false;*/
	private Boolean moving = false;
	
	//Variables regarding shooting
	private Boolean shooting = false;
	private final int bulletSpeed = 5;
	private boolean destroyed = false;

	public AlphaPanel(long period) {
		this.period = period;
		startTime = System.nanoTime();
		setBackground(Color.white);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
		setFocusable(true);
		requestFocus(); // the JPanel now has focus, so receives key events
		readyForTermination();
		
		alpha = new Alpha(startX, startY, leftMax, rightMax, 3/*shield start*/, 3/*shield max*/);
		bullets = new ArrayList<Bullet>();
		mooks = new ArrayList<Enemy>();
		
		addKeyListener(this);

	} // end of AlphaPanel()

	private void readyForTermination() {
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if ((keyCode == KeyEvent.VK_ESCAPE)) {
					running = false;
				}
			}
		});
	} // end of readyForTermination()

	public void addNotify()
	// wait for the JPanel to be added to the JFrame before starting
	{
		super.addNotify(); // creates the peer
		startGame(); // start the thread
	}

	private void startGame()
	// initialize and start the thread
	{
		if (animator == null || !running) {
			animator = new Thread(this);
			animator.start();
		}
	} // end of startGame()

	// ------------- game life cycle methods ------------
	// called by the JFrame's window listener methods

	public void resumeGame()
	// called when the JFrame is activated / deiconified
	{
		isPaused = false;
	}

	public void pauseGame()
	// called when the JFrame is deactivated / iconified
	{
		isPaused = true;
	}

	public void stopGame()
	// called when the JFrame is closing
	{
		running = false;
	}

	// ----------------------------------------------

	public void run()
	/* The frames of the animation are drawn inside the while loop. */
	{
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;

		gameStartTime = System.nanoTime();
		beforeTime = gameStartTime;

		running = true;

		while (running) {

			checkStartnew();
			updateAlpha();
			gameUpdate();
			gameRender();
			paintScreen();

			timer();
			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			sleepTime = (period - timeDiff) - overSleepTime;

			if (sleepTime > 0) { // some time left in this cycle
				try {
					Thread.sleep(sleepTime / 1000000L); // nano -> ms
				} catch (InterruptedException ex) {
				}
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
			} else { // sleepTime <= 0; the frame took longer than the period
				excess -= sleepTime; // store excess time value
				overSleepTime = 0L;

				if (++noDelays >= NO_DELAYS_PER_YIELD) {
					Thread.yield(); // give another thread a chance to run
					noDelays = 0;
				}
			}

			beforeTime = System.nanoTime();

			/*
			 * If frame animation is taking too long, update the game state
			 * without rendering it, to get the updates/sec nearer to the
			 * required FPS.
			 */
			int skips = 0;
			while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
				excess -= period;
				gameUpdate(); // update state but don't render
				skips++;
			}
			framesSkipped += skips;
		}

		System.exit(0); // so window disappears
	} // end of run()

	private void restartGame() {
		alpha.resetShieldMax(3);
		bullets.removeAll(bullets);
		delta = 1;
		moving = false;
		score = 0;
		cores = 0;
		destroyed = false;
		gameOver = false;
		pauseOnce = true;
		checkStartnew();
	}

	private void gameUpdate() {
		if (!isPaused && !gameOver) {
				this.updateBullets();
		return; //TODO update more stuff
		}

	} // end of gameUpdate()

	private void gameRender() {
		if (dbImage == null) {
			dbImage = createImage(PWIDTH, PHEIGHT);
			if (dbImage == null) {
				System.out.println("dbImage is null");
				return;
			} else
				dbg = dbImage.getGraphics();
		}

		// clear the background
		dbg.setColor(new Color(250, 253, 255));
		dbg.fillRect(0, 0, PWIDTH, PHEIGHT);

		dbg.setFont(new Font("Sans Serif", 3, 14));

		drawBackground(dbg);
		
		drawEntities(dbg);
		drawShieldBar(dbg);
		drawShip(dbg);
		drawBullets(dbg);
		dbg.setFont(new Font("Sans Serif", 1, 12));
		dbg.setColor(Color.black);
		dbg.drawString("Score: " + score, 10, 12); //TODO display score
		dbg.drawString("Cores: " + cores, PWIDTH-80, 12); //TODO display money
		dbg.drawString("(P)ause or (Esc)ape", PWIDTH-90, PHEIGHT-7);

		if (isPaused) {
			pausedMessage(dbg);
		}

		if (gameOver) {
			gameOverMessage(dbg);
		}
	} // end of gameRender()

	private void drawShip(Graphics g) {
		alpha.draw(g, deltaVector, moving);
		
	}
	
	private void drawBullets(Graphics g) {
		if(shooting){
			g.drawString("PEW", PWIDTH/2, PHEIGHT/2);
		}
	}

	private void paintScreen()
	// use active rendering to put the buffered image on-screen
	{
		Graphics g;
		try {
			g = this.getGraphics();
			if ((g != null) && (dbImage != null))
				g.drawImage(dbImage, 0, 0, null);
			g.dispose();
		} catch (Exception e) {
			System.out.println("Graphics context error: " + e);
		}
	} // end of paintScreen()

	private void drawShieldBar(Graphics g) {
		int barFill = (int) (alpha.shieldPercent()*100);
		g.setColor(Color.blue);
		g.fillRect(85, PHEIGHT-12, barFill, 10);
		g.setColor(Color.black);
		g.drawRect(85, PHEIGHT-12, 100, 10);
		g.setFont(new Font("verdana", 1, 11));
		g.drawString("Shield "+alpha.getShield()+"/"+alpha.getShieldMax(), 10, PHEIGHT-3);
	}

	private void drawBackground(Graphics g) {
		g.setColor(Color.black);
		g.drawRect(20, 20, PWIDTH-40, PHEIGHT-40);
	}

	private void drawEntities(Graphics g) {
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).draw(g);
		} // draw bullets
	}

	private void pausedMessage(Graphics g) {
		g.setFont(new Font("verdana", 4, 14));
		g.setColor(Color.white);
		g.fillRect(0, 0, PWIDTH, PHEIGHT);
		g.setColor(Color.black);
		g.drawString("PROJECT: A.L.P.H.A.",
				PWIDTH / 2 - 75, PHEIGHT / 2 - 120);
		g.drawString("The year is 30K. You are the rebellion.",
				PWIDTH / 2 - 150+5, PHEIGHT / 2 - 60);
		g.drawString("Pick up cores to purchase upgrades.",
				PWIDTH / 2 - 150+10, PHEIGHT / 2 - 30);
		g.drawString("Move with S<>D and shoot with K.",
				PWIDTH / 2 - 150+12, PHEIGHT / 2);
		g.drawString("Press \'o\' to play and \'p\' to pause.",
				PWIDTH / 2 - 150+15, PHEIGHT / 2 + 50);
	}

	private void gameOverMessage(Graphics g)
	// center the game-over message in the panel
	{
		String msg1 = "Game Over.";
		String msg2 = "Score: " + score;
		String msg3 = "Cores: " + cores;
		int total = Integer.valueOf(score)+Integer.valueOf(cores);
		String msg4 = "Total: " + total;
		String msg5 = "Rank: ";
		
		g.setColor(Color.white);
		g.fillRect(0, 0, PWIDTH, PHEIGHT);
		g.setFont(new Font("Sans Serif", 1, 16));
		g.setColor(Color.black);
		g.drawString(msg1, PWIDTH / 2 - 70+5, PHEIGHT / 2-60);
		g.drawString(msg2, PWIDTH / 2 - 70, PHEIGHT / 2-30);
		g.drawString(msg3, PWIDTH / 2 - 70, PHEIGHT / 2);
		g.drawString(msg4, PWIDTH / 2 - 70+8, PHEIGHT / 2+30);
		g.drawString(msg5, PWIDTH / 2 - 70, PHEIGHT / 2+60);
		if(score == 0){
			g.setColor(Color.red);
			 msg5 = "Flunkie"; //TODO if score in range rank = flavor text
		} else if(score > 0 && score < 100) {
			g.setColor(Color.blue);
			msg5 = "Ensign"; //TODO if score in range rank = flavor text
		}
		g.drawString(msg5, PWIDTH / 2 - 70+50, PHEIGHT / 2+60);
		
		g.setColor(Color.black);
		msg1 = "Press Enter to play again.";
		g.drawString(msg1, PWIDTH / 2 - 100, PHEIGHT / 2 + 90);
		msg1 = "Or ESC to exit.";
		g.drawString(msg1, PWIDTH / 2 - 100 + 30, PHEIGHT / 2 + 120);
	} // end of gameOverMessage()

	@Override
	public void keyPressed(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_D
				|| e.getKeyCode() == KeyEvent.VK_RIGHT) {
			keyD = true;
			if(!moving){
				deltaVector = delta;
			}
			moving = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_S
				|| e.getKeyCode() == KeyEvent.VK_LEFT) {
			keyS = true;
			if(!moving){
				deltaVector = 0-delta;
			}
			moving = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_K || e.getKeyCode() == KeyEvent.VK_UP) {
			keyK = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_P) {
			pauseGame();
		}
		if (e.getKeyCode() == KeyEvent.VK_O) {
			if (isPaused) {
				resumeGame();
				pauseOnce = false;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (gameOver) {
				restartGame();
			}
		}
		
		if (e.getKeyCode() == KeyEvent.VK_B) {
			mooks.add(new Enemy(100, 200, 1, 2));
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_D
				|| e.getKeyCode() == KeyEvent.VK_RIGHT) {
			keyD = false;
			if(!keyS){
				moving = false;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_S
				|| e.getKeyCode() == KeyEvent.VK_LEFT) {
			keyS = false;
			if(!keyD){
				moving = false;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_K || e.getKeyCode() == KeyEvent.VK_UP) {
			keyK = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	public void powerUp(Graphics g) {
		g.setColor(Color.RED);
		g.setFont(new Font("verdana", 2, 20));
	}

	private void updateAlpha() {
		boolean hit = false;

		/* Some logic is necessary in case multiple keys are pressed together,
		 * logic is keep moving until all keys released. 
		 * */
		if (keyD && !keyS) {
			deltaVector = delta;
		}
		if (keyS && !keyD) {
			deltaVector = 0-delta;
		}
		
		if (keyK) {
			shooting = true;
			if (!bullets.isEmpty()) {
				if (bullets.get(bullets.size() - 1).canShoot(
						bullets.get(bullets.size() - 1), 100) > 0) {
					System.out.println("case 1");
					bullets.add(new Bullet(alpha.getX(), alpha.getY(),
						bulletSpeed));
				} else if (bullets.get(bullets.size() - 1).canShoot(
					bullets.get(bullets.size() - 1), 100) > 0) {
						System.out.println("case 2");
						bullets.add(new Bullet(alpha.getX(), alpha.getY(), bulletSpeed));
				} 
			} else {
				System.out.println("bullet");
				bullets.add(new Bullet(alpha.getX(), alpha.getY(), bulletSpeed));
			}
		} else { //not key k
			shooting = false;
		} //endif
		
		if (alpha.getShield() < 1) {
			
			if(destroyed){
				System.out.println("Destroyed");
			}
		}
	
	}// end updateMoves
	
	private void updateBullets() {
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).move(bulletSpeed);
			int[] array = bullets.get(i).getLoc();
			if (array[0] > 700 || array[0] < 0) {
				bullets.remove(i);
			}
		}// end for
	}

	private void updateBaddies() {
		boolean hit;

		//Move mooks
		for (int i = 0; i < mooks.size(); i++) {
			mooks.get(i).move();
		
			//Check if bullet hit mook
			for (int j = 0; j < mooks.size(); j++) {

				hit = mooks.get(i).gotHit(bullets.get(j));
				if (hit) {
					mooks.get(i).damage();
					bullets.remove(j);
				}
			}
		}// end for
		
		for (int i = 0; i < mooks.size(); i++) {
			if (mooks.get(i).getHealth() <= 0) {
				mooks.remove(i);
				score++;
			}
		}// end for

	}// end baddies

	private void checkStartnew() {

		if (pauseOnce) {
			pauseGame();
		}
	}
	
	private void timer(){
		secondTime = System.nanoTime();
		long oneSecond = 1000000000;
		if(secondTime-startTime>=(oneSecond)){
			seconds++;
			startTime = secondTime;
		}
		if(seconds%1==0 && startTime == secondTime){
			System.out.println(seconds + " seconds.");
		}
	}

} // end of AlphaPanel class