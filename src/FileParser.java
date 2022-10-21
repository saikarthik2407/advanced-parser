import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class FileParser {

    private final Map<String, Integer> valueStore = new HashMap<>();
    private final List<String> codeLines = new ArrayList<>();

    FileParser() { }

    /**
     * Main parse function
     * @param file the file that needs to be parsed
     */
    public void parse(File file) {
        try  {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            splitCodeToSegments(bufferedReader);
            examineCodeLine();
        } catch (IOException e) {
            System.out.println("Error while reading a file.");
        }
    }

    /**
     * The function splits the whole file into lines and stores those lines as list of strings
     * @param br is the Buffered reader that has the file stored in it as String Buffer
     */
    private void splitCodeToSegments(BufferedReader br) {
        String line;
        try {
            while ((line = br.readLine()) != null) {
                codeLines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error while reading the lines.");
        }
    }

    /**
     * The function examines each line of code to parse
     */
    private void examineCodeLine() {
        codeLines.forEach(new Consumer<String>() {
            @Override
            public void accept(String lineOfCode) {
                String[] codeWordsArray = lineOfCode.split(" ");
                switch (codeWordsArray[0]) {
                    case "LET" -> examineInitializationStatement(codeWordsArray);
                    case "PRINT", "PRINTLN" -> examinePrintStatements(codeWordsArray);
                    default -> System.out.println("Syntax error occurred while parsing");
                }
            }
        });
    }

    /**
     * This function builds a numerical expression from a variable expression by retrieving the values of
     * variables from the hashMap which we stored during initialization
     * @param mathExpressionBuilder is the numerical expression builder
     * @param variableExpression is the expression that has variables instead of numbers
     * @return the numerical expression
     */
    private String buildMathematicalExpression(StringBuilder mathExpressionBuilder, String variableExpression) {
        for (int i = 0; i < variableExpression.length(); i++) {
            Boolean isContainedInMap = valueStore.containsKey(Character.toString(variableExpression.charAt(i)));
            if (isContainedInMap) {
                Integer x = valueStore.get(Character.toString(variableExpression.charAt(i)));
                mathExpressionBuilder.append(x.toString());
            } else {
                mathExpressionBuilder.append(variableExpression.charAt(i));
            }
        }
        return mathExpressionBuilder.toString();
    }

    /**
     * This part of code handles the lines of code that deal with initialization i.e: which start with LET
     * @param codeWords are the words of code in a particular line of code
     */
    private void examineInitializationStatement(String[] codeWords) {
        StringBuilder expressionBuilder = new StringBuilder();
        for (int i = 1; i < codeWords.length; expressionBuilder.append(codeWords[i++]));
        String expression = expressionBuilder.toString();
        String[] equationOperands = expression.split("=");
        String key = equationOperands[0];
        String value = equationOperands[1];
        boolean hasOnlyDigits = value.matches("[0-9]+");
        if (hasOnlyDigits) {
            Integer numericalValue = Integer.parseInt(equationOperands[1]);
            valueStore.put(key, numericalValue);
        } else {
            StringBuilder expressionToBeEvaluated = new StringBuilder();
            int result = evaluate(buildMathematicalExpression(expressionToBeEvaluated, value));
            valueStore.put(key, result);
        }
    }

    /**
     * The function examines the print statements and parses them
     * @param codeWords are words of code from print lines
     */
    private void examinePrintStatements(String[] codeWords) {
        StringBuilder find = new StringBuilder();
        for (int i = 1; i < codeWords.length; i++) {
            find.append(codeWords[i]);
        }
        String distinctWord = find.toString();
        if (valueStore.containsKey(distinctWord)) {
            Integer value = valueStore.get(distinctWord);
            if (Objects.equals(codeWords[0], "PRINT"))
                System.out.print(value);
            else
                System.out.println(value);
        } else {
            if (!distinctWord.startsWith("\"")) {
                StringBuilder printableToBeEvaluated = new StringBuilder();
                int expressionResult = evaluate(buildMathematicalExpression(printableToBeEvaluated, distinctWord));
                if (Objects.equals(codeWords[0], "PRINT"))
                    System.out.print(expressionResult);
                else
                    System.out.println(expressionResult);
            } else {
                StringBuilder printableString = new StringBuilder();
                for (int i = 1; i < codeWords.length; printableString.append(codeWords[i++]));
                String printableResult = printableString.toString().replaceAll("\"", "");
                if (Objects.equals(codeWords[0], "PRINT"))
                    System.out.print(printableResult);
                else
                    System.out.println(printableResult);
            }
        }
    }

    /**
     * This code for expression evaluation has been taken from Geeks for geeks website.
     * The function is intended to evaluate an expression containing numbers and mathematical operators
     * Link to the article: <a href="https://www.geeksforgeeks.org/expression-evaluation/">...</a>
     * @param expression : It is the expression that needs to be evaluated by using a stack
     * @return : The function returns an integer i.e: the result of the evaluated expression
     */
    public int evaluate(String expression)
    {
        char[] tokens = expression.toCharArray();
        Stack<Integer> values = new Stack<>();
        Stack<Character> ops = new Stack<>();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ')
                continue;
            if (tokens[i] >= '0' && tokens[i] <= '9') {
                StringBuffer sBuffer = new StringBuffer();
                while (i < tokens.length && tokens[i] >= '0' && tokens[i] <= '9')
                    sBuffer.append(tokens[i++]);
                values.push(Integer.parseInt(sBuffer.toString()));
                i--;
            }
            else if (tokens[i] == '(')
                ops.push(tokens[i]);
            else if (tokens[i] == ')') {
                while (ops.peek() != '(')
                    values.push(applyOp(ops.pop(),
                            values.pop(),
                            values.pop()));
                ops.pop();
            } else if (tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/') {
                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek()))
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.push(tokens[i]);
            }
        }
        while (!ops.empty())
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        return values.pop();
    }
    public boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')')
            return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'))
            return false;
        else
            return true;
    }

    public int applyOp(char op, int b, int a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0)
                    throw new UnsupportedOperationException("Cannot divide by zero");
                return a / b;
        }
        return 0;
    }
}
