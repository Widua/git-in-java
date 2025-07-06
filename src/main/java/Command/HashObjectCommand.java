package Command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.zip.Deflater;

public class HashObjectCommand implements Command {
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

                String sha1Blob = hashSHA1(baos.toByteArray());

                System.out.println(sha1Blob);
                String directoryName = sha1Blob.substring(0,2);
                String fileName = sha1Blob.substring(2);

                File root = new File(".git/objects");
                File hashedFileDirectory = new File(root, directoryName);
                hashedFileDirectory.mkdir();

                Files.write(Path.of(hashedFileDirectory.getPath(), fileName), zlibCompress(baos.toByteArray()) );

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String hashSHA1(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(content);
            byte[] result = md.digest();

            StringBuilder builder = new StringBuilder();
            for (byte b : result) {
               builder.append(String.format("%02x",b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] zlibCompress(byte[] content) {
        Deflater deflater = new Deflater();
        byte[] output = new byte[100];

        deflater.setInput(content);
        deflater.finish();
        deflater.deflate(output);
        deflater.end();

        return output;
    }

}
