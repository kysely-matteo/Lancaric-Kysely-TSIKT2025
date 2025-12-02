package sk.front.front.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sk.front.front.model.*;
import sk.front.front.service.ApiService;
import sk.front.front.service.AuthService;
import sk.front.front.service.ResourceService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class GroupDetailController {
    @FXML private Label groupNameLabel;
    @FXML private Label groupDescriptionLabel;
    @FXML private Label memberCountLabel;
    @FXML private TextArea groupInfoArea;
    @FXML private ListView<MembershipResponse> membersListView;
    @FXML private ListView<Task> tasksListView;
    @FXML private VBox tasksSection;
    @FXML private HBox taskFilterButtons;

    // FILE SHARING COMPONENTS
    @FXML private TableView<Resource> resourcesTableView;
    @FXML private TableColumn<Resource, String> resourceNameColumn;
    @FXML private TableColumn<Resource, String> resourceTypeColumn;
    @FXML private TableColumn<Resource, String> resourceUploaderColumn;
    @FXML private TableColumn<Resource, LocalDateTime> resourceDateColumn;
    @FXML private VBox resourcesSection;

    private final List<NotificationMessage> groupNotifications = FXCollections.observableArrayList();

    private ApiService apiService = new ApiService();
    private ResourceService resourceService = new ResourceService();
    private GroupResponse currentGroup;
    private User currentUser;
    private String currentTaskFilter = "ALL";

    public void setGroup(GroupResponse group) {
        this.currentGroup = group;
        loadGroupData();
        //setupWebSocketForGroup();
    }

    @FXML
    private void initialize() {
        currentUser = AuthService.getCurrentUser();
        setupTaskFilters();
        setupResourcesTable();

        // Format zobrazenia členov v ListView
        membersListView.setCellFactory(lv -> new javafx.scene.control.ListCell<MembershipResponse>() {
            @Override
            protected void updateItem(MembershipResponse member, boolean empty) {
                super.updateItem(member, empty);
                if (empty || member == null) {
                    setText(null);
                } else {
                    String roleIcon = "MEMBER".equals(member.getRole()) ? "👤" : "⭐";
                    setText(roleIcon + " " + member.getUserName() + " (" + member.getRole() + ")");
                }
            }
        });

        // Format zobrazenia úloh v ListView
        tasksListView.setCellFactory(lv -> new javafx.scene.control.ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Vytvoríme custom zobrazenie pre úlohu
                    VBox taskBox = new VBox(5);

                    // Hlavný riadok - názov a stav
                    HBox headerBox = new HBox(10);
                    Label titleLabel = new Label(task.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                    Label statusLabel = new Label(getStatusEmoji(task.getStatus()) + " " + task.getStatus());
                    statusLabel.setStyle(getStatusStyle(task.getStatus()));

                    headerBox.getChildren().addAll(titleLabel, statusLabel);

                    // Detailný riadok - popis a dátum
                    HBox detailBox = new HBox(10);
                    Label descLabel = new Label(task.getDescription());
                    descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

                    Label dateLabel = new Label(formatDate(task.getDeadline()));
                    dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");

                    detailBox.getChildren().addAll(descLabel, dateLabel);

                    // Autor
                    Label authorLabel = new Label("Vytvoril: " + task.getCreatorName());
                    authorLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11; -fx-font-style: italic;");

                    taskBox.getChildren().addAll(headerBox, detailBox, authorLabel);
                    setGraphic(taskBox);
                }
            }
        });
    }

    private void setupResourcesTable() {
        resourceNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // Jednoduchšie zobrazenie typu
        resourceTypeColumn.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getType();
            String formattedType = "LINK".equals(type) ? "🔗 Odkaz" :
                    "FILE".equals(type) ? "📁 Súbor" :
                            "DOCUMENT".equals(type) ? "📄 Dokument" : type;
            return new SimpleStringProperty(formattedType);
        });

        resourceUploaderColumn.setCellValueFactory(new PropertyValueFactory<>("uploaderName"));
        resourceDateColumn.setCellValueFactory(new PropertyValueFactory<>("uploadedAt"));

        // Formátovanie dátumu
        resourceDateColumn.setCellFactory(column -> new TableCell<Resource, LocalDateTime>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        // Context menu pre resources
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Zmazať materiál");

        deleteItem.setOnAction(e -> handleDeleteResource());

        contextMenu.getItems().addAll(deleteItem);
        resourcesTableView.setContextMenu(contextMenu);

        // ZJEDNODUŠENÉ: Dvojklik zobrazí informácie o materiáli (bez otvárania prehliadača)
        resourcesTableView.setRowFactory(tv -> {
            TableRow<Resource> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Resource resource = row.getItem();
                    String message;
                    if ("LINK".equals(resource.getType())) {
                        message = "Odkaz: " + resource.getPathOrUrl() +
                                "\nSkopírujte si tento odkaz do prehliadača.";
                    } else {
                        message = "Súbor: " + resource.getTitle() +
                                "\nTyp: " + resource.getType();
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Informácie o materiáli", message);
                }
            });
            return row;
        });
    }

    private void setupTaskFilters() {
        ToggleGroup filterGroup = new ToggleGroup();

        String[] filters = {"ALL", "OPEN", "IN_PROGRESS", "DONE"};
        String[] filterLabels = {"📋 Všetky", "🆕 Otvorené", "🔄 Prebieha", "✅ Dokončené"};

        for (int i = 0; i < filters.length; i++) {
            ToggleButton button = new ToggleButton(filterLabels[i]);
            button.setToggleGroup(filterGroup);
            button.setUserData(filters[i]);
            button.setOnAction(e -> {
                currentTaskFilter = (String) button.getUserData();
                loadGroupTasks();
            });
            taskFilterButtons.getChildren().add(button);

            // Nastav "ALL" ako predvolený
            if ("ALL".equals(filters[i])) {
                button.setSelected(true);
            }
        }
    }

    private void loadGroupData() {
        if (currentGroup != null) {
            groupNameLabel.setText(currentGroup.getName());
            groupDescriptionLabel.setText(currentGroup.getDescription());
            memberCountLabel.setText("Počet členov: " + currentGroup.getMemberCount());

            // Group info
            groupInfoArea.setText(
                    "Názov: " + currentGroup.getName() + "\n" +
                            "Popis: " + currentGroup.getDescription() + "\n" +
                            "Členov: " + currentGroup.getMemberCount() + "\n" +
                            "Vytvorená: " + currentGroup.getCreatedAt().toLocalDate() + "\n" +
                            "ID skupiny: " + currentGroup.getGroupId()
            );

            loadGroupMembers();
            loadGroupTasks();
            loadGroupResources();
        }
    }

    private void loadGroupMembers() {
        try {
            List<MembershipResponse> members = apiService.getGroupMembers(currentGroup.getGroupId());
            membersListView.getItems().clear();
            membersListView.getItems().addAll(members);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa načítať členov: " + e.getMessage());
        }
    }

    private void loadGroupTasks() {
        try {
            List<Task> tasks = apiService.getGroupTasks(currentGroup.getGroupId());
            tasksListView.getItems().clear();

            // Filtrovanie úloh
            for (Task task : tasks) {
                if ("ALL".equals(currentTaskFilter) || currentTaskFilter.equals(task.getStatus())) {
                    tasksListView.getItems().add(task);
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa načítať úlohy: " + e.getMessage());
        }
    }

    private void loadGroupResources() {
        try {
            List<Resource> resources = resourceService.getGroupResources(currentGroup.getGroupId());
            resourcesTableView.getItems().clear();
            resourcesTableView.getItems().addAll(resources);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa načítať materiály: " + e.getMessage());
        }
    }

    // FILE UPLOAD FUNCTIONALITY
    @FXML
    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vyberte súbor na nahranie");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Všetky súbory", "*.*"),
                new FileChooser.ExtensionFilter("PDF súbory", "*.pdf"),
                new FileChooser.ExtensionFilter("Obrázky", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Dokumenty", "*.doc", "*.docx", "*.txt")
        );

        File selectedFile = fileChooser.showOpenDialog(resourcesTableView.getScene().getWindow());
        if (selectedFile == null) return;

        // Dialóg pre názov materiálu
        TextInputDialog titleDialog = new TextInputDialog(selectedFile.getName());
        titleDialog.setTitle("Názov materiálu");
        titleDialog.setHeaderText("Zadajte názov pre materiál");
        titleDialog.setContentText("Názov:");

        Optional<String> titleResult = titleDialog.showAndWait();
        if (titleResult.isEmpty() || titleResult.get().trim().isEmpty()) {
            return;
        }

        try {
            // Upload súboru
            Resource uploadedResource = resourceService.uploadFileToGroup(
                    currentGroup.getGroupId(),
                    selectedFile,
                    titleResult.get().trim()
            );

            showAlert(Alert.AlertType.INFORMATION, "Úspech",
                    "Súbor '" + uploadedResource.getTitle() + "' bol úspešne nahraný!");

            loadGroupResources(); // Refresh zoznamu

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa nahrať súbor: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddLink() {
        TextInputDialog linkDialog = new TextInputDialog();
        linkDialog.setTitle("Pridať odkaz");
        linkDialog.setHeaderText("Zadajte URL odkaz");
        linkDialog.setContentText("URL:");

        Optional<String> linkResult = linkDialog.showAndWait();
        if (linkResult.isEmpty() || linkResult.get().trim().isEmpty()) {
            return;
        }

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Názov odkazu");
        titleDialog.setHeaderText("Zadajte názov pre odkaz");
        titleDialog.setContentText("Názov:");

        Optional<String> titleResult = titleDialog.showAndWait();
        if (titleResult.isEmpty() || titleResult.get().trim().isEmpty()) {
            return;
        }

        try {
            Resource createdResource = resourceService.createResourceLink(
                    currentGroup.getGroupId(),
                    titleResult.get().trim(),
                    linkResult.get().trim()
            );

            showAlert(Alert.AlertType.INFORMATION, "Úspech",
                    "Odkaz '" + createdResource.getTitle() + "' bol pridaný!");

            loadGroupResources(); // Refresh zoznamu

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa pridať odkaz: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteResource() {
        Resource selectedResource = resourcesTableView.getSelectionModel().getSelectedItem();
        if (selectedResource == null) {
            showAlert(Alert.AlertType.WARNING, "Upozornenie", "Vyberte materiál pre zmazanie");
            return;
        }

        // Kontrola oprávnení - iba uploader alebo admin môže mazať
        if (!selectedResource.getUploadedBy().equals(currentUser.getUserId())) {
            showAlert(Alert.AlertType.WARNING, "Oprávnenie",
                    "Môžete mazať iba svoje vlastné materiály");
            return;
        }

        ChoiceDialog<String> confirmDialog = new ChoiceDialog<>("Nie", "Áno", "Nie");
        confirmDialog.setTitle("Potvrdiť zmazanie");
        confirmDialog.setHeaderText("Zmazať materiál: " + selectedResource.getTitle());
        confirmDialog.setContentText("Naozaj chcete zmazať tento materiál?");

        Optional<String> confirmResult = confirmDialog.showAndWait();
        if (confirmResult.isEmpty() || "Nie".equals(confirmResult.get())) {
            return;
        }

        try {
            resourceService.deleteResource(currentGroup.getGroupId(), selectedResource.getResourceId());
            showAlert(Alert.AlertType.INFORMATION, "Úspech", "Materiál bol zmazaný!");
            loadGroupResources(); // Refresh zoznamu

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa zmazať materiál: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshResources() {
        loadGroupResources();
        showAlert(Alert.AlertType.INFORMATION, "Obnovené", "Zoznam materiálov bol obnovený");
    }

    @FXML
    private void handleRefreshAll() {
        loadGroupMembers();
        loadGroupTasks();
        loadGroupResources();
        showAlert(Alert.AlertType.INFORMATION, "Obnovené", "Všetky údaje boli obnovené");
    }

    // PÔVODNÉ METÓDY PRE GROUP MANAGEMENT
    @FXML
    private void handleAddMember() {
        // Dialóg na zadanie userId
        TextInputDialog userIdDialog = new TextInputDialog();
        userIdDialog.setTitle("Pridať člena");
        userIdDialog.setHeaderText("Pridať člena do skupiny: " + currentGroup.getName());
        userIdDialog.setContentText("Zadajte ID používateľa:");

        Optional<String> userIdResult = userIdDialog.showAndWait();
        if (userIdResult.isEmpty() || userIdResult.get().trim().isEmpty()) {
            return;
        }

        // Overenie či používateľ existuje
        try {
            Long userId = Long.parseLong(userIdResult.get().trim());
            UserResponse userToAdd = apiService.getUserById(userId);

            // Dialóg na potvrdenie
            ChoiceDialog<String> confirmDialog = new ChoiceDialog<>("Áno", "Áno", "Nie");
            confirmDialog.setTitle("Potvrdiť pridanie");
            confirmDialog.setHeaderText("Pridať používateľa: " + userToAdd.getName() + " (" + userToAdd.getEmail() + ")");
            confirmDialog.setContentText("Potvrdiť pridanie:");

            Optional<String> confirmResult = confirmDialog.showAndWait();
            if (confirmResult.isEmpty() || "Nie".equals(confirmResult.get())) {
                return;
            }

            // Dialóg na výber role
            ChoiceDialog<String> roleDialog = new ChoiceDialog<>("MEMBER", "MEMBER", "ADMIN");
            roleDialog.setTitle("Vyberte rolu");
            roleDialog.setHeaderText("Rola pre používateľa v skupine");
            roleDialog.setContentText("Vyberte rolu:");

            Optional<String> roleResult = roleDialog.showAndWait();
            if (roleResult.isEmpty()) {
                return;
            }

            // Pridanie člena
            MembershipResponse membership = apiService.addMemberToGroup(
                    currentGroup.getGroupId(),
                    userId,
                    roleResult.get()
            );

            showAlert(Alert.AlertType.INFORMATION, "Úspech",
                    "Používateľ " + membership.getUserName() + " bol pridaný do skupiny ako " + membership.getRole() + "!");

            // Refresh zoznamu členov
            loadGroupMembers();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Zadajte platné ID používateľa (číslo)");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa pridať člena: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateTask() {
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Vytvoriť úlohu");
        titleDialog.setHeaderText("Zadajte názov úlohy");
        titleDialog.setContentText("Názov:");

        Optional<String> titleResult = titleDialog.showAndWait();
        if (titleResult.isEmpty() || titleResult.get().trim().isEmpty()) {
            return;
        }

        TextInputDialog descDialog = new TextInputDialog();
        descDialog.setTitle("Vytvoriť úlohu");
        descDialog.setHeaderText("Zadajte popis úlohy");
        descDialog.setContentText("Popis:");

        Optional<String> descResult = descDialog.showAndWait();
        if (descResult.isEmpty()) {
            return;
        }

        // Zjednodušený deadline - aktuálny dátum + 7 dní
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);

        try {
            Task newTask = apiService.createTask(
                    currentGroup.getGroupId(),
                    titleResult.get().trim(),
                    descResult.get().trim(),
                    deadline,
                    currentUser.getUserId()
            );

            showAlert(Alert.AlertType.INFORMATION, "Úspech",
                    "Úloha '" + newTask.getTitle() + "' bola vytvorená!");
            loadGroupTasks(); // Refresh zoznamu úloh

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa vytvoriť úlohu: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateTaskStatus() {
        Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "Upozornenie", "Vyberte úlohu pre zmenu stavu");
            return;
        }

        ChoiceDialog<String> statusDialog = new ChoiceDialog<>(
                selectedTask.getStatus(), "OPEN", "IN_PROGRESS", "DONE"
        );
        statusDialog.setTitle("Zmeniť stav úlohy");
        statusDialog.setHeaderText("Zmeniť stav úlohy: " + selectedTask.getTitle());
        statusDialog.setContentText("Vyberte nový stav:");

        Optional<String> statusResult = statusDialog.showAndWait();
        if (statusResult.isEmpty()) {
            return;
        }

        try {
            Task updatedTask = apiService.updateTaskStatus(
                    currentGroup.getGroupId(),
                    selectedTask.getTaskId(),
                    statusResult.get()
            );

            showAlert(Alert.AlertType.INFORMATION, "Úspech",
                    "Stav úlohy bol zmenený na: " + updatedTask.getStatus());
            loadGroupTasks(); // Refresh zoznamu

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa zmeniť stav úlohy: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "Upozornenie", "Vyberte úlohu pre zmazanie");
            return;
        }

        ChoiceDialog<String> confirmDialog = new ChoiceDialog<>("Nie", "Áno", "Nie");
        confirmDialog.setTitle("Potvrdiť zmazanie");
        confirmDialog.setHeaderText("Zmazať úlohu: " + selectedTask.getTitle());
        confirmDialog.setContentText("Naozaj chcete zmazať túto úlohu?");

        Optional<String> confirmResult = confirmDialog.showAndWait();
        if (confirmResult.isEmpty() || "Nie".equals(confirmResult.get())) {
            return;
        }

        try {
            apiService.deleteTask(currentGroup.getGroupId(), selectedTask.getTaskId());

            showAlert(Alert.AlertType.INFORMATION, "Úspech", "Úloha bola zmazaná!");
            loadGroupTasks(); // Refresh zoznamu

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa zmazať úlohu: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshMembers() {
        loadGroupMembers();
        showAlert(Alert.AlertType.INFORMATION, "Obnovené", "Zoznam členov bol obnovený");
    }

    @FXML
    private void handleRefreshTasks() {
        loadGroupTasks();
        showAlert(Alert.AlertType.INFORMATION, "Obnovené", "Zoznam úloh bol obnovený");
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/dashboard.fxml"));
            Stage stage = (Stage) groupNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa vrátiť na dashboard");
        }
    }

    // Pomocné metódy pre formátovanie
    private String getStatusEmoji(String status) {
        switch (status) {
            case "OPEN": return "🆕";
            case "IN_PROGRESS": return "🔄";
            case "DONE": return "✅";
            default: return "📝";
        }
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "OPEN": return "-fx-text-fill: #2196F3; -fx-font-size: 12; -fx-font-weight: bold;";
            case "IN_PROGRESS": return "-fx-text-fill: #FF9800; -fx-font-size: 12; -fx-font-weight: bold;";
            case "DONE": return "-fx-text-fill: #4CAF50; -fx-font-size: 12; -fx-font-weight: bold;";
            default: return "-fx-text-fill: #666; -fx-font-size: 12;";
        }
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "Bez termínu";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return "Do: " + date.format(formatter);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleViewAnalytics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/analytics.fxml"));
            Parent root = loader.load();

            AnalyticsController controller = loader.getController();
            controller.setGroupId(currentGroup.getGroupId());

            Stage stage = new Stage();
            stage.setTitle("Štatistiky skupiny - " + currentGroup.getName());
            stage.setScene(new Scene(root, 1000, 800));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa otvoriť štatistiky: " + e.getMessage());
        }
    }


}