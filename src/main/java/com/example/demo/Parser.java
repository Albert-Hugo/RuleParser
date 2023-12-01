package com.example.demo;

public class Parser {
    private final char[] code;
    private int index;

    public Parser(char[] code) {
        System.out.println(new String(code));
        this.code = code;
    }

    public static void main(String[] args) {
//        Parser parser = new Parser("((1==2 or 1==1) and 1==3)".toCharArray());
//        Parser parser = new Parser("((1==31 or 2==2) and 2==3)".toCharArray());
        Parser parser = new Parser("(  ( ( ( ( 1==31 or 2==2 ) or (1==2)) and 1==1) and 2==3) or 1==1)".toCharArray());
//        Parser parser = new Parser("(2==2) and 1==2 and 1==1".toCharArray());
//        Parser parser2 = new Parser("(1==1) or (2==2)".toCharArray());
        boolean result = parser.parse();
//        System.out.println(result);
//       boolean result = parser2.expression();
        System.out.println(result);
    }

//    boolean blockExpression() {
//        ignoreWhiteSpace();
//        eat('(');
//        logicComposedExpression();
//        eat(')');
//        return blockResult;
//
//    }

    boolean logicExpression(boolean traceResult) {
        String logicOp = getNextToken(true);
        if (logicOp.equals(")") || logicOp.equals("(")) {
            //reaching the end or the start of another expression
            traceback(1);
            return traceResult;
        }

        if (logicOp.equals("and")) {
            traceResult = expression(traceResult) && traceResult;
            return traceResult;
        }
        if (logicOp.equals("or")) {
            traceResult = expression(traceResult) || traceResult;
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
            finalResult = expression(traceResult);
            ignoreWhiteSpace();
            eat(')');
        } else {
            traceback(startToken.length());
            boolean startResult = booleanExpression();
            finalResult = logicExpression(startResult);
        }

        String token = getNextToken();
        if (token.equals("and")) {
            return expression(finalResult) && traceResult;
        }
        if (token.equals("or")) {
            return expression(finalResult) || traceResult;
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

    boolean logicComposedExpression() {
        boolean start = booleanExpression();
        return logicExpression(start);
    }


    public Object[] expressionDot() {
        String logicOp = getNextToken();
        if (logicOp.equals("and") || logicOp.equals("or")) {

            boolean booleanResult = booleanExpression();
            return new Object[]{logicOp, booleanResult};
        }

        return new Object[]{null, booleanExpression()};
    }

    public boolean braceExpression() {
        eat('(');
        boolean rtv = booleanExpression();
        expressionDot();
        eat(')');
        return rtv;

    }

    public boolean booleanExpression() {
        String left = id();
        String comOp = compOp();
        String right = id();
        if (comOp.equalsIgnoreCase("==")) {
            System.out.println(left + comOp + right + ":" + left.equals(right));
            return left.equals(right);
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
                if (previewNextChar() == '=') {
                    return sb.toString();
                }
                continue;
            }
            break;
        } while (true);
        return sb.toString();

    }

    private boolean isSpecialChar(char c) {
        if (c == '(' || c == ')') {
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


    String getLeftString() {
        return new String(code).substring(index);
    }


    public void statement() {

    }


}
