package model;

//@Table(name = "e_Admin")
import util.Role;

public class Admin extends User {
	private String email;
	
	// private Date add_date private Date update_date
    public Admin(String userFirstName, String userLastName, String email, String password) {
        super(userFirstName, userLastName, password, Role.Admin);
        this.email = email;
    }
    public String getEmail() {
        return email;
    }

    @Override
    public void displayInfo() {
        System.out.println("Admin Username: " + getUserFirstName() + " " + getUserLastName());
        System.out.println("Email: " + email);
    }
    
}
