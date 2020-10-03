package com.geekbrains.roganov.server;

import com.geekbrains.roganov.common.CommandRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthorizeHandler extends ChannelInboundHandlerAdapter {
    boolean authorizationProcessed;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof CommandRequest){//тут false, после нажатия на кнопку authorize и отсылки с клиента
            // Network.sendMsg(new CommandRequest("/authorize")) из метода sendAuthData(). Разобраться почему?!????
            if(((CommandRequest) msg).getCommand().equals("/authorize")){
                authorizationProcessed = true;
            }
        }
       if(authorizationProcessed){
           ctx.writeAndFlush("/authOK");
       }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
