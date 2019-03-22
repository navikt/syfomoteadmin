package no.nav.syfo.util;

import java.util.List;
import java.util.function.BiConsumer;

public class TestUtil {
    private TestUtil() {
    }

    public static <T, U> void biForEach(List<T> c1, List<U> c2, BiConsumer<T, U> consumer) {
        biForEach(c1, c2, 0, consumer);
    }

    public static <T, U> void biForEach(List<T> c1, List<U> c2, int offset, BiConsumer<T, U> consumer) {
        for (int i = 0; i < c1.size(); i++) {
            consumer.accept(c1.get(i), c2.get(i + offset));
        }
    }
}
