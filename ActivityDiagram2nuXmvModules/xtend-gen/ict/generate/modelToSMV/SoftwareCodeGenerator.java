package ict.generate.modelToSMV;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import ict.generate.modelToSMV.CodeGeneratorUtil;
import ict.generate.modelToSMV.TriggerableSignal;
import ict.generate.modelToSMV.TriggerableSignalHolder;
import ict.generate.util.Pair;
import ict.generate.util.ThrowHelper;
import ict.generate.util.UIDGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityFinalNode;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityPartition;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.ControlFlow;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.ExceptionHandler;
import org.eclipse.uml2.uml.ExecutableNode;
import org.eclipse.uml2.uml.FlowFinalNode;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.JoinNode;
import org.eclipse.uml2.uml.MergeNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.OpaqueAction;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.TimeEvent;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class SoftwareCodeGenerator {
  public static CharSequence compileActivityModel(final String fileName, final Model m) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("-- Module Compilation of ");
    String _name = m.getName();
    _builder.append(_name);
    _builder.newLineIfNotEmpty();
    {
      Iterable<Activity> _filter = Iterables.<Activity>filter(m.eContents(), Activity.class);
      for(final Activity a : _filter) {
        CharSequence _compileActivity = SoftwareCodeGenerator.compileActivity(a);
        _builder.append(_compileActivity);
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }
  
  public static CharSequence compileActivity(final Activity a) {
    CharSequence _xblockexpression = null;
    {
      HashMap<ActivityPartition, List<String>> regionMap = new HashMap<ActivityPartition, List<String>>();
      int _size = IterableExtensions.size(Iterables.<InitialNode>filter(a.getNodes(), InitialNode.class));
      boolean _greaterThan = (_size > 1);
      if (_greaterThan) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Currently there is only one InitialNode per Activity supported");
        _builder.newLine();
        int _size_1 = IterableExtensions.size(Iterables.<InitialNode>filter(a.getNodes(), InitialNode.class));
        _builder.append(_size_1);
        _builder.append(" found at \"");
        String _name = a.getName();
        _builder.append(_name);
        _builder.append("\"");
        _builder.newLineIfNotEmpty();
        _builder.append("Please use Fork Nodes");
        _builder.newLine();
        IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(_builder.toString());
        ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _illegalArgumentException);
      }
      boolean _isEmpty = IterableExtensions.isEmpty(Iterables.<ActivityFinalNode>filter(a.getNodes(), ActivityFinalNode.class));
      if (_isEmpty) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("The activity \"");
        String _name_1 = a.getName();
        _builder_1.append(_name_1);
        _builder_1.append("\" does not contain any AcitivityFinalNodes");
        _builder_1.newLineIfNotEmpty();
        IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException(_builder_1.toString());
        ThrowHelper.customThrow(ThrowHelper.ThrowType.WARN, _illegalArgumentException_1);
      }
      int _size_2 = IterableExtensions.size(Iterables.<ActivityFinalNode>filter(a.getNodes(), ActivityFinalNode.class));
      boolean _greaterThan_1 = (_size_2 > 1);
      if (_greaterThan_1) {
        StringConcatenation _builder_2 = new StringConcatenation();
        _builder_2.append("Currently there is only one ActivityFinalNode per Activity supported");
        _builder_2.newLine();
        int _size_3 = IterableExtensions.size(Iterables.<ActivityFinalNode>filter(a.getNodes(), ActivityFinalNode.class));
        _builder_2.append(_size_3);
        _builder_2.append(" found at \"");
        String _name_2 = a.getName();
        _builder_2.append(_name_2);
        _builder_2.append("\"");
        _builder_2.newLineIfNotEmpty();
        _builder_2.append("Please use Merge Nodes");
        _builder_2.newLine();
        IllegalArgumentException _illegalArgumentException_2 = new IllegalArgumentException(_builder_2.toString());
        ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _illegalArgumentException_2);
      }
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("MODULE ");
      String _name_3 = a.getName();
      _builder_3.append(_name_3);
      _builder_3.append("(controlFlowIn, controlFlowOut, container, resetExt)");
      _builder_3.newLineIfNotEmpty();
      _builder_3.append("\t");
      _builder_3.append("VAR");
      _builder_3.newLine();
      {
        Iterable<ActivityPartition> _filter = Iterables.<ActivityPartition>filter(a.getGroups(), ActivityPartition.class);
        for(final ActivityPartition partition : _filter) {
          _builder_3.append("\t\t");
          String _name_4 = partition.getName();
          _builder_3.append(_name_4, "\t\t");
          _builder_3.append("Decision: 0..");
          final Function1<ExecutableNode, EList<ExceptionHandler>> _function = (ExecutableNode it) -> {
            return it.getHandlers();
          };
          int _size_4 = IterableExtensions.size(Iterables.<ExceptionHandler>concat(IterableExtensions.<ExecutableNode, EList<ExceptionHandler>>map(Iterables.<ExecutableNode>filter(partition.getNodes(), ExecutableNode.class), _function)));
          _builder_3.append(_size_4, "\t\t");
          _builder_3.append(";");
          _builder_3.newLineIfNotEmpty();
        }
      }
      {
        Iterable<ControlFlow> _filter_1 = Iterables.<ControlFlow>filter(a.eContents(), ControlFlow.class);
        for(final ControlFlow contFlow : _filter_1) {
          _builder_3.append("\t\t");
          String _name_5 = contFlow.getSource().getName();
          _builder_3.append(_name_5, "\t\t");
          _builder_3.append("to");
          String _name_6 = contFlow.getTarget().getName();
          _builder_3.append(_name_6, "\t\t");
          _builder_3.append(": ControlFlow();");
          _builder_3.newLineIfNotEmpty();
        }
      }
      {
        Iterable<ExecutableNode> _filter_2 = Iterables.<ExecutableNode>filter(a.getNodes(), ExecutableNode.class);
        for(final ExecutableNode node : _filter_2) {
          {
            EList<ExceptionHandler> _handlers = node.getHandlers();
            for(final ExceptionHandler handler : _handlers) {
              _builder_3.append("\t\t");
              String _name_7 = node.getName();
              _builder_3.append(_name_7, "\t\t");
              _builder_3.append("to");
              String _name_8 = node.getName();
              _builder_3.append(_name_8, "\t\t");
              _builder_3.append("Intercepter");
              Long _uID = UIDGenerator.getUID(handler);
              _builder_3.append(_uID, "\t\t");
              _builder_3.append(": ControlFlow();");
              _builder_3.newLineIfNotEmpty();
              _builder_3.append("\t\t");
              String _name_9 = node.getName();
              _builder_3.append(_name_9, "\t\t");
              _builder_3.append("Intercepter");
              Long _uID_1 = UIDGenerator.getUID(handler);
              _builder_3.append(_uID_1, "\t\t");
              _builder_3.append("to");
              String _name_10 = handler.getHandlerBody().getName();
              _builder_3.append(_name_10, "\t\t");
              _builder_3.append(": ControlFlow();");
              _builder_3.newLineIfNotEmpty();
            }
          }
        }
      }
      _builder_3.append("\t\t");
      _builder_3.newLine();
      {
        ArrayList<ActivityNode> _distinct = CodeGeneratorUtil.<ActivityNode>distinct(a.getNodes());
        for(final ActivityNode an : _distinct) {
          _builder_3.append("\t\t");
          CharSequence _moduleInitialisation = SoftwareCodeGenerator.moduleInitialisation(an);
          _builder_3.append(_moduleInitialisation, "\t\t");
          _builder_3.newLineIfNotEmpty();
        }
      }
      {
        Iterable<ExecutableNode> _filter_3 = Iterables.<ExecutableNode>filter(a.getNodes(), ExecutableNode.class);
        for(final ExecutableNode node_1 : _filter_3) {
          {
            EList<ExceptionHandler> _handlers_1 = node_1.getHandlers();
            for(final ExceptionHandler handler_1 : _handlers_1) {
              _builder_3.append("\t\t");
              String _generateFlowInterceptor = SoftwareCodeGenerator.generateFlowInterceptor(node_1, handler_1, regionMap);
              _builder_3.append(_generateFlowInterceptor, "\t\t");
              _builder_3.newLineIfNotEmpty();
            }
          }
        }
      }
      {
        final Function1<AcceptEventAction, EList<Trigger>> _function_1 = (AcceptEventAction it) -> {
          return it.getTriggers();
        };
        final Function1<Trigger, Boolean> _function_2 = (Trigger it) -> {
          Event _event = it.getEvent();
          return Boolean.valueOf((_event instanceof TimeEvent));
        };
        Iterable<Trigger> _filter_4 = IterableExtensions.<Trigger>filter(Iterables.<Trigger>concat(IterableExtensions.<AcceptEventAction, EList<Trigger>>map(Iterables.<AcceptEventAction>filter(a.getNodes(), AcceptEventAction.class), _function_1)), _function_2);
        for(final Trigger trig : _filter_4) {
          _builder_3.append("\t\t");
          String _generateTimeTrigger = SoftwareCodeGenerator.generateTimeTrigger(trig);
          _builder_3.append(_generateTimeTrigger, "\t\t");
          _builder_3.newLineIfNotEmpty();
        }
      }
      _builder_3.append("\t");
      _builder_3.newLine();
      {
        Set<Map.Entry<ActivityPartition, List<String>>> _entrySet = regionMap.entrySet();
        for(final Map.Entry<ActivityPartition, List<String>> entry : _entrySet) {
          _builder_3.append("\t");
          _builder_3.append("INVAR (");
          String _name_11 = entry.getKey().getName();
          _builder_3.append(_name_11, "\t");
          _builder_3.append("Decision != 0 | !");
          String _name_12 = entry.getKey().getName();
          _builder_3.append(_name_12, "\t");
          _builder_3.append("ExceptionFlow) & (");
          String _name_13 = entry.getKey().getName();
          _builder_3.append(_name_13, "\t");
          _builder_3.append("Decision = 0 | ");
          String _name_14 = entry.getKey().getName();
          _builder_3.append(_name_14, "\t");
          _builder_3.append("ExceptionFlow)");
          _builder_3.newLineIfNotEmpty();
          {
            List<String> _value = entry.getValue();
            for(final String inter : _value) {
              _builder_3.append("\t");
              _builder_3.append("INVAR (");
              String _name_15 = entry.getKey().getName();
              _builder_3.append(_name_15, "\t");
              _builder_3.append("Decision != ");
              int _indexOf = entry.getValue().indexOf(inter);
              int _plus = (_indexOf + 1);
              _builder_3.append(_plus, "\t");
              _builder_3.append(" | ");
              _builder_3.append(inter, "\t");
              _builder_3.append(".WAITING);");
              _builder_3.newLineIfNotEmpty();
            }
          }
        }
      }
      _builder_3.append("\t");
      _builder_3.newLine();
      _builder_3.append("\t");
      _builder_3.append("ASSIGN");
      _builder_3.newLine();
      _builder_3.append("\t\t");
      _builder_3.append("controlFlowIn.backwardSignal := controlFlowIn.forwardSignal & ");
      final Function1<ActivityNode, Boolean> _function_3 = (ActivityNode it) -> {
        return Boolean.valueOf((it instanceof InitialNode));
      };
      String _name_16 = IterableExtensions.<ActivityNode>findFirst(a.getNodes(), _function_3).getName();
      _builder_3.append(_name_16, "\t\t");
      _builder_3.append(".ACTIVE;");
      _builder_3.newLineIfNotEmpty();
      {
        final Function1<ActivityNode, Boolean> _function_4 = (ActivityNode it) -> {
          return Boolean.valueOf((it instanceof ActivityFinalNode));
        };
        ActivityNode _findFirst = IterableExtensions.<ActivityNode>findFirst(a.getNodes(), _function_4);
        boolean _tripleNotEquals = (_findFirst != null);
        if (_tripleNotEquals) {
          _builder_3.append("\t\t");
          _builder_3.append("controlFlowOut.forwardSignal := ");
          final Function1<ActivityNode, Boolean> _function_5 = (ActivityNode it) -> {
            return Boolean.valueOf((it instanceof ActivityFinalNode));
          };
          String _name_17 = IterableExtensions.<ActivityNode>findFirst(a.getNodes(), _function_5).getName();
          _builder_3.append(_name_17, "\t\t");
          _builder_3.append(".FINISHED;");
          _builder_3.newLineIfNotEmpty();
        } else {
          _builder_3.append("\t\t");
          _builder_3.append("controlFlowOut.forwardSignal := FALSE;");
          _builder_3.newLine();
        }
      }
      _builder_3.newLine();
      _builder_3.append("\t");
      _builder_3.append("DEFINE");
      _builder_3.newLine();
      {
        List<TriggerableSignal> _signals = TriggerableSignalHolder.getSingleton().getSignals(a);
        for(final TriggerableSignal s : _signals) {
          _builder_3.append("\t\t");
          String _name_18 = s.getName();
          _builder_3.append(_name_18, "\t\t");
          _builder_3.append(" := ");
          String _expression = s.getExpression();
          _builder_3.append(_expression, "\t\t");
          _builder_3.append(";");
          _builder_3.newLineIfNotEmpty();
        }
      }
      {
        Iterable<ActivityPartition> _filter_5 = Iterables.<ActivityPartition>filter(a.getGroups(), ActivityPartition.class);
        for(final ActivityPartition partition_1 : _filter_5) {
          _builder_3.append("\t\t");
          String _name_19 = partition_1.getName();
          _builder_3.append(_name_19, "\t\t");
          _builder_3.append("Reset := reset | ");
          final Function1<ExecutableNode, Boolean> _function_6 = (ExecutableNode it) -> {
            boolean _isEmpty_1 = it.getHandlers().isEmpty();
            return Boolean.valueOf((!_isEmpty_1));
          };
          final Function1<ExecutableNode, String> _function_7 = (ExecutableNode it) -> {
            String _name_20 = it.getName();
            return (_name_20 + ".RESET_OUT");
          };
          String _join = IterableExtensions.join(IterableExtensions.<ExecutableNode, String>map(IterableExtensions.<ExecutableNode>filter(Iterables.<ExecutableNode>filter(partition_1.getNodes(), ExecutableNode.class), _function_6), _function_7), " | ");
          _builder_3.append(_join, "\t\t");
          _builder_3.append(";");
          _builder_3.newLineIfNotEmpty();
          _builder_3.append("\t\t");
          String _name_20 = partition_1.getName();
          _builder_3.append(_name_20, "\t\t");
          _builder_3.append("IntercepterReset := reset | ");
          final Function1<ExecutableNode, EList<ExceptionHandler>> _function_8 = (ExecutableNode it) -> {
            return it.getHandlers();
          };
          final Function1<ExceptionHandler, String> _function_9 = (ExceptionHandler it) -> {
            EObject _eContainer = it.eContainer();
            String _name_21 = ((NamedElement) _eContainer).getName();
            String _plus_1 = (_name_21 + "Intercepter");
            Long _uID_2 = UIDGenerator.getUID(it);
            String _plus_2 = (_plus_1 + _uID_2);
            return (_plus_2 + ".RESET_OUT");
          };
          String _join_1 = IterableExtensions.join(IterableExtensions.<ExceptionHandler, String>map(Iterables.<ExceptionHandler>concat(IterableExtensions.<ExecutableNode, EList<ExceptionHandler>>map(Iterables.<ExecutableNode>filter(partition_1.getNodes(), ExecutableNode.class), _function_8)), _function_9), " | ");
          _builder_3.append(_join_1, "\t\t");
          _builder_3.append(";");
          _builder_3.newLineIfNotEmpty();
          _builder_3.append("\t\t");
          String _name_21 = partition_1.getName();
          _builder_3.append(_name_21, "\t\t");
          _builder_3.append("ExceptionFlowContinue := ");
          final Function1<ExecutableNode, EList<ExceptionHandler>> _function_10 = (ExecutableNode it) -> {
            return it.getHandlers();
          };
          final Function1<ExceptionHandler, String> _function_11 = (ExceptionHandler it) -> {
            EObject _eContainer = it.eContainer();
            String _name_22 = ((NamedElement) _eContainer).getName();
            String _plus_1 = (_name_22 + "Intercepter");
            Long _uID_2 = UIDGenerator.getUID(it);
            String _plus_2 = (_plus_1 + _uID_2);
            return (_plus_2 + ".ALLOWED");
          };
          String _join_2 = IterableExtensions.join(IterableExtensions.<ExceptionHandler, String>map(Iterables.<ExceptionHandler>concat(IterableExtensions.<ExecutableNode, EList<ExceptionHandler>>map(Iterables.<ExecutableNode>filter(partition_1.getNodes(), ExecutableNode.class), _function_10)), _function_11), " | ");
          _builder_3.append(_join_2, "\t\t");
          _builder_3.append(";");
          _builder_3.newLineIfNotEmpty();
          _builder_3.append("\t\t");
          String _name_22 = partition_1.getName();
          _builder_3.append(_name_22, "\t\t");
          _builder_3.append("ExceptionFlow := !");
          String _name_23 = partition_1.getName();
          _builder_3.append(_name_23, "\t\t");
          _builder_3.append("ExceptionFlowContinue &( ");
          final Function1<ExecutableNode, EList<ExceptionHandler>> _function_12 = (ExecutableNode it) -> {
            return it.getHandlers();
          };
          final Function1<ExceptionHandler, String> _function_13 = (ExceptionHandler it) -> {
            EObject _eContainer = it.eContainer();
            String _name_24 = ((NamedElement) _eContainer).getName();
            String _plus_1 = (_name_24 + 
              "Intercepter");
            Long _uID_2 = UIDGenerator.getUID(it);
            String _plus_2 = (_plus_1 + _uID_2);
            return (_plus_2 + ".WAITING");
          };
          String _join_3 = IterableExtensions.join(IterableExtensions.<ExceptionHandler, String>map(Iterables.<ExceptionHandler>concat(IterableExtensions.<ExecutableNode, EList<ExceptionHandler>>map(Iterables.<ExecutableNode>filter(partition_1.getNodes(), ExecutableNode.class), _function_12)), _function_13), " | ");
          _builder_3.append(_join_3, "\t\t");
          _builder_3.append(");");
          _builder_3.newLineIfNotEmpty();
          _builder_3.append("\t\t");
          String _name_24 = partition_1.getName();
          _builder_3.append(_name_24, "\t\t");
          _builder_3.append("Active := ");
          final Function1<ActivityNode, Boolean> _function_14 = (ActivityNode it) -> {
            return Boolean.valueOf((!((it instanceof ExecutableNode) && (!((ExecutableNode) it).getHandlers().isEmpty()))));
          };
          String _join_4 = IterableExtensions.join(SoftwareCodeGenerator.toActive(IterableExtensions.<ActivityNode>filter(partition_1.getNodes(), _function_14)), " | ");
          _builder_3.append(_join_4, "\t\t");
          _builder_3.append(";");
          _builder_3.newLineIfNotEmpty();
        }
      }
      _builder_3.append("\t\t");
      _builder_3.append("SUB_ACTIVE := ");
      String _join_5 = IterableExtensions.join(SoftwareCodeGenerator.allActiveElmsNames(a), " | ");
      _builder_3.append(_join_5, "\t\t");
      _builder_3.append(";");
      _builder_3.newLineIfNotEmpty();
      _builder_3.append("\t\t");
      _builder_3.append("reset := resetExt | controlFlowOut.backwardSignal;");
      _builder_3.newLine();
      _builder_3.newLine();
      _xblockexpression = _builder_3;
    }
    return _xblockexpression;
  }
  
  protected static CharSequence _moduleInitialisation(final InitialNode an) {
    CharSequence _xblockexpression = null;
    {
      SoftwareCodeGenerator.checkInOut(an, 0, 1);
      StringConcatenation _builder = new StringConcatenation();
      String _name = an.getName();
      _builder.append(_name);
      _builder.append(": InitialPoint(controlFlowIn.forwardSignal, ");
      String _outputSignal = SoftwareCodeGenerator.outputSignal(an, 0);
      _builder.append(_outputSignal);
      _builder.append(", ");
      String _reset = SoftwareCodeGenerator.getReset(an);
      _builder.append(_reset);
      _builder.append(");");
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  protected static CharSequence _moduleInitialisation(final OpaqueAction an) {
    SoftwareCodeGenerator.checkInOut(an, 1, 1);
    StringConcatenation _builder = new StringConcatenation();
    String _name = an.getName();
    _builder.append(_name);
    _builder.append(": Action(");
    String _preCondition = SoftwareCodeGenerator.preCondition(an);
    _builder.append(_preCondition);
    _builder.append(", ");
    String _postCondition = SoftwareCodeGenerator.postCondition(an);
    _builder.append(_postCondition);
    _builder.append(", ");
    String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
    _builder.append(_inputSignal);
    _builder.append(", ");
    String _outputSignal = SoftwareCodeGenerator.outputSignal(an, 0);
    _builder.append(_outputSignal);
    _builder.append(", ");
    String _reset = SoftwareCodeGenerator.getReset(an);
    _builder.append(_reset);
    _builder.append(");");
    return _builder.toString();
  }
  
  protected static CharSequence _moduleInitialisation(final ActivityFinalNode an) {
    CharSequence _xblockexpression = null;
    {
      SoftwareCodeGenerator.checkInOut(an, 1, 0);
      StringConcatenation _builder = new StringConcatenation();
      String _name = an.getName();
      _builder.append(_name);
      _builder.append(": EndPoint(controlFlowOut.backwardSignal, ");
      String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
      _builder.append(_inputSignal);
      _builder.append(", ");
      String _reset = SoftwareCodeGenerator.getReset(an);
      _builder.append(_reset);
      _builder.append(");");
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  protected static CharSequence _moduleInitialisation(final FlowFinalNode an) {
    CharSequence _xblockexpression = null;
    {
      SoftwareCodeGenerator.checkInOut(an, 1, 0);
      StringConcatenation _builder = new StringConcatenation();
      String _name = an.getName();
      _builder.append(_name);
      _builder.append(": FlowEndPoint(");
      String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
      _builder.append(_inputSignal);
      _builder.append(", ");
      String _reset = SoftwareCodeGenerator.getReset(an);
      _builder.append(_reset);
      _builder.append(");");
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  protected static CharSequence _moduleInitialisation(final MergeNode an) {
    CharSequence _xblockexpression = null;
    {
      SoftwareCodeGenerator.checkInOut(an, 2, 1);
      StringConcatenation _builder = new StringConcatenation();
      String _name = an.getName();
      _builder.append(_name);
      _builder.append(": Merge(");
      String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
      _builder.append(_inputSignal);
      _builder.append(", ");
      String _inputSignal_1 = SoftwareCodeGenerator.inputSignal(an, 1);
      _builder.append(_inputSignal_1);
      _builder.append(", ");
      String _outputSignal = SoftwareCodeGenerator.outputSignal(an, 0);
      _builder.append(_outputSignal);
      _builder.append(", ");
      String _reset = SoftwareCodeGenerator.getReset(an);
      _builder.append(_reset);
      _builder.append(");");
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  protected static CharSequence _moduleInitialisation(final ForkNode an) {
    CharSequence _xblockexpression = null;
    {
      SoftwareCodeGenerator.checkInOut(an, 1, 2);
      StringConcatenation _builder = new StringConcatenation();
      String _name = an.getName();
      _builder.append(_name);
      _builder.append(": Fork(");
      String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
      _builder.append(_inputSignal);
      _builder.append(", ");
      String _outputSignal = SoftwareCodeGenerator.outputSignal(an, 0);
      _builder.append(_outputSignal);
      _builder.append(", ");
      String _outputSignal_1 = SoftwareCodeGenerator.outputSignal(an, 1);
      _builder.append(_outputSignal_1);
      _builder.append(", ");
      String _reset = SoftwareCodeGenerator.getReset(an);
      _builder.append(_reset);
      _builder.append(");");
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  protected static CharSequence _moduleInitialisation(final JoinNode an) {
    CharSequence _xblockexpression = null;
    {
      SoftwareCodeGenerator.checkInOut(an, 2, 1);
      StringConcatenation _builder = new StringConcatenation();
      String _name = an.getName();
      _builder.append(_name);
      _builder.append(": Join(");
      String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
      _builder.append(_inputSignal);
      _builder.append(", ");
      String _inputSignal_1 = SoftwareCodeGenerator.inputSignal(an, 1);
      _builder.append(_inputSignal_1);
      _builder.append(", ");
      String _outputSignal = SoftwareCodeGenerator.outputSignal(an, 0);
      _builder.append(_outputSignal);
      _builder.append(", ");
      String _reset = SoftwareCodeGenerator.getReset(an);
      _builder.append(_reset);
      _builder.append(");");
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  protected static CharSequence _moduleInitialisation(final CallBehaviorAction an) {
    CharSequence _xblockexpression = null;
    {
      SoftwareCodeGenerator.checkInOut(an, 1, 1);
      StringConcatenation _builder = new StringConcatenation();
      String _name = an.getName();
      _builder.append(_name);
      _builder.append(": ");
      String _name_1 = an.getBehavior().getName();
      _builder.append(_name_1);
      _builder.append("(");
      String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
      _builder.append(_inputSignal);
      _builder.append(", ");
      String _outputSignal = SoftwareCodeGenerator.outputSignal(an, 0);
      _builder.append(_outputSignal);
      _builder.append(", container, ");
      String _reset = SoftwareCodeGenerator.getReset(an);
      _builder.append(_reset);
      _builder.append(");");
      _xblockexpression = _builder;
    }
    return _xblockexpression;
  }
  
  protected static CharSequence _moduleInitialisation(final SendSignalAction an) {
    SoftwareCodeGenerator.checkInOut(an, 1, 1);
    SoftwareCodeGenerator.checkSignal(an);
    StringConcatenation _builder = new StringConcatenation();
    String _name = an.getName();
    _builder.append(_name);
    _builder.append(": SendAction(");
    String _preCondition = SoftwareCodeGenerator.preCondition(an);
    _builder.append(_preCondition);
    _builder.append(", ");
    String _postCondition = SoftwareCodeGenerator.postCondition(an);
    _builder.append(_postCondition);
    _builder.append(", ");
    String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
    _builder.append(_inputSignal);
    _builder.append(", ");
    String _outputSignal = SoftwareCodeGenerator.outputSignal(an, 0);
    _builder.append(_outputSignal);
    _builder.append(", ");
    String _reset = SoftwareCodeGenerator.getReset(an);
    _builder.append(_reset);
    _builder.append(");");
    return _builder.toString();
  }
  
  protected static CharSequence _moduleInitialisation(final AcceptEventAction an) {
    EList<ActivityPartition> partitions = an.getInPartitions();
    int _size = partitions.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Action seems to be in multiple partions \"");
      String _name = an.getName();
      _builder.append(_name);
      _builder.append("\"");
      _builder.newLineIfNotEmpty();
      _builder.append("currently only one is supported");
      _builder.newLine();
      UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException(_builder.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _unsupportedOperationException);
      return "";
    } else {
      int _size_1 = partitions.size();
      boolean _equals = (_size_1 == 1);
      if (_equals) {
        int _size_2 = an.getIncomings().size();
        boolean _equals_1 = (_size_2 == 0);
        if (_equals_1) {
          SoftwareCodeGenerator.checkInOut(an, 0, 1);
          String _preCondition = SoftwareCodeGenerator.preCondition(an);
          boolean _notEquals = (!Objects.equal(_preCondition, "TRUE"));
          if (_notEquals) {
            StringConcatenation _builder_1 = new StringConcatenation();
            _builder_1.append("Preconditions are not supported when combined with Exception handlers at \"");
            String _name_1 = an.getName();
            _builder_1.append(_name_1);
            _builder_1.append("\"");
            _builder_1.newLineIfNotEmpty();
            UnsupportedOperationException _unsupportedOperationException_1 = new UnsupportedOperationException(_builder_1.toString());
            ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _unsupportedOperationException_1);
          }
          String _postCondition = SoftwareCodeGenerator.postCondition(an);
          boolean _notEquals_1 = (!Objects.equal(_postCondition, "TRUE"));
          if (_notEquals_1) {
            StringConcatenation _builder_2 = new StringConcatenation();
            _builder_2.append("Postconditions are not supported when combined with Exception handlers at \"");
            String _name_2 = an.getName();
            _builder_2.append(_name_2);
            _builder_2.append("\"");
            _builder_2.newLineIfNotEmpty();
            UnsupportedOperationException _unsupportedOperationException_2 = new UnsupportedOperationException(_builder_2.toString());
            ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _unsupportedOperationException_2);
          }
          ActivityPartition partition = partitions.get(0);
          StringConcatenation _builder_3 = new StringConcatenation();
          String _name_3 = an.getName();
          _builder_3.append(_name_3);
          _builder_3.append(": LeaveRegionAction(");
          String _name_4 = partition.getName();
          _builder_3.append(_name_4);
          _builder_3.append("Active, ");
          String _outputSignal = SoftwareCodeGenerator.outputSignal(an, 0);
          _builder_3.append(_outputSignal);
          _builder_3.append(", ");
          String _triggerSigs = SoftwareCodeGenerator.triggerSigs(an);
          _builder_3.append(_triggerSigs);
          _builder_3.append(");");
          return _builder_3.toString();
        }
      }
    }
    SoftwareCodeGenerator.checkInOut(an, 1, 1);
    StringConcatenation _builder_4 = new StringConcatenation();
    String _name_5 = an.getName();
    _builder_4.append(_name_5);
    _builder_4.append(": ReceiveAction(");
    String _preCondition_1 = SoftwareCodeGenerator.preCondition(an);
    _builder_4.append(_preCondition_1);
    _builder_4.append(", ");
    String _postCondition_1 = SoftwareCodeGenerator.postCondition(an);
    _builder_4.append(_postCondition_1);
    _builder_4.append(", ");
    String _triggerSigs_1 = SoftwareCodeGenerator.triggerSigs(an);
    _builder_4.append(_triggerSigs_1);
    _builder_4.append(", ");
    String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
    _builder_4.append(_inputSignal);
    _builder_4.append(", ");
    String _outputSignal_1 = SoftwareCodeGenerator.outputSignal(an, 0);
    _builder_4.append(_outputSignal_1);
    _builder_4.append(", ");
    String _reset = SoftwareCodeGenerator.getReset(an);
    _builder_4.append(_reset);
    _builder_4.append(");");
    return _builder_4.toString();
  }
  
  protected static CharSequence _moduleInitialisation(final DecisionNode an) {
    SoftwareCodeGenerator.checkInOut(an, 1, 2);
    StringConcatenation _builder = new StringConcatenation();
    String _name = an.getName();
    _builder.append(_name);
    _builder.append(": Decision(");
    String _conditionFor = SoftwareCodeGenerator.conditionFor(an, 0);
    _builder.append(_conditionFor);
    _builder.append(", ");
    String _conditionFor_1 = SoftwareCodeGenerator.conditionFor(an, 1);
    _builder.append(_conditionFor_1);
    _builder.append(", ");
    String _inputSignal = SoftwareCodeGenerator.inputSignal(an, 0);
    _builder.append(_inputSignal);
    _builder.append(", ");
    String _outputSignal = SoftwareCodeGenerator.outputSignal(an, 0);
    _builder.append(_outputSignal);
    _builder.append(", ");
    String _outputSignal_1 = SoftwareCodeGenerator.outputSignal(an, 1);
    _builder.append(_outputSignal_1);
    _builder.append(", ");
    String _reset = SoftwareCodeGenerator.getReset(an);
    _builder.append(_reset);
    _builder.append(");");
    return _builder.toString();
  }
  
  public static String inputSignal(final ActivityNode an, final int index) {
    int internalIdx = index;
    ArrayList<Pair<ActivityNode, ExceptionHandler>> handlerIncomings = CodeGeneratorUtil.findIncomings(an);
    int _size = handlerIncomings.size();
    boolean _greaterEqualsThan = (internalIdx >= _size);
    if (_greaterEqualsThan) {
      int _internalIdx = internalIdx;
      int _size_1 = handlerIncomings.size();
      internalIdx = (_internalIdx - _size_1);
    } else {
      Pair<ActivityNode, ExceptionHandler> inc = handlerIncomings.get(internalIdx);
      StringConcatenation _builder = new StringConcatenation();
      String _name = inc.getFirst().getName();
      _builder.append(_name);
      _builder.append("Intercepter");
      Long _uID = UIDGenerator.getUID(inc.getSecond());
      _builder.append(_uID);
      _builder.append("to");
      String _name_1 = inc.getSecond().getHandlerBody().getName();
      _builder.append(_name_1);
      return _builder.toString();
    }
    ActivityEdge _get = an.getIncomings().get(index);
    String _name_2 = ((ControlFlow) _get).getSource().getName();
    String _plus = (_name_2 + "to");
    String _name_3 = an.getName();
    return (_plus + _name_3);
  }
  
  public static String outputSignal(final ActivityNode an, final int index) {
    int internalIdx = index;
    if ((an instanceof ExecutableNode)) {
      int _size = ((ExecutableNode)an).getHandlers().size();
      boolean _greaterEqualsThan = (internalIdx >= _size);
      if (_greaterEqualsThan) {
        int _internalIdx = internalIdx;
        int _size_1 = ((ExecutableNode)an).getHandlers().size();
        internalIdx = (_internalIdx - _size_1);
      } else {
        StringConcatenation _builder = new StringConcatenation();
        String _name = ((ExecutableNode)an).getName();
        _builder.append(_name);
        _builder.append("to");
        String _name_1 = ((ExecutableNode)an).getName();
        _builder.append(_name_1);
        _builder.append("Intercepter");
        Long _uID = UIDGenerator.getUID(((ExecutableNode)an).getHandlers().get(internalIdx));
        _builder.append(_uID);
        return _builder.toString();
      }
    }
    String _name_2 = an.getName();
    String _plus = (_name_2 + "to");
    ActivityEdge _get = an.getOutgoings().get(internalIdx);
    String _name_3 = ((ControlFlow) _get).getTarget().getName();
    return (_plus + _name_3);
  }
  
  public static String preCondition(final ActivityNode an) {
    Stereotype semspec = an.getAppliedStereotype("semanticSepcification::ActionWithSemanticSpecification");
    String preString = "TRUE";
    if ((semspec != null)) {
      Object _value = an.getValue(semspec, "preCondition");
      EObject preCondition = ((EObject) _value);
      if ((preCondition != null)) {
        preString = CodeGeneratorUtil.buildStringForConstraint(preCondition);
      }
    }
    return preString;
  }
  
  public static String postCondition(final ActivityNode an) {
    Stereotype semspec = an.getAppliedStereotype("semanticSepcification::ActionWithSemanticSpecification");
    String postString = "TRUE";
    if ((semspec != null)) {
      Object _value = an.getValue(semspec, "postCondition");
      EObject postCondition = ((EObject) _value);
      if ((postCondition != null)) {
        postString = CodeGeneratorUtil.buildStringForConstraint(postCondition);
      }
    }
    return postString;
  }
  
  public static String triggerSigs(final AcceptEventAction an) {
    String triggers = "";
    EList<Trigger> _triggers = an.getTriggers();
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
          TriggerableSignalHolder.getSingleton().addSignal(((SignalEvent)event).getSignal());
          String _triggers_2 = triggers;
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("container.trig");
          Long _uID = UIDGenerator.getUID(((SignalEvent)event).getSignal());
          _builder.append(_uID);
          triggers = (_triggers_2 + _builder);
        } else {
          if ((event instanceof TimeEvent)) {
            int _length_1 = triggers.length();
            boolean _greaterThan_1 = (_length_1 > 0);
            if (_greaterThan_1) {
              String _triggers_3 = triggers;
              triggers = (_triggers_3 + " | ");
            }
            String _triggers_4 = triggers;
            StringConcatenation _builder_1 = new StringConcatenation();
            String _name = an.getName();
            _builder_1.append(_name);
            _builder_1.append("Time");
            Long _uID_1 = UIDGenerator.getUID(event);
            _builder_1.append(_uID_1);
            _builder_1.append(".TRIG_OUT");
            triggers = (_triggers_4 + _builder_1);
          } else {
            StringConcatenation _builder_2 = new StringConcatenation();
            _builder_2.append("Unsupported Event found at ");
            String _name_1 = an.getName();
            _builder_2.append(_name_1);
            _builder_2.append(" type ");
            String _canonicalName = event.getClass().getCanonicalName();
            _builder_2.append(_canonicalName);
            _builder_2.newLineIfNotEmpty();
            UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException(_builder_2.toString());
            ThrowHelper.customThrow(ThrowHelper.ThrowType.WARN, _unsupportedOperationException);
          }
        }
      }
    }
    return triggers;
  }
  
  public static String generateTimeTrigger(final Trigger trigger) {
    EObject _eContainer = trigger.eContainer();
    AcceptEventAction an = ((AcceptEventAction) _eContainer);
    StringConcatenation _builder = new StringConcatenation();
    String _name = an.getName();
    _builder.append(_name);
    _builder.append("Time");
    Long _uID = UIDGenerator.getUID(trigger.getEvent());
    _builder.append(_uID);
    String trigName = _builder.toString();
    boolean _isEmpty = an.getInPartitions().isEmpty();
    if (_isEmpty) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("AcceptEventAction must be inside partition in order to use TimeoutEvent");
      _builder_1.newLine();
      UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException(_builder_1.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _unsupportedOperationException);
      return "";
    }
    EList<ActivityPartition> partitions = an.getInPartitions();
    int _size = partitions.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("Action seems to be in multiple partions \"");
      String _name_1 = an.getName();
      _builder_2.append(_name_1);
      _builder_2.append("\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("currently only one is supported");
      _builder_2.newLine();
      UnsupportedOperationException _unsupportedOperationException_1 = new UnsupportedOperationException(_builder_2.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _unsupportedOperationException_1);
      return "";
    }
    ActivityPartition partition = partitions.get(0);
    Event _event = trigger.getEvent();
    TimeEvent event = ((TimeEvent) _event);
    StringConcatenation _builder_3 = new StringConcatenation();
    _builder_3.append(trigName);
    _builder_3.append(": TimeTrigger(");
    String _name_2 = partition.getName();
    _builder_3.append(_name_2);
    _builder_3.append("Active, ");
    int _integerValue = event.getWhen().getExpr().integerValue();
    _builder_3.append(_integerValue);
    _builder_3.append(");");
    return _builder_3.toString();
  }
  
  public static String conditionFor(final DecisionNode an, final int index) {
    ActivityEdge _get = an.getOutgoings().get(index);
    final ControlFlow flow = ((ControlFlow) _get);
    Stereotype stereotype = flow.getAppliedStereotype("semanticSepcification::ControlFlowWithConstraint");
    if ((stereotype == null)) {
      return "TRUE";
    }
    Object _value = flow.getValue(stereotype, "constraint");
    EObject constraint = ((EObject) _value);
    return CodeGeneratorUtil.buildStringForConstraint(constraint);
  }
  
  public static void checkInOut(final ActivityNode an, final int incomings, final int outgoings) {
    int _size = an.getIncomings().size();
    int _size_1 = CodeGeneratorUtil.findIncomings(an).size();
    int _plus = (_size + _size_1);
    boolean _notEquals = (_plus != incomings);
    if (_notEquals) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Wrong number of incomings found at ");
      String _name = an.getName();
      _builder.append(_name);
      _builder.newLineIfNotEmpty();
      _builder.append("expoected ");
      _builder.append(incomings);
      _builder.append(" got ");
      int _size_2 = an.getIncomings().size();
      _builder.append(_size_2);
      _builder.append(" for type ");
      String _canonicalName = an.getClass().getCanonicalName();
      _builder.append(_canonicalName);
      _builder.newLineIfNotEmpty();
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(_builder.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.WARN, _illegalArgumentException);
    }
    int exceptionOutgoings = 0;
    if ((an instanceof ExecutableNode)) {
      int _exceptionOutgoings = exceptionOutgoings;
      int _size_3 = ((ExecutableNode)an).getHandlers().size();
      exceptionOutgoings = (_exceptionOutgoings + _size_3);
    }
    int _size_4 = an.getOutgoings().size();
    int _plus_1 = (_size_4 + exceptionOutgoings);
    boolean _notEquals_1 = (_plus_1 != outgoings);
    if (_notEquals_1) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Wrong number of outgoings found at ");
      String _name_1 = an.getName();
      _builder_1.append(_name_1);
      _builder_1.newLineIfNotEmpty();
      _builder_1.append("expoected ");
      _builder_1.append(outgoings);
      _builder_1.append(" got ");
      int _size_5 = an.getOutgoings().size();
      _builder_1.append(_size_5);
      _builder_1.append(" for type ");
      String _canonicalName_1 = an.getClass().getCanonicalName();
      _builder_1.append(_canonicalName_1);
      _builder_1.newLineIfNotEmpty();
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException(_builder_1.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.WARN, _illegalArgumentException_1);
    }
  }
  
  public static String getReset(final ActivityNode an) {
    boolean _isEmpty = an.getInPartitions().isEmpty();
    if (_isEmpty) {
      return "reset";
    }
    EList<ActivityPartition> partitions = an.getInPartitions();
    int _size = partitions.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Action seems to be in multiple partions \"");
      String _name = an.getName();
      _builder.append(_name);
      _builder.append("\"");
      _builder.newLineIfNotEmpty();
      _builder.append("currently only one is supported");
      _builder.newLine();
      UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException(_builder.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _unsupportedOperationException);
      return "";
    }
    ActivityPartition partition = partitions.get(0);
    StringConcatenation _builder_1 = new StringConcatenation();
    String _name_1 = partition.getName();
    _builder_1.append(_name_1);
    _builder_1.append("Reset");
    return _builder_1.toString();
  }
  
  /**
   * Check that an signal is attached to the SendSignalAction
   */
  public static void checkSignal(final SendSignalAction an) {
    Signal _signal = an.getSignal();
    boolean _tripleEquals = (_signal == null);
    if (_tripleEquals) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("No Signal found for SendSignalAction at ");
      String _name = an.getName();
      _builder.append(_name);
      _builder.append(" type ");
      String _canonicalName = an.getClass().getCanonicalName();
      _builder.append(_canonicalName);
      _builder.newLineIfNotEmpty();
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(_builder.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.WARN, _illegalArgumentException);
    }
  }
  
  public static String generateFlowInterceptor(final ExecutableNode node, final ExceptionHandler handler, final Map<ActivityPartition, List<String>> lastRegion) {
    boolean _isEmpty = node.getInPartitions().isEmpty();
    if (_isEmpty) {
      return "reset";
    }
    EList<ActivityPartition> partitions = node.getInPartitions();
    int _size = partitions.size();
    boolean _greaterThan = (_size > 1);
    if (_greaterThan) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Action seems to be in multiple partions \"");
      String _name = node.getName();
      _builder.append(_name);
      _builder.append("\"");
      _builder.newLineIfNotEmpty();
      _builder.append("currently only one is supported");
      _builder.newLine();
      UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException(_builder.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _unsupportedOperationException);
      return "";
    }
    ActivityPartition partition = partitions.get(0);
    List<String> mapData = lastRegion.get(partition);
    if ((mapData == null)) {
      ArrayList<String> _arrayList = new ArrayList<String>();
      mapData = _arrayList;
      lastRegion.put(partition, mapData);
    }
    StringConcatenation _builder_1 = new StringConcatenation();
    String _name_1 = node.getName();
    _builder_1.append(_name_1);
    _builder_1.append("Intercepter");
    Long _uID = UIDGenerator.getUID(handler);
    _builder_1.append(_uID);
    String interName = _builder_1.toString();
    mapData.add(interName);
    StringConcatenation _builder_2 = new StringConcatenation();
    String _name_2 = node.getName();
    _builder_2.append(_name_2);
    _builder_2.append("to");
    _builder_2.append(interName);
    String srcFlow = _builder_2.toString();
    StringConcatenation _builder_3 = new StringConcatenation();
    _builder_3.append(interName);
    _builder_3.append("to");
    String _name_3 = handler.getHandlerBody().getName();
    _builder_3.append(_name_3);
    String targetFlow = _builder_3.toString();
    StringConcatenation _builder_4 = new StringConcatenation();
    String _name_4 = partition.getName();
    _builder_4.append(_name_4);
    _builder_4.append("Decision = ");
    int _size_1 = mapData.size();
    _builder_4.append(_size_1);
    String allow = _builder_4.toString();
    StringConcatenation _builder_5 = new StringConcatenation();
    String _name_5 = partition.getName();
    _builder_5.append(_name_5);
    _builder_5.append("IntercepterReset");
    String reset = _builder_5.toString();
    StringConcatenation _builder_6 = new StringConcatenation();
    _builder_6.append(interName);
    _builder_6.append(": ControlFlowIntercepter(");
    _builder_6.append(srcFlow);
    _builder_6.append(", ");
    _builder_6.append(targetFlow);
    _builder_6.append(", ");
    _builder_6.append(allow);
    _builder_6.append(", ");
    _builder_6.append(reset);
    _builder_6.append(");");
    _builder_6.newLineIfNotEmpty();
    return _builder_6.toString();
  }
  
  public static ArrayList<String> allActiveElmsNames(final Activity a) {
    ArrayList<String> names = new ArrayList<String>();
    EList<ActivityNode> _nodes = a.getNodes();
    for (final ActivityNode node : _nodes) {
      {
        if ((node instanceof CallBehaviorAction)) {
          String _name = ((CallBehaviorAction)node).getName();
          String _plus = (_name + ".SUB_ACTIVE");
          names.add(_plus);
        } else {
          String _name_1 = node.getName();
          String _plus_1 = (_name_1 + ".ACTIVE");
          names.add(_plus_1);
        }
        if ((node instanceof ExecutableNode)) {
          EList<ExceptionHandler> _handlers = ((ExecutableNode)node).getHandlers();
          for (final ExceptionHandler handler : _handlers) {
            StringConcatenation _builder = new StringConcatenation();
            String _name_2 = ((ExecutableNode)node).getName();
            _builder.append(_name_2);
            _builder.append("Intercepter");
            Long _uID = UIDGenerator.getUID(handler);
            _builder.append(_uID);
            _builder.append(".ACTIVE");
            names.add(_builder.toString());
          }
        }
      }
    }
    return names;
  }
  
  public static ArrayList<String> toActive(final Iterable<ActivityNode> nodes) {
    ArrayList<String> results = new ArrayList<String>();
    for (final ActivityNode node : nodes) {
      if ((node instanceof CallBehaviorAction)) {
        String _name = ((CallBehaviorAction)node).getName();
        String _plus = (_name + ".SUB_ACTIVE");
        results.add(_plus);
      } else {
        String _name_1 = node.getName();
        String _plus_1 = (_name_1 + ".ACTIVE");
        results.add(_plus_1);
      }
    }
    return results;
  }
  
  public static CharSequence moduleInitialisation(final ActivityNode an) {
    if (an instanceof CallBehaviorAction) {
      return _moduleInitialisation((CallBehaviorAction)an);
    } else if (an instanceof SendSignalAction) {
      return _moduleInitialisation((SendSignalAction)an);
    } else if (an instanceof AcceptEventAction) {
      return _moduleInitialisation((AcceptEventAction)an);
    } else if (an instanceof ActivityFinalNode) {
      return _moduleInitialisation((ActivityFinalNode)an);
    } else if (an instanceof FlowFinalNode) {
      return _moduleInitialisation((FlowFinalNode)an);
    } else if (an instanceof OpaqueAction) {
      return _moduleInitialisation((OpaqueAction)an);
    } else if (an instanceof DecisionNode) {
      return _moduleInitialisation((DecisionNode)an);
    } else if (an instanceof ForkNode) {
      return _moduleInitialisation((ForkNode)an);
    } else if (an instanceof InitialNode) {
      return _moduleInitialisation((InitialNode)an);
    } else if (an instanceof JoinNode) {
      return _moduleInitialisation((JoinNode)an);
    } else if (an instanceof MergeNode) {
      return _moduleInitialisation((MergeNode)an);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(an).toString());
    }
  }
}
