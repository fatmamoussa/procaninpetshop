package com.example.demo.demo3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ControllerAcceuil {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Button logoutBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button purchaseBtn;
    @FXML
    private Button salesChartBtn; // Bouton pour afficher le graphique des ventes
    @FXML
    private Label totalLabel;
    @FXML
    private Label availablePetLabel;
    public AreaChart<String, Number> topPetsChart;

    // Définir le nom d'utilisateur sur l'étiquette de bienvenue
    public void setUsername(String username) {
        if (username != null && !username.isEmpty()) {
            getData.username = username; // Stocker le nom d'utilisateur globalement
        }
        updateWelcomeLabel();
    }

    private void updateWelcomeLabel() {
        if (welcomeLabel != null && getData.username != null) {
            welcomeLabel.setText( getData.username );
        }
    }
    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/demo3/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Page de Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de revenir à la page de connexion.");
        }
    }

    @FXML
    public void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/demo3/gestion.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) addBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Page Gestion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de charger la page Gestion.");
        }
    }

    @FXML
    public void handlePurchase() {
        try {
            int availablePets = getAvailablePets();

            // Vérification du nombre de pets disponibles
            if (availablePets == 0) {
                showAlert(AlertType.WARNING, "Avertissement", "Il n'y a plus de pets disponibles à l'achat.");
            } else if (availablePets == 1) {
                // Afficher un message d'alerte mais permettre l'accès à la page
                showAlert(AlertType.INFORMATION, "Information", "Il ne reste plus qu'un seul pet disponible. Vous pouvez procéder à l'achat.");

                // Charger la page d'achat même s'il ne reste qu'un seul pet
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/demo3/purchase.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) purchaseBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Page Achat");
                stage.show();
            } else {
                // Si des pets sont disponibles, accéder normalement à la page Achat
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/demo3/purchase.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) purchaseBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Page Achat");
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de charger la page Achat.");
        }
    }


    // Afficher un graphique des ventes des pets les plus vendus
    @FXML
    public void showTopSellingPetsChart() {
        try {
            // Définir les axes du graphique
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Race des animaux");
            yAxis.setLabel("Quantité vendue");

            // Créer un graphique à barres
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Ventes des animaux les plus populaires");

            // Préparer les données
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ventes");

            // Requête SQL pour récupérer la race des animaux et la quantité vendue
            String query = "SELECT race, SUM(quantite) AS total_quantity FROM pet WHERE status = 1 GROUP BY race ORDER BY total_quantity DESC";

            // Connexion à la base de données et exécution de la requête
            try (Connection conn = Base.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                // Ajouter les données récupérées au graphique
                while (rs.next()) {
                    String race = rs.getString("race");
                    int quantity = rs.getInt("total_quantity");

                    // Ajouter chaque race et la quantité au graphique
                    series.getData().add(new XYChart.Data<>(race, quantity));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erreur", "Impossible de récupérer les données des ventes.");
                return;
            }

            // Ajouter les séries de données au graphique
            barChart.getData().add(series);

            // Afficher le graphique dans une nouvelle fenêtre
            Stage stage = new Stage();
            Scene scene = new Scene(barChart, 800, 600);
            stage.setScene(scene);
            stage.setTitle("Graphique des Ventes");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible d'afficher le graphique des ventes.");
        }
    }

    private ObservableList<XYChart.Series<String, Number>> generateChartData(CategoryAxis xAxis, NumberAxis yAxis) {
        // Création de la série de données
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pet Sales");

        // Requête SQL pour récupérer la race des animaux et la quantité vendue
        String query = "SELECT race, SUM(quantite) AS total_quantity FROM pet  GROUP BY race ORDER BY total_quantity DESC";

        // Connexion à la base de données et exécution de la requête
        try (Connection conn = Base.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Ajouter les données récupérées au graphique
            while (rs.next()) {
                String race = rs.getString("race");
                int quantity = rs.getInt("total_quantity");

                // Ajouter chaque race et la quantité au graphique
                series.getData().add(new XYChart.Data<>(race, quantity));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de récupérer les données des ventes.");
        }

        // Ajouter la série de données à une liste observable
        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList();
        data.add(series);
        return data;
    }

    private int getAvailablePets() {
        int availablePets = 0;
        String query = "SELECT COUNT(*) FROM pet WHERE status = 1";

        try (Connection conn = Base.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                availablePets = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de récupérer les informations sur les pets.");
        }
        return availablePets;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initialize() {
        updateWelcomeLabel();
        // Créez les axes pour l'AreaChart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Pet Name");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Sold Quantity");

        // Créez le graphique de type AreaChart
        topPetsChart.setTitle("Pet Sales Data");
        topPetsChart.setData(generateChartData(xAxis, yAxis));

        // Définissez l'animation du graphique
        topPetsChart.setAnimated(false); // Optionnel: pour améliorer la performance si vous ne voulez pas d'animation

        int availablePets = getAvailablePets();
        updateAvailablePets(availablePets);  // Appel à la méthode pour mettre à jour l'étiquette

        // Mettre à jour l'affichage du montant total
        double totalAmount = getTotalAmount();
        updateTotalAmount(totalAmount);
    }

    private void updateAvailablePets(int availablePets) {
        if (availablePetLabel != null) {
            availablePetLabel.setText("Pets disponibles: " + availablePets);
        }

    }

    private double getTotalAmount() {
        double totalAmount = 0.0;
        String query = "SELECT SUM(total) FROM customer_info";

        try (Connection conn = Base.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                totalAmount = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de récupérer le total des achats.");
        }
        return totalAmount;
    }

    private void updateTotalAmount(double totalAmount) {
        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(totalAmount) + " TND");
        }
    }
}
