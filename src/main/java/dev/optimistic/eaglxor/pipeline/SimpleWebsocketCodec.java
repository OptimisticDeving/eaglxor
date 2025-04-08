package dev.optimistic.eaglxor.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

@ChannelHandler.Sharable
public final class SimpleWebsocketCodec
  extends MessageToMessageCodec<BinaryWebSocketFrame, ByteBuf> {
  public static final SimpleWebsocketCodec INSTANCE = new SimpleWebsocketCodec();

  private SimpleWebsocketCodec() {

  }

  @Override
  protected void encode(
    ChannelHandlerContext ctx,
    ByteBuf msg,
    List<Object> out
  ) {
    msg.retain();
    out.add(new BinaryWebSocketFrame(msg));
  }

  @Override
  protected void decode(
    ChannelHandlerContext ctx,
    BinaryWebSocketFrame msg,
    List<Object> out
  ) {
    msg.retain();
    out.add(msg.content());
  }
}
