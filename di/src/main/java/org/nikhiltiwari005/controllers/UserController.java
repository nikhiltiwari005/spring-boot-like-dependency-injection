package org.nikhiltiwari005.controllers;


import org.nikhiltiwari005.annotations.Autowired;
import org.nikhiltiwari005.annotations.Component;
import org.nikhiltiwari005.services.UserService;

@Component
public class UserController {

    @Autowired
    private UserService userService;

    public void handleRequest() {
        userService.createUser();
    }
}
