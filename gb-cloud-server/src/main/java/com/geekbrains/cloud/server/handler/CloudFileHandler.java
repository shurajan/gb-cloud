package com.geekbrains.cloud.server.handler;

import com.geekbrains.cloud.model.*;
import com.geekbrains.cloud.server.services.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;
    private AuthService authService;
    private boolean isAuthenticated;

    public CloudFileHandler(AuthService authService) {
        System.out.println("Клиент подключился");
        this.currentDir = Path.of("server_files").toAbsolutePath();
        this.authService = authService;
        this.isAuthenticated = false;
    }

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        if (!isAuthenticated) return;
//
//        ctx.writeAndFlush(new ListFiles(currentDir));
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        //Ожидаем, что первое сообщение на аутентификацию
        if (cloudMessage instanceof AuthMessage authMessage) {
            if (authMessage.isNewUser()) {
                String id = authService.getIdByLogin(authMessage.getUserName());
                if (id == null) {
                    authService.addNewUser(authMessage.getUserName(), authMessage.getPassword());
                    id = authService.getIdByLoginAndPassword(authMessage.getUserName(), authMessage.getPassword());
                    System.out.println(id);
                    ctx.writeAndFlush(new AuthAcceptMessage(true, "Success"));
                    ctx.writeAndFlush(new ListFiles(currentDir));
                } else {
                    ctx.writeAndFlush(new AuthAcceptMessage(false, "Try another login"));
                }
            } else {
                String id = authService.getIdByLoginAndPassword(authMessage.getUserName(), authMessage.getPassword());
                if (id != null) {
                    isAuthenticated = true;
                    ctx.writeAndFlush(new AuthAcceptMessage(true, "Success"));
                    ctx.writeAndFlush(new ListFiles(currentDir));
                } else {
                    ctx.writeAndFlush(new AuthAcceptMessage(false, "Unknown user name or password"));
                }
            }

        }

        //Не обрабатываем запрсы если не авторизовались
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
                    currentDir = currentDir.getParent();
                    isRefreshRequired = true;
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
