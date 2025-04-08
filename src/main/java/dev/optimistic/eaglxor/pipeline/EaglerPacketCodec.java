package dev.optimistic.eaglxor.pipeline;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.versioning.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public final class EaglerPacketCodec extends ByteToMessageCodec<EaglerPacket> {
  @Override
  protected void encode(
    ChannelHandlerContext ctx,
    EaglerPacket msg,
    ByteBuf out
  ) {
    PacketRegistry
      .getPacketRegistry(EaglerAttrs.PROTOCOL_VERSION.getValue(ctx))
      .encode(out, msg);
  }

  @Override
  protected void decode(
    ChannelHandlerContext ctx,
    ByteBuf in,
    List<Object> out
  ) {
    while (in.isReadable()) {
      out.add(
        PacketRegistry.getPacketRegistry(
          EaglerAttrs.PROTOCOL_VERSION.getValue(ctx)
        ).decode(in)
      );
    }
  }
}
