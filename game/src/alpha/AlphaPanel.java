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
import javax.swing.JPanel;




import alpha.Alpha;

@SuppressWarnings("serial")
public class AlphaPanel extends JPanel implements Runnable, KeyListener {

	private static final int PWIDTH = 300; // size of default panel
	private static final int PHEIGHT = 400;
	private static final int leftMax = 50; // usable are inside panel, left-right
	private static final int rightMax = PWIDTH - 50;
	private static final int startX = PWIDTH / 2; 
	private static final int startY = PHEIGHT - 30;
	
	private ArrayList<Core> cores;
	private ArrayList<Bullet> bullets;
	private ArrayList<Enemy> mooks;
	private ArrayList<MiniBoss> miniBoss;
	private ArrayList<Boss> boss;
	private ArrayList<BadBullets> badBullets;
	private int level = 0;
	
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

	//private long framesSkipped = 0L; //does this need added back?
	private Thread animator; // the thread that performs the animation
	private boolean running = false; // used to stop the animation thread
	private boolean isPaused = true;
	private boolean shootReset = true;
	private boolean autoFire = false;

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
	private int delta = 4; //magnitude of ship speed
	private int deltaVector; //magnitude delta and +/- sign
	
	private Alpha alpha; //character model
	
	private int score; //total points
	private int coreTotal; //money for upgrades
	
	//Allows rules for when keys pressed together
	/*private Boolean movingLeft = false;
	private Boolean movingRight = false;*/
	private Boolean moving = false;
	
	//Variables regarding shooting
	private Boolean shooting = false;
	private final int bulletSpeed = 5;
	
	private boolean bumpLevel = false;
	private boolean storeLevel = false;
	private boolean fortune = false;
	private String fortuneString = "";
	
	private boolean doubleShot = false;
	private boolean bounceShot = false;
	private boolean laserShot = false;
	private boolean novaShot = false;
	private boolean tripleShot = false;
	private boolean bossDisrupt = false;
	private boolean isSecond = false;
	
	
	private int autoShotCost = 1500; //TODO fine tune store
	private int doubleShotCost = 1000;
	private int tripleShotCost = 5000;
	private int laserShotCost = 15000;
	private int bounceShotCost = 7000;
	private int novaShotCost = 3000;
	private int disruptShotCost = 50000;
	
	private double enemyFireDensity = 0;
	private int totalEnemies = 0;
	
	public AlphaPanel(long period) {
		this.period = period;
		startTime = System.nanoTime();
		setBackground(Color.white);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
		setFocusable(true);
		requestFocus(); // the JPanel now has focus, so receives key events
		readyForTermination();
		
		alpha = new Alpha(startX, startY, leftMax, rightMax, 3/*shield start*/, 3/*shield max*/);
		mooks = new ArrayList<Enemy>();
		miniBoss = new ArrayList<MiniBoss>();
		boss = new ArrayList<Boss>();
		bullets = new ArrayList<Bullet>();
		badBullets = new ArrayList<BadBullets>();
		cores = new ArrayList<Core>();
		
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
			//framesSkipped += skips;
		}

		System.exit(0); // so window disappears
	} // end of run()

	private void restartGame() {
		level = 0;
		alpha.resetShieldMax(3);
		alpha.upgradeRemove();
		bullets.removeAll(bullets);
		badBullets.removeAll(badBullets);
		mooks.removeAll(mooks);
		miniBoss.removeAll(miniBoss);
		boss.removeAll(boss);
		cores.removeAll(cores);
		moving = false;
		score = 0;
		coreTotal = 0;
		gameOver = false;
		pauseOnce = true;
		doubleShot = false;
		tripleShot = false;
		laserShot = false;
		autoFire = false;
		bounceShot = false;
		novaShot = false;
		shootReset = true;
		fortune = false;
		bossDisrupt = false;
		checkStartnew();
	}

	private void gameUpdate() {
		if (!isPaused && !gameOver) {
			this.updateBullets();
			this.updateBadBullets();
			this.updateBaddies();
			this.updateCores();
		return;
		}

	} // end of gameUpdate()

	private void updateCores() {
		for(int i=0; i<cores.size(); i++) {
			cores.get(i).move();
		}
		
	}

	private void updateBadBullets() {
		if(isSecond){
			for(int i = 0; i < mooks.size(); i++){
				double randomBullet = Math.random();
				if(randomBullet <= enemyFireDensity) {
					int direct = 0;
					if(bumpLevel){
						randomBullet = Math.random();
						if (randomBullet > .5) {
							direct = 1;
						} else {
							direct = -1;
						}
					}
					badBullets.add(new BadBullets(mooks.get(i).getX(), mooks.get(i).getY(), direct, 1, 1, leftMax, rightMax));	
				}
			}
			for(int i = 0; i < miniBoss.size(); i++){
				double randomBullet = Math.random();
				randomBullet = randomBullet % miniBoss.size();
				randomBullet = randomBullet / (miniBoss.size()*5);
				if(randomBullet <= enemyFireDensity) {
					randomBullet = Math.random();
					int direct;
					if (randomBullet > .8) {
						direct = 1;
					} else if(randomBullet < .2) {
						direct = -1;
					} else {
						direct = 0;
					}
					badBullets.add(new BadBullets(miniBoss.get(i).getX(), miniBoss.get(i).getY(), direct, 1, 2, leftMax, rightMax));	
				}
			}
			for(int i = 0; i < boss.size(); i++){
				double randomBullet = Math.random();
				randomBullet = randomBullet % boss.size();
				randomBullet = randomBullet / (boss.size()*5);
				if(randomBullet <= enemyFireDensity) {
					randomBullet = Math.random();
					int direct;
					if (randomBullet > .5) {
						direct = 1;
					} else {
						direct = -1;
					}
					badBullets.add(new BadBullets(boss.get(i).getX(), boss.get(i).getY(), direct, 1, 2, leftMax, rightMax));	
				}
			}
		}
		for(int i=0; i< badBullets.size(); i++){
			badBullets.get(i).move();
			if(badBullets.get(i).getLoc()[1] > PHEIGHT-20) {
				badBullets.remove(i);
			}
		}
		
	}

	private void gameRender() {
		if (dbImage == null) {
			dbImage = createImage(PWIDTH, PHEIGHT);
			if (dbImage == null) {
				System.out.println("image error");
				return;
			} else
				dbg = dbImage.getGraphics();
		}

		// clear the background
		dbg.setColor(new Color(250, 253, 255));
		dbg.fillRect(0, 0, PWIDTH, PHEIGHT);

		dbg.setFont(new Font("Sans Serif", 1, 12));

		drawBackground(dbg);
		
		drawEntities(dbg);
		drawShieldBar(dbg);
		drawShip(dbg);
		drawBullets(dbg);
		
		updateLevel(dbg);
		
		dbg.setFont(new Font("Sans Serif", 1, 12));
		dbg.setColor(Color.black);
		dbg.drawString("Score: " + score, 10, 12);
		dbg.drawString("Cores: " + coreTotal, PWIDTH/2-10, 12);
		dbg.drawString("Level: " + level, PWIDTH-leftMax, 12);
		dbg.drawString("(P)ause or (Esc)ape", PWIDTH-90, PHEIGHT-7);

		if (isPaused) {
			pausedMessage(dbg);
		}

		if (gameOver || alpha.isDestroyed()) {
			gameOverMessage(dbg);
		}
	} // end of gameRender()

	private void drawShip(Graphics g) {
		alpha.draw(g, deltaVector, moving);
		
	}
	
	private void drawBullets(Graphics g) {
		if(shooting){
			//may be used for effects
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
			System.out.println("image error");
		}
	} // end of paintScreen()

	private void drawShieldBar(Graphics g) {
		float sh = alpha.getShield();
		float ma = alpha.getShieldMax();
		int barFill = (int)((sh/ma)*100);
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
/*		for(int i = 0; i <  badBullets.size(); i++) {
			badBullets.get(i).draw(g);
		}*/
		for (int i = 0; i < cores.size(); i++) {
			cores.get(i).draw(g);
		} // draw bullets
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).draw(g);
		} // draw bullets
		for (int i = 0; i < badBullets.size(); i++) {
			badBullets.get(i).draw(g);
		} // draw Bad bullets
		for (int i=0; i < mooks.size(); i++){
			if(!mooks.get(i).isDestroyed()){
				mooks.get(i).draw(g);
			}
		}
		if(!miniBoss.isEmpty()){
			if(!miniBoss.get(0).isDestroyed()){
				miniBoss.get(0).draw(g);
			}
		}
		if(!boss.isEmpty()){
			if(!boss.get(0).isDestroyed()){
				boss.get(0).draw(g);
			}
		}
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
		g.drawString("Pick up cores ( ) to purchase upgrades.",
				PWIDTH / 2 - 150+10, PHEIGHT / 2 - 30);
		g.setColor(Color.green);
		g.drawString("¤",
				PWIDTH / 2-38, PHEIGHT / 2 - 30);
		g.setColor(Color.black);
		g.drawString("Move with S<>D and shoot with K.",
				PWIDTH / 2 - 150+12, PHEIGHT / 2);
		g.drawString("Press \'o\' to play and \'p\' to pause.",
				PWIDTH / 2 - 150+17, PHEIGHT / 2 + 50);
		g.drawString("Hit Enter to restart game.",
				PWIDTH / 2 - 150+22, PHEIGHT / 2 + 80);
	}

	private void gameOverMessage(Graphics g)
	// center the game-over message in the panel
	{
		String msg1 = "Game Over.";
		String msg2 = "Score: " + score;
		String msg3 = "Cores: " + coreTotal;
		int total = Integer.valueOf(score)+(Integer.valueOf(coreTotal)/10);
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
		if(score < 600){
			g.setColor(Color.red);
			 msg5 = "Flunkie";
		} else if(score >= 600 && score < 1600) {
			g.setColor(Color.blue);
			msg5 = "Space Cadet";
		} else if(score >= 1600 && score < 10000) {
			g.setColor(Color.blue);
			msg5 = "Ensign";
		} else if(score >= 10000 && score < 50000) {
			g.setColor(Color.green);
			msg5 = "Lieutenant";
		} else if(score >= 50000 && score < 100000) {
			g.setColor(Color.green);
			msg5 = "Commander";
		} else if(score >= 100000 && score < 200000) {
			g.setColor(Color.black);
			msg5 = "Captain";
		} else if(score >= 250000 && score < 500000) {
			g.setColor(Color.black);
			msg5 = "Admiral";
		} else if(score >= 600000 && score < 700000) {
			g.setColor(Color.green);
			msg5 = "Lord Admiral";
		} else if(score >= 700000 && score < 800000) {
			g.setColor(Color.blue);
			msg5 = "Emperor";
		} else if(score >= 800000) {
			g.setColor(Color.red);
			msg5 = "KING OF SPACE";
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
				|| e.getKeyCode() == KeyEvent.VK_RIGHT) { //Moving right
			keyD = true;
			if(!moving){
				deltaVector = delta;
			}
			moving = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_S
				|| e.getKeyCode() == KeyEvent.VK_LEFT) { //Moving left
			keyS = true;
			if(!moving){
				deltaVector = 0-delta;
			}
			moving = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_K || e.getKeyCode() == KeyEvent.VK_UP) { //Request to fire
			keyK = true;
		}
		/*if (e.getKeyCode() == KeyEvent.VK_B) { //deBug key
			level++;
			coreTotal += 5000;
		}*/
		if (e.getKeyCode() == KeyEvent.VK_P) { //Pause game
			pauseGame();
		}
		if (e.getKeyCode() == KeyEvent.VK_O) { //Game [O]ver
			if (isPaused) {
				resumeGame();
				pauseOnce = false;
			}
		}
		
		if (e.getKeyCode() == KeyEvent.VK_X) { //eXit store
			if(level%3 == 0){
				storeLevel = false;
				fortune = false;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE) { //Option to end game at level 99
			if(level == 99){
				gameOver = true;	
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER) { //End game
			if(isPaused || gameOver || alpha.isDestroyed()){
				restartGame();
				pauseOnce = false;
				isPaused = false;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_1) { //Store options, charge shield
			if(storeLevel){
				if(coreTotal >= (alpha.getShield()*30)){
					if(alpha.getShield() < alpha.getShieldMax()) {
						coreTotal -= (alpha.getShield()*30);
						alpha.addShield();
					}
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_2) { //Store options, add max shield
			if(storeLevel){
				if(coreTotal >= (alpha.getShield()*100)){
					coreTotal -= (alpha.getShield()*100);
					alpha.addShieldMax();
					alpha.addShield();
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_3) { //Store options, autofire
			if(storeLevel){
				if(coreTotal >= autoShotCost){
					if(!autoFire) {
						coreTotal -= autoShotCost;
						autoFire = true;
					}
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_4) { //Store options, doubleShot
			if(storeLevel){
				if(coreTotal >= doubleShotCost){
					if(!doubleShot) {
						alpha.upgradeSet();
						coreTotal -= doubleShotCost;
						doubleShot = true;
					}
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_5) { //Store options, tripleShot 
			if(storeLevel){
				if(coreTotal >= tripleShotCost){
					if(!tripleShot) {
						alpha.upgradeSet();
						coreTotal -= tripleShotCost	;
						doubleShot = false;
						tripleShot = true;
					}
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_6) { //Store options, bounceShot
			if(storeLevel){
				if(coreTotal >= bounceShotCost){
					if(!bounceShot) {
						coreTotal -= bounceShotCost;
						bounceShot = true;
					}
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_7) { //Store options, laserShot
			if(storeLevel){
				if(coreTotal >= laserShotCost){
					if(!laserShot) {
						alpha.upgradeSet();
						coreTotal -= laserShotCost;
						doubleShot = false;
						tripleShot = false;
						laserShot = true;
					}
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_8) { //Store options, novaShot
			if(storeLevel){
				if(coreTotal >= novaShotCost){
					if(!novaShot){
						coreTotal -= novaShotCost;
						novaShot = true;
					}
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_9) { //Store options, doubleShot
			if(storeLevel){
				if(coreTotal >= disruptShotCost){
					if(!bossDisrupt){
						coreTotal -= disruptShotCost;
						bossDisrupt = true;
					}
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_0) { //Store options, fortune
			if(storeLevel){
				if(coreTotal >= level){
					if(!fortune){
						coreTotal -= level;
						fortune = true;
					}
				}
			}
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
			shootReset = true;
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

		/* Keep moving in the original direction until all keys released. 
		 * */
		if (keyD && !keyS) {
			deltaVector = delta;
		}
		if (keyS && !keyD) {
			deltaVector = 0-delta;
		}
		
		if (keyK) {
			if (!bullets.isEmpty()) { //If no bullets on screen
				if (bullets.get(bullets.size() - 1).canShoot(bullets.get(bullets.size() - 1), 100, autoFire, shootReset)) {
					//If enough time has elapsed to fire again
					if(bounceShot){ //Bounce shot can combine with other shots
							bullets.add(new Bullet(alpha.getX()-15, alpha.getY()+10, bulletSpeed, -1, "B"));
							bullets.add(new Bullet(alpha.getX()+15, alpha.getY()+10, bulletSpeed, 1, "B"));
						}
					if(doubleShot){ //Two bullet spread, cannot stack
						bullets.add(new Bullet(alpha.getX()-5, alpha.getY(), bulletSpeed, 0));
						bullets.add(new Bullet(alpha.getX()+5, alpha.getY(), bulletSpeed, 0));
					} else if(tripleShot){ //Three bullet spread, cannot stack
						bullets.add(new Bullet(alpha.getX()-10, alpha.getY(), bulletSpeed, 0));
						bullets.add(new Bullet(alpha.getX(), alpha.getY(), bulletSpeed, 0));
						bullets.add(new Bullet(alpha.getX()+10, alpha.getY(), bulletSpeed, 0));
					} else if(laserShot){ //fires a line of bullets, cannot stack
						for(int i=0; (alpha.getY()-(i*80))>35; i++){
							bullets.add(new Bullet(alpha.getX()-10, alpha.getY()-(i*80), bulletSpeed, 0, "L"));
						}
					} else { //default shot
						bullets.add(new Bullet(alpha.getX(), alpha.getY(),bulletSpeed, 0));
					}
				}
			//endif isempty	
			} else if(novaShot){ //Replacement shot if first on screen
				bullets.add(new Bullet(alpha.getX()-5, alpha.getY(), bulletSpeed, -3, "B"));
				bullets.add(new Bullet(alpha.getX()-3, alpha.getY(), bulletSpeed, -2, "B"));
				bullets.add(new Bullet(alpha.getX()-1, alpha.getY(), bulletSpeed, -1, "B"));
				bullets.add(new Bullet(alpha.getX()+1, alpha.getY(), bulletSpeed, 1, "B"));
				bullets.add(new Bullet(alpha.getX()+3, alpha.getY(),
						bulletSpeed, 2, "B"));
				bullets.add(new Bullet(alpha.getX()+5, alpha.getY(),
						bulletSpeed, 3, "B"));
			} else if(doubleShot){
				bullets.add(new Bullet(alpha.getX()-5, alpha.getY(),
					bulletSpeed, 0));
				bullets.add(new Bullet(alpha.getX()+5, alpha.getY(),
					bulletSpeed, 0));
			} else if(tripleShot){
				bullets.add(new Bullet(alpha.getX()-10, alpha.getY(),
					bulletSpeed, 0));
				bullets.add(new Bullet(alpha.getX(), alpha.getY(),
					bulletSpeed, 0));
				bullets.add(new Bullet(alpha.getX()+10, alpha.getY(),
					bulletSpeed, 0));
			} else if(laserShot){ //fires a line of bullets, cannot stack
					for(int i=0; (alpha.getY()-(i*80))>35; i++){
							bullets.add(new Bullet(alpha.getX()-10, alpha.getY()-(i*80), bulletSpeed, 0, "L"));
					}					
			} else { //default shot
				bullets.add(new Bullet(alpha.getX(), alpha.getY(),
						bulletSpeed, 0));
			}
		shootReset = false; //Do not allow firing again unless screen empty or autoFire
		}
			
			for(int i=0; i<badBullets.size(); i++){
				int[] posAr = badBullets.get(i).getLoc();
				if(posAr[1]+5 >= alpha.getY() && posAr[1] <= alpha.getY()+alpha.getHeight()+5){
					if(posAr[0] >= alpha.getX() && posAr[0] <= alpha.getX()+alpha.getWidth()){
						alpha.damage();
						badBullets.remove(i);
					}
				}
			}
			
			for(int i=0; i<cores.size(); i++){
				int[] posAr = cores.get(i).getLoc();
				if(posAr[1] >= alpha.getY() && posAr[1] <= alpha.getY()+alpha.getHeight()){
					if(posAr[0] >= alpha.getX()-12 && posAr[0] <= alpha.getX()+alpha.getWidth()+21){
						coreTotal += (cores.get(i).getValue()*100);
						cores.remove(i);
					}
				}
			}
		
		}// endif KeyK
	
	private void updateBullets() {
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).move();
			int[] array = bullets.get(i).getLoc();
			if (array[1] < 35) {
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
			for (int j = 0; j < bullets.size(); j++) {

				hit = mooks.get(i).gotHit(bullets.get(j));
				if (hit) {
					mooks.get(i).damage();
					bullets.remove(j);
				}
			}
		
			if (mooks.get(i).getHealth() <= 0) {
				score = score+((mooks.get(i).getType())*100);
				double rand = Math.random();
				if(rand > .5){
					cores.add(new Core(mooks.get(i).getX(), mooks.get(i).getY(), mooks.get(i).getType()));
				}
				mooks.remove(i);
			}
		} //end mook update
		
		//Move moiniBoss
				for (int i = 0; i < miniBoss.size(); i++) {
					miniBoss.get(i).move();
				
					//Check if bullet hit miniBoss
					for (int j = 0; j < bullets.size(); j++) {

						hit = miniBoss.get(i).gotHit(bullets.get(j));
						if (hit) {
							miniBoss.get(i).damage();
							bullets.remove(j);
						}
					}
				
					if (miniBoss.get(i).getHealth() <= 0) {
						miniBoss.remove(i);
						score += (500*level);
						coreTotal += (50*level);
					}
				} //end miniBoss update

				//Move boss
				for (int i = 0; i < boss.size(); i++) {
					boss.get(i).move();
				
					//Check if bullet hit boss
					for (int j = 0; j < bullets.size(); j++) {

						hit = boss.get(i).gotHit(bullets.get(j));
						if (hit) {
							boss.get(i).damage();
							bullets.remove(j);
						}
					}
				
					if (boss.get(i).getHealth() <= 0) {
						boss.remove(i);
						score += (1000*level);
						coreTotal += (100*level);
					}
				} //end boss update
	}// end baddies
	
	private void updateLevel(Graphics g) {
		if(level == 99){
			g.drawString("Victory achieved!", 85, 100);
			g.drawString("Thanks for playing.", 75, 150);
			g.drawString("Press SPACE to view your score.", 50, 200);
		} else if(mooks.isEmpty() && miniBoss.isEmpty() && boss.isEmpty() && level != 99){
			if(!storeLevel){
				bumpLevel = false;
				level++;
				totalEnemies = 0;
			}
			if(level == 98) { //TODO big finale
				miniBoss.add(new MiniBoss(PWIDTH/2, 50, 1, level+2, leftMax, rightMax));
				boss.add(new Boss(PWIDTH/4, 50, 1, level*2, leftMax, rightMax));
				mooks.add(new Enemy(leftMax+30, 70, 3, 2, level));
				mooks.add(new Enemy(rightMax-30, 70, 3, 2, level));
				int x = 3;
				for(int i=0; i<x; i++){
					mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 100, 3, 1, level));
					mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 130, 2, 1, level));
					mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 160, 1, 1, level));
				}
			} else if (level == 1){
				int x = 5;
				for(int i=0; i<x; i++){
					mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 80, 1, 1, level));
					mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 110, 1, 1, level));
					mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 140, 1, 1, level));
				}
			} else if(level % 3 == 0 && level != 99) {
				storeLevel = true;
				if((level/3)%3 ==0){
					g.drawString("Welcome to the store.", leftMax, 50);
				} else if((level/3)%3 ==1){
					g.drawString("Zbart's Hyperspace Supply.", leftMax, 50);				
				} else if((level/3)%3 ==2){
					g.drawString("You again?", leftMax, 50);
				}
				if(alpha.getShield() == alpha.getShieldMax()){
					g.setColor(Color.gray);
					g.drawString("1) Restore shield ("+(alpha.getShield()*30)+" Cr)", leftMax, 70);
					g.setColor(Color.black);
				} else {
					g.drawString("1) Restore shield ("+(alpha.getShield()*30)+" Cr)", leftMax, 70);
				}
				g.drawString("2) Add max shield ("+(alpha.getShieldMax()*100)+" Cr)", leftMax, 90);
				if(autoFire){
					g.setColor(Color.gray);
					g.drawString("3) Auto fire ("+autoShotCost+" Cr)", leftMax, 110);
					g.setColor(Color.black);
				} else {
					g.drawString("3) Auto fire ("+autoShotCost+" Cr)", leftMax, 110);
				}
				
				/* Do not allow purchase of lower weapon upgrade than equipped
				 * Position: doubleShot
				 * Higher: tripleShot, laserShot
				 * */
				if(doubleShot == true || tripleShot == true || laserShot == true){
					g.setColor(Color.gray);
					g.drawString("4) Double shot ("+doubleShotCost+" Cr)", leftMax, 130);
					g.setColor(Color.black);
				} else {
					g.drawString("4) Double shot ("+doubleShotCost+" Cr)", leftMax, 130);
				}
				
				/* Do not allow purchase of lower weapon upgrade than equipped
				 * Position: tripleShot
				 * Higher: laserShot
				 * */
				if(tripleShot == true || laserShot == true){
					g.setColor(Color.gray);
					g.drawString("5) Triple shot ("+tripleShotCost+" Cr)", leftMax, 150);
					g.setColor(Color.black);
				} else {
					g.drawString("5) Triple shot ("+tripleShotCost+" Cr)", leftMax, 150);
				}
				
				if(bounceShot == true){
					g.setColor(Color.gray);
					g.drawString("6) Bounce shot ("+bounceShotCost+" Cr)", leftMax, 170);
					g.setColor(Color.black);
				} else {
					g.drawString("6) Bounce shot ("+bounceShotCost+" Cr)", leftMax, 170);
				}
				
				
				/* Do not allow purchase of lower weapon upgrade than equipped
				 * Position: laserShot
				 * Higher: TBD
				 * */
				if(laserShot == true){
					g.setColor(Color.gray);
					g.drawString("7) Laser shot ("+laserShotCost +" Cr)", leftMax, 190);
					g.setColor(Color.black);
				} else {
					g.drawString("7) Laser shot ("+laserShotCost +" Cr)", leftMax, 190);
				}
				
				/* Nova shot replaces current shot on first fire when the screen is empty
				 * Six bullets fire in rapid spread pattern
				 * Stackable: (special)
				 * */
				if(novaShot == true){
					g.setColor(Color.gray);
					g.drawString("8) Nova shot ("+novaShotCost+" Cr)", leftMax, 210);
					g.setColor(Color.black);
				} else {
					g.drawString("8) Nova shot ("+novaShotCost+" Cr)", leftMax, 210);
				}
				if(bossDisrupt == true){
					g.setColor(Color.gray);
					g.drawString("9) Boss disruptor ("+disruptShotCost+" Cr)", leftMax, 230);
					g.setColor(Color.black);
				} else {
					g.drawString("9) Boss disruptor ("+disruptShotCost+" Cr)", leftMax, 230);
				}
				if(fortune == true){
					g.setColor(Color.gray);
					g.drawString("0) Fortune ("+level+" Cr)", leftMax, 250);
					g.setColor(Color.red);
					switch (level) {
						case 3: fortuneString = "An investment will pay off.";
						break;
						case 6: fortuneString = "Only the center takes damage.";
						break;
						case 9: fortuneString = "Wings can collect cores.";
						break;
						case 12: fortuneString = "What has 4 wheels & flies?";
						break;
						case 15: fortuneString = "A garbage truck! Hyuk-hyuk.";
						break;
						case 18: fortuneString = "Novashot without autofire?";
						break;
						case 21: fortuneString = "A rock, a river, a tree.";
						break;
						case 24: fortuneString = "Drink Hyper Cola!";
						break;
						case 27: fortuneString = "Trapped in fortune factory.";
						break;
						case 30: fortuneString = "There is a rare smiley face.";
						break;
						case 33: fortuneString = "Did you waste those cores?";
						break;
						case 36: fortuneString = "Even bosses show pain.";
						break;
						case 39: fortuneString = "The answer is coming up!";
						break;
						case 42: fortuneString = "What was the question?";
						break;
						case 45: fortuneString = "What's a young horse called?";
						break;
						case 48: fortuneString = "99 is the level the game ends.";
						break;
						case 51: fortuneString = "Disrupted bosses take 1 hit.";
						break;
						case 54: fortuneString = "Auto-nova Laserbounce Shot?";
						break;
						case 57: fortuneString = "@--->--->-----";
						break;
						case 60: fortuneString = "What's brown and sticky?";
						break;
						case 63: fortuneString = "A stick!";
						break;
						case 66: fortuneString = "Play it backwards.";
						break;
						case 69: fortuneString = "Insert joke here.";
						break;
						case 72: fortuneString = "See you in 24 levels!";
						break;
						case 75: fortuneString = "Highscore counts 1/10 cores.";
						break;
						case 78: fortuneString = "Smile, you're being recorded.";
						break;
						case 81: fortuneString = "...just kidding.";
						break;
						case 84: fortuneString = "I wasn't kidding about that.";
						break;
						case 87: fortuneString = "Or was I?";
						break;
						case 90: fortuneString = "Just 9 more levels.";
						break;
						case 93: fortuneString = "I was kidding, before.";
						break;
						case 96: 
							fortuneString = "Did that investment pay off?";
							doubleShot = false;
							tripleShot = false;
							laserShot = true;
							bounceShot = true;
							novaShot = true;
							autoFire = true;
							bossDisrupt = true;
						break;
					}
					g.drawString(fortuneString, leftMax, 300);
				} else {
					g.drawString("0) Fortune ("+level+" Cr)", leftMax, 250);
				}
				g.drawString("Press (x) to continue", leftMax, 270);
			} else if (level % 2 == 0 && level%3 != 0){
				if(level%4 == 0){
					bumpLevel = true;
				}
				int x = 5;
					for(int i=0; i<x; i++){
						mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 100, 2, 1, level));
					}
					mooks.add(new Enemy((((PWIDTH-(leftMax*2))/2)+leftMax-30), 70, 3, 2, level));
			}else if(level !=99) {
				double levelRand = Math.random();
				if(levelRand < .2){
					int x = 5;
					for(int i=0; i<x; i++){
						mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 100, 3, 1, level));
						mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 130, 2, 1, level));
					}
				} else if (levelRand >= .2 && levelRand <= .8){
					int x = 5;
					for(int i=0; i<x; i++){
						mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 100, 2, 1, level));
						mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 130, 1, 1, level));
						mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 160, 1, 1, level));
					}
				} else if (levelRand >= .8 && levelRand <= .95){
					int x = 3;
					for(int i=0; i<x; i++){
						mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 100, 3, 1, level));
						mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 130, 2, 1, level));
						mooks.add(new Enemy((((PWIDTH-(leftMax*2))/x)*i+leftMax)-(10*i), 160, 1, 1, level));
					}
				} else { //5% chance of smiley face
					mooks.add(new Enemy((leftMax+25), 120, 2, 1, level));
					mooks.add(new Enemy((rightMax-105), 120, 2, 1, level));
					mooks.add(new Enemy((leftMax +15), 190, 3, 1, level));
					mooks.add(new Enemy((rightMax -95), 190, 3, 1, level));
					mooks.add(new Enemy((leftMax +45), 210, 3, 1, level));
					mooks.add(new Enemy((rightMax -125), 210, 3, 1, level));
				}
			}
			if(level%5 == 0 && level%3 != 0){ //levels with miniboss
				if(bossDisrupt){
					miniBoss.add(new MiniBoss(PWIDTH/2, 50, 1, 1, leftMax, rightMax));
				} else {
					miniBoss.add(new MiniBoss(PWIDTH/2, 50, 1, level+2, leftMax, rightMax));
				}
			}
			if(level%11 == 0 && level%3 != 0){ //levels with boss
				if(bossDisrupt){
					boss.add(new Boss(PWIDTH/4, 50, 1, 1, leftMax, rightMax));
				} else {
					boss.add(new Boss(PWIDTH/4, 50, 1, level*2, leftMax, rightMax));
				}
			}
			if(!storeLevel){ //final processing must be done after enemeis placed
				totalEnemies += mooks.size();
				totalEnemies += miniBoss.size();
				totalEnemies += boss.size();
				double lev = (double)level;
				double tE = (double)totalEnemies;
				enemyFireDensity = (((lev)/((lev+tE)*2))*Math.random());
			}
		}
	}
	
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
			isSecond = true;
		} else {
			isSecond = false;
		}
	}

} // end of AlphaPanel class