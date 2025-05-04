package org.nikhiltiwari005.services;


import org.nikhiltiwari005.annotations.Component;

@Component
public class UserService {

    public UserService() {
        System.out.println("User Constructor called");
    }

    public void createUser() {
        System.out.println("User Created");
    }
}
