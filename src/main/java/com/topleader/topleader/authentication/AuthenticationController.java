/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@RestController
@RequestMapping("/api/latest")
public class AuthenticationController {

    @GetMapping("/authentication")
    public void isAuthenticated() {
        //just check if the basic auth is working
    }

}
