package ict.generate.modelToSMV;

import com.google.common.base.Objects;
import ict.generate.util.UIDGenerator;
import java.util.ArrayList;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class TriggerableSignal {
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.NONE })
  private Signal target;
  
  private ArrayList<ActivityNode> triggerCausesNodes = new ArrayList<ActivityNode>();
  
  private ArrayList<String> triggerCausesString = new ArrayList<String>();
  
  public TriggerableSignal(final Signal target) {
    this.target = target;
  }
  
  public String getName() {
    Long _uID = UIDGenerator.getUID(this.target);
    return ("trig" + _uID);
  }
  
  public String getExpression() {
    boolean first = true;
    String expression = "";
    for (final ActivityNode an : this.triggerCausesNodes) {
      {
        if ((!first)) {
          String _expression = expression;
          expression = (_expression + " | ");
        }
        first = false;
        if ((an instanceof CallBehaviorAction)) {
          String _expression_1 = expression;
          String _name = ((CallBehaviorAction)an).getName();
          String _plus = (_name + ".");
          String _name_1 = this.getName();
          String _plus_1 = (_plus + _name_1);
          expression = (_expression_1 + _plus_1);
        } else {
          if ((an instanceof SendSignalAction)) {
            String _expression_2 = expression;
            String _name_2 = ((SendSignalAction)an).getName();
            String _plus_2 = (_name_2 + ".TRIGGEROUT");
            expression = (_expression_2 + _plus_2);
          } else {
            String _expression_3 = expression;
            String _name_3 = an.getName();
            String _plus_3 = (_name_3 + ".BEH");
            expression = (_expression_3 + _plus_3);
          }
        }
      }
    }
    for (final String str : this.triggerCausesString) {
      {
        if ((!first)) {
          String _expression = expression;
          expression = (_expression + " | ");
        }
        first = false;
        String _expression_1 = expression;
        String _name = this.getName();
        String _plus = ((str + ".") + _name);
        expression = (_expression_1 + _plus);
      }
    }
    if (first) {
      expression = "FALSE";
      first = false;
    }
    return expression;
  }
  
  public boolean targets(final Signal signal) {
    return Objects.equal(this.target, signal);
  }
  
  public boolean addTriggerCause(final ActivityNode an) {
    boolean _xifexpression = false;
    boolean _contains = this.triggerCausesNodes.contains(an);
    boolean _not = (!_contains);
    if (_not) {
      _xifexpression = this.triggerCausesNodes.add(an);
    }
    return _xifexpression;
  }
  
  public boolean addTriggerCause(final String str) {
    boolean _xifexpression = false;
    boolean _contains = this.triggerCausesString.contains(str);
    boolean _not = (!_contains);
    if (_not) {
      _xifexpression = this.triggerCausesString.add(str);
    }
    return _xifexpression;
  }
  
  @Pure
  public Signal getTarget() {
    return this.target;
  }
}
