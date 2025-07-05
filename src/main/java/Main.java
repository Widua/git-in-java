import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Main {
  public static void main(String[] args){
	final String command = args[0];

	switch (command) {
		case "init" -> {
				final File root = new File(".git");
				new File(root,"objects").mkdirs();
				new File(root, "refs").mkdirs();
				final File head = new File(root,"HEAD");

				try{
					head.createNewFile();
					Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
					System.out.println("Initialized git directory");
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		case "cat-file" -> {
			String cfSwitch = args[1];
			switch (cfSwitch){
				case "-p" -> {
					String blobObjectHash = args[2];
					if (blobObjectHash == null){
						System.out.println("Blob object hash not provided");
						return;
					}
					String objectDirectory = blobObjectHash.substring(0,2);
					String objectFileName = blobObjectHash.substring(2);

					byte[] hashedFile = readHashedFile(Path.of(String.format(".git/objects/%s/%s",objectDirectory,objectFileName)));
					byte[] decompressedFile = decompressZlib(hashedFile);

					byte[] format = Arrays.copyOfRange(decompressedFile,0,5);
					if (new String(format).equals("blob ")){
						int nullIndex = 0;
						for (int i = 4; i < decompressedFile.length; i++) {
							if (decompressedFile[i]==0){
								nullIndex = i;
								break;
							}
						}
						byte[] size = Arrays.copyOfRange(decompressedFile,5,nullIndex);
						byte[] content = Arrays.copyOfRange(decompressedFile,nullIndex+1,decompressedFile.length-1);
						String fileContent = new String(content);
						System.out.print(fileContent);
					}
				}
				default -> {
					System.out.println("Unknown command: "+command+" "+cfSwitch);
				}
			}
		}
		default -> {
				System.out.println("Unknown command: "+command);
			}
	}
  }


  public static byte[] readHashedFile(Path path){
      try {
          return Files.readAllBytes(path);
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
  }

  public static byte[] decompressZlib(byte[] input) {
	  Inflater inflater = new Inflater();
	  inflater.setInput(input);

	  ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	  byte[] buffer = new byte[1024];

	  while (!inflater.finished()){
          int decompressedSize = 0;
          try {
              decompressedSize = inflater.inflate(buffer);
          } catch (DataFormatException e) {
              throw new RuntimeException(e);
          }
          outputStream.write(buffer,0,decompressedSize);
	  }

	  return outputStream.toByteArray();
  }

}
