package com.snwolf.chat.server.handler;

import cn.hutool.core.util.ObjectUtil;
import com.snwolf.chat.message.GroupQuitRequestMessage;
import com.snwolf.chat.message.GroupQuitResponseMessage;
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
public class GroupQuitRequestMessageHandler extends SimpleChannelInboundHandler<GroupQuitRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupQuitRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        String username = msg.getUsername();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.removeMember(groupName, username);
        if (ObjectUtil.isNull(group)) {
            ctx.writeAndFlush(new GroupQuitResponseMessage(false, "退出群聊" + groupName + "失败"));
        }
    }
}
