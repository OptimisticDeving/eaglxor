package dev.optimistic.eaglxor;

import com.google.common.base.Suppliers;
import com.viaversion.viaversion.api.Via;
import dev.optimistic.eaglxor.pipeline.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
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
import net.minecraft.network.HandlerNames;
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
  private static final String ACCEPT_PATH_KEY = "accept-path";
  private ChannelInitializer<Channel> vanillaChannelInitializer;
  private MethodHandle initChannelHandle;
  private String acceptPath;

  @SuppressWarnings("unchecked")
  @Override
  public void onLoad() {
    this.getConfig().options().copyDefaults(true);
    this.saveConfig();

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

    this.acceptPath = getConfig().getString(ACCEPT_PATH_KEY);
    if (this.acceptPath == null)
      throw new IllegalStateException(ACCEPT_PATH_KEY + " must be non-null");
    if (!this.acceptPath.startsWith("/")) {
      this.acceptPath = "/" + this.acceptPath;
      getConfig().set(ACCEPT_PATH_KEY, this.acceptPath);
      saveConfig();
    }

    new ServerBootstrap()
      .group(eventLoopGroup)
      .channel(serverChannel)
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(initializer)
      .bind(getConfig().getInt("bind-port", 42069));
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
      ch.pipeline().addFirst(
        new WebSocketServerProtocolHandler(
          acceptPath
        )
      );
      ch.pipeline().addFirst(ExceptionIgnorer.INSTANCE);
      ch.pipeline().addFirst(new HttpObjectAggregator(65535));
      ch.pipeline().addFirst(new HttpServerCodec());
      ch.pipeline().replace(
        HandlerNames.PREPENDER,
        HandlerNames.PREPENDER,
        Stubs.OUTBOUND_STUB
      );
      ch.pipeline().replace(
        HandlerNames.SPLITTER,
        HandlerNames.SPLITTER,
        Stubs.INBOUND_STUB
      );
      ch.pipeline().addAfter(
        Via.getManager().getInjector().getEncoderName(),
        "success-skipper",
        LoginSuccessSkipper.INSTANCE
      );
      ch.pipeline().addLast(CompressionRemover.INSTANCE);
    }
  }
}
