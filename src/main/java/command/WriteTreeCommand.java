package command;

import parser.GitParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class WriteTreeCommand implements Command{
    private final GitParser parser;
    List<String> ignored ;

    public WriteTreeCommand(GitParser parser) {
        this.parser = parser;
        ignored = parser.getGitignored();
    }

    @Override
    public void execute(Map<String, String> options) {
        Path root = Paths.get("");

        resolveDirectory(root);
    }


    private void resolveDirectory(Path path){
        if (path.toString().contains(".git")|| ignored.stream().anyMatch((s)->path.toString().contains(s))  ){
            return;
        }
        File file = new File(path.toUri());
        if (file.isDirectory()){
            try {
                Files.list(path).forEach(this::resolveDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println(path);
        }
    }

}
