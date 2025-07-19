package command;

import handlers.ZlibHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class CatFileCommand implements Command {

    private ZlibHandler handler = ZlibHandler.getInstance();

    @Override
    public void execute(Map<String, String> options) {
        if (options.containsKey("p")) {
            String blobObjectHash = options.get("p");
            if (blobObjectHash == null) {
                System.out.println("Blob object hash not provided");
                return;
            }
            String objectDirectory = blobObjectHash.substring(0, 2);
            String objectFileName = blobObjectHash.substring(2);

            byte[] hashedFile = readHashedFile(Path.of(String.format(".git/objects/%s/%s", objectDirectory, objectFileName)));
            byte[] decompressedFile = handler.decompressZlib(hashedFile);

            byte[] format = Arrays.copyOfRange(decompressedFile, 0, 5);
            if (new String(format).equals("blob ")) {
                int nullIndex = 0;
                for (int i = 4; i < decompressedFile.length; i++) {
                    if (decompressedFile[i] == 0) {
                        nullIndex = i;
                        break;
                    }
                }
                byte[] content = Arrays.copyOfRange(decompressedFile, nullIndex + 1, decompressedFile.length);
                String fileContent = new String(content);
                System.out.print(fileContent);
            }
        }
    }

    private byte[] readHashedFile(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
