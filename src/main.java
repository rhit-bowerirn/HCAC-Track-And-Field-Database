import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.Scanner;

public class main {
	boolean isUsing = false;
	static String serverUsername = null;
	static String serverPassword = null;
	static DatabaseConnectionService dbService = new DatabaseConnectionService("[Redacted]",
			"[Database server was wiped]");

	public static void main(String[] args) {
		serverUsername = "[Account deleted]";
		serverPassword = "[Account deleted]";
		
		try {
			dbService.connect(serverUsername, serverPassword);
			//importData();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} 
//		catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}
		
		JFrame window = new ApplicationWindow(dbService);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				dbService.closeConnection();
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});
	}

	public static void importData() throws FileNotFoundException {
		String menData = "src/External/HCAC Men Data.csv";
		String womenData = "src/External/HCAC Women Data.csv";
		String meetData = "src/External/HCAC Meet Data.csv";
		String coachData = "src/External/HCAC Coach Data.csv";
		String teamData = "src/External/HCAC Team Data.csv";
		String eventData = "src/External/HCAC Event Data.csv";

		CoachService cs = new CoachService(dbService, true);
		AdminService as = new AdminService(dbService, true);
		FileReader file = null;
		Scanner s;

		//Import Event Data
		try {
			file = new FileReader(eventData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		s = new Scanner(file);
		while (s.hasNextLine()) {
			String line = s.nextLine();
			String[] info = line.split(",");
			String eventName = info[0];
			String eventType = info[1];
			as.addEvent(eventName, eventType);
		}
		s.close();
		System.out.println("Imported Event Data");

		//Import Meet Data
		try {
			file = new FileReader(meetData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		s = new Scanner(file);
		while (s.hasNextLine()) {
			String line = s.nextLine();
			String[] info = line.split(",");
			String meetName = info[0];
			String meetDate = info[1];
			String meetLocation = info[2];
			as.addMeetData(meetName, meetDate, meetLocation);
		}
		s.close();
		System.out.println("Imported Meet Data");

		//Import Team Data
		try {
			file = new FileReader(teamData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		s = new Scanner(file);
		while (s.hasNextLine()) {
			String line = s.nextLine();
			String[] info = line.split(",");
			String teamName = info[0];
			String teamLocation = info[1];
			as.addTeam(teamName, teamLocation);
		}
		s.close();
		System.out.println("Imported Team Data");

		//Import Coach Data
		try {
			file = new FileReader(coachData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		s = new Scanner(file);
		while (s.hasNextLine()) {
			String line = s.nextLine();
			String[] info = line.split(",");
			String team = info[0];
			String last = info[1];
			String first = info[2];
			String isHeadCoach = info[3];
			String eventType = info[4];
			cs.addCoach(first, last, team, eventType, Boolean.toString(Boolean.parseBoolean(isHeadCoach)));
		}
		s.close();
		System.out.println("Imported Coach Data");

		//Import Men Data
		try {
			file = new FileReader(menData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		s = new Scanner(file);
		while (s.hasNextLine()) {
			String gender = Boolean.toString(true);
			String line = s.nextLine();
			String[] info = line.split(",");
			String team = info[0];
			String last = info[1];
			String first = info[2];
			String year = info[3];
			String score = info[4];
			String meetName = info[5];
			String meetDate = info[6];
			String eventName = info[7];
			//String eventType = info[8];
			cs.addAthleteToTeam(first, last, year, team, gender);
			as.addMeetOffers(meetName, meetDate, eventName);
			as.addCompetes(meetName, team);

			as.addResultsData(first, last, year, gender, team, meetName, meetDate, eventName, score);
		}
		s.close();
		System.out.println("Imported Mens Data");

		//Import Women Data
		try {
			file = new FileReader(womenData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		s = new Scanner(file);
		while (s.hasNextLine()) {
			String gender = Boolean.toString(false);
			String line = s.nextLine();
			String[] info = line.split(",");
			String team = info[0];
			String last = info[1];
			String first = info[2];
			String year = info[3];
			String score = info[4];
			String meetName = info[5];
			String meetDate = info[6];
			String eventName = info[7];
			// String eventType = info[8];
			cs.addAthleteToTeam(first, last, year, team, gender);
			as.addMeetOffers(meetName, meetDate, eventName);
			as.addCompetes(meetName, team);
			as.addResultsData(first, last, year, gender, team, meetName, meetDate, eventName, score);
		}
		s.close();
		System.out.println("Imported Womens Data");
	}

}
