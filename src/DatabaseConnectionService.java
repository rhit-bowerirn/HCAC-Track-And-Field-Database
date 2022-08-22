import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A class to handle connecting to the database
 */
public class DatabaseConnectionService {
	private final String SampleURL = "jdbc:sqlserver://${dbServer};databaseName=${dbName};user=${user};password={${pass}}";
	private Connection connection = null;
	private String databaseName;
	private String serverName;

	/**
	 * ensures: initializes the database server and name
	 * 
	 * @param serverName   the name of the server
	 * @param databaseName the name of the database
	 */
	public DatabaseConnectionService(String serverName, String databaseName) {
		this.serverName = serverName;
		this.databaseName = databaseName;
	}

	/**
	 * ensures: connects to the database
	 * 
	 * @param user the account username
	 * @param pass the account password
	 * @return true if the connection was made
	 * @throws SQLException
	 */
	public boolean connect(String user, String pass) throws SQLException {
		String urlFormat = SampleURL;
		String realUrl = urlFormat.replace("${dbServer}", this.serverName).replace("${dbName}", this.databaseName)
				.replace("${user}", user).replace("${pass}", pass);
		this.connection = DriverManager.getConnection(realUrl);
		return true;
	}

	/**
	 * ensures: returns the database connection
	 * 
	 * @return connection
	 */
	public Connection getConnection() {
		return this.connection;
	}

	/**
	 * ensures: closes the database connection
	 */
	public void closeConnection() {
		try {
			if (this.connection != null && !this.connection.isClosed()) {
				this.connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
