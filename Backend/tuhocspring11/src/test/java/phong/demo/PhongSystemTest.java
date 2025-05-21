package phong.demo;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")

public class PhongSystemTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void employee(){
        when().request("GET", "/api/employee/info/name").then().statusCode(200);
    }

    @Test
    public void service(){
        when().request("GET", "/api/services").then().statusCode(200);
    }
    @Test
    public void user(){
        when().request("GET", "/api/users/Next/ranking/1").then().statusCode(200);
    }

    @Test
    public void profile(){
        when().request("GET", "/api/users/Profile/1").then().statusCode(200);
    }

    @Test
    public void getUserById_shouldReturnBao() {
        given()
                .pathParam("id", 1L)
                .when()
                .get("/api/users/{id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                // note the capital N in userName
                .body("userName", equalTo("kakadaica"))
                .body("email", equalTo("phongdz@gmail.com"))
                // note the weird capitalization in joinedDAte
                .body("joinedDAte", equalTo("2025-09-04"))
                // profile.phone stays the same
                .body("profile.phone", equalTo("319-677-2752"));
    }

    @Test
    public void nextRankingEndpoint_returnsExpectedGoal() {
        given()
                .pathParam("id", 1)
                .when()
                .get("/api/users/Next/ranking/{id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(equalTo("You are currently at the highest ranking of us !!!!"));
    }
    @Test
    public void getAllUsers_returnsPopulatedList() {
        when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                // assert there's at least 1 user in the array
                .body("size()", greaterThan(0))
                // spot-check the first user’s fields
                .body("[0].id", equalTo(1))
                .body("[0].username", equalTo("kakadaica"))
                .body("[0].email", equalTo("phongdz@gmail.com"))
                .body("[0].profile.firstName", equalTo("Bao"))
                .body("[0].profile.totalPoints", equalTo(11010));
    }




}





