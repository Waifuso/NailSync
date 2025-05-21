package phong.demo.DTO;

import java.time.LocalDate;
import java.util.ArrayList;

public class Employee_CreateRequest {

    private String user_name;
    // date of birth
    private LocalDate Dob;

    private ArrayList<Service_DTO>  Service =  new ArrayList<>();

    public Employee_CreateRequest(String user_name, LocalDate dob, ArrayList<Service_DTO> service) {
        this.user_name = user_name;

        this.Dob = dob;

        Service = service;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }


    public ArrayList<Service_DTO> getService() {
        return Service;
    }

    public void setService(ArrayList<Service_DTO> service) {
        Service = service;
    }

    public LocalDate getDob() {
        return Dob;
    }

    public void setDob(LocalDate dob) {
        Dob = dob;
    }
}
