import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * A class to house features related to editing an athlete
 */
public class AthletePanel extends ForwarderPanel {
	private CoachService cService;

	/**
	 * ensures: initializes the panel
	 * 
	 * @param aw the application window
	 */
	public AthletePanel(ApplicationWindow aw) {
		super(aw, "Athletes");
		this.cService = new CoachService(aw.dbService, false);
	}

	/**
	 * ensures: adds all components to the panel
	 */
	public void populateWindow() {
		this.panel.add(getFindAthleteByTeamButton());
		if(!applicationWindow.userType.equals("Athlete")){
			this.panel.add(getEditAthleteButton());
		}
	}

	/**
	 * ensures: creates a forwarder button to the FindAthleteByTeam screen
	 * 
	 * @return button the forwarder button
	 */
	public JButton getFindAthleteByTeamButton() {
		JButton button = new JButton("Search Athletes by Team");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// switches to the FindAthleteByTeamPanel
				applicationWindow.switchToOtherPanel(createFindAthleteByTeamPanel(), false, false);
				applicationWindow.menu.setText("Search Athletes by Team");
			}
		});

		return button;
	}



	/**
	 * ensures: creates a forwarder button to the EditAthlete screen
	 * 
	 * @return button the forwarder button
	 */
	public JButton getEditAthleteButton() {
		JButton editAthleteButton = new JButton();
		if(applicationWindow.userType.equals("Coach")){
			editAthleteButton.setText("Edit Athletes on Team");

			editAthleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// switches to the EditAthlete panel
					applicationWindow.switchToOtherPanel(createCoachEditAthletePanel(), false, false);
					applicationWindow.menu.setText("Edit Athlete");
				}
			});
		}else {
			editAthleteButton = new JButton("Edit All Athletes");

			editAthleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// switches to the EditAthlete panel
					applicationWindow.switchToOtherPanel(createAdminEditAthletePanel(), false, false);
					applicationWindow.menu.setText("Edit All Athlete");
				}
			});
		}

		return editAthleteButton;
	}



	/**
	 * ensures: creates the FindAthleteByTeam panel
	 * 
	 * @return panel
	 */
	protected JPanel createFindAthleteByTeamPanel() {
		JPanel panel = new JPanel();
		ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("Team Name: "));
		ArrayList<JTextField> textFields = new ArrayList<JTextField>();
		resetLeafPanel(panel, textFieldLabels, textFields);

		JButton button = new JButton("Find Athletes");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String team = textFields.get(0).getText();
				ArrayList<ArrayList<String>> records = cService.findAthletesByTeam(team);
				ArrayList<String> columnNames = new ArrayList<String>(
						Arrays.asList("First Name", "Last Name", "Year in School", "Gender"));
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(button);
				panel.add(createTableFromRecords(columnNames, records));
				panel.updateUI();
			}
		});

		panel.add(button);
		return panel;
	}

	/**
	 * ensures: creates the EditAthletePanel for a coach
	 * 
	 * @return panel
	 */
	public JPanel createCoachEditAthletePanel() {
		JPanel panel = new JPanel();
		ArrayList<String> textFieldLabels = new ArrayList<String>(
				Arrays.asList("First Name:  ", "  Last Name:  ", "Gender (M/F):  ", "Year In School: "));
		ArrayList<JTextField> textFields = new ArrayList<JTextField>();
		resetLeafPanel(panel, textFieldLabels, textFields);

		String usersTeam = cService.getUsersTeam(applicationWindow.username);

		final JTable[] athleteTable = {getUpdatedAthleteTable(textFields, usersTeam)};
		JScrollPane athletePane = new JScrollPane(athleteTable[0]);

		JButton deleteButton = new JButton("Delete selected athlete");
		// Adds final add button for add athlete
		JButton addButton = new JButton("Add athlete");
		panel.add(addButton);
		panel.add(deleteButton);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String firstName = textFields.get(0).getText();
				String lastName = textFields.get(1).getText();
				String year = textFields.get(2).getText();
				String gender = textFields.get(3).getText().toLowerCase();

				if (gender.equals("m") || gender.equals("male")) {
					gender = Boolean.toString(true);
				}
				else if (gender.equals("f") || gender.equals("female")) {
					gender = Boolean.toString(false);
				} else gender = "";


				//Call the SPROC
				if (cService.addAthleteToTeam(firstName, lastName, year, usersTeam, gender)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Athlete has been added");
					clearTextFields(textFields);
				}
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(addButton);
				panel.add(deleteButton);
				athleteTable[0] = getUpdatedAthleteTable(textFields, usersTeam);
				JScrollPane athletePane = new JScrollPane(athleteTable[0]);
				panel.add(athletePane);
				panel.updateUI();
			}
		});

		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Gets the row to be deleted
				int selectedRow = athleteTable[0].getSelectedRow();
				if(selectedRow != -1){
					//Call the SPROC
					if (cService.deleteAthleteFromTeam((String) athleteTable[0].getValueAt(selectedRow, 0),
							(String) athleteTable[0].getValueAt(selectedRow, 1),
							(String) athleteTable[0].getValueAt(selectedRow, 2), usersTeam,
							((String) athleteTable[0].getValueAt(selectedRow, 3)).equals("M") ? Boolean.toString(true) : Boolean.toString(false))) {
						JOptionPane.showMessageDialog(null, "SUCCESS: Athlete has been Deleted");
						clearTextFields(textFields);
					}
					resetLeafPanel(panel, textFieldLabels, textFields);
					panel.add(addButton);
					panel.add(deleteButton);
					athleteTable[0] = getUpdatedAthleteTable(textFields, usersTeam);
					JScrollPane athletePane = new JScrollPane(athleteTable[0]);
					panel.add(athletePane);
					panel.updateUI();
				}
			}
		});

		panel.add(athletePane);
		return panel;
	}

	/**
	 * ensures: creates the EditAthletePanel for admins
	 *
	 * @return panel
	 */
	public JPanel createAdminEditAthletePanel() {
		JPanel panel = new JPanel();
		ArrayList<String> textFieldLabels = new ArrayList<String>(
				Arrays.asList("First Name:  ", "  Last Name:  ", "Year In School: ", "Gender (M/F):  ", "Team Name: "));

		ArrayList<JTextField> textFields = new ArrayList<JTextField>();
		resetLeafPanel(panel, textFieldLabels, textFields);

		String usersTeam = "All";
		final JTable[] athleteTable = {getUpdatedAthleteTable(textFields, "All")};
		JScrollPane athletePane = new JScrollPane(athleteTable[0]);

		JButton deleteButton = new JButton("Delete selected athlete");
		// Adds final add button for add athlete
		JButton addButton = new JButton("Add athlete");
		panel.add(addButton);
		panel.add(deleteButton);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String firstName = textFields.get(0).getText();
				String lastName = textFields.get(1).getText();
				String year = textFields.get(2).getText();
				String gender = textFields.get(3).getText().toLowerCase();

				if (gender.equals("m") || gender.equals("male")) {
					gender = Boolean.toString(true);
				}
				else if (gender.equals("f") || gender.equals("female")) {
					gender = Boolean.toString(false);
				} else gender = "";

				String teamName = textFields.get(4).getText();

				//Call the SPROC
				if (cService.addAthleteToTeam(firstName, lastName, year, teamName, gender)) {
					JOptionPane.showMessageDialog(null, "SUCCESS: Athlete has been added");
					clearTextFields(textFields);
				}
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(addButton);
				panel.add(deleteButton);
				athleteTable[0] = getUpdatedAthleteTable(textFields, "All");
				JScrollPane athletePane = new JScrollPane(athleteTable[0]);
				panel.add(athletePane);
				panel.updateUI();
			}
		});

		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Gets the row to be deleted
				int selectedRow = athleteTable[0].getSelectedRow();
				if(selectedRow != -1){
					//Call the SPROC
					//TODO add gender functionality
					if (cService.deleteAthleteFromTeam((String) athleteTable[0].getValueAt(selectedRow, 0),
							(String) athleteTable[0].getValueAt(selectedRow, 1),
							(String) athleteTable[0].getValueAt(selectedRow, 2), (String) athleteTable[0].getValueAt(selectedRow, 4),
							((String) athleteTable[0].getValueAt(selectedRow, 3)).equals("M") ? Boolean.toString(true) : Boolean.toString(false))) {
						JOptionPane.showMessageDialog(null, "SUCCESS: Athlete has been Deleted");
						clearTextFields(textFields);
					}
					resetLeafPanel(panel, textFieldLabels, textFields);
					panel.add(addButton);
					panel.add(deleteButton);
					athleteTable[0] = getUpdatedAthleteTable(textFields, "All");
					JScrollPane athletePane = new JScrollPane(athleteTable[0]);
					panel.add(athletePane);
					panel.updateUI();
				}
			}
		});

		panel.add(athletePane);
		return panel;
	}

	/**
	 * ensures: gets an updated athlete table from insert/delete/update
	 *
	 * @return panel
	 */
	public JTable getUpdatedAthleteTable(ArrayList<JTextField> textFields, String usersTeam){
		ArrayList<ArrayList<String>> results = cService.findAthletesByTeam(usersTeam);
		ArrayList<String> columnNames;
		if(usersTeam.equals("All")){
			columnNames = new ArrayList<String>(
					Arrays.asList("First Name", "Last Name", "Year in School", "Gender", "Team Name"));
		}else {
			columnNames = new ArrayList<String>(
					Arrays.asList("First Name", "Last Name", "Year in School", "Gender"));
		}

		DefaultTableModel tableModel = getTableModel(columnNames, results, 0, 0);


		//Tracks table changes to call update from changed data
		tableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int affectedRow = e.getFirstRow();

				DefaultTableModel model = (DefaultTableModel) e.getSource();
				Vector rowData = (Vector)model.getDataVector().elementAt(affectedRow);

				if (e.getType() == TableModelEvent.UPDATE) {
					if(usersTeam.equals("All")){
						if (cService.updateAthleteOnTeam(results.get(affectedRow).get(0), (String) rowData.get(0),
								results.get(affectedRow).get(1), (String) rowData.get(1), results.get(affectedRow).get(2),
								(String) rowData.get(2),  results.get(affectedRow).get(4), (String) rowData.get(4),
								results.get(affectedRow).get(3).equals("M") ? Boolean.toString(true) : Boolean.toString(false),
								((String) rowData.get(3)).equals("M") ? Boolean.toString(true) : Boolean.toString(false))) {
							JOptionPane.showMessageDialog(null, "SUCCESS: Athlete has been updated");
							clearTextFields(textFields);
						}
					}else {
						if (cService.updateAthleteOnTeam(results.get(affectedRow).get(0), (String) rowData.get(0),
								results.get(affectedRow).get(1), (String) rowData.get(1), results.get(affectedRow).get(2),
								(String) rowData.get(2),  usersTeam, usersTeam,
								results.get(affectedRow).get(3).equals("M") ? Boolean.toString(true) : Boolean.toString(false),
								((String) rowData.get(3)).equals("M") ? Boolean.toString(true) : Boolean.toString(false))) {
							JOptionPane.showMessageDialog(null, "SUCCESS: Athlete has been updated");
							clearTextFields(textFields);
						}
					}
				}
			}
		});
		return new JTable(tableModel);
	}

}
