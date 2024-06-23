package com.snwolf.chat.protocol;

import com.snwolf.chat.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 6/23/2024
 * @Description: 可添加@Sharable注解的自定义编解码器
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        // 这里outList是编码后ByteBuf的集合
        ByteBuf out = ctx.alloc().buffer();
        log.info("encode...");
        // magic number
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 版本号
        out.writeByte(1);
        // 序列化方法
        out.writeByte(0);
        // 指令类型
        out.writeByte(msg.getMessageType());
        // 请求序号
        out.writeInt(msg.getSequenceId());
        // 对齐填充1个字节
        out.writeByte(0xff);
        // 使用jdk的序列化方式, 获取msg内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        // 得到字节数组
        byte[] bytes = bos.toByteArray();
        // 正文长度
        out.writeInt(bytes.length);
        // 消息正文
        out.writeBytes(bytes);
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        log.info("decode...");
        // magic number
        int magicNumber = in.readInt();
        // 版本
        byte version = in.readByte();
        // 序列化方法
        byte serializeType = in.readByte();
        // 指令类型
        byte messageType = in.readByte();
        // 请求序号
        int sequenceId = in.readInt();
        // 填充字节
        in.readByte();
        // 长度
        int length = in.readInt();
        // 消息正文
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        log.info("decode message: magicNumber: {}, version: {}, serializeType: {}, messageType: {}, sequenceId: {}, length: {}, bytes: {}", magicNumber, version, serializeType, messageType, sequenceId, length, bytes);

        // 将内容的bytes反序列化成msg对象
        if(serializeType == 0){
            // jdk 序列化方式
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Message message = (Message) ois.readObject();
            log.info("decode message: {}", message);

            out.add(message);
        }else{
            log.error("serialize type not supported: {}", serializeType);
        }
    }
}
