package command;

import handlers.ZlibHandler;
import parser.GitParser;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

public class CommitTreeCommand implements Command {
    private final GitParser parser;
    private final ZlibHandler handler = ZlibHandler.getInstance();


    public CommitTreeCommand(GitParser parser) {
        this.parser = parser;
    }

    @Override
    public void execute(Map<String, String> options) {
        String treeSha = options.get("FreeArgs");
        String commitSha = options.get("p");
        String message = options.get("m");

        String author = String.format("author %s <%s> %d +0000\n","Widua","mikolaj.widla@gmail.com",System.currentTimeMillis()/100);

        StringBuilder commitContentBuilder= new StringBuilder();

        commitContentBuilder.append(String.format("tree %s\n",treeSha));
        commitContentBuilder.append(String.format("parent %s\n",commitSha));
        commitContentBuilder.append(author);
        commitContentBuilder.append(author.replaceAll("author","commiter"));
        commitContentBuilder.append("\n").append(message).append("\n");
        try {
            System.out.println(writeCommit(commitContentBuilder.toString().getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String writeCommit(byte[] commit) throws IOException {
        byte[] commitHeader = String.format("commit %d\0",commit.length).getBytes();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write(commitHeader);
        outputStream.write(commit);

        String SHA1 = parser.objectHash(outputStream.toByteArray());
        handler.writeObject(SHA1,outputStream.toByteArray());
        return SHA1;
    }
}
