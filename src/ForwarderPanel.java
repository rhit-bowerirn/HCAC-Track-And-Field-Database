import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

/**
 * A Superclass for all panels that just have forwarder buttons
 */
public abstract class ForwarderPanel {
	protected final ApplicationWindow applicationWindow;
	protected JPanel panel;
	private String title;

	/**
	 * ensures: initializes the forwarder panel
	 * 
	 * @param aw    the application window
	 * @param title the title of the panel
	 */
	public ForwarderPanel(ApplicationWindow aw, String title) {
		this.applicationWindow = aw;
		this.panel = new JPanel();
		this.title = title;

		panel.setAlignmentX(250);
		panel.setAlignmentY(200);
		panel.setLayout(new FlowLayout());

		// Adds all the components to the panel
		this.populateWindow();
	}

	/**
	 * ensures: adds all the components to the panel
	 */
	public abstract void populateWindow();

	/**
	 * ensures: creates a forwarder button to this panel
	 * 
	 * @return forwarder
	 */
	public JButton getForwarderButton() {
		JButton forwarder = new JButton(title);
		forwarder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// switch to this panel when clicked
				applicationWindow.switchToOtherPanel(panel, false, false);
				applicationWindow.menu.setText(title);
			}
		});

		return forwarder;
	}

	/**
	 * ensures: returns this panel
	 * 
	 * @return panel
	 */
	public JPanel getPanel() {
		return this.panel;
	}

	/**
	 * ensures: creates a Scrollable Table component from a 2D ArrayList of data
	 * 
	 * @param columnNames the names of the table columns
	 * @param records     the 2D ArrayList of data
	 * @return dataTable a Scrollable Table component created from the data
	 */
	protected JScrollPane createTableFromRecords(ArrayList<String> columnNames, ArrayList<ArrayList<String>> records) {
		DefaultTableModel dataTable = new DefaultTableModel();
		dataTable.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				e.getColumn();
			}
		});

		// We have to add data by columns so we traverse the 2D array as such
		for (int c = 0; c < columnNames.size(); c++) {
			// addColumn takes an array, so we initialize it with all the data from the
			// current column
			String[] column = new String[records.size()];
			for (int r = 0; r < records.size(); r++) {
				column[r] = records.get(r).get(c);
			}

			// add the column
			dataTable.addColumn(columnNames.get(c), column);
		}
		JTable table = new JTable(dataTable);
		JScrollPane scrollableTable = new JScrollPane(table);

		return scrollableTable;
	}

	/**
	 * ensures: completely resets the panel to remove the Table for replacement
	 * 
	 * @param panel           the panel to reset
	 * @param textFieldLabels the labels of the the text fields in the panel
	 * @param textFields      the text fields in the panel
	 */
	protected void resetLeafPanel(JPanel panel, ArrayList<String> textFieldLabels, ArrayList<JTextField> textFields) {
		// wipe the panel
		textFields.clear();
		panel.removeAll();
		panel.setAlignmentY(250);
		FlowLayout layout = new FlowLayout();
		layout.setVgap(20);
		layout.setHgap(10);
		panel.setLayout(layout);

		// add back the text fields and labels
		for (String name : textFieldLabels) {
			JTextField textField;
			if(name.equals("Password")){
				textField = new JPasswordField();
			}else{
				textField = new JTextField();
			}
			textField.setPreferredSize(new Dimension(100, 30));
			JLabel label = new JLabel(name);
			panel.add(label);
			panel.add(textField);
			textFields.add(textField);
		}
	}

	/**
	 * ensures: clears all given text fields
	 * 
	 * @param textFields an ArrayList of textfields to clear
	 */
	protected void clearTextFields(ArrayList<JTextField> textFields) {
		for (JTextField textField : textFields) {
			textField.setText("");
		}
	}

	protected DefaultTableModel getTableModel(ArrayList<String> columnNames, ArrayList<ArrayList<String>> records,
											  int startOffset, int offset){
		DefaultTableModel dataTable = new DefaultTableModel();

		// We have to add data by columns so we traverse the 2D array as such
		for (int c = 0; c < columnNames.size(); c++) {
			// addColumn takes an array, so we initialize it with all the data from the
			// current column
			String[] column = new String[records.size()];
			for (int r = 0; r < records.size(); r++) {
				if(startOffset <= c){
					column[r] = records.get(r).get(c + offset);
				}else{
					column[r] = records.get(r).get(c);
				}
			}

			// add the column
			dataTable.addColumn(columnNames.get(c), column);
		}
		return dataTable;
	}

	protected JScrollPane createTableFromTableModel(DefaultTableModel tableModel){
		JTable table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return new JScrollPane(table);
	}
}
