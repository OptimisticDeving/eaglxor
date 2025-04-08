package dev.optimistic.eaglxor.pipeline;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.HandlerNames;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

public final class PipelineUtil {
  private PipelineUtil() {

  }

  public static void finish(ChannelHandlerContext ctx) {
    ctx.pipeline().remove(EaglerPacketCodec.class);
    ctx.pipeline().remove(EaglerHandlerCaller.class);
  }

  public static <T> void forwardToBeforeHandler(
    ChannelHandlerContext ctx,
    int id,
    T packet,
    StreamCodec<? super FriendlyByteBuf, T> codec,
    String handlerName
  ) {
    final var bodyBuf = ctx.alloc().buffer();
    VarInt.write(bodyBuf, id);
    codec.encode(new FriendlyByteBuf(bodyBuf), packet);
    ctx.pipeline().context(handlerName).fireChannelRead(bodyBuf);
  }

  public static <T> void forwardToVia(
    ChannelHandlerContext ctx,
    int id,
    T packet,
    StreamCodec<? super FriendlyByteBuf, T> codec
  ) {
    forwardToBeforeHandler(
      ctx,
      id,
      packet,
      codec,
      HandlerNames.SPLITTER
    );
  }


  public static <T> void forwardToNms(
    ChannelHandlerContext ctx,
    int id,
    T packet,
    StreamCodec<? super FriendlyByteBuf, T> codec
  ) {
    forwardToBeforeHandler(
      ctx,
      id,
      packet,
      codec,
      Via.getManager().getInjector().getDecoderName()
    );
  }

  public static UserConnection getVia(ChannelHandlerContext ctx) {
    return ctx.pipeline().get(BukkitEncodeHandler.class).connection();
  }
}
