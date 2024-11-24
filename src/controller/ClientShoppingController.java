package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos; // For alignment settings in layouts like HBox and VBox
import javafx.scene.text.Text; // For creating and styling text nodes
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.*;
import service.*;
import util.OrderStatus;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ClientShoppingController {

    @FXML private ImageView Img;
    @FXML private Button addCartButton, removeBtn, checkoutBtn, clientLogout, clientAccount, SearchButton;
    @FXML private TableColumn<OrderItem, String> itemCol;
    @FXML private TableColumn<OrderItem, Integer> qtyCol;
    @FXML private TableColumn<OrderItem, Double> costCol, amountCol;
    @FXML private TableView<OrderItem> cartTable;
    @FXML private ChoiceBox<Product> productNameChoice;
    @FXML private Label productPrice, totalLabel;
    @FXML private Spinner<Integer> productQty;
    @FXML private TextField SearchText;
    @FXML private FlowPane flowPane;
    

    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final UserService userservice = new UserService();
    private int clientId; // Holds the retrieved client_id
    private User loggedInUser;
    
    @FXML
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        clientAccount.setText(user.getUsername());
        System.out.println("Login successful for: " + user.getUsername());

        // Check if the logged-in user is a Client and fetch client-specific details
        if (user instanceof Client) {
            Client client = (Client) user;
            if (client.getClientId() > 0) {
                this.clientId = client.getClientId(); // Assign clientId to the class field
                System.out.println("Client ID for logged-in user: " + this.clientId);
            } else {
                showError("Failed to retrieve client information. Please contact support.");
            }
        }
    }



    @FXML
    public void initialize() {
        setupProductChoiceBox();
        setupTableView();
        setupSpinner();
        loadProductChoices();
        loadProductCards();
    }

    private void setupProductChoiceBox() {
        productNameChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayProductImage(newVal);
            }
        });
    }

    private void setupTableView() {
        itemCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getProductNameById(data.getValue().getProductId())));
        qtyCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));
        costCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getItemPrice()));
        amountCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().TotalPrice()));
        cartTable.setItems(orderItems);
        formatPriceColumns();
    }

    private void setupSpinner() {
        productQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
    }
    

    
    private void loadProductChoices() {
        try {
            List<Product> products = productService.getAllProducts();
            productNameChoice.setItems(FXCollections.observableArrayList(products));
        } catch (SQLException e) {
            showError("Failed to load products: " + e.getMessage());
        }
    }

    private void loadProductCards() {
        try {
            List<Product> products = productService.getAllProducts();
            flowPane.getChildren().clear();
            for (Product product : products) {
                VBox productCard = createProductCard(product);
                flowPane.getChildren().add(productCard);
            }
        } catch (SQLException e) {
            showError("Failed to load product cards: " + e.getMessage());
        }
    }
   

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchTerm = SearchText.getText().trim(); // Get text from the search field

        if (searchTerm.isEmpty()) {
            loadProductCards(); // Reload all products if the search term is empty
            return;
        }

        try {
            // Filter products based on the search term (case-insensitive search)
            List<Product> filteredProducts = productService.getAllProducts().stream()
                .filter(product -> product.getProductName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   product.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();

            flowPane.getChildren().clear(); // Clear the existing product cards

            if (filteredProducts.isEmpty()) {
                // Show a "no results" message if no products match
                Label noResultsLabel = new Label("No products match your search.");
                noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red;");
                flowPane.getChildren().add(noResultsLabel);
            } else {
                // Display filtered products
                for (Product product : filteredProducts) {
                    VBox productCard = createProductCard(product);
                    flowPane.getChildren().add(productCard);
                }
            }
        } catch (SQLException e) {
            showError("Failed to search products: " + e.getMessage());
        }
    }



    private VBox createProductCard(Product product) {
        // Create the product image
        ImageView productImage = new ImageView();
        String imagePath = product.getPimage();
        try {
            if (imagePath != null) {
                productImage.setImage(new Image(imagePath.startsWith("http") ? imagePath : "file:" + imagePath, true));
            } else {
                productImage.setImage(new Image("/images/default.png"));
            }
        } catch (Exception e) {
            productImage.setImage(new Image("/images/default.png"));
        }
        productImage.setFitWidth(200);
        productImage.setFitHeight(150);
        productImage.setPreserveRatio(true);

        // Create the product name label
        Label nameLabel = new Label(product.getProductName());
        nameLabel.setPrefHeight(26);
        nameLabel.setPrefWidth(147);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Create the product price label
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setPrefHeight(18);
        priceLabel.setPrefWidth(65);

        // Wrap name and price in an HBox
        HBox nameAndPriceBox = new HBox(30, nameLabel, priceLabel);
        nameAndPriceBox.setAlignment(Pos.CENTER);

        // Create the product description
        Text productDesc = new Text(product.getDescription());
        productDesc.setWrappingWidth(193); // Match the wrapping width from the FXML
        productDesc.setStyle("-fx-font-size: 12px; -fx-fill: #333333;"); // Optional styling

        // Create the VBox container
        VBox productCard = new VBox(10, nameAndPriceBox, productImage, productDesc);
        productCard.setAlignment(Pos.CENTER);
        productCard.setPrefWidth(200);
        productCard.setPrefHeight(300);
        productCard.setSpacing(10);
        productCard.setStyle("-fx-border-color: #cccccc; "
                           + "-fx-border-radius: 10px; "
                           + "-fx-background-color: #ffffff; "
                           + "-fx-background-radius: 10px; "
                           + "-fx-padding: 10px;");

        // Add click event to the card
        productCard.setOnMouseClicked(event -> {
            productNameChoice.getSelectionModel().select(product);
            displayProductImage(product);
            productQty.getValueFactory().setValue(1);
        });

        return productCard;
    }


    private String getProductNameById(int productId) {
        try {
            Product product = productService.getProductById(productId);
            return product != null ? product.getProductName() : "Unknown Product";
        } catch (SQLException e) {
            return "Unknown Product";
        }
    }

    private void displayProductImage(Product product) {
        try {
            String imagePath = product.getPimage();
            Img.setImage(new Image(imagePath.startsWith("http") ? imagePath : "file:" + imagePath, true));
        } catch (Exception e) {
            Img.setImage(new Image("/images/default.png"));
        }
    }

    @FXML
    private void handleAddToCart(ActionEvent event) {
        Product selectedProduct = productNameChoice.getSelectionModel().getSelectedItem();
        int quantity = productQty.getValue();

        if (selectedProduct == null || quantity <= 0) {
            showError("Invalid product selection or quantity.");
            return;
        }

        if (selectedProduct.getStockQuantity() < quantity) {
            showError("Insufficient stock available.");
            return;
        }

        // Update quantity if product exists in the cart
        OrderItem existingItem = orderItems.stream()
                .filter(item -> item.getProductId() == selectedProduct.getIdProduct())
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            orderItems.add(new OrderItem(0, selectedProduct.getIdProduct(), quantity, selectedProduct.getPrice()));
        }

        cartTable.refresh(); // Ensure table updates
        try {
            productService.updateProductStock(selectedProduct.getIdProduct(), -quantity);
            updateTotalCost();
        } catch (SQLException e) {
            showError("Failed to update stock: " + e.getMessage());
        }
    }

    private void updateTotalCost() {
        double total = orderItems.stream().mapToDouble(OrderItem::TotalPrice).sum();
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    @FXML
    private void handleRemoveButton(ActionEvent event) {
        OrderItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showError("No item selected.");
            return;
        }

        orderItems.remove(selectedItem);
        cartTable.refresh();
        try {
            productService.updateProductStock(selectedItem.getProductId(), selectedItem.getQuantity());
            updateTotalCost();
        } catch (SQLException e) {
            showError("Failed to update stock: " + e.getMessage());
        }
    }

   /* @FXML
    private void handleCheckout(ActionEvent event) {
        if (orderItems.isEmpty()) {
            showError("Cart is empty.");
            return;
        }

        try {
            // Calculate total cost
            double totalCost = orderItems.stream().mapToDouble(OrderItem::TotalPrice).sum();

            // Create a new order for the client
            Order newOrder = new Order(clientId, OrderStatus.in_progress, new ArrayList<>(orderItems));
            newOrder.setTotalAmount(totalCost); // Set the total amount
            orderService.createOrder(newOrder); // Save the order to the database

            // Clear the cart and reset UI
            orderItems.clear();
            cartTable.refresh();
            updateTotalCost();
            showInfo("Checkout completed successfully!");
        } catch (SQLException e) {
            showError("Checkout failed: " + e.getMessage());
        }
    }*/
    @FXML
    private void handleCheckout(ActionEvent event) {
        if (orderItems.isEmpty()) {
            showError("Cart is empty.");
            return;
        }

        try {
            double totalCost = orderItems.stream().mapToDouble(OrderItem::TotalPrice).sum();

            // Create a new order for the client
            Order newOrder = new Order(clientId, OrderStatus.in_progress, new ArrayList<>(orderItems));
            newOrder.setTotalAmount(totalCost);

            // Save the order to the database
            orderService.createOrder(newOrder);

            // Clear the cart and reset UI
            orderItems.clear();
            cartTable.refresh();
            updateTotalCost();
            showInfo("Checkout completed successfully!");
        } catch (SQLException e) {
            showError("Checkout failed: " + e.getMessage());
        }
    }


    @FXML
    public void handleLogout(ActionEvent event) {
        // Show confirmation alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be redirected to the login page.");
        
        // Wait for user response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("Admin logged out.");
            
            // Load the login page
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
                Parent loginPage = loader.load();

                // Get the current stage and set the login scene
                Stage stage = (Stage) clientLogout.getScene().getWindow();
                Scene scene = new Scene(loginPage);
                stage.setScene(scene);
                stage.setTitle("Login Page");
                stage.show();
            } catch (IOException e) {
                showError("Failed to load the login page: " + e.getMessage());
            }
        } else {
            // User cancelled logout
            System.out.println("Logout cancelled.");
        }
    }

    private void formatPriceColumns() {
        costCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });

        amountCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
}

    