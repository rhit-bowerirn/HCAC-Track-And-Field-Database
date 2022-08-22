import java.awt.FlowLayout;

import javax.swing.JPanel;

/**
 * A class to create the initial button panel of the application
 */
public class InitialButtonPanel {
	private AthletePanel athletePanel;
	private MeetAndEventPanel meetAndEventPanel;
	private EditMeetPanel editMeetPanel;
	private CoachesPanel coachesPanel;
	private TeamsPanel teamsPanel;
	private JPanel panel;

	/**
	 * ensures: initializes the button panel
	 * 
	 * @param aw the application window
	 */
	public InitialButtonPanel(ApplicationWindow aw) {
		// create the forwarder panels
		this.athletePanel = new AthletePanel(aw);
		this.meetAndEventPanel = new MeetAndEventPanel(aw);
		this.editMeetPanel = new EditMeetPanel(aw);
		this.coachesPanel = new CoachesPanel(aw);
		this.teamsPanel = new TeamsPanel(aw);

		// initialize the panel
		this.panel = new JPanel();
		this.panel.setAlignmentY(250);
		this.panel.setLayout(new FlowLayout());
		this.panel.add(teamsPanel.getForwarderButton());
		this.panel.add(athletePanel.getForwarderButton());
		if(!aw.userType.equals("Athlete")){
			this.panel.add(coachesPanel.getForwarderButton());

		}
		if(!aw.userType.equals("Athlete") && !aw.userType.equals("Coach")){
			this.panel.add(editMeetPanel.getForwarderButton());
		}
		this.panel.add(meetAndEventPanel.getForwarderButton());
		

	}

	/**
	 * ensures: returns the panel
	 * 
	 * @return panel
	 */
	public JPanel getPanel() {
		return this.panel;
	}
}
