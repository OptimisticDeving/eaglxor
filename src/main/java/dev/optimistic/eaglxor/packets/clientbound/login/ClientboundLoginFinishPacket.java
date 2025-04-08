package dev.optimistic.eaglxor.packets.clientbound.login;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.state.StateHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.codec.StreamCodec;

public final class ClientboundLoginFinishPacket implements EaglerPacket {
  public static final ClientboundLoginFinishPacket INSTANCE =
    new ClientboundLoginFinishPacket();
  public static final StreamCodec<ByteBuf, ClientboundLoginFinishPacket>
    STREAM_CODEC = StreamCodec.unit(INSTANCE);

  private ClientboundLoginFinishPacket() {

  }

  @Override
  public void handle(ChannelHandlerContext ctx, StateHandler stateHandler) {
    stateHandler.handleClientboundLoginFinishPacket(ctx, this);
  }
}
