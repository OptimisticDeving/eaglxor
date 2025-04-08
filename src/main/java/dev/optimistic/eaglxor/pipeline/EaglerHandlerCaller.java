package dev.optimistic.eaglxor.pipeline;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public final class EaglerHandlerCaller extends ChannelInboundHandlerAdapter {
  public static final EaglerHandlerCaller INSTANCE = new EaglerHandlerCaller();

  private EaglerHandlerCaller() {

  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (!(msg instanceof final EaglerPacket eaglerPacket)) {
      super.channelRead(ctx, msg);
      return;
    }

    eaglerPacket.handle(ctx, EaglerAttrs.STATE_HANDLER.getValue(ctx));
  }
}
