package ioio.robotar.pcconsole;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.versarius.xchords.ChordLibrary;

/**
 * Per user preferences
 *
 */
public class RoboTarPreferences {
	private static final Logger LOG = LoggerFactory.getLogger(RoboTarPreferences.class);
	
	/** where the corrections file is - correction values for servos */
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
	
	/** recent chord libraries */
	private List<String> libraries = new ArrayList<String>();
	
	/** last chosen library */
	private String chosenLibrary;
	
	// TODO add other preferences, recent files (chords, songs)...
	
	/**
	 * Loads and saves preferences.
	 * @param p
	 */
	protected RoboTarPreferences(Preferences p) {
		correctionsFile = p.get("correctionsFile", "corrections.xml");
		mainSize = p.getInt("mainSize", 12);
		markedSize = p.getInt("markedSize", 16);
		markedColor = decodeColor(p, "markedColor", "0x0000ff");
		markedChordSize = p.getInt("markedChordSize", 16);
		markedChordColor = decodeColor(p, "markedChordColor", "0x0000ff");
		// recent chord files - generally only 1 is visible in chords page
		chosenLibrary = p.get("chosenLibrary", ChordManager.DEFAULT_ROBOTAR); //?
		int i = 2;
		String fileName = p.get("chordLibraryFile1", null);
		while (fileName != null) {
			libraries.add(fileName);
			fileName = p.get("chordLibraryFile" + Integer.toString(i, 10), null);
			i++;
		}
		// after load, save them again (useful in first run, but not needed)
		update(p);
		flush(p);
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
		p.put("correctionsFile", correctionsFile);
		p.put("mainSize", Integer.toString(getMainSize(), 10));
		p.put("markedColor", encodeColor(getMarkedColor()));
		p.put("markedSize", Integer.toString(getMarkedSize(), 10));
		p.put("markedChordColor", encodeColor(getMarkedChordColor()));
		p.put("markedChordSize", Integer.toString(getMarkedChordSize(), 10));
		// recent chord files - generally only 1 is visible in chords page
		p.put("chosenLibrary", chosenLibrary);
		int i = 1;
		for (String lib : libraries) {
			p.put("chordLibraryFile" + Integer.toString(i, 10), lib);
			i++;
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

}
