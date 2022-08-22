import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

/**
 * A class to handle the visible window of the application
 */
public class ApplicationWindow extends JFrame {
	DatabaseConnectionService dbService;
	JPanel currentPanel;
	JMenu menu;
	JButton backButton;
	JButton logoutButton;
	Stack<JPanel> panelStack = new Stack<>();
	String userType;
	String username;

	/**
	 * ensures: initializes the application window and sets it to the login screen
	 * 
	 * @param dbService
	 */
	public ApplicationWindow(DatabaseConnectionService dbService) {
		// set window specs
		this.setTitle("HCAC Track and Field Database");
		this.setSize(775, 600);

		// important we initialize this before creating any panels that use it
		this.dbService = dbService;

		// initialize the back button
		this.backButton = getBackButton();

		// initialize the logout button
		this.logoutButton = getLogoutButton();

		// initialize the window to the login panel
		this.currentPanel = getLoginPanel();

		// Adds welcome message bar at top of frame
		this.menu = new JMenu("Welcome to the HCAC Track and Field Database!");
		this.menu.setHorizontalAlignment(SwingConstants.RIGHT);

		// Add back button
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(this.menu);
		this.backButton.setVisible(false);

		menuBar.add(this.backButton);
		menuBar.add(this.logoutButton);

		// Adds menu bar at top of frame
		this.add(menuBar, "North");
		this.add(currentPanel, "Center");

		// start
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * ensures: switches to the given panel
	 * 
	 * @param toSwitch     the panel to switch to
	 * @param isBackButton whether or not this was called by the back button
	 */
	public void switchToOtherPanel(JPanel toSwitch, boolean isBackButton, boolean isLogoutButton) {
		// if this wasn't called by the back button, we need to push the current window
		// on stack for the back button
		if (!isBackButton) {
			panelStack.push(this.currentPanel);
		}

		// switch panels
		this.currentPanel.setVisible(false);
		this.currentPanel = toSwitch;
		this.currentPanel.setVisible(true);
		this.add(currentPanel);

		// determine back button visibility
		if (!panelStack.isEmpty()) {
			this.backButton.setVisible(true);
			this.logoutButton.setVisible(false);
		} else {
			this.backButton.setVisible(false);
			if(isLogoutButton){
				this.logoutButton.setVisible(true);
			}
			this.menu.setText("Welcome to the HCAC Track and Field Database!");
		}
	}

	/**
	 * ensures: creates the back button
	 * 
	 * @return backButton the back button
	 */
	public JButton getBackButton() {
		JButton backButton = new JButton("Back");
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!panelStack.isEmpty()) {
					switchToOtherPanel(panelStack.pop(), true, true);
				}
			}
		});
		backButton.setVisible(false);
		return backButton;
	}

	/**
	 * ensures: creates the logout button
	 *
	 * @return logoutButton the logout button
	 */
	public JButton getLogoutButton() {
		JButton logoutButton = new JButton("Logout");
		ApplicationWindow aw = this;
		logoutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logoutButton.setVisible(false);
				switchToOtherPanel(getLoginPanel(), true, false);
			}
		});
		logoutButton.setVisible(false);
		return logoutButton;
	}

	/**
	 * ensures: creates the initial button panel
	 * 
	 * @return initialButtonPanel
	 */
	public JPanel getInitialButtonPanel() {
		return (new InitialButtonPanel(this)).getPanel();
	}

	/**
	 * ensures: creates the login panel
	 * 
	 * @return loginPanel
	 */
	public JPanel getLoginPanel() {
		return (new LoginPanel(this)).getPanel();
	}

	/**
	 * ensures: clears the stack used by the back button
	 */
	public void clearBackButton() {
		this.panelStack.clear();
	}

	/**
	 * ensures: a user's restrictions and allows them to only perform
	 * expected actions
	 */
	public void setUserType(String type) {
		this.userType = type;
	}
	public void setUsername(String username){
		this.username = username;
	}
}
