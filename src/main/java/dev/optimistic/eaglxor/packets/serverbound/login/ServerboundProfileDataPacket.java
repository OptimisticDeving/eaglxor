package dev.optimistic.eaglxor.packets.serverbound.login;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.state.StateHandler;
import dev.optimistic.eaglxor.types.EaglerTypes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.List;

public record ServerboundProfileDataPacket(List<ProfileData> data)
  implements EaglerPacket {
  public static final StreamCodec<ByteBuf, ServerboundProfileDataPacket>
    SINGLE_STREAM_CODEC = ProfileData.STREAM_CODEC
    .map(
      profileData -> new ServerboundProfileDataPacket(
        Collections.singletonList(profileData)
      ),
      packet -> {
        if (packet.data.size() > 1) {
          throw new EncoderException("Too much profile data for this version!");
        }
        return packet.data.getFirst();
      }
    );

  public static final StreamCodec<ByteBuf, ServerboundProfileDataPacket>
    MULTI_STREAM_CODEC =
    StreamCodec.composite(
      EaglerTypes.prefixedArrayCodec(
        EaglerTypes.UNSIGNED_BYTE_CODEC,
        ProfileData.STREAM_CODEC,
        16
      ),
      ServerboundProfileDataPacket::data,
      ServerboundProfileDataPacket::new
    );

  @Override
  public void handle(ChannelHandlerContext ctx, StateHandler stateHandler) {
    stateHandler.handleServerboundProfileDataPacket(ctx, this);
  }

  public record ProfileData(
    String dataType,
    ByteBuf data
  ) {
    public static final StreamCodec<ByteBuf, ProfileData> STREAM_CODEC =
      StreamCodec.composite(
        EaglerTypes.DEFAULT_STRING, ProfileData::dataType,
        EaglerTypes.DEFAULT_SHORT_PREFIXED_BYTES, ProfileData::data,
        ProfileData::new
      );
  }
}


