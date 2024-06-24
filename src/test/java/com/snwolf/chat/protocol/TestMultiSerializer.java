package com.snwolf.chat.protocol;

import com.snwolf.chat.config.Config;
import com.snwolf.chat.message.LoginRequestMessage;
import com.snwolf.chat.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Test;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/24/2024
 * @Description:
 */
public class TestMultiSerializer {

    @Test
    public void testSerialize() {
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        EmbeddedChannel channel = new EmbeddedChannel(LOGGING_HANDLER, MESSAGE_CODEC, LOGGING_HANDLER);

        LoginRequestMessage req = new LoginRequestMessage("zhangsan", "123");
        channel.writeOutbound(req);
    }

    @Test
    public void testDeserialize() {
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        EmbeddedChannel channel = new EmbeddedChannel(LOGGING_HANDLER, MESSAGE_CODEC, LOGGING_HANDLER);

        LoginRequestMessage req = new LoginRequestMessage("zhangsan", "123");
        ByteBuf buf = messageToByteBuf(req);
        channel.writeInbound(buf);
    }

    public static ByteBuf messageToByteBuf(Message msg) {
        int algorithm = Config.getSerializerAlgorithm().ordinal();
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        out.writeBytes(new byte[]{1, 2, 3, 4});
        out.writeByte(1);
        out.writeByte(algorithm);
        out.writeByte(msg.getMessageType());
        out.writeInt(msg.getSequenceId());
        out.writeByte(0xff);
        byte[] bytes = Serializer.SerializeAlgo.values()[algorithm].serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
        return out;
    }
}
