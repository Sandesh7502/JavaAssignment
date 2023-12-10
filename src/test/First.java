package test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;
import java.sql.Statement;



public class First extends Application {
    private Connection connection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Create the GUI interface
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("Database Management Tool");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button displayDataButton = new Button("Display Data");
        Button insertDataButton = new Button("Insert Data");
        Button modifyDataButton = new Button("Modify Data");
        Button deleteDataButton = new Button("Delete Data");

        root.getChildren().addAll(titleLabel, displayDataButton, insertDataButton, modifyDataButton, deleteDataButton);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Database Manager");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Connect to the database
        establishDatabaseConnection();

        // Set up event handlers for GUI buttons
        displayDataButton.setOnAction(event -> {
            // Prompt the user for the table name
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Table Name");
            dialog.setHeaderText("Enter the table name to display data from:");
            dialog.setContentText("Table Name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(tableName -> displayData(primaryStage, tableName));
        });
        insertDataButton.setOnAction(event -> insertData(primaryStage));
        modifyDataButton.setOnAction(event -> modifyData());
        deleteDataButton.setOnAction(event -> deleteInstructor());
    }

    private void establishDatabaseConnection() {
        // Initialize the database connection here
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookdb", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    //function for displaying data

    private void displayData(Stage primaryStage, String tableName) {
        try {
            // Create a new stage for displaying data
            Stage dataStage = new Stage();
            dataStage.setTitle("Data Display");

            // Create a TextArea to display the data
            TextArea dataTextArea = new TextArea();
            dataTextArea.setEditable(false); // Read-only

            // Get the metadata of the table
            ResultSetMetaData metaData = getTableMetadata(tableName);

            if (metaData == null) {
                // Handle the case when the table doesn't exist or other issues
                dataTextArea.setText("Table not found or error.");
            } else {
                // Construct the SQL query dynamically
                StringBuilder sqlQuery = new StringBuilder("SELECT ");
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    sqlQuery.append(columnName);
                    if (i < columnCount) {
                        sqlQuery.append(", ");
                    }
                }
                sqlQuery.append(" FROM ").append(tableName);

                // Create a statement and execute the query
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery.toString());

                // Build a string to display the data
                StringBuilder dataStringBuilder = new StringBuilder();
                while (resultSet.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        String columnValue = resultSet.getString(i);
                        dataStringBuilder.append(columnName).append(": ").append(columnValue).append(", ");
                    }
                    dataStringBuilder.append("\n");
                }

                // Close resources
                resultSet.close();
                statement.close();

                // Set the text in the TextArea
                dataTextArea.setText(dataStringBuilder.toString());
            }

            // Create a scene for displaying data
            Scene dataScene = new Scene(new VBox(dataTextArea), 400, 300);
            dataStage.setScene(dataScene);

            // Show the data in a new stage
            dataStage.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // helper function for displaying table data

    private ResultSetMetaData getTableMetadata(String tableName) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = resultSet.getMetaData();
            return metaData;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

//function to insert data in instructor table

    private void insertData(Stage primaryStage) {
        // Create a new stage for inserting data
        Stage insertDataStage = new Stage();
        insertDataStage.setTitle("Insert Data");

        // Create input fields for all columns
        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField deptNameField = new TextField();
        TextField salaryField = new TextField();

        Button insertButton = new Button("Insert");
        insertButton.setOnAction(event -> {
            // Retrieve data from the form
            String id = idField.getText();
            String name = nameField.getText();
            String deptName = deptNameField.getText();
            String salary = salaryField.getText();

            // Insert the data into the Instructor table
            if (insertInstructor(id, name, deptName, salary)) {
                System.out.println("Data inserted successfully.");
            } else {
                System.out.println("Failed to insert data.");
            }

            // Clear the form fields after insertion
            idField.clear();
            nameField.clear();
            deptNameField.clear();
            salaryField.clear();
        });

        VBox form = new VBox(10, 
            new Label("ID:"), idField,
            new Label("Name:"), nameField,
            new Label("Dept Name:"), deptNameField,
            new Label("Salary:"), salaryField,
            insertButton
        );
        form.setPadding(new Insets(10));

        Scene insertDataScene = new Scene(form);
        insertDataStage.setScene(insertDataScene);

        // Show the data insertion form in a new stage
        insertDataStage.show();
    }

    private boolean insertInstructor(String id, String name, String deptName, String salary) {
        try {
            // Prepare the INSERT statement
            String sql = "INSERT INTO instructor (id, name, dept_name, salary) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            // Set the values for the prepared statement
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, deptName);
            preparedStatement.setString(4, salary);

            // Execute the INSERT statement
            int rowsAffected = preparedStatement.executeUpdate();

            // Close the prepared statement
            preparedStatement.close();

            // Return true if insertion was successful
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    
    public void modifyData() {
        // Create a dialog to enter data for teaches table
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Modify Data in Teaches Table");
        dialog.setHeaderText("Enter Data for Teaches Table");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create labels and input fields for teaches table
        Label courseLabel = new Label("Course ID:");
        Label instructorLabel = new Label("Instructor ID:");
        Label secIdLabel = new Label("Section ID:");
        Label semesterLabel = new Label("Semester:");
        Label yearLabel = new Label("Year:");
        Label buildingLabel = new Label("Building:");
        Label roomNumberLabel = new Label("Room Number:");
        Label timeSlotLabel = new Label("Time Slot ID:");

        TextField courseIdField = new TextField();
        TextField instructorIdField = new TextField();
        TextField secIdField = new TextField();
        TextField semesterField = new TextField();
        TextField yearField = new TextField();
        TextField buildingField = new TextField();
        TextField roomNumberField = new TextField();
        TextField timeSlotField = new TextField();

        GridPane grid = new GridPane();
        grid.add(courseLabel, 1, 1);
        grid.add(courseIdField, 2, 1);
        grid.add(instructorLabel, 1, 2);
        grid.add(instructorIdField, 2, 2);
        grid.add(secIdLabel, 1, 3);
        grid.add(secIdField, 2, 3);
        grid.add(semesterLabel, 1, 4);
        grid.add(semesterField, 2, 4);
        grid.add(yearLabel, 1, 5);
        grid.add(yearField, 2, 5);
        grid.add(buildingLabel, 1, 6);
        grid.add(buildingField, 2, 6);
        grid.add(roomNumberLabel, 1, 7);
        grid.add(roomNumberField, 2, 7);
        grid.add(timeSlotLabel, 1, 8);
        grid.add(timeSlotField, 2, 8);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a pair of strings when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Pair<>(
                        courseIdField.getText(),
                        instructorIdField.getText()
                );
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        // Check if data was entered and insert into section and teaches tables
        result.ifPresent(data -> {
            String courseId = data.getKey();
            String instructorId = data.getValue();
            String secId = secIdField.getText();
            String semester = semesterField.getText();
            String year = yearField.getText();
            String building = buildingField.getText();
            String roomNumber = roomNumberField.getText();
            String timeSlot = timeSlotField.getText();

            // Insert data into the section table
            insertIntoSection(courseId, secId, semester, year, building, roomNumber, timeSlot);

            // Insert data into the teaches table
            insertIntoTeaches(instructorId, courseId, secId, semester, year);
        });
    }

    public void insertIntoSection(String courseId, String secId, String semester, String year, String building, String roomNumber, String timeSlot) {
        try {
            // Create a PreparedStatement to insert into the section table
            PreparedStatement sectionStatement = connection.prepareStatement(
                    "INSERT INTO section (course_id, sec_id, semester, year, building, room_number, time_slot_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            sectionStatement.setString(1, courseId);
            sectionStatement.setString(2, secId);
            sectionStatement.setString(3, semester);
            sectionStatement.setInt(4, Integer.parseInt(year));
            sectionStatement.setString(5, building);
            sectionStatement.setString(6, roomNumber);
            sectionStatement.setString(7, timeSlot);

            // Execute the section insert statement
            sectionStatement.executeUpdate();

            // Close the section PreparedStatement
            sectionStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertIntoTeaches(String instructorId, String courseId, String secId, String semester, String year) {
        try {
            // Create a PreparedStatement to insert into the teaches table
            PreparedStatement teachesStatement = connection.prepareStatement(
                    "INSERT INTO teaches (ID, course_id, sec_id, semester, year) " +
                            "VALUES (?, ?, ?, ?, ?)"
            );
            teachesStatement.setString(1, instructorId);
            teachesStatement.setString(2, courseId);
            teachesStatement.setString(3, secId);
            teachesStatement.setString(4, semester);
            teachesStatement.setInt(5, Integer.parseInt(year));

            // Execute the teaches insert statement
            teachesStatement.executeUpdate();

            // Close the teaches PreparedStatement
            teachesStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    //function to deleter instructor
    public void deleteInstructor() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Instructor");
        dialog.setHeaderText("Enter Instructor Name to Delete");
        dialog.setContentText("Instructor Name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(instructorName -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID, name FROM instructor WHERE name LIKE ?");
                preparedStatement.setString(1, "%" + instructorName + "%");

                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder instructorInfo = new StringBuilder("Matching Instructors:\n");

                while (resultSet.next()) {
                    int instructorId = resultSet.getInt("ID");
                    String instructorNameToDelete = resultSet.getString("name");
                    instructorInfo.append("ID: ").append(instructorId).append(", Name: ").append(instructorNameToDelete).append("\n");
                }

                if (instructorInfo.toString().equals("Matching Instructors:\n")) {
                    Alert noMatchingInstructorsAlert = new Alert(Alert.AlertType.INFORMATION);
                    noMatchingInstructorsAlert.setTitle("No Matching Instructors");
                    noMatchingInstructorsAlert.setHeaderText("No matching instructors found.");
                    noMatchingInstructorsAlert.showAndWait();
                } else {
                    Alert matchingInstructorsAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    matchingInstructorsAlert.setTitle("Matching Instructors");
                    matchingInstructorsAlert.setHeaderText(instructorInfo.toString());
                    matchingInstructorsAlert.setContentText("Enter the ID of the instructor to be deleted.");

                    Optional<ButtonType> result2 = matchingInstructorsAlert.showAndWait();

                    result2.ifPresent(buttonType -> {
                        if (buttonType == ButtonType.OK) {
                            TextInputDialog idDialog = new TextInputDialog();
                            idDialog.setTitle("Confirm Deletion");
                            idDialog.setHeaderText("Enter Instructor ID to Delete");
                            idDialog.setContentText("Instructor ID:");

                            Optional<String> idResult = idDialog.showAndWait();

                            idResult.ifPresent(instructorIdToDelete -> {
                                try {
                                    int id = Integer.parseInt(instructorIdToDelete);
                                    
                                    Alert deleteCourseAttachmentAlert = new Alert(Alert.AlertType.CONFIRMATION);
                                    deleteCourseAttachmentAlert.setTitle("Delete Course Attachments");
                                    deleteCourseAttachmentAlert.setHeaderText("Do you want to delete course attachments for this instructor?");
                                    deleteCourseAttachmentAlert.setContentText("If you select Yes, courses will be assigned to 'Staff' instructor.");
                                    
                                    Optional<ButtonType> deleteCourseAttachmentsResult = deleteCourseAttachmentAlert.showAndWait();
                                    
                                    if (deleteCourseAttachmentsResult.isPresent()) {
                                        if (deleteCourseAttachmentsResult.get() == ButtonType.YES) {
                                            // Code to delete course attachments and assign to 'Staff' instructor
                                        	// First, retrieve the ID of the "Staff" instructor
                                            int staffInstructorId = getStaffInstructorId(); // Implement this method to fetch the Staff instructor ID

                                            // Update the courses to assign them to "Staff" instructor
                                            PreparedStatement updateCoursesStatement = connection.prepareStatement("UPDATE courses SET instructor_id = ? WHERE instructor_id = ?");
                                            updateCoursesStatement.setInt(1, staffInstructorId);
                                            updateCoursesStatement.setInt(2, id);
                                            updateCoursesStatement.executeUpdate();
                                        }
                                        
                                        // Delete the instructor
                                        PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM instructor WHERE ID = ?");
                                        deleteStatement.setInt(1, id);
                                        
                                        int rowsAffected = deleteStatement.executeUpdate();
                                        if (rowsAffected > 0) {
                                            Alert deletionSuccessAlert = new Alert(Alert.AlertType.INFORMATION);
                                            deletionSuccessAlert.setTitle("Deletion Successful");
                                            deletionSuccessAlert.setHeaderText("Instructor with ID " + id + " deleted successfully.");
                                            deletionSuccessAlert.showAndWait();
                                        } else {
                                            Alert deletionFailureAlert = new Alert(Alert.AlertType.ERROR);
                                            deletionFailureAlert.setTitle("Deletion Failed");
                                            deletionFailureAlert.setHeaderText("Failed to delete the instructor.");
                                            deletionFailureAlert.showAndWait();
                                        }
                                    }
                                } catch (SQLException | NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    
    private int getStaffInstructorId() throws SQLException {
        int staffInstructorId = -1; // Initialize to a value that indicates no staff instructor found
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT ID FROM instructor WHERE name = ?");
        preparedStatement.setString(1, "Staff");

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            staffInstructorId = resultSet.getInt("ID");
        }

        return staffInstructorId;
    }



}

