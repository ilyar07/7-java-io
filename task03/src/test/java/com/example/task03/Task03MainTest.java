package com.example.task03;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Task03MainTest {

    private static Stream<Arguments> encodings() {
        return Stream.of(
                Arguments.of(new byte[]{48, 49, 50, 51}, "ASCII", "0123"),
                Arguments.of(new byte[]{}, "UTF8", ""),
                Arguments.of(new byte[]{-48, -97, -47, -128, -48, -72, -48, -78, -48, -75, -47, -126}, "UTF8", "Привет"),
                Arguments.of(new byte[]{-3, -14, -18}, "windows-1251", "это"),
                Arguments.of(new byte[]{-44, -59, -45, -44, -39}, "KOI8-R", "тесты"),
                Arguments.of(new byte[]{0, 0, 4, 62, 0, 0, 4, 61, 0, 0, 4, 56}, "UTF-32", "они"),
                Arguments.of(new byte[]{-30, -91, -31, -30, -88, -32, -29, -18, -30}, "IBM866", "тестируют")
        );
    }

    private String readAsString(final InputStream in, final Charset charset) throws IOException {
        try (InputStream stream = in) {
            return Task03Main.readAsString(stream, charset);
        }
    }

    @ParameterizedTest
    @MethodSource("encodings")
    void readAsString(final byte[] input, final String charset, final String expected) throws IOException {
        assertThat(readAsString(new ByteArrayInputStream(input), Charset.forName(charset)))
                .as("Input: %s, charset: %s", Arrays.toString(input), charset)
                .isEqualTo(expected);
    }

    @Test
    void readAsString_testFile() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/input.test")) {
            assertThat(in).isNotNull();
            assertThat(Task03Main.readAsString(in, Charset.forName("KOI8-R"))).isEqualTo("Текст прочитался правильно!");
        }
    }

    @Test
    void readAsString_multiByteCharacterSplitBetweenReads() throws IOException {
        final String expected = "Привет, мир!";
        final InputStream in = new OneByteAtATimeInputStream(new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)));

        assertThat(readAsString(in, StandardCharsets.UTF_8))
                .as("многобайтовый символ может прийти по частям, декодировать каждую порцию отдельно нельзя")
                .isEqualTo(expected);
    }

    @Test
    void readAsString_textLongerThanBuffer() throws IOException {
        final String expected = "Съешь ещё этих мягких французских булок, да выпей же чаю. ".repeat(1000);

        assertThat(readAsString(new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8))
                .isEqualTo(expected);
    }

    @Test
    void readAsString_nullStreamAndNullCharset() {
        assertThatThrownBy(() -> Task03Main.readAsString(null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readAsString_nullCharset() {
        assertThatThrownBy(() -> Task03Main.readAsString(new ByteArrayInputStream(new byte[0]), null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readAsString_nullStream() {
        assertThatThrownBy(() -> Task03Main.readAsString(null, Charset.defaultCharset())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readAsString_rethrowsIoException() {
        final InputStream failing = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("read failed");
            }
        };

        assertThatThrownBy(() -> Task03Main.readAsString(failing, StandardCharsets.UTF_8)).isInstanceOf(IOException.class);
    }

    private static final class OneByteAtATimeInputStream extends FilterInputStream {

        private OneByteAtATimeInputStream(final InputStream in) {
            super(in);
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            return super.read(b, off, Math.min(len, 1));
        }
    }
}
