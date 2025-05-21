package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;


@RestController
public class Welcome {

    @GetMapping("/")
    public String welcome() {
        return "Hello and welcome to our app";
    }

    @GetMapping("user/{name}/{number}")
    public String welcomeuser(@PathVariable String name,@PathVariable int number) {
        return "Hello and welcome our customer : " + name + "Phone number: " + number;
    }
    @GetMapping("em/{name}/{id}")
    public String welcomeem(@PathVariable String name,@PathVariable long id ) {
        return "Hello and welcome our employee : " + name + "ID: " + id ;
    }





}
