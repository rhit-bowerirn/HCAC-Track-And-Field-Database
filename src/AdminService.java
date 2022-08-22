import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminService extends Service {

	public AdminService(DatabaseConnectionService dbService, boolean importing) {
		super(dbService, importing);
	}
	
	/**
	 * ensures: calls the AddMeet SPROC
	 *
	 * @param meetName     the name of the meet
	 * @param meetDate     the date of the meet
	 * @param meetLocation the location of the meet
	 * @return true if the meet was added
	 */
	public boolean addMeetData(String meetName, String meetDate, String meetLocation) {
		String statement = "{? = call AddMeet(?,?,?)}";
		//		Do regex string validation for date format
		if(!validateDateString(meetDate)){
			meetDate = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(meetName, meetDate, meetLocation));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Meet Name cannot be null.",
				"ERROR: Invalid Year Format. Use: MM/DD/YYYY", "ERROR: Location cannot be null."));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the AddMeetOffers SPROC
	 *
	 * @param meetName     the name of the meet
	 * @param meetDate     the date of the meet
	 * @param eventName    the name of the added event
	 * @return true if the event was added to the meet
	 */
	public boolean addMeetOffers(String meetName, String meetDate, String eventName) {
		String statement = "{? = call AddMeetOffers(?,?,?)}";

//		//		Do regex string validation for date format
		if(!validateDateString(meetDate)){
			meetDate = null;
		}

		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(meetName, meetDate, eventName));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Meet Name cannot be null.",
				"ERROR: Meet date cannot be null or Invalid date format. Use: MM/DD/YYYY",
				"ERROR: Event name cannot be null.", "ERROR: Meet does not exist in database",
				"ERROR: Event does not exist in database", "ERROR: An error occurred adding event to meet"));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the DeleteMeetOffers SPROC
	 *
	 * @param meetName     the name of the meet
	 * @param meetDate     the date of the meet
	 * @param eventName    the name of the added event
	 * @return true if the event was added to the meet
	 */
	public boolean deleteMeetOffers(String meetName, String meetDate, String eventName) {
		String statement = "{? = call DeleteMeetOffers(?,?,?)}";
		//Do regex string validation for date format
		if(!validateDateString(meetDate)){
			meetDate = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(meetName, meetDate, eventName));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Meet Name cannot be null.",
				"ERROR: Meet date cannot be null or Invalid date format. Use: MM/DD/YYYY",
				"ERROR: Event name cannot be null.", "ERROR: Meet does not exist in database",
				"ERROR: Event does not exist in database", "ERROR: Meet must offer event to delete it.",
				"ERROR: Delete was unsuccessful"));
		return executeSproc(statement, parameters, errorMessages);
	}


	/**
	 * ensures: calls the DeleteMeet SPROC
	 *
	 * @param meetName     the name of the meet
	 * @param meetDate     the date of the meet
	 * @param meetLocation the location of the meet
	 * @return true if the meet was deleted
	 */
	public boolean deleteMeetData(String meetName, String meetDate, String meetLocation) {
		String statement = "{? = call DeleteMeet(?,?,?)}";
		//Do regex string validation for date format
		if(!validateDateString(meetDate)){
			meetDate = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(meetName, meetDate, meetLocation));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Meet Name cannot be null.",
				"ERROR: Meet Date cannot be null or Invalid Year Format. Use: MM/DD/YYYY", "ERROR: Location cannot be null.",
				"ERROR: Meet must exist in database.", "ERROR: Delete was unsuccessful"));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the AddAthleteParticipates SPROC
	 *
	 * @param firstName    the first name of the athlete
	 * @param lastName     the last name of the athlete
	 * @param year         the athlete's year in school
	 * @param teamName     the athlete's team name
	 * @param meetName     the name of the meet the athlete participated in
	 * @param meetDate     the date of the meet
	 * @param eventName    the name of the event
	 * @param score        the athlete's score
	 * @return true if the results were added
	 */
	public boolean addResultsData(String firstName, String lastName, String year, String gender, String teamName,
								  String meetName, String meetDate, String eventName, String score) {
		String statement = "{? = call AddAthleteParticipates(?,?,?,?,?,?,?,?,?)}";
		//Do regex string validation for date format
		if(!validateDateString(meetDate)){
			meetDate = null;
		}
		if(!validateYearString(year)){
			year = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, year, gender, teamName,
				meetName, meetDate, eventName, score));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: First name cannot be null.",
				"ERROR: Last name cannot be null.", "ERROR: Year In School cannot be null or must be a number.", "ERROR: Team name cannot be null.",
				"ERROR: Gender cannot be null.", "ERROR: Meet Name cannot be null.", "ERROR: Meet Date cannot be null or Invalid Year Format. Use: MM/DD/YYYY",
				"ERROR: Event name cannot be null.", "ERROR: Score cannot be null.", "ERROR: Athlete must exist in database.",
				"ERROR: Meet must exist in database.", "ERROR: Event must exist in database.", "ERROR: Meet must offer specified event.",
				"ERROR: Adding results failed"));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the AddAthleteParticipates SPROC
	 *
	 * @param firstName    the first name of the athlete
	 * @param lastName     the last name of the athlete
	 * @param year         the athlete's year in school
	 * @param teamName     the athlete's team name
	 * @param meetName     the name of the meet the athlete participated in
	 * @param meetDate     the date of the meet
	 * @param eventName    the name of the event
	 * @return true if the results were added
	 */
	public boolean deleteResultsData(String firstName, String lastName, String year, String gender, String teamName,
									 String meetName, String meetDate, String eventName) {
		String statement = "{? = call DeleteAthleteParticipates(?,?,?,?,?,?,?,?)}";
		if(!validateYearString(year)){
			year = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, year, gender, teamName,
				meetName, meetDate, eventName));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: First name cannot be null.",
				"ERROR: Last name cannot be null.", "ERROR: Year In School cannot be null or must be a number.", "ERROR: Team name cannot be null.",
				"ERROR: Meet Name cannot be null.", "ERROR: Meet Date cannot be null or Invalid Year Format. Use: MM/DD/YYYY",
				"ERROR: Event name cannot be null.", "ERROR: Athlete must exist in database.", "ERROR: Meet must exist in database.",
				"ERROR: Event must exist in database.", "ERROR: Meet must offer specified event.", "ERROR: Deleting results failed"));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the AddTeam SPROC
	 *
	 * @param teamName     the new team's name
	 * @param location     the new team's location
	 * @return true if the results were added
	 */
	public boolean addTeam(String teamName, String location) {
		String statement = "{? = call AddTeam(?,?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(teamName, location));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Team name cannot be null.",
		"ERROR: Team already exists with given name.", "ERROR: Location cannot be null."));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the DeleteTeam SPROC
	 *
	 * @param teamName     the new team's name
	 * @return true if the results were deleted
	 */
	public boolean deleteTeam(String teamName) {
		String statement = "{? = call DeleteTeam(?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(teamName));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Please provide a valid team name.",
				"ERROR: Team does not exist in the database."));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the UpdateTeam SPROC
	 *
	 * @param originalTeamName  the team's original name
	 * @param teamName          the team's new name
	 * @param originalLocation  the team's original location
	 * @param location          the team's new location
	 * @return true if the results were updated
	 */
	public boolean updateTeam(String originalTeamName, String teamName, String originalLocation, String location) {
		String statement = "{? = call UpdateTeam(?,?,?,?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(originalTeamName, teamName, originalLocation,
				location));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Team does not exist in the database."));
		return executeSproc(statement, parameters, errorMessages);
	}

	/*
	 * ensures: calls the UpdateTeam SPROC
	 *
	 * @param originalTeamName  the team's original name
	 * @param teamName          the team's new name
	 * @param originalLocation  the team's original location
	 * @param location          the team's new location
	 * @return true if the results were updated
	 */
	public ArrayList<ArrayList<String>> findTeam(String teamName, String location) {
		String statement = "{? = call FindTeam(?,?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(teamName, location));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: No teams with that name are found.",
				"ERROR: No teams found with specified home location"));
		return executeSproc(statement, parameters, errorMessages, 5);
	}
	
	public boolean addCompetes(String meetName, String team) {
		String statement = "{? = call AddCompetes(?, ?)}";
		if(meetName.isEmpty()){
			meetName = null;
		}
		if(team.isEmpty()){
			team = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(meetName, team));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Meet Name cannot be null.",
				"ERROR: Team Name cannot be null.", "ERROR: Records already exists in database"));
		return executeSproc(statement, parameters, errorMessages);
	}
	
	public boolean addEvent(String name, String type) {
		String statement = "{? = call AddEvent(?, ?)}";
		if(name.isEmpty()){
			name = null;
		}
		if(type.isEmpty()){
			type = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(name, type));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Event Name cannot be null.",
				"ERROR: Event Type cannot be null."));
		return executeSproc(statement, parameters, errorMessages);
	}

	public boolean validateDateString(String meetDate) {
		//		Do regex string validation for date format
		String dateRegex = "^[0-3]?[0-9]/[0-3]?[0-9]/[0-9]{4}$";
		Pattern pattern = Pattern.compile(dateRegex);
		Matcher matcher = pattern.matcher(meetDate);
		//System.out.println(matcher.matches());
		return matcher.matches();
	}
	
}
