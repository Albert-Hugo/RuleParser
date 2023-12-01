package com.example.demo;

public class Parser {
    private final char[] code;
    private int index;

    public Parser(char[] code) {
        System.out.println(new String(code));
        this.code = code;
    }



//

    private void printLeftCodes(){
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
