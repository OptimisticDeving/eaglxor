package dev.optimistic.eaglxor.types;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.codec.StreamCodec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class EaglerTypes {
  public static final StreamCodec<ByteBuf, Boolean> BOOLEAN_CODEC =
    StreamCodec.of(
      ByteBuf::writeBoolean,
      ByteBuf::readBoolean
    );
  public static final StreamCodec<ByteBuf, Integer> UNSIGNED_BYTE_CODEC =
    StreamCodec.of(
      (output, input) -> {
        if (input > 255) throw new EncoderException("Unsigned byte too large");
        output.writeByte(input);
      },
      input -> (int) input.readUnsignedByte()
    );
  public static final StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT_CODEC =
    StreamCodec.of(
      (output, input) -> {
        if (input > 65535) throw new EncoderException("Unsigned short too big");
        output.writeShort(input);
      },
      ByteBuf::readUnsignedShort
    );
  public static final StreamCodec<ByteBuf, ByteBuf>
    DEFAULT_BYTE_PREFIXED_BYTES = bytePrefixedByteArrayCodec(255);
  public static final StreamCodec<ByteBuf, ByteBuf>
    DEFAULT_SHORT_PREFIXED_BYTES = shortPrefixedByteArrayCodec(65535);
  private static final Charset STRING_CHARSET = StandardCharsets.US_ASCII;
  public static final StreamCodec<ByteBuf, String> DEFAULT_STRING =
    stringCodecWithLimit(255);

  private EaglerTypes() {

  }

  public static <T,
    TC extends StreamCodec<ByteBuf, T>> StreamCodec<ByteBuf, List<T>>
  unsignedShortPrefixedArrayCodec(
    TC typeCodec,
    int limit
  ) {
    return prefixedArrayCodec(
      UNSIGNED_SHORT_CODEC,
      typeCodec,
      Math.min(limit, 65535)
    );
  }

  public static StreamCodec<ByteBuf, ByteBuf> shortPrefixedByteArrayCodec(int limit) {
    final int theLimit = Math.clamp(limit, 0, 65535);

    return StreamCodec.of(
      (output, input) -> {
        if (input.readableBytes() > theLimit)
          throw new EncoderException("Byte array too big");
        output.writeShort(input.readableBytes());
        output.writeBytes(input);
      },
      input -> {
        final int length = input.readShort();
        if (length > theLimit)
          throw new DecoderException(
            "Received byte array too large: " + length +
              ", but limited to " + limit);
        return input.readBytes(length);
      }
    );
  }

  public static StreamCodec<ByteBuf, ByteBuf> bytePrefixedByteArrayCodec(int limit) {
    final int theLimit = Math.clamp(limit, 0, 255);

    return StreamCodec.of(
      (output, input) -> {
        if (input.readableBytes() > theLimit)
          throw new EncoderException("Byte array too big");
        output.writeByte(input.readableBytes());
        output.writeBytes(input);
      },
      input -> {
        final int length = input.readUnsignedByte();
        if (length > theLimit)
          throw new DecoderException(
            "Received byte array too large: " + length +
              ", but limited to " + theLimit);
        return input.readBytes(length);
      }
    );
  }

  public static <T> StreamCodec<ByteBuf, T> nothing() {
    return StreamCodec.of(
      (input, output) -> {
      },
      input -> null
    );
  }

  public static <T> StreamCodec<ByteBuf, T> constant(
    T constant,
    StreamCodec<ByteBuf, T> codec
  ) {
    return StreamCodec.of(
      (output, input) -> codec.encode(output, constant),
      input -> {
        codec.decode(input);
        return constant;
      }
    );
  }

  public static StreamCodec<ByteBuf, String> stringCodecWithLimit(int limit) {
    return StreamCodec.of(
      (output, input) -> {
        if (input.length() > limit)
          throw new EncoderException("String too large");
        output.writeByte(input.length());
        output.writeBytes(input.getBytes(STRING_CHARSET));
      },
      input -> {
        final int stringLength = input.readUnsignedByte();
        if (stringLength > limit)
          throw new DecoderException(
            "Received string too large: " + stringLength +
              ", but limited to " + limit
          );
        final String asString = input.toString(
          input.readerIndex(),
          stringLength,
          STRING_CHARSET
        );
        input.skipBytes(stringLength);
        return asString;
      }
    );
  }

  public static <PC extends StreamCodec<ByteBuf, Integer>,
    T, TC extends StreamCodec<ByteBuf, T>> StreamCodec<ByteBuf, List<T>>
  prefixedArrayCodec(
    PC prefixCodec,
    TC typeCodec,
    int limit
  ) {
    if (typeCodec == null)
      throw new AssertionError();

    return StreamCodec.of(
      (output, input) -> {
        if (input.size() > limit)
          throw new EncoderException("Array too large");
        prefixCodec.encode(output, input.size());
        for (final T element : input) {
          typeCodec.encode(output, element);
        }
      },
      input -> {
        final int length = prefixCodec.decode(input);
        if (length > limit)
          throw new DecoderException(
            "Array too large, received " + length +
              ", but limited to " + limit
          );

        final List<T> array = new ObjectArrayList<>(length);
        for (int i = 0; i < length; i++) {
          array.add(typeCodec.decode(input));
        }

        return array;
      }
    );
  }
}
