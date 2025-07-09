package parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class GitParser {

    private Map<String,String> options = new HashMap<>();
    private List<String> freeArgs = new ArrayList<>();
    private List<String> gitignored = new ArrayList<>();

    public Map<String, String> parseOptions(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].contains("--") || args[i].contains("-")) {
                String key = args[i].replaceAll("^-*", "");
                String value = "";
                if (i+1 < args.length){
                    value = args[i+1];
                }
                options.put(key, value);
                i++;
                continue;
            }
            if (args[i].contains("http://") || args[i].contains("https://")){
                String key = "url";
                options.put(key,args[i]);
                continue;
            }
            freeArgs.add(args[i]);
        }
        options.put("FreeArgs", String.join(" ", freeArgs));
        return options;
    }

    private void resolveGitignore(){
        Path gitignore = Path.of(".gitignore");
        if (gitignore.toFile().exists()){
            try {
                gitignored.addAll(Files.readAllLines(gitignore));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<String> getGitignored() {
        resolveGitignore();
        return gitignored;
    }

    public String objectHash(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(content);
            byte[] result = md.digest();

            StringBuilder builder = new StringBuilder();
            for (byte b : result) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] sha1HexToBytes(String sha1Hex) {
        int len = sha1Hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(sha1Hex.charAt(i), 16) << 4)
                    + Character.digit(sha1Hex.charAt(i + 1), 16));
        }
        return data;
    }
}