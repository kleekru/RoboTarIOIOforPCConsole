// RoboTar is a product of Life Upgrades Group, a company dedicated to helping everyone live with 
// ever changing technology options.
// RoboTar was built and developed by Kevin Krumwiede with assistance from Ytai Ben-Tsvi,
// who is the inventor of the IOIO board which makes up much of the brains for RoboTar.

// TODO Save Attributes for a chord including an image, name, a logical structure that can be sent to IOIO
// TODO Send full Chord to RoboTar based on button push
// TODO Send Next chord to RoboTar (saved in a song) based on a button push
// TODO Create songs page
// TODO Associate an image with each chord name
// TODO Add a custom chord
// TODO Create import of full songs based on XML file
// TODO Create a push button to download songs from an internet location (RESTful service?)
// TODO Create an About page


package ioio.robotar.pcconsole;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;

/**
 * RoboTar GUI main window.
 */
public class RoboTarStartPage {

	private static final Logger LOG = LoggerFactory.getLogger(RoboTarStartPage.class);
	
	private JFrame frmBlueAhuizote;
	public JButton btnChords;
	public JButton btnSongs;
	/**
	 * This is the field, which RoboTarIOIOforPCConsole reads in loop() 
	 * and uses its values to send them to the device.
	 */
	private ServoSettings servoSettings;
	
	/**
	 * LED settings for the current chord.
	 */
	private LEDSettings leds;
	
	/**
	 * This will hold all chord libraries loaded in one instance.
	 */
	private ChordManager chordManager = new ChordManager();
	private RoboTarChordsPage chordsPage;
	private RoboTarSongsPage songsPage;
	private RoboTarHelp helpPage;
	
	private ResourceBundle messages;

	/** per user preferences */
	private RoboTarPreferences preferences = RoboTarPreferences.load();
	
	//private RoboTarIOIOforPCConsole console;
	
	public RoboTarPreferences getPreferences() {
		return preferences;
	}

	/**
	 * Launch the application.
	 */
	public void mainstart(RoboTarIOIOforPCConsole console, String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//this.console = console;
					frmBlueAhuizote.pack();
					frmBlueAhuizote.setLocationByPlatform(true);
					frmBlueAhuizote.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public RoboTarStartPage() {
		try {
			initialize();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void closingMethod() {
		StringBuilder sb = new StringBuilder(100);
		if (chordsPage != null && chordsPage.isUnsavedChords()) {
			sb.append("There are unsaved chords on Chords page!\n\n");
		}
		if (songsPage != null && songsPage.getModifiedCount() > 0) {
			sb.append("There may be unsaved songs (");
			sb.append(songsPage.getModifiedCount());
			sb.append(") on Songs page!\n\n");
		}
		sb.append("Are You sure to close RoboTar?");
		int confirm = JOptionPane.showOptionDialog(frmBlueAhuizote,
                sb.toString(),
                "Exit confirmation", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (confirm == JOptionPane.YES_OPTION) {
        	// save all needed information for next start
        	preferences.save();
        	LOG.info("RoboTar finished with dialog message: {}", sb.toString());
        	// and exit
            System.exit(0);
        }
    }
	
	private class ExitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            closingMethod();
        }
    }
	
	private class ExitAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            closingMethod();
        }
    }
    
	/**
	 * Initialize the contents of the frame.
	 * @throws BackingStoreException 
	 */
	public void initialize() throws BackingStoreException {
		getChordManager();
		servoSettings = ServoSettings.loadCorrectionsFrom(new File(preferences.getCorrectionsFile()));
		messages = ResourceBundle.getBundle("ioio.robotar.pcconsole.RoboTarBundle", Locale.ENGLISH);
		
		frmBlueAhuizote = new JFrame();
		frmBlueAhuizote.setBackground(new Color(0, 0, 255));
		frmBlueAhuizote.setBounds(100, 100, 800, 600);
		frmBlueAhuizote.getContentPane().setBackground(new Color(30, 144, 255));
		frmBlueAhuizote.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmBlueAhuizote.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setSize(new Dimension(50, 50));
		lblNewLabel.setLocation(new Point(0, 43));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		java.net.URL res = RoboTarStartPage.class.getResource("/data/BlueAhuizoteIcon.png");
		System.out.println(res.getPath());
		lblNewLabel.setIcon(new ImageIcon(res));
		//lblNewLabel.setIcon(new ImageIcon(RoboTarStartPage.class.getResource("/data/BlueAhuizoteIcon.png")));
		lblNewLabel.setBorder(null);
		frmBlueAhuizote.getContentPane().setBackground(Color.BLUE);
		frmBlueAhuizote.getContentPane().add(lblNewLabel, BorderLayout.WEST);
		
		Action startChordsAction = new StartChordsPageAction(messages.getString("robotar.menu.chords"), KeyEvent.VK_C);
		btnChords = new JButton("");
		btnChords.addActionListener(startChordsAction);
		btnChords.setForeground(Color.BLUE);
		btnChords.setMinimumSize(new Dimension(100, 100));
		btnChords.setMaximumSize(new Dimension(100, 100));
		btnChords.setBackground(Color.BLUE);
		btnChords.setName("ChordsButton");
		btnChords.setMargin(new Insets(0, 0, 0, 0));
		btnChords.setIcon(new ImageIcon(RoboTarStartPage.class.getResource("/data/chords.png")));
		btnChords.setSelectedIcon(null);
		btnChords.setRolloverIcon(null);
		btnChords.setToolTipText("Create or Browse Chords");
		btnChords.setRolloverSelectedIcon(null);
		frmBlueAhuizote.getContentPane().setBackground(Color.BLUE);
		frmBlueAhuizote.getContentPane().add(btnChords, BorderLayout.CENTER);
		
		Action startSongsAction = new StartSongsPageAction(messages.getString("robotar.menu.songs"), KeyEvent.VK_S);
		btnSongs = new JButton("");
		btnSongs.addActionListener(startSongsAction);
		btnSongs.setBorderPainted(false);
		btnSongs.setBackground(Color.BLUE);
		btnSongs.setForeground(Color.BLUE);
		btnSongs.setMargin(new Insets(0, 0, 0, 0));
		btnSongs.setToolTipText("Select or Create Songs");
		btnSongs.setIcon(new ImageIcon(RoboTarStartPage.class.getResource("/data/SheetMusic.png")));
		btnSongs.setName("SongsButton");
		frmBlueAhuizote.getContentPane().setBackground(Color.BLUE);
		frmBlueAhuizote.getContentPane().add(btnSongs, BorderLayout.EAST);
		
		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setForeground(new Color(30, 144, 255));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setIcon(new ImageIcon(RoboTarStartPage.class.getResource("/data/RoboTarLogoFont3.png")));
		lblNewLabel_1.setBackground(Color.GRAY);
		frmBlueAhuizote.getContentPane().add(lblNewLabel_1, BorderLayout.NORTH);
		
		JLabel lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setIcon(new ImageIcon(RoboTarStartPage.class.getResource("/data/junglespeakermountainsmall.png")));
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		frmBlueAhuizote.getContentPane().add(lblNewLabel_2, BorderLayout.SOUTH);
		
        frmBlueAhuizote.addWindowListener(new ExitAdapter());
        
		JMenuBar menuBar = new JMenuBar();
		frmBlueAhuizote.setJMenuBar(menuBar);
		
		JMenu mnFileMenu = new JMenu(messages.getString("robotar.menu.file"));
		menuBar.add(mnFileMenu);
		
		JMenuItem mntmAbout = new JMenuItem(messages.getString("robotar.menu.about"));
		mntmAbout.setEnabled(false);
		mnFileMenu.add(mntmAbout);
		
		JMenuItem mntmExit = new JMenuItem(messages.getString("robotar.menu.exit"));
		mntmExit.addActionListener(new ExitListener());
		mntmExit.setMnemonic(KeyEvent.VK_X);
		mnFileMenu.add(mntmExit);
		
		JMenu mnChordLauncher = new JMenu(messages.getString("robotar.menu.chord_launcher"));
		menuBar.add(mnChordLauncher);
		
		JMenuItem mntmChords = new JMenuItem(startChordsAction);
		mnChordLauncher.add(mntmChords);
		
		JMenuItem mntmSongs = new JMenuItem(startSongsAction);
		mnChordLauncher.add(mntmSongs);
		
		JMenuItem mntmSongPlayer = new JMenuItem(messages.getString("robotar.menu.song_player"));
		mntmSongPlayer.setEnabled(false);
		mnChordLauncher.add(mntmSongPlayer);
		
		JMenu mnUtilities = new JMenu(messages.getString("robotar.menu.utilities"));
		menuBar.add(mnUtilities);
		
		JMenuItem corr = new JMenuItem(new CorrectionsAction(messages.getString("robotar.menu.servo_corrections"), KeyEvent.VK_R));
		mnUtilities.add(corr);
		
		JMenuItem mntmTuner = new JMenuItem(messages.getString("robotar.menu.tuner"));
		mntmTuner.setEnabled(false);
		mnUtilities.add(mntmTuner);
		
		JMenuItem mntmMetronome = new JMenuItem(messages.getString("robotar.menu.metronome"));
		mntmMetronome.setEnabled(false);
		mnUtilities.add(mntmMetronome);
		
		JMenuItem mntmSongDownloads = new JMenuItem(messages.getString("robotar.menu.song_downloads"));
		mntmSongDownloads.setEnabled(false);
		mnUtilities.add(mntmSongDownloads);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		Action startHelpAction = new StartHelpPageAction("Help", KeyEvent.VK_H);
		JMenuItem mntmHelp = new JMenuItem("RoboTar Help");
		mnHelp.add(mntmHelp);
		mntmHelp.addActionListener(startHelpAction);
		
		
	}
	
	protected void showCorrectionsDialog(ActionEvent evt) {
		CorrectionsDialog dlg = new CorrectionsDialog(this);
		dlg.setVisible(true);
	}

	private abstract class MyAction extends AbstractAction {
		public MyAction(String text, int mnemonic) {
			super(text);
		    putValue(MNEMONIC_KEY, mnemonic);
		}
	}
	
	private class StartChordsPageAction extends MyAction {
		public StartChordsPageAction(String text, int mnemonic) {
	       super(text, mnemonic);
	    }

		@Override
		public void actionPerformed(ActionEvent e) {
			startChordsPage();
		}
	}
	
	private class StartSongsPageAction extends MyAction {
		public StartSongsPageAction(String text, int mnemonic) {
	       super(text, mnemonic);
	    }

		@Override
		public void actionPerformed(ActionEvent e) {
			startSongsPage();
		}
	}
	
	private class StartHelpPageAction extends MyAction {
		public StartHelpPageAction(String text, int mnemonic) {
	       super(text, mnemonic);
	    }

		@Override
		public void actionPerformed(ActionEvent e) {
			startHelpPage();
		}
	}
	
	private class CorrectionsAction extends MyAction {
		public CorrectionsAction(String text, int mnemonic) {
			super(text, mnemonic);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			showCorrectionsDialog(e);
		}
	};
		
	public void startChordsPage() {
		if (chordsPage == null) {
			chordsPage = new RoboTarChordsPage(this);
		}
		getChordsPage().setVisible(true);
	}
	
	public RoboTarChordsPage getChordsPage() {
		return chordsPage;
	}

	public void startHelpPage() {
		if (helpPage == null) {
			helpPage = new RoboTarHelp(this);
		}
		getHelpPage().setVisible(true);
	}
	
	public RoboTarHelp getHelpPage() {
		return helpPage;
	}

	
	public void startSongsPage() {
		if (songsPage == null) {
			songsPage = new RoboTarSongsPage(this);
		}
		getSongsPage().setVisible(true);
	}

	public ChordManager getChordManager() {
		// TODO rewrite to better use of singleton pattern - synchronize!
		if (!chordManager.isInitialized()) {
			chordManager.initialize(preferences);
		}
		return chordManager;
	}

	public void setChordManager(ChordManager chordManager) {
		this.chordManager = chordManager;
	}

	public void setChordsPage(RoboTarChordsPage chordsPage) {
		this.chordsPage = chordsPage;
	}

	public RoboTarSongsPage getSongsPage() {
		return songsPage;
	}

	public void setSongsPage(RoboTarSongsPage songsPage) {
		this.songsPage = songsPage;
	}

	public void setHelpPage(RoboTarHelp helpPage) {
		this.helpPage = helpPage;
	}
	
	public ServoSettings getServoSettings() {
		return servoSettings;
	}

	public void setServoSettings(ServoSettings chordServo) {
		this.servoSettings = chordServo;
	}

	public LEDSettings getLeds() {
		return leds;
	}

	public void setLeds(LEDSettings leds) {
		this.leds = leds;
	}

	// mediator pattern...? 
	public boolean isActiveSongEditable() {
		return (songsPage != null && songsPage.isEditing()); 
	}

	public ResourceBundle getMessages() {
		return messages;
	}

	public void setMessages(ResourceBundle messages) {
		this.messages = messages;
	}
	
}
