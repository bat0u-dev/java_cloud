package com.geekbrains.roganov.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerFilesList extends AbstractMessage {

    private static final long serialVersionUID = -3789042922485174520L;

    String storagePath;
    ArrayList<String> list = new ArrayList<>();

    public ArrayList<String> getList() {
        return list;
    }

    public ServerFilesList(String storagePath) throws Exception {
        this.storagePath = storagePath;
        List<Path> pathList;
        try {
            pathList = Files.list(Paths.get(storagePath))
                    .map(Path::getFileName)
                    .collect(Collectors.toList());
            for (Path p :
                    pathList) {
                list.add(p.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


