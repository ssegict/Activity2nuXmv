package ict.generate.modelToSMV;

import com.google.common.collect.Iterables;
import ict.generate.modelToSMV.CodeGeneratorUtil;
import ict.generate.modelToSMV.TriggerableSignal;
import ict.generate.modelToSMV.TriggerableSignalHolder;
import ict.generate.mwe.ISlotWriteback;
import ict.generate.mwe.ResourceExecutor;
import ict.generate.util.FileAccess;
import ict.generate.util.UIDGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class MainCodeGenerator implements ResourceExecutor.IResourceExecutable {
  @Accessors
  private String basePath = "";
  
  @Override
  public void run(final List<Resource> resources, final ISlotWriteback writeback) {
    final Function1<Resource, EList<EObject>> _function = (Resource it) -> {
      return it.getContents();
    };
    final Function1<Model, EList<EObject>> _function_1 = (Model it) -> {
      return it.eContents();
    };
    Iterable<StateMachine> stateMachines = Iterables.<StateMachine>filter((Iterables.<EObject>concat(IterableExtensions.<Model, EList<EObject>>map(Iterables.<Model>filter((Iterables.<EObject>concat(ListExtensions.<Resource, EList<EObject>>map(resources, _function))), Model.class), _function_1))), StateMachine.class);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("#include \"../modules.h\"");
    _builder.newLine();
    _builder.append("#include \"../Property_state_machine.h\"");
    _builder.newLine();
    {
      final Function1<Resource, Boolean> _function_2 = (Resource it) -> {
        String _last = IterableExtensions.<String>last(((Iterable<String>)Conversions.doWrapArray(it.getURI().path().split("/"))));
        String _plus = ((this.basePath + "/") + _last);
        String _plus_1 = (_plus + ".smv");
        return Boolean.valueOf(new File(_plus_1).isFile());
      };
      Iterable<Resource> _filter = IterableExtensions.<Resource>filter(resources, _function_2);
      for(final Resource r : _filter) {
        _builder.append("#include \"");
        String _last = IterableExtensions.<String>last(((Iterable<String>)Conversions.doWrapArray(r.getURI().path().split("/"))));
        _builder.append(_last);
        _builder.append(".smv\"");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    {
      for(final String include : this.includes) {
        _builder.append("#include \"");
        _builder.append(include);
        _builder.append("\"");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.append("MODULE ObjectLifeCycle(container)");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("VAR");
    _builder.newLine();
    {
      for(final StateMachine stm : stateMachines) {
        _builder.append("\t\t");
        String _name = stm.getName();
        _builder.append(_name, "\t\t");
        _builder.append("obj : ");
        String _name_1 = stm.getName();
        _builder.append(_name_1, "\t\t");
        _builder.append("(container);");
        _builder.newLineIfNotEmpty();
      }
    }
    {
      final Function1<Resource, EList<EObject>> _function_3 = (Resource it) -> {
        return it.getContents();
      };
      final Function1<Model, EList<EObject>> _function_4 = (Model it) -> {
        return it.eContents();
      };
      final Function1<org.eclipse.uml2.uml.Class, EList<EObject>> _function_5 = (org.eclipse.uml2.uml.Class it) -> {
        return it.eContents();
      };
      final Function1<Property, Boolean> _function_6 = (Property it) -> {
        boolean _isReadOnly = it.isReadOnly();
        return Boolean.valueOf((!_isReadOnly));
      };
      Iterable<Property> _filter_1 = IterableExtensions.<Property>filter(Iterables.<Property>filter((Iterables.<EObject>concat(IterableExtensions.<org.eclipse.uml2.uml.Class, EList<EObject>>map(Iterables.<org.eclipse.uml2.uml.Class>filter((Iterables.<EObject>concat(IterableExtensions.<Model, EList<EObject>>map(Iterables.<Model>filter((Iterables.<EObject>concat(ListExtensions.<Resource, EList<EObject>>map(resources, _function_3))), Model.class), _function_4))), org.eclipse.uml2.uml.Class.class), _function_5))), Property.class), _function_6);
      for(final Property attr : _filter_1) {
        _builder.append("\t\t");
        String _name_2 = attr.getName();
        _builder.append(_name_2, "\t\t");
        Long _uID = UIDGenerator.getUID(attr);
        _builder.append(_uID, "\t\t");
        _builder.append(": ");
        int _integerValue = attr.getLowerValue().integerValue();
        _builder.append(_integerValue, "\t\t");
        _builder.append("..");
        int _integerValue_1 = attr.getUpperValue().integerValue();
        _builder.append(_integerValue_1, "\t\t");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("FROZENVAR");
    _builder.newLine();
    {
      final Function1<Resource, EList<EObject>> _function_7 = (Resource it) -> {
        return it.getContents();
      };
      final Function1<Model, EList<EObject>> _function_8 = (Model it) -> {
        return it.eContents();
      };
      final Function1<org.eclipse.uml2.uml.Class, EList<EObject>> _function_9 = (org.eclipse.uml2.uml.Class it) -> {
        return it.eContents();
      };
      final Function1<Property, Boolean> _function_10 = (Property it) -> {
        return Boolean.valueOf(it.isReadOnly());
      };
      Iterable<Property> _filter_2 = IterableExtensions.<Property>filter(Iterables.<Property>filter((Iterables.<EObject>concat(IterableExtensions.<org.eclipse.uml2.uml.Class, EList<EObject>>map(Iterables.<org.eclipse.uml2.uml.Class>filter((Iterables.<EObject>concat(IterableExtensions.<Model, EList<EObject>>map(Iterables.<Model>filter((Iterables.<EObject>concat(ListExtensions.<Resource, EList<EObject>>map(resources, _function_7))), Model.class), _function_8))), org.eclipse.uml2.uml.Class.class), _function_9))), Property.class), _function_10);
      for(final Property attr_1 : _filter_2) {
        _builder.append("\t\t");
        String _name_3 = attr_1.getName();
        _builder.append(_name_3, "\t\t");
        Long _uID_1 = UIDGenerator.getUID(attr_1);
        _builder.append(_uID_1, "\t\t");
        _builder.append(": ");
        int _integerValue_2 = attr_1.getLowerValue().integerValue();
        _builder.append(_integerValue_2, "\t\t");
        _builder.append("..");
        int _integerValue_3 = attr_1.getUpperValue().integerValue();
        _builder.append(_integerValue_3, "\t\t");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("DEFINE");
    _builder.newLine();
    {
      boolean _isEmpty = IterableExtensions.isEmpty(stateMachines);
      if (_isEmpty) {
        _builder.append("\t\t");
        _builder.append("ERROR := FALSE;");
        _builder.newLine();
      } else {
        _builder.append("\t\t");
        _builder.append("ERROR := ");
        final Function1<StateMachine, String> _function_11 = (StateMachine it) -> {
          String _name_4 = it.getName();
          return (_name_4 + "obj.ERROR");
        };
        String _join = IterableExtensions.join(IterableExtensions.<StateMachine, String>map(stateMachines, _function_11), " | ");
        _builder.append(_join, "\t\t");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.append("MODULE main");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("VAR");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("reset : boolean;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("forward : ControlFlow();");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("back : ControlFlow();");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("activator : MainActivator(forward);");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("mainAct : MainActivity(forward, back, self, reset);");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("terminator : MainTerminator(back);");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("olc : ObjectLifeCycle(self);");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("\t\t  \t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ASSIGN");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("reset := FALSE;");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("DEFINE");
    _builder.newLine();
    {
      ArrayList<TriggerableSignal> _mainSignals = TriggerableSignalHolder.getSingleton().getMainSignals(CodeGeneratorUtil.getMainActivity(resources));
      for(final TriggerableSignal s : _mainSignals) {
        _builder.append("\t\t");
        String _name_4 = s.getName();
        _builder.append(_name_4, "\t\t");
        _builder.append(" := ");
        String _expression = s.getExpression();
        _builder.append(_expression, "\t\t");
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    FileAccess.writeFile((this.basePath + "/main.smv"), _builder.toString());
  }
  
  private ArrayList<String> includes = new ArrayList<String>();
  
  public boolean addInclude(final String include) {
    boolean _xifexpression = false;
    boolean _contains = this.includes.contains(include);
    boolean _not = (!_contains);
    if (_not) {
      _xifexpression = this.includes.add(include);
    }
    return _xifexpression;
  }
  
  @Pure
  public String getBasePath() {
    return this.basePath;
  }
  
  public void setBasePath(final String basePath) {
    this.basePath = basePath;
  }
}
