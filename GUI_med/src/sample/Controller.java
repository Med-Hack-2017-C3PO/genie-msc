package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;

import utils.UIUtils;


import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;


public class Controller extends Application {

    private final String LAST_INPUT_DIR = "last_input_dir";

    @FXML
    public AnchorPane mainPage;
    @FXML
    private Button submit;
    @FXML
    private Button deleteAll;
    @FXML
    private TextField txtNextError;
    @FXML
    private TextField txtPreviousError;
    @FXML
    private Button btnOpenFile;
    @FXML
    private TextField txtFilePath;

    @FXML
    private FlowPane scrollContainer;
    @FXML
    private ScrollPane scrollPanel;
    private boolean isPlaying;
    private boolean isCollapsed;
    private Timeline timeline;
    private long[] timeData;
    private int currentIndex;
    private List<Integer> errorNodeList;
    private int currentErrorIndex;
    private Timeline backGroundTask;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    if (isPlaying) {
                        handlePauseEvent();
                    }
                    UIUtils.showExceptionDialog(e);
                    e.printStackTrace();
                }
            });

            // Read fxml file and render UI.
            Parent root = FXMLLoader.load(getClass().getResource("/layout/main_layout.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setTitle("Drone tool");
            primaryStage.setScene(scene);
            //primaryStage.getIcons().add(new Image("/com/ntn/dronetool/image/logo_black.png"));
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

                @Override
                public void handle(WindowEvent event) {
                    ButtonType result = UIUtils.showConfirmDialog("Exit application?");
                    if (!ButtonType.OK.equals(result)) {
                        event.consume();
                    }
                    Platform.exit();
                }
            });
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        initForm();
    }

    /**
     * Initialize the original components
     */
    private void initForm() {
        // Change look n feel
        if (!System.getProperty("os.name").contains("Windows")) {
            setUserAgentStylesheet(STYLESHEET_CASPIAN); // Switches to "Caspian"
            //setUserAgentStylesheet(STYLESHEET_MODENA);  // Switches to "Modena"
        }

        // disable these items when init
        scrollPanel.setDisable(true);

        scrollContainer = new FlowPane();
        scrollContainer.setPadding(new Insets(5, 5, 5, 5));
        scrollContainer.setVgap(5);
        scrollContainer.setHgap(5);
        scrollContainer.setAlignment(Pos.TOP_LEFT);
        scrollContainer.setOrientation(Orientation.VERTICAL);
        scrollContainer.setStyle("-fx-background-color: gray;");


        scrollPanel.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);    // Horizontal scroll bar
        scrollPanel.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);    // Vertical scroll bar
        scrollPanel.setContent(scrollContainer);
    }






    /**
     * Handle for button open file
     *
     * @param event
     */
    @FXML
    private void handleBtnOpenFile(ActionEvent event) {
        String lastInputDir = Context.getInstance().getPrefs().get(LAST_INPUT_DIR, "");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Input Files", "*.txt", "*.csv", "*.TXT", "*.CSV"));
        if (!Objects.equals(lastInputDir, "")) fileChooser.setInitialDirectory(new File(lastInputDir));
        File file = fileChooser.showOpenDialog(btnOpenFile.getScene().getWindow());
        if (file != null) {
            Context.getInstance().getPrefs().put(LAST_INPUT_DIR, file.getParent());
            List<File> files = Collections.singletonList(file);
            printLog(txtFilePath, files);

            UIUtils.getStage().getScene().setCursor(Cursor.WAIT);
            initAllData();
            UIUtils.getStage().getScene().setCursor(Cursor.DEFAULT);

            // enable these items when read data complete
            scrollPanel.setDisable(false);
            btnPlay.setDisable(false);
            btnNext.setDisable(false);
            btnPrevious.setDisable(false);
            btnCollapse.setDisable(false);
            if (errorNodeList != null && errorNodeList.size() > 0) {
                btnNextError.setDisable(false);
                currentErrorIndex = -1;
                txtNextError.setText(TimeUtils.convertEpochTime(timeData[errorNodeList.get(0)], TimeUtils.MMDDYYYYHHMMSS));
            }
            sldTime.setDisable(false);
            isCollapsed = false;
            btnCollapse.setText("Collapse all");
        }
    }

    /**
     * Handle collapse, expand all button event
     *
     * @param event
     */
    @FXML
    private void handleBtnCollapse(ActionEvent event) {
        if (isCollapsed) {
            for (Node node : scrollContainer.getChildren()) {
                if (node instanceof TitledPane) {
                    ((TitledPane) node).setExpanded(true);
                }
            }
            btnCollapse.setText("Collapse all");
            isCollapsed = false;
        } else {
            for (Node node : scrollContainer.getChildren()) {
                if (node instanceof TitledPane) {
                    ((TitledPane) node).setExpanded(false);
                }
            }
            btnCollapse.setText("Expand all");
            isCollapsed = true;
        }

    }

    /**
     * Handle jump to next error button event
     *
     * @param event
     */
    @FXML
    private void handleBtnJumpToNextError(ActionEvent event) {
        if (errorNodeList.isEmpty()) {
            return;
        }
        currentErrorIndex++;
        if (currentErrorIndex > 0) {
            btnPreviousError.setDisable(false);
            txtPreviousError.setText(TimeUtils.convertEpochTime(timeData[errorNodeList.get(currentErrorIndex - 1)], TimeUtils.MMDDYYYYHHMMSS));
        }
        sldTime.setValue(timeData[errorNodeList.get(currentErrorIndex)]);

        if (currentErrorIndex >= errorNodeList.size() - 1) {
            btnNextError.setDisable(true);
        } else {
            txtNextError.setText(TimeUtils.convertEpochTime(timeData[errorNodeList.get(currentErrorIndex + 1)], TimeUtils.MMDDYYYYHHMMSS));
        }
    }

    /**
     * Handle for button open file
     *
     * @param event
     */
    @FXML
    private void handleBtnPlay(ActionEvent event) {
        // Handle play event
        if (!isPlaying) {
            handlePlayEvent();
        } else {
            // Handle pause event
            handlePauseEvent();
        }
    }

    /**
     * Handle for button next
     *
     * @param event
     */
    @FXML
    private void handleBtnNext(ActionEvent event) {
        // Handle play event
        if (isPlaying) {
            // Handle pause event
            handlePauseEvent();
        }
        // go to next time
        if (currentIndex < this.timeData.length - 1) {
            currentIndex++;
            sldTime.setValue(timeData[currentIndex]);
        }

    }

    /**
     * Handle for button Previous
     *
     * @param event
     */
    @FXML
    private void handleBtnPrevious(ActionEvent event) {
        // Handle play event
        if (isPlaying) {
            // Handle pause event
            handlePauseEvent();
        }
        // go to previous time
        if (currentIndex > 0) {
            currentIndex--;
            sldTime.setValue(timeData[currentIndex]);
        }
    }

    /**
     * Handle for button Block Increment
     *
     * @param event
     */
    @FXML
    private void handleBtnBlockIncrement(ActionEvent event) {
        // Handle setting event
        if (isPlaying) {
            handlePauseEvent();
        }
        String result = UIUtils.showInputDialog("Change Slider block increment", "block increment",
                String.valueOf(Context.getInstance().getPrefs().getDouble(BLOCK_INCREMENT_NUM, 100)));
        if (StringUtils.isNumeric(result)) {
            sldTime.setBlockIncrement(Double.parseDouble(result));
            Context.getInstance().getPrefs().putDouble(BLOCK_INCREMENT_NUM, Double.valueOf(result));
        }
    }

    /**
     * Handle for button open simulator
     *
     * @param event
     */
    @FXML
    private void handleBtnOpenControlsSimulation(ActionEvent event) {
        if (SimulatorController.getInstance().isShowing()) {
            SimulatorController.getInstance().getPrimaryStage().requestFocus();
            return;
        }
        // Handle setting event
        Parent sub;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ntn/dronetool/layout/simulator_layout.fxml"));
            fxmlLoader.setController(SimulatorController.getInstance());
            sub = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Controls simulation");
            stage.setScene(new Scene(sub));
            SimulatorController.getInstance().setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle for button open map tracking
     *
     * @param event
     */
    @FXML
    private void handleBtnOpenMapTracking(ActionEvent event) {
        if (MapTrackingController.getInstance().isShowing()) {
            MapTrackingController.getInstance().getPrimaryStage().requestFocus();
            return;
        }
        // Handle setting event
        Parent sub;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ntn/dronetool/layout/map_tracking_layout.fxml"));
            fxmlLoader.setController(MapTrackingController.getInstance());
            sub = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Map tracking");
            stage.setScene(new Scene(sub));
            MapTrackingController.getInstance().setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBtnOpenArtifical(ActionEvent event) {
        if (AttitudeIndicatorController.getInstance().isShowing()) {
            AttitudeIndicatorController.getInstance().getPrimaryStage().requestFocus();
            return;
        }
        // Handle setting event
        Parent sub;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ntn/dronetool/layout/attitude_indicator_layout.fxml"));
            fxmlLoader.setController(AttitudeIndicatorController.getInstance());
            sub = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Artificial Horizon");
            stage.setScene(new Scene(sub));
            stage.setMaxHeight(400);
            stage.setMaxWidth(400);
            AttitudeIndicatorController.getInstance().setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle event when user press play button
     */
    private void handlePlayEvent() {
        btnOpenFile.setDisable(true);
        ImageView imgPlay = new ImageView("/com/ntn/dronetool/image/pause.png");
        btnPlay.setText("Pause");
        btnPlay.setGraphic(imgPlay);
        btnPlay.setContentDisplay(ContentDisplay.LEFT);

        // tick slider per second
        timeline = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                if (sldTime.getValue() < sldTime.getMax()) {
                    // last increment step
                    if ((sldTime.getMax() - sldTime.getValue()) < sldTime.getBlockIncrement()) {
                        sldTime.setValue(sldTime.getMax());
                    } else {
                        // normal increment step
                        sldTime.increment();
                    }
                } else {
                    handlePauseEvent();
                }
            }

        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        isPlaying = true;
    }

    /**
     * Handle event when user press pause button
     * or time value increase to max value
     */
    private void handlePauseEvent() {
        btnOpenFile.setDisable(false);
        ImageView imgPlay = new ImageView("/com/ntn/dronetool/image/play.png");
        btnPlay.setText("Play");
        btnPlay.setGraphic(imgPlay);
        btnPlay.setContentDisplay(ContentDisplay.LEFT);

        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isPlaying = false;
    }

    /**
     * Set file path to text field
     *
     * @param textField
     * @param files
     */
    private void printLog(TextField textField, List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        textField.clear();
        for (File file : files) {
            // single choice
            if (files.size() == 1) {
                textField.appendText(file.getAbsolutePath());
            } else {
                // multi choice
                textField.appendText(file.getAbsolutePath() + ",");
            }
        }
    }

    /**
     * Add group to UI
     *
     * @param columnGroupInfo
     * @param index
     */
    private void addGroupPanel(ColumnGroupInfo columnGroupInfo, int index) {
        // create TitledPane
        TitledPane titledPane = new TitledPane();
        titledPane.setId(columnGroupInfo.getName());
        titledPane.setText(columnGroupInfo.getName());

        // init content of TitledPane
        VBox content = new VBox();
        content.setStyle("-fx-background-color: " + columnGroupInfo.getColor() + ";");
        // get data of row at index
        List<Pair<String, String>> datas = Context.getInstance().droneDataEntity().getDataByColumnGroupInfo(columnGroupInfo, index);
        Label label;
        double threshold;
        // add all columns to panel
        for (Pair<String, String> data : datas) {
            if (data.getKey().equals("epoch")) {
                label = new Label(String.format("%s : %s", data.getKey(),
                        TimeUtils.convertEpochTime(Double.valueOf(data.getValue()).longValue(), TimeUtils.MMDDYYYYHHMMSS)));
            } else {
                if (columnGroupInfo.getName().contains("DEFAULT"))
                    label = new Label(String.format("%s : %s", data.getKey(), data.getValue()));
                else {
                    threshold = columnGroupInfo.getColumnNames().get(datas.indexOf(data)).getThreshold();
                    label = new Label(String.format("%s : %s ( %.1f )", data.getKey(), data.getValue(), threshold));
                    if (parseDouble(data.getValue()) > threshold)
                        content.setStyle("-fx-background-color: red;");
                }
            }
            label.setId(data.getKey());
            label.setTextFill(Color.WHITE);
            content.getChildren().add(label);
        }

        titledPane.setContent(content);

        // Set titledPane opened
        titledPane.setExpanded(true);
        scrollContainer.getChildren().add(titledPane);
    }

    private double parseDouble(String value) {
        if (StringUtils.isNumeric(value)) return Double.valueOf(value);
        return -1;
    }


    /**
     * Get index of approximately Time
     */
    private int getApproximatelyTimeItemIndex(long currentTime) {
        int index = FindClosestValueFromArray.find(timeData, currentTime);
        return index > 0 ? index : index * (-1);
    }

    /**
     * Jump to previous error
     *
     * @param actionEvent
     */
    @FXML
    private void handleBtnJumpToPreviousError(ActionEvent actionEvent) {
        if (errorNodeList.isEmpty()) {
            return;
        }
        currentErrorIndex--;
        btnNextError.setDisable(false);
        sldTime.setValue(timeData[errorNodeList.get(currentErrorIndex)]);

        if (currentErrorIndex <= 0) {
            btnPreviousError.setDisable(true);
        } else {
            txtPreviousError.setText(TimeUtils.convertEpochTime(timeData[errorNodeList.get(currentErrorIndex - 1)], TimeUtils.MMDDYYYYHHMMSS));
            txtNextError.setText(TimeUtils.convertEpochTime(timeData[errorNodeList.get(currentErrorIndex + 1)], TimeUtils.MMDDYYYYHHMMSS));
        }
    }

    @FXML
    private void handleBtnGroupsConfig(ActionEvent actionEvent) {
        if (GroupsConfigController.getInstance().isShowing()) {
            GroupsConfigController.getInstance().getPrimaryStage().requestFocus();
            return;
        }
        if (isPlaying) {
            handlePauseEvent();
        }

        // Handle setting event
        Parent sub;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ntn/dronetool/layout/groups_config_layout.fxml"));
            fxmlLoader.setController(GroupsConfigController.getInstance());
            sub = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Groups Config");
            stage.setScene(new Scene(sub));
            GroupsConfigController.getInstance().setStage(stage);
            stage.showAndWait();
            // re-init all data
            if(Context.getInstance().haveData()) {
                int currIndex = this.currentIndex;
                initAllData();
                // restore to previous index
                sldTime.setValue(timeData[currIndex]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}