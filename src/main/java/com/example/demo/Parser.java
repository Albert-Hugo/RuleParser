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

    public Parser(char[] code) {
        System.out.println(new String(code));
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
            System.out.println("reaching ( or )");
            printLeftCodes();
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
        String startToken = getNextToken(true);
        boolean finalResult = true;
        if (startToken.equals("(")) {
            finalResult = expression(true);
            ignoreWhiteSpace();
            eat(')');
        } else {
            traceback(startToken.length());
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


    public Object[] expressionDot() {
        String logicOp = getNextToken();
        if (logicOp.equals("and") || logicOp.equals("or")) {

            boolean booleanResult = booleanExpression();
            return new Object[]{logicOp, booleanResult};
        }

        return new Object[]{null, booleanExpression()};
    }


    public boolean booleanExpression() {
        String left = id();
        String comOp = compOp();
        String right = id();
        if (comOp.equalsIgnoreCase("==")) {
            System.out.println(left + comOp + right + ":" + left.equals(right));
            return left.equals(right);
        }
        if (comOp.equalsIgnoreCase("!=")) {
            System.out.println(left + comOp + right + ":" + (!left.equals(right)));
            return !left.equals(right);
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
                return sb.toString();
            }
            if (!specialToken && isSpecialChar(c)) {
                traceback(1);
                return sb.toString();
            }
            if (c == '!' && previewNextChar() == '=') {
                sb.append(c);
                sb.append(getNextChar());
                return sb.toString();

            }
            if (c == '=' && previewNextChar() == '=') {
                sb.append(c);
                sb.append(getNextChar());
                return sb.toString();

            }
            if (c == '=') {
                sb.append(c);
                return sb.toString();

            }
            if (!isWhiteSpace(c)) {
                sb.append(c);
                if (previewNextChar() == '=' || previewNextChar() == '!') {
                    return sb.toString();
                }
                continue;
            }
            break;
        } while (true);
        return sb.toString();

    }

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
        while (isWhiteSpace(c) && !isEOF()) {
            c = getNextChar();
        }
        index--;
    }

    private void ifStatement() {
        match("if");
        match("{");
        statement();
        match("}");
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


    boolean isVariable(String token) {
        String regex = "^[a-zA-Z]\\w*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(token);
        return matcher.matches();
    }


    public void statement() {
        if (isEOF()) {
            return;
        }
        setSavePoint();
        String startToken = getNextToken();
        if (isVariable(startToken) && getNextToken().equals("=")) {
            backToSavePoint();
            assignment();
            statement();
            return;
        }
        if (isVariable(startToken) && getNextToken(true).equals("(")) {
            backToSavePoint();
            functionCall();
            match(";");
            statement();
            return;
        }
        switch (startToken) {
            case "if":
                traceback("if".length());
                ifStatement();
                break;
            default:
                throw new IllegalStateException("Unexpected token: " + startToken);
        }


    }

    private Object functionCall() {
        Object rtv = null;
        String functionName = getNextToken();
        match("(");
        switch (functionName) {
            case "print":
                String param = getNextToken();
                if (param.startsWith("\"") && param.endsWith("\"")) {
                    System.out.println(param.substring(1, param.length() - 1));
                    break;
                }
                if (env.get(param) != null) {
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

    private void backToSavePoint() {
        this.index = this.traceIndex;
    }

    String previewNextToken(boolean includeSpecial) {
        String token = getNextToken(includeSpecial);
        traceback(token.length());
        return token;
    }

    String previewNextToken() {
        String token = getNextToken(false);
        traceback(token.length());
        return token;
    }

    private void assignment() {
        String variable = getNextToken();
        match("=");
        setSavePoint();
        Object value = getNextToken();
        if (previewNextToken(true).equals("(")) {
            backToSavePoint();
            value = functionCall();
        }
        //todo support local variable or not?
        this.env.put(variable, value);
        match(";");
    }


    private void setSavePoint() {
        this.traceIndex = this.index;
    }


}
