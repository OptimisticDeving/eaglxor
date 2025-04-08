package dev.optimistic.eaglxor.pipeline;

import dev.optimistic.eaglxor.packets.state.StateHandler;
import dev.optimistic.eaglxor.packets.state.impl.HandshakeStateHandler;
import dev.optimistic.eaglxor.types.ProtocolVersion;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EaglerAttrs {
  public static final EaglerAttribute<String> BRAND =
    new EaglerAttribute<>(
      "brand",
      "Unknown"
    );
  public static final EaglerAttribute<String> BRAND_VERSION =
    new EaglerAttribute<>(
      "brand_version",
      "v0.0.0"
    );
  public static final EaglerAttribute<StateHandler> STATE_HANDLER =
    new EaglerAttribute<>(
      "state_handler",
      HandshakeStateHandler.INSTANCE
    );
  public static final EaglerAttribute<String> USERNAME =
    new EaglerAttribute<>("username");
  public static final EaglerAttribute<ProtocolVersion> PROTOCOL_VERSION =
    new EaglerAttribute<>(
      "protocol_version",
      ProtocolVersion.PENDING
    );
  public static final EaglerAttribute<Integer> GAME_VERSION =
    new EaglerAttribute<>(
      "game_version"
    );

  private EaglerAttrs() {


  }

  private static <T> AttributeKey<T> newKey(String name) {
    return AttributeKey.newInstance("eaglxor:" + name);
  }

  public static final class EaglerAttribute<T> {
    private final AttributeKey<T> attributeKey;
    private final @Nullable T theDefault;

    private EaglerAttribute(
      String name,
      @Nullable T theDefault
    ) {
      this.attributeKey = AttributeKey.newInstance("eaglxor:" + name);
      this.theDefault = theDefault;
    }

    private EaglerAttribute(String name) {
      this(name, null);
    }

    public @NotNull T getValue(ChannelHandlerContext ctx) {
      final var attr = ctx.channel().attr(this.attributeKey);
      final var attrVal = attr.get();
      if (attrVal != null) return attrVal;
      if (this.theDefault == null)
        throw new UnsupportedOperationException(
          attributeKey.name() + " accessed before init"
        );
      attr.set(this.theDefault);
      return this.theDefault;
    }

    public void setValue(
      ChannelHandlerContext ctx,
      @NotNull T newValue
    ) {
      ctx.channel().attr(this.attributeKey).set(newValue);
    }
  }
}