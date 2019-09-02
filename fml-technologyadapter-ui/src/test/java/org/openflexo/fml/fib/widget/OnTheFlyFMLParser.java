package org.openflexo.fml.fib.widget;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

public class OnTheFlyFMLParser extends AbstractParser {

	// private SAXParserFactory spf;
	private DefaultParseResult result;
	// private EntityResolver entityResolver;

	public OnTheFlyFMLParser() {
		result = new DefaultParseResult(this);
	}

	/**
	 * Returns whether this parser does DTD validation.
	 *
	 * @return Whether this parser does DTD validation.
	 * @see #setValidating(boolean)
	 */
	/*public boolean isValidating() {
		return false;
	}*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParseResult parse(RSyntaxDocument doc, String style) {

		result.clearNotices();
		// Element root = doc.getDefaultRootElement();
		result.setParsedLines(0, 10);

		result.addNotice(new DefaultParserNotice(this, "Premiere erreur", 0, 3, -1));
		result.addNotice(new DefaultParserNotice(this, "Deuxieme erreur", 1, 25, 4));
		result.addNotice(new DefaultParserNotice(this, "Troisieme erreur", 3, -1, -1));

		/*if (spf == null || doc.getLength() == 0) {
			return result;
		}
		
		try {
			SAXParser sp = spf.newSAXParser();
			Handler handler = new Handler(doc);
			DocumentReader r = new DocumentReader(doc);
			InputSource input = new InputSource(r);
			sp.parse(input, handler);
			r.close();
		} catch (SAXParseException spe) {
			// A fatal parse error - ignore; a ParserNotice was already created.
		} catch (Exception e) {
			// e.printStackTrace(); // Will print if DTD specified and can't be found
			result.addNotice(new DefaultParserNotice(this, "Error parsing XML: " + e.getMessage(), 0, -1, -1));
		}*/

		return result;

	}

	/**
	 * Sets whether this parser will use DTD validation if required.
	 *
	 * @param validating
	 *            Whether DTD validation should be enabled. If this is <code>true</code>, documents must specify a DOCTYPE, and you should
	 *            have used the constructor specifying an entity resolver.
	 * @see #isValidating()
	 */
	/*public void setValidating(boolean validating) {
		spf.setValidating(validating);
	}*/

	/*
		public static void main(String[] args) {
			javax.swing.JFrame frame = new javax.swing.JFrame();
			org.fife.ui.rsyntaxtextarea.RSyntaxTextArea textArea = new
				org.fife.ui.rsyntaxtextarea.RSyntaxTextArea(25, 40);
			textArea.setSyntaxEditingStyle("text/xml");
			XmlParser parser = new XmlParser(new EntityResolver() {
				public InputSource resolveEntity(String publicId, String systemId)
						throws SAXException, IOException {
			    	if ("http://fifesoft.com/rsyntaxtextarea/theme.dtd".equals(systemId)) {
			    		return new org.xml.sax.InputSource(getClass().getResourceAsStream("/theme.dtd"));
			    	}
			    	return null;
				}
			});
			parser.setValidating(true);
			textArea.addParser(parser);
			try {
				textArea.read(new java.io.BufferedReader(new java.io.FileReader("C:/temp/test.xml")), null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			frame.setContentPane(new org.fife.ui.rtextarea.RTextScrollPane(textArea));
			frame.pack();
			frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
	*/

	/**
	 * Callback notified when errors are found in the XML document. Adds a notice to be squiggle-underlined.
	 */
	/*private final class Handler extends DefaultHandler {
	
		private Document doc;
	
		private Handler(Document doc) {
			this.doc = doc;
		}
	
		private void doError(SAXParseException e, ParserNotice.Level level) {
			int line = e.getLineNumber() - 1;
			Element root = doc.getDefaultRootElement();
			Element elem = root.getElement(line);
			int offs = elem.getStartOffset();
			int len = elem.getEndOffset() - offs;
			if (line == root.getElementCount() - 1) {
				len++;
			}
			DefaultParserNotice pn = new DefaultParserNotice(XmlParser.this, e.getMessage(), line, offs, len);
			pn.setLevel(level);
			result.addNotice(pn);
		}
	
		@Override
		public void error(SAXParseException e) {
			doError(e, ParserNotice.Level.ERROR);
		}
	
		@Override
		public void fatalError(SAXParseException e) {
			doError(e, ParserNotice.Level.ERROR);
		}
	
		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
			if (entityResolver != null) {
				return entityResolver.resolveEntity(publicId, systemId);
			}
			return super.resolveEntity(publicId, systemId);
		}
	
		@Override
		public void warning(SAXParseException e) {
			doError(e, ParserNotice.Level.WARNING);
		}
	
	}*/

}
