package org.openflexo.foundation.lsp.utils;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openflexo.foundation.fml.rm.CompilationUnitResource;

/**
 * Represents the context of a document managed by the LSP server.
 * This class stores all relevant information about a single document, including:
 *   - The LSP protocol URI (string form, as received from the client).
 *   - The corresponding real {@link URI} and local {@link File}.
 *   - The raw text content of the document.
 *   - The associated {@link CompilationUnitResource} used for parsing and analysis.
 *   
 * It acts as a central data holder, allowing the server to keep track of document state,
 * its parsed representation, and the link between protocol URIs and actual file paths.
 */
public class DocumentContext {
	
	/** The URI of the document as provided by the LSP client (string form). */
    private String lspUri;

    /** The real URI representation of the document. */
    private URI realUri;

    /** The local file corresponding to the document. */
    private File fmlFile;

    /** The raw text content of the document. */
    private String rawText;

    /** The parsed compilation unit for this document. */
    private CompilationUnitResource compilationUnit;

    public DocumentContext(String lspUri, String rawText) {
    	this.lspUri  = lspUri;
    	this.setRawText(rawText);
    	
    	initUriAndFile(this.lspUri);
    }

	private void initUriAndFile(String lspUri) {
		realUri = URI.create(lspUri);
        Path path = Paths.get(realUri);
        fmlFile = path.toFile();
	}
	

	public String getLspUri() {
	    return lspUri;
	}

	public URI getRealUri() {
	    return realUri;
	}

	public File getFmlFile() {
	    return fmlFile;
	}
	
	public void setRawText(String rawText) {
		this.rawText = rawText;
	}

	public String getRawText() {
	    return rawText;
	}
	
	public void setCompilationUnit(CompilationUnitResource compilationUnit) {
		this.compilationUnit = compilationUnit;
	}
	
	public CompilationUnitResource getCompilationUnit() {
	    return compilationUnit;
	}
	
}

