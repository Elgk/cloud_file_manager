package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.lang.Thread;


public class Controller implements Initializable {
    @FXML
    public ListView<String> clientList;
    @FXML
    public ListView<String> serverList;
    @FXML
    public TextField clientPath;
    @FXML
    public TextField serverPath;
    @FXML
    public Button btnLogIn;
    @FXML
    public Button btnServerUp;

    private RegController regController;
    private Stage regStage;
    private Stage stage;

    private Path currentDir;
    private ObjectEncoderOutputStream out;
    private ObjectDecoderInputStream in;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Platform.runLater(()->{stage = (Stage) clientList.getScene().getWindow();});

            String homeDir = System.getProperty("user.home");
            currentDir = Path.of(homeDir);
            System.out.println("current user: " + currentDir.toString());
            //   log.info("Current user {}", System.getProperty("user.home"));
            refreshClientList();
            addNavigationListeners();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshServerList(List<String> list){
        Platform.runLater(() ->{
            serverList.getItems().clear();
            serverList.getItems().addAll(list);
        });
    }

    private void refreshClientList() throws IOException {
        clientPath.setText(currentDir.toString());
        List<String> names = Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(()->{
            clientList.getItems().clear();
            clientList.getItems().addAll(names);
        });

    }

    public void addNavigationListeners(){
        clientList.setOnMouseClicked(e ->{
            if (e.getClickCount() == 2){
                String item = clientList.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                if (Files.isDirectory(newPath)){
                    currentDir = newPath;
                    try {
                        refreshClientList();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
        serverList.setOnMouseClicked(e ->{
            if (e.getClickCount() == 2){
                if (!serverList.getItems().isEmpty()){
                    String item = serverList.getSelectionModel().getSelectedItem();
                    try {
                        out.writeObject(new PathInRequest(item));
                        out.flush();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
    }

    private void connect(){
        try {
            Socket socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream());

            Thread readThread = new Thread(()->{
                    try {
                        while (true){
                            AbstractCommand command = (AbstractCommand) in.readObject();
                            switch (command.getType()){
                                case AUTH_OK:
                                    AuthOkResponse authOk = (AuthOkResponse) command;
                                    regController.tryRegResult(authOk.getNickname(), command.getType());
                                    break;
                                case AUTH_NO:
                                    AuthNoResponse authNo = (AuthNoResponse) command;
                                    regController.tryRegResult(authNo.getMessage(), command.getType()) ;
                                    break;
                                case LIST_MESSAGE:
                                    ListResponse response = (ListResponse) command;
                                    List<String> names = response.getNames();
                                    refreshServerList(names);
                                    break;
                                case PATH_RESPONSE:
                                    PathUpResponse pathUpResponse = (PathUpResponse) command;
                                    String newPath = pathUpResponse.getPath();
                                    Platform.runLater(()->{serverPath.setText(newPath);});
                                    break;
                                case FILE_MESSAGE:
                                    FileMessage file = (FileMessage) command;
                                    Files.write(currentDir.resolve(file.getName()), file.getData());
                                    refreshClientList();
                                    break;
                            }
                        }

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }



    public void downLoad(ActionEvent actionEvent) throws IOException {
        String file = serverList.getSelectionModel().getSelectedItem();
        out.writeObject(new FileRequest(file));
        out.flush();
    }

    public void upLoad(ActionEvent actionEvent) throws IOException {
        String file = clientList.getSelectionModel().getSelectedItem();
        Path path = currentDir.resolve(file);
        out.writeObject(new FileMessage(path));
        out.flush();
    }

    public void clientUp(ActionEvent actionEvent) throws IOException {
        currentDir = currentDir.getParent();
        refreshClientList();
        clientPath.setText(currentDir.toString());
    }

    public void serverUp(ActionEvent actionEvent) throws IOException {
        if (serverPath.getText().length() != 0){
            out.writeObject(new PathUpRequest());
            out.flush();
        }
    }

    public void authentication(String login, String password) throws IOException {
        connect();
        out.writeObject(new AuthRequest(login, password));
        out.flush();
    }

    private void initRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/regManager.fxml"));
            Parent root = fxmlLoader.load();
            regController = fxmlLoader.getController();
            regController.setController(this); /*// Controller установил ссылку на себя в контроллере RegController*/

            regStage = new Stage();

            regStage.setScene(new Scene(root, 450, 340));
            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegWindow(ActionEvent actionEvent) {
        if (regStage == null){
            initRegWindow();
        }
        regStage.setTitle("Connection to server");
        regStage.show();
    }

    public void setTitle(String nickname){
        Platform.runLater(()->{
            if (nickname.equals("")){
                stage.setTitle("Network Storage Manager");
            }else
                stage.setTitle(String.format("Network Storage Manager [%s]",nickname));
        });
    }

    public void deleteFile(ActionEvent actionEvent) throws IOException {

      if (serverList.getSelectionModel().getSelectedItems().isEmpty()) {
          Alert alert = new Alert(Alert.AlertType.WARNING, "No file is selected.", ButtonType.OK);
          alert.showAndWait();
      }else{
          String name = serverList.getSelectionModel().getSelectedItem();
          out.writeObject(new DeleteRequest(name));
          out.flush();
      }
    }
}
