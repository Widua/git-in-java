package parser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class GitParser {
    public Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].contains("--") || args[i].contains("-")) {
                String key = args[i].replaceAll("^-*", "");
                String value = args[i + 1];
                options.put(key, value);
                i++;
            }
        }
        return options;
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
}