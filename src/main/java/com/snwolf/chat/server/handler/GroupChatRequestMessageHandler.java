package com.snwolf.chat.server.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.snwolf.chat.message.GroupChatRequestMessage;
import com.snwolf.chat.message.GroupChatResponseMessage;
import com.snwolf.chat.server.session.GroupSession;
import com.snwolf.chat.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/24/2024
 * @Description:
 */
@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        List<Channel> membersChannel = groupSession.getMembersChannel(groupName);
        if(CollectionUtil.isEmpty(membersChannel)){
            // 当前群聊不存在
            ctx.writeAndFlush(new GroupChatResponseMessage(false, "当前群聊" + groupName + "不存在"));
        }
        membersChannel.forEach(channel -> channel.writeAndFlush(new GroupChatResponseMessage(msg.getFrom(), msg.getContent())));
    }
}
