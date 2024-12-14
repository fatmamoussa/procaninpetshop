package com.example.demo.demo3;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.demo.demo3.getData.loggedInUserId;

public class ControllerGestion  implements Initializable {

    @FXML
    private TextField race;
    @FXML
    private TextField age;
    @FXML
    private TextField prix;
    @FXML
    private TextField quantite;
    @FXML
    private Button addBtn;
    @FXML
    private Button clear;
    @FXML
    private Button updateBtn;

    @FXML
    private Button logoutBtn;
    @FXML
    private Button homeBtn;

    @FXML
    private TextField searchField;

    @FXML
    private Button achatBtn;
    @FXML
    private TableView<Object[]> petTableView;
    @FXML
    private Button deleteBtn;
    @FXML
    private Label username;
    @FXML
    private TableColumn<Object[], String> colRace;
    @FXML
    private TableColumn<Object[], Integer> colAge;
    @FXML
    private TableColumn<Object[], Double> colPrix;
    @FXML
    private TableColumn<Object[], Integer> colQuantite;
    @FXML
    private TableColumn<Object[], String> colSexe;
    @FXML
    private ComboBox<String> sexBox;
    @FXML
    private TableColumn<Object[], Integer> colId;

    private ObservableList<Object[]> petList = FXCollections.observableArrayList();
    private FilteredList<Object[]> filteredPetList = new FilteredList<>(petList, p -> true);

    private Object[] selectedPet = null;
    private int connectedUserId;
    private int selectedPetId; //// Variable pour stocker l'ID sélectionné
    int petIdToUpdate = getSelectedPetId();




    public void displayUsername() {
        if (username == null) {
            System.out.println("Erreur : Le label 'username' n'est pas initialisé.");
            return;
        }

        try {
            if (getData.username != null) {
                System.out.println("Nom d'utilisateur récupéré : " + getData.username); // Débogage
                username.setText(getData.username);
            } else {
                username.setText("Guest");
                System.out.println("Nom d'utilisateur non défini, affichage de Guest.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            username.setText("Erreur");
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Afficher le nom d'utilisateur
        displayUsername();
        connectedUserId = getCurrentUserId();

        // Configurer les colonnes de la TableView, y compris la nouvelle colonne pour l'ID
        colId.setCellValueFactory(cellData -> new SimpleIntegerProperty((Integer) cellData.getValue()[5]).asObject()); // ID à la 6ème position
        colRace.setCellValueFactory(cellData -> new SimpleStringProperty((String) cellData.getValue()[0]));
        colAge.setCellValueFactory(cellData -> new SimpleIntegerProperty((Integer) cellData.getValue()[1]).asObject());
        colPrix.setCellValueFactory(cellData -> new SimpleDoubleProperty((Double) cellData.getValue()[2]).asObject());
        colQuantite.setCellValueFactory(cellData -> new SimpleIntegerProperty((Integer) cellData.getValue()[3]).asObject());
        colSexe.setCellValueFactory(cellData -> new SimpleStringProperty((String) cellData.getValue()[4]));

        // Lier la liste observable à la TableView (utiliser filteredPetList ici)
        petTableView.setItems(filteredPetList);

        petTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPet = newSelection;
                populateForm(selectedPet);
            }
        });

        // Charger les animaux depuis la base de données
        loadPetsFromDatabase();

        // Configurer le ComboBox avec les options
        sexBox.setItems(FXCollections.observableArrayList("Male", "Female"));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPetList.setPredicate(pet -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return pet[0].toString().toLowerCase().contains(lowerCaseFilter) || pet[4].toString().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    private int getSelectedPetId() {
        if (selectedPet != null && selectedPet.length >= 6) {
            return (int) selectedPet[5]; // L'ID est situé en 6ème position dans le tableau
        } else {

            return -1; // Retourner une valeur invalide si aucun animal n'est sélectionné
        }
    }



    private void populateForm(Object[] pet) {
        race.setText((String) pet[0]);
        age.setText(String.valueOf(pet[1]));
        prix.setText(String.valueOf(pet[2]));
        quantite.setText(String.valueOf(pet[3]));
        sexBox.setValue((String) pet[4]);
    }

    private void loadPetsFromDatabase() {
        petList.clear();

        String query = "SELECT pet_id, race, age, prix, quantite, sexe FROM pet WHERE status = 1";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("pet_id");
                String race = resultSet.getString("race");
                int age = resultSet.getInt("age");
                double prix = resultSet.getDouble("prix");
                int quantite = resultSet.getInt("quantite");
                String sexe = resultSet.getString("sexe");

                Object[] pet = {race, age, prix, quantite, sexe, id}; // Inclure l'ID ici
                petList.add(pet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de la récupération des données: " + e.getMessage());
        }
    }

    // Simule un utilisateur connecté avec l'ID 1

    private int getCurrentUserId() {
        return loggedInUserId; // Retourne l'ID de l'utilisateur simulé
    }


    @FXML
    private void handleAdd() {
        // Récupérer les valeurs des champs
        String raceText = race.getText();
        String ageText = age.getText();
        String prixText = prix.getText();
        String quantiteText = quantite.getText();
        String sexText = sexBox.getValue();
        int currentUserId = getCurrentUserId(); // Implémente la logique pour récupérer l'ID de l'utilisateur actuel

        if (!validateInputs(raceText, ageText, prixText, quantiteText, sexText)) {
            return; // Validation échoue
        }

        String query = "INSERT INTO pet (pet_id, race, age, prix, quantite, sexe, status, owner_id) VALUES (NULL, ?, ?, ?, ?, ?, 1, ?)";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // Passer les valeurs aux paramètres
            statement.setString(1, raceText);
            statement.setInt(2, Integer.parseInt(ageText));
            statement.setDouble(3, Double.parseDouble(prixText));
            statement.setInt(4, Integer.parseInt(quantiteText));
            statement.setString(5, sexText);
            statement.setInt(6, currentUserId);

            // Exécuter la requête
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // Obtenir l'ID généré par la base de données
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1); // ID généré automatiquement par la base de données
                    Object[] newPet = {raceText, Integer.parseInt(ageText), Double.parseDouble(prixText),
                            Integer.parseInt(quantiteText), sexText, generatedId, currentUserId};

                    // Ajouter dans la liste observable avec l'ID généré
                    petList.add(newPet);
                    showSuccessMessage("Animal ajouté avec succès !");
                }
            } else {
                showErrorMessage("Aucune donnée ajoutée.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de l'ajout de l'animal : " + e.getMessage());
        }
    }



    @FXML
    private void handleClear() {
        clearFormFields();
    }

    private void clearFormFields() {
        race.clear();
        age.clear();
        prix.clear();
        quantite.clear();
        sexBox.getSelectionModel().clearSelection();  // Effacer la sélection du ComboBox
        sexBox.setValue(null); // Réinitialiser visuellement la valeur du ComboBox
    }


    @FXML
    private void handleDelete() {
        // Vérifier si un animal est sélectionné dans la TableView
        Object[] selected = petTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorMessage("Veuillez sélectionner un animal à désactiver !");
            return;
        }

        // Récupérer l'ID de l'animal sélectionné
        String race = (String) selected[0];
        int selectedPetId = getPetIdByRace(race);

        if (selectedPetId == -1) {
            showErrorMessage("Erreur : l'animal sélectionné n'existe pas dans la base de données.");
            return;
        }

        // Vérifier si l'utilisateur connecté est le propriétaire de l'animal
        int petOwnerId = getPetOwnerId(selectedPetId);

        if (petOwnerId == -1) {
            showErrorMessage("L'animal sélectionné n'a pas de propriétaire associé.");
            return;
        }

        if (petOwnerId != getCurrentUserId()) {
            showErrorMessage("Vous n'êtes pas le propriétaire de cet animal.");
            return;
        }
        deactivatePet(selectedPetId);

        // Désactiver l'animal si toutes les vérifications passent

    }
    private int getPetIdByRace(String race) {
        String query = "SELECT pet_id FROM pet WHERE race = ?";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, race);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("pet_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // Retourner -1 en cas d'erreur ou si l'animal n'est pas trouvé
    }
    private void deactivatePet(int petId) {
        String query = "UPDATE pet SET status = 0 WHERE pet_id = ?";
        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, petId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showSuccessMessage("L'animal a été désactivé avec succès !");
                loadPetsFromDatabase();
            } else {
                showErrorMessage("Aucun animal trouvé avec l'ID spécifié.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de la désactivation de l'animal.");
        }
    }
    private int getPetOwnerId(int petId) {
        String query = "SELECT owner_id FROM pet WHERE pet_id = ?";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, petId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int ownerId = resultSet.getInt("owner_id");
                System.out.println("ID propriétaire dans la base de données : " + ownerId);
                return ownerId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }





    @FXML
    public void handleLogout() {
        try {
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/demo3/login.fxml"));
            Parent root = loader.load();

            // Obtenir la fenêtre actuelle (stage) et définir la nouvelle scène
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Page de Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la page de connexion.");
        }
    }
    @FXML
    public void handleAcceuil() {
        try {
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/demo3/acceuil.fxml"));
            Parent root = loader.load();

            // Obtenir la fenêtre actuelle (stage) et définir la nouvelle scène
            Stage stage = (Stage) homeBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Page de Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la page de connexion.");
        }
    }
    @FXML
    public void handlePurchase() {
        try {
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/demo3/purchase.fxml"));
            Parent root = loader.load();

            // Obtenir la fenêtre actuelle (stage) et définir la nouvelle scène
            Stage stage = (Stage) achatBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Page de Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la page de connexion.");
        }
    }




    @FXML
    private void handleUpdate() {
        // Récupérer les valeurs des champs
        String raceText = race.getText();
        String ageText = age.getText();
        String prixText = prix.getText();
        String quantiteText = quantite.getText();
        String sexText = sexBox.getValue();
        int currentUserId = getCurrentUserId(); // Récupérer l'ID de l'utilisateur connecté
        int petIdToUpdate = getSelectedPetId(); // Récupérer l'ID de l'animal sélectionné

        if (petIdToUpdate == -1) {
            showErrorMessage("Veuillez sélectionner un animal valide pour la mise à jour.");
            return;
        }

        // Vérifier si l'utilisateur est le propriétaire de l'animal
        int petOwnerId = getPetOwnerId(petIdToUpdate);

        if (petOwnerId == -1) {
            showErrorMessage("Erreur lors de la récupération du propriétaire de l'animal.");
            return;
        }

        if (petOwnerId != currentUserId) {
            showErrorMessage("Vous n'êtes pas le propriétaire de cet animal et ne pouvez pas le mettre à jour.");
            return;
        }

        // Valider les entrées
        if (!validateInputs(raceText, ageText, prixText, quantiteText, sexText)) {
            return; // Arrêter si une validation échoue
        }

        // Requête SQL pour la mise à jour
        String query = "UPDATE pet SET race = ?, age = ?, prix = ?, quantite = ?, sexe = ? WHERE pet_id = ?";

        try (Connection connection = Base.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, raceText);
            statement.setInt(2, Integer.parseInt(ageText));
            statement.setDouble(3, Double.parseDouble(prixText));
            statement.setInt(4, Integer.parseInt(quantiteText));
            statement.setString(5, sexText);
            statement.setInt(6, petIdToUpdate);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                showSuccessMessage("Animal mis à jour avec succès !");
                loadPetsFromDatabase();
            } else {
                showErrorMessage("Aucun enregistrement mis à jour. Vérifiez l'ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }



    // Afficher un message de succès
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private boolean validateInputs(String raceText, String ageText, String prixText, String quantiteText, String sexText) {
        // Valider race
        if (raceText.isEmpty() || !raceText.matches("[a-zA-Z]{1,10}")) {
            showErrorMessage("Le champ 'Race' doit contenir uniquement des lettres (max 10 caractères).");
            return false;
        }

        // Valider âge
        int ageValue;
        try {
            ageValue = Integer.parseInt(ageText);
            if (ageValue < 1 || ageValue > 8) {
                showErrorMessage("L'âge doit être un entier compris entre 1 et 8.");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("L'âge doit être un entier.");
            return false;
        }

        // Valider prix
        double prixValue;
        try {
            prixValue = Double.parseDouble(prixText);
            if (prixValue < 50 || prixValue > 5000) {
                showErrorMessage("Le prix doit être un nombre compris entre 1 et 5000.");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("Le prix doit être un nombre valide.");
            return false;
        }

        // Valider quantité
        int quantiteValue;
        try {
            quantiteValue = Integer.parseInt(quantiteText);
            if (quantiteValue < 1 || quantiteValue > 20) {
                showErrorMessage("La quantité doit être un entier compris entre 1 et 20.");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("La quantité doit être un entier.");
            return false;
        }

        // Valider sexe
        if (sexText == null || sexText.isEmpty()) {
            showErrorMessage("Veuillez sélectionner le sexe de l'animal.");
            return false;
        }

        return true; // Toutes les validations ont réussi
    }


    // Afficher un message d'erreur
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



}
