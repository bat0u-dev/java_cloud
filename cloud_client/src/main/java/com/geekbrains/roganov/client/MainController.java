package com.geekbrains.roganov.client;

import com.geekbrains.roganov.common.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> filesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    clickToChooseFileListener();
                    AbstractMessage am = Network.readObject();
                    clickToChooseFileListener();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("com\\geekbrains\\roganov\\client\\client_storage" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    } else if(am instanceof ServerFilesList){
                        ArrayList<String> serverList = ((ServerFilesList) am).getList();
                        for (String value: serverList) {
                            Files.createFile(Paths.get("com\\geekbrains\\roganov\\client\\client_storage" + value));
                        }
                        refreshLocalFilesList();
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
    }

    public void clickToChooseFileListener(){
        filesList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if(click.getClickCount() == 2) {
                    tfFileName.setText(filesList.getSelectionModel().getSelectedItem());
                }
            }
        });
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(tfFileName.getText()));
            tfFileName.clear();
        }
    }

    public void getServerFilesList(ActionEvent actionEvent) {//доделать обновление серверного списка файлов на клиенте
        updateUI(() -> {
                filesList.getItems().clear();
                Network.sendMsg(new CommandRequest("/update file list"));
        });
    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("com\\geekbrains\\roganov\\client\\client_storage\\"))
                        .map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }


}
