package dev.optimistic.eaglxor.types;

import com.google.common.base.Suppliers;
import dev.optimistic.eaglxor.packets.serverbound.handshake.ServerboundHandshakePacket;
import dev.optimistic.eaglxor.packets.serverbound.handshake.ServerboundHandshakePacketV1;
import dev.optimistic.eaglxor.packets.serverbound.handshake.ServerboundHandshakePacketV2;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter
public enum ProtocolVersion {
  PENDING(-1),
  FUTURE(-127),
  /// Used in 1.5.2
  V1(
    1,
    Suppliers.memoize(() -> ServerboundHandshakePacketV1.STREAM_CODEC)
  ),
  V2(2),
  V3(3),
  V4(4);

  public static final StreamCodec<ByteBuf, ProtocolVersion> STREAM_CODEC =
    StreamCodec.composite(
      EaglerTypes.UNSIGNED_SHORT_CODEC,
      ProtocolVersion::getNum,
      ProtocolVersion::fromNum
    );
  public static final StreamCodec<ByteBuf, ProtocolVersion> BYTE_STREAM_CODEC =
    StreamCodec.composite(
      EaglerTypes.UNSIGNED_BYTE_CODEC,
      ProtocolVersion::getNum,
      ProtocolVersion::fromNum
    );

  @Getter
  private final int num;
  private final Supplier<StreamCodec<ByteBuf, ? extends ServerboundHandshakePacket>> brandPacketCodec;

  ProtocolVersion(int num) {
    this(
      num,
      Suppliers.memoize(() -> ServerboundHandshakePacketV2.STREAM_CODEC)
    );
  }

  public static ProtocolVersion fromNum(final int num) {
    return switch (num) {
      case 1 -> ProtocolVersion.V1;
      case 2 -> ProtocolVersion.V2;
      case 3 -> ProtocolVersion.V3;
      case 4 -> ProtocolVersion.V4;
      default -> ProtocolVersion.FUTURE;
    };
  }

  public StreamCodec<ByteBuf, ? extends ServerboundHandshakePacket> getBrandPacketCodec() {
    return this.brandPacketCodec.get();
  }

  public int serializableLegacyVersion() {
    return this == ProtocolVersion.V1 ? 1 : 2;
  }

  public boolean isLegacyVersion() {
    return this == ProtocolVersion.V1;
  }
}
