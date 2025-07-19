package handlers;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class ZlibHandler {
    private static ZlibHandler instance;

    private ZlibHandler() {
    }

    public static ZlibHandler getInstance() {
        if (instance == null) {
            instance = new ZlibHandler();
        }
        return instance;
    }

    public void writeObject(String sha1, byte[] content) throws IOException {
        String directory = sha1.substring(0, 2);
        String fileName = sha1.substring(2);
        new File(".git/objects", directory).mkdir();
        try (
                FileOutputStream os = new FileOutputStream(new File(Path.of(".git/objects", directory, fileName).toUri()));
                DeflaterOutputStream deflater = new DeflaterOutputStream(os)
        ) {
            deflater.write(content);
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
