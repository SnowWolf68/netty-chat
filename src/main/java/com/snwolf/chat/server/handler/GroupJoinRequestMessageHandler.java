package com.snwolf.chat.server.handler;

import cn.hutool.core.util.ObjectUtil;
import com.snwolf.chat.message.GroupJoinRequestMessage;
import com.snwolf.chat.message.GroupJoinResponseMessage;
import com.snwolf.chat.server.session.Group;
import com.snwolf.chat.server.session.GroupSession;
import com.snwolf.chat.server.session.GroupSessionFactory;
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
public class GroupJoinRequestMessageHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String groupName = msg.getGroupName();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.joinMember(groupName, username);
        if (ObjectUtil.isNull(group)) {
            ctx.writeAndFlush(new GroupJoinResponseMessage(false, "要加入的群聊" + groupName + "不存在"));
        } else {
            ctx.writeAndFlush(new GroupJoinResponseMessage(true, username + "加入群聊" + groupName + "成功"));
        }
    }
}
