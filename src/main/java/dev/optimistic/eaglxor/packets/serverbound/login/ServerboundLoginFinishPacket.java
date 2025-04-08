package dev.optimistic.eaglxor.packets.serverbound.login;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.state.StateHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.codec.StreamCodec;

public final class ServerboundLoginFinishPacket
  implements EaglerPacket {
  public static final ServerboundLoginFinishPacket INSTANCE =
    new ServerboundLoginFinishPacket();
  public static final StreamCodec<ByteBuf, ServerboundLoginFinishPacket>
    STREAM_CODEC = StreamCodec.unit(INSTANCE);

  private ServerboundLoginFinishPacket() {

  }

  @Override
  public void handle(ChannelHandlerContext ctx, StateHandler stateHandler) {
    stateHandler.handleServerboundLoginFinishPacket(ctx, this);
  }
}