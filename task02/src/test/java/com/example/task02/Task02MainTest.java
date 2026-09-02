package com.example.task02;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Task02MainTest {

    private static Stream<Arguments> conversions() {
        return Stream.of(
                Arguments.of(new byte[]{65, 13, 10, 10, 13}, new byte[]{65, 10, 10, 13}),
                Arguments.of(new byte[]{}, new byte[]{}),
                Arguments.of(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}),
                Arguments.of(new byte[]{65, 66, 13, 13, 10, 10, 13, 67, 13, 13}, new byte[]{65, 66, 13, 10, 10, 13, 67, 13, 13}),
                Arguments.of(new byte[]{1, 2, 13, 10, 3, 4, 13, 10, 5, 6}, new byte[]{1, 2, 10, 3, 4, 10, 5, 6}),
                Arguments.of(new byte[]{1, 2, 3, 13, 10, 13, 10, 4, 5, 6}, new byte[]{1, 2, 3, 10, 10, 4, 5, 6}),
                Arguments.of(new byte[]{13, 10, 1, 2, 3, 4, 5, 6}, new byte[]{10, 1, 2, 3, 4, 5, 6}),
                Arguments.of(new byte[]{1, 2, 3, 13, 10, 4, 5, 6}, new byte[]{1, 2, 3, 10, 4, 5, 6}),
                Arguments.of(new byte[]{1, 2, 3, 4, 5, 6, 13, 10}, new byte[]{1, 2, 3, 4, 5, 6, 10}),
                Arguments.of(new byte[]{1, 2, 3, 13, 13, 10, 4, 5, 6}, new byte[]{1, 2, 3, 13, 10, 4, 5, 6}),
                Arguments.of(new byte[]{1, 2, 3, 10, 13, 4, 5, 6}, new byte[]{1, 2, 3, 10, 13, 4, 5, 6}),
                // одиночный '\r' в самом конце потока выводится как есть
                Arguments.of(new byte[]{13}, new byte[]{13}),
                Arguments.of(new byte[]{13, 13}, new byte[]{13, 13}),
                // байты со старшим битом: чтение до `read() != -1` по byte оборвало бы поток на 0xFF
                Arguments.of(new byte[]{(byte) 0xFF, 13, 10, (byte) 0xFE, (byte) 0x80}, new byte[]{(byte) 0xFF, 10, (byte) 0xFE, (byte) 0x80}),
                Arguments.of(new byte[]{(byte) 0xFF}, new byte[]{(byte) 0xFF})
        );
    }

    private void check(final byte[] input, final byte[] expected) throws IOException {
        final InputStream originalIn = System.in;
        final PrintStream originalOut = System.out;
        try {
            final ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
            System.setIn(new ByteArrayInputStream(input));
            System.setOut(new PrintStream(resultStream));

            Task02Main.main(new String[]{});

            assertThat(resultStream.toByteArray())
                    .as("Input: %s", Arrays.toString(input))
                    .containsExactly(expected);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    private byte[] resource(final String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(name)) {
            assertThat(in).as("ресурс %s", name).isNotNull();

            return in.readAllBytes();
        }
    }

    @ParameterizedTest
    @MethodSource("conversions")
    void main(final byte[] input, final byte[] expected) throws IOException {
        check(input, expected);
    }

    @Test
    void main_testFiles() throws IOException {
        check(resource("/input.test"), resource("/expected.test"));
    }

    @Test
    void main_largeInput() throws IOException {
        final int lines = 50_000;
        final byte[] input = new byte[lines * 3];
        final byte[] expected = new byte[lines * 2];
        for (int i = 0; i < lines; i++) {
            input[i * 3] = 65;
            input[i * 3 + 1] = 13;
            input[i * 3 + 2] = 10;
            expected[i * 2] = 65;
            expected[i * 2 + 1] = 10;
        }

        check(input, expected);
    }
}
