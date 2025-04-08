package dev.optimistic.eaglxor.packets.serverbound.login;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.state.StateHandler;
import dev.optimistic.eaglxor.types.EaglerTypes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public record ServerboundLoginStartPacket(
  String username,
  String requestedServer,
  ByteBuf authPassword,
  @Nullable CookieData cookieData
) implements EaglerPacket {
  public static final StreamCodec<ByteBuf, ServerboundLoginStartPacket> COOKIELESS_CODEC =
    codec(EaglerTypes.nothing());
  public static final StreamCodec<ByteBuf, ServerboundLoginStartPacket> WITH_COOKIE_CODEC =
    codec(CookieData.STREAM_CODEC);

  private static StreamCodec<ByteBuf, ServerboundLoginStartPacket> codec(
    StreamCodec<ByteBuf, @Nullable CookieData> cookieCodec
  ) {
    return StreamCodec.composite(
      EaglerTypes.stringCodecWithLimit(16), ServerboundLoginStartPacket::username,
      EaglerTypes.DEFAULT_STRING, ServerboundLoginStartPacket::requestedServer,
      EaglerTypes.DEFAULT_BYTE_PREFIXED_BYTES, ServerboundLoginStartPacket::authPassword,
      cookieCodec, ServerboundLoginStartPacket::cookieData,
      ServerboundLoginStartPacket::new
    );
  }

  @Override
  public void handle(
    ChannelHandlerContext ctx,
    StateHandler stateHandler
  ) {
    stateHandler.handleServerboundLoginStartPacket(
      ctx,
      this
    );
  }

  public record CookieData(boolean enableCookie, ByteBuf cookie) {
    public static final StreamCodec<ByteBuf, CookieData> STREAM_CODEC = StreamCodec
      .composite(
        ByteBufCodecs.BOOL, CookieData::enableCookie,
        EaglerTypes.DEFAULT_BYTE_PREFIXED_BYTES, CookieData::cookie,
        CookieData::new
      );
  }
}
