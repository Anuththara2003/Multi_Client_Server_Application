package lk.ijse.gdse71.groupchat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lk.ijse.gdse71.groupchat.Client.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

public class ServerController implements Initializable {

    @FXML
    private Button btnSendSever;

    @FXML
    private TextArea txtAreaServerChatDisplay;

    @FXML
    private TextField txtClientName;

    ArrayList<Client> clients = new ArrayList<>();
    private ServerSocket serverSocket;
    private String massage = "";
    private Client client;





    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        new Thread(() -> {
            System.out.println("server thread");

            try {
                serverSocket = new ServerSocket(5000);
                while (true) {
                    Socket socket = serverSocket.accept();
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    client = new Client();
                    client.setDataInputStream(dataInputStream);
                    client.setDataOutputStream(dataOutputStream);
                    client.setSocket(socket);
                    client.setClientName(txtClientName.getText());
                    clients.add(client);

                    handleClient(client);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();


    }

    private void handleClient(Client client) {
        new Thread(() -> {
            try {
              DataInputStream dataInputStream = client.getDataInputStream();
              while (true) {
                  massage = dataInputStream.readUTF();

                  if (massage.startsWith("IMAGE:")){

                      String imageName = massage.substring(6);
                      int length = dataInputStream.readInt();
                      byte[] imageData = new byte[length];
                      dataInputStream.readFully(imageData);
                      brotcastImage(imageName, imageData, client);

                  } else if (massage.startsWith("FILE:")){

                      String fileName = massage.substring(5);
                      int length = dataInputStream.readInt();
                      byte[] fileData = new byte[length];
                      dataInputStream.readFully(fileData);
                      brotcastFile(fileName, fileData, client);
                  }else {
                      System.out.println("Received Massage : " + massage);
                      brotcast(massage , client);
                  }

                  if (massage.equals("Finished")) {
                      break;
                  }
              }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                synchronized (clients) {
                    clients.remove(client);
                }
                try {
                    client.getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void brotcastImage(String imageName, byte[] imageData, Client sender) {
        synchronized (clients) {
            for (Client c : clients) {
                if (c != sender) {
                    try {
                        c.getDataOutputStream().writeUTF("IMAGE:" + sender.getClientName() + ":" + imageName);
                        c.getDataOutputStream().writeInt(imageData.length);
                        c.getDataOutputStream().write(imageData);
                        c.getDataOutputStream().flush();
                        System.out.println("Image sent to: " + c.getClientName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void brotcastFile(String fileName, byte[] fileData, Client sender) {
        synchronized (clients) {
            for (Client c : clients) {
                if (c != sender) {
                    try {
                        c.getDataOutputStream().writeUTF("FILE:"+ sender.getClientName() + ":"+ fileName);
                        c.getDataOutputStream().writeInt(fileData.length);
                        c.getDataOutputStream().write(fileData);
                        c.getDataOutputStream().flush();
                        System.out.println("File sent to: " + c.getClientName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void brotcast(String massage, Client sender) {
        synchronized (clients) {
            for (Client c : clients) {
                if (c != sender) {
                    try {
                        c.getDataOutputStream().writeUTF(sender.getClientName() + ": " +massage);
                        System.out.println("sender name : " + sender.getClientName());
                        c.getDataOutputStream().flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }

    }

    @FXML
    void btnAddNewClientONAction(ActionEvent event) throws IOException {
        txtAreaServerChatDisplay.appendText(txtClientName.getText() + " joined the chat\n");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Client.fxml"));
        Parent parent = loader.load();
        ClientController clientController = loader.getController();
        clientController.lblClientName.setText(txtClientName.getText());

        Stage stage = new Stage();
        stage.setTitle(txtClientName.getText());
        stage.setScene(new Scene(parent));
        stage.show();
        txtClientName.clear();
    }


}
