package org.openflexo.foundation.lsp.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitParser;
import org.openflexo.foundation.fml.parser.ParseException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;


/**
 * Utility class for parsing FML documents and converting parsing errors
 * into LSP diagnostics.
 */
public class LSPParsing {
	
	private static final Logger logger = Logger.getLogger(LSPParsing.class.getPackage().getName());

    /**
     * Creates a {@link PublishDiagnosticsParams} object from a {@link ParseException}.
     * Converts the exception details (line, position, message) into an LSP {@link Diagnostic}.
     *
     * @param exception the parsing exception thrown by the parser
     * @param uri the URI of the document being parsed
     * @return diagnostics object containing the error information
     */
    private static PublishDiagnosticsParams createDiagnostic(ParseException exception, String uri) {
        Diagnostic diagnostic = new Diagnostic();

        // By default, mark the diagnostic as an error (this could be made configurable)
        diagnostic.setSeverity(DiagnosticSeverity.Error);


        System.out.println(exception.getMessage());

        // Set the diagnostic message for the LSP client
        diagnostic.setMessage(exception.getMessage());

        // LSP counts lines and characters starting from 0, so we subtract 1 to match its format.
        int line = exception.getLine() - 1;
        int charStart = exception.getPosition() - 1;

        diagnostic.setRange(new Range(
            new Position(line, charStart),
            new Position(line, charStart + exception.getLength())
        ));

        // Create the diagnostic container for the specified document
        PublishDiagnosticsParams diagnostics = new PublishDiagnosticsParams();
        diagnostics.setUri(uri);
        diagnostics.setDiagnostics(Collections.singletonList(diagnostic));

        return diagnostics;
    }

    /**
     * Parses a given FML document using the provided parser and resource.
     * If parsing fails with a {@link ParseException}, returns diagnostics
     * for the LSP client. On {@link IOException}, no diagnostics are returned
     * (TODO: handle more gracefully).
     *
     * @param uri the URI of the document to parse
     * @param newText the raw text content of the document
     * @param fmlResource the resource representing the FML document
     * @param fmlParser the parser used to parse the document
     * @return diagnostics if a parsing error occurred, or {@code null} if parsing succeeded or failed with an I/O error
     */
    public static PublishDiagnosticsParams parse(
        String uri,
        String newText,
        CompilationUnitResource fmlResource,
        FMLCompilationUnitParser fmlParser
    ) {
    	
    	if (logger.isLoggable(Level.INFO)) {
			logger.info("Parsing started for document: " + uri);
		}

        try {
            // Parse the document
            FMLCompilationUnit parsedCompilationUnit = fmlParser.parse(
                newText,
                fmlResource.getFactory(),
                (modelSlotClasses) -> fmlResource.updateFMLModelFactory(modelSlotClasses),
                true
            );

            fmlResource.setUnparseableContents(null);
            parsedCompilationUnit.setResource(fmlResource);
     
        } catch (IOException e) {
            // TODO: Properly log or handle I/O errors (currently ignored)
            e.printStackTrace();
        } catch (ParseException e) {
            // Return diagnostics based on the parsing exception
            return createDiagnostic(e, uri);
        }

        // No diagnostics on success (or unhandled I/O failure)
        return null;
    }
}
