
// Display the results of queries against the bikes table in the bikedb database.
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.Box;

public class DisplayQueryResults extends JFrame {
   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private ResultSetTableModel tableModel;
   private JTextArea queryArea;
   private JTextArea userNameTextArea;
   private JPasswordField passwordTextArea;

   // create ResultSetTableModel and GUI
   public DisplayQueryResults() {
      super("Displaying Query Results");

      // create ResultSetTableModel and display database table
      try {
         // create TableModel for results of query SELECT * FROM bikes
         tableModel = new ResultSetTableModel();

         // set up JTextArea in which user types queries
         // queryArea = new JTextArea( 3, 100);
         queryArea = new JTextArea("", 3, 100);
         queryArea.setWrapStyleWord(true);
         queryArea.setLineWrap(true);

         userNameTextArea = new JTextArea("", 1, 1);
         userNameTextArea.setWrapStyleWord(true);
         userNameTextArea.setLineWrap(true);

         passwordTextArea = new JPasswordField();

         JScrollPane scrollPane = new JScrollPane(queryArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

         Box queryAndHeaderBox = Box.createVerticalBox();
         JLabel queryAreaHeader = new JLabel("Enter An SQL Command");
         queryAreaHeader.setForeground(Color.BLUE);
         queryAndHeaderBox.add(queryAreaHeader);
         queryAndHeaderBox.add(scrollPane);

         // set up JButton for executing commands
         JButton submitButton = new JButton("Execute SQL Command");
         submitButton.setBackground(Color.GREEN);
         submitButton.setForeground(Color.BLACK);
         submitButton.setBorderPainted(false);
         submitButton.setOpaque(true);

         JButton connectToDBButton = new JButton("Connect to Database");
         connectToDBButton.setBackground(Color.BLUE);
         connectToDBButton.setForeground(Color.WHITE);
         connectToDBButton.setBorderPainted(false);
         connectToDBButton.setOpaque(true);

         JButton clearSQLCommandButton = new JButton("Clear SQL Command");
         clearSQLCommandButton.setBackground(Color.WHITE);
         clearSQLCommandButton.setForeground(Color.RED);
         clearSQLCommandButton.setBorderPainted(false);
         clearSQLCommandButton.setOpaque(true);

         JTextArea connectionStatusLabel = new JTextArea("No Connection Now", 1, 1);
         connectionStatusLabel.setBackground(Color.BLACK);
         connectionStatusLabel.setForeground(Color.RED);
         connectionStatusLabel.setLineWrap(true);
         connectionStatusLabel.setWrapStyleWord(true);
         connectionStatusLabel.setEditable(false);

         // create Box to manage placement of queryArea and
         // submitButton in GUI
         Box box = Box.createHorizontalBox();
         box.setBorder(new EmptyBorder(10, 10, 10, 10));
         Box connectionOptionsBox = Box.createVerticalBox();
         JLabel databaseInfoHeader = new JLabel("Enter Database Information");
         databaseInfoHeader.setForeground(Color.BLUE);
         connectionOptionsBox.add(databaseInfoHeader);
         connectionOptionsBox.add(buildDropdownWithLabel("JDBC Driver", getAvailableJDBCDrivers()));
         connectionOptionsBox.add(buildDropdownWithLabel("Database URL", getAvailableJDBCURLs()));
         connectionOptionsBox.add(buildTextAreaWithLabel("Username", userNameTextArea));
         connectionOptionsBox.add(buildPasswordFieldWithLabel("Password", passwordTextArea));
         box.add(connectionOptionsBox);
         box.add(queryAndHeaderBox);

         Box verticalMainBox = Box.createVerticalBox();
         verticalMainBox.setBorder(new EmptyBorder(0, 0, 10, 0));

         // Create buttons bar
         Box buttonBar = Box.createHorizontalBox();
         buttonBar.add(connectionStatusLabel);
         buttonBar.add(Box.createRigidArea(new Dimension(10, 0)));
         buttonBar.add(connectToDBButton);
         buttonBar.add(Box.createRigidArea(new Dimension(10, 0)));
         buttonBar.add(clearSQLCommandButton);
         buttonBar.add(Box.createRigidArea(new Dimension(10, 0)));
         buttonBar.add(submitButton);
         buttonBar.setBorder(new EmptyBorder(10, 10, 10, 10));

         // create JTable delegate for tableModel
         Box resultTableAndHeader = Box.createVerticalBox();
         JTable resultTable = new JTable(tableModel);
         resultTable.setGridColor(Color.BLACK);
         JLabel resultWindowHeader = new JLabel("SQL Execution Result Window");
         resultWindowHeader.setForeground(Color.BLUE);
         resultTableAndHeader.add(resultWindowHeader);

         verticalMainBox.add(box);
         verticalMainBox.add(buttonBar);

         // place GUI components on content pane
         add(verticalMainBox, BorderLayout.NORTH);
         JScrollPane resultScrollPane = new JScrollPane(resultTable);
         resultTableAndHeader.add(resultScrollPane);
         add(resultTableAndHeader, BorderLayout.CENTER);

         JButton clearResultWindowButton = new JButton("Clear Result Window");
         clearResultWindowButton.setBackground(Color.YELLOW);
         clearResultWindowButton.setForeground(Color.BLACK);
         clearResultWindowButton.setBorderPainted(false);
         clearResultWindowButton.setOpaque(true);

         add(clearResultWindowButton, BorderLayout.PAGE_END);

         // create event listener for submitButton
         submitButton.addActionListener(

               new ActionListener() {
                  // pass query to table model
                  public void actionPerformed(ActionEvent event) {
                     // perform a new query
                     try {
                        String queryToExecute = queryArea.getText();

                        if (queryToExecute.startsWith("select") || queryToExecute.startsWith("SELECT")) {
                           tableModel.setQuery(queryToExecute);
                        } else {
                           tableModel.setUpdate(queryToExecute);
                        }

                     } // end try
                     catch (Exception exception) {
                        System.out.println(exception);
                        exception.printStackTrace();
                        JOptionPane.showMessageDialog(null, exception.getMessage(), "Database error",
                              JOptionPane.ERROR_MESSAGE);
                     } // end outer catch
                  } // end actionPerformed
               } // end ActionListener inner class
         ); // end call to addActionListener

         connectToDBButton.addActionListener(new ActionListener() {
            // pass query to table model
            public void actionPerformed(ActionEvent event) {
               // perform a new query
               try {
                  String url = getAvailableJDBCURLs().get(0);
                  String username = userNameTextArea.getText();
                  String password = new String(passwordTextArea.getPassword());

                  tableModel.connectToDatabase(url, username, password);

                  connectionStatusLabel.setText("Connected to " + url);
                  connectToDBButton.setEnabled(false);
                  connectToDBButton.setText("CONNECTED");
               } // end try
               catch (SQLException sqlException) {
                  JOptionPane.showMessageDialog(null, sqlException.getMessage(), "Database error",
                        JOptionPane.ERROR_MESSAGE);
               } // end outer catch
            } // end actionPerformed
         } // end ActionListener inner class
         ); // end call to addActionListener

         clearSQLCommandButton.addActionListener(new ActionListener() {
            // pass query to table model
            public void actionPerformed(ActionEvent event) {
               // perform a new query
               queryArea.setText("");
            } // end actionPerformed
         } // end ActionListener inner class
         ); // end call to addActionListener

         clearResultWindowButton.addActionListener(new ActionListener() {
            // pass query to table model
            public void actionPerformed(ActionEvent event) {
               tableModel.clearTable();
            } // end actionPerformed
         } // end ActionListener inner class
         );

         setSize(1000, 500); // set window size
         setVisible(true); // display window
      } catch (Exception sqlException) {
         JOptionPane.showMessageDialog(null, sqlException.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);

         // ensure database connection is closed
         tableModel.disconnectFromDatabase();

         System.exit(1); // terminate application
      } // end catch

      // dispose of window when user quits application (this overrides
      // the default of HIDE_ON_CLOSE)
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      // ensure database connection is closed when user quits application
      addWindowListener(new WindowAdapter() {
         // disconnect from database and exit when window has closed
         public void windowClosed(WindowEvent event) {
            tableModel.disconnectFromDatabase();
            System.exit(0);
         } // end method windowClosed

      } // end WindowAdapter inner class
      ); // end call to addWindowListener
   } // end DisplayQueryResults constructor

   public static Box buildDropdownWithLabel(String label, List<String> choices) {
      Box box = Box.createHorizontalBox();
      String[] choicesArray = choices.toArray(new String[choices.size()]);
      JComboBox<String> cb = new JComboBox<String>(choicesArray);
      cb.setVisible(true);
      box.add(new JLabel(label));
      box.add(cb);

      return box;
   }

   public static List<String> getAvailableJDBCDrivers() {
      List<String> availableDrivers = new ArrayList<>();

      availableDrivers.add("com.mysql.cj.jdbc.Driver");

      return availableDrivers;
   }

   public static List<String> getAvailableJDBCURLs() {
      List<String> availableURLs = new ArrayList<>();

      availableURLs.add("jdbc:mysql://localhost:3306/project3?useTimezone=true&serverTimezone=UTC");

      return availableURLs;
   }

   public static Box buildTextAreaWithLabel(String label, JTextArea textArea) {
      Box box = Box.createHorizontalBox();
      JLabel jlabel = new JLabel(label);
      jlabel.setBorder(new EmptyBorder(0, 0, 0, 5));
      textArea.setBorder(new EtchedBorder());

      box.add(jlabel);
      box.add(textArea);
      box.setBorder(new EmptyBorder(5, 0, 0, 5));

      return box;
   }

   public static Box buildPasswordFieldWithLabel(String label, JPasswordField passwordField) {
      Box box = Box.createHorizontalBox();
      JLabel jlabel = new JLabel(label);
      jlabel.setBorder(new EmptyBorder(0, 0, 0, 5));
      passwordField.setBorder(new EtchedBorder());

      box.add(jlabel);
      box.add(passwordField);
      box.setBorder(new EmptyBorder(5, 0, 0, 5));

      return box;
   }

   // execute application
   public static void main(String args[]) {
      new DisplayQueryResults();
   } // end main
} // end class DisplayQueryResults
