package org.openflexo.foundation.fml.parser;

import java.io.File;

import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;
import org.sablecc.sablecc.SableCC;

public class CompileFMLParser {

	public static void main(String[] args) {
		File grammar = ((FileResourceImpl) ResourceLocator.locateResource("FML/fml-grammar.sablecc")).getFile();
		System.out.println("file : " + grammar.getAbsolutePath());
		System.out.println("exist=" + grammar.exists());
		File output = grammar.getParentFile().getParentFile().getParentFile().getParentFile();
		output = new File(output, "src/main/java");
		System.out.println("output=" + output);
		// File output = ((FileResourceImpl) ResourceLocator.locateResource("fml-parser")).getFile();
		try {
			SableCC.processGrammar(grammar, output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
