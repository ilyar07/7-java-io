package com.example.task01;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Task01MainTest {

    private static Stream<Arguments> checkSums() {
        return Stream.of(
                Arguments.of(new byte[]{0x33, 0x45, 0x01}, 71),
                Arguments.of(new byte[]{}, 0),
                Arguments.of(new byte[]{0x00}, 0),
                Arguments.of(new byte[]{0x44}, 68),
                Arguments.of(new byte[]{1, 2, 3, 4}, 2),
                Arguments.of(new byte[]{10, 20, 30, 40}, 20),
                Arguments.of(new byte[]{11, 22, 33, 44}, 110),
                Arguments.of(new byte[]{0, 9, 8, 7, 6, 5, 4, 3, 2, 1}, 3193),
                Arguments.of(new byte[]{0x52, 0x11, 0x34, 0x7F, 0x0A}, 1420),
                Arguments.of(new byte[]{42, 35, 52, 62, 46, 34, 37, 2, 41, 5, 41, 4, 15, 3, 78, 36, 36, 3, 79, 37, 34, 67}, 104865115),
                // байты со старшим битом: read() возвращает 0..255, знаковое расширение byte даёт другой ответ
                Arguments.of(new byte[]{(byte) 0xFF}, 255),
                Arguments.of(new byte[]{(byte) 0x80, (byte) 0xFF, 0x00}, 1022),
                Arguments.of(new byte[]{(byte) 0xC0, (byte) 0xDE, (byte) 0xCA, (byte) 0xFE}, 1042),
                // 0xFF в середине потока: чтение до `read() != -1` по byte оборвало бы поток здесь
                Arguments.of(new byte[]{0x01, (byte) 0xFF, 0x02}, 504),
                // больше 32 сдвигов: обычный сдвиг влево потерял бы биты, циклический — нет
                Arguments.of(highBitFollowedByZeros(39), 16384),
                Arguments.of(repeated((byte) 0xFF, 33), 255)
        );
    }

    private static byte[] highBitFollowedByZeros(final int zeros) {
        final byte[] result = new byte[zeros + 1];
        result[0] = (byte) 0x80;

        return result;
    }

    private static byte[] repeated(final byte value, final int count) {
        final byte[] result = new byte[count];
        Arrays.fill(result, value);

        return result;
    }

    private static byte[] pattern(final int size) {
        final byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte) (i * 7 + 3);
        }

        return result;
    }

    @ParameterizedTest
    @MethodSource("checkSums")
    void checkSumOfStream(final byte[] input, final int expected) throws IOException {
        try (InputStream in = new ByteArrayInputStream(input)) {
            assertThat(Task01Main.checkSumOfStream(in))
                    .as("Input: %s", Arrays.toString(input))
                    .isEqualTo(expected);
        }
    }

    @Test
    void checkSumOfStream_streamGivesOneByteAtATime() throws IOException {
        final byte[] input = pattern(1000);

        try (InputStream in = new OneByteAtATimeInputStream(new ByteArrayInputStream(input))) {
            assertThat(Task01Main.checkSumOfStream(in)).isEqualTo(-1845512644);
        }
    }

    @Test
    void checkSumOfStream_doesNotCloseStream() throws IOException {
        final CloseTrackingInputStream in = new CloseTrackingInputStream(new ByteArrayInputStream(new byte[]{0x33, 0x45, 0x01}));

        Task01Main.checkSumOfStream(in);

        assertThat(in.isClosed()).as("метод не открывал поток, значит и закрывать его не должен").isFalse();
    }

    @Test
    void checkSumOfStream_rethrowsIoException() {
        final InputStream failing = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("read failed");
            }
        };

        assertThatThrownBy(() -> Task01Main.checkSumOfStream(failing)).isInstanceOf(IOException.class);
    }

    @Test
    void checkSumOfStream_nullStream() {
        assertThatThrownBy(() -> Task01Main.checkSumOfStream(null)).isInstanceOf(IllegalArgumentException.class);
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

    private static final class CloseTrackingInputStream extends FilterInputStream {

        private boolean closed;

        private CloseTrackingInputStream(final InputStream in) {
            super(in);
        }

        private boolean isClosed() {
            return closed;
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
