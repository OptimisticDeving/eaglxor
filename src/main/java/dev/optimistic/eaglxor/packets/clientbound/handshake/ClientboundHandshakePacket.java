package dev.optimistic.eaglxor.packets.clientbound.handshake;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.state.StateHandler;
import dev.optimistic.eaglxor.types.EaglerTypes;
import dev.optimistic.eaglxor.types.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientboundHandshakePacket(
  VersionData version,
  String brand,
  String versionString,
  boolean authEnabled,
  ByteBuf salt
) implements EaglerPacket {
  public static final StreamCodec<ByteBuf, ClientboundHandshakePacket>
    MODERN_STREAM_CODEC = createCodec(VersionData.MODERN_STREAM_CODEC);
  public static final StreamCodec<ByteBuf, ClientboundHandshakePacket>
    LEGACY_STREAM_CODEC = createCodec(VersionData.LEGACY_STREAM_CODEC);

  private static StreamCodec<ByteBuf, ClientboundHandshakePacket> createCodec(
    StreamCodec<ByteBuf, VersionData> versionDataCodec
  ) {
    return StreamCodec.composite(
      versionDataCodec, ClientboundHandshakePacket::version,
      EaglerTypes.DEFAULT_STRING, ClientboundHandshakePacket::brand,
      EaglerTypes.DEFAULT_STRING, ClientboundHandshakePacket::versionString,
      ByteBufCodecs.BOOL, ClientboundHandshakePacket::authEnabled,
      EaglerTypes.shortPrefixedByteArrayCodec(0), ClientboundHandshakePacket::salt,
      ClientboundHandshakePacket::new
    );
  }

  @Override
  public void handle(
    ChannelHandlerContext ctx,
    StateHandler stateHandler
  ) {
    stateHandler.handleClientboundHandshakePacket(
      ctx,
      this
    );
  }

  public record VersionData(
    ProtocolVersion eaglerProtocol,
    int minecraftProtocol
  ) {
    public static final StreamCodec<ByteBuf, VersionData> MODERN_STREAM_CODEC =
      StreamCodec.composite(
        ProtocolVersion.STREAM_CODEC, VersionData::eaglerProtocol,
        EaglerTypes.UNSIGNED_SHORT_CODEC, VersionData::minecraftProtocol,
        VersionData::new
      );
    public static final StreamCodec<ByteBuf, VersionData> LEGACY_STREAM_CODEC =
      StreamCodec.composite(
        ProtocolVersion.BYTE_STREAM_CODEC, VersionData::eaglerProtocol,
        EaglerTypes.constant(
          47,
          EaglerTypes.UNSIGNED_BYTE_CODEC
        ), VersionData::minecraftProtocol,
        VersionData::new
      );
  }
}
