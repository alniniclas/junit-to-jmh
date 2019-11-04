package se.chalmers.ju2jmh;

import picocli.CommandLine;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "ju2jmh", mixinStandardHelpOptions = true)
public class Converter implements Callable<Integer> {
    @CommandLine.Parameters()
    private Path inputFile;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Converter()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        if (!Files.exists(inputFile)) {
            throw new FileNotFoundException(inputFile + " does not exist");
        }
        System.out.println(Files.readString(inputFile));
        return 0;
    }
}
