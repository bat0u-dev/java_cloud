package com.geekbrains.roganov.server;

import com.geekbrains.roganov.common.CommandRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class AuthorizeHandler extends ChannelInboundHandlerAdapter {
    boolean authorizationInProcess;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        try {
            String inCommand = ((CommandRequest)msg).getCommand();
            System.out.println(inCommand);
            if (msg instanceof CommandRequest) {//тут false, после нажатия на кнопку authorize и отсылки с клиента
                // Network.sendMsg(new CommandRequest("/authorize")) из метода sendAuthData(). Разобраться почему?!????
                if (((CommandRequest) msg).getCommand().equals("/authorize")) {
                    authorizationInProcess = true;
                } else if(((CommandRequest) msg).getCommand().equals("/update file list") && !authorizationInProcess){
                    ctx.writeAndFlush(new CommandRequest("/update file list"));//либо отказаться от отдельного обработчика
                    //для обработки авторизации, либо прокидывать всё, кроме сообщения об авторизации дальше по конвееру!
                    //Решить, как лучше сделать!
                }
            }
            if (authorizationInProcess) {
                ctx.writeAndFlush(new CommandRequest("/authOK"));
                authorizationInProcess = false;
            }
//        }
//        finally {
//            ReferenceCountUtil.release(msg);
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
