import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.openflexo.foundation.fml.parser.lexer.*;
import org.openflexo.foundation.fml.parser.parser.*;
import org.openflexo.foundation.fml.parser.node.*;

public class Harness {
  public static void main(String[] a) throws Exception {
    int ok=0, ko=0;
    for (String f : Files.readAllLines(Paths.get(a[0]))) {
      CustomLexer.EntryPointKind k = f.endsWith(".fmlscript") ? CustomLexer.EntryPointKind.Script : CustomLexer.EntryPointKind.CompilationUnit;
      try (Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8")) {
        Parser p = new Parser(new CustomLexer(new PushbackReader(r, 1024), k));
        p.parse(); ok++;
      } catch (Throwable t) {
        ko++; String m = String.valueOf(t.getMessage()).replace('\n',' ');
        System.out.println("FAIL " + f + " :: " + m);
      }
    }
    System.out.println("OK=" + ok + " FAIL=" + ko);
  }
}
