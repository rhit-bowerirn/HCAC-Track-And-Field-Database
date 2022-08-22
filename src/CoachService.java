import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * A class that houses all application features accessible to Coaches
 */
public class CoachService extends Service {

	/**
	 * ensures: initializes database connection
	 * 
	 * @param dbService
	 */
	public CoachService(DatabaseConnectionService dbService, boolean importing) {
		super(dbService, importing);
	}

	/**
	 * ensures: calls the AddAthlete SPROC
	 * 
	 * @param firstName the first name of the athlete
	 * @param lastName  the last name of the athlete
	 * @param year      the year of the athlete
	 * @param team      the team of the athlete
	 * @return true if the athlete was added
	 */
	public boolean addAthleteToTeam(String firstName, String lastName, String year, String team, String gender) {
		String statement = "{? = call AddAthlete(?,?,?,?,?)}";
		if(!validateYearString(year)){
			year = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, year, team, gender));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: First Name cannot be null",
				"ERROR: Last Name cannot be null", "ERROR: Invalid Year", "ERROR: Team cannot be null", "ERROR: Invalid Gender",
				"ERROR: Athlete records already exist in the DataBase"));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the FindAthletesByTeam SPROC
	 * 
	 * @param team the team to be searched
	 * @return parsedResults a 2D ArrayList of the ResultSet
	 */
	public ArrayList<ArrayList<String>> findAthletesByTeam(String team) {
		String statement = "{? = call FindAthletes(?, ?, ?, ?, ?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList("", "", "", team, ""));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Provided team cannot be null",
				"ERROR: Provided team must exist within the database"));
		if(team.equals("All")){
			return executeSproc(statement, parameters, errorMessages, 5);
		}else{
			return executeSproc(statement, parameters, errorMessages, 4);
		}
	}

	/**
	 * ensures: calls the FindCoach SPROC
	 * 
	 * @param firstName the first name of the coach
	 * @param lastName  the last name of the coach
	 * @param team      the team of the coach
	 * @return parsedResults a 2D ArrayList of the ResultSet
	 */
	public ArrayList<ArrayList<String>> findCoaches(String firstName, String lastName, String team) {
		String statement = "{? = call FindCoach(?, ?, ?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, team));
		ArrayList<String> errorMessages = new ArrayList<String>();
		return executeSproc(statement, parameters, errorMessages, 5);
	}

	/**
	 * ensures: calls the UpdateAthlete SPROC
	 * 
	 * @param firstName the first name of the athlete
	 * @param lastName  the last name of the athlete
	 * @param year      the year of the athlete
	 * @param team      the team of the athlete
	 * @return true if the athlete was updated
	 */
	public boolean updateAthleteOnTeam(String originalFirstName, String firstName, String originalLastName,
									   String lastName, String originalYear, String year, String originalTeamName,
									   String team, String originalGender, String gender) {
		String statement = "{? = call UpdateAthlete(?,?,?,?,?,?,?,?,?,?)}";
		if(!validateYearString(year)){
			year = null;
		}
		if(!validateYearString(originalYear)){
			originalYear = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(originalFirstName, firstName, originalLastName,
				lastName, originalYear, year, originalTeamName, team, originalGender, gender));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: First Name cannot be null",
				"ERROR: Last Name cannot be null", "ERROR: Invalid Year", "ERROR: Team cannot be null", "ERROR: Invalid Gender",
				"ERROR: No records exist for this Athlete"));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the DeleteAthlete SPROC
	 * 
	 * @param firstName the first name of the athlete
	 * @param lastName  the last name of the athlete
	 * @param year      the year of the athlete
	 * @param team      the team of the athlete
	 * @return true if the athlete was deleted
	 */
	public boolean deleteAthleteFromTeam(String firstName, String lastName, String year, String team, String gender) {
		String statement = "{? = call DeleteAthlete(?,?,?,?,?)}";
		if(!validateYearString(year)){
			year = null;
		}
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, year, team, gender));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: First Name cannot be null",
				"ERROR: Last Name cannot be null", "ERROR: Invalid Year", "ERROR: Team cannot be null", "ERROR: Invalid Gender",
				"ERROR: No records exist for this Athlete"));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the DeleteCoach SPROC
	 *
	 * @param firstName the first name of the coach
	 * @param lastName  the last name of the coach
	 * @param team      the team of the coach
	 * @return true if the coach was deleted
	 */
	public boolean deleteCoachFromTeam(String firstName, String lastName, String team) {
		String statement = "{? = call DeleteCoach(?,?,?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, team));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Provided First Name or Last Name cannot be null.",
				"ERROR: Team cannot be null", "ERROR: Provided team name does not exist.",
				"ERROR: Provided person does not exist in database.", "ERROR: Provided ID does not exist in Coach table.",
				"ERROR: Delete coach did not complete successfully.", "ERROR: Delete person did not complete successfully."));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the UpdateCoach SPROC
	 *
	 * @param firstName   the first name of the coach
	 * @param lastName    the last name of the coach
	 * @param team        the team of the coach
	 * @param eventType   the event of the coach
	 * @param isHeadCoach the isHeadCoach bit of the coach
	 * @return true if the athlete was updated
	 */
	public boolean updateCoachOnTeam(String firstName, String lastName, String team, String eventType, Boolean isHeadCoach) {
		String statement = "{? = call UpdateCoach(?,?,?,?,?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, team, eventType, isHeadCoach.toString()));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Provided First Name or Last Name cannot be null.",
				"ERROR: Provided team name does not exist.", "Provided event type does not exist in database,",
				"ERROR: Provided person does not exist in database.", "ERROR: Provided ID does not exist in Coach table."));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the AddCoach SPROC
	 *
	 * @param firstName   the first name of the coach
	 * @param lastName    the last name of the coach
	 * @param team        the team of the coach
	 * @param eventType   the event of the coach
	 * @param isHeadCoach the isHeadCoach bit of the coach
	 * @return true if the athlete was updated
	 */
	public boolean addCoach(String firstName, String lastName, String team, String eventType, String isHeadCoach) {
		String statement = "{? = call AddCoach(?,?,?,?,?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(firstName, lastName, team, eventType, isHeadCoach));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("ERROR: Provided First Name or Last Name cannot be null.",
				"ERROR: Provided team name does not exist.", "Provided event type does not exist in database,",
				"ERROR: Provided person does not exist in database.", "ERROR: Provided ID does not exist in Coach table."));
		return executeSproc(statement, parameters, errorMessages);
	}



	public String getUsersTeam(String username){
		String statement = "{? = call FindCoachesTeam(?)}";
		ArrayList<String> parameter = new ArrayList<>(Collections.singletonList(username));
		ArrayList<String> errorMessages = new ArrayList<>(Arrays.asList("ERROR: Username cannot be null",
				"ERROR: User must exist in database"));
		ArrayList<ArrayList<String>> results = executeSproc(statement, parameter, errorMessages, 1);
		return results.get(0).get(0);
	}


}