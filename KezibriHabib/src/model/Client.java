package model;

////@Table(name = "e_Client")
import util.Role;

public class Client extends User {
    private String email;
    private String phone;
    private String address;
 // private Date add_date private Date update_date

    public Client(String userFirstName, String userLastName, String password, String email, String phone, String address) {
        super(userFirstName, userLastName, password, Role.Client); // Set role using Role enum
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Getters and setters
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    @Override
    public void displayInfo() {
        System.out.println("Client Username: " + getUserFirstName() + " " + getUserLastName());
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Address: " + address);
    }
}
