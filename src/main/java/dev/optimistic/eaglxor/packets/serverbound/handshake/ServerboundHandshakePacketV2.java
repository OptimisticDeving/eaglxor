package dev.optimistic.eaglxor.packets.serverbound.handshake;

import dev.optimistic.eaglxor.types.EaglerTypes;
import dev.optimistic.eaglxor.types.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.List;

public record ServerboundHandshakePacketV2(
  ProtocolVersion protocolVersion,
  short minecraftVersion,
  String brand,
  String brandVersion,
  boolean hasAuth,
  String authUsername
) implements ServerboundHandshakePacket {
  public static final StreamCodec<ByteBuf, ServerboundHandshakePacketV2>
    STREAM_CODEC = StreamCodec.composite(
    //
    EaglerTypes.unsignedShortPrefixedArrayCodec(
        ProtocolVersion.STREAM_CODEC,
        16
      )
      .map(Collections::max, List::of),
    ServerboundHandshakePacketV2::protocolVersion,
    //
    EaglerTypes.unsignedShortPrefixedArrayCodec(
        EaglerTypes.UNSIGNED_SHORT_CODEC,
        16
      )
      .map(
        list -> Collections.max(list).shortValue(),
        raw -> List.of((int) raw)
      ),
    ServerboundHandshakePacketV2::minecraftVersion,
    //
    EaglerTypes.DEFAULT_STRING, ServerboundHandshakePacketV2::brand,
    EaglerTypes.DEFAULT_STRING, ServerboundHandshakePacketV2::brandVersion,
    EaglerTypes.BOOLEAN_CODEC, ServerboundHandshakePacketV2::hasAuth,
    EaglerTypes.DEFAULT_STRING, ServerboundHandshakePacketV2::authUsername,
    ServerboundHandshakePacketV2::new
  );
}
