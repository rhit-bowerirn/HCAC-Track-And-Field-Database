import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * A superclass for different Services
 */
public class Service {
	protected DatabaseConnectionService dbService;
	protected boolean importing;
	/**
	 * initialize the Service
	 * 
	 * @param dbService
	 */
	public Service(DatabaseConnectionService dbService, boolean importing) {
		this.dbService = dbService;
		this.importing = importing;
	}

	/**
	 * ensures: executes the given ResultSet SPROC with the given parameters
	 * 
	 * @param statement     the SPROC in String form
	 * @param parameters    the parameters for the SPROC
	 * @param errorMessages the error messages for different return codes of the
	 *                      SPROC
	 * @param numCols       the number of columns in the ResultSet
	 * @return parsedResults a 2D ArrayList of the ResultSet
	 */
	public ArrayList<ArrayList<String>> executeSproc(String statement, ArrayList<String> parameters,
			ArrayList<String> errorMessages, int numCols) {
		ArrayList<ArrayList<String>> parsedResults = new ArrayList<ArrayList<String>>();
		CallableStatement stmt = null;
		try {
			// prepare and execute the SPROC
			stmt = prepareStatement(statement, parameters);
			ResultSet rs = stmt.executeQuery();

			// parse the results
			parsedResults = parseResults(rs, numCols);

			// check the error code in case executeQuery() didn't throw a SQLException
			if (stmt.getInt(1) == 0) {
				System.out.println("Success!");
			} else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			try {
				// handle error messages
				assert stmt != null;
				showErrorMessage(stmt.getInt(1), errorMessages);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		}
		return parsedResults;
	}

	/**
	 * ensures: executes the given result-less SPROC with the given parameters
	 * 
	 * @param statement     the SPROC in String form
	 * @param parameters    the parameters for the SPROC
	 * @param errorMessages the error messages for different return codes of the
	 *                      SPROC
	 * @return true if the SPROC executed successfully
	 */
	public boolean executeSproc(String statement, ArrayList<String> parameters, ArrayList<String> errorMessages) {
		CallableStatement stmt = null;
		try {
			// prepare and execute the SPROC
			stmt = prepareStatement(statement, parameters);
			stmt.execute();

			// handle return codes
			return showErrorMessage(stmt.getInt(1), errorMessages);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				return showErrorMessage(stmt.getInt(1), errorMessages);
			} catch (SQLException ex) {
				e.printStackTrace();
			}
		}
		return false;

	}

	/**
	 * ensures: Prepares a CallableStatement for a SPROC
	 * 
	 * @param statement  the SPROC in String form
	 * @param parameters the parameters to the SPROC
	 * @return stmt the CallableStatement of the SPROC
	 */
	private CallableStatement prepareStatement(String statement, ArrayList<String> parameters) {
		CallableStatement stmt = null;
		;
		try {
			// initialize the statement
			stmt = this.dbService.getConnection().prepareCall(statement);

			// register the return code
			stmt.registerOutParameter(1, Types.INTEGER);

			// register the parameters
			for (int i = 0; i < parameters.size(); i++) {
				stmt.setString(i + 2, parameters.get(i));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return stmt;
	}

	/**
	 * ensures: parses the ResultSet of a SPROC into a 2D ArrayList
	 * 
	 * @param rs      the ResultSet of the SPROC
	 * @param numCols the number of columns in the ResultSet
	 * @return parsedResults a 2D ArrayList of the ResultSet
	 */
	private ArrayList<ArrayList<String>> parseResults(ResultSet rs, int numCols) {
		ArrayList<ArrayList<String>> parsedResults = new ArrayList<ArrayList<String>>();
		try {
			// loop through each row of the ResultSet
			while (rs.next()) {
				ArrayList<String> row = new ArrayList<String>();

				// loop through each column in the current row of the ResultSet and add it to an
				// ArrayList
				for (int i = 1; i < numCols + 1; i++) {
					row.add(rs.getString(i));
				}

				// add the row to the 2D ArrayList
				parsedResults.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return parsedResults;
	}

	/**
	 * ensures: shows the proper error message for the return code of the SPROC
	 * 
	 * @param returnCode    the return code of the SPROC
	 * @param errorMessages the error messages for different return codes of the
	 *                      SPROC
	 * @return true if the SPROC executed successfully
	 */
	private boolean showErrorMessage(int returnCode, ArrayList<String> errorMessages) {
		// if the SPROC executed successfully
		if (returnCode == 0) {
			if (!importing) {
				System.out.println("Success!");
			}
			return true;
			// show the proper error message if the SPROC failed
		} else {
			if (!importing) {
				JOptionPane.showMessageDialog(null, errorMessages.get(returnCode - 1));
			} else System.out.println(errorMessages.get(returnCode - 1));
			return false;
		}
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
