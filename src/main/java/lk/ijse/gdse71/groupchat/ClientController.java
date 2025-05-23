package lk.ijse.gdse71.groupchat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.Date;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    private Button btnSendClient;

    @FXML
    private Button btnUpload;

    @FXML
    private TextArea txtAreaClientChatDisplay;

    @FXML
    private TextField txtFieldClientMessage;

    @FXML
    Label lblClientName;

    @FXML
    private Button btnImage;

    @FXML
    private VBox chatPain;


    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String massage = "";
    private String clientName = "";



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 5000);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                clientName = lblClientName.getText();
                System.out.println(clientName);

                while (!massage.equals("Bye")) {

                    massage = dataInputStream.readUTF();

                    if (massage.startsWith("IMAGE:")){

                        String[] parts = massage.split(":", 3);
                        String senderName = parts[1];
                        String imageName = parts[2];
                        int length = dataInputStream.readInt();
                        byte[] imageData = new byte[length];
                        dataInputStream.readFully(imageData);

                        Platform.runLater(() -> {
                            Label messageLabel = new Label(senderName + " sent image: " + imageName + "\n");
                            messageLabel.setFont(new Font(14));
                            chatPain.getChildren().add(messageLabel);

                            try {
                                Files.createDirectories(Paths.get("downloads"));
                                Files.write(Paths.get("downloads/" + imageName), imageData);


                                ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                                Image image = new Image(bis);
                                ImageView imageView = new ImageView(image);
                                imageView.setFitWidth(100);
                                imageView.setFitHeight(100);
                                chatPain.getChildren().add(imageView);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    } else if (massage.startsWith("FILE:")) {

                        String fileName = massage.substring(5);
                        int length = dataInputStream.readInt();
                        byte[] fileData = new byte[length];
                        dataInputStream.readFully(fileData);

                        Platform.runLater(() -> {
                            txtAreaClientChatDisplay.appendText("Received file: " + fileName + "\n");
                            try {
                                Files.write(Paths.get("downloads" + fileName), fileData);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }else{
                        Platform.runLater(() -> {
                            Label messageLabel = new Label(massage);
                            messageLabel.setFont(new Font(14));
                            chatPain.getChildren().add(messageLabel);
                        });
                    }

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @FXML
    void btnSendOnAction(ActionEvent event) {
        String sendingMassage = txtFieldClientMessage.getText();
        try {
            dataOutputStream.writeUTF(sendingMassage);
            dataOutputStream.flush();
            txtFieldClientMessage.clear();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void btnUploadOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                dataOutputStream.writeUTF("FILE:" + file.getName());
                dataOutputStream.flush();
                dataOutputStream.writeInt(fileData.length);
                dataOutputStream.write(fileData);
                dataOutputStream.flush();
                System.out.println("File sent: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @FXML
    void btnImageOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] imageData = Files.readAllBytes(file.toPath());
                dataOutputStream.writeUTF("IMAGE:" + file.getName());
                dataOutputStream.writeInt(imageData.length);
                dataOutputStream.write(imageData);
                dataOutputStream.flush();
                System.out.println("Image Sent: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


