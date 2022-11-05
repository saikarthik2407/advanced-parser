import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileParser {

    private final List<String> codeLines = new ArrayList<>();
    private final HashMap<String, Variable> variables = new HashMap<>();
    private final InputFile inputFile;
    private final Scanner readInput;

    FileParser() {
        inputFile = InputFile.getInstance();
        readInput = new Scanner(System.in);
    }

    /**
     * Main parse function
     * @param file the file that needs to be parsed
     */
    public void parse(File file) {
        try  {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            splitCodeToSegments(bufferedReader);
            // execute code from first line to last line
            executeInstructions(inputFile.getFirstLineNumber(), inputFile.getLastLineNumber());
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
            inputFile.setCodeLines(codeLines);
        } catch (IOException e) {
            System.out.println("Error while reading the lines.");
        }
    }

    private void executeInstructions(Integer startLineNumber, Integer endLineNumber) {
        inputFile.getCodeLines().keySet().forEach(lineNumber -> {
            if (lineNumber >= startLineNumber && lineNumber <= endLineNumber)
                examineCodeLine(lineNumber);
        });
    }

    /**
     * The function examines each line of code to parse
     */
    private void examineCodeLine(Integer lineNumber) {
        String code = inputFile.getCodeLines().get(lineNumber);
        String decisionWord = StringUtils.getFirstWord(code);
        switch (decisionWord) {
            case "INTEGER" -> examineDeclarationInstruction(lineNumber, code);
            case "INPUT" -> examineInputInstruction(lineNumber, code);
            case "LET" -> examineInitializationInstruction(lineNumber, code);
            case "PRINT", "PRINTLN" -> examinePrintInstruction(lineNumber, code);
            case "IF" -> examineConditionalInstruction(lineNumber, code);
            case "GOTO" -> examineGotoInstruction(lineNumber, code);
            case "END" -> System.exit(0);
            default -> System.out.println("Syntax error occurred while parsing");
        }
    }

    /**
     * This function examines declaration instructions and creates variables for the encountered variable declarations
     * @param lineNumber line number of declaration instruction
     * @param codeLine instruction code line
     */
    private void examineDeclarationInstruction(Integer lineNumber, String codeLine) {
        String instruction = StringUtils.removeFirstWordFromString(codeLine);
        String[] instructionVariables = instruction.split(",");
        for (String variable: instructionVariables) {
            if (StringUtils.isValidVariableName(variable))
                variables.put(variable, new Variable(variable));
            else {
                System.out.println("Not a valid variable name at "+lineNumber);
                System.exit(0);
            }
        }
    }

    /**
     * This function helps to retrieve console input from the user.
     * @param lineNumber line number of input instruction
     * @param codeLine instruction code line
     */
    private void examineInputInstruction(Integer lineNumber, String codeLine) {
        String instruction = StringUtils.removeFirstWordFromString(codeLine);
        String[] instructionVariables = instruction.split(",");
        for (String variable: instructionVariables) {
            if (!variables.containsKey(variable)) {
                System.out.println(variable+" variable not declared at "+lineNumber);
                System.exit(0);
            }
        }
        String consoleInput = readInput.nextLine();
        if (!Objects.equals(consoleInput, "")) {
            String[] inputs = consoleInput.split(" ");
            if (inputs.length == instructionVariables.length) {
                for (int i = 0; i < instructionVariables.length; i++) {
                    try {
                        Variable requiredVariable = variables.get(instructionVariables[i]);
                        requiredVariable.value = Integer.parseInt(inputs[i]);
                        requiredVariable.state = VariableState.INITIALIZED;
                    } catch (NumberFormatException e) {
                        System.out.println(e.getMessage());
                        System.exit(0);
                    }
                }
            } else {
                System.out.println("value of "+instructionVariables[inputs.length]+" is not entered at "+lineNumber);
                System.exit(0);
            }
        } else {
            System.out.println("value of "+instructionVariables[0]+" is not entered at "+lineNumber);
            System.exit(0);
        }
    }

    /**
     * This part of code handles the lines of code that deal with initialization i.e: which start with LET
     * @param lineNumber is the line number of the initialization instruction
     * @param code is a particular line of code
     */
    private void examineInitializationInstruction(Integer lineNumber, String code) {
        String instruction = StringUtils.removeFirstWordFromString(code);
        String expression = instruction.replace(" ", "");
        String[] operands = expression.split("=");
        String variableBeingAssigned = operands[0];
        if (!variables.containsKey(variableBeingAssigned)) {
            System.out.println(variableBeingAssigned+" variable not declared at "+lineNumber);
            System.exit(0);
        } else if (operands.length > 2) {
            System.out.println("Invalid initialization at "+lineNumber);
            System.exit(0);
        }
        String expressionString = operands[1];
        boolean hasOnlyDigits = expressionString.matches("[0-9]+");
        if (hasOnlyDigits) {
            variables.get(variableBeingAssigned).value = Integer.parseInt(expressionString);
        } else {
            StringBuilder mathExpressionBuilder = new StringBuilder();
            variables.get(variableBeingAssigned).value = evaluate(buildMathematicalExpressionForEvaluation(
                    mathExpressionBuilder, expressionString));
        }
        variables.get(variableBeingAssigned).state = VariableState.INITIALIZED;
    }

    /**
     * The function examines the print statements and parses them
     * @param lineNumber is the instruction line to be printed
     * @param code is the line to print
     */
    private void examinePrintInstruction(Integer lineNumber, String code) {
        String decisionWord = StringUtils.getFirstWord(code);
        String instruction = StringUtils.removeFirstWordFromString(code);
        if (variables.containsKey(instruction)) {
            Integer value = variables.get(instruction).value;
            if (Objects.equals(decisionWord, "PRINT"))
                System.out.print(value);
            else
                System.out.println(value);
        } else {
            if (!instruction.startsWith("\"")) {
                StringBuilder printableToBeEvaluated = new StringBuilder();
                int expressionResult = evaluate(buildMathematicalExpressionForEvaluation(printableToBeEvaluated, instruction));
                if (Objects.equals(decisionWord, "PRINT"))
                    System.out.print(expressionResult);
                else
                    System.out.println(expressionResult);
            } else {
                String printableResult = instruction.replaceAll("\"", "");
                if (Objects.equals(decisionWord, "PRINT"))
                    System.out.print(printableResult);
                else
                    System.out.println(printableResult);
            }
        }
    }

    private void examineConditionalInstruction(Integer lineNumber, String code) {
        String codeWithoutIf = StringUtils.removeFirstWordFromString(code);
        String[] conditionalClauses = codeWithoutIf.split("THEN");
        String ifClause = conditionalClauses[0];
        String thenClause = conditionalClauses[1];
        ifClause = ifClause.replace(" ", "");
        String relationalOperator = ifClause.contains("<") ? "<" : (ifClause.contains(">") ? ">"
                : (ifClause.contains("=") ? "=" : (ifClause.contains("!") ? "!" : "" )));
        if (relationalOperator.equals("")) {
            System.out.println("If clause doesn't have a valid relational operator at "+lineNumber);
            System.exit(0);
        }
        String[] relationalOperands = ifClause.split(relationalOperator);
        for (int i = 0; i < relationalOperands.length; i++) {
            relationalOperands[i] = mapVariableNamesToValuesInExpression(relationalOperands[i]);
        }
        if (computeExpressionResult(relationalOperator, evaluate(relationalOperands[0]), evaluate(relationalOperands[1]))) {
            thenClause = thenClause.trim();
            String decisionWord = StringUtils.getFirstWord(thenClause);
            switch (decisionWord) {
                case "PRINT", "PRINTLN" -> examinePrintInstruction(lineNumber, thenClause);
                case "GOTO" -> examineGotoInstruction(lineNumber, thenClause);
            }
        }
    }

    private void examineGotoInstruction(Integer lineNumberOfGotoInstruction, String code) {
        String[] codeWords = code.trim().split(" ");
        if (codeWords.length != 2) {
            System.out.println("Improper GOTO statement at "+lineNumberOfGotoInstruction);
            System.exit(0);
        }
        Integer gotoLineNumber = Integer.parseInt(codeWords[codeWords.length - 1]);
        executeInstructions(gotoLineNumber, inputFile.getLastLineNumber());
    }

    /**
     * This function builds a numerical expression from a variable expression by retrieving the values of
     * variables from the hashMap which we stored during initialization
     * @param mathExpressionBuilder is the numerical expression builder
     * @param expression is the expression that has variables instead of numbers
     * @return the numerical expression
     */
    private String buildMathematicalExpressionForEvaluation(StringBuilder mathExpressionBuilder, String expression) {
        StringBuilder variableNameBuilder = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '+' || expression.charAt(i) == '-'
                    || expression.charAt(i) == '*' || expression.charAt(i) == '/'
                    || (expression.charAt(i) >= '0' && expression.charAt(i) <= '9')) {
                mathExpressionBuilder.append(expression.charAt(i));
                variableNameBuilder.delete(0, variableNameBuilder.length());
            } else {
                String variableName = variableNameBuilder.append(expression.charAt(i)).toString();
                if (variables.containsKey(variableName)) {
                    mathExpressionBuilder.append(variables.get(variableName).value);
                }
            }
        }
        return mathExpressionBuilder.toString();
    }

    private String mapVariableNamesToValuesInExpression(String expression) {
        String mappedExpression = expression;
        for (String variableName: variables.keySet()) {
            if (mappedExpression.contains(variableName)) {
                mappedExpression = mappedExpression.replace(variableName, variables.get(variableName).value.toString());
            }
        }
        return mappedExpression;
    }

    private Boolean computeExpressionResult(String relationalOperator, Integer leftOperand, Integer rightOperand) {
        return switch (relationalOperator) {
            case "<" -> leftOperand < rightOperand;
            case ">" -> leftOperand > rightOperand;
            case "=" -> Objects.equals(leftOperand, rightOperand);
            default -> !Objects.equals(leftOperand, rightOperand);
        };
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

class Variable {
    String name;
    Integer value;
    VariableState state;

    Variable(String name) {
        value = Integer.MIN_VALUE;
        state = VariableState.DECLARED;
    }
}

enum VariableState {
    DECLARED,
    INITIALIZED
}
