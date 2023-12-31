package com.example.demo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ParserTest {

    @Test
    void parse_no_block() {
        Parser parser = new Parser("( 1==1 and 2==3 or 2==1)".toCharArray());
        Parser parser2 = new Parser("( 1==1 and 2==2 or 2==1)".toCharArray());
        boolean result = parser.parse();
        boolean result2 = parser2.parse();
        Assertions.assertThat(result).isFalse();
        Assertions.assertThat(result2).isTrue();
    }

    @Test
    void parse_one_block() {
        Parser parser = new Parser("( (1==1 and 2==3) or 2==1)".toCharArray());
        boolean result = parser.parse();
        Assertions.assertThat(result).isFalse();
        Parser parser2 = new Parser("( (1==1 and 2==2) or 2==1)".toCharArray());
        boolean result2 = parser2.parse();
        Assertions.assertThat(result2).isTrue();
    }

    @Test
    void parse_nested_block() {
        Object[][] prams = new Object[][]{
                {"( ( 1==31 or 2==2 ) or (1==2)) ", true}
                , {"( ( ( 1==31 or 2==2 ) or (1==2)) and 1==1)", true}
                , {"( ( ( ( 1==31 or 2==2 ) or (1==2)) and 1==1) and 2==3) ", false}
                , {"(  ( ( ( ( 1==31 or 2==2 ) or (1==2)) and 1==1) and 2==3) or 2==1)", false}
        };
        for (Object[] p : prams) {
            Parser parser = new Parser(((String) p[0]).toCharArray());
            boolean result = parser.parse();
            Assertions.assertThat(result).isEqualTo(p[1]);
        }

    }

    @Test
    void parse() {
        Parser parser = new Parser("(  ( ( ( ( 1==31 or 2==2 ) or (1==2)) and 1==1) and 2==3) or 2==1)".toCharArray());
        boolean result = parser.parse();
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void getLeftString() {
        Parser parser = new Parser(" fdsf".toCharArray());
        parser.ignoreWhiteSpace();
        String result = parser.getLeftString();
        Assertions.assertThat(result).isEqualTo("fdsf");
    }

    @Test
    void getNextToken() {
        Parser parser = new Parser(" fdsf ".toCharArray());
        String result = parser.getNextToken();
        Assertions.assertThat(result).isEqualTo("fdsf");
    }

    @Test
    void getNextToken_comOp() {
        Parser parser = new Parser(" != ".toCharArray());
        String result = parser.getNextToken();
        Assertions.assertThat(result).isEqualTo("!=");
    }

    @Test
    void statement_builtin_print() {
        Parser parser = new Parser("print(\"data\");".toCharArray());
        parser.statement();
//        Assertions.assertThat(result).isEqualTo("!=");
    }

    @Test
    void statement_assignment() {
        Parser parser = new Parser(("data = 1;\n" +
                "print(data);").toCharArray());
        parser.statement();
//        Assertions.assertThat(result).isEqualTo("!=");
    }

    @Test
    void getContxtValue() {
        String code = "tag8 = getValue(8);\n" +
                "        tag2 = getValue(2);\n" +
                "        print(tag8);\n" +
                "        print(tag2);";
        Parser parser = new Parser((code).toCharArray());
        Map<String, String> contxt = new HashMap<>();
        contxt.put("8", "4.4");
        contxt.put("2", "20230211");
        parser.setContext(contxt);
        parser.statement();
//        Assertions.assertThat(result).isEqualTo("!=");
    }

    @Test
    void test_ifStatement() {
        String code = "tag8 = getValue(8);\n" +
                "if(tag8 == 4.4){\n" +
                "print(\"version is 4.4\");\n" +
                "}\t\n" +
                "\n";
        Parser parser = new Parser((code).toCharArray());

        Map<String, String> contxt = new HashMap<>();
        contxt.put("8", "4.4");
        contxt.put("2", "20230211");
        parser.setContext(contxt);
        parser.statement();
//        Assertions.assertThat(result).isEqualTo("!=");
    }

    @Test
    void test_nested_function_call() {
        String code = "tag8=4;\n" +
                "if(find(getValue(2),getValue(8))){\n" +
                "\tprint(\"tag2 contains tag8\");\t\n" +
                "}\t\n" +
                "print(tag8);\n";
        Parser parser = new Parser((code).toCharArray());

        Map<String, String> contxt = new HashMap<>();
        contxt.put("2", "20230211");
        contxt.put("8", "2");
        parser.setContext(contxt);
        parser.statement();
//        Assertions.assertThat(result).isEqualTo("!=");
    }

    @Test
    void test_elseIfStatement() {
        String code = "tag8 = getValue(8);\n" +
                "if(tag8 == 4.4){\n" +
                "print(\"version is 4.4\");\n" +
                " }elseIf(tag8 == 4.3){" +
                "print(\"version is 4.3\");\n" +
                "}elseIf(tag8 == 4.2){" +
                " print(\"version is 4.2\");\n" +
                "" +
                "}else{" +
                " print(\"unknown version!\");" +
                "}\t\n" +
                "\n";
        Parser parser = new Parser((code).toCharArray());

        Map<String, String> contxt = new HashMap<>();
        contxt.put("8", "4");
        parser.setContext(contxt);
        parser.statement();
//        Assertions.assertThat(result).isEqualTo("!=");
    }

    @Test
    void test_elseStatement() {
        String code = "tag8 = getValue(8);\n" +
                "if(tag8 == 4.4){\n" +
                "print(\"version is 4.4\");\n" +
                " }else{" +
                "print(\"version is not 4.4\");\n" +
                "}\t\n" +
                "\n";
        Parser parser = new Parser((code).toCharArray());

        Map<String, String> contxt = new HashMap<>();
        contxt.put("8", "4.4");
        parser.setContext(contxt);
        parser.statement();
//        Assertions.assertThat(result).isEqualTo("!=");
    }

    @Test
    void test_function_in_expression() {
        String code = "(getValue(8) == 4.4)";
        Parser parser = new Parser((code).toCharArray());

        Map<String, String> contxt = new HashMap<>();
        contxt.put("8", "4.4");
        contxt.put("2", "20230211");
        parser.setContext(contxt);
        Assertions.assertThat(parser.parse()).isTrue();
    }

    @Test
    void test_nested_ifStatement() {
        String code = "tag8 = getValue(8);\n" +
                "tag2 = getValue(2);\n" +
                "if(tag8 == 4.4){\n" +
                "\tprint(\"version is not 4.4\");\n" +
                "\tif(tag2 == 3){\n" +
                "\t\tprint(\"heelo\");\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "\t\n" +
                "}\t\n" +
                "print(tag8);\n" +
                "print(tag2);";
        Parser parser = new Parser((code).toCharArray());

        Map<String, String> contxt = new HashMap<>();
        contxt.put("8", "4.4");
        contxt.put("2", "3");
        parser.setContext(contxt);
        parser.statement();
//        Assertions.assertThat(result).isEqualTo("!=");
    }

    @Test
    public void test_previewToken() {
        Parser parser = new Parser((" tag8 ").toCharArray());
        String token = parser.previewNextToken();
        Assertions.assertThat(token).isEqualTo("tag8");
        Assertions.assertThat(parser.getIndex()).isEqualTo(0);
    }

//    @Test
//    public void getNextToken_without_special() {
//        Parser parser = new Parser((" tag8 ").toCharArray());
//        String token = parser.getNextToken();
//        Assertions.assertThat(token).isEqualTo("tag8");
//        Assertions.assertThat(parser.getIndex()).isEqualTo(5);
//    }


    @Test
    public void testIsStringValue() {

        Assertions.assertThat(Parser.isStringValue("\"value\"")).isTrue();
    }

    @Test
    public void testGetValueFromString() {

        Assertions.assertThat(Parser.getValueFromString("\"value\"")).isEqualTo("value");
    }

    @Test
    public void getNextToken_with_special() {
        Parser parser = new Parser((" tag8( ").toCharArray());
        String token = parser.getNextToken();
        Assertions.assertThat(token).isEqualTo("tag8");
        Assertions.assertThat(parser.getIndex()).isEqualTo(5);
        token = parser.getNextToken();
        Assertions.assertThat(token).isEqualTo("(");
        Assertions.assertThat(parser.getIndex()).isEqualTo(6);
    }

    @Test
    public void mathExpression() {
        Parser parser = new Parser((" 1 + 3+ 5 * 2 /2 -6" ).toCharArray());
        Assertions.assertThat(parser.mathExpression(0)).isEqualTo(6);
    }
}