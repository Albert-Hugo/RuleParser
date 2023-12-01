package com.example.demo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Parser parser = new Parser("(  ( ( ( ( 1==31 or 2==2 ) or (1==2)) and 1==1) and 2==3) or 2==1)".toCharArray());
        boolean result = parser.parse();
        Assertions.assertThat(result).isFalse();
        Parser parser2 = new Parser("(  ( ( ( ( 1==31 or 2==2 ) or (1==2)) and 1==1) and 2==2) or 2==1)".toCharArray());
        boolean result2 = parser2.parse();
        Assertions.assertThat(result2).isTrue();
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

}