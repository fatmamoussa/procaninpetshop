package com.example.demo.demo3;

public class getData {
    public static String username;
    public static int loggedInUserId ;

    public static String getUsername() {
        return username;
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void setLoggedInUserId(int id) {
        loggedInUserId = id;
    }
}

