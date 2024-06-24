package com.snwolf.chat.server.handler;

import com.snwolf.chat.message.LoginRequestMessage;
import com.snwolf.chat.message.LoginResponseMessage;
import com.snwolf.chat.server.service.UserService;
import com.snwolf.chat.server.service.UserServiceFactory;
import com.snwolf.chat.server.session.SessionFactory;
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
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();
        UserService userService = UserServiceFactory.getUserService();
        boolean result = userService.login(username, password);
        LoginResponseMessage response = null;
        if (result) {
            // 保存Channel和用户的对应关系
            SessionFactory.getSession().bind(ctx.channel(), username);
            response = new LoginResponseMessage(true, "登录成功");
        } else {
            response = new LoginResponseMessage(false, "用户名或密码错误");
        }
        ctx.writeAndFlush(response);
    }
}
