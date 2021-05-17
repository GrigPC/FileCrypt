package filecryptBase;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {
    String password = null;
    File destination = null; //destination file / path
    List<File> filesToCrypt = null; //for multiple files

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("FileCrypter");
        final FileChooser fileChooser = new FileChooser();
        final Button openMultipleButton = new Button("Open file(s)");
        final Button setDestination = new Button("Set destination");
        final Button encrypt = new Button("Encrypt");
        final Button decrypt = new Button("Decrypt");
        final TextField keyInput = new TextField();
        keyInput.setPromptText("Enter password/key");

        HBox hbox = new HBox();
        HBox hbox1 = new HBox();
        TextFlow logs = new TextFlow();

        //TODO: Improve interface
        hbox.setSpacing(10);
        hbox1.setSpacing(10);
        hbox.getChildren().addAll(openMultipleButton, setDestination, encrypt, decrypt);
        hbox1.getChildren().addAll(keyInput);

        openMultipleButton.setMinSize(100, 50);
        setDestination.setMinSize(100, 50);
        encrypt.setMinSize(100, 50);
        decrypt.setMinSize(100, 50);
        keyInput.setPrefSize(430, 0);
        logs.setPrefSize(430, 400);

        final Pane rootgroup = new VBox(12);
        rootgroup.setPrefSize(440, 400);
        rootgroup.setMaxSize(440, 700);
        rootgroup.getChildren().addAll(hbox, hbox1, keyInput, new Text("Logs:\n"), logs);
        rootgroup.setPadding(new Insets(12, 12, 12, 12));

        openMultipleButton.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    filesToCrypt = fileChooser.showOpenMultipleDialog(primaryStage);
                    if (filesToCrypt != null) {
                        if (filesToCrypt.size() > 10) {
                            logs.getChildren().addAll(new Text("Maximum of 10 files allowed!\n"));
                            return;
                        }
                        logs.getChildren().clear();
                        Text filesSelected = new Text("Files selected:\n");
                        filesSelected.setStyle("-fx-font-size: 20px;");
                        logs.getChildren().addAll(filesSelected);
                        for (int i = 0; i < filesToCrypt.size(); i++) {
                            File file = filesToCrypt.get(i);
                            Text fileName = new Text("File " + (i+1) + ": " + file.getName() + "\n");
                            logs.getChildren().addAll(fileName);
                        }
                    }
                }
            }
        );


        DirectoryChooser dirChooser = new DirectoryChooser();
        setDestination.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        destination = dirChooser.showDialog(primaryStage);
                        if (destination != null) {
                            Text destNotification = new Text("Selected directory: " + destination.getAbsolutePath() + '\n');
                            logs.getChildren().addAll(destNotification);
                        }
                    }
                }
        );

        encrypt.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        password = keyInput.getText();
                        if (filesToCrypt != null && destination != null && password.length() > 0) {
                            try {
                                String basePath = destination.getAbsolutePath();
                                for (int i = 0; i < filesToCrypt.size(); i++) {
                                    destination = new File(basePath + "/File" + i + getExtension(filesToCrypt.get(i)));
                                    cryptFile(filesToCrypt.get(i), destination, password, false);
                                    if (!destination.exists()) {
                                        logs.getChildren().addAll(new Text("Error encountered encrypting File" + i + '\n'));
                                    }
                                }
                                logs.getChildren().addAll(new Text("Encryption completed.\n"));
                                //reset state
                                filesToCrypt = null;
                                password = null;
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                logs.getChildren().addAll(new Text("An error has occurred.\n"));
                            }
                        }
                        else {
                            if (filesToCrypt == null) {
                                logs.getChildren().addAll(new Text("Please select a file to encrypt!\n"));
                            }
                            else if (destination == null) {
                                logs.getChildren().addAll(new Text("Please select a destination!\n"));
                            }
                            else if (password == null || password.equals("")) {
                                logs.getChildren().addAll(new Text("Please input a password of at least one character!\n"));
                            }
                        }
                    }
                }
        );

        decrypt.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        password = keyInput.getText();
                        if (filesToCrypt != null && destination != null && password.length() > 0) {
                            try {
                                String basePath = destination.getAbsolutePath();
                                for (int i = 0; i < filesToCrypt.size(); i++) {
                                    destination = new File(basePath + "/File" + i + getExtension(filesToCrypt.get(i)));
                                    cryptFile(filesToCrypt.get(i), destination, password, true);
                                    if (!destination.exists()) {
                                        logs.getChildren().addAll(new Text("Error encountered encrypting File" + i + '\n'));
                                    }
                                }
                                logs.getChildren().addAll(new Text("Decryption completed.\n"));
                                filesToCrypt = null;
                                password = null;
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                logs.getChildren().addAll(new Text("Decryption failed. Please make sure the file selected and your key match!\n"));
                            }
                        }
                        else {
                            if (filesToCrypt == null) {
                                logs.getChildren().addAll(new Text("Please select a file to encrypt!\n"));
                            }
                            else if (destination == null) {
                                logs.getChildren().addAll(new Text("Please select a destination!\n"));
                            }
                            else if (password == null || password.equals("")) {
                                logs.getChildren().addAll(new Text("Please input a password of at least one character!\n"));
                            }
                        }
                    }
                }
        );

        primaryStage.setScene(new Scene(rootgroup));
        primaryStage.show();
    }

    //MODE = 0 FOR ENCRYPTION, 1 FOR DECRYPTION
    private void cryptFile(File file, File destination, String password, boolean mode) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] decodedData = fis.readAllBytes();
        byte[] encodedData;

        if (!mode) {
            encodedData = crypter.encryptData(decodedData, password);
        }
        else {
            encodedData = crypter.decryptData(decodedData, password);
        }

        FileOutputStream fos = new FileOutputStream(destination);
        fos.write(encodedData);
    }

    private String getExtension(File file) {
        Pattern regx = Pattern.compile("\\.[0-9a-z]+$");
        Matcher matcher = regx.matcher(file.getAbsolutePath());
        if (matcher.find()) {
            return matcher.group(0);
        }
        //if no extension found
        else {
            System.out.println("No extension found!");
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
