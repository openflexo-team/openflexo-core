package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.fml.rm.FMLParser;
import org.openflexo.foundation.fml.rm.FMLParserFactory;

public class DefaultFMLParserFactory implements FMLParserFactory {

	@Override
	public FMLParser makeFMLParser() {
		return new DefaultFMLParser();
	}

}
