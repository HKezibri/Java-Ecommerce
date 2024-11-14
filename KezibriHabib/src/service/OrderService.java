package service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Order;
import model.OrderItem;
import mySQLdb.DBConnect;
import util.OrderStatus;

public class OrderService {

	public void createOrder(Order order) throws SQLException {
        String query = "INSERT INTO e_Orders (client_id, status, total) VALUES (?, ?, ?)";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getClientId());
            stmt.setString(2, order.getStatus().name());
            stmt.setDouble(3, order.getTotalAmount());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to create order, no rows affected.");
            }

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int orderId = generatedKeys.getInt(1);
                order.setOrderId(orderId);
                addOrderItems(order.getItems(), orderId, conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error creating order: " + e.getMessage(), e);
        }
    }

    private void addOrderItems(List<OrderItem> items, int orderId, Connection conn) throws SQLException {
        String query = "INSERT INTO e_OrderItem (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        // Check if the order_id exists in e_Orders table
        String checkOrderQuery = "SELECT COUNT(*) FROM e_Orders WHERE order_id = ?";
        try (PreparedStatement checkOrderStmt = conn.prepareStatement(checkOrderQuery)) {
            checkOrderStmt.setInt(1, orderId);
            ResultSet rs = checkOrderStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("Invalid order_id: " + orderId + " does not exist in e_Orders.");
            }
        }

        // Validate product existence in e_Product table
        String checkProductQuery = "SELECT COUNT(*) FROM e_Product WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (OrderItem item : items) {
                int productId = item.getProductId();
                
                // Validate product ID
                try (PreparedStatement checkProductStmt = conn.prepareStatement(checkProductQuery)) {
                    checkProductStmt.setInt(1, productId);
                    ResultSet rs = checkProductStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new SQLException("Invalid product_id: " + productId + " does not exist in e_Product.");
                    }
                }

                // Insert the order item into the table
                stmt.setInt(1, orderId);
                stmt.setInt(2, productId);
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getPrice());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error adding order items: " + e.getMessage(), e);
        }
    }



    public List<Order> getOrdersByClientId(int clientId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM e_Orders WHERE client_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("client_id"),
                        OrderStatus.valueOf(rs.getString("status")),
                        new ArrayList<>()  // Empty list for order items
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching orders: " + e.getMessage(), e);
        }
        return orders;
    }

    public void updateOrderStatus(int orderId, OrderStatus status) throws SQLException {
        String query = "UPDATE e_Orders SET status = ? WHERE order_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, orderId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update order status, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error updating order status: " + e.getMessage(), e);
        }
    }

    public void deleteOrder(int orderId) throws SQLException {
        String query = "DELETE FROM e_Orders WHERE order_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to delete order, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error deleting order: " + e.getMessage(), e);
        }
    }
}
