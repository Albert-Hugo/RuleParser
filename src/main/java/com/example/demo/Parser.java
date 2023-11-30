package com.example.demo;

public class Parser {
    private final char[] code;
    private int index;

    public Parser(char[] code) {
        System.out.println(new String(code));
        this.code = code;
    }

    public static void main(String[] args) {
        Parser parser = new Parser("((((1==31) or (2==2)) or (1==2)) and 1==1)".toCharArray());
//        Parser parser = new Parser("(2==2) and 1==1".toCharArray());
//        Parser parser2 = new Parser("(1==1) or (2==2)".toCharArray());
        boolean result = parser.expression();
//        System.out.println(result);
//       boolean result = parser2.expression();
        System.out.println(result);
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
        String startT = getNextToken(true);
        boolean braceResult = true;
        if(startT.equals(")") && isEOF()){
            return true;
        }
        if (startT.equals("(")) {
            if ('(' == previewNextChar()) {
                braceResult = expression();
            } else {
                braceResult = booleanExpression();
            }
            String t = getNextToken(true);
            if (t.equals("and")) {
                boolean r = expression() && braceResult;
                System.out.println("composed result:"+r);
                return r;
            }
            if (t.equals("or")) {
                boolean r = expression() || braceResult;
                System.out.println("composed result:"+r);
            }
            if (t.equals(")")) {
                boolean r = expression() && braceResult;
                System.out.println("composed result:"+r);
                return r;
            }
        }

        if ("and".equals(startT)) {
            ignoreWhiteSpace();
            return expression() && braceResult;
        }
        if ("or".equals(startT)) {
            ignoreWhiteSpace();
            return expression() || braceResult;
        }
        if (")".equals(startT)) {
            return expression() && braceResult;
        }
        traceback(startT.length());

        if(isEOF()){
            return braceResult;
        }
        boolean firstResult = booleanExpression();
        ignoreWhiteSpace();

        return braceResult && firstResult;
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
