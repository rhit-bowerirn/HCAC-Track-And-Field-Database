import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that houses all application features accessible to any user
 */
public class GeneralService extends Service {

	/**
	 * ensures: initializes the database connection
	 * 
	 * @param dbService
	 */
	public GeneralService(DatabaseConnectionService dbService, boolean importing) {
		super(dbService, importing);
	}

	/**
	 * ensures: calls the FindMeets SPROC
	 * 
	 * @param meetName     the name of the meet
	 * @param meetDate     the date of the meet
	 * @param meetLocation the location of the meet
	 * @return parsedResults a 2D ArrayList of the ResultSet
	 */
	public ArrayList<ArrayList<String>> findMeets(String meetName, String meetDate, String meetLocation) {
		String statement = "{? = call FindMeets(?, ?, ?)}";
		//Do regex string validation for date format
		if(!validateDateString(meetDate)){
			meetDate = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(meetName, meetDate, meetLocation));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: No meets found named " + meetName,
				"ERROR: No meets found on " + meetDate + " (Please use MM/DD/YYYY format)",
				"ERROR: No meets found at " + meetLocation));
		return executeSproc(statement, parameters, errorMessages, 3);
	}

	/**
	 * ensures: calls the FindCompetingTeams SPROC
	 * 
	 * @param meetName     the name of the meet
	 * @param meetDate     the date of the meet
	 * @param meetLocation the location of the meet
	 * @return parsedResults a 2D ArrayList of the ResultSet
	 */
	public ArrayList<ArrayList<String>> findCompetingTeams(String meetName, String meetDate, String meetLocation) {
		String statement = "{? = call FindCompetingTeams(?, ?, ?)}";
		//Do regex string validation for date format
		if(!validateDateString(meetDate)){
			meetDate = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(meetName, meetDate, meetLocation));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Meet Name cannot be null.",
				"ERROR: Meet Date cannot be null or Invalid Year Format. Use: MM/DD/YYYY",
				"ERROR: Location cannot be null.", "ERROR: No such Meet exists"));
		return executeSproc(statement, parameters, errorMessages, 4);
	}

	/**
	 * ensures: calls the FindEventsOffered SPROC
	 * 
	 * @param meetName     the name of the meet
	 * @param meetDate     the date of the meet
	 * @param meetLocation the location of the meet
	 * @return parsedResults a 2D ArrayList of the ResultSet
	 */
	public ArrayList<ArrayList<String>> findEventsOffered(String meetName, String meetDate, String meetLocation) {
		String statement = "{? = call FindEventsOffered(?, ?, ?)}";
		//Do regex string validation for date format
		if(!validateDateString(meetDate)){
			meetDate = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(meetName, meetDate, meetLocation));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Meet Name cannot be null.",
				"ERROR: Meet Date cannot be null or Invalid Year Format. Use: MM/DD/YYYY", "ERROR: Location cannot be null.", "ERROR: No such Meet exists"));
		return executeSproc(statement, parameters, errorMessages, 2);
	}

	/**
	 * ensures: calls the ListEventResults SPROC
	 * 
	 * @param meetName     the name of the meet
	 * @param meetDate     the date of the meet
	 * @param meetLocation the location of the meet
	 * @param eventName    the name of the Event
	 * @return parsedResults a 2D ArrayList of the ResultSet
	 */
	public ArrayList<ArrayList<String>> listEventResults(String meetName, String meetDate, String meetLocation,
			String eventName) {
		String statement = "{? = call ListEventResults(?, ?, ?, ?)}";
		//Do regex string validation for date format
		if(!validateDateString(meetDate)){
			meetDate = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(
				Arrays.asList(meetName, meetDate, meetLocation, eventName));
		ArrayList<String> errorMessages = new ArrayList<String>(
				Arrays.asList("ERROR: Meet Name cannot be null.", "ERROR: Meet Date cannot be null or Invalid Year " +
								"Format. Use: MM/DD/YYYY", "ERROR: Location cannot be null.", "Event Name cannot be null",
						"ERROR: No such Meet exists", "ERROR: No such Event exists", "ERROR: " + meetName + " does not offer " + eventName));
		return executeSproc(statement, parameters, errorMessages, 7);
	}

	/**
	 * ensures: calls the FindAthletesMeets SPROC
	 * 
	 * @param firstName the first name of the athlete
	 * @param lastName  the last name of the athlete
	 * @param year      the year of the athlete
	 * @param team      the team of the athlete
	 * @return parsedResults a 2D ArrayList of the ResultSet
	 */
	public ArrayList<ArrayList<String>> findAnAthletesMeets(String firstName, String lastName, String year,
			String team, String gender) {
		String statement = "{? = call FindAthletesMeets(?, ?, ?, ?, ?)}";
		if(!validateYearString(year)){
			year = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, year, gender, team));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: First Name cannot be null",
				"ERROR: Last Name cannot be null", "ERROR: Invalid Year", "ERROR: Team cannot be null", "ERROR: Invalid Gender",
				"ERROR: No records exist for this Athlete"));
		return executeSproc(statement, parameters, errorMessages, 6);
	}
	
	public ArrayList<ArrayList<String>> listPBsByEvent(String eventName) {
		String statement = "{? = call ListPBsByEvent(?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(eventName));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Event Name cannot be null",
				"ERROR: No Such Event Exists"));
		return executeSproc(statement, parameters, errorMessages, 6);
	}
	
	public ArrayList<ArrayList<String>> listPBsByAthlete(String firstName, String lastName, String year,
			String team, String gender) {
		String statement = "{? = call ListPBsByAthlete(?, ?, ?, ?, ?)}";
		if(!validateYearString(year)){
			year = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, year, team, gender));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: First Name cannot be null",
				"ERROR: Last Name cannot be null", "ERROR: Invalid Year", "ERROR: Team cannot be null", "ERROR: Invalid Gender",
				"ERROR: No records exist for this Athlete"));
		return executeSproc(statement, parameters, errorMessages, 3);
	}
	
	public ArrayList<ArrayList<String>> listTeamRankings(String gender) {
		String statement = "{? = call ListTeamRankings(?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(gender));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Gender cannot be null"));
		return executeSproc(statement, parameters, errorMessages, 3);
	}

	public boolean validateDateString(String meetDate) {
		//		Do regex string validation for date format
		String dateRegex = "^[0-3]?[0-9]/[0-3]?[0-9]/[0-9]{4}$";
		Pattern pattern = Pattern.compile(dateRegex);
		Matcher matcher = pattern.matcher(meetDate);
		return matcher.matches();
	}



}
