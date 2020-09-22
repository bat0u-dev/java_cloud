package com.geekbrains.roganov.server;

import com.geekbrains.roganov.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class MainHandler extends ChannelInboundHandlerAdapter {
    boolean getFileMessage;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\" + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\" + fr.getFilename()));
                    ctx.writeAndFlush(fm);
                }
            } else if(msg instanceof CommandRequest){
                if(((CommandRequest) msg).getCommand().equals("/update file list")){
                    ServerFilesList currList = new ServerFilesList("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\");
                    ctx.writeAndFlush(currList);
                } else if(((CommandRequest)msg).getCommand().equals("/upload")){
                    getFileMessage = true;
                }
            }

            if(msg instanceof FileMessage && getFileMessage){
                Files.write(Paths.get("cloud_server\\src\\main\\java\\com\\geekbrains\\roganov\\server\\server_storage\\"
                        + ((FileMessage) msg).getFilename()),((FileMessage) msg).getData(),StandardOpenOption.CREATE);
            }
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
