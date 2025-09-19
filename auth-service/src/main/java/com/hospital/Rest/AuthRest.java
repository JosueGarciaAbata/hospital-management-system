package com.hospital.Rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthRest {

    @GetMapping("/foo")
    public String getFoo() {
        return "It's working!";
    }
}
