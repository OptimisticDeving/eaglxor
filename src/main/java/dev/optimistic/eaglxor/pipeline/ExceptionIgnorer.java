package dev.optimistic.eaglxor.pipeline;

import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.exception.CancelException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public final class ExceptionIgnorer extends ChannelInboundHandlerAdapter {
  public static final ExceptionIgnorer INSTANCE = new ExceptionIgnorer();

  private ExceptionIgnorer() {

  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (
      cause instanceof CancelEncoderException
        || cause instanceof CancelDecoderException
        || cause instanceof CancelException
    ) {
      System.out.print("Caught cancel exception");
      return;
    }

    super.exceptionCaught(ctx, cause);
  }
}
