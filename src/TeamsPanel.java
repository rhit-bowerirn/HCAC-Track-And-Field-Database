import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class TeamsPanel extends ForwarderPanel{
    AdminService aService;
    CoachService cService;
    GeneralService gService;

    public TeamsPanel(ApplicationWindow aw){
        super(aw, "Teams");
        this.cService = new CoachService(aw.dbService, false);
        this.aService = new AdminService(aw.dbService, false);
        this.gService = new GeneralService(aw.dbService, false);
    }

    @Override
    public void populateWindow() {
        panel.add(getListTeamRankingsMenButton());
        panel.add(getListTeamRankingsWomenButton());
        if(!applicationWindow.userType.equals("Athlete")){
            panel.add(getEditTeamsButton());
        }

    }

    public JButton getEditTeamsButton(){
        JButton button;
        if(applicationWindow.userType.equals("Admin")){
            button = new JButton("Edit All Teams");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applicationWindow.switchToOtherPanel(createAdminEditTeamsPanel(), false, false);
                    applicationWindow.menu.setText("Edit All Teams");
                }
            });
        }else{
            button = new JButton("Edit Your Team");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applicationWindow.switchToOtherPanel(createCoachEditTeamsPanel(), false, false);
                    applicationWindow.menu.setText("Edit Your Team");
                }
            });
        }


        return button;
    }

    private JPanel createCoachEditTeamsPanel() {
        JPanel panel = new JPanel();

        String usersTeam = this.cService.getUsersTeam(applicationWindow.username);

        final JTable[] teamTable = {getUpdatedTeamTable(usersTeam)};
        JScrollPane teamPane = new JScrollPane(teamTable[0]);

        panel.add(teamPane);
        return panel;

    }

    private JPanel createAdminEditTeamsPanel() {
        JPanel panel = new JPanel();
        ArrayList<String> textFieldLabels = new ArrayList<String>(
                Arrays.asList("Team Name: ", "Location:  "));
        ArrayList<JTextField> textFields = new ArrayList<JTextField>();
        resetLeafPanel(panel, textFieldLabels, textFields);

        String usersTeam = "All";

        final JTable[] teamTable = {getUpdatedTeamTable(usersTeam)};
        JScrollPane teamPane = new JScrollPane(teamTable[0]);

        JButton deleteButton = new JButton("Delete Selected Team");
        // Adds final add button for add athlete
        JButton addButton = new JButton("Add Team");
        panel.add(addButton);
        panel.add(deleteButton);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String teamName = textFields.get(0).getText();
                String location = textFields.get(1).getText();

                //Call the SPROC
                if (aService.addTeam(teamName, location)) {
                    JOptionPane.showMessageDialog(null, "SUCCESS: Team has been added");
                    clearTextFields(textFields);
                }
                resetLeafPanel(panel, textFieldLabels, textFields);
                panel.add(addButton);
                panel.add(deleteButton);
                teamTable[0] = getUpdatedTeamTable(usersTeam);
                JScrollPane teamPane = new JScrollPane(teamTable[0]);
                panel.add(teamPane);
                panel.updateUI();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Gets the row to be deleted
                int selectedRow = teamTable[0].getSelectedRow();
                if(selectedRow != -1){
                    //Call the SPROC
                    if (aService.deleteTeam((String) teamTable[0].getValueAt(selectedRow, 0))) {
                        JOptionPane.showMessageDialog(null, "SUCCESS: Team has been Deleted");
                        clearTextFields(textFields);
                    }
                    resetLeafPanel(panel, textFieldLabels, textFields);
                    panel.add(addButton);
                    panel.add(deleteButton);
                    teamTable[0] = getUpdatedTeamTable(usersTeam);
                    JScrollPane coachPane = new JScrollPane(teamTable[0]);
                    panel.add(coachPane);
                    panel.updateUI();
                }
            }
        });

        panel.add(teamPane);
        return panel;

    }

    private JTable getUpdatedTeamTable(String usersTeam) {
        ArrayList<ArrayList<String>> results = aService.findTeam(usersTeam, null);
        ArrayList<String> columnNames = new ArrayList<String>(
                    Arrays.asList("Team Name", "Location", "Men's Points", "Women's' Points"));

        DefaultTableModel tableModel = getTableModel(columnNames, results, 0, 1);


        //Tracks table changes to call update from changed data
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int affectedRow = e.getFirstRow();

                DefaultTableModel model = (DefaultTableModel) e.getSource();
                Vector rowData = (Vector)model.getDataVector().elementAt(affectedRow);

                if (e.getType() == TableModelEvent.UPDATE) {
                    boolean validUpdate = true;
                    for(int i = 2; i <= 3; i++){
                        if(!results.get(affectedRow).get(i + 1).equals((String) rowData.get(i))){
                            JOptionPane.showMessageDialog(null, "ERROR: Points cannot be updated");
                            validUpdate = false;
                        }
                    }
                    if(validUpdate){
                        if (aService.updateTeam(results.get(affectedRow).get(1), results.get(affectedRow).get(2),
                                (String) rowData.get(0), (String) rowData.get(1))) {
                            JOptionPane.showMessageDialog(null, "SUCCESS: Team has been updated");
                        }
                    }
                }
            }
        });
        return new JTable(tableModel);
    }


    private JButton getListTeamRankingsMenButton() {
        JButton button = new JButton("Mens Rankings");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applicationWindow.switchToOtherPanel(createListTeamRankingsPanel(true), false, false);
                applicationWindow.menu.setText("Mens Team Rankings");
            }
        });
        return button;
    }

    private JButton getListTeamRankingsWomenButton() {
        JButton button = new JButton("Womens Rankings");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applicationWindow.switchToOtherPanel(createListTeamRankingsPanel(false), false, false);
                applicationWindow.menu.setText("Womens Team Rankings");
            }
        });
        return button;
    }

    protected JPanel createListTeamRankingsPanel(boolean isMale) {
        JPanel panel = new JPanel();
        String gender;
        if(isMale) {
            gender = "1";
        }
        else {
            gender = "0";
        }
        ArrayList<ArrayList<String>> records = gService.listTeamRankings(gender);
        ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList("Ranking", "Team Name", "Points"));
        panel.add(createTableFromRecords(columnNames, records));
        panel.updateUI();

        return panel;
    }
}
