import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class to house features related to Meets and Events
 */
public class MeetAndEventPanel extends ForwarderPanel {
	private GeneralService gService;
	
	/**
	 * initializes the panel
	 * 
	 * @param aw the application window
	 */
    public MeetAndEventPanel(ApplicationWindow aw){
    	super(aw, "Meets and Events");
    	this.gService = new GeneralService(aw.dbService, false);
    }
    
    /**
	 * ensures: adds all components to the panel
	 */
    public void populateWindow() {
        panel.add(getSearchByAthleteButton());               
        panel.add(getFindMeetsButton());        
        panel.add(getFindCompetingTeamsButton());       
        panel.add(getFindEventsOfferedButton());       
        panel.add(getListEventResultsButton());
        panel.add(getListPBsByEventButton());
        panel.add(getListPBsByAthleteButton());
    }   

    /**
	 * ensures: creates a forwarder button to the SearchByAthlete screen
	 * 
	 * @return button the forwarder button
	 */
    public JButton getSearchByAthleteButton(){
        JButton button = new JButton("Search by Athlete");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	applicationWindow.switchToOtherPanel(createSearchByAthletePanel(), false, false);
		        applicationWindow.menu.setText("Search For An Athlete's Meets");	
            }
        });

        return button;
    }
    
    /**
	 * ensures: creates a forwarder button to the FindMeets screen
	 * 
	 * @return button the forwarder button
	 */
    public JButton getFindMeetsButton() {
    	JButton button = new JButton("Search for Meets");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        applicationWindow.switchToOtherPanel(createFindMeetsPanel(), false, false);
		        applicationWindow.menu.setText("Search For Meets");				
			}});
    	
    	return button;
    }
    
    /**
	 * ensures: creates a forwarder button to the FindCompetingTeams screen
	 * 
	 * @return button the forwarder button
	 */
    public JButton getFindCompetingTeamsButton() {
    	JButton button = new JButton("Search for Teams by Meet");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {	        
		        applicationWindow.switchToOtherPanel(createFindCompetingTeamsPanel(), false, false);
		        applicationWindow.menu.setText("Search For Teams by Meet");				
			}});
    	
    	return button;
    }
    
    /**
	 * ensures: creates a forwarder button to the FindEventsOffered screen
	 * 
	 * @return button the forwarder button
	 */
    public JButton getFindEventsOfferedButton() {
    	JButton button = new JButton("Search Events Offered");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {       
		        applicationWindow.switchToOtherPanel(createFindEventsOfferedPanel(), false, false);
		        applicationWindow.menu.setText("Search For Events By Meet");				
			}});
    	
    	return button;
    }
    
    /**
	 * ensures: creates a forwarder button to the ListEventResults screen
	 * 
	 * @return button the forwarder button
	 */
    public JButton getListEventResultsButton() {
    	JButton button = new JButton("Search for Event Results");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {		        
		        applicationWindow.switchToOtherPanel(createListEventResultsPanel(), false, false);
		        applicationWindow.menu.setText("Search for Event Results");				
			}});
    	
    	return button;
    }
    
    public JButton getListPBsByEventButton() {
    	JButton button = new JButton("Best Times by Event");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {		        
		        applicationWindow.switchToOtherPanel(createListPBsByEventPanel(), false, false);
		        applicationWindow.menu.setText("Search for PBs by Event");				
			}});
    	
    	return button;
    }
    
    public JButton getListPBsByAthleteButton() {
    	JButton button = new JButton("Best Times by Athlete");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {		        
		        applicationWindow.switchToOtherPanel(createListPBsByAthletePanel(), false, false);
		        applicationWindow.menu.setText("Search for PBs by Athlete");				
			}});
    	
    	return button;
    }
    
    /**
	 * ensures: creates the SearchByAthlete panel
	 * 
	 * @return panel
	 */
    public JPanel createSearchByAthletePanel() {
    	JPanel panel = new JPanel();
    	ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("First Name: ", "Last Name: ", "Year In School: ", "Team Name: ", "Gender (M/F): "));
    	ArrayList<JTextField> textFields = new ArrayList<JTextField>();
    	resetLeafPanel(panel, textFieldLabels, textFields);
    	
    	JButton button = new JButton("Find Athlete's Meets");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String firstName = textFields.get(0).getText();
				String lastName = textFields.get(1).getText();
				String year = textFields.get(2).getText();
				String team = textFields.get(3).getText();
				String gender = textFields.get(4).getText().toLowerCase();
				if (gender.equals("m") || gender.equals("male")) {
					gender = Boolean.toString(true);
				}
				else if (gender.equals("f") || gender.equals("female")) {
					gender = Boolean.toString(false);
				} else gender = "";
				ArrayList<ArrayList<String>> records = gService.findAnAthletesMeets(firstName, lastName, year, team, gender);
				ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList("Meet Name", "Meet Date", "Meet Location", "Event Name", "Score", "Points"));
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(button);
				panel.add(createTableFromRecords(columnNames, records));
				panel.updateUI();				
			}});
    	
        panel.add(button);
    	return panel;
    }
    
    /**
	 * ensures: creates the FindMeets panel
	 * 
	 * @return panel
	 */
    public JPanel createFindMeetsPanel() {
    	JPanel panel = new JPanel();
    	ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("Meet Name: ", "Meet Date: ", "Meet Location: "));
    	ArrayList<JTextField> textFields = new ArrayList<JTextField>();
    	resetLeafPanel(panel, textFieldLabels, textFields);
    	
    	JButton button = new JButton("Find Meets");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String meetName = textFields.get(0).getText();
				String meetDate = textFields.get(1).getText();
				String meetLocation = textFields.get(2).getText();
				ArrayList<ArrayList<String>> records = gService.findMeets(meetName, meetDate, meetLocation);
				ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList("Name", "Date", "Location"));
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(button);
				panel.add(createTableFromRecords(columnNames, records));
				panel.updateUI();				
			}});
    	
        panel.add(button);
    	return panel;
    }

    /**
	 * ensures: creates the FindCompetingTeams panel
	 * 
	 * @return panel
	 */
    public JPanel createFindCompetingTeamsPanel() {
    	JPanel panel = new JPanel();
    	ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("Meet Name: ", "Meet Date: ", "Meet Location: "));
    	ArrayList<JTextField> textFields = new ArrayList<JTextField>();
    	resetLeafPanel(panel, textFieldLabels, textFields);
    	
    	JButton button = new JButton("Find Competing Teams");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String meetName = textFields.get(0).getText();
				String meetDate = textFields.get(1).getText();
				String meetLocation = textFields.get(2).getText();
				ArrayList<ArrayList<String>> records = gService.findCompetingTeams(meetName, meetDate, meetLocation);
				ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList("School Name", "Location", "Mens Points", "Womens Points"));
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(button);
				panel.add(createTableFromRecords(columnNames, records));
				panel.updateUI();				
			}});
    	
        panel.add(button);
    	return panel;
    }
    
    /**
	 * ensures: creates the FindEventsOffered panel
	 * 
	 * @return panel
	 */
    public JPanel createFindEventsOfferedPanel() {
        JPanel panel = new JPanel();
    	ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("Meet Name: ", "Meet Date: ", "Meet Location: "));
    	ArrayList<JTextField> textFields = new ArrayList<JTextField>();
    	resetLeafPanel(panel, textFieldLabels, textFields);
    	
    	JButton button = new JButton("Find Events Offered");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String meetName = textFields.get(0).getText();
				String meetDate = textFields.get(1).getText();
				String meetLocation = textFields.get(2).getText();
				ArrayList<ArrayList<String>> records = gService.findEventsOffered(meetName, meetDate, meetLocation);
				ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList("Name", "Type"));
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(button);
				panel.add(createTableFromRecords(columnNames, records));
				panel.updateUI();				
			}});
    	
        panel.add(button);
    	return panel;
    }

    /**
	 * ensures: creates the ListEventResults panel
	 * 
	 * @return panel
	 */
    public JPanel createListEventResultsPanel() {
    	JPanel panel = new JPanel();
    	ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("Meet Name: ", "Meet Date: ", "Meet Location: ", "Event Name: "));
    	ArrayList<JTextField> textFields = new ArrayList<JTextField>();
    	resetLeafPanel(panel, textFieldLabels, textFields);
    	
    	JButton button = new JButton("List Results");
    	button.addActionListener(new ActionListener() {    		
			@Override
			public void actionPerformed(ActionEvent e) {
				String meetName = textFields.get(0).getText();
				String meetDate = textFields.get(1).getText();
				String meetLocation = textFields.get(2).getText();
				String eventName = textFields.get(3).getText();
				ArrayList<ArrayList<String>> records = gService.listEventResults(meetName, meetDate, meetLocation, eventName);
				ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList("First Name", "Last Name", "Year In School", "Gender", "School Name", "Score", "Points"));
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(button);
				panel.add(createTableFromRecords(columnNames, records));
				panel.updateUI();				
			}});
    	
    	panel.add(button);
    	return panel;
    }
    
    public JPanel createListPBsByEventPanel() {
    	JPanel panel = new JPanel();
    	ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("Event Name: "));
    	ArrayList<JTextField> textFields = new ArrayList<JTextField>();
    	resetLeafPanel(panel, textFieldLabels, textFields);
    	
    	JButton button = new JButton("Find PBs");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String eventName = textFields.get(0).getText();
				ArrayList<ArrayList<String>> records = gService.listPBsByEvent(eventName);
				System.out.println(records.toString());
				ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList("First Name", "Last Name", "Year in School", "Gender", "Team Name", "Score"));
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(button);
				panel.add(createTableFromRecords(columnNames, records));
				panel.updateUI();				
			}});
    	
        panel.add(button);
    	return panel;
    }
    
    public JPanel createListPBsByAthletePanel() {
    	JPanel panel = new JPanel();
    	ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("First Name: ", "Last Name: ", "Year In School: ", "Team Name: ", "Gender (M/F): "));
    	ArrayList<JTextField> textFields = new ArrayList<JTextField>();
    	resetLeafPanel(panel, textFieldLabels, textFields);
    	
    	JButton button = new JButton("Find PBs");
    	button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String firstName = textFields.get(0).getText();
				String lastName = textFields.get(1).getText();
				String year = textFields.get(2).getText();
				String team = textFields.get(3).getText();
				String gender = textFields.get(4).getText().toLowerCase();
				if (gender.equals("m") || gender.equals("male")) {
					gender = Boolean.toString(true);
				}
				else if (gender.equals("f") || gender.equals("female")) {
					gender = Boolean.toString(false);
				} else gender = "";
				ArrayList<ArrayList<String>> records = gService.listPBsByAthlete(firstName, lastName, year, team, gender);
				ArrayList<String> columnNames = new ArrayList<String>(Arrays.asList("Event Name", "Event Type", "Score"));
				resetLeafPanel(panel, textFieldLabels, textFields);
				panel.add(button);
				panel.add(createTableFromRecords(columnNames, records));
				panel.updateUI();				
			}});
    	
        panel.add(button);
    	return panel;
    }
}
