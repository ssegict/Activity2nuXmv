package ict.generate.mwe;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.resource.generic.AbstractGenericResourceRuntimeModule;

public class ReaderModule extends AbstractGenericResourceRuntimeModule {

	@Override
	protected String getLanguageName() {
		return "UMLEditor";
	}

	@Override
	protected String getFileExtensions() {
		return "uml";//
	}

	public Class<? extends IGenerator> bindIGenerator() {
		return null;
	}

	public Class<? extends ResourceSet> bindResourceSet() {
		return ResourceSetImpl.class;
	}
}
