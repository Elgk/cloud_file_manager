package server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class Server {
    public Server(){

        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    //   new ByteInboundHandler());
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new MessageHandler()
                            );
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(8189).sync();
            log.debug("Server started...");
            if (!SQLHandler.connect()) {
                RuntimeException e = new RuntimeException("Attempt to connect to DB is failed");
                throw e;
            }
            log.debug("DB connection is open...");
            channelFuture.channel().closeFuture().sync();  //block
        } catch (Exception e) {
            log.error("  ", e);
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
            try {
                SQLHandler.disconnect();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }



}
