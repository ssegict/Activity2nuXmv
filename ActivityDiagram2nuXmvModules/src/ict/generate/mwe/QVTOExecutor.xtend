package ict.generate.mwe

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import org.eclipse.emf.common.util.BasicDiagnostic
import org.eclipse.emf.common.util.Diagnostic
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext
import org.eclipse.m2m.qvt.oml.BasicModelExtent
import org.eclipse.m2m.qvt.oml.ExecutionContextImpl
import org.eclipse.m2m.qvt.oml.TransformationExecutor
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.diagnostics.Severity
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.validation.CheckMode

/**
 * Loads all Models in given input directory an performs the given QVTO Transformation on them
 * All contents of the input Models are joined together befor execution
 * Saves the output afterwards to outFile
 */
class QVTOExecutor implements IWorkflowComponent {
	@Accessors String inDirectory = ""
	@Accessors String outFile = ""
	@Accessors String transformation = ""
	
	/**
	 * This code is based on https://wiki.eclipse.org/QVTOML/Examples/InvokeInJava
	 */
	def exec(Iterable<String> filenames) throws IOException {
		// Refer to an existing transformation via URI
		var transformationURI = URI.createURI(transformation)
		// create executor for the given transformation
		var executor = new TransformationExecutor(transformationURI)

		// define the transformation input
		// Remark: we take the objects from a resource, however
		// a list of arbitrary in-memory EObjects may be passed
		var resourceSet = new ResourceSetImpl()
		UMLResourcesUtil.init(resourceSet)
		
		//Generate resources for all input Files and load them
		var inObjects = new ArrayList<EObject>
		var inResources = new ArrayList<Resource>
		for(inFile : filenames) {
			var res = resourceSet.getResource(URI.createFileURI(inDirectory + "/" + inFile), true)
			res.load(null)
			res.URI = URI.createFileURI(inFile)
			inResources.add(res)
		}
		
		//check for validation errors
		var foundErrors = false
		for(res : inResources) {
			var provider = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(res.getURI());
			if (provider !== null) {
				var result = provider.getResourceValidator().validate(res, CheckMode.ALL, null);
				result.forEach[println(it.message)]
				if(! result.filter[severity === Severity.ERROR].empty) {
					foundErrors = true
				}
			}
			inObjects.addAll(res.contents)
		}
		if(foundErrors) {
			throw new RuntimeException("Validation errors have been found during loading cannot proceed")
		}
		

		// create the input extent with its initial contents
		var input = new BasicModelExtent(inObjects)

		// setup the execution environment details -> 
		// configuration properties, logger, monitor object etc.
		var context = new ExecutionContextImpl()
		context.setConfigProperty("keepModeling", true)

		// run the transformation assigned to the executor with the given
		// input and output and execution context -> ChangeTheWorld(in, out)
		// Remark: variable arguments count is supported
		var result = executor.execute(context, input)
		
		// check the result for success
		if(result.getSeverity() == Diagnostic.OK) {
			// the output objects got captured in the output extent
			var outObjects = input.getContents()
			// let's persist them using a resource
			var outSet = new ResourceSetImpl()
			UMLResourcesUtil.init(outSet)
			var outResource = outSet.createResource(
					URI.createURI(outFile))
			outResource.getContents().addAll(outObjects)
			
			outResource.save(Collections.emptyMap())
		} else {
			// turn the result diagnostic into status and send it to error log
			var status = BasicDiagnostic.toIStatus(result)
			println("Error during Transformation!!!!\n" + status)
			System.exit(0)
		}
	}
	
	override invoke(IWorkflowContext ctx) {
		try {
			exec(new File(inDirectory).list.filter[endsWith(".uml")])
		} catch (IOException e) {
			throw new RuntimeException("Error during Transformation", e)
		}
		
	}
	
	override postInvoke() {}
	override preInvoke() {}
}
