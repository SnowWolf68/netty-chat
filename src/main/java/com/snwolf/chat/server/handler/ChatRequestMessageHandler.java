package com.snwolf.chat.server.handler;

import cn.hutool.core.util.ObjectUtil;
import com.snwolf.chat.message.ChatRequestMessage;
import com.snwolf.chat.message.ChatResponseMessage;
import com.snwolf.chat.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/24/2024
 * @Description:
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        String msgTo = msg.getTo();
        // 要发送对方的Channel
        Channel channel = SessionFactory.getSession().getChannel(msgTo);
        if (ObjectUtil.isNull(channel)) {
            // 对方不在线, 向当前用户发送一条提示信息
            ctx.writeAndFlush(new ChatResponseMessage(false, "对方用户不存在或者不在线"));
        } else {
            // 对方在线
            ChatResponseMessage resp = ChatResponseMessage.builder()
                    .from(msg.getFrom())
                    .content(msg.getContent())
                    .build();
            channel.writeAndFlush(resp);
        }
    }
}
