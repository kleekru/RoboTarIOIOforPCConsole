package com.robotar.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.internal.ws.api.pipe.NextAction;

import cz.versarius.xchords.ChordManager;

/**
 * Per user preferences
 *
 */
public class RoboTarPreferences {
	
	private static final Logger LOG = LoggerFactory.getLogger(RoboTarPreferences.class);
	
	/** constants for pedal mode. */
	public static final int PRESS_AND_HOLD = 0;
	public static final int PRESS_AND_RELEASE = 1;
	
	/** constants for after timeout action. */
	public static final int PLAY_THE_SAME = 0;
	public static final int MOVE_TO_NEXT = 1;
	
	/** where the corrections file is - correction values for servos. Relative to current working folder
	 * or {user.home}/.robotar folder. In this order. */
	private String correctionsFile;
	
	/** Font size of text of song. */
	private int mainSize;
	
	/** Active text line font size, in points. */
	private int markedSize;
	
	/** Active text line color, in 0xRRGGBB format, XX : 00-FF. */
	private Color markedColor;
	
	/** Active chord font size, in points.*/
	private int markedChordSize;
	
	/** Active chord color, in 0xRRGGBB format, XX: 00-FF. */
	private Color markedChordColor;
	
	/** Active chord color when in editing mode, in 0xRRGGBB format, XX : 00-FF. 
	 * Size remains on default mainSize */
	private Color editMarkedChordColor;
	
	/** recent chord libraries */
	private List<String> libraries = new ArrayList<String>();
	
	/** last chosen library */
	private String chosenLibrary;
	
	/** recent song list */
	private List<String> songs = new ArrayList<String>();
	
	/** Max inactivity time (in seconds) */
	private int maxInactivity;
	private static final int MAX_MAX_INACTIVITY = 15;
	
	/** Pedal mode. */
	private int pedalMode = PRESS_AND_HOLD;
	
	/** Meaning of pedal press after inactivity timeout. */
	private int afterTimeout = PLAY_THE_SAME;
	
	/** Check for new version at the startup? */
	private boolean checkNewVersion;
	
	/** Language */
	private Locale locale;
	
	// keys in preferences file 
	private static final String CORRECTIONS_FILE = "correctionsFile";
	private static final String MAIN_SIZE = "mainSize";
	private static final String SONG_FILE = "songFile";
	private static final String CHORD_LIBRARY_FILE = "chordLibraryFile";
	private static final String CHOSEN_LIBRARY = "chosenLibrary";
	private static final String EDIT_MARKED_CHORD_COLOR = "editMarkedChordColor";
	private static final String MARKED_CHORD_COLOR = "markedChordColor";
	private static final String MARKED_CHORD_SIZE = "markedChordSize";
	private static final String MARKED_COLOR = "markedColor";
	private static final String MARKED_SIZE = "markedSize";
	private static final String MAX_INACTIVITY = "maxInactivity";
	private static final String CHECK_NEW_VERSION = "checkNewVersion";
	private static final String LOCALE = "locale";
	private static final String PEDAL_MODE = "pedalMode";
	private static final String PRESS_AFTER_TIMEOUT = "pressAfterTimeout";
	
	/**
	 * Loads and saves preferences.
	 * @param p
	 */
	protected RoboTarPreferences(Preferences p) {
		correctionsFile = p.get(CORRECTIONS_FILE, "corrections.xml");
		checkNewVersion = p.getBoolean(CHECK_NEW_VERSION, true);
		locale = decodeLocale(p, LOCALE, "en");
		mainSize = p.getInt(MAIN_SIZE, 12);
		markedSize = p.getInt(MARKED_SIZE, 18);
		markedColor = decodeColor(p, MARKED_COLOR, "0x0000ff");
		markedChordSize = p.getInt(MARKED_CHORD_SIZE, 16);
		markedChordColor = decodeColor(p, MARKED_CHORD_COLOR, "0x0000ff");
		editMarkedChordColor = decodeColor(p, EDIT_MARKED_CHORD_COLOR, "0x00ff00");
		// max inactivity - check against the physical limit of 180sec
		maxInactivity = p.getInt(MAX_INACTIVITY, MAX_MAX_INACTIVITY);
		if (maxInactivity <= 0 || maxInactivity > MAX_MAX_INACTIVITY) {
			maxInactivity = MAX_MAX_INACTIVITY;
		}
		// pedal mode
		pedalMode = p.getInt(PEDAL_MODE, PRESS_AND_HOLD);
		if (pedalMode < 0 || pedalMode > PRESS_AND_RELEASE) {
			pedalMode = PRESS_AND_HOLD;
		}
		// press after inactivity timeout
		afterTimeout = p.getInt(PRESS_AFTER_TIMEOUT, MOVE_TO_NEXT);
		if (afterTimeout < 0 || afterTimeout > MOVE_TO_NEXT) {
			afterTimeout = MOVE_TO_NEXT;
		}
		// recent chord files - generally only 1 is visible in chords page
		chosenLibrary = p.get(CHOSEN_LIBRARY, ChordManager.DEFAULT_ROBOTAR); //?
		int i = 2;
		String fileName = p.get(CHORD_LIBRARY_FILE + "1", null);
		while (fileName != null) {
			libraries.add(fileName);
			fileName = p.get(CHORD_LIBRARY_FILE + Integer.toString(i, 10), null);
			i++;
		}
		// recent songs list
		int si = 2;
		String songFileName = p.get(SONG_FILE + "1", null);
		while (songFileName != null) {
			songs.add(songFileName);
			songFileName = p.get(SONG_FILE + Integer.toString(si, 10), null);
			si++;
		}
		// after load, save them again (useful in first run, but not needed)
		update(p);
		flush(p);
	}
	
	private Locale decodeLocale(Preferences p, String name, String defValue) {
		String []arr = p.get(name, defValue).split("_");
		if (arr.length < 2) {
			return Locale.ENGLISH;
		}
		// what if wrong codes?
		return new Locale(arr[0], arr[1]);
	}
	
	private String encodeLocale(Locale locale) {
		return locale.getLanguage() + "_" + locale.getCountry();
	}
	
	private Color decodeColor(Preferences p, String name, String defValue) {
		String si = p.get(name, defValue);
		int i = Integer.decode(si);
		return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
	}
	
	private String encodeColor(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		StringBuilder sb = new StringBuilder(10);
		sb.append("0x");
		sb.append(encode2chars(r));
		sb.append(encode2chars(g));
		sb.append(encode2chars(b));
		return sb.toString();
	}
	
	private String encode2chars(int d) {
		String str = Integer.toString(d, 16);
		if (str.length() < 2) {
			return "0" + str;
		}
		return str;
	}

	public static RoboTarPreferences load() {
		Preferences up = Preferences.userRoot();
		Preferences p = up.node(RoboTarPreferences.class.getName());
		
		return new RoboTarPreferences(p);
	}
	
	public void save() {
		Preferences up = Preferences.userRoot();
		Preferences p = up.node(this.getClass().getName());
		update(p);
		flush(p);
	}

	private void update(Preferences p) {
		p.put(CORRECTIONS_FILE, correctionsFile);
		p.putBoolean(CHECK_NEW_VERSION, checkNewVersion);
		p.put(LOCALE, encodeLocale(getLocale()));
		p.put(MAIN_SIZE, Integer.toString(getMainSize(), 10));
		p.put(MARKED_COLOR, encodeColor(getMarkedColor()));
		p.put(MARKED_SIZE, Integer.toString(getMarkedSize(), 10));
		p.put(MARKED_CHORD_COLOR, encodeColor(getMarkedChordColor()));
		p.put(MARKED_CHORD_SIZE, Integer.toString(getMarkedChordSize(), 10));
		p.put(EDIT_MARKED_CHORD_COLOR, encodeColor(getEditMarkedChordColor()));
		p.put(MAX_INACTIVITY, Integer.toString(getMaxInactivity(), 10));
		p.put(PEDAL_MODE, Integer.toString(getPedalMode(), 10));
		p.put(PRESS_AFTER_TIMEOUT, Integer.toString(getAfterTimeout(), 10));
		
		// recent chord files - generally only 1 is visible in chords page
		p.put(CHOSEN_LIBRARY, chosenLibrary);
		int i = 1;
		for (String lib : libraries) {
			p.put(CHORD_LIBRARY_FILE + Integer.toString(i, 10), lib);
			i++;
		}
		// recent song list
		int si = 1;
		for (String song : songs) {
			p.put(SONG_FILE + Integer.toString(si, 10), song);
			si++;
		}
	}

	private void flush(Preferences p) {
		try {
			p.flush();
		} catch (BackingStoreException e) {
			LOG.error("cannot save preferences", e);
		}
	}

	public String getCorrectionsFile() {
		return correctionsFile;
	}

	public void setCorrectionsFile(String correctionsFile) {
		this.correctionsFile = correctionsFile;
	}

	public int getMainSize() {
		return mainSize;
	}

	public Locale getLocale() {
		return locale;
	}
	
	public Color getMarkedColor() {
		return markedColor;
	}

	public int getMarkedSize() {
		return markedSize;
	}

	public Color getMarkedChordColor() {
		return markedChordColor;
	}

	public int getMarkedChordSize() {
		return markedChordSize;
	}

	public int getMaxInactivity() {
		return maxInactivity;
	}
	
	public List<String> getLibraries() {
		return libraries;
	}

	public void setLibraries(List<String> libraries) {
		this.libraries = libraries;
	}

	public String getChosenLibrary() {
		return chosenLibrary;
	}

	public void setChosenLibrary(String chosenLibrary) {
		this.chosenLibrary = chosenLibrary;
	}

	public List<String> getSongs() {
		return songs;
	}

	public void setSongs(List<String> songs) {
		this.songs = songs;
	}

	public Color getEditMarkedChordColor() {
		return editMarkedChordColor;
	}

	public void setEditMarkedChordColor(Color editMarkedChordColor) {
		this.editMarkedChordColor = editMarkedChordColor;
	}

	public boolean isCheckNewVersion() {
		return checkNewVersion;
	}

	public void setCheckNewVersion(boolean checkNewVersion) {
		this.checkNewVersion = checkNewVersion;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public int getPedalMode() {
		return pedalMode;
	}

	public void setPedalMode(int pedalMode) {
		this.pedalMode = pedalMode;
	}

	public int getAfterTimeout() {
		return afterTimeout;
	}

	public void setAfterTimeout(int afterTimeout) {
		this.afterTimeout = afterTimeout;
	}
	
}
