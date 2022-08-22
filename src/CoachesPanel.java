import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class CoachesPanel extends ForwarderPanel{

    CoachService cService;

    /**
     * ensures: initializes the panel
     *
     * @param aw the application window
     */
    public CoachesPanel(ApplicationWindow aw) {
        super(aw, "Coaches");
        this.cService = new CoachService(aw.dbService, false);
    }

    @Override
    public void populateWindow() {
        this.panel.add(getFindCoachesButton());
        this.panel.add(getEditCoachesButton());
    }



    /**
     * ensures: creates a forwarder button to the FindCoaches screen
     *
     * @return button the forwarder button
     */
    public JButton getFindCoachesButton() {
        JButton button = new JButton("All Coaches");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // switches to the FindCoaches panel
                applicationWindow.switchToOtherPanel(createFindCoaches(), false, false);
                applicationWindow.menu.setText("Find Coaches");
            }
        });

        return button;
    }

    /**
     * ensures: creates the FindCoaches panel
     *
     * @return panel
     */
    protected JPanel createFindCoaches() {
        JPanel panel = new JPanel();
        ArrayList<ArrayList<String>> records = this.cService.findCoaches("", "", "All");
        ArrayList<String> columnNames = new ArrayList<String>(
                Arrays.asList("First Name", "Last Name", "School", "Is Head Coach", "Events"));
        panel.add(createTableFromRecords(columnNames, records));
        panel.updateUI();
        return panel;
    }

    private JButton getEditCoachesButton() {
        JButton button = new JButton();
        if(applicationWindow.userType.equals("Admin")){
            button.setText("Edit All Coaches");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switches to the FindAthleteByTeamPanel
                    applicationWindow.switchToOtherPanel(createAdminEditCoachesPanel(), false, false);
                    applicationWindow.menu.setText("Edit All Coaches");
                }
            });
        }else{
            button.setText("Edit Coaches on Team");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // switches to the FindAthleteByTeamPanel
                    applicationWindow.switchToOtherPanel(createCoachEditCoachesPanel(), false, false);
                    applicationWindow.menu.setText("Edit Coaches On Team");
                }
            });
        }


        return button;
    }

    private JPanel createCoachEditCoachesPanel() {
        JPanel panel = new JPanel();
        ArrayList<String> textFieldLabels = new ArrayList<String>(
                Arrays.asList("First Name: ", "  Last Name: ", "Event Type: "));
        ArrayList<JTextField> textFields = new ArrayList<JTextField>();
        JCheckBox headCoachCheckbox = new JCheckBox("Are They a Head Coach?");
        resetLeafPanel(panel, textFieldLabels, textFields);
        headCoachCheckbox.setSelected(false);
        panel.add(headCoachCheckbox);

        String usersTeam = this.cService.getUsersTeam(applicationWindow.username);
        System.out.println(usersTeam);
        final JTable[] coachTable = {getUpdatedCoachesTable(textFields, usersTeam)};
        JScrollPane coachPane = new JScrollPane(coachTable[0]);

        JButton deleteButton = new JButton("Delete selected coach");
        // Adds final add button for add athlete
        JButton addButton = new JButton("Add coach");
        panel.add(addButton);
        panel.add(deleteButton);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String firstName = textFields.get(0).getText();
                String lastName = textFields.get(1).getText();
                String isHeadCoach = Boolean.toString(headCoachCheckbox.isSelected());
                String eventType = textFields.get(2).getText();

                //Call the SPROC
                if (cService.addCoach(firstName, lastName, usersTeam, eventType, isHeadCoach)) {
                    JOptionPane.showMessageDialog(null, "SUCCESS: Coach has been added");
                    clearTextFields(textFields);
                }
                resetLeafPanel(panel, textFieldLabels, textFields);
                headCoachCheckbox.setSelected(false);
                panel.add(headCoachCheckbox);
                panel.add(addButton);
                panel.add(deleteButton);
                coachTable[0] = getUpdatedCoachesTable(textFields, usersTeam);
                JScrollPane coachPane = new JScrollPane(coachTable[0]);
                panel.add(coachPane);
                panel.updateUI();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Gets the row to be deleted
                int selectedRow = coachTable[0].getSelectedRow();
                if(selectedRow != -1){
                    //Call the SPROC
                    if (cService.deleteCoachFromTeam((String) coachTable[0].getValueAt(selectedRow, 0),
                            (String) coachTable[0].getValueAt(selectedRow, 1),
                            usersTeam)) {
                        JOptionPane.showMessageDialog(null, "SUCCESS: Coach has been Deleted");
                        clearTextFields(textFields);
                    }
                    resetLeafPanel(panel, textFieldLabels, textFields);
                    headCoachCheckbox.setSelected(false);
                    panel.add(headCoachCheckbox);
                    panel.add(addButton);
                    panel.add(deleteButton);
                    coachTable[0] = getUpdatedCoachesTable(textFields, usersTeam);
                    JScrollPane coachPane = new JScrollPane(coachTable[0]);
                    panel.add(coachPane);
                    panel.updateUI();
                }
            }
        });

        panel.add(coachPane);
        return panel;


    }

    private JPanel createAdminEditCoachesPanel() {
        JPanel panel = new JPanel();
        ArrayList<String> textFieldLabels = new ArrayList<String>(
                    Arrays.asList("First Name:  ", "  Last Name:  ", "Team Name: ", "Event Type: "));
        ArrayList<JTextField> textFields = new ArrayList<JTextField>();
        JCheckBox headCoachCheckbox = new JCheckBox("Are They a Head Coach?");
        resetLeafPanel(panel, textFieldLabels, textFields);
        headCoachCheckbox.setSelected(false);
        panel.add(headCoachCheckbox);

        String usersTeam = "All";

        final JTable[] coachTable = {getUpdatedCoachesTable(textFields, usersTeam)};
        JScrollPane coachPane = new JScrollPane(coachTable[0]);

        JButton deleteButton = new JButton("Delete selected coach");
        // Adds final add button for add athlete
        JButton addButton = new JButton("Add coach");
        panel.add(addButton);
        panel.add(deleteButton);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String firstName = textFields.get(0).getText();
                String lastName = textFields.get(1).getText();
                String teamName = textFields.get(2).getText();
                String isHeadCoach = Boolean.toString(headCoachCheckbox.isSelected());
                String eventType = textFields.get(3).getText();

                //Call the SPROC
                if (cService.addCoach(firstName, lastName, teamName, eventType, isHeadCoach)) {
                    JOptionPane.showMessageDialog(null, "SUCCESS: Coach has been added");
                    clearTextFields(textFields);
                }
                resetLeafPanel(panel, textFieldLabels, textFields);
                headCoachCheckbox.setSelected(false);
                panel.add(headCoachCheckbox);
                panel.add(addButton);
                panel.add(deleteButton);
                coachTable[0] = getUpdatedCoachesTable(textFields, usersTeam);
                JScrollPane coachPane = new JScrollPane(coachTable[0]);
                panel.add(coachPane);
                panel.updateUI();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Gets the row to be deleted
                int selectedRow = coachTable[0].getSelectedRow();
                if(selectedRow != -1){
                    //Call the SPROC
                    if (cService.deleteCoachFromTeam((String) coachTable[0].getValueAt(selectedRow, 0),
                            (String) coachTable[0].getValueAt(selectedRow, 1),
                            (String) coachTable[0].getValueAt(selectedRow, 2))) {
                        JOptionPane.showMessageDialog(null, "SUCCESS: Coach has been Deleted");
                        clearTextFields(textFields);
                    }
                    resetLeafPanel(panel, textFieldLabels, textFields);
                    headCoachCheckbox.setSelected(false);
                    panel.add(headCoachCheckbox);
                    panel.add(addButton);
                    panel.add(deleteButton);
                    coachTable[0] = getUpdatedCoachesTable(textFields, usersTeam);
                    JScrollPane coachPane = new JScrollPane(coachTable[0]);
                    panel.add(coachPane);
                    panel.updateUI();
                }
            }
        });

        panel.add(coachPane);
        return panel;
    }

    /*
     * ensures: gets an updated athlete table from insert/delete/update
	 *
     * @return panel
	 */
    public JTable getUpdatedCoachesTable(ArrayList<JTextField> textFields, String usersTeam){
        System.out.println(usersTeam);
        ArrayList<ArrayList<String>> results = cService.findCoaches(null, null, usersTeam);
        ArrayList<String> columnNames;
        DefaultTableModel tableModel;
        if(usersTeam.equals("All")){
            columnNames = new ArrayList<String>(
                    Arrays.asList("First Name", "Last Name", "Team Name", "Is Head Coach", "Events"));
            tableModel = getTableModel(columnNames, results, 0, 0);
        }else {
            columnNames = new ArrayList<String>(
                    Arrays.asList("First Name", "Last Name", "Is Head Coach", "Events"));
            tableModel = getTableModel(columnNames, results, 2, 1);
        }



        //Tracks table changes to call update from changed data
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int affectedRow = e.getFirstRow();

                DefaultTableModel model = (DefaultTableModel) e.getSource();
                Vector rowData = (Vector)model.getDataVector().elementAt(affectedRow);

                if (e.getType() == TableModelEvent.UPDATE) {
                    boolean isHeadCoach = rowData.get(3).equals("1");

                    if(usersTeam.equals("All")){
                        if (cService.updateCoachOnTeam(results.get(affectedRow).get(0), results.get(affectedRow).get(1),
                                (String) rowData.get(2), (String) rowData.get(4), isHeadCoach)) {
                            JOptionPane.showMessageDialog(null, "SUCCESS: Coach has been updated");
                            clearTextFields(textFields);
                        }
                    }else{

                        if (cService.updateCoachOnTeam(results.get(affectedRow).get(0), results.get(affectedRow).get(1),
                                usersTeam, (String) rowData.get(3), isHeadCoach)) {
                            JOptionPane.showMessageDialog(null, "SUCCESS: Coach has been updated");
                            clearTextFields(textFields);
                        }
                    }

                }
            }
        });
        return new JTable(tableModel);
    }
}
