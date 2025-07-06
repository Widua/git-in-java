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
}