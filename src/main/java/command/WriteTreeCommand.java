package command;

import handlers.ZlibHandler;
import parser.GitParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WriteTreeCommand implements Command {
    private final GitParser parser;
    private final ZlibHandler zlib = ZlibHandler.getInstance();
    List<String> ignored;
    Map<String, List<String>> structure = new HashMap<>();
    Map<String,List<String>> trees = new HashMap<>();

    public WriteTreeCommand(GitParser parser) {
        this.parser = parser;
        ignored = parser.getGitignored();
        trees.put("",new ArrayList<>());
    }

    @Override
    public void execute(Map<String, String> options) {
        Path root = Paths.get("");

        resolveDirectory(root);
        // System.out.println(structure);
    }


    private void resolveDirectory(Path path) {
        if (path.toString().contains(".git") || ignored.stream().anyMatch((s) -> path.toString().contains(s.replace("/", "")))) {
            return;
        }
        System.out.println(path);
        String currentDirectory = "";
        String currentPath = path.toString();
        Path parentDir = Path.of("");
        String[] splittedPath = currentPath.split("/");
        File file = new File(path.toUri());
        if (file.isDirectory()) {
            String parentDirectory = "";
            String directoryName = splittedPath[splittedPath.length-1];
            currentDirectory = directoryName;
            trees.put(directoryName,new ArrayList<>());
            structure.put(directoryName,new ArrayList<>());

            if (splittedPath.length > 1){
                parentDirectory = splittedPath[splittedPath.length-2];
            }
            List<String> currList = structure.get(parentDirectory);
            if (!currentDirectory.equals(parentDirectory)){
               currList.add(currentDirectory+"/");
            }
            try {
                Files.list(path).forEach(this::resolveDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (splittedPath.length > 1){
                parentDir = Path.of("",Arrays.copyOfRange(splittedPath,0,splittedPath.length-2));
            }
            writeBlob(path,parentDir);
        }
    }

    private void checkEntries(Path parent){
    }

    private void writeBlob(Path path,Path parent){
        try {
            System.out.println("PARENT: "+parent.toString());
            System.out.println("ENTRIES: "+trees);
            boolean isExecutable = new File(path.toString()).canExecute();
            byte[] fileContent = Files.readAllBytes(path);
            byte[] header = new String("blob "+fileContent.length+"\0").getBytes();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(header);
            baos.write(fileContent);
            String sha1Hash = parser.objectHash(baos.toByteArray());
            String blobDirectory = sha1Hash.substring(0,2);
            String blobfile = sha1Hash.substring(2);
            File bloblocation= new File(".git/objects/"+blobDirectory);
            bloblocation.mkdir();
            Files.write( Path.of(bloblocation.toString(),blobfile), zlib.zlibCompress(baos.toByteArray()) );

            String entryMode;
            String entryName = path.getFileName().toString();

            if (isExecutable){
                entryMode = "100755 ";
            } else {
                entryMode = "100644 ";
            }
            ByteArrayOutputStream entry = new ByteArrayOutputStream();
            entry.write(entryMode.getBytes());
            entry.write(entryName.getBytes());
            entry.write("\0".getBytes());
            entry.write(sha1Hash.getBytes());

            List<String> entries = trees.get(parent.toString());

            entries.add(entry.toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
