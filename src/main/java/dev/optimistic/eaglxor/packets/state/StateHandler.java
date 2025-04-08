package dev.optimistic.eaglxor.packets.state;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.clientbound.handshake.ClientboundHandshakePacket;
import dev.optimistic.eaglxor.packets.clientbound.login.ClientboundLoginFinishPacket;
import dev.optimistic.eaglxor.packets.clientbound.login.ClientboundLoginStartPacket;
import dev.optimistic.eaglxor.packets.serverbound.handshake.ServerboundHandshakePacket;
import dev.optimistic.eaglxor.packets.serverbound.login.ServerboundLoginFinishPacket;
import dev.optimistic.eaglxor.packets.serverbound.login.ServerboundLoginStartPacket;
import dev.optimistic.eaglxor.packets.serverbound.login.ServerboundProfileDataPacket;
import io.netty.channel.ChannelHandlerContext;

public interface StateHandler {
  default void handleUnexpected(EaglerPacket eaglerPacket) {
    throw new UnsupportedOperationException(
      "Wasn't expecting " + eaglerPacket.getClass().getTypeName()
    );
  }

  default void handleServerboundHandshakePacket(
    ChannelHandlerContext ctx,
    ServerboundHandshakePacket packet
  ) {
    this.handleUnexpected(packet);
  }

  default void handleClientboundHandshakePacket(
    ChannelHandlerContext ctx,
    ClientboundHandshakePacket packet
  ) {
    this.handleUnexpected(packet);
  }

  default void handleServerboundLoginStartPacket(
    ChannelHandlerContext ctx,
    ServerboundLoginStartPacket packet
  ) {
    this.handleUnexpected(packet);
  }

  default void handleClientboundLoginStartPacket(
    ChannelHandlerContext ctx,
    ClientboundLoginStartPacket packet
  ) {
    this.handleUnexpected(packet);
  }

  default void handleServerboundProfileDataPacket(
    ChannelHandlerContext ctx,
    ServerboundProfileDataPacket packet
  ) {
    this.handleUnexpected(packet);
  }

  default void handleServerboundLoginFinishPacket(
    ChannelHandlerContext ctx,
    ServerboundLoginFinishPacket packet
  ) {
    this.handleUnexpected(packet);
  }

  default void handleClientboundLoginFinishPacket(
    ChannelHandlerContext ctx,
    ClientboundLoginFinishPacket packet
  ) {
    this.handleUnexpected(packet);
  }
}
