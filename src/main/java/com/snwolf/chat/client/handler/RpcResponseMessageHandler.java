package com.snwolf.chat.client.handler;

import cn.hutool.core.util.ObjectUtil;
import com.snwolf.chat.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 7/7/2024
 * @Description:
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
        if (ObjectUtil.isNotNull(promise)) {
            if (ObjectUtil.isNull(msg.getExceptionValue())) {
                promise.setSuccess(msg.getReturnValue());
            } else {
                promise.setFailure(msg.getExceptionValue());
            }
        }
        log.info("{}", msg);
    }
}
