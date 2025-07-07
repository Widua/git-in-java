package command;

import parser.GitParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WriteTreeCommand implements Command {
    private final GitParser parser;
    List<String> ignored;
    String currentDirectory = "";
    Map<String, List<String>> structure = new HashMap<>();

    public WriteTreeCommand(GitParser parser) {
        this.parser = parser;
        ignored = parser.getGitignored();
    }

    @Override
    public void execute(Map<String, String> options) {
        Path root = Paths.get("");

        resolveDirectory(root);
        System.out.println(structure);
    }


    private void resolveDirectory(Path path) {
        if (path.toString().contains(".git") || ignored.stream().anyMatch((s) -> path.toString().contains(s.replace("/", "")))) {
            return;
        }
        String currentPath = path.toString();
        File file = new File(path.toUri());
        if (file.isDirectory()) {
            String currentPathIn = currentPath.split("/")[currentPath.split("/").length - 1];
            currentDirectory = currentPathIn;
            List<String> fileList = new ArrayList<>();

            fileList.add(currentDirectory+"/");
            structure.put(currentPathIn,fileList);
            try {
                Files.list(path).forEach(this::resolveDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String[] splittedPath = currentPath.split("/");
            if (splittedPath.length > 1){
                currentDirectory = splittedPath[splittedPath.length-2];
            } else {
                currentDirectory = "";
            }
            String currentFile = splittedPath[splittedPath.length-1];
            List<String> currentFiles = structure.get(currentDirectory);
            currentFiles.add(currentFile);
            structure.put(currentDirectory, currentFiles);
        }
    }

}
