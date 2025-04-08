package dev.optimistic.eaglxor.packets.state.impl;

import dev.optimistic.eaglxor.Main;
import dev.optimistic.eaglxor.packets.clientbound.handshake.ClientboundHandshakePacket;
import dev.optimistic.eaglxor.packets.serverbound.handshake.ServerboundHandshakePacket;
import dev.optimistic.eaglxor.packets.state.StateHandler;
import dev.optimistic.eaglxor.pipeline.EaglerAttrs;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public final class HandshakeStateHandler implements StateHandler {
  public static final HandshakeStateHandler INSTANCE =
    new HandshakeStateHandler();

  private HandshakeStateHandler() {

  }

  @Override
  public void handleServerboundHandshakePacket(
    ChannelHandlerContext ctx,
    ServerboundHandshakePacket packet
  ) {
    EaglerAttrs.PROTOCOL_VERSION.setValue(ctx, packet.protocolVersion());
    EaglerAttrs.GAME_VERSION.setValue(ctx, (int) packet.minecraftVersion());
    EaglerAttrs.BRAND.setValue(ctx, packet.brand());
    EaglerAttrs.BRAND_VERSION.setValue(ctx, packet.brandVersion());

    ctx.writeAndFlush(
      new ClientboundHandshakePacket(
        new ClientboundHandshakePacket.VersionData(
          packet.protocolVersion(),
          packet.minecraftVersion()
        ),
        "Eaglxor",
        Main.INSTANCE_SUPPLIER.get().getPluginMeta().getVersion(),
        false,
        Unpooled.EMPTY_BUFFER
      )
    );

    EaglerAttrs.STATE_HANDLER.setValue(
      ctx,
      new LoginStateHandler()
    );
  }
}