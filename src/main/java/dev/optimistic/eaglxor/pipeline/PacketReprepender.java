package dev.optimistic.eaglxor.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.VarInt;

@ChannelHandler.Sharable
public final class PacketReprepender extends ChannelInboundHandlerAdapter {
  public static final PacketReprepender INSTANCE = new PacketReprepender();

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (!(msg instanceof final ByteBuf byteBuf)) {
      super.channelRead(ctx, msg);
      return;
    }

    final var newBuf = ctx.alloc().buffer();
    VarInt.write(newBuf, byteBuf.readableBytes());
    super.channelRead(ctx, newBuf);
  }
}
