package com.snwolf.chat.client;

import com.snwolf.chat.message.LoginRequestMessage;
import com.snwolf.chat.protocol.MessageCodecSharable;
import com.snwolf.chat.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Scanner;

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
                    ch.pipeline().addLast("clientHandler", new ChannelInboundHandlerAdapter() {

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.info("read message: {}", msg);
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
                                System.out.print("请输入密码: ");
                                String password = scanner.nextLine();
                                LoginRequestMessage loginRequestMessage = LoginRequestMessage.builder()
                                        .username(username)
                                        .password(password)
                                        .build();
                                // 发送消息
                                ctx.writeAndFlush(loginRequestMessage);

                                // 等待用户输入
                                System.out.println("waiting...");
                                try {
                                    System.in.read();
                                } catch (IOException e) {
                                    log.error("client error: {}", e);
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
