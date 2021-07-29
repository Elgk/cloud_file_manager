package client;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.AbstractCommand;
import model.CommandType;

import java.io.IOException;

public class RegController {
    @FXML
    public Button btnReg;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextArea textArea;

    private  Controller controller;


    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void tryRegResult(String message, CommandType commandType){
        textArea.clear();
        if (commandType == CommandType.AUTH_OK){
            controller.btnLogIn.setManaged(false);
            controller.btnLogIn.setVisible(false);
            textArea.appendText(message +":  Connection is successful");
            controller.setTitle(message);
        }else {
            textArea.appendText(message);
        }

    }

    public void tryToReg(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        if (login.length()*password.length() != 0){
            controller.authentication(login, password);
        }
    }

}
