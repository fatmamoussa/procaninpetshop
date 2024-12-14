package com.example.demo.demo3;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import java.net.URL;

import static com.example.demo.demo3.getData.loggedInUserId;
import static com.example.demo.demo3.getData.username;

public class PurchaseController  implements Initializable {

    // Déclarations des éléments FXML
    @FXML
    private ComboBox<String> breedComboBox;
    private String loggedInUser;

    @FXML
    private Spinner<Integer> quantitySpinner;

    @FXML
    private TableView<ObservableList<Object>> petTableView;

    @FXML
    private TableColumn<ObservableList<Object>, String> colRace;

    @FXML
    private TableColumn<ObservableList<Object>, Integer> colAge;

    @FXML
    private TableColumn<ObservableList<Object>, String> colSexe;

    @FXML
    private TableColumn<ObservableList<Object>, Integer> colQuantite;

    @FXML
    private TableColumn<ObservableList<Object>, Double> colPrix;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private TextField totalAmountInput;

    @FXML
    private Button payButton;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button homeBtn;

    @FXML
    private Button gestionBtn;

    @FXML
    private Label balanceLabel;
    @FXML
    private Label username;
    // Variables pour stocker les données de l'application
    private ObservableList<String> breedList = FXCollections.observableArrayList();
    private ObservableList<ObservableList<Object>> petData = FXCollections.observableArrayList();
    private double totalAmount = 0.0;

    // Méthode d'initialisation
    @FXML
    public void initialize() {

        loadBreedsFromDatabase();
        configureQuantitySpinner();
        configureTableView();
        loadAvailablePets();  // Load available pets into the table
    }
    private void loadAvailablePets() {
        String query = "SELECT race, age, sexe, quantite, prix FROM pet WHERE status = 1 AND quantite > 0";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            petData.clear();
            while (resultSet.next()) {
                String race = resultSet.getString("race");
                int age = resultSet.getInt("age");
                String sexe = resultSet.getString("sexe");
                double prix = resultSet.getDouble("prix");
                int quantite = resultSet.getInt("quantite");

                petData.add(FXCollections.observableArrayList(race, age, sexe, quantite, prix));
            }

            petTableView.setItems(petData);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Problème lors du rechargement des données dans la TableView.");
        }
    }





    // Gérer la déconnexion
    public void handleLogout() {
        navigateToPage("/com/example/demo/demo3/login.fxml", "Page de Connexion");
    }

    // Gérer l'acceuil
    public void handleAcceuil() {
        navigateToPage("/com/example/demo/demo3/acceuil.fxml", "Page d'Accueil");
    }

    // Gérer la gestion
    public void handleGestion() {
        navigateToPage("/com/example/demo/demo3/gestion.fxml", "Page de Gestion");
    }

    // Méthode pour naviguer vers une autre page
    private void navigateToPage(String fxmlPath, String pageTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) homeBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(pageTitle);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers la page.");
        }
    }

    // Afficher une alerte
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Charger les races depuis la base de données
    private void loadBreedsFromDatabase() {
        String query = "SELECT race FROM pet WHERE status = 1";
        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String breed = resultSet.getString("race");
                breedList.add(breed);
            }
            breedComboBox.setItems(breedList);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement des races", "Une erreur est survenue lors du chargement des races.");
        }
    }

    // Configurer le Spinner pour la quantité
    private void configureQuantitySpinner() {
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
    }

    private void configureTableView() {
        colRace.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().get(0).toString());
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleStringProperty("");
            }
        });

        colAge.setCellValueFactory(cellData -> {
            try {
                return new SimpleIntegerProperty((Integer) cellData.getValue().get(1)).asObject();
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleIntegerProperty(0).asObject();
            }
        });

        colSexe.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().get(2).toString());
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleStringProperty("");
            }
        });

        colQuantite.setCellValueFactory(cellData -> {
            try {
                return new SimpleIntegerProperty((Integer) cellData.getValue().get(3)).asObject();
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleIntegerProperty(0).asObject();
            }
        });

        colPrix.setCellValueFactory(cellData -> {
            try {
                return new SimpleDoubleProperty((Double) cellData.getValue().get(4)).asObject();
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleDoubleProperty(0.0).asObject();
            }
        });
    }

    // Gérer la commande pour l'achat
    @FXML
    private void handleAdd() {
        ObservableList<Object> selectedPet = petTableView.getSelectionModel().getSelectedItem();

        if (selectedPet == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez sélectionner un animal dans la table.");
            return;
        }

        String selectedBreed = selectedPet.get(0).toString();
        Integer quantity = quantitySpinner.getValue();

        if (quantity < 1) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "La quantité doit être au moins 1.");
            return;
        }

        // Vérifier la quantité disponible dans la base de données avant d'ajouter
        if (!isQuantityAvailable(selectedBreed, quantity)) {
            showAlert(Alert.AlertType.WARNING, "Quantité insuffisante", "Il n'y a pas assez de stock pour cette quantité.");
            totalAmountLabel.setText("0.00"); // Réinitialiser le total si quantité insuffisante
            return;
        }

        loadPetData(selectedBreed, quantity);
    }


    private boolean isQuantityAvailable(String breed, int quantity) {
        String query = "SELECT quantite FROM pet WHERE race = ? AND status = 1";
        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, breed);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int availableQuantity = resultSet.getInt("quantite");
                return availableQuantity >= quantity;
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la vérification de la quantité.");
        }
        return false; // Si la requête échoue ou n'a pas de résultat, retourner false
    }

    private void storeOrderInDatabase(int ownerId, String breed, int quantity, double totalAmount) {
        String query = "INSERT INTO customer_info (id_owner, total, pet_id, quantite, race) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, ownerId); // id_owner
            statement.setDouble(2, totalAmount); // total
            statement.setInt(3, getPetIdFromBreed(breed)); // pet_id
            statement.setInt(4, quantity); // quantite
            statement.setString(5, breed); // race

            statement.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande enregistrée avec succès !");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer la commande dans la base de données.");
            e.printStackTrace();
        }
    }

    private int getPetIdFromBreed(String breed) {
        String query = "SELECT pet_id FROM pet WHERE race = ? LIMIT 1";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, breed);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("pet_id");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La race spécifiée n'existe pas dans la base de données.");
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération de l'ID de l'animal.");
            return -1;
        }
    }



    // Charger les données des animaux depuis la base de données
    private void loadPetData(String breed, int quantity) {
        String query = "SELECT race, age, sexe, prix FROM pet WHERE race = ? AND status = 1";
        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, breed);
            ResultSet resultSet = statement.executeQuery();

            petData.clear();
            totalAmount = 0.0;

            while (resultSet.next()) {
                String race = resultSet.getString("race");
                int age = resultSet.getInt("age");
                String sexe = resultSet.getString("sexe");
                double prix = resultSet.getDouble("prix");

                double totalPriceForItem = prix * quantity;

                ObservableList<Object> row = FXCollections.observableArrayList(race, age, sexe, quantity, prix);
                petData.add(row);

                totalAmount += totalPriceForItem;
            }

            totalAmountLabel.setText(String.format("%.2f", totalAmount));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors du chargement des données.");
        }
    }
    private int getPetQuantityFromDB(String breed) {
        String query = "SELECT quantite FROM pet WHERE race = ?";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, breed);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("quantite");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de récupérer la quantité dans la base de données.");
        }

        return 0; // Retour par défaut
    }


    // Gérer le paiement
    @FXML
    private void handlePayment() {
        if (totalAmount == 0.0) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Aucun article sélectionné ou la quantité n'est pas disponible.");
            return;
        }

        try {
            double paidAmount = Double.parseDouble(totalAmountInput.getText());

            if (paidAmount >= totalAmount) {
                double balance = paidAmount - totalAmount;
                balanceLabel.setText("Rendu : " + String.format("%.2f", balance) + "€");

                for (ObservableList<Object> pet : petData) {
                    String breed = pet.get(0).toString();
                    int quantity = (Integer) pet.get(3);

                    // Mettre à jour le statut en fonction de la quantité
                    if (quantity == getPetQuantityFromDB(breed)) {
                        // Si la quantité totale est épuisée, désactiver l'animal
                        updatePetStatus(breed, quantity);
                    } else {
                        // Sinon, réduire simplement la quantité
                        updatePetStatus(breed, quantity);
                    }

                    // Enregistrer la commande dans la base de données
                    storeOrderInDatabase(loggedInUserId, breed, quantity, totalAmount);
                }

                showAlert(Alert.AlertType.INFORMATION, "Paiement effectué", "Le paiement a été effectué avec succès !");
                loadAvailablePets();
                resetPurchase();
            } else {
                showAlert(Alert.AlertType.WARNING, "Paiement insuffisant", "Le montant payé est insuffisant pour couvrir le total.");
                balanceLabel.setText("");
            }
        } catch (NumberFormatException e) {
            balanceLabel.setText("Erreur : Montant invalide");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un montant valide.");
        }
    }




    // Réinitialiser les valeurs après un paiement
    private void resetPurchase() {
        totalAmount = 0.0;
        totalAmountLabel.setText("0.00");
        totalAmountInput.clear();
        petData.clear();
        balanceLabel.setText("");
        loadAvailablePets(); // Reload available pets
    }

    // Méthode pour mettre à jour le statut des animaux dans la base de données
    private void updatePetStatus(String breed, int quantity) {
        String query = "UPDATE pet SET quantite = quantite - ?, status = CASE WHEN quantite = 0 THEN 0 ELSE status END WHERE race = ? AND status = 1";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, quantity);
            statement.setString(2, breed);

            statement.executeUpdate();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le statut des animaux.");
            e.printStackTrace();
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displayUsername();
        configureTableView();
        loadAvailablePets();
        configureQuantitySpinner();
        loadBreedsFromDatabase();
        loadOrdersForUser(loggedInUserId);

    }


    private void loadOrdersForUser(int ownerId) {
        String query = "SELECT * FROM customer_info WHERE id_owner = ?";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, ownerId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int orderId = resultSet.getInt("customer_id");
                String breed = resultSet.getString("race");
                int quantity = resultSet.getInt("quantite");
                double total = resultSet.getDouble("total");


            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les commandes.");
            e.printStackTrace();
        }
    }



    public void displayUsername() {
        if (username == null) {
            return; // Évitez d'afficher quoi que ce soit
        }

        try {
            if (getData.username != null) {
                username.setText(getData.username);
            } else {
                username.setText("Guest");
            }
        } catch (Exception e) {
            username.setText("Erreur");
        }
    }



}