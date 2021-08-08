package ict.generate.mwe;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.eclipse.xtext.resource.containers.IAllContainersState;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;

/**
 * Ignores Model pre Validation
 *
 */
public class Reader extends org.eclipse.xtext.mwe.Reader {
	private boolean ignoreValidationErrors = false;

	public Reader() {
		super();
	}

	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		if (ignoreValidationErrors) {
			getValidator().setStopOnError(false);
			Issues temp = new org.eclipse.emf.mwe.core.issues.IssuesImpl();
			invokeInternalCopy(ctx, temp);
			System.out.println(getValidator().toString(temp));
		} else {
			invokeInternalCopy(ctx, issues);
		}
	}

	public void setIgnoreValidationErrors(boolean value) {
		this.ignoreValidationErrors = value;
	}

	private void invokeInternalCopy(WorkflowContext ctx, Issues issues) {
		ResourceSet resourceSet = getResourceSet();

		// fix from
		// https://www.eclipse.org/forums/index.php?t=msg&th=197123&goto=1278522&#msg_1278522
		// initializes the resource set to be used with UML
		UMLResourcesUtil.init(resourceSet);

		Multimap<String, URI> uris = getPathTraverser().resolvePathes(this.pathes, new Predicate<URI>() {
			@Override
			public boolean apply(URI input) {
				boolean result = true;
				if (Reader.this.getUriFilter() != null)
					result = Reader.this.getUriFilter().matches(input);
				if (result)
					result = (Reader.this.getRegistry().getResourceServiceProvider(input) != null);
				return result;
			}
		});
		IAllContainersState containersState = getContainersStateFactory().getContainersState(this.pathes, uris);
		installAsAdapter(resourceSet, containersState);
		populateResourceSet(resourceSet, uris);
		getValidator().validate(resourceSet, getRegistry(), issues);
		addModelElementsToContext(ctx, resourceSet);
	}
}
