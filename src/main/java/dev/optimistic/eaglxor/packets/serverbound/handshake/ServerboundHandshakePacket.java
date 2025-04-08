package dev.optimistic.eaglxor.packets.serverbound.handshake;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.state.StateHandler;
import dev.optimistic.eaglxor.types.EaglerTypes;
import dev.optimistic.eaglxor.types.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.codec.StreamCodec;

public interface ServerboundHandshakePacket extends EaglerPacket {
  StreamCodec<ByteBuf, ServerboundHandshakePacket> STREAM_CODEC =
    EaglerTypes.UNSIGNED_BYTE_CODEC.dispatch(
      packet -> packet
        .protocolVersion()
        .serializableLegacyVersion(),
      rawProtocolVersion -> ProtocolVersion
        .fromNum(rawProtocolVersion)
        .getBrandPacketCodec()
    );

  ProtocolVersion protocolVersion();

  short minecraftVersion();

  String brand();

  String brandVersion();

  @Override
  default void handle(ChannelHandlerContext ctx, StateHandler stateHandler) {
    stateHandler.handleServerboundHandshakePacket(
      ctx,
      this
    );
  }
}
