package com.snwolf.chat.client;

import cn.hutool.core.util.ObjectUtil;
import com.snwolf.chat.message.*;
import com.snwolf.chat.protocol.MessageCodecSharable;
import com.snwolf.chat.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/23/2024
 * @Description: 聊天的客户端
 */
@Slf4j
public class ChatClient {

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();

        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        AtomicBoolean LOGIN = new AtomicBoolean(false);
        AtomicBoolean EXIT = new AtomicBoolean(false);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtocolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 心跳检测
                    ch.pipeline().addLast(new IdleStateHandler(0, 3, 0));
                    ch.pipeline().addLast(new ChannelDuplexHandler() {
                        /**
                         * 用来响应特殊事件的触发
                         * @param ctx
                         * @param evt
                         * @throws Exception
                         */
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            if (ObjectUtil.equal(event.state(), IdleState.WRITER_IDLE)) {
                                log.info("写空闲... 发送心跳包...");
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });
                    ch.pipeline().addLast("clientHandler", new ChannelInboundHandlerAdapter() {

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("连接已经关闭! 按任意键退出...");
                            EXIT.set(true);
                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.info("read message: {}", msg);
                            if (msg instanceof LoginResponseMessage) {
                                LoginResponseMessage responseMessage = (LoginResponseMessage) msg;
                                LOGIN.set(responseMessage.isSuccess());
                                // 唤醒 system-in 线程
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }

                        // 连接建立后触发active事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 由于用户名和密码需要用户输入, 因此这里单独起一个线程来处理
                            // 这个新的线程单独用于处理用户输入, 发送消息
                            new Thread(() -> {
                                // 提示用户输入用户名和密码
                                Scanner scanner = new Scanner(System.in);
                                System.out.print("请输入用户名: ");
                                String username = scanner.nextLine();
                                if (EXIT.get()) {
                                    return;
                                }
                                System.out.print("请输入密码: ");
                                String password = scanner.nextLine();
                                if (EXIT.get()) {
                                    return;
                                }
                                LoginRequestMessage loginRequestMessage = LoginRequestMessage.builder()
                                        .username(username)
                                        .password(password)
                                        .build();
                                // 发送消息
                                ctx.writeAndFlush(loginRequestMessage);

                                try {
                                    WAIT_FOR_LOGIN.await();
                                    if (!LOGIN.get()) {
                                        // 登录失败
                                        ctx.channel().close();
                                        return;
                                    }
                                    // 登录成功, 继续向下执行, 发送聊天信息
                                    while (true) {
                                        System.out.println("==================================");
                                        System.out.println("send [username] [content]");
                                        System.out.println("gsend [group name] [content]");
                                        System.out.println("gcreate [group name] [m1,m2,m3...]");
                                        System.out.println("gmembers [group name]");
                                        System.out.println("gjoin [group name]");
                                        System.out.println("gquit [group name]");
                                        System.out.println("quit");
                                        System.out.println("==================================");
                                        String command = scanner.nextLine();
                                        if (EXIT.get()) {
                                            return;
                                        }
                                        String[] s = command.split(" ");
                                        switch (s[0]) {
                                            case "send":
                                                ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                                                break;
                                            case "gsend":
                                                ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                                                break;
                                            case "gcreate":
                                                HashSet<String> membersSet = new HashSet<>(Arrays.asList(s[2].split(",")));
                                                // 建群的时候不要忘了把自己添加进去
                                                membersSet.add(username);
                                                ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], membersSet));
                                                break;
                                            case "gmembers":
                                                ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                                break;
                                            case "gjoin":
                                                ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                                                break;
                                            case "gquit":
                                                ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                                break;
                                            case "quit":
                                                ctx.channel().close();
                                                return;
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    log.error("system-in error: {}", e);
                                }
                            }, "system-in").start();
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("cliend error: {}", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
