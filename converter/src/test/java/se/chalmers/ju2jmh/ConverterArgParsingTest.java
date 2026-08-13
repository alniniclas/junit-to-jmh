package se.chalmers.ju2jmh;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterArgParsingTest {

    @Test
    public void parsesClassAndMethodArgs() {
        Converter converter = new Converter();
        CommandLine cmd = new CommandLine(converter);
        String[] args = new String[]{"src", "classes", "out", "com.example.Test", "com.example.Other#m1,m2"};
        ParseResult pr = cmd.parseArgs(args);
        assertNotNull(pr);
    }
}
