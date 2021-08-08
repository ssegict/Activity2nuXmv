package ict.generate.modelToSMV;

import com.google.common.collect.Iterables;
import ict.generate.modelToSMV.TriggerableSignal;
import ict.generate.util.ThrowHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class TriggerableSignalHolder {
  private static final TriggerableSignalHolder SINGLETON = new TriggerableSignalHolder();
  
  public static TriggerableSignalHolder getSingleton() {
    return TriggerableSignalHolder.SINGLETON;
  }
  
  private HashMap<Activity, ArrayList<TriggerableSignal>> signalMappings = new HashMap<Activity, ArrayList<TriggerableSignal>>();
  
  public List<TriggerableSignal> readSignals(final Activity activity) {
    ArrayList<TriggerableSignal> signals = new ArrayList<TriggerableSignal>();
    this.signalMappings.put(activity, signals);
    final Function1<ActivityNode, Boolean> _function = (ActivityNode it) -> {
      Stereotype _appliedStereotype = it.getAppliedStereotype("semanticSepcification::ActionWithSemanticSpecification");
      return Boolean.valueOf((_appliedStereotype != null));
    };
    Iterable<ActivityNode> _filter = IterableExtensions.<ActivityNode>filter(activity.getNodes(), _function);
    for (final ActivityNode node : _filter) {
      {
        Stereotype stero = node.getAppliedStereotype("semanticSepcification::ActionWithSemanticSpecification");
        Object _value = node.getValue(stero, "objectlifecycleBehavior");
        Interaction interaction = ((Interaction) _value);
        if ((interaction != null)) {
          EList<Action> _actions = interaction.getActions();
          for (final Action act : _actions) {
            if ((act instanceof SendSignalAction)) {
              final Signal target = ((SendSignalAction)act).getSignal();
              final Function1<TriggerableSignal, Boolean> _function_1 = (TriggerableSignal it) -> {
                return Boolean.valueOf(it.targets(target));
              };
              TriggerableSignal signal = IterableExtensions.<TriggerableSignal>findFirst(signals, _function_1);
              if ((signal == null)) {
                TriggerableSignal _triggerableSignal = new TriggerableSignal(target);
                signal = _triggerableSignal;
                signals.add(signal);
                this.addSignal(target);
              }
              signal.addTriggerCause(node);
            } else {
              StringConcatenation _builder = new StringConcatenation();
              _builder.append("Action ");
              String _name = node.getName();
              _builder.append(_name);
              _builder.append(" refers to a broken specification ");
              String _name_1 = interaction.getName();
              _builder.append(_name_1);
              _builder.newLineIfNotEmpty();
              _builder.append("Specifications must contain only SendSignalActions \"");
              String _canonicalName = act.getClass().getCanonicalName();
              _builder.append(_canonicalName);
              _builder.append("\" found");
              _builder.newLineIfNotEmpty();
              IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(_builder.toString());
              ThrowHelper.customThrow(ThrowHelper.ThrowType.WARN, _illegalArgumentException);
            }
          }
        }
      }
    }
    Iterable<SendSignalAction> _filter_1 = Iterables.<SendSignalAction>filter(activity.getNodes(), SendSignalAction.class);
    for (final SendSignalAction seact : _filter_1) {
      {
        final Signal target = seact.getSignal();
        final Function1<TriggerableSignal, Boolean> _function_1 = (TriggerableSignal it) -> {
          return Boolean.valueOf(it.targets(target));
        };
        TriggerableSignal signal = IterableExtensions.<TriggerableSignal>findFirst(signals, _function_1);
        if ((signal == null)) {
          TriggerableSignal _triggerableSignal = new TriggerableSignal(target);
          signal = _triggerableSignal;
          signals.add(signal);
          this.addSignal(target);
        }
        signal.addTriggerCause(seact);
      }
    }
    Iterable<CallBehaviorAction> _filter_2 = Iterables.<CallBehaviorAction>filter(activity.getNodes(), CallBehaviorAction.class);
    for (final CallBehaviorAction callAct : _filter_2) {
      Behavior _behavior = callAct.getBehavior();
      List<TriggerableSignal> _signals = this.getSignals(((Activity) _behavior));
      for (final TriggerableSignal subSignal : _signals) {
        {
          final Function1<TriggerableSignal, Boolean> _function_1 = (TriggerableSignal it) -> {
            return Boolean.valueOf(it.targets(subSignal.getTarget()));
          };
          TriggerableSignal signal = IterableExtensions.<TriggerableSignal>findFirst(signals, _function_1);
          if ((signal == null)) {
            Signal _target = subSignal.getTarget();
            TriggerableSignal _triggerableSignal = new TriggerableSignal(_target);
            signal = _triggerableSignal;
            signals.add(signal);
            this.addSignal(subSignal.getTarget());
          }
          signal.addTriggerCause(callAct);
        }
      }
    }
    return signals;
  }
  
  public List<TriggerableSignal> getSignals(final Activity a) {
    ArrayList<TriggerableSignal> signals = this.signalMappings.get(a);
    if ((signals == null)) {
      return this.readSignals(a);
    }
    return signals;
  }
  
  private ArrayList<Signal> allSignals = new ArrayList<Signal>();
  
  public ArrayList<TriggerableSignal> getMainSignals(final Activity mainAct) {
    ArrayList<TriggerableSignal> retval = new ArrayList<TriggerableSignal>();
    for (final Signal sig : this.allSignals) {
      TriggerableSignal _triggerableSignal = new TriggerableSignal(sig);
      retval.add(_triggerableSignal);
    }
    List<TriggerableSignal> _signals = this.getSignals(mainAct);
    for (final TriggerableSignal sig_1 : _signals) {
      {
        final Function1<TriggerableSignal, Boolean> _function = (TriggerableSignal it) -> {
          return Boolean.valueOf(it.targets(sig_1.getTarget()));
        };
        TriggerableSignal trigSig = IterableExtensions.<TriggerableSignal>findFirst(retval, _function);
        if ((trigSig == null)) {
          Signal _target = sig_1.getTarget();
          TriggerableSignal _triggerableSignal_1 = new TriggerableSignal(_target);
          trigSig = _triggerableSignal_1;
          retval.add(trigSig);
        }
        trigSig.addTriggerCause("mainAct");
      }
    }
    for (final Signal sig_2 : this.testerSigs) {
      {
        final Function1<TriggerableSignal, Boolean> _function = (TriggerableSignal it) -> {
          return Boolean.valueOf(it.targets(sig_2));
        };
        TriggerableSignal trigSig = IterableExtensions.<TriggerableSignal>findFirst(retval, _function);
        if ((trigSig == null)) {
          TriggerableSignal _triggerableSignal_1 = new TriggerableSignal(sig_2);
          trigSig = _triggerableSignal_1;
          retval.add(trigSig);
        }
        trigSig.addTriggerCause("test");
      }
    }
    return retval;
  }
  
  public boolean addSignal(final Signal toAdd) {
    boolean _xifexpression = false;
    boolean _contains = this.allSignals.contains(toAdd);
    boolean _not = (!_contains);
    if (_not) {
      _xifexpression = this.allSignals.add(toAdd);
    }
    return _xifexpression;
  }
  
  private ArrayList<Signal> testerSigs = new ArrayList<Signal>();
  
  public String addTesterCall(final Signal target) {
    this.testerSigs.add(target);
    return new TriggerableSignal(target).getName();
  }
}
