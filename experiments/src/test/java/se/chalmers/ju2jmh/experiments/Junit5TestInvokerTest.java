package se.chalmers.ju2jmh.experiments;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test; // Nota: non importiamo più Disabled
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class Junit5TestInvokerTest {

    private static void assertNextEventsAre(Queue<String> events, String... expected) {
        Queue<String> current = new ArrayDeque<>(expected.length);
        while (current.size() < expected.length) {
            if (events.isEmpty()) {
                throw new AssertionError("La coda degli eventi è vuota! Il test interno non è stato eseguito. Atteso: " + expected[0]);
            }
            current.add(events.remove());
        }
        assertThat(current, hasItems(expected));
    }


    private static Queue<String> events = new ArrayDeque<>();

    @BeforeEach
    public void createEventQueue() {
        events.clear();
    }


    public static class SimpleUnitTest {
        @Test
        public void test() {

            if (events != null) events.add("test");
        }
    }

    @Test
    public void simpleUnitTest() throws Throwable {
        JUnit5TestInvoker.invoke(SimpleUnitTest.class, "test");
        assertNextEventsAre(events, "test");
    }


    public static class UnitTestWithFixture {
        @BeforeEach
        public void before1() {
            if (events != null) events.add("before1");
        }

        @BeforeEach
        public void before2() {
            if (events != null) events.add("before2");
        }

        @AfterEach
        public void after1() {
            if (events != null) events.add("after1");
        }

        @AfterEach
        public void after2() {
            if (events != null) events.add("after2");
        }

        @Test
        public void test() {
            if (events != null) events.add("test");
        }
    }

    @Test
    public void unitTestWithFixture() throws Throwable {
        JUnit5TestInvoker.invoke(UnitTestWithFixture.class, "test");

        assertNextEventsAre(events, "before1", "before2");
        assertNextEventsAre(events, "test");
        assertNextEventsAre(events, "after1", "after2");
    }

    public static class EventLoggingExtension implements BeforeEachCallback, AfterEachCallback {
        private final String name;

        public EventLoggingExtension(String name) {
            this.name = name;
        }

        @Override
        public void beforeEach(ExtensionContext context) {
            if (events != null) events.add(name + " before");
        }

        @Override
        public void afterEach(ExtensionContext context) {
            if (events != null) events.add(name + " after");
        }
    }

    // --- Caso 3: Extensions e Fixtures ---
    // RIMOSSO @Disabled
    public static class UnitTestWithExtensionsAndFixture {
        @BeforeEach
        public void before1() {
            if (events != null) events.add("before1");
        }

        @BeforeEach
        public void before2() {
            if (events != null) events.add("before2");
        }

        @AfterEach
        public void after1() {
            if (events != null) events.add("after1");
        }

        @AfterEach
        public void after2() {
            if (events != null) events.add("after2");
        }

        @RegisterExtension
        public EventLoggingExtension ext1 = new EventLoggingExtension("ext1");

        @RegisterExtension
        public EventLoggingExtension ext2 = new EventLoggingExtension("ext2");

        @Test
        public void test() {
            if (events != null) events.add("test");
        }
    }

    @Test
    public void unitTestWithExtensionsAndFixture() throws Throwable {
        JUnit5TestInvoker.invoke(UnitTestWithExtensionsAndFixture.class, "test");

        assertNextEventsAre(events, "ext1 before", "ext2 before");
        assertNextEventsAre(events, "before1", "before2");
        assertNextEventsAre(events, "test");
        assertNextEventsAre(events, "after1", "after2");
        assertNextEventsAre(events, "ext2 after", "ext1 after");
    }

    public static class UnitTestWithInheritance extends UnitTestWithExtensionsAndFixture {
        @BeforeEach
        public void subclassBefore() {
            if (events != null) events.add("subclassBefore");
        }

        @AfterEach
        public void subclassAfter() {
            if (events != null) events.add("subclassAfter");
        }

        @RegisterExtension
        public EventLoggingExtension subclassExt = new EventLoggingExtension("subclassExt");
    }

    @Test
    public void unitTestWithInheritance() throws Throwable {
        JUnit5TestInvoker.invoke(UnitTestWithInheritance.class, "test");

        assertNextEventsAre(events, "ext1 before", "ext2 before", "subclassExt before");
        assertNextEventsAre(events, "before1", "before2", "subclassBefore");

        assertNextEventsAre(events, "test");
        assertNextEventsAre(events, "subclassAfter", "after1", "after2");
        assertNextEventsAre(events, "subclassExt after", "ext2 after", "ext1 after");
    }
}