package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HeavyCalculationTest {

    @Test
    public void testSortLargeList() {

        List<Integer> numbers = new Random().ints(10_000).boxed().collect(Collectors.toList());

        //benchmarking the sorting of a large list
        Collections.sort(numbers);
    }

    @Test
    public void testStreamProcessing() {
        long sum = IntStream.range(0, 100_000)
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> n * n)
                .sum();

        if (sum == 0) throw new RuntimeException("Impossible");
    }

    @Test
    public void benchmarkWorkload() {
        int a = 1 + 10;
    }
}