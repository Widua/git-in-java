package parser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class GitParser {

    private Map<String,String> options = new HashMap<>();
    private List<String> freeArgs = new ArrayList<>();

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