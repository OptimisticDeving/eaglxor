package dev.optimistic.eaglxor;

import com.google.common.base.Suppliers;
import dev.optimistic.eaglxor.pipeline.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import net.minecraft.server.network.ServerConnectionListener;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

public final class Main extends JavaPlugin {
  public static final Supplier<Main> INSTANCE_SUPPLIER = Suppliers
    .memoize(() -> JavaPlugin.getPlugin(Main.class));
  private ChannelInitializer<Channel> vanillaChannelInitializer;
  private MethodHandle initChannelHandle;

  @SuppressWarnings("unchecked")
  @Override
  public void onLoad() {
    try {
      final Class<?> serverConnectionListenerChannelHandlerClass =
        Class.forName("net.minecraft.server.network" +
          ".ServerConnectionListener$1");
      final var constructor = serverConnectionListenerChannelHandlerClass
        .getDeclaredConstructor(ServerConnectionListener.class);
      constructor.setAccessible(true);
      this.vanillaChannelInitializer = (ChannelInitializer<Channel>) constructor
        .newInstance(((CraftServer) this.getServer()).getServer().getConnection());

      final var ourLookup = MethodHandles.lookup();
      final var nettyLookup =
        MethodHandles.privateLookupIn(
          ChannelInitializer.class,
          ourLookup
        );
      this.initChannelHandle = nettyLookup.findVirtual(
        ChannelInitializer.class,
        "initChannel",
        MethodType.methodType(void.class, Channel.class)
      );
    } catch (Exception e) {
      throw
        new RuntimeException(
          "Failed to reflectively access NMS netty handler",
          e
        );
    }
  }

  @Override
  public void onEnable() {
    final boolean epoll = Epoll.isAvailable();
    final Class<? extends ServerChannel> serverChannel = epoll
      ?
      EpollServerSocketChannel.class
      :
      NioServerSocketChannel.class;
    final EventLoopGroup eventLoopGroup = epoll ?
      new EpollEventLoopGroup()
      :
      new NioEventLoopGroup();

    final Initializer initializer = new Initializer();

    new ServerBootstrap()
      .group(eventLoopGroup)
      .channel(serverChannel)
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(initializer)
      .bind(42069);
  }

  private final class Initializer
    extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
      try {
        initChannelHandle.invoke(
          vanillaChannelInitializer,
          ch
        );
      } catch (Throwable e) {
        throw new RuntimeException(
          "Unexpected exception in NMS initChannel",
          e
        );
      }

      ch.pipeline().addFirst(EaglerHandlerCaller.INSTANCE);
      ch.pipeline().addFirst(new EaglerPacketCodec());
      ch.pipeline().addFirst(SimpleWebsocketCodec.INSTANCE);
      ch.pipeline().addFirst(new WebSocketServerProtocolHandler("/socket"));
      ch.pipeline().addFirst(ExceptionIgnorer.INSTANCE);
      ch.pipeline().addFirst(new HttpObjectAggregator(65535));
      ch.pipeline().addFirst(new HttpServerCodec());
      ch.pipeline().replace(
        "prepender",
        "prepender",
        new ChannelOutboundHandlerAdapter()
      );
      ch.pipeline().replace(
        "splitter",
        "splitter",
        new ChannelInboundHandlerAdapter()
      );
      ch.pipeline().addAfter(
        "via-encoder",
        "success-skipper",
        LoginSuccessSkipper.INSTANCE
      );
    }
  }
}
