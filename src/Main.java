import java.io.File;

public class Main {

    public static void main(String[] args) {

        // retrieve the file from the file name given in command line arguments
        File customCommandsFile = new File(args[0]);

        // Create an instance of File Parser that supports custom file parsing
        FileParser fileParser = new FileParser();
        fileParser.parse(customCommandsFile);
    }
}