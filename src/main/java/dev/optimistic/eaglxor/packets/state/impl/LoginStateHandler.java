package dev.optimistic.eaglxor.packets.state.impl;

import dev.optimistic.eaglxor.Main;
import dev.optimistic.eaglxor.packets.clientbound.login.ClientboundLoginFinishPacket;
import dev.optimistic.eaglxor.packets.clientbound.login.ClientboundLoginStartPacket;
import dev.optimistic.eaglxor.packets.minecraft.ServerboundHelloPacket;
import dev.optimistic.eaglxor.packets.serverbound.login.ServerboundLoginFinishPacket;
import dev.optimistic.eaglxor.packets.serverbound.login.ServerboundLoginStartPacket;
import dev.optimistic.eaglxor.packets.serverbound.login.ServerboundProfileDataPacket;
import dev.optimistic.eaglxor.packets.state.StateHandler;
import dev.optimistic.eaglxor.pipeline.EaglerAttrs;
import dev.optimistic.eaglxor.pipeline.PipelineUtil;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;

public final class LoginStateHandler implements StateHandler {
  private boolean handledLoginStart = false;
  private boolean sentLoginStart = false;
  private int profileDataReceived = 0;

  public LoginStateHandler() {

  }

  @Override
  public void handleServerboundLoginStartPacket(
    ChannelHandlerContext ctx,
    ServerboundLoginStartPacket packet
  ) {
    if (this.handledLoginStart)
      throw new IllegalStateException("Duplicate login start");

    EaglerAttrs.USERNAME.setValue(ctx, packet.username());
    this.handledLoginStart = true;
    final var uuid = UUIDUtil.createOfflinePlayerUUID(packet.username());
    ctx.writeAndFlush(new ClientboundLoginStartPacket(
      packet.username(),
      uuid
    ));
    this.sentLoginStart = true;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void handleServerboundLoginFinishPacket(
    ChannelHandlerContext ctx,
    ServerboundLoginFinishPacket packet
  ) {
    if (!this.sentLoginStart || !this.handledLoginStart)
      throw new IllegalStateException("Client sent login finish too early");

    ctx.writeAndFlush(ClientboundLoginFinishPacket.INSTANCE)
      .syncUninterruptibly();

    PipelineUtil.remove(ctx);

    final int protocolVersion = EaglerAttrs.GAME_VERSION.getValue(ctx);
    PipelineUtil.forwardToVia(
      ctx,
      0x00,
      new ClientIntentionPacket(
        protocolVersion,
        Main.NAME,
        25565,
        ClientIntent.LOGIN
      ),
      ClientIntentionPacket.STREAM_CODEC
    );

    final var username = EaglerAttrs.USERNAME.getValue(ctx);

    PipelineUtil.forwardToVia(
      ctx,
      0x00,
      new ServerboundHelloPacket(username),
      ServerboundHelloPacket.STREAM_CODEC
    );
  }

  @Override
  public void handleServerboundProfileDataPacket(
    ChannelHandlerContext ctx,
    ServerboundProfileDataPacket packet
  ) {
    if (!this.sentLoginStart)
      throw new IllegalStateException("Received profile data too early");
    this.profileDataReceived += packet.data().size();
    if (this.profileDataReceived > 12)
      throw new IllegalStateException("Too much profile data received");
    // ... and then do nothing ;)
  }
}
