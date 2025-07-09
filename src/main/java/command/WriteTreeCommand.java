package command;

import handlers.ZlibHandler;
import parser.GitParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WriteTreeCommand implements Command {
    private final GitParser parser;
    private final ZlibHandler zlib = ZlibHandler.getInstance();
    private final List<String> ignored;
    @Override
    public void execute(Map<String, String> options) {
        try {
            String treeSha = createTree(new File("."));
            System.out.println(treeSha);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public WriteTreeCommand(GitParser parser){
       this.parser = parser;
       ignored = parser.getGitignored();
    }

    private String createTree(File directory) throws IOException {
        File[] files = directory.listFiles((file, name )-> !name.contains(".git") && ignored.stream().noneMatch((ignore)->name.contains(ignore.replace("/",""))));
        if (files == null){
            return null;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        ByteArrayOutputStream tree = new ByteArrayOutputStream();
        for (File file : files) {
            String entryMode;
            String sha1;
            String fileName = file.getName();
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            if (file.isFile()){
                if (file.canExecute()){
                    entryMode = "100755";
                } else {
                    entryMode= "100644";
                }
                byte[] fileContent = Files.readAllBytes(file.toPath());
                byte[] header = new String( "blob "+fileContent.length+"\0").getBytes();
                blob.write(header);
                blob.write(fileContent);
                sha1 = parser.objectHash(blob.toByteArray());
                byte[] encoded = zlib.zlibCompress(blob.toByteArray());
                writeObject(sha1,encoded);
            } else {
                entryMode = "40000";
                sha1 = createTree(file);
            }
            tree.write( new String(entryMode+" "+fileName+"\0").getBytes() );
            tree.write(parser.sha1HexToBytes(sha1));
        }
        ByteArrayOutputStream fullTree = new ByteArrayOutputStream();
        byte[] treeHeader = new String("tree "+tree.toByteArray().length+"\0").getBytes();
        fullTree.write(treeHeader);
        fullTree.write(tree.toByteArray());
        byte[] fullTreeArr = fullTree.toByteArray();
        String treeSha1 = parser.objectHash(fullTreeArr);
        byte[] compressedTree = zlib.zlibCompress(fullTreeArr);
        writeObject(treeSha1, compressedTree);

        return treeSha1;
    }

    private void writeObject(String sha1, byte[] encodedFile) throws IOException {
        String directory = sha1.substring(0,2);
        String fileName = sha1.substring(2);
        new File(".git/objects",directory).mkdir();
        Files.write(Path.of(".git/objects/",directory,fileName),encodedFile);
    }

}
