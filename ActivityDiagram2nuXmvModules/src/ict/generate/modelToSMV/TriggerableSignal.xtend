package ict.generate.modelToSMV;

import ict.generate.util.UIDGenerator
import java.util.ArrayList
import org.eclipse.uml2.uml.ActivityNode
import org.eclipse.uml2.uml.CallBehaviorAction
import org.eclipse.uml2.uml.SendSignalAction
import org.eclipse.uml2.uml.Signal
import org.eclipse.xtend.lib.annotations.Accessors

class TriggerableSignal {
	@Accessors(#[PUBLIC_GETTER, NONE]) Signal target
	var triggerCausesNodes = new ArrayList<ActivityNode>()
	var triggerCausesString = new ArrayList<String>()
	
	new(Signal target) {
		this.target = target
	}
	
	def getName() {
		return "trig" + UIDGenerator.getUID(target)
	}
	
	def getExpression() {
		var first = true
		var expression = ""
		
		for(an : triggerCausesNodes) {
			if(!first) expression += " | "
			first = false
			
			if(an instanceof CallBehaviorAction) {
				expression += an.name + "." + getName()
			} else if(an instanceof SendSignalAction) {
				expression += an.name + ".TRIGGEROUT"
			} else {
				expression += an.name + ".BEH"
			}
		}
		
		for(str : triggerCausesString) {
			if(!first) expression += " | "
			first = false
			expression += str + "." + getName()
		}
		
		if(first) {
			expression = "FALSE"
			first = false
		}
		
		return expression
	}
	
	def targets(Signal signal) {
		return target == signal
	}
	
	def addTriggerCause(ActivityNode an) {
		if(! triggerCausesNodes.contains(an))
			triggerCausesNodes.add(an)
	}
	
	def addTriggerCause(String str) {
		if(! triggerCausesString.contains(str))
			triggerCausesString.add(str)
	}
}
