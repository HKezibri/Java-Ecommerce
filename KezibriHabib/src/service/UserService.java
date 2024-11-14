package service;

import java.sql.*;
import model.User;
import mySQLdb.DBConnect;
import model.Client;
import model.Admin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserService {

    // Validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Validate phone number format (simple pattern)
    private boolean isValidPhoneNumber(String phone) {
        String phoneRegex = "^\\+?[0-9]{10,13}$"; // E.g., +1234567890 or 1234567890
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    // Validate name (no "aaa", "xxx" etc., only alphabetic characters with allowed spaces and special characters)
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        name = name.trim();
        if (name.length() < 2 || name.length() > 50) {
            return false;
        }
        String[] placeholderNames = {"aaa", "xxx", "test", "placeholder"};
        for (String placeholder : placeholderNames) {
            if (name.toLowerCase().contains(placeholder)) {
                return false;
            }
        }
        String nameRegex = "^[A-Za-zÀ-ÿ\\s'-]+$";
        return name.matches(nameRegex);
    }

    // Validate password (at least 8 characters, with letters and numbers)
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8 && password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*");
    }

    // Create a new user
    public void createUser(User user) throws SQLException {
        // Validate input data
        validateUserInput(user);

        String query = "INSERT INTO e_Users (userFirstname, userLastname, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUserFirstName());
            stmt.setString(2, user.getUserLastName());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole().toString());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to create user, no rows affected.");
            }

            // Get the auto-generated user_id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    insertUserDetails(user, conn, userId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error creating user: " + e.getMessage(), e);
        }
    }

    // Validate user input
    private void validateUserInput(User user) {
        if (!isValidName(user.getUserFirstName()) || !isValidName(user.getUserLastName())) {
            throw new IllegalArgumentException("Invalid first or last name.");
        }

        if (user instanceof Client) {
            Client client = (Client) user;
            if (!isValidEmail(client.getEmail())) {
                throw new IllegalArgumentException("Invalid email address.");
            }
            if (!isValidPhoneNumber(client.getPhone())) {
                throw new IllegalArgumentException("Invalid phone number.");
            }
        }

        if (!isValidPassword(user.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and numbers.");
        }
    }

    // Insert user-specific details based on user type (Admin or Client)
    private void insertUserDetails(User user, Connection conn, int userId) throws SQLException {
        if (user instanceof Admin) {
            insertAdmin(userId, (Admin) user, conn);
        } else if (user instanceof Client) {
            insertClient(userId, (Client) user, conn);
        }
    }

    private void insertAdmin(int userId, Admin admin, Connection conn) throws SQLException {
        String insertAdminQuery = "INSERT INTO e_Admin (user_id, userFirstName, userLastName) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertAdminQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, admin.getUserFirstName());
            pstmt.setString(3, admin.getUserLastName());
            pstmt.executeUpdate();
        }
    }

    private void insertClient(int userId, Client client, Connection conn) throws SQLException {
        String insertClientQuery = "INSERT INTO e_Client (user_id, phone, address, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertClientQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, client.getPhone());
            pstmt.setString(3, client.getAddress());
            pstmt.setString(4, client.getEmail());
            pstmt.executeUpdate();
        }
    }

    // Get a user by ID
    public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM e_Users WHERE user_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                if (role.equals("admin")) {
                    return new Admin(
                            rs.getString("userFirstname"),
                            rs.getString("userLastname"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                } else {
                    return new Client(
                            rs.getString("userFirstname"),
                            rs.getString("userLastname"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            } else {
                throw new SQLException("User not found.");
            }
        }
    }

    // Update user details
    public void updateUser(User user) throws SQLException {
        validateUserInput(user);

        String query = "UPDATE e_Users SET userFirstname = ?, userLastname = ?, password = ?, role = ? WHERE user_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUserFirstName());
            stmt.setString(2, user.getUserLastName());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole().toString());
            stmt.setInt(5, user.getUserId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update user, no rows affected.");
            }
        }
    }

    // Delete a user
    public void deleteUser(int userId) throws SQLException {
        String query = "DELETE FROM e_Users WHERE user_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to delete user, no rows affected.");
            }
        }
    }

    // Search for users by userLastname
    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM e_Users WHERE userLastname = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                if (role.equals("admin")) {
                    return new Admin(
                            rs.getString("userFirstname"),
                            rs.getString("userLastname"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                } else {
                    return new Client(
                            rs.getString("userFirstname"),
                            rs.getString("userLastname"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            } else {
                throw new SQLException("User not found.");
            }
        }
    }
}
