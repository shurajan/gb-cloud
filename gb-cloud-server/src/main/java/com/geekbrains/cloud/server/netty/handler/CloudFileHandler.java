package com.geekbrains.cloud.server.netty.handler;

import com.geekbrains.cloud.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;

    public CloudFileHandler() {
        currentDir = Path.of("server_files").toAbsolutePath();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListFiles(currentDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
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
            if(isRefreshRequired) ctx.writeAndFlush(new ListFiles(currentDir));
        }
    }
}
