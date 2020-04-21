package com.MIA.controller;

import com.MIA.model.Task;
import com.MIA.model.User;
import com.MIA.repository.TaskRepository;
import com.MIA.repository.UserRepository;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import utils.Caesar;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.event.ChangeEvent;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;


public class Controller {

    @FXML
    private Button btnAddTodo;
    @FXML
    private VBox vBox;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnRegister;
    @FXML
    private MenuItem menuFileClose;
    @FXML
    private Label lblUsername;
    @FXML
    private Button btnShowPassword;
    @FXML
    private TextField txtFieldPasswordShow;
    @FXML
    private TextField txtFieldUsernameRegister;
    @FXML
    private PasswordField pwdFieldRegister;
    @FXML
    private PasswordField pwdFieldConfirmRegister;
    @FXML
    private TextField txtFieldUsernameLogin;
    @FXML
    private PasswordField pwdFieldLogin;
    @FXML
    private Label lblInformationLogin;
    @FXML
    private Label lblInformationRegister;
    @FXML
    private MenuItem menuItemLogin;

    @FXML
    private MenuItem mnuItemRegister;

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane registerLayout;
    @FXML
    private AnchorPane loginLayout;

    private User loginUser;

    private UserRepository userRepository;

    private TaskRepository taskRepository;

    private boolean isConnectionSuccessful = true;

    private Tab todoTab;
    private Tab loginTab;
    private Tab registerTab;

    public Controller() {
    }

    public void initialize() {
        try {
            persistenceConnection();

        } catch (Exception ex) {
            System.out.println("Connection is not allowed");
            System.out.println(ex.toString());
            isConnectionSuccessful = false;
        }
    }

    private void persistenceConnection() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("TODOFx");

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        userRepository = new UserRepository(entityManager);
        taskRepository = new TaskRepository(entityManager);
    }


    @FXML
    private void registerUser(ActionEvent actionEvent) {
        clearInfoText();

        if (txtFieldUsernameRegister.getText().equals("")) {
            updateInfoText("Username's empty. Please fill in!");
            return;
        }
        if (pwdFieldRegister.getText().equals("")) {
            updateInfoText("Password field is empty. Please fill in!");
            return;
        }
        if (pwdFieldConfirmRegister.getText().equals("")) {
            updateInfoText("Password field is empty. Please fill in!");
            return;
        }

        if (userRepository.usernameAlreadyInDB(txtFieldUsernameRegister.getText())) {
            updateInfoText("Username's already taken!");

            User user = userRepository.findByUsername(txtFieldUsernameRegister.getText());
            List<Task> tasks = user.getTasks();
            return;
        }

        if (!pwdFieldRegister.getText().equals(pwdFieldConfirmRegister.getText())) {
            updateInfoText("Passwords don't match!");
            return;
        }

        User user = new User();
        user.setUsername(txtFieldUsernameRegister.getText());
        user.setPassword(Caesar.encrypt(pwdFieldRegister.getText(), 3, 3)); // encrypting the password :)

        userRepository.save(user);

        if (userRepository.usernameAlreadyInDB(user.getUsername())) {
            txtFieldUsernameRegister.setText("");
            pwdFieldRegister.setText("");
            pwdFieldConfirmRegister.setText("");
            updateInfoText("Username registered successfully!");


        } else {
            updateInfoText("Registration Failed!");
        }
    }


    private void clearInfoText() {
        updateInfoText("");
    }

    private void updateInfoText(String message) {
        lblInformationRegister.setText(message);
    }

    @FXML
    private void loginUser(ActionEvent actionEvent) {
        User user = userRepository.findByUsername(txtFieldUsernameLogin.getText());
        if (user == null) {
            lblInformationLogin.setText("Invalid username!");
            return;
        }
        if (!Caesar.encrypt(user.getPassword(), 23, 7).equals(pwdFieldLogin.getText())) {
            lblInformationLogin.setText("Wrong password!");
            return;
        }


        lblInformationLogin.setText("Login successful");
        toggleTodoTab();
        populateTodoLayout(user.getTasks());
        loginUser = user; // save the login user
    }
    public void populateTodoLayout(List<Task> tasks) {
        vBox.getChildren().clear();
        final ScrollPane scrollPane1 = new ScrollPane();
        final VBox vbox = new VBox();
        int i = 1;
//        Collections.sort(tasks, new Comparator<Task>() {
//            public int compare(Task o1, Task o2) {
//                return o1.getCreatedAt().compareTo(o2.getCreatedAt());
//            }
//        }); Is anulate deoarece am folosit @OrderBy("created_at ASC") in User!
        for (final Task task : tasks) {
            CheckBox checkBox = new CheckBox(i + ". " + task.getDescription());
            checkBox.setSelected(!task.isInProgress());
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    task.setInProgress(!newValue);
                    taskRepository.save(task);
                    String ssound = Paths.get("pencil.mp3").toUri().toString();
                    Media sound = new Media(ssound);
                    MediaPlayer mediaPlayer = new MediaPlayer(sound);
                    mediaPlayer.play();
                }
            });
            vbox.getChildren().add(checkBox);
            i++;
        }
        final HBox hbox = new HBox();
        final Label label = new Label();
        label.getStyleClass().add("warning");
        label.setText("Please fill in a To Do :)");
        Button addTodoButton = new Button("Add To Do");
        final TextField textField = new TextField();
        addTodoButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (textField.getText().equals("")) {
                    if (!hbox.getChildren().contains(label))
                        hbox.getChildren().add(label); //Daca field-ul de To Do este empty atunci afiseaza "Empty field" iar daca nu adauga un todo!
                } else {
                    addTodo(event, textField.getText());
                }
            }
        });
        hbox.getChildren().add(addTodoButton);
        hbox.getChildren().add(textField);
        vBox.getChildren().add(hbox);
        scrollPane1.setContent(vbox);
        vBox.getChildren().add(scrollPane1);
    }

    public void toggleTodoTab() {
        todoTab = createTodoTab();
        tabPane.getTabs().add(todoTab);
    }

    public Tab createTodoTab() {
        Tab todoTab = new Tab();
        todoTab.setText("To Do");
        vBox.setVisible(true);
        todoTab.setContent(vBox);

        return todoTab;
    }
// encrypt


    public void showPassword(ActionEvent actionEvent) {
        if (!txtFieldPasswordShow.isVisible()) {
            btnShowPassword.setText("Hide");
            txtFieldPasswordShow.setText(pwdFieldLogin.getText());
            txtFieldPasswordShow.setEditable(false);
            txtFieldPasswordShow.setVisible(true);
            pwdFieldLogin.setVisible(false);
        } else {
            btnShowPassword.setText("Show");
            txtFieldPasswordShow.setVisible(false);
            pwdFieldLogin.setVisible(true);
        }
    }

    public void closeApp(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void toggleLoginTab(ActionEvent actionEvent) {
        if (!loginLayout.isVisible()) {
            loginTab = createLoginTab();
            menuItemLogin.setText("Hide Login");
            tabPane.getTabs().add(loginTab);
        } else {
            loginLayout.setVisible(false);
            menuItemLogin.setText("Show Login");
            tabPane.getTabs().remove(loginTab);
        }
    }


    public Tab createLoginTab() {
        Tab loginTab = new Tab();
        loginTab.setText("Login");
        loginTab.setContent(loginLayout);
        loginLayout.setVisible(true);

        return loginTab;
    }

    public void toggleRegisterTab(ActionEvent actionEvent) {
        if (!registerLayout.isVisible()) {
            registerTab = createRegisterTab();
            mnuItemRegister.setText("Hide Register");
            tabPane.getTabs().add(registerTab);

        } else {
            registerLayout.setVisible(false);
            mnuItemRegister.setText("Show Register");
            tabPane.getTabs().remove(registerTab);
        }
    }

    public Tab createRegisterTab() {
        Tab registerTab = new Tab();
        registerTab.setText("Register");
        registerTab.setContent(registerLayout);
        registerLayout.setVisible(true);

        return registerTab;
    }

    public void addTaskToUser() {

    }

    public void addTodo(ActionEvent actionEvent, String description) {
        Task task = new Task();
        task.setCreatedAt(new Date());
        task.setDescription(description);
        task.setInProgress(true);
        task.setUser(loginUser);
        taskRepository.save(task);

        User user = userRepository.findByUsername(loginUser.getUsername());
        populateTodoLayout(user.getTasks());
    }
}
