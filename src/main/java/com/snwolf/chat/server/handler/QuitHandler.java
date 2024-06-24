package com.snwolf.chat.server.handler;

import com.snwolf.chat.server.session.Session;
import com.snwolf.chat.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/24/2024
 * @Description:
 */
@Slf4j
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {

    /**
     * 当客户端连接断开时触发
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Session session = SessionFactory.getSession();
        session.unbind(ctx.channel());
        log.info("客户端断开连接: {}", ctx.channel());
    }

    /**
     * 客户端异常断开时触发
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Session session = SessionFactory.getSession();
        session.unbind(ctx.channel());
        log.info("客户端: {} 异常断开, 异常是: {}", ctx.channel(), cause);
    }
}
