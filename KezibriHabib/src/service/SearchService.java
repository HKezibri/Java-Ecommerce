package service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Product;
import model.Order;
import model.User;
import model.Client;
import model.Admin;
import mySQLdb.DBConnect;
import util.OrderStatus;

public class SearchService {

    // Search for products by name
    public List<Product> searchProductsByName(String keyword) throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM e_Product WHERE name LIKE ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("imagePath"),
                        rs.getInt("stockQuantity")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error searching for products: " + e.getMessage(), e);
        }
        return products;
    }

    // Search for orders by client ID and/or status
    public List<Order> searchOrders(int clientId, String status) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM e_Orders WHERE client_id = ? AND status LIKE ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, clientId);
            stmt.setString(2, "%" + status + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrderStatus orderStatus;
                try {
                    orderStatus = OrderStatus.valueOf(rs.getString("status").toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new SQLException("Invalid order status in database for order ID: " + rs.getInt("order_id"), e);
                }

                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getInt("client_id"),
                    orderStatus,
                    new ArrayList<>()  // Empty list for items
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error searching for orders: " + e.getMessage(), e);
        }
        return orders;
    }

    // Search for users by userLastname
    public List<User> searchUsersByLastname(String keyword) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM e_Users WHERE userLastname LIKE ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String role = rs.getString("role").toLowerCase();
                User user;

                switch (role) {
                    case "Admin":
                        user = new Admin(
                                rs.getString("userFirstname"),
                                rs.getString("userLastname"),
                                rs.getString("password"),
                                rs.getString("email")
                        );
                        break;
                    case "Client":
                        user = new Client(
                                rs.getString("userFirstname"),
                                rs.getString("userLastname"),
                                rs.getString("password"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("address")
                        );
                        break;
                    default:
                        throw new SQLException("Invalid role for user ID: " + rs.getInt("user_id"));
                }
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error searching for users: " + e.getMessage(), e);
        }
        return users;
    }
}
