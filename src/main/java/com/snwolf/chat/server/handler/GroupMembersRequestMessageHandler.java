package com.snwolf.chat.server.handler;

import com.snwolf.chat.message.GroupMembersRequestMessage;
import com.snwolf.chat.message.GroupMembersResponseMessage;
import com.snwolf.chat.server.session.GroupSession;
import com.snwolf.chat.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/24/2024
 * @Description:
 */
@ChannelHandler.Sharable
public class GroupMembersRequestMessageHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Set<String> membersSet = groupSession.getMembers(groupName);
        ctx.writeAndFlush(new GroupMembersResponseMessage(membersSet));
    }
}
