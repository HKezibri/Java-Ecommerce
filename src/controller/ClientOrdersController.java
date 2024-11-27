package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Client;
import model.Order;
import model.User;
import service.OrderService;
import service.UserService;
import util.OrderStatus;
import util.SessionContext;

import javafx.geometry.Insets; // For setting padding/margins around nodes
import javafx.scene.layout.VBox; // For vertical box layout to structure elements vertically

//Import statements remain the same...

public class ClientOrdersController {
	
	 @FXML private TableView<Order> ordersTableView;
	 @FXML private TableColumn<Order, Integer> orderNumber; 
	 @FXML private TableColumn<Order, String> orderUsername;
	 @FXML private TableColumn<Order, String> orderAddress;
	 @FXML private TableColumn<Order, String> orderDate;
	 @FXML private TableColumn<Order, Double> orderTotal;
	 @FXML private TableColumn<Order, String> orderStatus;
	 @FXML private TableColumn<Order, Button> orderInvoice;
	 @FXML private TableColumn<Order, Button> updateStatusButton;  // New column for updating status
	 @FXML private Button clientAccount;
	 @FXML private Button clientLogout;
	 @FXML private Button homePage;
	 @FXML private Button orderDelButton;
	 @FXML private Button yourOrders;
	 @FXML private Button update;

	
	 private final OrderService orderService = new OrderService();
	 private final UserService userService = new UserService();
	 private int clientId;
	 private User loggedInUser;
	
	 @FXML
	 public void initialize() {
	     initializeOrderTable();
	     
	     // Fetch the logged-in client ID from the session context
	 loggedInUser = SessionContext.getInstance().getLoggedInUser();
	 if (loggedInUser instanceof Client) {
	     clientId = ((Client) loggedInUser).getClientId();
	     clientAccount.setText(formatUsername(loggedInUser.getUsername())); // Set formatted username on the button 
	 }
	
	 if (clientId > 0) {
	     loadClientOrderData();
	 } else {
	     System.out.println("Client ID not found in session context. Please login again.");
	     showError("Unable to find your session. Please try logging in again.");
	     }
	 }
	
	 private void initializeOrderTable() {
	     // Incrementing order number column (1-based index)
	 orderNumber.setCellValueFactory(cellData -> {
	     int index = ordersTableView.getItems().indexOf(cellData.getValue()) + 1;
	     return new ReadOnlyObjectWrapper<>(index);
	 });
	
	 // Set up other table columns
	 orderUsername.setCellValueFactory(data -> {
	     Client client = fetchClientById(data.getValue().getClientId());
	     return new ReadOnlyObjectWrapper<>(client != null ? client.getUsername() : "Unknown");
	 });
	
	 orderAddress.setCellValueFactory(data -> {
	     Client client = fetchClientById(data.getValue().getClientId());
	     return new ReadOnlyObjectWrapper<>(client != null ? client.getAddress() : "Unknown");
	 });
	
	 // Set up order date column to format the date
	 orderDate.setCellValueFactory(data -> {
	     LocalDate date = data.getValue().getOrderDate();
	     return date != null
	             ? new ReadOnlyObjectWrapper<>(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
	             : new ReadOnlyObjectWrapper<>("");
	 });
	
	 // Set up order total column
	 orderTotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalAmount()));
	
	 // Set up order status column
	 orderStatus.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStatus().name()));
	
	 // Add "View Invoice" button dynamically
	 orderInvoice.setCellFactory(column -> new TableCell<>() {
	     private final Button invoiceButton = new Button("View Invoice");
	
	     @Override
	     protected void updateItem(Button item, boolean empty) {
	         super.updateItem(item, empty);
	         if (empty) {
	             setGraphic(null);
	         } else {
	             Order order = getTableView().getItems().get(getIndex());
	             if (order.getStatus() == OrderStatus.validated || order.getStatus() == OrderStatus.delivered) {
	                 invoiceButton.setOnAction(e -> handleViewInvoice(order));
	                 setGraphic(invoiceButton);
	             } else {
	                 setGraphic(null);  // Hide button if order is not validated
	             }
	         }
	     }
	 });
	 }
	
	 private void loadClientOrderData() {
	     // Check if clientId is valid before trying to load orders
	 if (clientId <= 0) {
	     showError("Client ID is invalid. Cannot load orders.");
	     return;
	 }
	
	 try {
	     System.out.println("Fetching orders for client ID: " + clientId);
	     List<Order> orders = orderService.getOrdersByClientId(clientId);
	     ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
	
	     // Set data in the TableView
	     ordersTableView.setItems(orderList);
	     ordersTableView.refresh();  // Ensure table updates properly
	 } catch (SQLException e) {
	     showError("Failed to load orders: " + e.getMessage());
	     }
	 }
	
	 
	 @FXML
	 private void handleUpdateOrderStatus(ActionEvent event) {
	     // Get the selected order from the table
	        Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();		
		 if (selectedOrder == null) {
		     showError("No order selected.");
		     return;
		 }
		
		 // Create a choice dialog for updating the order status
		 ChoiceDialog<String> dialog = new ChoiceDialog<>(
		     selectedOrder.getStatus().name(), 
		     "in_progress", "validated"
		 );
		 dialog.setTitle("Update Order Status");
		 dialog.setHeaderText("Change the status of the selected order");
		 dialog.setContentText("Select new status:");
		
		 // Show the dialog and wait for the user's input
		 dialog.showAndWait().ifPresent(newStatus -> {
		     try {
		         // Update the order's status in the database
		         selectedOrder.setStatus(OrderStatus.valueOf(newStatus));
		         orderService.updateOrderStatus(selectedOrder.getOrderId(), OrderStatus.valueOf(newStatus));
		
		         // Reload the table data to reflect the changes
		         loadClientOrderData();
		         showInfo("Order status updated successfully.");
		     } catch (IllegalArgumentException e) {
		         showError("Invalid status selected.");
		     } catch (SQLException e) {
		         showError("Failed to update order status: " + e.getMessage());
		     }
		 }); 
		     
	 }
	
	 private Client fetchClientById(int clientId) {
	     try {
	         // Replace with actual database query logic
	     return userService.getClientById(clientId);
	 } catch (SQLException e) {
	     showError("Failed to fetch client data: " + e.getMessage());
	         return null;
	     }
	 }
	
	 @FXML
	 private void handleOrderDeleteButton(ActionEvent event) {
	     Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();
	     if (selectedOrder == null) {
	         showError("No order selected.");
	     return;
	 }
	
	 try {
	     orderService.deleteOrder(selectedOrder.getOrderId());
	     loadClientOrderData(); // Reload data after deletion
	     showInfo("Order deleted successfully.");
	 } catch (SQLException e) {
	     showError("Failed to delete order: " + e.getMessage());
	     }
	 }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be redirected to the login page.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/WelcomePage.fxml"));
                Parent loginPage = loader.load();

                Stage stage = (Stage) clientLogout.getScene().getWindow();
                stage.setScene(new Scene(loginPage));
                stage.setTitle("Welcome Page");
                stage.centerOnScreen();
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                showError("Failed to load the login page: " + e.getMessage());
            }
        }
    }

    @FXML
    private void GoToHomePage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClientShoppingPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Unable to load the home page: " + e.getMessage());
        }
    }

    private void handleViewInvoice(Order order) {
        System.out.println("Viewing invoice for Order ID: " + order.getOrderId());

        // Create a new Stage to show the invoice
        Stage invoiceStage = new Stage();
        invoiceStage.setTitle("Invoice for Order #" + order.getOrderId());

        // Create the layout for the invoice (using a VBox for simplicity)
        VBox invoiceLayout = new VBox();
        invoiceLayout.setSpacing(10);
        invoiceLayout.setPadding(new Insets(20));

        // Add the invoice details as Labels
        Label invoiceTitle = new Label("Invoice");
        invoiceTitle.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        // Assuming that orderDate is guaranteed to be non-null
        String orderDateText;
        try {
            orderDateText = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (NullPointerException e) {
            // Fallback in case of unexpected null value
            orderDateText = "Date not available (Data issue)";
        }

        Label orderIdLabel = new Label("Order ID: " + order.getOrderId());
        Label orderDateLabel = new Label("Order Date: " + orderDateText);

        Client client = fetchClientById(order.getClientId());
        Label clientNameLabel = new Label("Client: " + (client != null ? client.getUsername() : "Unknown"));
        Label clientAddressLabel = new Label("Address: " + (client != null ? client.getAddress() : "Unknown"));

        Label orderTotalLabel = new Label("Total Amount: $" + String.format("%.2f", order.getTotalAmount()));
        Label orderStatusLabel = new Label("Status: " + order.getStatus().name());

        Label thankYouNote = new Label("Thank you for your purchase!");
        thankYouNote.setStyle("-fx-font-style: italic;");

        // Add all labels to the layout
        invoiceLayout.getChildren().addAll(invoiceTitle, orderIdLabel, orderDateLabel, clientNameLabel, clientAddressLabel, orderTotalLabel, orderStatusLabel, thankYouNote);

        // Create a Scene for the new Stage and add the VBox layout to it
        Scene invoiceScene = new Scene(invoiceLayout, 300, 400);
        invoiceStage.setScene(invoiceScene);

        // Show the invoice Stage
        invoiceStage.show();
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Utility method to capitalize the first letter of each word in the username.
     * Example: "john doe" -> "John Doe"
     */
    private String formatUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "";
        }

        String[] words = username.split("\\s+"); // Split by whitespace
        StringBuilder formattedUsername = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formattedUsername.append(Character.toUpperCase(word.charAt(0)))  // Capitalize first letter
                                 .append(word.substring(1).toLowerCase())       // Rest in lowercase
                                 .append(" ");
            }
        }

        return formattedUsername.toString().trim(); // Remove trailing space
    }
}
