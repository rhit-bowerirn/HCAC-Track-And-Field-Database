import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JOptionPane;

/**
 * A class to house register and login features
 */
public class UserService extends Service {
	private static final Random RANDOM = new SecureRandom();
	private static final Base64.Encoder enc = Base64.getEncoder();
	private static final Base64.Decoder dec = Base64.getDecoder();

	/**
	 * ensures: initializes the service
	 * 
	 * @param dbService
	 */
	public UserService(DatabaseConnectionService dbService, boolean importing) {
		super(dbService, importing);
	}

	/**
	 * ensures: Logs into the application
	 * 
	 * @param username
	 * @param password
	 * @return true if the login was successful
	 */
	public boolean login(String username, String password) {
		// Query to get the password salt and hash for the entered username
		try (PreparedStatement stmt = dbService.getConnection()
				.prepareStatement("SELECT PasswordSalt, PasswordHash FROM [User] WHERE Username = ?")) {
			stmt.setString(1, username);

			// get the password salt and hash from the query
			ResultSet rs = stmt.executeQuery();
			rs.next();
			String salt = rs.getString("PasswordSalt");
			String userHash = rs.getString("PasswordHash");

			// decode the salt from the salt stored in the database
			byte[] userSalt = dec.decode(salt);

			// find the hash of the password entered with the same salt stored in the
			// database
			String enteredHash = hashPassword(userSalt, password);

			// check if the hash of the entered password is the same as the one stored for
			// the entered username
			if (!enteredHash.equals(userHash)) {
				JOptionPane.showMessageDialog(null, "Login Failed");
				return false;
			}
			return true;
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Login Failed");
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * ensures: calls the RegisterAthlete SPROC
	 * 
	 * @param email     the email of the new Athlete account
	 * @param username  the username of the new Athlete account
	 * @param password  the password of the new Athlete account
	 * @param firstName the first name of the athlete
	 * @param lastName  the last name of the athlete
	 * @param year      the year of the athlete
	 * @param team      the team of the athlete
	 * @return true if the account was registered
	 */
	public boolean registerAthlete(String email, String username, String password, String firstName, String lastName,
			String year, String team, String gender) {
		byte[] passwordSalt = getNewSalt();
		String passwordHash = hashPassword(passwordSalt, password);
		if(!validateYearString(year)){
			year = null;
		}
		String statement = "{? = call RegisterAthlete(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
		ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(email, username,
				getStringFromBytes(passwordSalt), passwordHash, firstName, lastName, year, team, gender, "Athlete"));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("Registration Failed"));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: calls the RegisterAthlete SPROC
	 * 
	 * @param email       the email of the new Coach account
	 * @param username    the username of the new Coach account
	 * @param password    the password of the new Coach account
	 * @param firstName   the first name of the coach
	 * @param lastName    the last name of the coach
	 * @param team        the team of the coach
	 * @param eventType   the event type of the coach
	 * @param isHeadCoach whether or not the coach is a head coach
	 * @return true if the account was registered
	 */
	public boolean registerCoach(String email, String username, String password, String firstName, String lastName,
			String team, String eventType, boolean isHeadCoach) {
		byte[] passwordSalt = getNewSalt();
		String passwordHash = hashPassword(passwordSalt, password);
		String statement = "{? = call RegisterCoach(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
		ArrayList<String> parameters = new ArrayList<String>(
				Arrays.asList(email, username, getStringFromBytes(passwordSalt), passwordHash, firstName, lastName,
						team, eventType, Boolean.toString(isHeadCoach), "Coach"));
		ArrayList<String> errorMessages = new ArrayList<String>(Arrays.asList("Registration Failed"));
		return executeSproc(statement, parameters, errorMessages);
	}

	/**
	 * ensures: gets a new salt
	 * 
	 * @return salt
	 */
	public byte[] getNewSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return salt;
	}

	/**
	 * ensures: encodes a given salt
	 * 
	 * @param data the salt
	 * @return the encoded salt
	 */
	public String getStringFromBytes(byte[] data) {
		return enc.encodeToString(data);
	}

	/**
	 * ensures: hashes the given password with the given salt
	 * 
	 * @param salt     the salt to hash the password with
	 * @param password the password to hash
	 * @return hash the password hash
	 */
	public String hashPassword(byte[] salt, String password) {

		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		SecretKeyFactory f;
		byte[] hash = null;
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hash = f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException e) {
			JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
			e.printStackTrace();
		}
		return getStringFromBytes(hash);
	}

	public String findUserType(String username){
		String statement = "{? = call FindUserType(?)}";
		ArrayList<String> parameter = new ArrayList<>(Arrays.asList(username));
		ArrayList<String> errorMessages = new ArrayList<>();
		ArrayList<ArrayList<String>> results = executeSproc(statement, parameter, errorMessages, 1);
		return results.get(0).get(0);

	}

	public boolean validateYearString(String year) {
		boolean isYear = false;

		try{
			int yearVal = Integer.parseInt(year);
			if(yearVal < 1){
				return isYear;
			}else{
				isYear = true;
			}
		}catch(NumberFormatException e){
			return false;
		}
		return isYear;
	}
}
