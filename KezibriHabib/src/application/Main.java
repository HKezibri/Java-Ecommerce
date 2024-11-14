package application;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import mySQLdb.DBConnect;
import java.sql.Connection;
import java.sql.SQLException;

import model.*;
import service.*;
import util.OrderStatus;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Test the database connection
            Connection conn = DBConnect.getConnection();
            if (conn == null) {
                showAlert("Error", "Failed to establish database connection.");
                return;
            }

            // Initialize services
            ProductService productService = new ProductService();
            UserService userService = new UserService();
            OrderService orderService = new OrderService();
            InvoiceService invoiceService = new InvoiceService();
            SearchService searchService = new SearchService();

            // Add sample products to the database
            Product sampleProduct1 = new Product("Hydrating Serum", "A serum to hydrate skin", 29.99, "image1", 100);
            Product sampleProduct2 = new Product("Sunscreen SPF 50", "Protects skin from UV rays", 15.99, "image2", 50);
            productService.addProduct(sampleProduct1);
            productService.addProduct(sampleProduct2);
            
            System.out.println("Sample products added.");

            // Add sample users to the database
            Admin adminUser = new Admin("Alice", "Anderson", "admin@example.com", "password123");
            Client clientUser = new Client("Bob", "Brown", "password123", "client@example.com", "+1234567890", "123 Main St");

            userService.createUser(adminUser);
            userService.createUser(clientUser);

            System.out.println("Sample users added.");

            // Ensure we retrieve user IDs after creation
            int adminUserId = adminUser.getUserId();
            int clientUserId = clientUser.getUserId();

            if (clientUserId == 0 || adminUserId == 0) {
                throw new SQLException("Failed to retrieve user IDs.");
            }

            // Create a sample order for the client
            Order sampleOrder = new Order(clientUserId, OrderStatus.In_Progress, new ArrayList<>());

            // Add items to the order
            OrderItem item1 = new OrderItem(sampleOrder.getOrderId(), sampleProduct1.getIdProduct(), 2);
            OrderItem item2 = new OrderItem(sampleOrder.getOrderId(), sampleProduct2.getIdProduct(), 1);

            sampleOrder.getItems().add(item1);
            sampleOrder.getItems().add(item2);

            orderService.createOrder(sampleOrder);
            
            System.out.println("Sample order added.");

            // Generate an invoice for the sample order
            invoiceService.CreateInvoice(sampleOrder.getOrderId());
            System.out.println("Invoice generated for the sample order.");

            // Search for products by keyword
            List<Product> foundProducts = searchService.searchProductsByName("Serum");
            System.out.println("Products found with 'Serum' in name:");
            for (Product product : foundProducts) {
                System.out.println(product.getName() + ": " + product.getDescription());
            }

            // Search for orders by client ID
            List<Order> foundOrders = searchService.searchOrders(clientUserId, "PENDING");
            System.out.println("Orders found for client " + clientUserId + ": " + foundOrders.size());

            // Search for users by last name
            List<User> foundUsers = searchService.searchUsersByLastname("Doe");
            System.out.println("Users found with last name 'Doe': " + foundUsers.size());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database operation failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
