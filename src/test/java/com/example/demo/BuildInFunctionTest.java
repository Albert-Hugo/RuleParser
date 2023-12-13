package com.example.demo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class BuildInFunctionTest {
    @Test
    void test_find() {
        String code = "find(\"4.4\",\"4\");";
        Parser parser = new Parser((code).toCharArray());
        boolean result = (boolean) parser.functionCall();
        Assertions.assertThat(result).isTrue();
        code = "find(\"4.4\",\"3\");";
        parser = new Parser((code).toCharArray());
        result = (boolean) parser.functionCall();
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void test_find_in_code() {
        String code = "tag8 = getValue(8);\n" +
                "tag2 = getValue(2);\n" +
                "if(find(tag8,4)){\n" +
                "\tprint(\"4 is in tag8\");\n" +
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
    void test_function_with_other_expression() {
        String code = "tag8 = getValue(8);\n" +
                "tag2 = getValue(2);\n" +
                "if(find(tag8,4) and 1==3 ){\n" +
                "\tprint(\"4 is in tag8\");\n" +
                "\n" +
                "\t\n" +
                "}else{" +
                " print(\"4 not in tag8\");" +
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
}
