
// A TableModel that supplies ResultSet data to a JTable.
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.table.AbstractTableModel;
import com.mysql.cj.jdbc.MysqlDataSource;

// ResultSet rows and columns are counted from 1 and JTable 
// rows and columns are counted from 0. When processing 
// ResultSet rows or columns for use in a JTable, it is 
// necessary to add 1 to the row or column number to manipulate
// the appropriate ResultSet column (i.e., JTable column 0 is 
// ResultSet column 1 and JTable row 0 is ResultSet row 1).
public class ResultSetTableModel extends AbstractTableModel {
   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private Connection connection;
   private Statement queryStatement;
   private Statement updateStatement;
   private ResultSet resultSet;
   private ResultSetMetaData metaData;
   private int numberOfRows;
   private int numberOfColumns = 0;
   private boolean performedUpdate = false;

   // keep track of database connection status
   private boolean connectedToDatabase = false;

   // constructor initializes resultSet and obtains its meta data object;
   // determines number of rows
   public ResultSetTableModel() {

   } // end constructor ResultSetTableModel

   public void connectToDatabase(String url, String username, String password) throws SQLException {
      MysqlDataSource dataSource = null;

      dataSource = new MysqlDataSource();
      dataSource.setURL(url);
      dataSource.setUser(username);
      dataSource.setPassword(password);

      // connect to database bikes and query database
      // establish connection to database
      Connection connection = dataSource.getConnection();

      // create Statement to query database
      queryStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      updateStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      // update database connection status
      connectedToDatabase = true;

      // set query and execute it
      // setQuery(query);

      // set update and execute it
      // setUpdate (query);
   }

   // get class that represents column type
   public Class<?> getColumnClass(int column) throws IllegalStateException {
      // ensure database connection is available
      if (!connectedToDatabase)
         throw new IllegalStateException("Not Connected to Database");

      // determine Java class of column
      try {
         String className = metaData.getColumnClassName(column + 1);

         // return Class object that represents className
         return Class.forName(className);
      } // end try
      catch (Exception exception) {
         exception.printStackTrace();
      } // end catch

      return Object.class; // if problems occur above, assume type Object
   } // end method getColumnClass

   // get number of columns in ResultSet
   public int getColumnCount() throws IllegalStateException {
      // ensure database connection is available
      if (!connectedToDatabase)
         return 0;

      if (performedUpdate)
         return 0;

      return numberOfColumns;
   } // end method getColumnCount

   // get name of a particular column in ResultSet
   public String getColumnName(int column) throws IllegalStateException {
      // ensure database connection is available
      if (!connectedToDatabase)
         throw new IllegalStateException("Not Connected to Database");

      // determine column name
      try {
         return metaData.getColumnName(column + 1);
      } // end try
      catch (SQLException sqlException) {
         sqlException.printStackTrace();
      } // end catch

      return ""; // if problems, return empty string for column name
   } // end method getColumnName

   // return number of rows in ResultSet
   public int getRowCount() throws IllegalStateException {
      // ensure database connection is available
      if (!connectedToDatabase)
         return 0;

      if (performedUpdate)
         return 0;

      return numberOfRows;
   } // end method getRowCount

   public void clearTable() {
      numberOfRows = 0;
      numberOfColumns = 0;

      fireTableStructureChanged();
   }

   // obtain value in particular row and column
   public Object getValueAt(int row, int column) throws IllegalStateException {
      // ensure database connection is available
      if (!connectedToDatabase)
         throw new IllegalStateException("Not Connected to Database");

      // obtain a value at specified ResultSet row and column
      try {
         resultSet.next(); /* fixes a bug in MySQL/Java with date format */
         resultSet.absolute(row + 1);
         return resultSet.getObject(column + 1);
      } // end try
      catch (SQLException sqlException) {
         sqlException.printStackTrace();
      } // end catch

      return ""; // if problems, return empty string object
   } // end method getValueAt

   // set new database query string
   public void setQuery(String query) throws SQLException, IllegalStateException {
      // ensure database connection is available
      if (!connectedToDatabase)
         throw new IllegalStateException("Not Connected to Database");

      // specify query and execute it
      resultSet = queryStatement.executeQuery(query);

      // obtain meta data for ResultSet
      metaData = resultSet.getMetaData();

      // determine number of rows in ResultSet
      resultSet.last(); // move to last row
      numberOfRows = resultSet.getRow(); // get row number
      numberOfColumns = metaData.getColumnCount();
      performedUpdate = false;
      // notify JTable that model has changed
      fireTableStructureChanged();
   } // end method setQuery

   // set new database update-query string
   public void setUpdate(String query) throws SQLException, IllegalStateException {

      int res;
      // ensure database connection is available
      if (!connectedToDatabase)
         throw new IllegalStateException("Not Connected to Database");

      // specify query and execute it
      res = queryStatement.executeUpdate(query);

      System.out.println(res);
      /*
       * // obtain meta data for ResultSet metaData = resultSet.getMetaData(); //
       * determine number of rows in ResultSet resultSet.last(); // move to last row
       * numberOfRows = resultSet.getRow(); // get row number
       */
      // notify JTable that model has changed
      performedUpdate = true;
      fireTableStructureChanged();
   } // end method setUpdate

   // close Statement and Connection
   public void disconnectFromDatabase() {
      if (!connectedToDatabase)
         return;
      // close Statement and Connection
      try {
         queryStatement.close();
         updateStatement.close();
         connection.close();
      } // end try
      catch (SQLException sqlException) {
         sqlException.printStackTrace();
      } // end catch
      finally // update database connection status
      {
         connectedToDatabase = false;
      } // end finally
   } // end method disconnectFromDatabase
} // end class ResultSetTableModel
