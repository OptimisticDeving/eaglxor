package dev.optimistic.eaglxor.pipeline;

import io.netty.channel.*;

public final class Stubs {
  public static final ChannelInboundHandler INBOUND_STUB =
    InboundStub.INSTANCE;
  public static final ChannelOutboundHandler OUTBOUND_STUB =
    OutboundStub.INSTANCE;


  private Stubs() {

  }

  @ChannelHandler.Sharable
  private static final class InboundStub extends ChannelInboundHandlerAdapter {
    public static final InboundStub INSTANCE = new InboundStub();

    private InboundStub() {

    }
  }

  @ChannelHandler.Sharable
  private static final class OutboundStub extends ChannelOutboundHandlerAdapter {
    public static final OutboundStub INSTANCE = new OutboundStub();

    private OutboundStub() {

    }
  }
}
