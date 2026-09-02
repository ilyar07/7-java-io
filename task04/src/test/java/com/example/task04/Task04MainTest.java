package com.example.task04;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Task04MainTest {

    private static Stream<Arguments> sums() {
        return Stream.of(
                Arguments.of("1 2 3", "6.000000"),
                Arguments.of("a1 b2 c3", "0.000000"),
                Arguments.of("-1e3\n18 .111 11bbb", "-981.889000"),
                Arguments.of("", "0.000000"),
                Arguments.of("   \n\n   ", "0.000000"),
                // разделителем считается любой пробельный символ, в том числе табуляция и '\r'
                Arguments.of("1\t2\r\n3", "6.000000"),
                // Double.parseDouble принимает суффиксы d/f, дробь без цифр после точки и шестнадцатеричные литералы
                Arguments.of("1f 2d 3. 0x1p3", "14.000000"),
                // число, слипшееся с текстом или с запятой, числом не считается
                Arguments.of("1,2 3;4 (5) 6", "6.000000"),
                Arguments.of("0.0000005 1", "1.000001"),
                Arguments.of("-2.5 2.5", "0.000000")
        );
    }

    private void check(final byte[] input, final String expected) throws IOException {
        final InputStream originalIn = System.in;
        final PrintStream originalOut = System.out;
        try {
            final ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
            System.setIn(new ByteArrayInputStream(input));
            System.setOut(new PrintStream(resultStream, true, StandardCharsets.UTF_8));

            Task04Main.main(new String[]{});

            assertThat(resultStream.toString(StandardCharsets.UTF_8).trim())
                    .as("Input: %s", new String(input, StandardCharsets.UTF_8))
                    .isEqualTo(expected);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    private void check(final String input, final String expected) throws IOException {
        check(input.getBytes(StandardCharsets.UTF_8), expected);
    }

    @ParameterizedTest
    @MethodSource("sums")
    void main(final String input, final String expected) throws IOException {
        check(input, expected);
    }

    @Test
    void main_testFile() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/input.test")) {
            assertThat(in).isNotNull();
            check(in.readAllBytes(), "351.731900");
        }
    }

    @Test
    void main_localeWithCommaAsDecimalSeparator() throws IOException {
        final Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);

            check("1 2 3", "6.000000");
        } finally {
            Locale.setDefault(originalLocale);
        }
    }
}
