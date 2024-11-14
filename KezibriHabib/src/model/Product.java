package model;

//@Table(name = "e_Product")

public class Product {
	
	private int idProduct; //auto-generated
    private String name;
    private String description;
    private double price;
    private String pImage;
    private int stockQuantity;
    // private Date add_date private Date update_date
    
    //Constructor
    public Product(String name, String descrip, double price, String pimage, int stock) {
        this.name = name;
        this.description = descrip;
        this.price = price;
        this.pImage = pimage;
        this.stockQuantity = stock;
    }
    public Product(int idProduct, String name, String description, double price, String pImage, int stockQuantity) {
        this.idProduct = idProduct;
        this.name = name;
        this.description = description;
        this.price = price;
        this.pImage = pImage;
        this.stockQuantity = stockQuantity;
    }

    // Getters & setters
    public int getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public String getName() {
        return name;
    }

    public void setName(String nom) {
        this.name = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getPimage() {
		return pImage;
	}
	public void setPimage(String pimage) {
		this.pImage = pimage;
	}

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stock) {
        this.stockQuantity = stock;
    }
    
    // Method to reduce stock quantity when an order is placed
    public boolean reduceStock(int quantity) {
        if (this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
            return true; // Stock reduced successfully
        }
        return false; // System.out.println("Not enough stock for the requested quantity.");
    }
    
    // Method to return product information as a string
    @Override
    public String toString() {
        return "Product{" +
                "idProduct=" + idProduct +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                '}';
    }

}
