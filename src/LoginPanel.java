import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class to handle registering and logging into the application
 */
public class LoginPanel extends ForwarderPanel {
	private UserService uService;

	/**
	 * initializes the panel
	 * 
	 * @param aw the application window
	 */
	public LoginPanel(ApplicationWindow aw) {
		super(aw, "Login or Register");
		this.uService = new UserService(aw.dbService, false);
	}

	/**
	 * ensures: adds all components to the panel
	 */
	public void populateWindow() {
		this.panel.add(createRegisterButton());
		this.panel.add(createLoginButton());
	}

	/**
	 * ensures: creates a forwarder button to the Login screen
	 * 
	 * @return button the forwarder button
	 */
	public JButton createLoginButton() {
		JButton button = new JButton("Login");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applicationWindow.switchToOtherPanel(createLoginPanel(), false, false);
				applicationWindow.menu.setText("Login");
			}
		});

		return button;
	}

	/**
	 * ensures: creates a forwarder button to the Register screen
	 * 
	 * @return button the forwarder button
	 */
	public JButton createRegisterButton() {
		RegisterPanel rp = new RegisterPanel(this.applicationWindow);
		return rp.getForwarderButton();
	}

	/**
	 * ensures: creates the Login panel
	 * 
	 * @return panel
	 */
	public JPanel createLoginPanel() {
		JPanel panel = new JPanel();
		ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("Username", "Password"));
		ArrayList<JTextField> textFields = new ArrayList<JTextField>();
		resetLeafPanel(panel, textFieldLabels, textFields);

		JButton button = new JButton("Login");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = textFields.get(0).getText();
				String password = textFields.get(1).getText();

				if (!uService.login(username, password)) {
					JOptionPane.showMessageDialog(null, "Login Failed. Please try again");
					clearTextFields(textFields);
				} else {
					// We don't want to be able to log out with the back button
					applicationWindow.clearBackButton();
					applicationWindow.setUserType(uService.findUserType(username));
					applicationWindow.setUsername(username);
					applicationWindow.switchToOtherPanel(applicationWindow.getInitialButtonPanel(), true, true);
				}
			}
		});

		panel.add(button);
		return panel;
	}

	/**
	 * A class to handle Registration
	 */
	private class RegisterPanel extends ForwarderPanel {

		/**
		 * ensures: initializes the panel
		 * 
		 * @param aw the application window
		 */
		public RegisterPanel(ApplicationWindow aw) {
			super(aw, "Register a User");
		}

		/**
		 * ensures: adds all components to the panel
		 */
		public void populateWindow() {
			this.panel.add(getRegisterAthleteButton());
			this.panel.add(getRegisterCoachButton());
		}

		/**
		 * ensures: creates a forwarder button to the Athlete Registration screen
		 * 
		 * @return button the forwarder button
		 */
		public JButton getRegisterAthleteButton() {
			JButton button = new JButton("I'm an Athlete");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					applicationWindow.switchToOtherPanel(createRegisterAthletePanel(), false, false);
					applicationWindow.menu.setText("Athlete Registration");
				}
			});

			return button;
		}

		/**
		 * ensures: creates a forwarder button to the Coach Registration screen
		 * 
		 * @return button the forwarder button
		 */
		public JButton getRegisterCoachButton() {
			JButton button = new JButton("I'm a Coach");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					applicationWindow.switchToOtherPanel(createRegisterCoachPanel(), false, false);
					applicationWindow.menu.setText("Coach Registration");
				}
			});

			return button;
		}

		/**
		 * ensures: creates the RegisterAthlete panel
		 * 
		 * @return panel
		 */
		public JPanel createRegisterAthletePanel() {
			JPanel panel = new JPanel();
			ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("Email", "Username", "Password",
					"First Name", "Last Name", "Team Name", "Year in School", "Gender (M/F)"));
			ArrayList<JTextField> textFields = new ArrayList<JTextField>();
			resetLeafPanel(panel, textFieldLabels, textFields);

			JButton button = new JButton("Register");
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String email = textFields.get(0).getText();
					String username = textFields.get(1).getText();
					String password = textFields.get(2).getText();
					String firstName = textFields.get(3).getText();
					String LastName = textFields.get(4).getText();
					String team = textFields.get(5).getText();
					String year = textFields.get(6).getText();
					String gender = textFields.get(7).getText().toLowerCase();
					if (gender.equals("m") || gender.equals("male")) {
						gender = Boolean.toString(true);
					}
					else if (gender.equals("f") || gender.equals("female")) {
						gender = Boolean.toString(false);
					} else gender = "";
					if (!uService.registerAthlete(email, username, password, firstName, LastName, year, team, gender)) {
						JOptionPane.showMessageDialog(null, "Registration Failed. Please try again");
						clearTextFields(textFields);
					} else {
						// We don't want to be able to log out with the back button
						applicationWindow.clearBackButton();
						applicationWindow.setUserType(uService.findUserType(username));
						applicationWindow.setUsername(username);
						applicationWindow.switchToOtherPanel(applicationWindow.getInitialButtonPanel(), true, true);
					}
				}
			});

			panel.add(button);
			return panel;
		}

		/**
		 * ensures: creates the RegisterCoach panel
		 * 
		 * @return panel
		 */
		public JPanel createRegisterCoachPanel() {
			JPanel panel = new JPanel();
			ArrayList<String> textFieldLabels = new ArrayList<String>(Arrays.asList("Email", "Username", "Password",
					"First Name", "Last Name", "Team Name", "Event Type"));
			ArrayList<JTextField> textFields = new ArrayList<JTextField>();
			JCheckBox headCoachCheckbox = new JCheckBox("Are You a Head Coach?");
			resetLeafPanel(panel, textFieldLabels, textFields);
			headCoachCheckbox.setSelected(false);
			panel.add(headCoachCheckbox);

			JButton button = new JButton("Register");
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String email = textFields.get(0).getText();
					String username = textFields.get(1).getText();
					String password = textFields.get(2).getText();
					String firstName = textFields.get(3).getText();
					String LastName = textFields.get(4).getText();
					String team = textFields.get(5).getText();
					String eventType = textFields.get(6).getText();
					boolean isHeadCoach = headCoachCheckbox.isSelected();
					if (!uService.registerCoach(email, username, password, firstName, LastName, team, eventType,
							isHeadCoach)) {
						JOptionPane.showMessageDialog(null, "Registration Failed. Please try again");
//						clearTextFields(textFields);
//						headCoachCheckbox.setSelected(false);

					} else {
						// We don't want to be able to log out with the back button
						applicationWindow.clearBackButton();
						applicationWindow.setUserType(uService.findUserType(username));
						applicationWindow.setUsername(username);
						applicationWindow.switchToOtherPanel(applicationWindow.getInitialButtonPanel(), true, true);
					}
				}
			});

			panel.add(button);
			return panel;
		}
	}
}
