package se.chalmers.ju2jmh.junit5;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

public class ComplexLifecycleTest {

    private static List<String> sharedResource;
    private List<String> transactionLog;


    @BeforeAll
    public static void globalSetup() {
        sharedResource = new ArrayList<>();
        sharedResource.add("DB_CONFIG");
        try { Thread.sleep(50); } catch (InterruptedException e) {}
    }

    @AfterAll
    public static void globalTeardown() {
        sharedResource = null;
    }

    @BeforeEach
    public void methodSetup() {
        transactionLog = new ArrayList<>();
        transactionLog.add("START_TX");
    }

    @AfterEach
    public void methodTeardown() {
        transactionLog.clear();
    }

    @Test
    public void testInteraction() {
        if (!sharedResource.contains("DB_CONFIG")) {
            throw new RuntimeException("Global setup failed");
        }
        transactionLog.add("COMMIT");
    }
}