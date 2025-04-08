package dev.optimistic.eaglxor.packets.versioning;

import dev.optimistic.eaglxor.packets.EaglerPacket;
import dev.optimistic.eaglxor.packets.clientbound.handshake.ClientboundHandshakePacket;
import dev.optimistic.eaglxor.packets.clientbound.login.ClientboundLoginFinishPacket;
import dev.optimistic.eaglxor.packets.clientbound.login.ClientboundLoginStartPacket;
import dev.optimistic.eaglxor.packets.serverbound.handshake.ServerboundHandshakePacket;
import dev.optimistic.eaglxor.packets.serverbound.login.ServerboundLoginFinishPacket;
import dev.optimistic.eaglxor.packets.serverbound.login.ServerboundLoginStartPacket;
import dev.optimistic.eaglxor.packets.serverbound.login.ServerboundProfileDataPacket;
import dev.optimistic.eaglxor.types.EaglerTypes;
import dev.optimistic.eaglxor.types.ProtocolVersion;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public enum PacketRegistry implements StreamCodec<ByteBuf, EaglerPacket> {
  FUTURE,
  INITIAL(
    PacketRegistration.of(
      1,
      ServerboundHandshakePacket.class, ServerboundHandshakePacket.STREAM_CODEC
    )
  ),
  V1(
    PacketRegistration.of(
      2,
      ClientboundHandshakePacket.class,
      ClientboundHandshakePacket.LEGACY_STREAM_CODEC
    ),
    PacketRegistration.of(
      4,
      ServerboundLoginStartPacket.class,
      ServerboundLoginStartPacket.COOKIELESS_CODEC
    ),
    PacketRegistration.of(
      5,
      ClientboundLoginStartPacket.class,
      ClientboundLoginStartPacket.STREAM_CODEC
    ),
    PacketRegistration.of(
      7,
      ServerboundProfileDataPacket.class,
      ServerboundProfileDataPacket.SINGLE_STREAM_CODEC
    ),
    PacketRegistration.of(
      8,
      ServerboundLoginFinishPacket.class,
      ServerboundLoginFinishPacket.STREAM_CODEC
    ),
    PacketRegistration.of(
      9,
      ClientboundLoginFinishPacket.class,
      ClientboundLoginFinishPacket.STREAM_CODEC
    )
  ),
  V2(
    V1,
    PacketRegistration.of(
      2,
      ClientboundHandshakePacket.class,
      ClientboundHandshakePacket.MODERN_STREAM_CODEC
    )
  ),
  V3(V2),
  V4(
    V3,
    PacketRegistration.of(
      4,
      ServerboundLoginStartPacket.class,
      ServerboundLoginStartPacket.WITH_COOKIE_CODEC
    ),
    PacketRegistration.of(
      7,
      ServerboundProfileDataPacket.class,
      ServerboundProfileDataPacket.MULTI_STREAM_CODEC
    )
  );

  private final StreamCodec<ByteBuf, EaglerPacket> streamCodec;

  private final Map<Integer, StreamCodec<ByteBuf, ? extends EaglerPacket>> idToCodec;
  private final Map<Class<? extends EaglerPacket>, Integer> classToId;

  PacketRegistry(
    @Nullable PacketRegistry parent,
    PacketRegistration<?>... registrations
  ) {
    this.idToCodec =
      new Int2ObjectOpenHashMap<>(registrations.length);
    this.classToId =
      new IdentityHashMap<>(registrations.length);

    if (parent != null) {
      this.idToCodec.putAll(parent.idToCodec);
      this.classToId.putAll(parent.classToId);
    }

    for (PacketRegistration<?> registration : registrations) {
      idToCodec.put(registration.id, registration.codec);
      classToId.put(registration.packetClass, registration.id);
    }

    streamCodec = EaglerTypes.UNSIGNED_BYTE_CODEC
      .dispatch(
        packet -> classToId.get(packet.getClass()),
        id -> {
          final var codec = idToCodec.get(id);
          if (codec == null)
            throw new DecoderException("Unrecognized packet " + id);
          return codec;
        }
      );
  }

  PacketRegistry(PacketRegistration<?>... registrations) {
    this(null, registrations);
  }

  // Needed to avoid cylic initialization
  public static PacketRegistry getPacketRegistry(
    ProtocolVersion protocolVersion
  ) {
    return switch (protocolVersion) {
      case PENDING -> PacketRegistry.INITIAL;
      case FUTURE -> PacketRegistry.FUTURE;
      case V1 -> PacketRegistry.V1;
      case V2 -> PacketRegistry.V2;
      case V3 -> PacketRegistry.V3;
      case V4 -> PacketRegistry.V4;
    };
  }

  @Override
  public @NotNull EaglerPacket decode(@NotNull ByteBuf buffer) {
    return this.streamCodec.decode(buffer);
  }

  @Override
  public void encode(@NotNull ByteBuf buffer, @NotNull EaglerPacket value) {
    this.streamCodec.encode(buffer, value);
  }

  private record PacketRegistration<T extends EaglerPacket>(
    int id,
    Class<T> packetClass,
    StreamCodec<ByteBuf, T> codec
  ) {
    private static <T extends EaglerPacket> PacketRegistration<T> of(
      int id,
      Class<T> packetClass,
      StreamCodec<ByteBuf, T> codec
    ) {
      return new PacketRegistration<>(id, packetClass, codec);
    }
  }
}
