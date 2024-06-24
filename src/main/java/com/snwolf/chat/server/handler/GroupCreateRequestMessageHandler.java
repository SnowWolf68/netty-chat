package com.snwolf.chat.server.handler;

import cn.hutool.core.util.ObjectUtil;
import com.snwolf.chat.message.GroupCreateRequestMessage;
import com.snwolf.chat.message.GroupCreateResponseMessage;
import com.snwolf.chat.server.session.Group;
import com.snwolf.chat.server.session.GroupSession;
import com.snwolf.chat.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/24/2024
 * @Description:
 */
@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        Set<String> groupInitMembers = msg.getMembers();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, groupInitMembers);
        if (ObjectUtil.isNull(group)) {
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, "创建群聊" + groupName + "成功"));
            // 给群成员发送创建群通知
            List<Channel> membersChannel = groupSession.getMembersChannel(groupName);
            membersChannel.forEach(channel -> channel.writeAndFlush(new GroupCreateResponseMessage(true, "您已被拉入" + groupName + "群聊")));
        } else {
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, "群聊" + groupName + "已经存在"));
        }
    }
}
