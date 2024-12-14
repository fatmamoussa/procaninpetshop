
package com.example.demo.demo3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Base{
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/petshop"; // URL de la base de données
        String user = "root"; // Nom d'utilisateur MySQL
        String password = ""; // Mot de passe MySQL

        return DriverManager.getConnection(url, user, password);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion : " + e.getMessage());
        }
    }
}
