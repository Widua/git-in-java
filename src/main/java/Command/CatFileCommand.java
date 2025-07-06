package Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class CatFileCommand implements Command {
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
            byte[] decompressedFile = decompressZlib(hashedFile);

            byte[] format = Arrays.copyOfRange(decompressedFile, 0, 5);
            if (new String(format).equals("blob ")) {
                int nullIndex = 0;
                for (int i = 4; i < decompressedFile.length; i++) {
                    if (decompressedFile[i] == 0) {
                        nullIndex = i;
                        break;
                    }
                }
                byte[] size = Arrays.copyOfRange(decompressedFile, 5, nullIndex);
                byte[] content = Arrays.copyOfRange(decompressedFile, nullIndex + 1, decompressedFile.length);
                String fileContent = new String(content);
                System.out.print(fileContent);
            }
        }
    }

    public byte[] readHashedFile(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] decompressZlib(byte[] input) {
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!inflater.finished()) {
            int decompressedSize = 0;
            try {
                decompressedSize = inflater.inflate(buffer);
            } catch (DataFormatException e) {
                throw new RuntimeException(e);
            }
            outputStream.write(buffer, 0, decompressedSize);
        }

        return outputStream.toByteArray();
    }

}
