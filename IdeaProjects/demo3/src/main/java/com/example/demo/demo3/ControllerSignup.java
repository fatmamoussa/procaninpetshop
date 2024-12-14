package com.example.demo.demo3;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ControllerSignup {

    @FXML
    private PasswordField password;
    @FXML
    private AnchorPane main_form;

    @FXML
    private PasswordField password1;

    @FXML
    private TextField username;

    @FXML
    private Hyperlink inscrire;
    @FXML
    private Button signup;

    // Méthode pour gérer l'inscription
    @FXML
    public void goToSignup() {
        try {
            URL fxmlLocation = getClass().getResource("/com/example/demo/demo3/login.fxml");
            if (fxmlLocation == null) {
                System.out.println("FXML file not found!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = (Stage) main_form.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Signup Page");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Échec du chargement de la page d'inscription : " + e.getMessage());
        }
    }

    @FXML
    public void signupAction() {
        String user = username.getText();
        String pass = password.getText();
        String passConfirm = password1.getText();

        // Vérification des contraintes sur le nom d'utilisateur
        if (user.length() > 10) {
            showAlert(AlertType.ERROR, "Erreur", "Le nom d'utilisateur ne peut pas dépasser 10 caractères.");
            return;
        }
        if (user.contains("@") || user.contains("/")) {
            showAlert(AlertType.ERROR, "Erreur", "Le nom d'utilisateur ne peut pas contenir '@' ou '/'.");
            return;
        }

        // Vérification que les mots de passe correspondent
        if (!pass.equals(passConfirm)) {
            showAlert(AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas.");
            return;
        }

        // Vérification si l'utilisateur existe déjà
        if (userExists(user)) {
            showAlert(AlertType.ERROR, "Erreur", "L'utilisateur existe déjà.");
        } else {
            // Insérer l'utilisateur dans la base de données
            if (addUser(user, pass)) {
                showAlert(AlertType.INFORMATION, "Succès", "Utilisateur ajouté avec succès !");
                goToHomePage();  // Appel à la méthode pour rediriger vers la page d'accueil
            } else {
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de l'utilisateur.");
            }
        }
    }

    // Vérification si un utilisateur existe déjà dans la base
    private boolean userExists(String username) {
        String query = "SELECT COUNT(*) FROM admin WHERE username = ?";
        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Ajouter un utilisateur dans la base de données
    private boolean addUser(String username, String password) {
        String query = "INSERT INTO admin (username, password) VALUES (?, ?)";
        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password); // Idéalement, hachez le mot de passe avant de l'insérer
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void goToHomePage() {
        try {
            // Charger le fichier FXML de la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/demo3/acceuil.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur associé
            ControllerAcceuil controller = loader.getController();

            // Passer le nom d'utilisateur au contrôleur de la page d'accueil
            String user = username.getText(); // Nom d'utilisateur saisi
            controller.setUsername(user);

            // Remplacer la scène actuelle par la nouvelle
            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Page d'accueil");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Une erreur s'est produite lors du chargement de la page d'accueil.");
        }
    }


    // Afficher une alerte
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
