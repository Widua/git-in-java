package handlers;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
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

    public byte[] zlibCompress(byte[] content) {
        Deflater deflater = new Deflater();
        byte[] output = new byte[100];

        deflater.setInput(content);
        deflater.finish();
        deflater.deflate(output);
        deflater.end();

        return output;
    }
}
