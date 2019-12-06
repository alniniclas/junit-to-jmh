package se.chalmers.ju2jmh;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.stream.Collectors;

public class AstMatcher extends BaseMatcher<CompilationUnit> {
    private static final PrettyPrinterConfiguration AST_PRETTY_PRINTER_CONFIG =
            new PrettyPrinterConfiguration();
    private final CompilationUnit expectedAst;

    private AstMatcher(CompilationUnit expectedAst) {
        this.expectedAst = StaticJavaParser.parse(expectedAst.toString(AST_PRETTY_PRINTER_CONFIG));
    }

    @Override
    public boolean matches(Object actual) {
        if (!(actual instanceof CompilationUnit)) {
            return false;
        }
        CompilationUnit actualAst = StaticJavaParser.parse(actual.toString());
        return actualAst.equals(expectedAst);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expectedAst);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        super.describeMismatch(item, description);
        if (!(item instanceof CompilationUnit)) {
            return;
        }
        CompilationUnit actualAst = (CompilationUnit) item;
        List<String> expectedLines = expectedAst.toString(AST_PRETTY_PRINTER_CONFIG).lines()
                .collect(Collectors.toUnmodifiableList());
        List<String> actualLines = actualAst.toString(AST_PRETTY_PRINTER_CONFIG).lines()
                .collect(Collectors.toUnmodifiableList());
        DiffRowGenerator diffRowGenerator = DiffRowGenerator.create()
                .build();
        try {
            List<DiffRow> diffRows = diffRowGenerator.generateDiffRows(expectedLines, actualLines);
            String newline = System.lineSeparator();
            description.appendText(newline).appendText("diff: <");
            for (DiffRow diffRow : diffRows) {
                switch (diffRow.getTag()) {
                    case EQUAL:
                        break;
                    case DELETE:
                        description.appendText(newline)
                                .appendText("- ")
                                .appendText(diffRow.getOldLine());
                        break;
                    case INSERT:
                        description.appendText(newline)
                                .appendText("+ ")
                                .appendText(diffRow.getNewLine());
                        break;
                    case CHANGE:
                        description.appendText(newline)
                                .appendText("- ")
                                .appendText(diffRow.getOldLine())
                                .appendText(newline)
                                .appendText("+ ")
                                .appendText(diffRow.getNewLine());
                        break;
                }
            }
            description.appendText(newline).appendText(">");
        } catch (DiffException e) {
            description.appendText(", diff unavailable");
        }
    }

    public static Matcher<CompilationUnit> equalsAst(CompilationUnit ast) {
        return new AstMatcher(ast);
    }
}
