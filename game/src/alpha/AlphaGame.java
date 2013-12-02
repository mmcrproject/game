package alpha;

import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class AlphaGame extends JFrame implements WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8804945792633445862L;

	private static int DEFAULT_FPS = 50;

	private AlphaPanel ap;

	public AlphaGame(/*AlphaGameag,*/ final long period) {
	
		super("Alpha v.1.5");
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				makeGUI(period);
			}
		});
		setLocation(200,100);
		addWindowListener(this);
	
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				pack();
				setResizable(false);
				setVisible(true);
			}
		});
	} // end of KirbyChase() constructor

	private void makeGUI(long period) {
		Container c = getContentPane();
		ap = new AlphaPanel(/*this,*/ period);
		c.add(ap, "Center");

	} // end of makeGUI()

	// ----------------- window listener methods -------------

	public void windowActivated(WindowEvent e) {
		
	}

	public void windowDeactivated(WindowEvent e) {
		ap.pauseGame();
	}

	public void windowDeiconified(WindowEvent e) {
		
	}

	public void windowIconified(WindowEvent e) {
		ap.pauseGame();
	}

	public void windowClosing(WindowEvent e) {
		ap.stopGame();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	// ----------------------------------------------------

	public static void main(String args[]) {
		int fps = DEFAULT_FPS;
		if (args.length != 0)
			fps = Integer.parseInt(args[0]);

		long period = (long) 1000.0 / fps;

			new AlphaGame(period * 1050000L); // ms --> nanosecs
	}

} // end of KirbyGame class
