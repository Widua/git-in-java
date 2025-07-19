package command;

import handlers.ZlibHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LsTreeCommand implements Command {
    private final ZlibHandler zlib = ZlibHandler.getInstance();

    @Override
    public void execute(Map<String, String> options) {
        String hash;

        if (options.containsKey("name-only")) {
            hash = options.get("name-only");
        } else {
            hash = options.get("FreeArgs");
        }

        String directory = hash.substring(0, 2);
        String fileName = hash.substring(2);

        Path path = Path.of(".git/objects/", directory, fileName);

        byte[] tree = readShaFile(path);
        int headerEndingIndex = getIndexOfFirstNull(tree);
        String header = new String(Arrays.copyOfRange(tree, 0, headerEndingIndex));
        byte[] headerlessTree = Arrays.copyOfRange(tree, headerEndingIndex + 1, tree.length);
        List<String> entries = new ArrayList<>();
        for (int i = 0; i < headerlessTree.length; ) {
            int nullIndex = getIndexOfFirstNull(Arrays.copyOfRange(headerlessTree, i, headerlessTree.length)) + i;
            String modeAndName = new String(Arrays.copyOfRange(headerlessTree, i, nullIndex));
            String readableSHA1 = assembleSHA1(Arrays.copyOfRange(headerlessTree, nullIndex + 1, nullIndex + 20));
            entries.add(modeAndName + " " + readableSHA1);
            i = (nullIndex + 21);
        }
        if (options.containsKey("name-only")) {
            entries.stream().map((entry) -> entry.split(" ")[1]).forEach(System.out::println);
        } else {
            entries.forEach(System.out::println);
        }


    }

    private byte[] readShaFile(Path path) {
        try {
            byte[] fileContent = Files.readAllBytes(path);
            return zlib.decompressZlib(fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String assembleSHA1(byte[] bytes) {
        StringBuilder builder = new StringBuilder();

        for (byte aByte : bytes) {
            builder.append(String.format("%02x", aByte));
        }
        return builder.toString();
    }


    private int getIndexOfFirstNull(byte[] bytearr) {
        for (int i = 0; i < bytearr.length; i++) {
            if (bytearr[i] == 0) {
                return i;
            }
        }
        return 0;
    }
}
