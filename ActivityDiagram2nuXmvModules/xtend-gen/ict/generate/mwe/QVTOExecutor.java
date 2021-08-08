package ict.generate.mwe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;
import org.eclipse.m2m.qvt.oml.BasicModelExtent;
import org.eclipse.m2m.qvt.oml.ExecutionContextImpl;
import org.eclipse.m2m.qvt.oml.ExecutionDiagnostic;
import org.eclipse.m2m.qvt.oml.TransformationExecutor;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * Loads all Models in given input directory an performs the given QVTO Transformation on them
 * All contents of the input Models are joined together befor execution
 * Saves the output afterwards to outFile
 */
@SuppressWarnings("all")
public class QVTOExecutor implements IWorkflowComponent {
  @Accessors
  private String inDirectory = "";
  
  @Accessors
  private String outFile = "";
  
  @Accessors
  private String transformation = "";
  
  /**
   * This code is based on https://wiki.eclipse.org/QVTOML/Examples/InvokeInJava
   */
  public void exec(final Iterable<String> filenames) throws IOException {
    URI transformationURI = URI.createURI(this.transformation);
    TransformationExecutor executor = new TransformationExecutor(transformationURI);
    ResourceSetImpl resourceSet = new ResourceSetImpl();
    UMLResourcesUtil.init(resourceSet);
    ArrayList<EObject> inObjects = new ArrayList<EObject>();
    ArrayList<Resource> inResources = new ArrayList<Resource>();
    for (final String inFile : filenames) {
      {
        Resource res = resourceSet.getResource(URI.createFileURI(((this.inDirectory + "/") + inFile)), true);
        res.load(null);
        res.setURI(URI.createFileURI(inFile));
        inResources.add(res);
      }
    }
    boolean foundErrors = false;
    for (final Resource res : inResources) {
      {
        IResourceServiceProvider provider = IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(res.getURI());
        if ((provider != null)) {
          List<Issue> result = provider.getResourceValidator().validate(res, CheckMode.ALL, null);
          final Consumer<Issue> _function = (Issue it) -> {
            InputOutput.<String>println(it.getMessage());
          };
          result.forEach(_function);
          final Function1<Issue, Boolean> _function_1 = (Issue it) -> {
            Severity _severity = it.getSeverity();
            return Boolean.valueOf((_severity == Severity.ERROR));
          };
          boolean _isEmpty = IterableExtensions.isEmpty(IterableExtensions.<Issue>filter(result, _function_1));
          boolean _not = (!_isEmpty);
          if (_not) {
            foundErrors = true;
          }
        }
        inObjects.addAll(res.getContents());
      }
    }
    if (foundErrors) {
      throw new RuntimeException("Validation errors have been found during loading cannot proceed");
    }
    BasicModelExtent input = new BasicModelExtent(inObjects);
    ExecutionContextImpl context = new ExecutionContextImpl();
    context.setConfigProperty("keepModeling", Boolean.valueOf(true));
    ExecutionDiagnostic result = executor.execute(context, input);
    int _severity = result.getSeverity();
    boolean _equals = (_severity == Diagnostic.OK);
    if (_equals) {
      List<EObject> outObjects = input.getContents();
      ResourceSetImpl outSet = new ResourceSetImpl();
      UMLResourcesUtil.init(outSet);
      Resource outResource = outSet.createResource(
        URI.createURI(this.outFile));
      outResource.getContents().addAll(outObjects);
      outResource.save(Collections.<Object, Object>emptyMap());
    } else {
      IStatus status = BasicDiagnostic.toIStatus(result);
      InputOutput.<String>println(("Error during Transformation!!!!\n" + status));
      System.exit(0);
    }
  }
  
  @Override
  public void invoke(final IWorkflowContext ctx) {
    try {
      final Function1<String, Boolean> _function = (String it) -> {
        return Boolean.valueOf(it.endsWith(".uml"));
      };
      this.exec(IterableExtensions.<String>filter(((Iterable<String>)Conversions.doWrapArray(new File(this.inDirectory).list())), _function));
    } catch (final Throwable _t) {
      if (_t instanceof IOException) {
        final IOException e = (IOException)_t;
        throw new RuntimeException("Error during Transformation", e);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Override
  public void postInvoke() {
  }
  
  @Override
  public void preInvoke() {
  }
  
  @Pure
  public String getInDirectory() {
    return this.inDirectory;
  }
  
  public void setInDirectory(final String inDirectory) {
    this.inDirectory = inDirectory;
  }
  
  @Pure
  public String getOutFile() {
    return this.outFile;
  }
  
  public void setOutFile(final String outFile) {
    this.outFile = outFile;
  }
  
  @Pure
  public String getTransformation() {
    return this.transformation;
  }
  
  public void setTransformation(final String transformation) {
    this.transformation = transformation;
  }
}
