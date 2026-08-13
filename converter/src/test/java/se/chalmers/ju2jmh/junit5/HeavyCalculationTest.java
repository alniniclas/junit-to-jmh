package se.chalmers.ju2jmh.junit5;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HeavyCalculationTest {

    @Test
    public void testSortLargeList() {
        List<Integer> numbers = new Random().ints(10_000).boxed().collect(Collectors.toList());

        Collections.sort(numbers);
    }

    @Test
    public void testStreamProcessing() {
        long sum = IntStream.range(0, 100_000)
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> n * n)
                .sum();

        if (sum == 0) throw new RuntimeException("Impossibile");
    }

    @Test
    public void benchmarkWorkload() {
        int a = 1 + 10;
    }
}