package dev.optimistic.eaglxor.pipeline;

import com.viaversion.viabackwards.protocol.v1_20_2to1_20.storage.ConfigurationPacketStorage;
import com.viaversion.viaversion.api.protocol.packet.State;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;

@ChannelHandler.Sharable
public final class LoginSuccessSkipper extends ChannelDuplexHandler {
  public static final LoginSuccessSkipper INSTANCE = new LoginSuccessSkipper();

  private LoginSuccessSkipper() {

  }

  @Override
  public void write(
    ChannelHandlerContext ctx,
    Object msg,
    ChannelPromise promise
  ) throws Exception {
    if (!(msg instanceof final ByteBuf buf)) {
      super.write(ctx, msg, promise);
      return;
    }

    buf.markReaderIndex();
    final var packetId = VarInt.read(buf);
    if (packetId != 0x02) {
      buf.resetReaderIndex();
      super.write(ctx, buf, promise);
      return;
    }

    promise.setSuccess();
    ctx.pipeline().remove(this);
    final var via = PipelineUtil.getVia(ctx);
    via.put(new ConfigurationPacketStorage());
    via.getProtocolInfo().setState(State.CONFIGURATION);
    PipelineUtil.forwardToNms(
      ctx,
      0x03,
      ServerboundLoginAcknowledgedPacket.INSTANCE,
      ServerboundLoginAcknowledgedPacket.STREAM_CODEC
    );
  }
}
