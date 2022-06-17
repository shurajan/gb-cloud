package com.geekbrains.cloud.server.handler;

import com.geekbrains.cloud.model.*;
import com.geekbrains.cloud.server.services.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {
    private static final Path SERVER_ROOT;
    private String clientID;
    private Path currentDir;
    private AuthService authService;
    private boolean isAuthenticated;

    static {
        SERVER_ROOT = Path.of("server_files").toAbsolutePath();
    }

    public CloudFileHandler(AuthService authService) {
        this.authService = authService;
        this.isAuthenticated = false;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        //Ожидаем, что первое сообщение на аутентификацию
        if (cloudMessage instanceof AuthMessage authMessage) {

            String id = authMessage.isNewUser() ? authService.getIdByLogin(authMessage.getUserName()) :
                    authService.getIdByLoginAndPassword(authMessage.getUserName(), authMessage.getPassword());
            String outMessage = "";

            if (authMessage.isNewUser() && id == null) {
                authService.addNewUser(authMessage.getUserName(), authMessage.getPassword());
                id = authService.getIdByLoginAndPassword(authMessage.getUserName(), authMessage.getPassword());
                isAuthenticated = true;
                outMessage = "Success";
            } else {
                outMessage = "Try another login";
            }

            if (!authMessage.isNewUser() && id != null) {
                isAuthenticated = true;
            } else {
                outMessage = "Unknown user name or password";
            }

            ctx.writeAndFlush(new AuthAcceptMessage(isAuthenticated, outMessage));
            if (isAuthenticated) {
                clientID = id;
                currentDir = Paths.get(SERVER_ROOT.toString(), clientID).toAbsolutePath();
                if (!Files.exists(currentDir)) Files.createDirectory(currentDir);
                ctx.writeAndFlush(new ListFiles(currentDir));
            }
        }

        //Не обрабатываем запросы если не авторизовались
        if (!isAuthenticated) return;

        if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(currentDir));
        } else if (cloudMessage instanceof PathChangeRequest pathChangeRequest) {
            boolean isRefreshRequired = false;
            if (pathChangeRequest.getNewFolder().equals("..")) {
                if (currentDir.getParent() != null) {
                    if(!currentDir.getParent().equals(SERVER_ROOT)) {
                        currentDir = currentDir.getParent();
                        isRefreshRequired = true;
                    }
                }
            } else {
                Path newPath = Paths.get(currentDir.toString(), pathChangeRequest.getNewFolder());
                if (Files.exists(newPath) && Files.isDirectory(newPath)) {
                    currentDir = newPath;
                    isRefreshRequired = true;
                }
            }
            if (isRefreshRequired) ctx.writeAndFlush(new ListFiles(currentDir));
        }
    }
}
