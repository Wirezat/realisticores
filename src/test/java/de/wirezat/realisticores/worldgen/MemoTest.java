package de.wirezat.realisticores.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MemoTest {
    @Test
    void aValueIsComputedOnceUntilTheCacheIsFull() {
        Memo<Integer, Integer> memo = new Memo<>(2);
        AtomicInteger computed = new AtomicInteger();
        memo.get(1, key -> computed.incrementAndGet());
        memo.get(1, key -> computed.incrementAndGet());
        memo.get(2, key -> computed.incrementAndGet());
        assertEquals(2, computed.get());
        memo.get(3, key -> computed.incrementAndGet());
        memo.get(1, key -> computed.incrementAndGet());
        assertEquals(4, computed.get());
    }
}
