package dev.optimistic.eaglxor.packets.serverbound.handshake;

import dev.optimistic.eaglxor.types.EaglerTypes;
import dev.optimistic.eaglxor.types.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ServerboundHandshakePacketV1(
  short minecraftVersion,
  String brand,
  String brandVersion
) implements ServerboundHandshakePacket {
  public static final StreamCodec<ByteBuf, ServerboundHandshakePacketV1> STREAM_CODEC =
    StreamCodec.composite(
      EaglerTypes.UNSIGNED_BYTE_CODEC
        .map(Integer::shortValue, Short::intValue),
      ServerboundHandshakePacketV1::minecraftVersion,
      EaglerTypes.DEFAULT_STRING, ServerboundHandshakePacketV1::brand,
      EaglerTypes.DEFAULT_STRING, ServerboundHandshakePacketV1::brandVersion,
      ServerboundHandshakePacketV1::new
    );

  @Override
  public ProtocolVersion protocolVersion() {
    return ProtocolVersion.V1;
  }
}
