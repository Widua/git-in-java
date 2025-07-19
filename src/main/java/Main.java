import command.*;
import parser.GitParser;

import java.util.Arrays;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        final GitParser parser = new GitParser();
        Command command = null;
        Map<String, String> options = parser.parseOptions(Arrays.copyOfRange(args, 1, args.length));
        switch (args[0]) {
            case "init" -> {
                command = new InitCommand();
            }
            case "cat-file" -> {
                command = new CatFileCommand();
            }
            case "hash-object" -> {
                command = new HashObjectCommand(parser);
            }
            case "ls-tree" -> {
                command = new LsTreeCommand();
            }
            case "write-tree" -> {
                command = new WriteTreeCommand(parser);
            }
            case "commit-tree" -> {
                command = new CommitTreeCommand(parser);
            }
            default -> {
                System.out.println("Unknown command: " + args[0]);
            }
        }
        if (command != null) {
            command.execute(options);
        }
    }
}
