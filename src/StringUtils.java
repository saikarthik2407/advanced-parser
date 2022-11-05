public class StringUtils {

    public static String getFirstWord(String sentence) {
        return sentence.split(" ")[0];
    }

    public static Boolean isValidVariableName(String variableName) {
        char firstLetter = variableName.charAt(0);
        Boolean isVariableFirstLetterAppropriate = ((firstLetter >= 'a' && firstLetter <= 'z') ||
                (firstLetter >= 'A' && firstLetter <= 'Z') || firstLetter == '_' || firstLetter == '$');
        Boolean areVariableLettersValid = variableName.matches("[a-zA-Z0-9_$]+");
        return isVariableFirstLetterAppropriate && areVariableLettersValid;
    }

    public static String removeFirstWordFromString(String actualString) {
        int wordStartPosition = getFirstWord(actualString).length();
        while (actualString.charAt(wordStartPosition) == ' ') {
            wordStartPosition += 1;
        }
        return actualString.substring(wordStartPosition);
    }
}
