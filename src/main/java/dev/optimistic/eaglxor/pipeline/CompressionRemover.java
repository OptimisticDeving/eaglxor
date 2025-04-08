package dev.optimistic.eaglxor.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.HandlerNames;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;

@ChannelHandler.Sharable
public final class CompressionRemover extends ChannelOutboundHandlerAdapter {
  public static final CompressionRemover INSTANCE = new CompressionRemover();

  private CompressionRemover() {

  }

  private static void replaceHandler(ChannelHandlerContext ctx, String name) {
    if (ctx.pipeline().get(name) != null) {
      System.out.println("Removing " + name);
      ctx.pipeline().remove(name);
      System.out.println(ctx.pipeline());
    }
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof ClientboundLoginCompressionPacket) {
      replaceHandler(ctx, HandlerNames.COMPRESS);
      replaceHandler(ctx, HandlerNames.DECOMPRESS);
      ctx.pipeline().remove(this);
      return;
    }

    super.write(ctx, msg, promise);
  }
}
