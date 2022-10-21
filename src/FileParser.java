import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class FileParser {

    private Map<String, Integer> valueStore = new HashMap<>();
    private List<String> codeLines = new ArrayList<>();

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

    public void splitCodeToSegments(BufferedReader br) {
        String line;
        try {
            while ((line = br.readLine()) != null) {
                codeLines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error while reading the lines.");
        }
    }

    public void examineCodeLine() {
        codeLines.forEach(new Consumer<String>() {
            @Override
            public void accept(String lineOfCode) {
                String[] codeWordsArray = lineOfCode.split(" ");
                switch (codeWordsArray[0]) {
                    case "LET" -> examineInitializationStatement(codeWordsArray);
                    case "PRINT", "PRINTLN" -> examinePrintStatements(codeWordsArray);
                }
            }
        });
    }

    public void examineInitializationStatement(String[] codeWords) {
        StringBuilder expressionBuilder = new StringBuilder();
        for (int i = 1; i < codeWords.length; expressionBuilder.append(codeWords[i++]));
        String expression = expressionBuilder.toString();
        String[] equationOperands = expression.split("=");
        String key = equationOperands[0];
        String value = equationOperands[1];
        Boolean hasOnlyDigits = value.matches("[0-9]+");
        if (hasOnlyDigits) {
            Integer numericalValue = Integer.parseInt(equationOperands[1]);
            valueStore.put(key, numericalValue);
        } else {
            StringBuilder expressionToBeEvaluated = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                Boolean isContainedInMap = valueStore.containsKey(Character.toString(value.charAt(i)));
                if (isContainedInMap) {
                    Integer x = valueStore.get(Character.toString(value.charAt(i)));
                    expressionToBeEvaluated.append(x.toString());
                } else {
                    expressionToBeEvaluated.append(value.charAt(i));
                }
            }
            int result = evaluate(expressionToBeEvaluated.toString());
            valueStore.put(key, result);
        }
    }

    public void examinePrintStatements(String[] codeWords) {
        String distinctWord = codeWords[1];
        if (valueStore.containsKey(distinctWord)) {
            Integer value = valueStore.get(distinctWord);
            if (Objects.equals(codeWords[0], "PRINT"))
                System.out.print(value);
            else
                System.out.println(value);
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

    public int evaluate(String expression)
    {
        char[] tokens = expression.toCharArray();
        Stack<Integer> values = new Stack<Integer>();
        Stack<Character> ops = new Stack<Character>();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ')
                continue;
            if (tokens[i] >= '0' && tokens[i] <= '9') {
                StringBuffer sbuf = new StringBuffer();
                while (i < tokens.length && tokens[i] >= '0' && tokens[i] <= '9')
                    sbuf.append(tokens[i++]);
                values.push(Integer.parseInt(sbuf.toString()));
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
        if ((op1 == '*' || op1 == '/') &&
                (op2 == '+' || op2 == '-'))
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
                    throw new
                            UnsupportedOperationException(
                            "Cannot divide by zero");
                return a / b;
        }
        return 0;
    }
}
