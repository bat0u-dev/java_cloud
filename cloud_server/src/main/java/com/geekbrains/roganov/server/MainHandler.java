package com.geekbrains.roganov.server;

import com.geekbrains.roganov.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {
    boolean getFileMessage;
    String username;
    DBConnector connector = new DBConnector();


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof AuthorizationData){
                username = connector.getUserNameByLogAndPass(((AuthorizationData) msg).getLogin(),((AuthorizationData) msg).getPassword());
            } else if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\" + username + "\\" + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\" + username + "\\" + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            } else if (msg instanceof CommandRequest) {
                switch (((CommandRequest) msg).getCommand()) {
//                    case "/authorize"://вынесено в отдельный handler
//                        authorizationProcessed = true;
//                        break;
                    case "/update file list":
                        ServerFilesList currList = new ServerFilesList("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\" + username);
                        ctx.writeAndFlush(currList);
                        break;
                    case "/upload":
                        getFileMessage = true;
                        break;
                    case "/stopUpload":
                        getFileMessage = false;
                        break;
                }
            } else if (getFileMessage) {
                if(msg instanceof FileMessage) {
                    Files.write(Paths.get("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\" + username + "\\"
                            + ((FileMessage) msg).getFilename()), ((FileMessage) msg).getData(), StandardOpenOption.CREATE);
                }
            }
//            else if(authorizationProcessed){//вынесемно в отдельный handler
//                if(msg instanceof AuthorizationData){
////                    String userName = DBConnector.getUserNameByLogAndPass(((AuthorizationData) msg).getLogin()
////                            ,((AuthorizationData) msg).getPassword());
////                    if(!userName.equals("")){
//                        ctx.writeAndFlush("/authOK");
////                    }
//                }
//            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
