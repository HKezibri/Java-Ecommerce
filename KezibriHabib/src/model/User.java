package model;

//@Table(name = "e_Users")
import util.Role;

public abstract class User {
    private int idUser; // auto-generated
    private String userFirstName;
    private String userLastName;
    private String username;
    private String password;
    private Role role;
 // private Date add_date private Date update_date

    public User(String userFirstName, String userLastName, String password, Role role) {
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.password = password;
        this.role = role;
    }
    public User(String u_name, String psw) {
    	this.username = u_name;
    	this.password = psw;
    }

    // Getters and setters
    public int getUserId() { return idUser; }
    public String getUserFirstName() { return userFirstName; }
    public String getUserLastName() { return userLastName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public abstract void displayInfo(); // Method to be overridden by Admin and Client
}
