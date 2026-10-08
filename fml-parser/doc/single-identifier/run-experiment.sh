#!/bin/bash
# Usage: run-experiment.sh <grammar.sablecc> <files-list.txt> [sablecc-jar]
# Generates the parser in a temp dir (prints SableCC conflicts), compiles it with the real CustomLexer,
# parses every file of the list (.fmlscript -> Script entry point, others -> CompilationUnit),
# prints failures and "OK=n FAIL=m". Needs a JDK 8 and the sablecc-maven.sablecc-3.7.jar of the gradle cache.
set -e
HERE="$(cd "$(dirname "$0")" && pwd)"
GRAMMAR="$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"; LIST="$(cd "$(dirname "$2")" && pwd)/$(basename "$2")"
JAR="${3:-$(find ~/.gradle/caches/modules-2 -name 'sablecc-maven.sablecc-3.7.jar' | head -1)}"
LEXER="$HERE/../../src/main/java/org/openflexo/foundation/fml/parser/lexer/CustomLexer.java"
W="$(mktemp -d)"; cp "$GRAMMAR" "$W/fml.sablecc"; cd "$W"; mkdir out cls
java -cp "$JAR" org.sablecc.sablecc.SableCC -d out fml.sablecc 2>&1 | tr -d '.' | grep -v '^$' > log.txt || true
if grep -q "conflict in state" log.txt; then
  grep -E "conflict in state" log.txt | sed 's/ on T.*//' | sort | uniq -c; echo "CONFLICTS (log: $W/log.txt)"; exit 1
fi
cp "$LEXER" out/org/openflexo/foundation/fml/parser/lexer/
javac -nowarn -d cls $(find out -name '*.java') "$HERE/Harness.java"
java -cp cls:out Harness "$LIST" | tail -${TAIL:-5}
