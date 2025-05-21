package phong.demo.DTO;

import java.time.LocalDate;

public class Update_Employee__DTO {

    private String username;

    private String email;

    private String servicePassword;

    private boolean available;

    private LocalDate Dob;


    public Update_Employee__DTO(String username, String email, String servicePassword, boolean available, LocalDate dob) {


        this.username = username;

        this.email = email;

        this.servicePassword = servicePassword;

        this.available = available;

        Dob = dob;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServicePassword() {
        return servicePassword;
    }

    public void setServicePassword(String servicePassword) {
        this.servicePassword = servicePassword;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDate getDob() {
        return Dob;
    }

    public void setDob(LocalDate dob) {
        Dob = dob;
    }
}
