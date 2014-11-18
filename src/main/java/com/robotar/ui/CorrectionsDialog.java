package com.robotar.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robotar.ioio.ServoSettings;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class CorrectionsDialog extends JDialog {
	private static final long serialVersionUID = 1091251507083649724L;

	private static final Logger LOG = LoggerFactory.getLogger(CorrectionsDialog.class);
	
	private RoboTarPC page;
	private ResourceBundle messages;
	
	private float VAL = 0.0f;
	private float MIN = -0.2f;
	private float MAX = 0.2f;
	private float STEP = 0.001f;
	
	JSpinner[][] spinners;

	protected boolean copyFirst;
	
	public CorrectionsDialog(final RoboTarPC page) {
		setBackground(Const.BACKGROUND_COLOR);
		setResizable(false);
		setSize(460, 400);
		this.page = page;
		this.messages = page.getMessages();
		setTitle(messages.getString("robotar.corrections.title"));
		
		this.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    	LOG.debug("closing");
		    	CorrectionsDialog.this.page.getServoSettings().setCorrections(getValues());
		    	ServoSettings.saveCorrectionsAs(new File(page.getPreferences().getCorrectionsFile()), page.getServoSettings());
		    }
		});
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {80, 80, 80, 80, 80, 80, 80, 20};
		gridBagLayout.rowHeights = new int[]{23, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);

		// copy value from 1st to other fields
		JCheckBox chboxCopy = new JCheckBox(messages.getString("robotar.corrections.copy_first"));
		chboxCopy.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent ev) {
				copyFirst = (ItemEvent.SELECTED == ev.getStateChange());
			}
		});
		GridBagConstraints gbc_chboxCopy = new GridBagConstraints();
		gbc_chboxCopy.insets = new Insets(0, 0, 5, 5);
		gbc_chboxCopy.gridx = 0;
		gbc_chboxCopy.gridy = 0;
		getContentPane().add(chboxCopy, gbc_chboxCopy);
		chboxCopy.setSelected(true);
		
		// load button
		JButton btnLoadCorrections = new JButton(messages.getString("robotar.corrections.load_from"));
		btnLoadCorrections.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					loadCorrections();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});
		GridBagConstraints gbc_btnLoadButton = new GridBagConstraints();
		gbc_btnLoadButton.gridwidth = 2;
		gbc_btnLoadButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnLoadButton.gridx = 3;
		gbc_btnLoadButton.gridy = 0;
		getContentPane().add(btnLoadCorrections, gbc_btnLoadButton);
		
		// save button
		JButton btnSaveCorrections = new JButton(messages.getString("robotar.corrections.save_as"));
		btnSaveCorrections.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCorrections();
			}
		});
		GridBagConstraints gbc_btnSaveButton = new GridBagConstraints();
		gbc_btnSaveButton.gridwidth = 2;
		gbc_btnSaveButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnSaveButton.gridx = 5;
		gbc_btnSaveButton.gridy = 0;
		getContentPane().add(btnSaveCorrections, gbc_btnSaveButton);

		// horizontal labels
		createLabel(0, 1, messages.getString("robotar.corrections.string"), getContentPane());
		createLabel(1, 1, messages.getString("robotar.corrections.frets"), getContentPane());
		createLabel(2, 1, messages.getString("robotar.corrections.servo"), getContentPane());
		createLabel(3, 1, messages.getString("robotar.corrections.open"), getContentPane());
		createLabel(4, 1, messages.getString("robotar.corrections.mute"), getContentPane());
		createLabel(5, 1, messages.getString("robotar.corrections.left"), getContentPane());
		createLabel(6, 1, messages.getString("robotar.corrections.right"), getContentPane());
		
		// vertical labels
		// labels for strings
		for (int i = 12; i>0; i--) {
			createLabel(0, 12-i+2, ""+(i+1)/2, getContentPane());
		}
		// labels for frets
		for (int i = 0; i<12; i++) {
			createLabel(1, i+2, ((i%2)==0 ? "1-2" : "3-4"), getContentPane());
		}
		// labels for servos
		for (int i = 0; i<12; i++) {
			createLabel(2, i+2, ""+i, getContentPane());
		}
		
		// servo settings
		spinners = new JSpinner[12][];
		for (int i = 0; i<12; i++) {
			spinners[i] = new JSpinner[4];
			for (int j = 0; j<4; j++) {
				JSpinner spinner = new JSpinner();
				SpinnerNumberModel snm = new SpinnerNumberModel(VAL, MIN, MAX, STEP);
				spinner.setModel(snm);
				GridBagConstraints gbc_spinner = new GridBagConstraints();
				gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
				gbc_spinner.insets = new Insets(0, 0, 5, 0);
				gbc_spinner.gridx = j+3;
				gbc_spinner.gridy = i+2;
				installFocusListener(spinner);
				getContentPane().add(spinner, gbc_spinner);
				spinners[i][j] = spinner;
				spinner.setName(String.valueOf(i*4 + j));
				
				// copy first feature
				JComponent comp = spinner.getEditor();
			    JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
			    DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
			    // prepare .123 style (instead of 0.123)
			    if (formatter instanceof NumberFormatter) {
			    	NumberFormat nf = (NumberFormat) ((NumberFormatter)formatter).getFormat();
			    	nf.setMinimumIntegerDigits(0);
			    	nf.setMaximumFractionDigits(3);
			    }
			    formatter.setCommitsOnValidEdit(true);
			    spinner.addChangeListener(new ChangeListener() {

			        @Override
			        public void stateChanged(ChangeEvent e) {
			        	Object curValue = ((JSpinner)e.getSource()).getValue();
			            String name = ((JSpinner)e.getSource()).getName();
			            int x = Integer.valueOf(name);
			            if (x%4 == 0 && copyFirst) {
			            	// set others
			            	spinners[x/4][1].setValue(curValue);
			            	spinners[x/4][2].setValue(curValue);
			            	spinners[x/4][3].setValue(curValue);
			            }
			        }
			    });
			}
		}
		
		// notice to users
		JLabel lblRange = new JLabel(messages.getString("robotar.corrections.values_range"));
		lblRange.setMaximumSize(new Dimension(400, 40));
		GridBagConstraints gbc_lblRange = new GridBagConstraints();
		gbc_lblRange.insets = new Insets(0, 0, 5, 5);
		gbc_lblRange.gridwidth = 4;
		gbc_lblRange.gridx = 3;
		gbc_lblRange.gridy = 15;
		getContentPane().add(lblRange, gbc_lblRange);

		// display values from actual settings
		updateControls(page.getServoSettings().getCorrections());
		
		pack();
		setLocationByPlatform(true);
		setVisible(true);
	}

	private void createLabel(int i, int j, String openStr, Container contentPane) {
		JLabel lblServo = new JLabel(openStr);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(0, 0, 5, 5);
		gbc_lbl.gridx = i;
		gbc_lbl.gridy = j;
		getContentPane().add(lblServo, gbc_lbl);
	}


	protected void loadCorrections() throws FileNotFoundException {
		JFileChooser fc = new JFileChooser();
		int returnValue = fc.showOpenDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            ServoSettings sett = ServoSettings.loadCorrectionsFrom(file, RoboTarPC.ROBOTAR_FOLDER);
            page.setServoSettings(sett);
            updateControls(page.getServoSettings().getCorrections());
		}
	}
	
	protected void updateControls(float[][] v) {
		for (int i=0; i<12; i++) {
			for (int j=0; j<4; j++) {
				spinners[i][j].setValue(checkLimit(v[i][j]));
			}
		}
	}
	
	private float checkLimit(float value) {
		//LOG.debug("checking value: {}", value);
		if (value < MIN) {
			return MIN;
		} else if (value > MAX) {
			return MAX;
		} else {
			return value;
		}
	}
	protected float[][] getValues() {
		float [][] vals = new float[12][];
		for (int i=0; i<12; i++) {
			vals[i] = new float[4];
			for (int j=0; j<4; j++) {
				vals[i][j] = checkLimit(((Number) spinners[i][j].getValue()).floatValue());
			}
		}
		return vals;
	}
	
	protected void saveCorrections() {
		JFileChooser fc = new JFileChooser();
		int returnValue = fc.showSaveDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            page.getServoSettings().setCorrections(getValues());
            ServoSettings.saveCorrectionsAs(file, page.getServoSettings());
		}
		
	}

	// from here to the end of the class, only because of little easy feature:
	// on TAB -> select all in spinner. welcome to Java. 
	// http://stackoverflow.com/questions/15328185/make-jspinner-select-text-when-focused
	public static final SelectOnFocusGainedHandler SHARED_INSTANCE = new SelectOnFocusGainedHandler();

	public void installFocusListener(JSpinner spinner) {

        JComponent spinnerEditor = spinner.getEditor();

        if (spinnerEditor != null) {

            // This is me spending a few days trying to make this work and 
            // eventually throwing a hissy fit and just grabbing all the 
            // JTextComponent components contained within the editor....
            List<JTextComponent> lstChildren = findAllChildren(spinner, JTextComponent.class);
            if (lstChildren != null && lstChildren.size() > 0) {

                JTextComponent editor = lstChildren.get(0);
                editor.addFocusListener(SHARED_INSTANCE);

            }

        }

    }

    public static <T extends Component> List<T> findAllChildren(JComponent component, Class<T> clazz) {

        List<T> lstChildren = new ArrayList<T>(5);
        for (Component comp : component.getComponents()) {

            if (clazz.isInstance(comp)) {

                lstChildren.add((T) comp);

            } else if (comp instanceof JComponent) {

                lstChildren.addAll(findAllChildren((JComponent) comp, clazz));

            }

        }

        return Collections.unmodifiableList(lstChildren);

    }

    public static class SelectOnFocusGainedHandler extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent e) {

            Component comp = e.getComponent();
            if (comp instanceof JTextComponent) {
                final JTextComponent textComponent = (JTextComponent) comp;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(25);
                        } catch (InterruptedException ex) {
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                textComponent.selectAll();
                            }
                        });
                    }
                }).start();
            }            
        }        
    }
}
