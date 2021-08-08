package ict.generate.modelToSMV;

import com.google.common.collect.Iterables;
import ict.generate.modelToSMV.CodeGeneratorUtil;
import ict.generate.modelToSMV.OLCCodeGenerator;
import ict.generate.modelToSMV.SoftwareCodeGenerator;
import ict.generate.mwe.ISlotWriteback;
import ict.generate.mwe.ResourceExecutor;
import ict.generate.util.FileAccess;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class SMVGenerator implements ResourceExecutor.IResourceExecutable {
  @Accessors
  private String outputPath = "";
  
  @Override
  public void run(final List<Resource> resources, final ISlotWriteback writeback) {
    for (final Resource resource : resources) {
      {
        String[] splited = resource.getURI().path().split("/");
        final String[] _converted_splited = (String[])splited;
        int _size = ((List<String>)Conversions.doWrapArray(_converted_splited)).size();
        int _minus = (_size - 1);
        final String name = splited[_minus];
        InputOutput.<String>println(name);
        String generated = "";
        Iterable<Model> _filter = Iterables.<Model>filter(resource.getContents(), Model.class);
        for (final Model m : _filter) {
          {
            int _length = generated.length();
            boolean _greaterThan = (_length > 0);
            if (_greaterThan) {
              String _generated = generated;
              generated = (_generated + "\n\n");
            }
            String _generated_1 = generated;
            String _compile = this.compile(m, name);
            generated = (_generated_1 + _compile);
          }
        }
        int _length = generated.length();
        boolean _greaterThan = (_length > 0);
        if (_greaterThan) {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append(this.outputPath);
          _builder.append("/");
          _builder.append(name);
          _builder.append(".smv");
          String resPath = _builder.toString();
          FileAccess.writeFile(resPath, generated);
        }
      }
    }
  }
  
  public String compile(final Model m, final String name) {
    String data = "";
    boolean _containsModules = CodeGeneratorUtil.containsModules(m);
    if (_containsModules) {
      String _data = data;
      CharSequence _compileActivityModel = SoftwareCodeGenerator.compileActivityModel(name, m);
      data = (_data + _compileActivityModel);
    }
    boolean _containsOLC = CodeGeneratorUtil.containsOLC(m);
    if (_containsOLC) {
      String _data_1 = data;
      CharSequence _compileSTMModel = OLCCodeGenerator.compileSTMModel(m);
      data = (_data_1 + _compileSTMModel);
    }
    return data;
  }
  
  public void recursivePrint(final EObject obj, final String prefix, final String seperator) {
    InputOutput.<String>print(prefix);
    InputOutput.<EObject>println(obj);
    if ((obj instanceof Activity)) {
      Activity a = ((Activity) obj);
      EList<ActivityNode> _nodes = a.getNodes();
      for (final EObject m : _nodes) {
        this.recursivePrint(m, (prefix + seperator), seperator);
      }
    }
    EList<EObject> _eContents = obj.eContents();
    for (final EObject m_1 : _eContents) {
      this.recursivePrint(m_1, (prefix + seperator), seperator);
    }
  }
  
  @Pure
  public String getOutputPath() {
    return this.outputPath;
  }
  
  public void setOutputPath(final String outputPath) {
    this.outputPath = outputPath;
  }
}
