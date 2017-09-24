package utils;

import com.sun.javafx.stage.StageHelper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("restriction")
public class UIUtils {
    private static Dialog<Void> dialog;

    public static Stage getStage() {return StageHelper.getStages().get(0);}

    /**
     * Show loading dialog
     */

    public static void showLoading(){
        dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(UIUtils.getStage());
        dialog.initStyle(StageStyle.TRANSPARENT);
        Label loader = new Label("Loading...");
        loader.setContentDisplay(ContentDisplay.LEFT);
        loader.setGraphic(new ProgressIndicator());
        dialog.getDialogPane().setGraphic(loader);
        DropShadow ds = new DropShadow();
        ds.setOffsetX(1.3);
        ds.setOffsetY(1.3);
        ds.setColor(Color.DARKGRAY);
        dialog.getDialogPane().setEffect(ds);
        dialog.show();
    }

    /**
     * Hide loading dialog
     */

    public static void hideLoading(){
        if(dialog != null && dialog.isShowing()){
            dialog.hide();
            dialog = null;
        }
    }

    /**
     *
     */

    public static ButtonType showConfirmDialog(String mes){
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(mes);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get();
    }

    /**
     *
     */
    public static ButtonType showWarningDialog (String mes){
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("warning");
        alert.setHeaderText(null);
        alert.setContentText(mes);

        Optional<ButtonType> result = alert.showAndWait();
        return result.get();
    }
    /**
     * Show exception dialog
     *
     * @param e
     * @return OK/Cancel
     */
    public static void showExceptionDialog(Throwable e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Exception has occurred!");
        alert.setHeaderText("Please handle this exception");
        alert.setContentText(e.getMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.show();
    }
    /**
     * Show input dialog
     *
     * @param mes
     * @param itemName
     * @param defaultVal
     * @return input value
     */
    public static String showInputDialog(String mes, String itemName, String defaultVal) {
        String text = "";
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(mes);
        dialog.setHeaderText("Change " + itemName + " value?");
        dialog.setContentText("Please enter " + itemName + ": ");
        dialog.getEditor().setText(defaultVal);

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            text = Objects.equals(result.get(), "") ? defaultVal : result.get();
        }
        return Objects.equals(text, "") ? defaultVal : text;
    }
    public static String showInputColorDialog(String mes, String itemName, String defaultVal) {
        String text = "";
        Dialog dialog = new Dialog();
        dialog.setTitle(mes);
        dialog.setHeaderText("Please select " + itemName + ": ");

        // Set the button types.
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        TextField color = new TextField();
        color.setPromptText("Color");
        color.setText(defaultVal);

        final ColorPicker colorPicker = new ColorPicker();
        try {
            colorPicker.setValue(Color.valueOf(defaultVal));
        } catch (Exception e) {
            colorPicker.setValue(Color.RED);
        }
        colorPicker.setOnAction((ActionEvent t) -> {
            color.setText("#" + Integer.toHexString(colorPicker.getValue().hashCode()));
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Color:"), 0, 0);
        grid.add(color, 1, 0);
        grid.add(colorPicker, 2, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return color.getText();
            }
            return defaultVal;
        });

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            text = result.get();
        }
        try {
            Color.valueOf(text);
        } catch (Exception e) {
            text = "red";
        }
        return text;
    }



}
