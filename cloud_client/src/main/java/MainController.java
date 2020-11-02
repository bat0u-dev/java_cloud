import com.geekbrains.roganov.common.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    HBox authPanel, mainUIPanel;

    @FXML
    Label authInfo;

    @FXML
    ButtonBar btnBar;

    @FXML
    TextField loginField;

    @FXML
    TextField passwordField;

    @FXML
    Button connectBtn;

    @FXML
    Button btnExit;

    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> filesListLocal;

    @FXML
    ListView<String> filesListServer;

    private AbstractMessage am;
    private boolean isAuthorized;//надо добавить кнопку выхода из клиента и сообщение при вводе некоректных данных аторизации.


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startSession();
    }

    public void startSession() {
        Network.start();
        setAuthorized(false);
        filesListLocal.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filesListServer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refreshLocalFilesList();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    am = Network.readObject();
                    if (am instanceof CommandRequest) {
                        if (((CommandRequest) am).getCommand().equals("/authOK")) {
                            isAuthorized = true;
                            setAuthorized(true);
                            Network.sendMsg(new CommandRequest("/update file list"));
                            break;
                        }
                    }
                }
                while (Network.sessionRun) {
                    am = Network.readObject();
                    clickToChooseFileListener();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert fileExistsAlert = new Alert(Alert.AlertType.CONFIRMATION, "File " + fm.getFilename()
                                        + " already exists in client storage. Do you want to replace it?", ButtonType.OK, ButtonType.CANCEL);
                                if (Files.exists(Paths.get("cloud_client\\src\\main\\java\\client_storage\\" + fm.getFilename()))) {
                                    fileExistsAlert.getModality();
                                    fileExistsAlert.showAndWait();
                                    if (fileExistsAlert.getResult() == ButtonType.OK) {
                                        try {
                                            Files.write(Paths.get("cloud_client\\src\\main\\java\\client_storage\\" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    try {
                                        Files.write(Paths.get("cloud_client\\src\\main\\java\\client_storage\\" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } else if (am instanceof ServerFilesList) {
                        ArrayList<String> serverList = ((ServerFilesList) am).getList();
                        refreshServerFilesList(serverList);
                        refreshLocalFilesList();
                    }
                }
            } finally {
                Network.stop();
                Network.sessionRun = false;
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (isAuthorized) {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            mainUIPanel.setVisible(true);
            mainUIPanel.setManaged(true);
            btnBar.setVisible(true);
            btnBar.setManaged(true);
        } else {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            mainUIPanel.setVisible(false);
            mainUIPanel.setManaged(false);
            btnBar.setVisible(false);
            btnBar.setManaged(false);
        }
    }

    public void sendAuthData() {
        if(!isAuthorized) {
            authInfo.setTextFill(Paint.valueOf("RED"));
            authInfo.setText("Неправильный логин или пароль");
        }
        if (!Network.sessionRun) {
            startSession();
        }
        Network.sendMsg(new AuthorizationData(loginField.getText(), passwordField.getText()));
        loginField.clear();
        passwordField.clear();
    }

    public void clickToChooseFileListener() {
        filesListServer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    tfFileName.setText(filesListServer.getSelectionModel().getSelectedItem());
                }
            }
        });
        filesListLocal.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    tfFileName.setText(filesListLocal.getSelectionModel().getSelectedItem());
                }
            }
        });
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        ObservableList<String> fileNamesList = filesListServer.getSelectionModel().getSelectedItems();
        for (String fileName : fileNamesList) {
            if (!fileName.equals("")) {
                Network.sendMsg(new FileRequest(fileName));
                tfFileName.clear();
                Network.sendMsg(new CommandRequest("/update file list"));
                refreshLocalFilesList();
            }
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {

        Network.sendMsg(new CommandRequest("/upload"));
        ObservableList<String> fileNamesList = filesListLocal.getSelectionModel().getSelectedItems();
        for (String fileName : fileNamesList) {
            try {
                if (!fileName.equals("")) {
                    Network.sendMsg(new FileMessage(Paths.get("cloud_client\\src\\main\\java\\client_storage\\" + fileName)));
                }//Сделать по аналогии с alert о замене существующего файла на сервере! Можно добавить respond класс
                // ответ от сервера, что такой файл уже есть и перезаписывать при согласии пользователя
                // (на сервере boolean флаг isReplaced делать true, и добавить в условие Files.write())
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Network.sendMsg(new CommandRequest("/stopUpload"));//необходим ли маркер?
        // Или будет работать и без него по умолчанию?!Проверить!
        tfFileName.clear();
        Network.sendMsg(new CommandRequest("/update file list"));
        refreshLocalFilesList();
    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            filesListLocal.getItems().clear();
            try {
                Files.list(Paths.get("cloud_client\\src\\main\\java\\client_storage\\"))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> filesListLocal.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void getServerFilesList(ActionEvent actionEvent) {
        updateUI(() -> {
            filesListLocal.getItems().clear();
            Network.sendMsg(new CommandRequest("/update file list"));
        });
    }

    public void refreshServerFilesList(ArrayList<String> serverList) {
        updateUI(() -> {
            filesListServer.getItems().clear();
            for (String value : serverList) {
                filesListServer.getItems().add(value);
                if (!Files.exists(Paths.get("cloud_client\\src\\main\\java\\client_storage\\" + value))) {
                    filesListLocal.getItems().add(value);
                }
            }
        });
    }

    public void DelFromLocalStorage(ActionEvent actionEvent) {
        String deletedFileName = filesListLocal.getSelectionModel().getSelectedItem();
        try {
            if (deletedFileName != null) {
                Files.delete(Paths.get("cloud_client\\src\\main\\java\\client_storage\\" + deletedFileName));
                refreshLocalFilesList();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void exit(ActionEvent actionEvent) {
        Network.stop();
        Network.sessionRun = false;
        isAuthorized = false;
        setAuthorized(false);
        authInfo.setTextFill(Paint.valueOf("BLACK"));
        authInfo.setText("Пожалуйста авторизуйтесь");
    }
}
