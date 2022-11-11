import java.io.File;

public class Main {
    public static void main(String[] args) {
        // retrieve the file from the device based on the file name given in the command line arguments
        File silFile = new File(args[0]);

        // Create an instance of File Parser that supports SIL file parsing
        FileParser silFileParser = new FileParser();

        // Start SIL file parsing
        silFileParser.parse(silFile);
    }
}