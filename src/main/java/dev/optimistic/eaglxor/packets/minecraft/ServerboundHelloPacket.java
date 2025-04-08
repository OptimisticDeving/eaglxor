package dev.optimistic.eaglxor.packets.minecraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ServerboundHelloPacket(String username) {
  public static final StreamCodec<ByteBuf, ServerboundHelloPacket>
    STREAM_CODEC =
    StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, ServerboundHelloPacket::username,
      ServerboundHelloPacket::new
    );
}
