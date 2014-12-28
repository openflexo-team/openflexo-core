package test.java5;

import java.io.File;

import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;
import org.sablecc.sablecc.SableCC;

public class CompileJavaPreProcessor {

	public static void main(String[] args) {
		File grammar = ((FileResourceImpl) ResourceLocator.locateResource("Java1.5/java-1.5-preprocessor.sablecc")).getFile();
		System.out.println("file : " + grammar.getAbsolutePath());
		System.out.println("exist=" + grammar.exists());
		File output = grammar.getParentFile().getParentFile().getParentFile().getParentFile();
		output = new File(output, "src/dev/java");
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
