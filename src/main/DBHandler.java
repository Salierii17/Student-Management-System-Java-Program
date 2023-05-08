package main;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * The class that allows access to a database for reading and writing data
 * purposes
 *
 * @author Artiom
 *
 */
public class DBHandler {

    /**
     * Login to connect to the database
     */
    private static String login;

    /**
     * Password to connect to the database
     */
    private static String password;

    /**
     * Database URL
     */
    static String databaseUrl;

    /**
     * The var that stores students table's name
     */
    private final static String studentsTable;

    /**
     * The var that stores courses table's name
     */
    private final static String coursesTable;

    /**
     * The var that stores faculties table's name
     */
    private final static String facultiesTable;

    /**
     * Default constructor
     */
    public DBHandler() {

    }

    static {
        login = "root";
        databaseUrl = "jdbc:mysql://localhost:3306/sms";

        studentsTable = "students";
        coursesTable = "courses";
        facultiesTable = "faculties";
    }

    /**
     * @return The login to connect to the database
     */
    public static String getLogin() {
        return login;
    }

    /**
     * @param login - The login to set to connect to the database
     */
    public static void setLogin(final String login) {
        DBHandler.login = login;
    }

    /**
     * @return The password to connect to the database
     */
    public static String getPassword() {
        return password;
    }

    /**
     * @param password - The password to set to connect to the database
     */
    public static void setPassword(final String password) {
        DBHandler.password = password;
    }

    /**
     * @param databaseUrl - the database url to set
     */
    public static void setDatabaseUrl(final String databaseUrl) {
        DBHandler.databaseUrl = databaseUrl;
    }

    /**
     * @return The database URL
     */
    public static String getDatabaseUrl() {
        return databaseUrl;
    }

    /**
     * @return The students table's name
     */
    public static String getStudentsTable() {
        return studentsTable;
    }

    /**
     * @return The faculties table's name
     */
    public static String getFacultiesTable() {
        return facultiesTable;
    }

    /**
     * @return The courses table's name
     */
    public static String getCoursesTable() {
        return coursesTable;
    }

    /**
     * Checks if a certain table already exists in the database
     *
     * @param tableName - Table's name that is wanted to be checked
     * @return True if table exists, false otherwise
     */
    public static boolean checkIfTableExists(final String tableName) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);

            // Check if a table with tableName name already exists
            DatabaseMetaData dbmData = connection.getMetaData();
            ResultSet resultSet = dbmData.getTables(null, null, tableName, null);
            while (resultSet.next()) {
                if (resultSet.getString(3).equals(tableName)) {
                    // Return true if the table has been found
                    return true;
                }
            }

            connection.close();
            resultSet.close();

            // Return false if no table has been found
            return false;
        } catch (SQLException e) {
            e.printStackTrace();

            // Return false if an exception has been thrown
            return false;
        }
    }

    /**
     * Creates a table of students, courses and faculties
     *
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean createTables() {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            Statement statement = connection.createStatement();

            if (!checkIfTableExists(studentsTable)) {
                // Creating a table of students
                statement.executeUpdate("create table " + studentsTable + "  ( ID INT PRIMARY KEY AUTO_INCREMENT"
                        + " Name VARCHAR(50) NOT NULL,"
                        + " Birth_Date DATE NOT NULL,"
                        + " Gender VARCHAR(6) NOT NULL,"
                        + " Course_ID INT,"
                        + " Faculty_id INT,"
                        + " FOREIGN KEY (Course_ID) REFERENCES courses(ID),"
                        + " FOREIGN KEY (Faculty_ID) REFERENCES faculties(ID))");
            }

            if (!checkIfTableExists(coursesTable)) {
                // Creating a table of courses
                statement.executeUpdate("create table " + coursesTable + "(ID INT PRIMARY KEY AUTO_INCREMENT"
                        + "Name VARCHAR(50) NOT NULL,"
                        + "Faculty_ID INT,"
                        + "Duration INT NOT NULL,"
                        + "Attendees_Count INT NOT NULL,"
                        + "FOREIGN KEY (Faculty_ID) REFERENCES faculties(ID))");
            }

            if (!checkIfTableExists(facultiesTable)) {
                // Creating a table of faculties
                statement.executeUpdate("create table " + facultiesTable + "(ID INT PRIMARY KEY AUTO_INCREMENT"
                        + "Name VARCHAR(50) NOT NULL,"
                        + "Courses_Count INT NOT NULL,"
                        + "Attendees_Count INT NOT NULL)");
            }

            connection.close();
            statement.close();

            // Return true if no exception has been thrown
            return true;

        } catch (SQLException e) {
            e.printStackTrace();

            // Return false if an exception has been thrown
            return false;
        }
    }

    /**
     * Adds a new student to the table
     *
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean addStudent(int courseId, int facultyId) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            String query = "INSERT INTO " + studentsTable + " (Name, Gender, Birth_Date, Course_ID, Faculty_id) "
                    + "SELECT ?, ?, STR_TO_DATE(?, '%m %d %Y'), ?, ? "
                    + "FROM DUAL "
                    + "WHERE EXISTS (SELECT * FROM Courses c WHERE c.ID = ?) "
                    + "AND EXISTS (SELECT * FROM Faculties f WHERE f.ID = ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, ManagementView.nameField.getText());
            String gender = getSelectedGender();
            if (gender == null) {
                throw new IllegalArgumentException("Gender cannot be null");
            }
            statement.setString(2, gender);
            statement.setString(3, ManagementView.birthField.getText());
            statement.setInt(4, courseId);
            statement.setInt(5, facultyId);
            statement.setInt(6, courseId);
            statement.setInt(7, facultyId);
            int rowsInserted = statement.executeUpdate();
            connection.close();
            statement.close();
            updateStudents();
            return rowsInserted > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static String getSelectedGender() {
        if (ManagementView.bttGroup.getSelection() != null) {
            return ManagementView.bttGroup.getSelection().getActionCommand();
        } else {
            return null;
        }
    }

    /**
     * Updates the contents of the table
     *
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean updateStudents() {
        int howManyColumns = 0, currentColumn = 0;

        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT s.ID, s.Name, s.Gender, s.Birth_Date, c.Name AS Course_Name, f.Name AS Faculty_Name "
                    + "FROM " + studentsTable + " s "
                    + "JOIN Courses c ON s.Course_ID = c.ID "
                    + "JOIN Faculties f ON s.Faculty_ID = f.ID");

            // Reading data from table
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData rsmData = resultSet.getMetaData();

            howManyColumns = rsmData.getColumnCount();

            DefaultTableModel recordTable = (DefaultTableModel) ManagementView.table.getModel();
            recordTable.setRowCount(0);

            while (resultSet.next()) {
                Vector columnData = new Vector();

                for (currentColumn = 1; currentColumn <= howManyColumns; currentColumn++) {
                    columnData.add(resultSet.getString("ID"));
                    columnData.add(resultSet.getString("Name"));
                    columnData.add(resultSet.getString("Birth_Date"));

                    columnData.add(resultSet.getString("Gender"));
                    columnData.add(resultSet.getString("Course_Name"));
                    columnData.add(resultSet.getString("Faculty_Name"));

                }

                recordTable.addRow(columnData);
            }

            updateAttendees();

            connection.close();
            preparedStatement.close();
            resultSet.close();

            // Return true if no exception has been thrown
            return true;
        } catch (SQLException e) {
            e.printStackTrace();

            // Return false if exception has been thrown
            return false;
        }
    }

    /**
     * Deletes the selected student from the table
     *
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean deleteStudent() {
        // Getting row that user selected
        DefaultTableModel recordTable = (DefaultTableModel) ManagementView.table.getModel();
        int selectedRow = ManagementView.table.getSelectedRow();
        ManagementView.table.clearSelection();

        try {
            // Geting the ID of the student in the selected row
            final int ID = Integer.parseInt(recordTable.getValueAt(selectedRow, 0).toString());

            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            PreparedStatement preparedStatement = connection
                    .prepareStatement("delete from " + studentsTable + " where id = ?");

            preparedStatement.setInt(1, ID);
            preparedStatement.executeUpdate();

            connection.close();
            preparedStatement.close();

            updateStudents();

            // Return true if no exception has been thrown
            return true;
        } catch (SQLException e) {
            e.printStackTrace();

            // Return false if exception has been thrown
            return false;
        }
    }

    /**
     * Adds a faculty to the faculties table
     *
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean addFaculty(final String facultyName) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into " + facultiesTable + " (Name, Courses_count, Attendees_count) values " + "(?,?,?)");

            preparedStatement.setString(1, facultyName);
            preparedStatement.setInt(2, 0);
            preparedStatement.setInt(3, 0);

            preparedStatement.executeUpdate();

            connection.close();
            preparedStatement.close();

            // Return true if no exception has been thrown
            return true;
        } catch (SQLException e) {
            e.printStackTrace();

            // Return false if exception has been thrown
            return false;
        }
    }

    /**
     * Adds a course to the courses table
     *
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean addCourse(final String courseName, final int facultyId, final int duration) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "insert into " + coursesTable + " (Name, Faculty_ID, Duration, Attendees_Count) values " + "(?, ?, ?, ?)");

            preparedStatement.setString(1, courseName);
            preparedStatement.setInt(2, facultyId);
            preparedStatement.setInt(3, duration);
            preparedStatement.setInt(4, 0);

            preparedStatement.executeUpdate();

            connection.close();
            preparedStatement.close();

            updateAttendees();

            // Return true if no exception has been thrown
            return true;
        } catch (SQLException e) {
            e.printStackTrace();

            // Return false if exception has been thrown
            return false;
        }
    }

    /**
     * Gets all the faculties from the faculties table
     *
     * @return An array with all the faculties
     */
    public static String[][] getFacultiesWithIds() {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT ID, Name FROM " + facultiesTable);

            List<String[]> faculties = new ArrayList<>();
            while (resultSet.next()) {
                String[] faculty = new String[2];
                faculty[0] = Integer.toString(resultSet.getInt("ID"));
                faculty[1] = resultSet.getString("Name");
                faculties.add(faculty);
            }

            connection.close();
            statement.close();
            resultSet.close();

            return faculties.toArray(new String[0][0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[0][0];
        }
    }

    public static Object[][] getFaculties() {
        List<Object[]> faculties = new ArrayList<>();

        try (
                Connection connection = DriverManager.getConnection(databaseUrl, login, password); PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID, Name FROM faculties"); ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("Name");
                faculties.add(new Object[]{id, name});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convert "faculties" list to 2-dimensional Object array and return it
        return faculties.toArray(new Object[0][]);
    }

    public static Object[][] getCourses() {
        Vector<Object[]> courses = new Vector<>();

        try (
                Connection connection = DriverManager.getConnection(databaseUrl, login, password); PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID, Name FROM Courses"); ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("Name");
                Object[] course = {id, name};
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convert "courses" vector to 2D Object array and return it
        return courses.toArray(new Object[0][]);
    }

    public static Integer[] getCourseIDs() {
        Vector<Integer> ids = new Vector<Integer>();

        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID FROM course");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                ids.add(id);
            }

            connection.close();
            preparedStatement.close();
            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convert "ids" vector to Integer array and return it
        return ids.toArray(new Integer[0]);
    }

    public static String[] getCoursesByFaculty(int facultyID) {
        Vector<String> courses = new Vector<String>();

        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT Name FROM courses WHERE Faculty_ID = ?");
            preparedStatement.setInt(1, facultyID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("Name");
                courses.add(name);
            }

            connection.close();
            preparedStatement.close();
            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convert "courses" vector to String array and return it
        return courses.toArray(new String[0]);
    }

    public static Integer[] getCourseIDsByFaculty(int facultyID) {
        ArrayList<Integer> courseIDs = new ArrayList<>();

        try (
                Connection connection = DriverManager.getConnection(databaseUrl, login, password); PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID FROM courses WHERE Faculty_ID= ?")) {

            preparedStatement.setInt(1, facultyID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    courseIDs.add(resultSet.getInt("id"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courseIDs.toArray(new Integer[0]);
    }

    /**
     * Updates the number of attendees in faculties and courses tables
     */
    private static void updateAttendees() {
        updateCoursesAttendees();
        updateFacultiesAttendees();
    }

    /**
     * Updates the number of attendees in courses table
     *
     * @return True if no exception has been thrown, false otherwise
     */
    private static boolean updateCoursesAttendees() {
        try (Connection connection = DriverManager.getConnection(databaseUrl, login, password); PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT c.Name as Course_Name, COUNT(*) as Attendees_Count "
                + "FROM " + studentsTable + " s "
                + "JOIN " + coursesTable + " c ON s.Course_ID = c.ID "
                + "GROUP BY c.Name"); PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE " + coursesTable + " SET Attendees_Count = ? WHERE Name = ?")) {

            // Update the number of attendees to the courses in the courses table
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                System.out.println("No rows returned from query");
                return false;
            }
            while (resultSet.next()) {
                String courseName = resultSet.getString("Course_Name");
                int attendeesCount = resultSet.getInt("Attendees_Count");
                updateStatement.setInt(1, attendeesCount);
                updateStatement.setString(2, courseName);
                updateStatement.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the number of attendees in faculties table
     *
     * @return True if no exception has been thrown, false otherwise
     */
    private static boolean updateFacultiesAttendees() {
        try (Connection connection = DriverManager.getConnection(databaseUrl, login, password); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SELECT f.Name, COUNT(DISTINCT c.ID) AS Courses_Count, SUM(c.Attendees_Count) AS Attendees_Count FROM faculties f LEFT JOIN courses c ON f.Name = c.Faculty_ID GROUP BY f.Name")) {

            // Setting number of courses and attendees to 0 initially, in order to avoid wrong calculations
            statement.executeUpdate("update " + facultiesTable + " set Attendees_Count = 0, Courses_Count = 0");

            while (resultSet.next()) {
                final String faculty = resultSet.getString("Name");
                final int courseAttendees = resultSet.getInt("Attendees_Count");
                final int courseCount = resultSet.getInt("Courses_Count");

                statement.executeUpdate("update " + facultiesTable + " set Attendees_Count = "
                        + courseAttendees + " where Name = \"" + faculty + "\"");

                statement.executeUpdate("update " + facultiesTable + " set Courses_Count = " + courseCount
                        + " where Name = \"" + faculty + "\"");
            }

            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        } // Return false if exception has been thrown
        catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Searches if there is already an element with a certain name in a certain
     * table
     *
     * @param tableName - The table in which user wants to check if element
     * already exists
     * @param name - The name of the element user wants to check
     * @return true if the element has been found, false otherwise
     */
    public static boolean checkIfElementExists(final String tableName, final String name) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            PreparedStatement preparedStatement = connection.prepareStatement("select Name from " + tableName);

            // Get all the elements' name
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString("Name").equals(name)) {
                    // Return true if an element has been found
                    return true;
                }
            }

            connection.close();
            preparedStatement.close();
            resultSet.close();

            // Return false if no element has been found in the table
            return false;
        } catch (SQLException e) {
            e.printStackTrace();

            // Return false if an exception has been thrown
            return false;
        }
    }

    /**
     * Gets the number of attendees in a course or faculty
     *
     * @param tableName - The table in which user wants to check the number of
     * attendees(Faculties/Courses table)
     * @param element - The course/faculty name in which user wants to check the
     * number of attendees
     * @return The number of attendees in a faculty/course.
     */
    public static int getNumberOfAttendees(final String tableName, final String element) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            PreparedStatement preparedStatement = connection
                    .prepareStatement("select Attendees_Count from " + coursesTable + " where Name = " + "\"" + element + "\"");

            // Get all the elements' name
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int attendees = resultSet.getInt("Attendees_Count");

            connection.close();
            preparedStatement.close();
            resultSet.close();

            return attendees;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Deletes the students that attend a certain course
     *
     * @param course - The course's name which attendees should be deleted
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean deleteCourseAttendees(final String course) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            Statement statement = connection.createStatement();

            statement.executeUpdate("delete from " + getStudentsTable() + " where Course = " + "\"" + course + "\"");

            updateStudents();

            connection.close();
            statement.close();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Deletes a course from the courses table
     *
     * @param course - The course's name which should be deleted
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean deleteCourse(final String course) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            Statement statement = connection.createStatement();

            statement.executeUpdate("delete from " + getCoursesTable() + " where Name = " + "\"" + course + "\"");

            updateStudents();

            connection.close();
            statement.close();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Deletes a faculty from the faculties table
     *
     * @param faculty - The faculty name which should be deleted
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean deleteFaculty(final String faculty) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            Statement statement = connection.createStatement();

            statement.executeUpdate("delete from " + getFacultiesTable() + " where Name = " + "\"" + faculty + "\"");

            updateStudents();

            connection.close();
            statement.close();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Deletes all the courses in a certain faculty
     *
     * @param faculty - The faculty whose courses should be deleted
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean deleteFacultyCourses(final String faculty) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            Statement statement = connection.createStatement();

            // Getting the courses in that faculty, in order to delete students attending
            // them
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "select Name from " + getCoursesTable() + " where Faculty = " + "\"" + faculty + "\"");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                deleteCourseAttendees(resultSet.getString("Name"));
            }

            // Deleting the course
            statement.executeUpdate("delete from " + getCoursesTable() + " where Faculty = " + "\"" + faculty + "\"");

            updateStudents();

            connection.close();
            statement.close();
            preparedStatement.close();
            resultSet.close();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Gets the number of courses in a faculty
     *
     * @param faculty - The faculty's name whose number of courses should be
     * read
     * @return The number of courses in a faculty
     */
    public static int getNumberOfCourses(final String faculty) {
        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "select Course_ID from " + getFacultiesTable() + " where Name = " + "\"" + faculty + "\"");

            // Get Courses field's value
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int courses = resultSet.getInt("Course_ID");

            connection.close();
            preparedStatement.close();

            return courses;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Updates the contents of the database, taking into account changes from
     * table
     *
     * @return True if no exception has been thrown, false otherwise
     */
    public static boolean updateDatabase() {
        // Getting row and column that user selected
        int selectedRow = ManagementView.table.getSelectedRow();
        int selectedColumn = ManagementView.table.getSelectedColumn();

        try {
            Connection connection = DriverManager.getConnection(databaseUrl, login, password);
            Statement statement = connection.createStatement();

            // If a cell has been selected
            if (selectedRow > -1 && selectedColumn > -1) {
                // Geting the selected field of the selected student and changing it in database
                if (selectedColumn == 1) {
                    statement.executeUpdate("UPDATE " + studentsTable + " SET Name = " + "\""
                            + ManagementView.table.getValueAt(selectedRow, selectedColumn).toString() + "\""
                            + " WHERE ID = "
                            + Integer.parseInt(ManagementView.table.getValueAt(selectedRow, 0).toString()));
                } else if (selectedColumn == 2) {
                    statement.executeUpdate("UPDATE " + studentsTable + " SET Birth_Date = " + "\""
                            + ManagementView.table.getValueAt(selectedRow, selectedColumn).toString() + "\""
                            + " WHERE ID = "
                            + Integer.parseInt(ManagementView.table.getValueAt(selectedRow, 0).toString()));
                } else if (selectedColumn == 3) {
                    statement.executeUpdate("UPDATE " + studentsTable + " SET Gender = " + "\""
                            + ManagementView.table.getValueAt(selectedRow, selectedColumn).toString() + "\""
                            + " WHERE ID = "
                            + Integer.parseInt(ManagementView.table.getValueAt(selectedRow, 0).toString()));
                } else if (selectedColumn == 4) {
                    statement.executeUpdate("UPDATE " + studentsTable + " SET Course_ID = " + "\""
                            + ManagementView.table.getValueAt(selectedRow, selectedColumn).toString() + "\""
                            + " WHERE ID = "
                            + Integer.parseInt(ManagementView.table.getValueAt(selectedRow, 0).toString()));
                } else if (selectedColumn == 5) {
                    statement.executeUpdate("UPDATE " + studentsTable + " SET Faculty_id = " + "\""
                            + ManagementView.table.getValueAt(selectedRow, selectedColumn).toString() + "\""
                            + " WHERE ID = "
                            + Integer.parseInt(ManagementView.table.getValueAt(selectedRow, 0).toString()));
                }
            }

            connection.close();
            statement.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();

            // Return false if exception has been thrown
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

}
