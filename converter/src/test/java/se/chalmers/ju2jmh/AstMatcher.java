package se.chalmers.ju2jmh;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AstMatcher extends BaseMatcher<Node> {
    private static final String NEWLINE = System.lineSeparator();
    private static final PrinterConfiguration AST_PRINTER_CONFIG =
            new DefaultPrinterConfiguration();
    private final Node expected;
    private final String expectedCode;

    private AstMatcher(Node expected) {
        this.expected = expected;
        this.expectedCode = expected.toString(AST_PRINTER_CONFIG);
    }

    @Override
    public boolean matches(Object actual) {
        if (!(actual instanceof Node)) {
            return false;
        }
        Node actualAst = (Node) actual;
        return actualAst.toString(AST_PRINTER_CONFIG).equals(expectedCode);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expected);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        super.describeMismatch(item, description);
        if (!(item instanceof Node)) {
            return;
        }
        Node actualAst = (Node) item;
        List<String> expectedLines = expectedCode.lines().collect(Collectors.toUnmodifiableList());
        List<String> actualLines = actualAst.toString(AST_PRINTER_CONFIG).lines()
                .collect(Collectors.toUnmodifiableList());
        DiffRowGenerator diffRowGenerator = DiffRowGenerator.create().build();
        List<DiffRow> diffRows = diffRowGenerator.generateDiffRows(expectedLines, actualLines);
        description.appendText(NEWLINE).appendText("diff: <");
        for (int i = 0; i < diffRows.size(); i++) {
            DiffRow diffRow = diffRows.get(i);
            switch (diffRow.getTag()) {
                case EQUAL:
                    description.appendText(NEWLINE)
                            .appendText("  ")
                            .appendText(diffRow.getOldLine());
                    break;
                case DELETE:
                    description.appendText(NEWLINE)
                            .appendText("- ")
                            .appendText(diffRow.getOldLine());
                    break;
                case INSERT:
                    description.appendText(NEWLINE)
                            .appendText("+ ")
                            .appendText(diffRow.getNewLine());
                    break;
                case CHANGE:
                    List<DiffRow> changed = new ArrayList<>();
                    changed.add(diffRow);
                    for (int j = i + 1; j < diffRows.size(); j++) {
                        DiffRow futureRow = diffRows.get(j);
                        if (futureRow.getTag() == DiffRow.Tag.CHANGE) {
                            changed.add(futureRow);
                        } else {
                            break;
                        }
                    }
                    i += changed.size() - 1;
                    int oldCount = changed.size();
                    int newCount = changed.size();
                    for (int j = changed.size() - 1; j >= 0; j--) {
                        if (!changed.get(j).getOldLine().isEmpty()) {
                            break;
                        }
                        oldCount--;
                    }
                    for (int j = changed.size() - 1; j >= 0; j--) {
                        if (!changed.get(j).getNewLine().isEmpty()) {
                            break;
                        }
                        newCount--;
                    }
                    for (int j = 0; j < oldCount; j++) {
                        description.appendText(NEWLINE)
                                .appendText("- ")
                                .appendText(changed.get(j).getOldLine());
                    }
                    for (int j = 0; j < newCount; j++) {
                        description.appendText(NEWLINE)
                                .appendText("+ ")
                                .appendText(changed.get(j).getNewLine());
                    }
                    break;
            }
        }
        description.appendText(NEWLINE).appendText(">");
    }

    public static Matcher<Node> equalsAst(Node ast) {
        return new AstMatcher(ast);
    }
}
