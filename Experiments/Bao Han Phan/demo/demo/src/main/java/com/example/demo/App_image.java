package com.example.demo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;


@Controller
public class App_image {
    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("message", "Hello from Thymeleaf!");
        return "index"; // Loads index.html from templates/
    }
}
