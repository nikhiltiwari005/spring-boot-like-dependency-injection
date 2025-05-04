package org.nikhiltiwari005;

import org.nikhiltiwari005.container.MyApplicationContext;
import org.nikhiltiwari005.controllers.UserController;

public class Main {

    public static void main(String[] args) {
        MyApplicationContext context = new MyApplicationContext("org.nikhiltiwari005");
        UserController controller = context.getBean(UserController.class);
        controller.handleRequest();
    }
}