package dev.optimistic.eaglxor.packets;

import dev.optimistic.eaglxor.packets.state.StateHandler;
import io.netty.channel.ChannelHandlerContext;

public interface EaglerPacket {
  void handle(
    ChannelHandlerContext ctx,
    StateHandler stateHandler
  );
}