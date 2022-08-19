package se.chalmers.ju2jmh.experiments;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class Junit4TestInvokerTest {
    private static void assertNextEventsAre(Queue<String> events, String... expected) {
        Queue<String> current = new ArrayDeque<>(expected.length);
        while (current.size() < expected.length) {
            current.add(events.remove());
        }
        assertThat(current, hasItems(expected));
    }

    private static Queue<String> events;

    @Before
    public void createEventQueue() {
        events = new ArrayDeque<>();
    }

    @Ignore
    public static class SimpleUnitTest {
        @Test
        public void test() {
            events.add("test");
        }
    }

    @Test
    public void simpleUnitTest() throws Throwable {
        JUnit4TestInvoker.invoke(SimpleUnitTest.class, "test");

        assertNextEventsAre(events, "test");
    }

    @Ignore
    public static class UnitTestWithFixture {
        @Before
        public void before1() {
            Junit4TestInvokerTest.events.add("before1");
        }

        @Before
        public void before2() {
            Junit4TestInvokerTest.events.add("before2");
        }

        @After
        public void after1() {
            Junit4TestInvokerTest.events.add("after1");
        }

        @After
        public void after2() {
            Junit4TestInvokerTest.events.add("after2");
        }

        @Test
        public void test() {
            Junit4TestInvokerTest.events.add("test");
        }
    }

    @Test
    public void unitTestWithFixture() throws Throwable {
        JUnit4TestInvoker.invoke(UnitTestWithFixture.class, "test");

        assertNextEventsAre(events, "before1", "before2");
        assertNextEventsAre(events, "test");
        assertNextEventsAre(events, "after1", "after2");
    }

    private static class EventLoggingRule implements TestRule {
        private final String name;

        private EventLoggingRule(String name) {
            this.name = name;
        }

        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    Junit4TestInvokerTest.events.add(name + " before");
                    base.evaluate();
                    Junit4TestInvokerTest.events.add(name + " after");
                }
            };
        }
    }

    @Ignore
    public static class UnitTestWithRulesAndFixture {
        @Before
        public void before1() {
            Junit4TestInvokerTest.events.add("before1");
        }

        @Before
        public void before2() {
            Junit4TestInvokerTest.events.add("before2");
        }

        @After
        public void after1() {
            Junit4TestInvokerTest.events.add("after1");
        }

        @After
        public void after2() {
            Junit4TestInvokerTest.events.add("after2");
        }

        @Rule
        public TestRule fieldRule1 = new EventLoggingRule("fieldRule1");

        @Rule
        public TestRule fieldRule2 = new EventLoggingRule("fieldRule2");

        @Rule
        public TestRule methodRule1() {
            return new EventLoggingRule("methodRule1");
        }

        @Rule
        public TestRule methodRule2() {
            return new EventLoggingRule("methodRule2");
        }

        @Test
        public void test() {
            Junit4TestInvokerTest.events.add("test");
        }
    }

    @Test
    public void unitTestWithRulesAndFixture() throws Throwable {
        JUnit4TestInvoker.invoke(UnitTestWithRulesAndFixture.class, "test");

        assertNextEventsAre(events, "fieldRule1 before", "fieldRule2 before");
        assertNextEventsAre(events, "methodRule1 before", "methodRule2 before");
        assertNextEventsAre(events, "before1", "before2");
        assertNextEventsAre(events, "test");
        assertNextEventsAre(events, "after1", "after2");
        assertNextEventsAre(events, "methodRule1 after", "methodRule2 after");
        assertNextEventsAre(events, "fieldRule1 after", "fieldRule2 after");
    }

    @Ignore
    public static class UnitTestWithInheritance extends UnitTestWithRulesAndFixture {
        @Before
        public void subclassBefore() {
            Junit4TestInvokerTest.events.add("subclassBefore");
        }

        @After
        public void subclassAfter() {
            Junit4TestInvokerTest.events.add("subclassAfter");
        }

        @Rule
        public TestRule subclassFieldRule = new EventLoggingRule("subclassFieldRule");

        @Rule
        public TestRule subclassMethodRule() {
            return new EventLoggingRule("subclassMethodRule");
        }
    }

    @Test
    public void unitTestWithInheritance() throws Throwable {
        JUnit4TestInvoker.invoke(UnitTestWithInheritance.class, "test");

        assertNextEventsAre(events, "fieldRule1 before", "fieldRule2 before",
                "subclassFieldRule before");
        assertNextEventsAre(events, "methodRule1 before", "methodRule2 before",
                "subclassMethodRule before");
        assertNextEventsAre(events, "before1", "before2", "subclassBefore");
        assertNextEventsAre(events, "test");
        assertNextEventsAre(events, "after1", "after2", "subclassAfter");
        assertNextEventsAre(events, "methodRule1 after", "methodRule2 after",
                "subclassMethodRule after");
        assertNextEventsAre(events, "fieldRule1 after", "fieldRule2 after",
                "subclassFieldRule after");
    }
}
