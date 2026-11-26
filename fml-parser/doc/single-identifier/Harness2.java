import java.io.*;
import java.nio.file.*;
import org.openflexo.foundation.fml.parser.lexer.*;
import org.openflexo.foundation.fml.parser.parser.*;
public class Harness2 {
  public static void main(String[] a) throws Exception {
    int ok=0, ko=0;
    for (String l : Files.readAllLines(Paths.get(a[0]))) {
      int i=l.indexOf('\t'); String k=l.substring(0,i);
      String s=l.substring(i+1).replace("\\n","\n").replace("\\\\","\\");
      try {
        Parser p = new Parser(new CustomLexer(new PushbackReader(new StringReader(s), 1024), CustomLexer.EntryPointKind.valueOf(k)));
        p.parse(); ok++;
      } catch (Throwable t) { ko++; System.out.println("FAIL "+k+" "+l.substring(i+1)+" :: "+String.valueOf(t.getMessage()).replace('\n',' ')); }
    }
    System.out.println("OK=" + ok + " FAIL=" + ko);
  }
}
