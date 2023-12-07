package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final char[] code;
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
        String logicOp = getNextToken(true);
        if (logicOp.equals(")") || logicOp.equals("(")) {
            //reaching the end or the start of another expression
//            System.out.println("reaching ( or )");
//            printLeftCodes();
            traceback(1);
            return traceResult;
        }


        if (logicOp.equals("or")) {
            traceResult = expression(true) || traceResult;
            return traceResult;
        }
        if (logicOp.equals("and")) {
            traceResult = expression(true) && traceResult;
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
    public boolean expression(boolean traceResult) {
        ignoreWhiteSpace();
        String startToken = previewNextToken(true);
        boolean finalResult = true;
        if (startToken.equals("(")) {
            match("(");
            finalResult = expression(true);
            match(")");
        } else {
//            traceback(startToken.length());
            boolean startResult = booleanExpression();
            finalResult = logicExpression(startResult);
        }

        String token = getNextToken();

        if (token.equals("or")) {
            return expression(true) || finalResult;
        }
        if (token.equals("and")) {
            return expression(true) && finalResult;
        }

        return finalResult;
    }

    public boolean parse() {
        ignoreWhiteSpace();
        eat('(');
        boolean result = expression(true);
        ignoreWhiteSpace();
        eat(')');
        return result;
    }



    private Object getActualValueIfVariable(String token) {
        if (isVariable(token) && env.get(token)!=null) {
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
        String left = id();
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
        String token = getNextToken();
        return token;
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
        return getNextToken(false);
    }

    String getNextToken(boolean specialToken) {
        ignoreWhiteSpace();
        char c;
        StringBuilder sb = new StringBuilder();
        do {
            c = getNextChar();
//            if (isEOF()) {
//                sb.append(c);
//                return sb.toString();
//            }
            if (specialToken && isSpecialChar(c)) {
                sb.append(c);
                break;
//                return sb.toString();
            }
            //if not include special char, traceback index
            if (!specialToken && isSpecialChar(c)) {
                traceback(1);
                break;
//                return sb.toString();
            }
            //handle 2 char special chars
            if (c == '!' && previewNextChar() == '=') {
                sb.append(c);
                sb.append(getNextChar());
                break;
//                return sb.toString();

            }
            if (c == '=' && previewNextChar() == '=') {
                sb.append(c);
                sb.append(getNextChar());
                break;
//                return sb.toString();

            }
            if (c == '=') {
                sb.append(c);
                break;
//                return sb.toString();

            }
            if (!isWhiteSpace(c)) {
                sb.append(c);
                if (previewNextChar() == '=' || previewNextChar() == '!') {
                    break;
//                    return sb.toString();
                }
                continue;
            } else {
                traceback(1);
            }
            break;
        } while (true);
        //handle String value
        String result = sb.toString();
//        if(result.startsWith("\"") && result.endsWith("\"")){
//            return result.substring(1,result.length()-1);
//        }
        return result;

    }

    static boolean isStringValue(String token){
        return token.startsWith("\"") && token.endsWith("\"");
    }

    static String getValueFromString(String token){
        return token.substring(1,token.length()-1);
    }

    /**
     * only for 1 char character
     *
     * @param c
     * @return
     */
    private boolean isSpecialChar(char c) {
        if (c == '(' || c == ')' || c == ';') {
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

    private void ifStatement() {
        match("if", false);
        match("(");
        if (expression(true)) {
            match(")");
            match("{");
            statement();
            match("}");
        }
    }


    String getLeftString() {
        return new String(code).substring(index);
    }

    private void match(String expect) {
        String actual = getNextToken(true);
        if (!expect.equals(actual)) {
            throw new IllegalStateException("expect " + expect + " but get: " + actual);
        }
    }

    private void match(String expect, boolean special) {
        String actual = getNextToken(special);
        if (!expect.equals(actual)) {
            throw new IllegalStateException("expect " + expect + " but get: " + actual);
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
        String t = previewNextToken(true);
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

        if (!startToken.contains("if(") && startToken.contains("(")) {
            functionCall();
            match(";");
            statement();
            return;
        }


    }

    private Object functionCall() {
        Object rtv = null;
        String functionName = getNextToken();
        match("(");
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
            result.append(getNextToken(true));
        }
        backToSavePoint();
        return result.toString();
    }


    String previewNextToken(boolean includeSpecial) {
        setSavePoint();
        String token = getNextToken(includeSpecial);
        backToSavePoint();
        return token;
    }

    String previewNextToken() {
        return previewNextToken(false);
    }

    private void assignment() {
        String variable = getNextToken();
        match("=");
        String token = previewNextToken(true);
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
