package dev.optimistic.eaglxor.pipeline;

import dev.optimistic.eaglxor.Main;
import dev.optimistic.eaglxor.text.MOTD;
import dev.optimistic.eaglxor.text.TextPacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.bukkit.Bukkit;

@ChannelHandler.Sharable
public final class MOTDSpecialHandler extends ChannelInboundHandlerAdapter {
  public static final MOTDSpecialHandler INSTANCE = new MOTDSpecialHandler();

  private MOTDSpecialHandler() {

  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    if (!(msg instanceof TextWebSocketFrame textWebSocketFrame)) {
      ctx.pipeline().remove(this);
      return;
    }

    if (!textWebSocketFrame.text().equals("Accept: MOTD"))
      throw new DecoderException("Don't know how to handle non-MOTD text frame");

    PipelineUtil.remove(ctx);

    final var instance = Main.INSTANCE_SUPPLIER.get();
    final var serverInfo = instance.getServerInfoContainer();
    final var server = Bukkit.getServer();
    final var motd = MOTD.create(server);
    final var packet = TextPacketRegistry.INSTANCE.serialize(serverInfo, motd);

    ctx.write(new TextWebSocketFrame(packet));

    final ByteBuf faviconBuf;
    if (motd.favicon() && (faviconBuf = instance.getServerIcon()) != null) {
      ctx.write(new BinaryWebSocketFrame(faviconBuf.copy()));
    }

    ctx.flush();
    ctx.close();
  }
}
