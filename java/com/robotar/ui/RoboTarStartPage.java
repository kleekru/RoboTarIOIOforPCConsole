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


package com.robotar.ui;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOSwingApp;

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

import net.infotrek.util.prefs.FilePreferencesFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robotar.ioio.LEDSettings;
import com.robotar.ioio.Pins;
import com.robotar.ioio.ServoSettings;
import com.robotar.util.RoboTarPreferences;

import java.awt.Point;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;

/**
 * RoboTar GUI main window.
 */
public class RoboTarStartPage extends IOIOSwingApp {

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
	
	private boolean stateLedOn = false;

	/**
	 * This will hold all chord libraries loaded in one instance.
	 */
	private ChordManagerPC chordManager = new ChordManagerPC();
	private RoboTarChordsPage chordsPage;
	private RoboTarSongsPage songsPage;
	private RoboTarHelp helpPage;
	
	private ResourceBundle messages;

	/** per user preferences */
	private RoboTarPreferences preferences = RoboTarPreferences.load();
	
	public static final String ROBOTAR_FOLDER = ".robotar";
	public static final String ROBOTAR_PROPS_FILE = ".robotar.properties";
	
	public static void main(String[] args) throws Exception {
		// set preferences factory implementation and filename
		System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
	    System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
	    		ROBOTAR_FOLDER + File.separator + ROBOTAR_PROPS_FILE);
	 
	    // check that the folder .robotar exists, if not, create it. 
	    String userHome = System.getProperty("user.home");
	    String folder = userHome + File.separator + ROBOTAR_FOLDER;
	    Path robotarFolder = Paths.get(folder);
	    if (!Files.exists(robotarFolder)) {
	    	try {
	    		Files.createDirectory(robotarFolder);
	    		LOG.debug(".robotar folder created");
	    	} catch (Exception e) {
	    		LOG.error("cannot create .robotar folder in user home!", e);
	    		// continue without it - problems will arise, but the software will be at least partially working
	    	}
	    }
	    
	    // now start the app
	    new RoboTarStartPage().go(args);
	}
	
	@Override
	protected Window createMainWindow(String[] args) {
		// ... create main window ...
		try {
			initialize();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOG.info("End of the Run method");
		if (frmBlueAhuizote == null) {
			LOG.error("frame still not available"); ///!!
		}
		return frmBlueAhuizote;
	}
	
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
		servoSettings = ServoSettings.loadCorrectionsFrom(new File(preferences.getCorrectionsFile()), ROBOTAR_FOLDER);
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
		
		
		frmBlueAhuizote.pack();
		frmBlueAhuizote.setLocationByPlatform(true);
		frmBlueAhuizote.setVisible(true);
		
		// display warning if device not yet configured!
		if (!servoSettings.isAnyCorrectionSet()) {
			JOptionPane.showMessageDialog(frmBlueAhuizote, 
					messages.getString("robotar.corrections.notset"), 
					"RoboTar WARNING", JOptionPane.WARNING_MESSAGE);
		}
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

	public ChordManagerPC getChordManager() {
		// TODO rewrite to better use of singleton pattern - synchronize!
		if (!chordManager.isInitialized()) {
			chordManager.initialize(preferences);
		}
		return chordManager;
	}

	public void setChordManager(ChordManagerPC chordManager) {
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
	
	@Override
	public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
		return new BaseIOIOLooper() {
			private final int I2C_PAIR = 0; //IOIO Pair for I2C
			private static final float FREQ = 50.0f;
			private static final int PCA_ADDRESS = 0x40;
			private static final byte PCA9685_MODE1 = 0x00;
			private static final byte PCA9685_PRESCALE = (byte) 0xFE;
			private TwiMaster twi_;
			
			private DigitalOutput stateLED;
			private DigitalInput pedalButton;
			// all the leds
			private DigitalOutput[][] fretLEDs = new DigitalOutput[6][4];
			// reference to actually turned on leds, to be able to turn them off
			private DigitalOutput[] fretLEDsTurnedOn = new DigitalOutput[6];
			
			private boolean lastKnownPedalPosition = true;
			
			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
				LOG.info("IOIO is connected");
				
				// on-board pin
				stateLED = ioio_.openDigitalOutput(IOIO.LED_PIN, true);

				// pedal input setup
				pedalButton = ioio_.openDigitalInput(Pins.PEDAL_PIN, DigitalInput.Spec.Mode.PULL_UP);
				
				// fret leds output setup
				fretLEDs = prepareLEDs(false);
				
				// Setup IOIO TWI Pins
				twi_ = ioio_.openTwiMaster(I2C_PAIR, TwiMaster.Rate.RATE_1MHz, false);
				
				reset();
			}

			private DigitalOutput[][] prepareLEDs(boolean startValue) throws ConnectionLostException {
				for (int i = 0; i < 6; i++) {
					for (int j = 0; j < 4; j++) {
						// pin matching Pins.java
						fretLEDs[i][j] = ioio_.openDigitalOutput(Pins.getLEDPin(i, j+1), startValue);
					}
				}
				return fretLEDs;
			}
			
			private void reset() throws ConnectionLostException,
			InterruptedException {
				// Set prescaler - see PCA9685 data sheet
				LOG.info("Start of the BaseIOIOLooper.reset method");
				float prescaleval = 25000000;
				prescaleval /= 4096;
				prescaleval /= FREQ;
				prescaleval -= 1;
				byte prescale = (byte) Math.floor(prescaleval + 0.5);
				
				write8(PCA9685_MODE1, (byte) 0x10); // go to sleep... prerequisite to set prescaler
				write8(PCA9685_PRESCALE, prescale); // set the prescaler
				write8(PCA9685_MODE1, (byte) 0x20); // Wake up and set Auto Increment
			}
			
			private void write8(byte reg, byte val) throws ConnectionLostException,
				InterruptedException {
				LOG.info("Start of the write8 method");
				byte[] request = {reg, val};
				twi_.writeReadAsync(PCA_ADDRESS, false, request, request.length, null, 0);
			}
		
			@Override
			public void loop() throws ConnectionLostException,
					InterruptedException {
				//LOG.info("Start of the loop method");
				stateLED.write(!stateLedOn);

				// initial position
				// high = true, low = false
				boolean pedalInHighPosition = pedalButton.read();
				//LOG.debug("current position of pedal is: {}", pedalInHighPosition);

				//LOG.debug("lastPedalPosition: {}", lastKnownPedalPosition);
				if (lastKnownPedalPosition == pedalInHighPosition) {
					// no change from last time
					return;
				}
				
				if (!pedalInHighPosition) {
					LOG.debug("Pedal is pressed");
					// PEDAL IS PRESSED
					stateLedOn = true;

					// we are checking and logging the status first
					if (frmBlueAhuizote == null) {
						LOG.error("There is no RoboTar GUI!");
					} else {
						if (RoboTarStartPage.this.getChordsPage() == null) {
							LOG.debug("informative - there is no chords page");
						}
						if (RoboTarStartPage.this.getSongsPage() == null) {
							LOG.debug("informative - there is no songs page");
						}
						if (RoboTarStartPage.this.getServoSettings() == null) {
							// this should not happen, servo settings are initialized to neutral positions in the constructor
							LOG.warn("There is no chord chosen!");
						} else {
							// if songs page exists and we already play the song, play next chord
							if (RoboTarStartPage.this.getSongsPage() != null && RoboTarStartPage.this.getSongsPage().isPlaying()) {
								RoboTarStartPage.this.getSongsPage().simPedalPressed();
							} else if (RoboTarStartPage.this.getChordsPage() != null) {
								// if not, and chords page exists, play chord that is set in radio buttons
								RoboTarStartPage.this.getChordsPage().prepareChord();
							}
							
							// everything is set correctly and we have servo settings available 
							// (either from songs or chords page, or default - neutral) or last one? - check
							ServoSettings chordServoValues = RoboTarStartPage.this.getServoSettings();
							LEDSettings leds = RoboTarStartPage.this.getLeds();
							LOG.debug("got chord: {}", chordServoValues.debugOutput());
							LOG.debug("leds: {}", leds);
							long timeStart = System.currentTimeMillis();
							for (int i = 0; i < 6; i++) {
								int servoNumber = chordServoValues.getServos()[i];
								float servoValue = chordServoValues.getValues()[i];
								setServo(servoNumber, servoValue);
								if (leds != null) {
									LOG.debug("leds 2: {}", leds.getLeds());
									if (leds.getLeds() != null) {
										setLED(i, leds.getLeds()[i]);
									}
								}
							}
							long timeEnd = System.currentTimeMillis();
							LOG.debug("It took {} ms to execute 6 servos and LEDs", timeEnd - timeStart);
						}
					}
				} else {
					LOG.debug("Pedal is released");
					// PEDAL IS RELEASED
					// turn off led
					stateLedOn = false;
					// reset servos
					resetAll();
					
				} 

				// save current status of the pedal
				lastKnownPedalPosition = pedalInHighPosition;
				
				/*
				 //TODO what is this?
				//PWM Range below is 0.0. to 1.5.  Cycle through each servo channel.
				for (int c=0; c<16; c++) {
					for (float p = 1.5f; p>0.0; p-=0.5f) {
						Thread.sleep(200);
						setServo(c, p);
						led_.write(ledOn_);
					}
				
					for (float p=0.0f; p<1.5f; p+=0.5f) {
						Thread.sleep(200);
						setServo(c, p);
					}
				}*/
				
			}
			
			/**
			 * Reset all servos to neutral position.
			 * 
			 * @throws ConnectionLostException
			 * @throws InterruptedException
			 */
			public void resetAll() throws ConnectionLostException, InterruptedException {
				stateLedOn = false;
				ServoSettings sett = RoboTarStartPage.this.getServoSettings();
				for (int servo = 0; servo < 12; servo++) {
					setServo(servo, sett.getInitial(servo));
				}
				turnOffFretLEDs();
				LOG.info("Servos in neutral position default");
			}

			private void turnOffFretLEDs() throws ConnectionLostException {
				for (int i = 0; i < 6; i++) {
					for (int j = 0; j < 4; j++) {
						fretLEDs[i][j].write(false);
					}
					fretLEDsTurnedOn[i] = null;
				}
			}
			
			/**
			 * Set Servo channel and milliseconds input to PulseWidth calculation
			 * 
			 * @param servoNum
			 * @param pos
			 * @throws ConnectionLostException
			 * @throws InterruptedException
			 */
			public void setServo(int servoNum, float pos) throws ConnectionLostException, InterruptedException {
				LOG.debug("setServo call: servo: {}, value: {}", servoNum, pos);
				setPulseWidth(servoNum, pos + 1.0f);  //
			}
			
			protected void setPulseWidth(int channel, float ms) throws ConnectionLostException, InterruptedException {
				// Set pulsewidth according to PCA9685 data sheet based on milliseconds value sent from setServo method
				// 4096 steps per cycle, frequency is 50MHz (50 steps per millisecond)
				int pw = Math.round(ms / 1000 * FREQ * 4096);
				// Skip to every 4th address value to turn off the pulse (see datasheet addresses for LED#_OFF_L)
				byte[] request = { (byte) (0x08 + channel * 4), (byte) pw, (byte) (pw >> 8) };
				twi_.writeReadAsync(PCA_ADDRESS, false, request, request.length, null, 0);
			}

			/**
			 * 
			 * @param stringNum 0..5
			 * @param fretNum 1..4
			 * @throws ConnectionLostException
			 */
			public void setLED(int stringNum, int fretNum) throws ConnectionLostException {
				LOG.debug("setLED call: string: {}, fretNum: {}", stringNum, fretNum);
				if (fretNum <= 0) {
					if (fretLEDsTurnedOn[stringNum] != null) {
						// if we know what was last turned on
						fretLEDsTurnedOn[stringNum].write(false);
					} else {
						// turn off all LEDs on this string
						for (int j = 0; j < 4; j++) {
							fretLEDs[stringNum][j].write(false);
						}
					}
					fretLEDsTurnedOn[stringNum] = null;
				} else {
					// turn off last turned on LED on this string
					if (fretLEDsTurnedOn[stringNum] != null) {
						fretLEDsTurnedOn[stringNum].write(false);
					}
					// turn on the one LED on this string
					fretLEDs[stringNum][fretNum-1].write(true);
					fretLEDsTurnedOn[stringNum] = fretLEDs[stringNum][fretNum-1];
				}
			}
			
			@Override
			public void disconnected() {
				LOG.info("IOIO disconnected");
			}

			@Override
			public void incompatible() {
				LOG.info("Incompatible firmware version of IOIO");
			}
		};
	}
}
