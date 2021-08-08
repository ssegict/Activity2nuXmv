package ict.generate.modelToSMV;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import ict.generate.modelToSMV.ComparatorEnum;
import ict.generate.util.Pair;
import ict.generate.util.ThrowHelper;
import ict.generate.util.UIDGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExceptionHandler;
import org.eclipse.uml2.uml.ExecutableNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.RedefinableElement;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.Vertex;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class CodeGeneratorUtil {
  public static <T extends Object> ArrayList<T> distinct(final Iterable<T> in) {
    ArrayList<T> unique = new ArrayList<T>();
    for (final T elm : in) {
      boolean _contains = unique.contains(elm);
      boolean _not = (!_contains);
      if (_not) {
        unique.add(elm);
      }
    }
    return unique;
  }
  
  private static final String AND_CONSTRAINT = "AndConstraint";
  
  private static final String OR_CONSTRAINT = "OrConstraint";
  
  private static final String NOT_CONSTRAINT = "NotConstraint";
  
  private static final String CONSTRAINT = "Constraint";
  
  private static final String VALUE_CONSTRAINT = "ValueConstraint";
  
  private static final String OBJECTLIFECYCLE_CONSTRAINT = "ObjectLifeCycleConstraint";
  
  private static final String STATE_STRUCTURAL_FEATURE = "vertex";
  
  private static final String CONSTRAINTS_STRUCTURAL_FEATURE = "constraints";
  
  private static final String ATTRIBUTE_STRUCTURAL_FEATURE = "attribute";
  
  private static final String VALUE_STRUCTURAL_FEATURE = "value";
  
  private static final String COMPARATOR_STRUCTURAL_FEATURE = "comperator";
  
  public static String buildStringForConstraint(final EObject obj) {
    String _name = obj.eClass().getName();
    if (_name != null) {
      switch (_name) {
        case CodeGeneratorUtil.OBJECTLIFECYCLE_CONSTRAINT:
          Object _eGet = obj.eGet(obj.eClass().getEStructuralFeature(CodeGeneratorUtil.STATE_STRUCTURAL_FEATURE));
          Vertex state = ((Vertex) _eGet);
          return CodeGeneratorUtil.buildConditionForState(state);
        case CodeGeneratorUtil.VALUE_CONSTRAINT:
          Object _eGet_1 = obj.eGet(obj.eClass().getEStructuralFeature(CodeGeneratorUtil.ATTRIBUTE_STRUCTURAL_FEATURE));
          Property property = ((Property) _eGet_1);
          Object _eGet_2 = obj.eGet(obj.eClass().getEStructuralFeature(CodeGeneratorUtil.VALUE_STRUCTURAL_FEATURE));
          String value = ((String) _eGet_2);
          Object comparatorObj = obj.eGet(obj.eClass().getEStructuralFeature(CodeGeneratorUtil.COMPARATOR_STRUCTURAL_FEATURE));
          ComparatorEnum comp = ComparatorEnum.getEnum(comparatorObj.toString());
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("(");
          CharSequence _generatePath = CodeGeneratorUtil.generatePath(property);
          _builder.append(_generatePath);
          String _name_1 = property.getName();
          _builder.append(_name_1);
          Long _uID = UIDGenerator.getUID(property);
          _builder.append(_uID);
          _builder.append(" ");
          _builder.append(comp);
          _builder.append(" ");
          _builder.append(value);
          _builder.append(")");
          return _builder.toString();
        case CodeGeneratorUtil.AND_CONSTRAINT:
          Object _eGet_3 = obj.eGet(obj.eClass().getEStructuralFeature(CodeGeneratorUtil.CONSTRAINTS_STRUCTURAL_FEATURE));
          EList<EObject> andConstraints = ((EList<EObject>) _eGet_3);
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("(");
          final Function1<EObject, String> _function = (EObject it) -> {
            return CodeGeneratorUtil.buildStringForConstraint(it);
          };
          String _join = IterableExtensions.join(ListExtensions.<EObject, String>map(andConstraints, _function), " & ");
          _builder_1.append(_join);
          _builder_1.append(")");
          return _builder_1.toString();
        case CodeGeneratorUtil.OR_CONSTRAINT:
          Object _eGet_4 = obj.eGet(obj.eClass().getEStructuralFeature(CodeGeneratorUtil.CONSTRAINTS_STRUCTURAL_FEATURE));
          EList<EObject> orConstraints = ((EList<EObject>) _eGet_4);
          StringConcatenation _builder_2 = new StringConcatenation();
          _builder_2.append("(");
          final Function1<EObject, String> _function_1 = (EObject it) -> {
            return CodeGeneratorUtil.buildStringForConstraint(it);
          };
          String _join_1 = IterableExtensions.join(ListExtensions.<EObject, String>map(orConstraints, _function_1), " | ");
          _builder_2.append(_join_1);
          _builder_2.append(")");
          return _builder_2.toString();
        case CodeGeneratorUtil.NOT_CONSTRAINT:
          Object _eGet_5 = obj.eGet(obj.eClass().getEStructuralFeature(CodeGeneratorUtil.CONSTRAINTS_STRUCTURAL_FEATURE));
          EObject notConstraint = ((EObject) _eGet_5);
          StringConcatenation _builder_3 = new StringConcatenation();
          _builder_3.append("!(");
          String _buildStringForConstraint = CodeGeneratorUtil.buildStringForConstraint(notConstraint);
          _builder_3.append(_buildStringForConstraint);
          _builder_3.append(")");
          return _builder_3.toString();
        case CodeGeneratorUtil.CONSTRAINT:
          Constraint constr = ((Constraint) obj);
          Element _get = constr.getConstrainedElements().get(0);
          State element = ((State) _get);
          ValueSpecification _specification = constr.getSpecification();
          OpaqueExpression spec = ((OpaqueExpression) _specification);
          StringConcatenation _builder_4 = new StringConcatenation();
          _builder_4.append("(");
          CharSequence _generatePath_1 = CodeGeneratorUtil.generatePath(element.getContainer());
          _builder_4.append(_generatePath_1);
          String _name_2 = element.getContainer().getName();
          _builder_4.append(_name_2);
          _builder_4.append(" = ");
          String _name_3 = element.getName();
          _builder_4.append(_name_3);
          _builder_4.append(") = ");
          String _upperCase = spec.getBodies().get(0).toUpperCase();
          _builder_4.append(_upperCase);
          return _builder_4.toString();
        default:
          {
            StringConcatenation _builder_5 = new StringConcatenation();
            _builder_5.append("Unknown class name \"");
            String _name_4 = obj.eClass().getName();
            _builder_5.append(_name_4);
            _builder_5.append("\"");
            UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException(_builder_5.toString());
            ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _unsupportedOperationException);
            return "";
          }
      }
    } else {
      {
        StringConcatenation _builder_5 = new StringConcatenation();
        _builder_5.append("Unknown class name \"");
        String _name_4 = obj.eClass().getName();
        _builder_5.append(_name_4);
        _builder_5.append("\"");
        UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException(_builder_5.toString());
        ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _unsupportedOperationException);
        return "";
      }
    }
  }
  
  public static String buildConditionForState(final Vertex state) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("(");
    CharSequence _generatePath = CodeGeneratorUtil.generatePath(state.getContainer());
    _builder.append(_generatePath);
    String _name = state.getContainer().getName();
    _builder.append(_name);
    _builder.append(" = ");
    String _name_1 = state.getName();
    _builder.append(_name_1);
    _builder.append(")");
    return _builder.toString();
  }
  
  protected static CharSequence _generatePath(final Region r) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("container.olc.");
    String _name = r.containingStateMachine().getName();
    _builder.append(_name);
    _builder.append("obj.");
    return _builder;
  }
  
  protected static CharSequence _generatePath(final Property p) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("container.olc.");
    return _builder;
  }
  
  protected static String _generatePath(final Action a) {
    String _xblockexpression = null;
    {
      String path = CodeGeneratorUtil.recursiveFindPath(CodeGeneratorUtil.getMainActivity(a), a);
      if ((path == null)) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Unable to generate path for activity ");
        String _name = a.getName();
        _builder.append(_name);
        IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(_builder.toString());
        ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _illegalArgumentException);
      }
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("container.mainAct");
      String _recursiveFindPath = CodeGeneratorUtil.recursiveFindPath(CodeGeneratorUtil.getMainActivity(a), a);
      _builder_1.append(_recursiveFindPath);
      _xblockexpression = _builder_1.toString();
    }
    return _xblockexpression;
  }
  
  public static boolean containsModules(final Model m) {
    boolean _isEmpty = IterableExtensions.isEmpty(Iterables.<Activity>filter(m.getOwnedElements(), Activity.class));
    return (!_isEmpty);
  }
  
  public static boolean containsOLC(final Model m) {
    boolean _isEmpty = IterableExtensions.isEmpty(Iterables.<StateMachine>filter(m.getOwnedElements(), StateMachine.class));
    return (!_isEmpty);
  }
  
  public static String recursiveFindPath(final Activity findIn, final Action toFind) {
    ArrayList<ActivityNode> _distinct = CodeGeneratorUtil.<ActivityNode>distinct(findIn.getNodes());
    for (final ActivityNode an : _distinct) {
      {
        boolean _equals = Objects.equal(an, toFind);
        if (_equals) {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append(".");
          String _name = an.getName();
          _builder.append(_name);
          return _builder.toString();
        }
        if ((an instanceof CallBehaviorAction)) {
          Behavior _behavior = ((CallBehaviorAction)an).getBehavior();
          String subFind = CodeGeneratorUtil.recursiveFindPath(((Activity) _behavior), toFind);
          if ((subFind != null)) {
            StringConcatenation _builder_1 = new StringConcatenation();
            _builder_1.append(".");
            String _name_1 = ((CallBehaviorAction)an).getName();
            _builder_1.append(_name_1);
            _builder_1.append(subFind);
            return _builder_1.toString();
          }
        }
      }
    }
    return null;
  }
  
  public static Activity getMainActivity(final EObject anyObject) {
    EList<Resource> resources = anyObject.eResource().getResourceSet().getResources();
    return CodeGeneratorUtil.getMainActivity(resources);
  }
  
  public static Activity getMainActivity(final Iterable<Resource> resources) {
    for (final Resource r : resources) {
      {
        final Function1<Model, EList<EObject>> _function = (Model it) -> {
          return it.eContents();
        };
        final Function1<Activity, Boolean> _function_1 = (Activity it) -> {
          return Boolean.valueOf(it.getName().equals("MainActivity"));
        };
        Iterable<Activity> mainAct = IterableExtensions.<Activity>filter(Iterables.<Activity>filter((Iterables.<EObject>concat(IterableExtensions.<Model, EList<EObject>>map(Iterables.<Model>filter(r.getContents(), Model.class), _function))), Activity.class), _function_1);
        boolean _isEmpty = IterableExtensions.isEmpty(mainAct);
        boolean _not = (!_isEmpty);
        if (_not) {
          final Iterable<Activity> _converted_mainAct = (Iterable<Activity>)mainAct;
          return ((Activity[])Conversions.unwrapArray(_converted_mainAct, Activity.class))[0];
        }
      }
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("No Activity named MainActivity found in given resources");
    IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(_builder.toString());
    ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _illegalArgumentException);
    return null;
  }
  
  public static EObject findByNamePath(final Iterable<EObject> haystack, final String needlePath) {
    return CodeGeneratorUtil.findByNamePath(haystack, needlePath, true);
  }
  
  public static EObject findByNamePath(final Iterable<EObject> haystack, final String needlePath, final boolean canReturnNull) {
    int nextSep = needlePath.indexOf(".");
    if ((nextSep == (-1))) {
      nextSep = needlePath.length();
    }
    final String nextElm = needlePath.substring(0, nextSep);
    String _xifexpression = null;
    int _length = needlePath.length();
    int _minus = (_length - 2);
    boolean _lessThan = (nextSep < _minus);
    if (_lessThan) {
      _xifexpression = needlePath.substring((nextSep + 1));
    } else {
      _xifexpression = "";
    }
    String remaining = _xifexpression;
    final Function1<NamedElement, Boolean> _function = (NamedElement it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, nextElm));
    };
    Iterable<NamedElement> possible = IterableExtensions.<NamedElement>filter(Iterables.<NamedElement>filter(haystack, NamedElement.class), _function);
    boolean _isEmpty = IterableExtensions.isEmpty(possible);
    if (_isEmpty) {
      if (canReturnNull) {
        return null;
      }
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Element needs to be found, but has not been found");
      _builder.newLine();
      _builder.append("Elements in stack:");
      _builder.newLine();
      final Function1<NamedElement, String> _function_1 = (NamedElement it) -> {
        return it.getName();
      };
      String _join = IterableExtensions.join(IterableExtensions.<NamedElement, String>map(Iterables.<NamedElement>filter(haystack, NamedElement.class), _function_1), ", ");
      _builder.append(_join);
      _builder.newLineIfNotEmpty();
      _builder.append("Searched for: ");
      _builder.append(nextElm);
      _builder.newLineIfNotEmpty();
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(_builder.toString());
      ThrowHelper.customThrow(ThrowHelper.ThrowType.ERROR, _illegalArgumentException);
    }
    int _length_1 = remaining.length();
    boolean _equals = (_length_1 == 0);
    if (_equals) {
      final Iterable<NamedElement> _converted_possible = (Iterable<NamedElement>)possible;
      return ((EObject[])Conversions.unwrapArray(_converted_possible, EObject.class))[0];
    }
    final Function1<NamedElement, EList<EObject>> _function_2 = (NamedElement it) -> {
      return it.eContents();
    };
    return CodeGeneratorUtil.findByNamePath(Iterables.<EObject>concat(IterableExtensions.<NamedElement, EList<EObject>>map(possible, _function_2)), remaining);
  }
  
  public static ArrayList<Pair<ActivityNode, ExceptionHandler>> findIncomings(final ActivityNode an) {
    ArrayList<Pair<ActivityNode, ExceptionHandler>> incomings = new ArrayList<Pair<ActivityNode, ExceptionHandler>>();
    if ((an instanceof ExecutableNode)) {
      Activity act = ((ExecutableNode)an).getActivity();
      Iterable<ExecutableNode> _filter = Iterables.<ExecutableNode>filter(act.getNodes(), ExecutableNode.class);
      for (final ExecutableNode node : _filter) {
        EList<ExceptionHandler> _handlers = node.getHandlers();
        for (final ExceptionHandler handler : _handlers) {
          ExecutableNode _handlerBody = handler.getHandlerBody();
          boolean _equals = Objects.equal(_handlerBody, an);
          if (_equals) {
            Pair<ActivityNode, ExceptionHandler> _pair = new Pair<ActivityNode, ExceptionHandler>(node, handler);
            incomings.add(_pair);
          }
        }
      }
    }
    return incomings;
  }
  
  public static CharSequence generatePath(final RedefinableElement a) {
    if (a instanceof Action) {
      return _generatePath((Action)a);
    } else if (a instanceof Property) {
      return _generatePath((Property)a);
    } else if (a instanceof Region) {
      return _generatePath((Region)a);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(a).toString());
    }
  }
}
