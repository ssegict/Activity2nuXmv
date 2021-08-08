package ict.generate.mwe;

import org.eclipse.xtext.resource.generic.AbstractGenericResourceSupport;

import com.google.inject.Module;

public class ReaderSupport extends AbstractGenericResourceSupport {

	@Override
	protected Module createGuiceModule() {
		return new ReaderModule();
	}

}