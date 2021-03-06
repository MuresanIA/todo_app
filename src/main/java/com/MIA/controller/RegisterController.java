package com.MIA.controller;

import com.MIA.model.User;
import com.MIA.repository.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import utils.Caesar;

@Component
@FxmlView("register.fxml")
public class RegisterController {
    public AnchorPane registerLayout;
    public TextField txtFieldUsernameRegister;
    public Button btnRegister;
    public PasswordField pwdFieldRegister;
    public PasswordField pwdFieldConfirmRegister;
    public Label lblInformationRegister;
    public Button btnPreviousScene;
    public Label registerText;
    public Label usernameLabel;
    public Label passwordLabel;
    public Label confirmPasswordLabel;
    public Label emptyUserName;
    public Label emptyPasswordRegister;
    public Label emptyPasswordConfirmRegister;
    public Label userNameTaken;
    public Label passwordsDontMatch;

    @Autowired
    private UserRepository userRepository;

    @FXML
    public void goBack(ActionEvent event)  {
        Stage stageTheEventSourceNodeBelongs = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stageTheEventSourceNodeBelongs.setScene(getWelcomeScene());
    }

    public Scene getWelcomeScene() {
        Parent root = ApplicationContextSingleton.createContextFromResource("welcome.fxml");
        return new Scene(root, 600, 600);
    }

    @FXML
    private void registerUser(ActionEvent actionEvent) {
        clearErrorMessages();
        //TODO: Clear errors Register button is pressed!
        if (txtFieldUsernameRegister.getText().equals("")) {
            emptyUserName.setText("Username is empty!");
            return;
        }
        if (pwdFieldRegister.getText().equals("")) {
            emptyPasswordRegister.setText("Password field is empty!");
            return;
        }
        if (pwdFieldConfirmRegister.getText().equals("")) {
            emptyPasswordConfirmRegister.setText("Password field is empty!");
            return;
        }
        if (userRepository.findByUsername(txtFieldUsernameRegister.getText()) != null) {
            userNameTaken.setText("Username's already taken!");
            return;
        }

        if (!pwdFieldRegister.getText().equals(pwdFieldConfirmRegister.getText())) {
            passwordsDontMatch.setText("Passwords don't match!");
            return;
        }

        User user = new User();
        user.setUsername(txtFieldUsernameRegister.getText());
        user.setPassword(Caesar.encrypt(pwdFieldRegister.getText(), 3, 3)); // encrypting the password :)
        if (user.getUsername().contains("admin")) {
            user.setAdmin(true);
        }
        userRepository.save(user);
        if (userRepository.findByUsername(user.getUsername()) != null) {
            txtFieldUsernameRegister.setText("");
            pwdFieldRegister.setText("");
            pwdFieldConfirmRegister.setText("");
            updateInfoText("Username registered successfully!");
        } else {
            updateInfoText("Registration Failed!");
        }
    }

    private void clearErrorMessages() {
        emptyUserName.setText("");
        emptyPasswordRegister.setText("");
        emptyPasswordConfirmRegister.setText("");
        userNameTaken.setText("");
        passwordsDontMatch.setText("");
    }

    private void updateInfoText(String message) {
        lblInformationRegister.setText(message);
    }


}
