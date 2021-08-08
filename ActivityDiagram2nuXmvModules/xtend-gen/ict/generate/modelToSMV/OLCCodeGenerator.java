package ict.generate.modelToSMV;

import com.google.common.collect.Iterables;
import ict.generate.modelToSMV.CodeGeneratorUtil;
import ict.generate.modelToSMV.TriggerableSignalHolder;
import ict.generate.util.ThrowHelper;
import ict.generate.util.UIDGenerator;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Vertex;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class OLCCodeGenerator {
  public static CharSequence compileSTMModel(final Model model) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("-- STM Compilation of ");
    String _name = model.getName();
    _builder.append(_name);
    _builder.newLineIfNotEmpty();
    {
      Iterable<StateMachine> _filter = Iterables.<StateMachine>filter(model.eContents(), StateMachine.class);
      for(final StateMachine stm : _filter) {
        String _compile = OLCCodeGenerator.compile(stm);
        _builder.append(_compile);
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }
  
  public static String compile(final StateMachine stm) {
    final Function1<Region, EList<Transition>> _function = (Region it) -> {
      return it.getTransitions();
    };
    final Function1<Transition, EList<Trigger>> _function_1 = (Transition it) -> {
      return it.getTriggers();
    };
    final Function1<Trigger, Event> _function_2 = (Trigger it) -> {
      return it.getEvent();
    };
    final Consumer<SignalEvent> _function_3 = (SignalEvent it) -> {
      TriggerableSignalHolder.getSingleton().addSignal(it.getSignal());
    };
    Iterables.<SignalEvent>filter(IterableExtensions.<Trigger, Event>map(Iterables.<Trigger>concat(IterableExtensions.<Transition, EList<Trigger>>map(Iterables.<Transition>concat(ListExtensions.<Region, EList<Transition>>map(stm.getRegions(), _function)), _function_1)), _function_2), SignalEvent.class).forEach(_function_3);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("MODULE ");
    String _name = stm.getName();
    _builder.append(_name);
    _builder.append("(container)");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("VAR");
    _builder.newLine();
    _builder.append("\t\t");
    CharSequence _generateVarSection = OLCCodeGenerator.generateVarSection(stm);
    _builder.append(_generateVarSection, "\t\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("DEFINE");
    _builder.newLine();
    _builder.append("\t\t");
    CharSequence _generateDefineSection = OLCCodeGenerator.generateDefineSection(stm);
    _builder.append(_generateDefineSection, "\t\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ASSIGN");
    _builder.newLine();
    _builder.append("\t\t");
    CharSequence _generateASSIGNSection = OLCCodeGenerator.generateASSIGNSection(stm);
    _builder.append(_generateASSIGNSection, "\t\t");
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }
  
  public static CharSequence generateVarSection(final StateMachine stm) {
    StringConcatenation _builder = new StringConcatenation();
    {
      Iterable<Region> _filter = Iterables.<Region>filter(stm.eContents(), Region.class);
      for(final Region r : _filter) {
        String _name = r.getName();
        _builder.append(_name);
        _builder.append(" : {");
        String _states = OLCCodeGenerator.states(r);
        _builder.append(_states);
        _builder.append("};");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }
  
  public static String states(final Region r) {
    Iterable<Vertex> vertex = Iterables.<Vertex>filter(r.eContents(), Vertex.class);
    final Function1<Vertex, Boolean> _function = (Vertex it) -> {
      return Boolean.valueOf((!((it instanceof State) || (it instanceof Pseudostate))));
    };
    Iterable<Vertex> notState = IterableExtensions.<Vertex>filter(vertex, _function);
    boolean _isEmpty = IterableExtensions.isEmpty(notState);
    boolean _not = (!_isEmpty);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Unknown vertex Type(s) should be State or Pseudostate");
      _builder.newLine();
      {
        for(final Vertex vert : notState) {
          String _name = vert.getName();
          _builder.append(_name);
          _builder.append(": ");
          String _canonicalName = vert.getClass().getCanonicalName();
          _builder.append(_canonicalName);
          _builder.newLineIfNotEmpty();
        }
      }
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(_builder.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.WARN, _illegalArgumentException);
    }
    final Function1<Vertex, Boolean> _function_1 = (Vertex it) -> {
      return Boolean.valueOf(((it instanceof State) || (it instanceof Pseudostate)));
    };
    final Function1<Vertex, String> _function_2 = (Vertex it) -> {
      return it.getName();
    };
    String _join = IterableExtensions.join(IterableExtensions.<Vertex, String>map(IterableExtensions.<Vertex>filter(vertex, _function_1), _function_2), ", ");
    String _xifexpression = null;
    boolean _needsErrorSignal = OLCCodeGenerator.needsErrorSignal(r);
    if (_needsErrorSignal) {
      _xifexpression = ", error";
    } else {
      _xifexpression = "";
    }
    return (_join + _xifexpression);
  }
  
  public static CharSequence generateDefineSection(final StateMachine stm) {
    StringConcatenation _builder = new StringConcatenation();
    {
      final Function1<Region, Boolean> _function = (Region it) -> {
        return Boolean.valueOf(OLCCodeGenerator.needsErrorSignal(it));
      };
      Iterable<Region> _filter = IterableExtensions.<Region>filter(stm.getRegions(), _function);
      for(final Region r : _filter) {
        _builder.append("no_signal_");
        String _name = r.getName();
        _builder.append(_name);
        _builder.append(" := !(");
        final Function1<Transition, EList<Trigger>> _function_1 = (Transition it) -> {
          return it.getTriggers();
        };
        final Function1<Trigger, Event> _function_2 = (Trigger it) -> {
          return it.getEvent();
        };
        final Function1<SignalEvent, String> _function_3 = (SignalEvent it) -> {
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("container.trig");
          Long _uID = UIDGenerator.getUID(it.getSignal());
          _builder_1.append(_uID);
          return _builder_1.toString();
        };
        String _join = IterableExtensions.join(CodeGeneratorUtil.<String>distinct(IterableExtensions.<SignalEvent, String>map(Iterables.<SignalEvent>filter(IterableExtensions.<Trigger, Event>map(Iterables.<Trigger>concat(ListExtensions.<Transition, EList<Trigger>>map(r.getTransitions(), _function_1)), _function_2), SignalEvent.class), _function_3)), " | ");
        _builder.append(_join);
        _builder.append(");");
        _builder.newLineIfNotEmpty();
      }
    }
    {
      final Function1<Region, Boolean> _function_4 = (Region it) -> {
        return Boolean.valueOf(OLCCodeGenerator.needsErrorSignal(it));
      };
      boolean _isEmpty = IterableExtensions.isEmpty(IterableExtensions.<Region>filter(stm.getRegions(), _function_4));
      if (_isEmpty) {
        _builder.append("ERROR := FALSE;");
        _builder.newLine();
      } else {
        _builder.append("ERROR := ");
        final Function1<Region, Boolean> _function_5 = (Region it) -> {
          return Boolean.valueOf(OLCCodeGenerator.needsErrorSignal(it));
        };
        final Function1<Region, String> _function_6 = (Region it) -> {
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("(");
          String _name_1 = it.getName();
          _builder_1.append(_name_1);
          _builder_1.append(" = error)");
          return _builder_1.toString();
        };
        String _join_1 = IterableExtensions.join(IterableExtensions.<Region, String>map(IterableExtensions.<Region>filter(stm.getRegions(), _function_5), _function_6), " | ");
        _builder.append(_join_1);
        _builder.append(";");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }
  
  public static boolean needsErrorSignal(final Region r) {
    return r.getName().endsWith("_error");
  }
  
  public static CharSequence generateASSIGNSection(final StateMachine stm) {
    StringConcatenation _builder = new StringConcatenation();
    {
      Iterable<Region> _filter = Iterables.<Region>filter(stm.eContents(), Region.class);
      for(final Region region : _filter) {
        CharSequence _generateImplementation = OLCCodeGenerator.generateImplementation(region);
        _builder.append(_generateImplementation);
        _builder.newLineIfNotEmpty();
        _builder.newLine();
      }
    }
    return _builder;
  }
  
  public static CharSequence generateImplementation(final Region r) {
    CharSequence _xblockexpression = null;
    {
      final Function1<Pseudostate, Boolean> _function = (Pseudostate it) -> {
        return Boolean.valueOf(it.getIncomings().isEmpty());
      };
      Iterable<Pseudostate> startStates = IterableExtensions.<Pseudostate>filter(Iterables.<Pseudostate>filter(r.eContents(), Pseudostate.class), _function);
      int _size = IterableExtensions.size(startStates);
      boolean _greaterThan = (_size > 1);
      if (_greaterThan) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Multiple possible start nodes found at ");
        String _name = r.getName();
        _builder.append(_name);
        _builder.newLineIfNotEmpty();
        {
          for(final Pseudostate start : startStates) {
            String _name_1 = start.getName();
            _builder.append(_name_1);
            _builder.append(": ");
            String _canonicalName = start.getClass().getCanonicalName();
            _builder.append(_canonicalName);
            _builder.newLineIfNotEmpty();
          }
        }
        IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(_builder.toString());
        ThrowHelper.customThrow(ThrowHelper.ThrowType.WARN, _illegalArgumentException);
      }
      int _size_1 = IterableExtensions.size(startStates);
      boolean _lessThan = (_size_1 < 1);
      if (_lessThan) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("No start Nodes found at ");
        String _name_2 = r.getName();
        _builder_1.append(_name_2);
        _builder_1.newLineIfNotEmpty();
        _builder_1.append("A start Node must not contain any incoming Transitions");
        _builder_1.newLine();
        IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException(_builder_1.toString());
        ThrowHelper.customThrow(ThrowHelper.ThrowType.WARN, _illegalArgumentException_1);
      }
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("init(");
      String _name_3 = r.getName();
      _builder_2.append(_name_3);
      _builder_2.append(") := ");
      final Function1<Pseudostate, Boolean> _function_1 = (Pseudostate it) -> {
        return Boolean.valueOf(it.getIncomings().isEmpty());
      };
      String _name_4 = (((Pseudostate[])Conversions.unwrapArray(IterableExtensions.<Pseudostate>filter(Iterables.<Pseudostate>filter(r.eContents(), Pseudostate.class), _function_1), Pseudostate.class))[0]).getName();
      _builder_2.append(_name_4);
      _builder_2.append(";");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("next(");
      String _name_5 = r.getName();
      _builder_2.append(_name_5);
      _builder_2.append(") :=");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t");
      _builder_2.append("case");
      _builder_2.newLine();
      {
        Iterable<Transition> _filter = Iterables.<Transition>filter(r.eContents(), Transition.class);
        for(final Transition trans : _filter) {
          _builder_2.append("\t\t");
          _builder_2.append("(");
          String _name_6 = r.getName();
          _builder_2.append(_name_6, "\t\t");
          _builder_2.append(" = ");
          String _name_7 = trans.getSource().getName();
          _builder_2.append(_name_7, "\t\t");
          _builder_2.append(") ");
          String _constraints = OLCCodeGenerator.getConstraints(trans);
          _builder_2.append(_constraints, "\t\t");
          _builder_2.append(": ");
          String _name_8 = trans.getTarget().getName();
          _builder_2.append(_name_8, "\t\t");
          _builder_2.append(";");
          _builder_2.newLineIfNotEmpty();
        }
      }
      {
        boolean _needsErrorSignal = OLCCodeGenerator.needsErrorSignal(r);
        if (_needsErrorSignal) {
          _builder_2.append("\t\t");
          _builder_2.append("no_signal_");
          String _name_9 = r.getName();
          _builder_2.append(_name_9, "\t\t");
          _builder_2.append(" : ");
          String _name_10 = r.getName();
          _builder_2.append(_name_10, "\t\t");
          _builder_2.append(";");
          _builder_2.newLineIfNotEmpty();
          _builder_2.append("\t\t");
          _builder_2.append("TRUE : error;");
          _builder_2.newLine();
        } else {
          _builder_2.append("\t\t");
          _builder_2.append("TRUE : ");
          String _name_11 = r.getName();
          _builder_2.append(_name_11, "\t\t");
          _builder_2.append(";");
          _builder_2.newLineIfNotEmpty();
        }
      }
      _builder_2.append("\t");
      _builder_2.append("esac;");
      _builder_2.newLine();
      _xblockexpression = _builder_2;
    }
    return _xblockexpression;
  }
  
  public static String getConstraints(final Transition t) {
    String triggers = "";
    EList<Trigger> _triggers = t.getTriggers();
    for (final Trigger trig : _triggers) {
      {
        Event event = trig.getEvent();
        if ((event instanceof SignalEvent)) {
          int _length = triggers.length();
          boolean _greaterThan = (_length > 0);
          if (_greaterThan) {
            String _triggers_1 = triggers;
            triggers = (_triggers_1 + " | ");
          }
          String _triggers_2 = triggers;
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("container.trig");
          Long _uID = UIDGenerator.getUID(((SignalEvent)event).getSignal());
          _builder.append(_uID);
          triggers = (_triggers_2 + _builder);
        }
      }
    }
    String guard = "";
    Constraint _guard = t.getGuard();
    boolean _tripleNotEquals = (_guard != null);
    if (_tripleNotEquals) {
      guard = CodeGeneratorUtil.buildStringForConstraint(t.getGuard());
    }
    String constraints = triggers;
    if (((triggers.length() > 0) && (guard.length() > 0))) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("(");
      _builder.append(constraints);
      _builder.append(") &");
      constraints = _builder.toString();
    }
    String _constraints = constraints;
    constraints = (_constraints + guard);
    int _length = constraints.length();
    boolean _greaterThan = (_length > 0);
    if (_greaterThan) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("& (");
      _builder_1.append(constraints);
      _builder_1.append(")");
      return _builder_1.toString();
    }
    return "";
  }
}
