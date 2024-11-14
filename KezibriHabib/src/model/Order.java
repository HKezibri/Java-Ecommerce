package model;

//@Table(name = "e_Orders")
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import util.OrderStatus;

public class Order {
    private int order_id; // auto-generated
    private int clientId;
    private List<OrderItem> items;
    private Date orderDate;
    private OrderStatus status;
    private double totalAmount;
 // private Date update_date

    // Constructor with calculation for total amount based on order items
    public Order(int clientId, OrderStatus status, List<OrderItem> items) {
        this.clientId = clientId;
        this.status = status != null ? status : OrderStatus.In_Progress; // Default status to Pending if null
        this.items = items != null ? items : new ArrayList<>();  // Ensure items list is not null
        this.totalAmount = calculateTotalAmount();
    }
    public Order(int order_id, int clientId, OrderStatus status, List<OrderItem> items) {
    	this.order_id = order_id;
        this.clientId = clientId;
        this.status = status != null ? status : OrderStatus.In_Progress; // Default status to Pending if null
        this.items = items != null ? items : new ArrayList<>();  // Ensure items list is not null
        this.totalAmount = calculateTotalAmount();
    }

    // Getters and Setters
    public int getOrderId() {
    	return order_id;
    }
    
    public void setOrderId(int orderId) {
    	this.order_id = orderId;
    }

    public int getClientId() { 
    	return clientId;
    }
    
    public void setClientId(int clientId) { 
    	this.clientId = clientId;
    }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        if (items == null) {
            this.items = new ArrayList<>();//message here
        } else {
            this.items = items;
        }
        this.totalAmount = calculateTotalAmount();  // Recalculate total price when items are updated
    }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    

    public double getTotalAmount() { return totalAmount; }

    // Calculate total amount based on order items
    private double calculateTotalAmount() {
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.TotalPrice();
        }
        return total;
    }
    
    // better handled in the controller
    // Method to remove an item or decrease its quantity, and adjust the total amount accordingly
    public void removeItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        boolean itemRemoved = false;

        // Loop through the items and adjust the quantity or remove the item if necessary
        for (OrderItem orderItem : items) {
            // Check if the product ID matches (to account for duplicates)
            if (orderItem.getProductId() == item.getProductId()) {
                if (orderItem.getQuantity() > 1) {
                    // Decrease the quantity by 1 (or adjust as needed)
                    orderItem.setQuantity(orderItem.getQuantity() - 1);
                    itemRemoved = true;
                    break; // No need to check further if the item quantity is reduced
                } else {
                    // Remove the item entirely from the list if quantity reaches 1
                    items.remove(orderItem);
                    itemRemoved = true;
                    break; // Exit the loop after the item is removed
                }
            }
        }

        if (!itemRemoved) {
            throw new IllegalArgumentException("Item not found in the order");
        }

        // Recalculate total amount after item quantity change or removal
        this.totalAmount = calculateTotalAmount(); // Ensure the total amount is updated
    }



    @Override
    public String toString() {
        return "Order{" +
               "orderId=" + order_id +
               ", orderDate=" + orderDate +
               ", status=" + status +
               ", totalAmount=" + totalAmount +
               '}';
    }
}
