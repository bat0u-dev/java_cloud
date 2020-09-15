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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> filesListLocal;

    @FXML
    ListView<String> filesListServer;

    AbstractMessage am;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    clickToChooseFileListener();
                    am = Network.readObject();
                    clickToChooseFileListener();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("cloud_client\\src\\main\\java\\client_storage\\" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);

                    } else if(am instanceof ServerFilesList){
                        ArrayList<String> serverList = ((ServerFilesList) am).getList();
                        refreshServerFilesList(serverList);
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
        filesListLocal.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if(click.getClickCount() == 2) {
                    tfFileName.setText(filesListLocal.getSelectionModel().getSelectedItem());
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
            filesListLocal.getItems().clear();
                Network.sendMsg(new CommandRequest("/update file list"));
        });
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

    public void refreshServerFilesList(ArrayList<String> serverList){
        updateUI(()->{

            filesListServer.getItems().clear();
            for (String value: serverList) {
                filesListServer.getItems().add(value);
                if(!Files.exists(Paths.get("cloud_client\\src\\main\\java\\client_storage\\" + value)))
                {
                    try {
                        Files.createFile(Paths.get("cloud_client\\src\\main\\java\\client_storage\\" + value));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
