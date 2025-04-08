package dev.optimistic.eaglxor.packets.clientbound.login;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.state.StateHandler;
import dev.optimistic.eaglxor.types.EaglerTypes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record ClientboundLoginStartPacket(
  String acceptedUsername,
  UUID profileId
) implements EaglerPacket {
  public static final StreamCodec<ByteBuf, ClientboundLoginStartPacket>
    STREAM_CODEC =
    StreamCodec.composite(
      EaglerTypes.DEFAULT_STRING, ClientboundLoginStartPacket::acceptedUsername,
      UUIDUtil.STREAM_CODEC, ClientboundLoginStartPacket::profileId,
      ClientboundLoginStartPacket::new
    );

  @Override
  public void handle(ChannelHandlerContext ctx, StateHandler stateHandler) {
    stateHandler.handleClientboundLoginStartPacket(ctx, this);
  }
}
