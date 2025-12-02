package sk.front.front.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sk.front.front.model.GroupAnalyticsDTO;
import sk.front.front.model.MemberActivityDTO;
import sk.front.front.service.AnalyticsService;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class AnalyticsController implements Initializable {

    @FXML private Label groupNameLabel;
    @FXML private Label monthLabel;
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label totalResourcesLabel;
    @FXML private Label avgCompletionTimeLabel;

    @FXML private VBox taskChartContainer;
    @FXML private VBox resourceChartContainer;
    @FXML private VBox memberChartContainer;

    @FXML private TableView<MemberActivityDTO> memberActivityTable;
    @FXML private TableColumn<MemberActivityDTO, String> memberNameColumn;
    @FXML private TableColumn<MemberActivityDTO, String> memberRoleColumn;
    @FXML private TableColumn<MemberActivityDTO, Long> tasksCreatedColumn;
    @FXML private TableColumn<MemberActivityDTO, Long> tasksCompletedColumn;
    @FXML private TableColumn<MemberActivityDTO, Long> resourcesColumn;
    @FXML private TableColumn<MemberActivityDTO, Integer> activityScoreColumn;

    private AnalyticsService analyticsService = new AnalyticsService();
    private Long groupId;

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
        loadAnalytics();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMemberActivityTable();
    }

    private void setupMemberActivityTable() {
        memberNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        memberRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        tasksCreatedColumn.setCellValueFactory(new PropertyValueFactory<>("tasksCreated"));
        tasksCompletedColumn.setCellValueFactory(new PropertyValueFactory<>("tasksCompleted"));
        resourcesColumn.setCellValueFactory(new PropertyValueFactory<>("resourcesUploaded"));
        activityScoreColumn.setCellValueFactory(new PropertyValueFactory<>("activityScore"));

        // Formátovanie stĺpcov
        memberRoleColumn.setCellFactory(column -> new TableCell<MemberActivityDTO, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                } else {
                    setText(role);
                    if ("ADMIN".equals(role)) {
                        setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2196F3;");
                    }
                }
            }
        });
    }

    private void loadAnalytics() {
        try {
            GroupAnalyticsDTO analytics = analyticsService.getGroupAnalytics(groupId);
            updateUI(analytics);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Chyba", "Nepodarilo sa načítať štatistiky: " + e.getMessage());
        }
    }

    private void updateUI(GroupAnalyticsDTO analytics) {
        // Základné štatistiky
        monthLabel.setText("Mesačná štatistika: " + analytics.getMonth());
        totalTasksLabel.setText(String.valueOf(analytics.getTotalTasks()));
        completedTasksLabel.setText(String.valueOf(analytics.getCompletedTasks()));
        completionRateLabel.setText(String.format("%.1f%%", analytics.getCompletionRate()));
        totalResourcesLabel.setText(String.valueOf(analytics.getTotalResources()));

        // Graf stavov úloh
        createTaskStatusChart(analytics);

        // Graf typov materiálov
        createResourceTypeChart(analytics);

        // Graf aktivity členov
        createMemberActivityChart(analytics);

        // Tabuľka aktivity členov
        ObservableList<MemberActivityDTO> memberData = FXCollections.observableArrayList(
                analytics.getMemberActivities()
        );
        memberActivityTable.setItems(memberData);
    }

    private void createTaskStatusChart(GroupAnalyticsDTO analytics) {
        taskChartContainer.getChildren().clear();

        PieChart taskChart = new PieChart();
        taskChart.setTitle("Stav úloh v skupine");
        taskChart.setLegendSide(Side.RIGHT);
        taskChart.setLabelsVisible(true);

        PieChart.Data openData = new PieChart.Data("Otvorené", analytics.getOpenTasks());
        PieChart.Data inProgressData = new PieChart.Data("Prebieha", analytics.getInProgressTasks());
        PieChart.Data completedData = new PieChart.Data("Dokončené", analytics.getCompletedTasks());

        taskChart.getData().addAll(openData, inProgressData, completedData);

        // Farba pre jednotlivé sekcie
        if (completedData.getNode() != null) {
            completedData.getNode().setStyle("-fx-pie-color: #4CAF50;");
        }
        if (inProgressData.getNode() != null) {
            inProgressData.getNode().setStyle("-fx-pie-color: #FF9800;");
        }
        if (openData.getNode() != null) {
            openData.getNode().setStyle("-fx-pie-color: #2196F3;");
        }

        taskChartContainer.getChildren().add(taskChart);
    }

    private void createResourceTypeChart(GroupAnalyticsDTO analytics) {
        resourceChartContainer.getChildren().clear();

        if (analytics.getResourceTypeStats() == null || analytics.getResourceTypeStats().isEmpty()) {
            Label noDataLabel = new Label("Žiadne materiály na zobrazenie");
            noDataLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            resourceChartContainer.getChildren().add(noDataLabel);
            return;
        }

        BarChart<String, Number> barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        barChart.setTitle("Typy materiálov");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<String, Long> entry : analytics.getResourceTypeStats().entrySet()) {
            String type = entry.getKey();
            String displayType = type;

            switch (type) {
                case "FILE": displayType = "📁 Súbory"; break;
                case "LINK": displayType = "🔗 Odkazy"; break;
                case "DOCUMENT": displayType = "📄 Dokumenty"; break;
            }

            series.getData().add(new XYChart.Data<>(displayType, entry.getValue()));
        }

        barChart.getData().add(series);

        resourceChartContainer.getChildren().add(barChart);
    }

    private void createMemberActivityChart(GroupAnalyticsDTO analytics) {
        memberChartContainer.getChildren().clear();

        if (analytics.getMemberActivities() == null || analytics.getMemberActivities().isEmpty()) {
            return;
        }

        // Zobrazíme len top 5 najaktívnejších členov
        int limit = Math.min(5, analytics.getMemberActivities().size());

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Skóre aktivity");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Top " + limit + " najaktívnejších členov");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int i = 0; i < limit; i++) {
            MemberActivityDTO member = analytics.getMemberActivities().get(i);
            series.getData().add(new XYChart.Data<>(member.getUserName(), member.getActivityScore()));
        }

        barChart.getData().add(series);

        memberChartContainer.getChildren().add(barChart);
    }

    @FXML
    private void handleRefresh() {
        loadAnalytics();
    }

    @FXML
    private void handleBack() {
        try {
            // Vráť sa späť na dashboard
            Parent root = FXMLLoader.load(getClass().getResource("/view/dashboard.fxml"));
            Stage stage = (Stage) groupNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}