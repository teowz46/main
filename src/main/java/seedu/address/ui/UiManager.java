package seedu.address.ui;

import static seedu.address.logic.parser.CliSyntax.FLAG_CHECKED_IN_GUEST;
import static seedu.address.logic.parser.CliSyntax.FLAG_GUEST;
import static seedu.address.logic.parser.CliSyntax.FLAG_ROOM;

import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seedu.address.MainApp;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.storage.DataSavingExceptionEvent;
import seedu.address.commons.events.ui.DeselectGuestListEvent;
import seedu.address.commons.events.ui.ListingChangedEvent;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.model.UserPrefs;

/**
 * The manager of the UI component.
 */
public class UiManager extends ComponentManager implements Ui {

    public static final String ALERT_DIALOG_PANE_FIELD_ID = "alertDialogPane";

    public static final String FILE_OPS_ERROR_DIALOG_STAGE_TITLE = "File Op Error";
    public static final String FILE_OPS_ERROR_DIALOG_HEADER_MESSAGE = "Could not save data";
    public static final String FILE_OPS_ERROR_DIALOG_CONTENT_MESSAGE = "Could not save data to file";

    private static final Logger logger = LogsCenter.getLogger(UiManager.class);
    private static final String ICON_APPLICATION = "/images/concierge_icon.png";

    private Logic logic;
    private Config config;
    private UserPrefs prefs;
    private MainWindow mainWindow;

    public UiManager(Logic logic, Config config, UserPrefs prefs) {
        super();
        this.logic = logic;
        this.config = config;
        this.prefs = prefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting UI...");

        //Set the application icon.
        primaryStage.getIcons().add(getImage(ICON_APPLICATION));

        try {
            mainWindow = new MainWindow(primaryStage, config, prefs, logic);
            mainWindow.show(); //This should be called before creating other UI parts
            mainWindow.fillInnerParts();

        } catch (Throwable e) {
            logger.severe(StringUtil.getDetails(e));
            showFatalErrorDialogAndShutdown("Fatal error during initializing", e);
        }
    }

    @Override
    public void stop() {
        prefs.updateLastUsedGuiSetting(mainWindow.getCurrentGuiSetting());
        mainWindow.hide();
    }

    private void showFileOperationAlertAndWait(String description, String details, Throwable cause) {
        final String content = details + ":\n" + cause.toString();
        showAlertDialogAndWait(AlertType.ERROR, FILE_OPS_ERROR_DIALOG_STAGE_TITLE, description, content);
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    void showAlertDialogAndWait(Alert.AlertType type, String title, String headerText, String contentText) {
        showAlertDialogAndWait(mainWindow.getPrimaryStage(), type, title, headerText, contentText);
    }

    /**
     * Shows an alert dialog on {@code owner} with the given parameters.
     * This method only returns after the user has closed the alert dialog.
     */
    private static void showAlertDialogAndWait(Stage owner, AlertType type, String title, String headerText,
                                               String contentText) {
        final Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.showAndWait();
    }

    /**
     * Shows an error alert dialog with {@code title} and error message, {@code e},
     * and exits the application after the user has closed the alert dialog.
     */
    private void showFatalErrorDialogAndShutdown(String title, Throwable e) {
        logger.severe(title + " " + e.getMessage() + StringUtil.getDetails(e));
        showAlertDialogAndWait(Alert.AlertType.ERROR, title, e.getMessage(), e.toString());
        Platform.exit();
        System.exit(1);
    }

    //==================== Event Handling Code ===============================================================

    @Subscribe
    private void handleDataSavingExceptionEvent(DataSavingExceptionEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        showFileOperationAlertAndWait(FILE_OPS_ERROR_DIALOG_HEADER_MESSAGE, FILE_OPS_ERROR_DIALOG_CONTENT_MESSAGE,
                event.exception);
    }

    @Subscribe
    private void handleListingChangeEvent(ListingChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));

        if (event.getFlag().equals(FLAG_GUEST)) {
            setDisplayGuestList();
            showGuestList();
            showGuestDetailedPanel();
            mainWindow.clearGuestSelection();
        } else if (event.getFlag().equals(FLAG_ROOM)) {
            showRoomList();
            showRoomDetailedPanel();
            mainWindow.clearRoomSelection();
        } else if (event.getFlag().equals(FLAG_CHECKED_IN_GUEST)) {
            setDisplayCheckedInGuestList();
            showGuestList();
            showGuestDetailedPanel();
            mainWindow.clearGuestSelection();
        }
    }

    @Subscribe
    private void handleDeselectGuestListEvent(DeselectGuestListEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        mainWindow.clearGuestSelection();
    }

    //==================== UI Visibility Functions ============================================================

    private void showGuestList() {
        mainWindow.showGuestList();
    }

    private void setDisplayCheckedInGuestList() {
        mainWindow.setGuestListPanelDisplayCheckedInGuestList();
    }

    private void setDisplayGuestList() {
        mainWindow.setGuestListPanelDisplayGuestList();
    }

    private void showRoomList() {
        mainWindow.showRoomList();
    }

    private void showGuestDetailedPanel() {
        mainWindow.showGuestDetailedPanel();
    }

    private void showRoomDetailedPanel() {
        mainWindow.showRoomDetailedPanel();
    }
}
