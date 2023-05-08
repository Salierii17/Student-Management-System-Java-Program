package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.jdatepicker.impl.UtilCalendarModel;

/**
 * The class that holds the front-end table-management part of the application
 * and manages the actions performed out there
 *
 * @author Artiom
 *
 */
public class ManagementView {

    /**
     * The contents of the management window where you read and write students
     * data
     */
    static JFrame managementFrame;

    /**
     * The table containing all students
     */
    static JTable table;

    static Properties p;
    JDatePicker picker;
    JDatePanelImpl datePanel;

    /**
     * The field where user should write the student's name
     */
    static JTextField nameField;

    /**
     * The field where user should write the student's surname
     */
    static JTextField surnameField;

    /**
     * The field where user should write the student's age
     */
    static JTextField birthField;

    /**
     * The field where user should write the date when the student started
     * attending the course
     */
//    static JTextField startedDateField;
    /**
     * The box that user uses in order to select student's gender
     */
//    static JComboBox genderSelectionBox;
    static ButtonGroup bttGroup;
    static JRadioButton maleRadioButton, femaleRadioButton;

    /**
     * The box that allows user to select a course for a student
     */
    static JComboBox courseSelectionBox;
    static JComboBox facultiesSelectionBox;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Reading messages in dependance of the selected language(by default ENG)
                Translator.getMessagesFromXML();

                try {
                    ManagementView window = new ManagementView();
                    window.managementFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ManagementView() {
        initialize();
        // Clear the selection in the table, to avoid issues with updateDatabase method
        // when cells are selected
        table.clearSelection();
        // Make it visible in constructor, in order to make tests in
        // ManagementViewTest.java work
        managementFrame.setVisible(true);
        DBHandler.updateStudents();
    }

    /**
     * Updates the list of courses
     */
    
      private static String getSelectedGender() {
        if (bttGroup.getSelection() != null) {
            return bttGroup.getSelection().getActionCommand();
        } else {
            return ""; // or some other default value
        }
      }
    private void updateFaculties() {
        // Get the array of faculties and their corresponding IDs
        Object[][] faculties = DBHandler.getFaculties();

        // Create a new DefaultComboBoxModel and add the faculty names to it
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Object[] faculty : faculties) {
            model.addElement(faculty[1].toString());
        }

        // Set the new model on the facultiesSelectionBox
        facultiesSelectionBox.setModel(model);
    }

    private void updateCourses() {
        // Get the lists of courses and their corresponding IDs
        Object[][] courses = DBHandler.getCourses();

        // Create a new DefaultComboBoxModel and add the faculty names to it
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Object[] faculty : courses) {
            model.addElement(faculty[1].toString());
        }

        courseSelectionBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = courseSelectionBox.getSelectedIndex();
                if (selectedIndex >= 0) { // Check if an item is actually selected
                    int courseId = (int) courses[selectedIndex][0];
                    String courseName = courses[selectedIndex][1].toString();
                    String message = "Selected course: " + courseName + "\nCourse ID: " + courseId;
                    JOptionPane.showMessageDialog(null, message, "Course Selected", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Set the new model on the facultiesSelectionBox
        courseSelectionBox.setModel(model);
        // Add the corresponding course IDs as client properties of each item in the combo box
    }
    

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        managementFrame = new JFrame();
        managementFrame.setBounds(345, 150, 860, 600);
        managementFrame.setResizable(false);
        managementFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        managementFrame.setTitle(Translator.getValue("sms"));
        managementFrame.getContentPane().setLayout(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem importMenuItem = new JMenuItem("Import SQL");
        importMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(managementFrame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        String url = "jdbc:mysql://localhost:3306/sms";
                        String user = "root";
                        String password = "password";
                        Connection conn = DriverManager.getConnection(url, user, password);
                        Statement stmt = conn.createStatement();
                        String sql = "source " + file.getAbsolutePath().replace("\\", "/");
                        stmt.execute(sql);
                        JOptionPane.showMessageDialog(managementFrame, "SQL file imported successfully.");
                        stmt.close();
                        conn.close();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(managementFrame, "Error importing SQL file: " + ex.getMessage());
                    }
                }
            }
        });
        importMenuItem.setVisible(true);
        fileMenu.add(importMenuItem);
        menuBar.add(fileMenu); // Add fileMenu to the menuBar
        managementFrame.setJMenuBar(menuBar);

        JMenu historyMenu = new JMenu("History");
        JMenuItem viewHistoryItem = new JMenuItem("View History");
//        viewHistoryItem.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                // Add code to handle the "View History" action here
//                // Create a new JDialog window to display the history
//                JDialog historyDialog = new JDialog(managementFrame, "History", true);
//                historyDialog.setSize(400, 300);
//                historyDialog.setLocationRelativeTo(managementFrame);
//
//                // Create a JTextArea to display the history
//                JTextArea historyTextArea = new JTextArea();
//                historyTextArea.setEditable(false);
//
//                // Add a JScrollPane to the JTextArea
//                JScrollPane scrollPane = new JScrollPane(historyTextArea);
//                historyDialog.getContentPane().add(scrollPane);
//
//                // Get the history from the DBHandler
//                String[] history = DBHandler.getHistory();
//
//                // Display the history in the JTextArea
//                for (String line : history) {
//                    historyTextArea.append(line + "\n");
//                }
//
//                // Show the historyDialog
//                historyDialog.setVisible(true);
//            }
//        });
//        historyMenu.add(viewHistoryItem);
//        menuBar.add(historyMenu);

        // The panel where students table is located
        JPanel tablePanel = new JPanel();

        tablePanel.setBorder(
                new LineBorder(SystemColor.textHighlight, 5));
        tablePanel.setBounds(
                260, 10, 575, 395);
        managementFrame.getContentPane()
                .add(tablePanel);
        tablePanel.setLayout(
                null);

        // The scroll pane that allows navigation through table
        JScrollPane tableScrollPane = new JScrollPane();

        tableScrollPane.setBounds(
                10, 10, 555, 375);
        tablePanel.add(tableScrollPane);

        // Initializing the table and setting its model
        table = new JTable();

        tableScrollPane.setViewportView(table);

        table.setColumnSelectionAllowed(
                true);
        table.setModel(
                new DefaultTableModel(new Object[][]{},
                new String[]{
                    Translator.getValue("ID"),
                    Translator.getValue("name"),
                    Translator.getValue("birthdate"),
                    Translator.getValue("gender"),
                    Translator.getValue("faculty"),
                    Translator.getValue("course"),}
                ) {
            boolean[] columnEditables = new boolean[]{false, true, true, true, false, false};

            public boolean isCellEditable(int row, int column) {
                return columnEditables[column];
            }
        }
        );

        // Creating a sorter for the table
        TableRowSorter tableSorter = new TableRowSorter(table.getModel());
        table.setRowSorter(tableSorter);

        // Creating a Table Listener to detect cell modifications
        table.getModel().addTableModelListener(new TableModelListener() {

            // Actions to perform when a cell has been edited
            public void tableChanged(TableModelEvent e) {
                if (!DBHandler.updateDatabase()) {
                    JOptionPane.showMessageDialog(managementFrame, Translator.getValue("checkInput"),
                            Translator.getValue("sms"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // The panel where all buttons are located
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(new LineBorder(new Color(0, 120, 215), 5));
        buttonsPanel.setBackground(UIManager.getColor("Button.background"));
        buttonsPanel.setBounds(10, 415, 825, 80);
        managementFrame.getContentPane().add(buttonsPanel);

        // The button to press to delete an information from the table
        JButton deleteButton = new JButton(Translator.getValue("delete"));
        deleteButton.setName("deleteButton");

        // Actions to perform when "delete" button clicked
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // If no row has been selected
                if (table.getSelectedRow() == -1) {
                    JOptionPane.showMessageDialog(managementFrame, Translator.getValue("noStudentSelected"),
                            Translator.getValue("sms"), JOptionPane.ERROR_MESSAGE);
                } else {
                    // Asking the user if they are sure about that
                    if (JOptionPane.showConfirmDialog(managementFrame, Translator.getValue("warningDelete"),
                            Translator.getValue("sms"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        if (DBHandler.deleteStudent()) {
                            JOptionPane.showMessageDialog(managementFrame,
                                    Translator.getValue("studentSuccessfullyDeleted"), Translator.getValue("sms"),
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(managementFrame,
                                    Translator.getValue("somethingWrongUnexpected"), Translator.getValue("sms"),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        deleteButton.setFont(new Font("Tahoma", Font.PLAIN, 16));

        // The button to press to add a student to the table
        JButton addButton = new JButton(Translator.getValue("add"));
        addButton.setName("addButton");
        
        
        // Actions to perform when "add" button clicked
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                table.clearSelection();

                if (DBHandler.getFaculties().length == 0) {
                    JOptionPane.showMessageDialog(managementFrame, Translator.getValue("cannotAddStudent"),
                            Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // If one of the fields are empty warn user about that
                if (nameField.getText().equals("") || getSelectedGender().equals("") || birthField.getText().equals("")
                        || facultiesSelectionBox.getSelectedItem().equals("") || courseSelectionBox.getSelectedItem().equals("")) {

                    JOptionPane.showMessageDialog(managementFrame, Translator.getValue("fillEmptyFields"),
                            Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                } else {
//                    try {
//                        // Check if the written data is written correctly according to the format
//                        SimpleDateFormat format = new SimpleDateFormat("MMMM-dd-yyyy");
//                        format.setLenient(false);
//                        format.parse(birthField.getText());
//                    } catch (ParseException ex) {
//                        ex.printStackTrace();
//
//                        JOptionPane.showMessageDialog(managementFrame, Translator.getValue("dateFormatError"),
//                                Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
//
//                        return;
//                    }

                    // Get the ID of the selected course
                    Object[][] courses = DBHandler.getCourses();
                    int courseId = -1;
                    int courseIndex = courseSelectionBox.getSelectedIndex();
                    if (courseIndex >= 0) { // Check if an item is actually selected
                        courseId = (int) courses[courseIndex][0];
                    }

                    // Get the ID of the selected faculty
                    Object[][] faculties = DBHandler.getFaculties();
                    int facultyId = -1;
                    int facultyIndex = facultiesSelectionBox.getSelectedIndex();
                    if (facultyIndex >= 0) { // Check if an item is actually selected
                        facultyId = (int) faculties[facultyIndex][0];
                    }

                    if (DBHandler.addStudent(courseId, facultyId)) {
                        JOptionPane.showMessageDialog(managementFrame, Translator.getValue("studentSuccessfullyAdded"),
                                Translator.getValue("success"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(managementFrame, Translator.getValue("somethingWrongInput"),
                                Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        buttonsPanel.setLayout(new GridLayout(0, 5, 0, 0));

        addButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        buttonsPanel.add(addButton);

// Add an ActionListener to the facultiesSelectionBox
        // Add an ActionListener to the facultiesSelectionBox
//        facultiesSelectionBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // Get the ID of the selected faculty
//                int facultyID = (int) ((JComponent) facultiesSelectionBox.getSelectedItem()).getClientProperty("ID");
//
//                // Get the lists of courses and their corresponding IDs for the selected faculty
//                String[] courseNames = DBHandler.getCoursesByFaculty(facultyID);
//                Integer[] courseIDs = DBHandler.getCourseIDsByFaculty(facultyID);
//
//                // Create a new DefaultComboBoxModel with the course names
//                DefaultComboBoxModel courses = new DefaultComboBoxModel(courseNames);
//
//                // Add the corresponding course IDs as client properties of each item in the combo box
//                for (int i = 0; i < courseIDs.length; i++) {
//                    JComboBox courseBox = (JComboBox) courses.getElementAt(i);
//                    courseBox.putClientProperty("ID", courseIDs[i]);
//                }
//
//                // Set the model of the courseSelectionBox to the new DefaultComboBoxModel
//                courseSelectionBox.setModel(courses);
//            }
//        });
        // The button to press to update an information in the table
        JButton updateButton = new JButton(Translator.getValue("update"));

        // Actions to perform when "update" button clicked
        updateButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                table.clearSelection();
                DBHandler.updateStudents();
            }
        }
        );

        updateButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);

        // The button to press to exit the application
        JButton exitButton = new JButton(Translator.getValue("exit"));
        exitButton.setName("exitButton");

        // Actions to perform when "exit" button clicked
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(managementFrame, Translator.getValue("confirmDialog"),
                        Translator.getValue("sms"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    managementFrame.dispose();
                    System.exit(0);
                }
            }
        });

        // The button that user have to press in order to disconnect from the current
        // database
        JButton disconnectButton = new JButton(Translator.getValue("disconnect"));
        disconnectButton.setName("disconnectButton");

        // Actions to perform when "disconnect" button has been clicked
        disconnectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(managementFrame, Translator.getValue("confirmDialog"),
                        Translator.getValue("sms"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    // Return back to the connection window
                    Home.main(null);
                    managementFrame.dispose();
                }
            }
        });

        disconnectButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        buttonsPanel.add(disconnectButton);

        exitButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        buttonsPanel.add(exitButton);

        // The panel where user writes information about a student
        JPanel studentPanel = new JPanel();
        studentPanel.setBorder(new LineBorder(SystemColor.textHighlight, 5));
        studentPanel.setBounds(10, 10, 240, 395);
        managementFrame.getContentPane().add(studentPanel);
        studentPanel.setLayout(null);

        // The text that informs the user where they have to write the student's name
        JLabel nameText = new JLabel(Translator.getValue("name"));
        nameText.setFont(new Font("Tahoma", Font.PLAIN, 16));
        nameText.setBounds(10, 22, 67, 19);
        studentPanel.add(nameText);

        // Initializing name text field
        nameField = new JTextField();
        nameField.setName("nameField");
        nameField.setBounds(85, 23, 143, 22);
        studentPanel.add(nameField);
        nameField.setColumns(10);

        // The text that informs the user where they have to write the student's surname
        // JLabel surnameText = new JLabel(Translator.getValue("surname"));
        // surnameText.setFont(new Font("Tahoma", Font.PLAIN, 16));
        // surnameText.setBounds(10, 54, 67, 19);
        // studentPanel.add(surnameText);
        // Initializing surname text field
        // surnameField = new JTextField();
        // surnameField.setName("surnameField");
        // surnameField.setColumns(10);
        // surnameField.setBounds(85, 51, 143, 22);
        // studentPanel.add(surnameField);
        // The text that informs the user where they have to write the student's age
//        JLabel ageText = new JLabel(Translator.getValue("age"));
//        ageText.setFont(new Font("Tahoma", Font.PLAIN, 16));
//        ageText.setBounds(10, 54, 67, 19);
//        // ageText.setBounds(10, 86, 67, 19);
//        studentPanel.add(ageText);
//
//      
//
//        // The text that informs the user where they have to select student's gender
        JLabel genderText = new JLabel(Translator.getValue("gender"));
        genderText.setFont(new Font("Tahoma", Font.PLAIN, 16));
        genderText.setBounds(10, 54, 67, 19);
        studentPanel.add(genderText);

        // Initializing the box where user selects the student's gender
        bttGroup = new ButtonGroup();
        maleRadioButton = new JRadioButton("Male");
        maleRadioButton.setBounds(85, 54, 143, 22);
        maleRadioButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
        maleRadioButton.setActionCommand("Male"); // set action command
        studentPanel.add(maleRadioButton);
        bttGroup.add(maleRadioButton);
        
        femaleRadioButton = new JRadioButton("Female");
        femaleRadioButton.setBounds(85, 78, 143, 22);
        femaleRadioButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
        maleRadioButton.setActionCommand("Female"); // set action command
        studentPanel.add(femaleRadioButton);
        bttGroup.add(femaleRadioButton);

        JLabel birthdate = new JLabel("birthdate");
        birthdate.setFont(new Font("Tahoma", Font.PLAIN, 16));
        birthdate.setBounds(10, 100, 67, 19);
        studentPanel.add(birthdate);

        birthField = new JTextField();
        birthField.setName("birthField");
        birthField.setColumns(10);
        birthField.setBounds(85, 100, 120, 22);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        JFormattedTextField.AbstractFormatter formatter = new JFormattedTextField.AbstractFormatter() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object stringToValue(String text) throws ParseException {
                return dateFormat.parse(text);
            }

            @Override
            public String valueToString(Object value) throws ParseException {
                if (value instanceof Date) {
                    DateFormat dayFormat = new SimpleDateFormat("dd");
                    DateFormat monthFormat = new SimpleDateFormat("MM");
                    DateFormat yearFormat = new SimpleDateFormat("yyyy");
                    Date date = (Date) value;
                    String day = dayFormat.format(date);
                    String month = monthFormat.format(date);
                    String year = yearFormat.format(date);
                    return month + " " + day + " " + year;
                }
                return "";
            }
        };

        UtilCalendarModel calenderModel = new UtilCalendarModel();
        Properties p = new Properties();
        JDatePanelImpl datePanel = new JDatePanelImpl(calenderModel, p);

        JButton calendarButton = new JButton("...");
        calendarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog();
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setLayout(new BorderLayout());
                dialog.add(datePanel, BorderLayout.CENTER);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        calendarButton.setBounds(200, 100, 22, 22);

        datePanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Calendar selectedCalendar = (Calendar) datePanel.getModel().getValue();
                Date selectedDate = selectedCalendar.getTime();
                String formattedDate;
                try {
                    formattedDate = formatter.valueToString(selectedDate);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(studentPanel, "Error parsing date", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                birthField.setText(formattedDate);
            }
        });

        studentPanel.add(birthField);
        studentPanel.add(calendarButton);

        JLabel facultiesText = new JLabel(Translator.getValue("course"));
        facultiesText.setFont(new Font("Tahoma", Font.PLAIN, 16));
        facultiesText.setBounds(10, 130, 67, 19);
        studentPanel.add(facultiesText);

        facultiesSelectionBox = new JComboBox();
        facultiesSelectionBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
        facultiesSelectionBox.setBounds(85, 130, 143, 22);
        updateFaculties();
        studentPanel.add(facultiesSelectionBox);

        JLabel courseText = new JLabel(Translator.getValue("course"));
        courseText.setFont(new Font("Tahoma", Font.PLAIN, 16));
        courseText.setBounds(10, 160, 67, 19);
        studentPanel.add(courseText);

        courseSelectionBox = new JComboBox();
        courseSelectionBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
        courseSelectionBox.setBounds(85, 160, 143, 22);
        updateCourses();
        studentPanel.add(courseSelectionBox);

        JButton checkButton = new JButton("Verify");
        checkButton.setBounds(75, 180, 80, 22);
        checkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new JDialog();
                dialog.setTitle("CAPTCHA");
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setLayout(new BorderLayout());
                dialog.setModal(true);

                JCheckBox checkBox = new JCheckBox("I'm not a robot");
                JPanel panel = new JPanel();
                panel.add(checkBox);
                dialog.add(panel, BorderLayout.CENTER);

                ImageIcon icon = new ImageIcon("path/to/your/icon.png"); // Load your icon image
                dialog.setIconImage(icon.getImage());

                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (checkBox.isSelected()) {
                            dialog.dispose();
                            // continue with the program
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Please check the box to confirm you're not a robot.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                JPanel buttonPanel = new JPanel();
                buttonPanel.add(okButton);
                dialog.add(buttonPanel, BorderLayout.SOUTH);

                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });

        studentPanel.add(checkButton);

        JButton addFacultyButton = new JButton(Translator.getValue("addFaculty"));
        addFacultyButton.setName("addFacultyButton");

        // Actions to perform when "add faculty" button clicked
        addFacultyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String facultyName = "";

                facultyName = JOptionPane.showInputDialog(managementFrame, Translator.getValue("typeNameFaculty"));

                if (facultyName == null || facultyName.equals("")) {
                    JOptionPane.showMessageDialog(managementFrame, Translator.getValue("emptyNameFaculty"),
                            Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                } else {
                    if (DBHandler.checkIfElementExists(DBHandler.getFacultiesTable(), facultyName)) {
                        JOptionPane.showMessageDialog(managementFrame, Translator.getValue("facultyAlreadyExists"),
                                Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (DBHandler.addFaculty(facultyName)) {
                            JOptionPane.showMessageDialog(managementFrame,
                                    Translator.getValue("facultySuccessfullyAdded"), Translator.getValue("success"),
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(managementFrame, Translator.getValue("facultyNotAdded"),
                                    Translator.getValue("success"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        addFacultyButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        addFacultyButton.setBounds(10, 220, 220, 30);
        studentPanel.add(addFacultyButton);

        // Button that adds a new course
        JButton addCourseButton = new JButton(Translator.getValue("addCourse"));
        addCourseButton.setName("addCourseButton");
        addCourseButton.addActionListener(new ActionListener() {

            // Actions to perform when "add course" button clicked
            public void actionPerformed(ActionEvent e) {
                // If there are no faculties there is no way to add a course
                if (DBHandler.getFaculties().length == 0) {
                    JOptionPane.showMessageDialog(managementFrame, Translator.getValue("cannotAddCourse"),
                            Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String courseName = "";
                int facultyId = 0, duration = 0;

                courseName = JOptionPane.showInputDialog(managementFrame, Translator.getValue("typeNameCourse"));

                // If no name has been written for the course
                if (courseName == null || courseName.equals("")) {
                    JOptionPane.showMessageDialog(managementFrame, Translator.getValue("emptyNameCourse"),
                            Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    String[][] faculties = DBHandler.getFacultiesWithIds();
                    String[] facultyNames = new String[faculties.length];
                    for (int i = 0; i < faculties.length; i++) {
                        facultyNames[i] = faculties[i][1];
                    }
                    String selectedFacultyName = (String) JOptionPane.showInputDialog(null, Translator.getValue("chooseFaculty"), Translator.getValue("sms"), JOptionPane.QUESTION_MESSAGE, null, facultyNames, facultyNames[0]);
                    for (String[] faculty : faculties) {
                        if (faculty[1].equals(selectedFacultyName)) {
                            facultyId = Integer.parseInt(faculty[0]);
                            break;
                        }
                    }
                }

                // If no faculty has been selected for the course
                if (facultyId == 0) {
                    JOptionPane.showMessageDialog(managementFrame, Translator.getValue("courseNotAddedNoFaculty"),
                            Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    // In case the user types letters for the duration
                    try {
                        duration = Integer.parseInt(JOptionPane.showInputDialog(managementFrame,
                                Translator.getValue("courseTypeDuration")));
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();

                        JOptionPane.showMessageDialog(managementFrame,
                                Translator.getValue("courseNotAddedNoDuration"), Translator.getValue("error"),
                                JOptionPane.ERROR_MESSAGE);

                        return;
                    }

                    if (DBHandler.checkIfElementExists(DBHandler.getCoursesTable(), courseName)) {
                        JOptionPane.showMessageDialog(managementFrame, Translator.getValue("courseAlreadyExists"),
                                Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (DBHandler.addCourse(courseName, facultyId, duration)) {
                            JOptionPane.showMessageDialog(managementFrame,
                                    Translator.getValue("courseSuccessfullyAdded"), Translator.getValue("success"),
                                    JOptionPane.INFORMATION_MESSAGE);

                            updateCourses();
                        } else {
                            JOptionPane.showMessageDialog(managementFrame, Translator.getValue("courseNotAdded"),
                                    Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        addCourseButton.setFont(
                new Font("Tahoma", Font.PLAIN, 16));
        addCourseButton.setBounds(
                10, 260, 220, 30);
        studentPanel.add(addCourseButton);

        // Initializing the course selection box
        // Button that allows to delete a faculty
        JButton deleteFacultyButton = new JButton(Translator.getValue("deleteFaculty"));

        deleteFacultyButton.setName(
                "deleteFacultyButton");

        // Actions to perform when "Delete Faculty" button clicked
        deleteFacultyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                table.clearSelection();

                String faculty = (String) JOptionPane.showInputDialog(null, Translator.getValue("sms"),
                        Translator.getValue("chooseFacultyDelete"), JOptionPane.QUESTION_MESSAGE, null,
                        DBHandler.getFaculties(), DBHandler.getFaculties()[0]);

                // If no faculty has been selected
                if (faculty == null) {
                    return;
                }

                // If there are students attending the courses in this faculty
                if (DBHandler.getNumberOfCourses(faculty) > 0) {
                    if (JOptionPane.showConfirmDialog(managementFrame, Translator.getValue("deleteFacultyWithCourses"),
                            Translator.getValue("sms"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        if (DBHandler.deleteFacultyCourses(faculty)) {
                            JOptionPane.showMessageDialog(managementFrame,
                                    Translator.getValue("coursesFromFacultySuccessfullyDeleted"),
                                    Translator.getValue("success"), JOptionPane.INFORMATION_MESSAGE);

                            if (DBHandler.deleteFaculty(faculty)) {
                                JOptionPane.showMessageDialog(managementFrame, Translator.getValue("facultyDeleted"),
                                        Translator.getValue("success"), JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(managementFrame,
                                        Translator.getValue("somethingWrongTryAgain"), Translator.getValue("error"),
                                        JOptionPane.ERROR_MESSAGE);
                            }

                        }
                    }
                } else {
                    if (DBHandler.deleteFaculty(faculty)) {
                        JOptionPane.showMessageDialog(managementFrame, Translator.getValue("facultyDeleted"),
                                Translator.getValue("success"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(managementFrame, ("somethingWrongsdasdTryAgain"),
                                Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
                updateCourses();
            }
        });

        deleteFacultyButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        deleteFacultyButton.setBounds(10, 300, 220, 30);
        studentPanel.add(deleteFacultyButton);

        // Button that allows to delete a course
        JButton deleteCourseButton = new JButton(Translator.getValue("deleteCourse"));
        deleteCourseButton.setName("deleteCourseButton");

        // Actions to perform when "Delete Course" button clicked
        deleteCourseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                table.clearSelection();

                String course = (String) JOptionPane.showInputDialog(null, Translator.getValue("sms"),
                        Translator.getValue("chooseCourseDelete"), JOptionPane.QUESTION_MESSAGE, null,
                        DBHandler.getCourses(), DBHandler.getCourses()[0]);

                // If no course has been selected
                if (course == null) {
                    return;
                }

                // If there are students attending the course
                if (DBHandler.getNumberOfAttendees(DBHandler.getCoursesTable(), course) > 0) {
                    if (JOptionPane.showConfirmDialog(managementFrame, Translator.getValue("deleteCourseWithStudents"),
                            Translator.getValue("deleteCourse"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        if (DBHandler.deleteCourseAttendees(course)) {
                            JOptionPane.showMessageDialog(managementFrame,
                                    Translator.getValue("studentsAttendingSuccessfullyDeleted"),
                                    Translator.getValue("success"), JOptionPane.INFORMATION_MESSAGE);

                            if (DBHandler.deleteCourse(course)) {
                                JOptionPane.showMessageDialog(managementFrame, Translator.getValue("courseDeleted"),
                                        Translator.getValue("success"), JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(managementFrame,
                                        Translator.getValue("somethingWrongTryAgain"), "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(managementFrame,
                                    Translator.getValue("somethingWrongTryAgain"), Translator.getValue("error"),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    if (DBHandler.deleteCourse(course)) {
                        JOptionPane.showMessageDialog(managementFrame, Translator.getValue("courseDeleted"),
                                Translator.getValue("success"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(managementFrame, Translator.getValue("somethingWrongTryAgain"),
                                Translator.getValue("error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
                updateCourses();
            }
        });

        deleteCourseButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        deleteCourseButton.setBounds(10, 340, 220, 30);
        studentPanel.add(deleteCourseButton);
    }

}
