package com.snwolf.chat.protocol;

import com.snwolf.chat.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/23/2024
 * @Description:
 */
@Slf4j
public class TestMessageCodec {

    /**
     * 使用EmbeddedChannel测试消息编解码器
     */
    @Test
    public void testMessageCodec() throws Exception {
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        LengthFieldBasedFrameDecoder FRAME_DECODER = new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0);
        EmbeddedChannel channel = new EmbeddedChannel(
                LOGGING_HANDLER,
                // 添加LengthFieldBasedFrameDecoder解决半包问题
                FRAME_DECODER,
                new MessageCodec()
        );
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123456");
        // 测试出站(encode)
        channel.writeOutbound(message);
        log.info("=======================");
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buf);
        ByteBuf slice1 = buf.slice(0, 100);
        buf.retain();
        ByteBuf slice2 = buf.slice(100, buf.readableBytes() - 100);
        buf.retain();
        // 测试入站(decode)
//        channel.writeInbound(buf);
        channel.writeInbound(slice1);
        channel.writeInbound(slice2);
    }
}