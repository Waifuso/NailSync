package phong.demo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    private Long id; // Sử dụng cùng ID với User


    private String firstName;

    private String lastName;

    private String phone;

    @Enumerated(EnumType.STRING)
    private Ranking ranking;

    private Integer totalPoints;



    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-profiles")
    private User user;




    // Constructors
    public Profile() {}

    public Profile(String firstName, String lastName, String phone) {
        this.firstName = firstName;

        this.lastName = lastName;

        this.phone = phone;
    }

    // Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Ranking getRanking() {
        return ranking;
    }

    public void setRanking(Ranking ranking) {
        this.ranking = ranking;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }
}

