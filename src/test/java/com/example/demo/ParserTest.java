package com.example.demo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ParserTest {

    @Test
    void getLeftString() {
        Parser parser = new Parser(" fdsf".toCharArray());
        parser.ignoreWhiteSpace();
        String result = parser.getLeftString();
        Assertions.assertThat(result).isEqualTo("fdsf");
    }

    @Test
    void getNextStr() {
        Parser parser = new Parser(" fdsf ".toCharArray());
        String result = parser.getNextToken();
        Assertions.assertThat(result).isEqualTo("fdsf");
    }
}