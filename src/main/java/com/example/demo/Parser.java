package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final char[] code;
    private int line;
    private int index;
    private int traceIndex;
    private Map<String, Object> env = new HashMap<>();
    private Map<String, String> context = new HashMap<>();

    public int getIndex() {
        return index;
    }

    public Parser(char[] code) {
//        System.out.println(new String(code));
        this.code = code;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    //

    private void printLeftCodes() {
        System.out.println(new String(this.code).substring(index));
    }

    boolean logicExpression(boolean traceResult) {
        String logicOp = getNextToken();
        if (logicOp.equals(")") || logicOp.equals("(")) {
            //reaching the end or the start of another expression
            traceback(1);
            return traceResult;
        }


        if (logicOp.equals("or")) {
            traceResult = expression() || traceResult;
            return traceResult;
        }
        if (logicOp.equals("and")) {
            traceResult = expression() && traceResult;
            return traceResult;
        }
        throw new IllegalStateException("unexpected token:" + logicOp);
    }

    /**
     * E -> BE | (BE)
     * E-> F | F and F | F or F
     * F -> id == id | id != id
     * id-> string
     * E->TE' | (T)E'
     * E'->and T | or T
     * T-> FT'
     * T'-> ==F | >F
     * F->id
     *
     * @return
     */
    public boolean expression() {
        ignoreWhiteSpace();
        String startToken = previewNextToken();
        boolean finalResult = true;
        if (startToken.equals("(")) {
            match("(");
            finalResult = expression();
            match(")");
        } else {
            boolean startResult = booleanExpression();
            finalResult = logicExpression(startResult);
        }

        String token = previewNextToken();

        if (token.equals("or")) {
            match("or");
            return expression() || finalResult;
        }
        if (token.equals("and")) {
            match("and");
            return expression() && finalResult;
        }


        return finalResult;
    }

    public boolean parse() {
        ignoreWhiteSpace();
        eat('(');
        boolean result = expression();
        ignoreWhiteSpace();
        eat(')');
        return result;
    }


    private Object getActualValueIfVariable(String token) {
        if (isVariable(token) && env.get(token) != null) {
            return env.get(token);
        }
        return token;
    }

    private String getActualValueIfString(String token) {
        if (isStringValue(token)) {
            return getValueFromString(token);
        }
        return token;
    }


    public boolean booleanExpression() {
        String startToken = previewToken(2);
        String left = "";
        //fix for not boolean expression call
        if (isFunctionalCall(startToken)) {
            Object rtv = functionCall();
            if (rtv instanceof Boolean) {
                return (boolean) rtv;
            }

            left = (String) rtv;
        } else {
            left = id();
        }

        String comOp = compOp();
        String right = id();
        left = getActualValueIfString(left);
        right = getActualValueIfString(right);
        Object leftValue = getActualValueIfVariable(left);
        Object rightValue = getActualValueIfVariable(right);
        if (comOp.equalsIgnoreCase("==")) {
//            System.out.println(left + comOp + right + ":" + left.equals(right));
            return rightValue.equals(leftValue);
        }
        if (comOp.equalsIgnoreCase("!=")) {
//            System.out.println(left + comOp + right + ":" + (!left.equals(right)));
            return !rightValue.equals(leftValue);
        }

        throw new IllegalStateException("Unexpected token :" + comOp);

    }

    public String id() {
        String token = previewToken(2);
        if (isFunctionalCall(token)) {
            return (String) functionCall();
        }
        return getNextToken();
    }

    public String compOp() {
        String op = getNextToken();
        return op;
    }

    public String logicOp() {
        String op = getNextToken();
        return op;
    }


    private void eat(char c) {
        char actual = code[index++];
        if (c != actual) {
            throw new IllegalStateException("expect " + c + " but get " + actual);

        }
    }

    char getNextChar() {
        if (isEOF()) {
            return ' ';
        }
//        System.out.println("current index: "+ index +" and code length:" +this.code.length);
        return this.code[index++];
    }

    boolean isEOF() {
        return index == this.code.length;
    }

    String getNextToken() {
        ignoreWhiteSpace();
        char c;
        StringBuilder sb = new StringBuilder();
        do {
            c = getNextChar();
            //handle 2 char special chars
            if (isSpecialChar(c) && (previewNextChar() == '=')) {
                sb.append(c);
                sb.append(getNextChar());
                break;

            }

            //if not include special char, traceback index
            if (isSpecialChar(c) && !sb.toString().isEmpty()) {
                traceback(1);
                break;
            }

            if (isSpecialChar(c)) {
                sb.append(c);
                break;
            }


            if (!isWhiteSpace(c)) {
                sb.append(c);
                if (previewNextChar() == '=' || previewNextChar() == '!') {
                    break;
                }
                continue;
            } else {
                traceback(1);
            }
            break;
        } while (true);
        return sb.toString();

    }

    static boolean isStringValue(String token) {
        return token.startsWith("\"") && token.endsWith("\"");
    }

    static String getValueFromString(String token) {
        return token.substring(1, token.length() - 1);
    }

    /**
     * only for 1 char character
     *
     * @param c
     * @return
     */
    private boolean isSpecialChar(char c) {
        if (c == ',' || c == '/' || c == '*' || c == '-' || c == '+' || c == '}' || c == '{' || c == '!' || c == '=' || c == '(' || c == ')' || c == ';') {
            return true;
        }
        return false;
    }

    void traceback(int step) {
        index = index - step;
    }

    private char previewNextChar() {
        if (isEOF()) {
            return ' ';
        }
        char c = code[index];
        return c;
    }

    private boolean isWhiteSpace(char c) {
        if (c == '\n') {
            line++;
        }
        return c == ' ' || c == '\n' || c == '\t';
    }

    void ignoreWhiteSpace() {
        char c = getNextChar();
        while (isWhiteSpace(c)) {
            c = getNextChar();
            if (isEOF()) {
                return;
            }
        }
        index--;
    }

    private void elseIfStatement() {
        match("elseIf");
        match("(");
        boolean expressionResult = expression();
        match(")");
        if (expressionResult) {
            match("{");
            statement();
            match("}");
        } else {
            skipUntil("}");
        }
        String nextToken = previewToken(2);
        if (nextToken.equals("elseIf(")) {
            elseIfStatement();
        }


    }

    private void elseStatement() {
        String token = previewToken(2);
        if (token.equals("else{")) {
            match("else");
            match("{");
            statement();
            match("}");
        }
    }

    private void ifStatement() {
        match("if");
        match("(");
        boolean expressionResult = expression();
        match(")");
        match("{");
        if (expressionResult) {
            statement();
            match("}");
        } else {
            skipUntil("}");
        }
        String token = previewToken(2);
        if (token.equals("elseIf(")) {
            elseIfStatement();
        }
        elseStatement();
        statement();
    }

    private void skipUntil(String expect) {
        String c;
        do {
            c = getNextToken();
        } while (!c.equals(expect));
    }


    String getLeftString() {
        return new String(code).substring(index);
    }

    private void match(String expect) {
        String actual = getNextToken();
        if (!expect.equals(actual)) {
            throw new IllegalStateException("expect " + expect + " but get: " + actual + " at line:" + this.line);
        }
    }


    boolean isVariable(String token) {
        String regex = "^[a-zA-Z]\\w*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(token);
        return matcher.matches();
    }


    public void statement() {
        ignoreWhiteSpace();
        if (isEOF()) {
            return;
        }
        String t = previewNextToken();
        if (t.equals("}")) {
            return;
        }
        String startToken = previewToken(2);
        if (startToken.contains("=")) {
            assignment();
            statement();
            return;
        }

        if (startToken.startsWith("if(")) {
            ifStatement();
            statement();
        }

        if (isFunctionalCall(startToken)) {
            functionCall();
            match(";");
            statement();
            return;
        }


    }

    boolean isFunctionalCall(String token) {
        return !token.contains("if(") && token.contains("(");
    }

    Object functionCall() {
        Object rtv = null;
        String functionName = getNextToken();
        match("(");
        System.out.println("calling function: " + functionName);
        switch (functionName) {
            case "print":
                String param = previewNextToken();
                if (param.startsWith("\"")) {
                    String wholeString = getWholeString();
                    System.out.println(wholeString);
                    break;
                }
                if (env.get(param) != null) {
                    param = getNextToken();
                    System.out.println(env.get(param));
                    break;
                }

                break;
            case "getValue":
                String tag = getNextToken();
                Integer.parseInt(tag);
                rtv = context.get(tag);
                break;
            case "find":
                String source = id();
                source = getActualValueIfVariable(source).toString();
                match(",");
                String target = id();
                target = getActualValueIfString(target);
                rtv = source.contains(target);
                break;
            default:
                throw new IllegalStateException("Unexpected token");
        }
        match(")");
        return rtv;
    }

    private String getWholeString() {
        StringBuilder result = new StringBuilder();
        char t;
        ignoreWhiteSpace();
        eat('\"');
        do {
            t = getNextChar();
            if (t != '\"') {
                result.append(t);
                continue;
            }
            return result.toString();
        } while (true);

    }

    private void backToSavePoint() {
        this.index = this.traceIndex;
    }

    String previewToken(int number) {
        setSavePoint();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < number; i++) {
            result.append(getNextToken());
        }
        backToSavePoint();
        return result.toString();
    }


    String previewNextToken() {
        setSavePoint();
        String token = getNextToken();
        backToSavePoint();
        return token;
    }

    private void assignment() {
        String variable = getNextToken();
        match("=");
        String token = previewToken(2);
        Object value = null;
        if (token.contains("(")) {
            value = functionCall();
        } else {
            value = getNextToken();
        }
        //todo support local variable or not?
        this.env.put(variable, value);
        match(";");
    }


    private void setSavePoint() {
        this.traceIndex = this.index;
    }


}
