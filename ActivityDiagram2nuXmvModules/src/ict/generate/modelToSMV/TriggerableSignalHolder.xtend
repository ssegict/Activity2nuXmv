package ict.generate.modelToSMV;

import java.util.ArrayList
import java.util.HashMap
import java.util.List
import org.eclipse.uml2.uml.Activity
import org.eclipse.uml2.uml.CallBehaviorAction
import org.eclipse.uml2.uml.Interaction
import org.eclipse.uml2.uml.SendSignalAction
import org.eclipse.uml2.uml.Signal
import ict.generate.util.ThrowHelper
import ict.generate.util.ThrowHelper.ThrowType

class TriggerableSignalHolder {
	static val SINGLETON = new TriggerableSignalHolder()
	static def getSingleton() {
		return SINGLETON
	}
	
	var signalMappings = new HashMap<Activity, ArrayList<TriggerableSignal>>()
	
	def List<TriggerableSignal> readSignals(Activity activity) {
		var signals = new ArrayList<TriggerableSignal>()
		//put in first so circle definitions won't cause problems
		signalMappings.put(activity, signals)
		
		// Filter all specifications for the given activity
		for(node : activity.nodes
			.filter[getAppliedStereotype("semanticSepcification::ActionWithSemanticSpecification") !== null]) {
			
			var stero = node.getAppliedStereotype("semanticSepcification::ActionWithSemanticSpecification")
			var interaction = node.getValue(stero, "objectlifecycleBehavior") as Interaction
			if(interaction !== null) {
				for(act : interaction.actions) {
					if(act instanceof SendSignalAction) {
						val target = act.signal
						var signal = signals.findFirst[targets(target)]
						
						if(signal === null) {
							signal = new TriggerableSignal(target)
							signals.add(signal)
							addSignal(target)
						}
						signal.addTriggerCause(node)
					} else {
						ThrowHelper.customThrow(ThrowType.WARN, new IllegalArgumentException('''
							Action «node.name» refers to a broken specification «interaction.name»
							Specifications must contain only SendSignalActions "«act.class.canonicalName»" found
						'''))
					}
				}
			}
		}
		
		//Handle SendSignalActions
		for(seact : activity.nodes.filter(SendSignalAction)) {
			val target = seact.signal
			var signal = signals.findFirst[targets(target)]
			
			if(signal === null) {
				signal = new TriggerableSignal(target)
				signals.add(signal)
				addSignal(target)
			}
			signal.addTriggerCause(seact)
		}
		
		//handle sub calls
		for(callAct : activity.nodes.filter(CallBehaviorAction)) {
			for(TriggerableSignal subSignal : getSignals(callAct.behavior as Activity)) {
				var signal = signals.findFirst[targets(subSignal.target)]
				
				if(signal === null) {
					signal = new TriggerableSignal(subSignal.target)
					signals.add(signal)
					addSignal(subSignal.target)
				}
				signal.addTriggerCause(callAct)
			}
		}
		
		return signals
	}
	
	def getSignals(Activity a) {
		var signals = signalMappings.get(a)
		if(signals === null) {
			return readSignals(a)
		}
		return signals
	}
	
	var allSignals = new ArrayList<Signal>()
	def getMainSignals(Activity mainAct) {
		var retval = new ArrayList<TriggerableSignal>()
		
		//Create Signals
		for(sig : allSignals) {
			retval.add(new TriggerableSignal(sig))
		}
		
		//Add main act trigger source
		for(sig : getSignals(mainAct)) {
			var trigSig = retval.findFirst[targets(sig.target)]
			
			if(trigSig === null) {
				trigSig = new TriggerableSignal(sig.target)
				retval.add(trigSig)
			}
			trigSig.addTriggerCause("mainAct")
		}
		
		for(sig : testerSigs) {
			var trigSig = retval.findFirst[targets(sig)]
			
			if(trigSig === null) {
				trigSig = new TriggerableSignal(sig)
				retval.add(trigSig)
			}
			trigSig.addTriggerCause("test")
		}
		return retval
	}
	
	def addSignal(Signal toAdd) {
		if(! allSignals.contains(toAdd)) {
			allSignals.add(toAdd)
		}
	}
	
	var testerSigs = new ArrayList<Signal>
	def addTesterCall(Signal target) {
		testerSigs.add(target)
		return new TriggerableSignal(target).name
	}
}
