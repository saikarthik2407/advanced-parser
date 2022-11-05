import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class is a singleton which is used to store the content and properties of input instructions file.
 * Class stores code lines, last line number, current line under execution
 */
public class InputFile {

    private static InputFile sInstance;
    private final LinkedHashMap<Integer, String> codeLines = new LinkedHashMap<>();
    private Integer currentLineUnderExecution;
    private Integer firstLineNumber;
    private Integer lastLineNumber;

    // constructor made private because this is going to be a Singleton
    private InputFile() { }

    // returns the InputFile Singleton
    public static InputFile getInstance() {
        if (sInstance == null) {
            sInstance = new InputFile();
        }
        return sInstance;
    }

    public void setCodeLines(List<String> rawLines) {
        // format the rawLines to codeLines
        rawLines.forEach(rawLine -> {
            String lineNumberString = StringUtils.getFirstWord(rawLine);
            Integer lineNumber = Integer.parseInt(lineNumberString);
            String codeLine = StringUtils.removeFirstWordFromString(rawLine);
            codeLines.put(lineNumber, codeLine);
        });
        // set the lastLineNumber
        Object[] keys =  codeLines.keySet().toArray();
        setFirstLineNumber((Integer) keys[0]);
        setLastLineNumber((Integer) keys[keys.length - 1]);
    }

    public LinkedHashMap<Integer, String> getCodeLines() {
        return codeLines;
    }

    public void setCurrentLineUnderExecution(Integer lineNumber) {
        currentLineUnderExecution = lineNumber;
    }

    public Integer getCurrentLineUnderExecution() {
        return currentLineUnderExecution;
    }

    private void setFirstLineNumber(Integer firstLineNumber) {
        this.firstLineNumber = firstLineNumber;
    }

    public Integer getFirstLineNumber() {
        return firstLineNumber;
    }

    private void setLastLineNumber(Integer lineNumber) {
        lastLineNumber = lineNumber;
    }

    public Integer getLastLineNumber() {
        return lastLineNumber;
    }
}