package command;

import handlers.ZlibHandler;
import parser.GitParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class HashObjectCommand implements Command {
    private GitParser parser;
    private ZlibHandler handler = ZlibHandler.getInstance();

    public HashObjectCommand(GitParser parser) {
        this.parser = parser;
    }

    @Override
    public void execute(Map<String, String> options) {
        if (options.containsKey("w")) {
            String filename = options.get("w");
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] filecontent = Files.readAllBytes(Path.of(filename));
                byte[] header = new String("blob " + filecontent.length + "\0").getBytes();
                baos.write(header);
                baos.write(filecontent);
                String sha1Blob = parser.objectHash(baos.toByteArray());
                System.out.println(sha1Blob);
                handler.writeObject(sha1Blob, baos.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
