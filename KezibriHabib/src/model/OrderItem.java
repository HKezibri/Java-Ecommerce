package model;

public class OrderItem {
	private int orderItemId;   // Auto-generated (primary key)
    private int orderId;       // Foreign key to the Order
    private int productId;     // Foreign key to the Product table
    private int quantity;
    private double itemPrice;

    // Constructor
    public OrderItem(int orderId, int productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.orderId = orderId;      // Store the order ID (foreign key)
        this.productId = productId;  // Store the product ID (foreign key)
        this.quantity = quantity;
        this.itemPrice = TotalPrice();
    }

    public int getOrderId() {
        return orderId;
    }
    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
    	return quantity;
    }
    public void setQuantity(int quantity) { 
    	this.quantity = quantity;
    	this.itemPrice = TotalPrice();
    }

    public double getPrice() {
        return itemPrice = TotalPrice();
    }

    // Calculate total price for this order item
    public double TotalPrice() {
    	return itemPrice * quantity;
    }
    
}
