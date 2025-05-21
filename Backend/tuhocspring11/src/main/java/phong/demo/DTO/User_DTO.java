package phong.demo.DTO;

import phong.demo.Entity.Profile;

import java.time.LocalDate;

public class User_DTO {

    private String userName;

    private String email;

    private LocalDate joinedDAte;

    private Profile profile;

    public User_DTO() {
    }

    public User_DTO(String userName, String email, LocalDate joinedDAte, Profile profile) {
        this.userName = userName;
        this.email = email;
        this.joinedDAte = joinedDAte;
        this.profile = profile;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getJoinedDAte() {
        return joinedDAte;
    }

    public void setJoinedDAte(LocalDate joinedDAte) {
        this.joinedDAte = joinedDAte;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
