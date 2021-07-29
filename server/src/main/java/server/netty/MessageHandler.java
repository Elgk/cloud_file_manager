package server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    private Path currentPath;
    private String authResultMessage;

    public MessageHandler() throws IOException {
        currentPath = Path.of("server_dir");
        if (!Files.exists(currentPath)){
            Files.createDirectory(currentPath);
        }
    }
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(new ListResponse(currentPath));
//        ctx.writeAndFlush(new PathUpResponse(currentPath.toString()));
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) throws Exception {
        log.debug("recieve{} ", command.getType());
        switch (command.getType()){
            case FILE_REQUEST:
                FileRequest fileRequest = (FileRequest) command;
                FileMessage msg = new FileMessage(currentPath.resolve(fileRequest.getName()));
                ctx.writeAndFlush(msg);
                break;
            case FILE_MESSAGE:
                FileMessage message = (FileMessage) command;
                Files.write(currentPath.resolve(message.getName()), message.getData());
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_UP:
                if (currentPath.getParent() != null){
                    currentPath = currentPath.getParent();
                }
                ctx.writeAndFlush(new PathUpResponse(currentPath.toString()));
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
            case LIST_REQUEST:
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_IN_REQUEST:
                PathInRequest request = (PathInRequest) command;
                Path newPath = currentPath.resolve(request.getDir());
                if (Files.isDirectory(newPath)){
                    currentPath = newPath;
                    ctx.writeAndFlush(new PathUpResponse(currentPath.toString()));
                    ctx.writeAndFlush(new ListResponse(currentPath));
                }
                break;
            case AUTH_REQUEST:
                AuthRequest authRequest = (AuthRequest) command;
               // String result = authentication(authRequest.getLogin(), authRequest.getPassword());
                if (authResult(authRequest.getLogin(), authRequest.getPassword())){
                    ctx.writeAndFlush(new AuthOkResponse(authResultMessage));
                    ctx.writeAndFlush(new ListResponse(currentPath));
                    ctx.writeAndFlush(new PathUpResponse(currentPath.toString()));
                }else {
                    ctx.writeAndFlush(new AuthNoResponse(authResultMessage));
                }
                break;
        }
    }

    private boolean authResult(String login, String password){
        String[] userData = SQLHandler.getNicknameByLoginPassword(login, password);
        if (userData != null) {
            String userFolder = SQLHandler.getUserFolder(currentPath.toString(), userData[1] );
            String nickName =  userData[0];
            if (userFolder != null){
              //  currentPath = Path.of(userFolder);
                currentPath = currentPath.resolve(userFolder);
                authResultMessage = userData[0];
                return true;
            }
            authResultMessage = "No access";
            return false;
        }else {
            authResultMessage = "Entered login or password is not correct";
            return false;
        }
    }
}
