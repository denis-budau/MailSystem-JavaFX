package com.unito.common;

public class UserSession {
    private static User currentUser;

    // Impostazione dell'utente corrente nella sessione
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Recupero dell'utente corrente dalla sessione
    public static User getCurrentUser() {
        return currentUser;
    }
}