package dev.optimistic.eaglxor.pipeline;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

public final class PipelineUtil {
  private PipelineUtil() {

  }

  public static void complete(ChannelHandlerContext ctx) {
    ctx.pipeline().remove(EaglerPacketCodec.class);
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
    forwardToBeforeHandler(ctx, id, packet, codec, "splitter");
  }


  public static <T> void forwardToNms(
    ChannelHandlerContext ctx,
    int id,
    T packet,
    StreamCodec<? super FriendlyByteBuf, T> codec
  ) {
    forwardToBeforeHandler(ctx, id, packet, codec, "via-decoder");
  }

  public static UserConnection getVia(ChannelHandlerContext ctx) {
    return ctx.pipeline().get(BukkitEncodeHandler.class).connection();
  }
}
