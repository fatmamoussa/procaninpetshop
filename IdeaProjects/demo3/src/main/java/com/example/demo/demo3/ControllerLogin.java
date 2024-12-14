package com.example.demo.demo3;

import com.example.demo.demo3.Base;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.util.ResourceBundle;

public class ControllerLogin implements Initializable {

    @FXML
    private AnchorPane main_form;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    private Hyperlink inscrire;

    @FXML
    private Button loginBtn;

    // Méthode de connexion utilisateur
    @FXML

    public void handleLogin() {
        String user = username.getText();
        String pass = password.getText();

        // Validation de base
        if (user.isEmpty() || pass.isEmpty()) {
            showAlert(AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        try (Connection conn = Base.getConnection()) {
            // Vérifier si l'utilisateur existe
            String query = "SELECT id, password FROM admin WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, user);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    int userId = rs.getInt("id"); // Récupérer l'ID de l'utilisateur

                    if (pass.equals(storedPassword)) {
                        // Stocker le nom d'utilisateur et l'ID dans la session globale
                        getData.username = user;
                        getData.loggedInUserId = userId;

                        showAlert(AlertType.INFORMATION, "Connexion réussie", "Bienvenue, " + user + " !");
                        goToHomePage(); // Rediriger après une connexion réussie
                    } else {
                        showErrorAlert("Mot de passe incorrect !");
                    }
                } else {
                    showErrorAlert("Utilisateur non trouvé !");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de la connexion à la base de données.");
        }
    }

    // Méthode pour rediriger vers la page d'accueil
    private void goToHomePage() {
        try {
            URL fxmlLocation = getClass().getResource("/com/example/demo/demo3/acceuil.fxml");
            if (fxmlLocation == null) {
                System.err.println("Le fichier acceuil.fxml est introuvable !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = (Stage) username.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Page d'accueil");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'accueil.");
        }
    }


    // Méthode pour afficher une alerte d'erreur
    private void showErrorAlert(String message) {
        showAlert(AlertType.ERROR, "Erreur de connexion", message);
    }

    // Méthode générique pour afficher des alertes
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour rediriger vers la page d'inscription
    @FXML
    public void goToSignup() {
        try {
            URL fxmlLocation = getClass().getResource("/com/example/demo/demo3/signup.fxml");
            if (fxmlLocation == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier signup.fxml est introuvable !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = (Stage) main_form.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Page d'inscription");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'inscription : " + e.getMessage());
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loginBtn.setOnAction(event -> handleLogin());  // Associer l'action de connexion au bouton
    }
}
