import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;

/**
 * A class to house features related to editing Meets
 */
public class EditMeetPanel extends ForwarderPanel {
	private AdminService aService;

	/**
	 * initializes the panel
	 * 
	 * @param aw the application window
	 */
	public EditMeetPanel(ApplicationWindow aw) {
		super(aw, "Edit Meets");
		this.aService = new AdminService(aw.dbService, false);
	}

	/**
	 * ensures: adds all components to the panel
	 */
	public void populateWindow() {
		this.panel.add(getEditMeetButton());
		this.panel.add(getAddAthleteResultsButton());
		this.panel.add(getDeleteAthleteResultsButton());
	}

	/**
	 * ensures: creates a forwarder button to the EditMeet screen
	 * 
	 * @return button the forwarder button
	 */
	public JButton getEditMeetButton() {

			JButton button = new JButton("Edit Meets");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					applicationWindow.switchToOtherPanel(createEditMeetPanel(), false, false);
					applicationWindow.menu.setText("Edit Meets");
				}
			});
			return button;
	}

	/**
	 * ensures: creates a forwarder button to the AddAthleteResults screen
	 *
	 * @return button the forwarder button
	 */
	public JButton getAddAthleteResultsButton() {

		JButton button = new JButton("Add Athlete Results");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applicationWindow.switchToOtherPanel(createAddAthleteResultsPanel(), false, false);
				applicationWindow.menu.setText("Add Athlete Results to Meet");
			}
		});
		return button;
	}

	/**
	 * ensures: creates a forwarder button to the DeleteAthleteResults screen
	 *
	 * @return button the forwarder button
	 */
	public JButton getDeleteAthleteResultsButton() {

		JButton button = new JButton("Delete Athlete Results");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applicationWindow.switchToOtherPanel(createDeleteAthleteResultsPanel(), false, false);
				applicationWindow.menu.setText("Delete Athlete Results from Meet");
			}
		});
		return button;
	}

	/**
	 * ensures: creates the EditMeet panel
	 * 
	 * @return panel
	 */
	protected JPanel createEditMeetPanel() {
		JPanel panel = new JPanel();
		ArrayList<String> textFieldLabels = new ArrayList<String>(
				Arrays.asList("Meet Name: ", "   Meet Date (MM/DD/YYYY): ", "   Meet Location: "));
		ArrayList<JTextField> textFields = new ArrayList<JTextField>();
		resetLeafPanel(panel, textFieldLabels, textFields);

		JButton addMeetButton = new JButton("Add Meet");
		addMeetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String meetName = textFields.get(0).getText();
				String meetDate = textFields.get(1).getText();
				String meetLocation = textFields.get(2).getText();
				if (aService.addMeetData(meetName, meetDate, meetLocation)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Meet has been added");
					clearTextFields(textFields);
				}
			}
		});

		panel.add(addMeetButton);

		JButton deleteMeetButton = new JButton("Delete Meet");
		deleteMeetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String meetName = textFields.get(0).getText();
				String meetDate = textFields.get(1).getText();
				String meetLocation = textFields.get(2).getText();
				if (aService.deleteMeetData(meetName, meetDate, meetLocation)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Meet has been deleted");
					clearTextFields(textFields);
				}
			}
		});

		panel.add(deleteMeetButton);

		JPanel panel2 = new JPanel();
		ArrayList<String> textFieldLabels2 = new ArrayList<String>(
				Arrays.asList("Meet Name: ", "Meet Date (MM/DD/YYYY): ", "Event Name: "));
		ArrayList<JTextField> textFields2 = new ArrayList<JTextField>();
		resetLeafPanel(panel2, textFieldLabels2, textFields2);

		JButton addOffersButton = new JButton("Add Event to Meet");
		addOffersButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String meetName = textFields2.get(0).getText();
				String meetDate = textFields2.get(1).getText();
				String eventName = textFields2.get(2).getText();
				if (aService.addMeetOffers(meetName, meetDate, eventName)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Event has been added to meet");
					clearTextFields(textFields2);
				}
			}
		});

		JButton deleteOffersButton = new JButton("Delete Event from Meet");
		deleteOffersButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String meetName = textFields2.get(0).getText();
				String meetDate = textFields2.get(1).getText();
				String eventName = textFields2.get(2).getText();
				if (aService.deleteMeetOffers(meetName, meetDate, eventName)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Event has been deleted from meet");
					clearTextFields(textFields2);
				}
			}
		});
		panel.add(Box.createVerticalStrut(20));
		panel.add(panel2);
		panel.add(addOffersButton);
		panel.add(deleteOffersButton);
		panel.add(getAddCompetesPanel());
		panel.add(getAddEventPanel());
		return panel;
	}

	public JPanel getAddCompetesPanel() {
		JPanel panel = new JPanel();
		ArrayList<String> textFieldLabels2 = new ArrayList<String>(
				Arrays.asList("Meet Name: ", "Team Name: "));
		ArrayList<JTextField> textFields2 = new ArrayList<JTextField>();
		resetLeafPanel(panel, textFieldLabels2, textFields2);

		JButton addCompetesButton = new JButton("Add a Competing Team");
		addCompetesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String meetName = textFields2.get(0).getText();
				String teamName = textFields2.get(1).getText();
				if (aService.addCompetes(meetName, teamName)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Team has been added to meet");
					clearTextFields(textFields2);
				}
			}
		});

		panel.add(addCompetesButton);
		return panel;
	}

	public JPanel getAddEventPanel() {
		JPanel panel = new JPanel();
		ArrayList<String> textFieldLabels2 = new ArrayList<String>(
				Arrays.asList("Event Name: ", "Event Type: "));
		ArrayList<JTextField> textFields2 = new ArrayList<JTextField>();
		resetLeafPanel(panel, textFieldLabels2, textFields2);

		JButton addEventsButton = new JButton("Add an Event");
		addEventsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String eventName = textFields2.get(0).getText();
				String eventType = textFields2.get(1).getText();
				if (aService.addEvent(eventName, eventType)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Event has been added to database");
					clearTextFields(textFields2);
				}
			}
		});

		panel.add(addEventsButton);
		return panel;
	}

	/**
	 * ensures: creates the AddAthleteResults panel
	 *
	 * @return panel
	 */
	protected JPanel createAddAthleteResultsPanel() {
		JPanel panel = new JPanel();
		ArrayList<String> textFieldLabels = new ArrayList<String>(
				Arrays.asList("Athlete First Name: ", "Athlete Last Name: ", "Athlete Year In School: ",
						"Athlete Gender:  ", "Athlete Team: ", "Meet Name: ", "Meet Date (MM/DD/YYYY): ", "Event Name: ",
						"Score: "));
		ArrayList<JTextField> textFields = new ArrayList<JTextField>();
		resetLeafPanel(panel, textFieldLabels, textFields);

		JButton addResultsButton = new JButton("Add Results");
		addResultsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String fName = textFields.get(0).getText();
				String lName = textFields.get(1).getText();
				String year = textFields.get(2).getText();
				String gender = textFields.get(3).getText().equals("M") ? Boolean.toString(true) : Boolean.toString(false);
				String teamName = textFields.get(4).getText();
				String meetName = textFields.get(5).getText();
				String meetDate = textFields.get(6).getText();
				String eventName = textFields.get(7).getText();
				String score = textFields.get(8).getText();
				if (aService.addResultsData(fName, lName, year, gender, teamName, meetName, meetDate, eventName, score)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Results have been added");
					clearTextFields(textFields);
				}
			}
		});

		panel.add(addResultsButton);

		return panel;
	}

	/**
	 * ensures: creates the AddAthleteResults panel
	 *
	 * @return panel
	 */
	protected JPanel createDeleteAthleteResultsPanel() {
		JPanel panel = new JPanel();
		ArrayList<String> textFieldLabels = new ArrayList<String>(
				Arrays.asList("Athlete First Name: ", "Athlete Last Name: ", "Athlete Year In School: ",
						"Athlete Gender: ", "Athlete Team: ", "Meet Name: ", "Meet Date (MM/DD/YYYY): ", "Event Name: "));
		ArrayList<JTextField> textFields = new ArrayList<JTextField>();
		resetLeafPanel(panel, textFieldLabels, textFields);

		JButton deleteResultsButton = new JButton("Delete Results");
		deleteResultsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String fName = textFields.get(0).getText();
				String lName = textFields.get(1).getText();
				String year = textFields.get(2).getText();
				String gender = textFields.get(3).getText().equals("M") ? Boolean.toString(true) : Boolean.toString(false);
				String teamName = textFields.get(4).getText();
				String meetName = textFields.get(5).getText();
				String meetDate = textFields.get(6).getText();
				String eventName = textFields.get(7).getText();
				if (aService.deleteResultsData(fName, lName, year, gender, teamName, meetName, meetDate, eventName)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Results have been deleted");
					clearTextFields(textFields);
				}
			}
		});

		panel.add(deleteResultsButton);

		return panel;
	}

}
