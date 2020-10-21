package com.geekbrains.roganov.server;

import com.geekbrains.roganov.common.AuthorizationData;
import com.geekbrains.roganov.common.CommandRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AuthorizeHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        try {
//            String inCommand = ((CommandRequest)msg).getCommand();
//            System.out.println(inCommand);
//        if (msg instanceof CommandRequest) {//тут false, после нажатия на кнопку authorize и отсылки с клиента
//            // Network.sendMsg(new CommandRequest("/authorize")) из метода sendAuthData(). Разобраться почему?!????
//            if (((CommandRequest) msg).getCommand().equals("/authorize")) {
//                authorizationInProcess = true;
//            } else {
//                ctx.fireChannelRead(msg);
//            }
////                    if(((CommandRequest) msg).getCommand().equals("/update file list") && !authorizationInProcess){
////                    ctx.writeAndFlush(new CommandRequest("/update file list"));//либо отказаться от отдельного обработчика
//            //для обработки авторизации, либо прокидывать всё, кроме сообщения об авторизации дальше по конвееру!
//            //Решить, как лучше сделать!
//            if (authorizationInProcess) {
//                ctx.writeAndFlush(new CommandRequest("/authOK"));
//                authorizationInProcess = false;
//            }
//        } else {
//            ctx.fireChannelRead(msg);
//        }
        if (msg instanceof AuthorizationData) {//тут false, после нажатия на кнопку authorize и отсылки с клиента
            // Network.sendMsg(new CommandRequest("/authorize")) из метода sendAuthData(). Разобраться почему?!????

//                    if(((CommandRequest) msg).getCommand().equals("/update file list") && !authorizationInProcess){
//                    ctx.writeAndFlush(new CommandRequest("/update file list"));//либо отказаться от отдельного обработчика
            //для обработки авторизации, либо прокидывать всё, кроме сообщения об авторизации дальше по конвееру!
            //Решить, как лучше сделать!
            DBConnector connector = new DBConnector();
            connector.connectToDB();
            String username = connector.getUserNameByLogAndPass(((AuthorizationData) msg).getLogin(), ((AuthorizationData) msg).getPassword());
            if (!username.equals("Incorrect authorization data.")) {
                ctx.writeAndFlush(new CommandRequest("/authOK"));
                ctx.fireChannelRead(msg);
                if(!Files.exists(Paths.get("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\" + username)))
                {
                    Files.createDirectory(Paths.get("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\" + username));
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }

    }

//        }
//        finally {
//            ReferenceCountUtil.release(msg);
//        }
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
