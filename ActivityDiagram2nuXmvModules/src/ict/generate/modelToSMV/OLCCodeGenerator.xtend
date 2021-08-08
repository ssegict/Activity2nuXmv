package ict.generate.modelToSMV;

import ict.generate.util.ThrowHelper
import ict.generate.util.ThrowHelper.ThrowType
import ict.generate.util.UIDGenerator
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Pseudostate
import org.eclipse.uml2.uml.Region
import org.eclipse.uml2.uml.SignalEvent
import org.eclipse.uml2.uml.State
import org.eclipse.uml2.uml.StateMachine
import org.eclipse.uml2.uml.Transition
import org.eclipse.uml2.uml.Vertex

import static extension ict.generate.modelToSMV.CodeGeneratorUtil.*

class OLCCodeGenerator {
	static def compileSTMModel(Model model) {
		'''
			-- STM Compilation of «model.name»
			«FOR stm : model.eContents.filter(StateMachine)»
				«stm.compile»
			«ENDFOR»
		'''
	}
	
	static def compile(StateMachine stm) {
		stm.regions.map[transitions].flatten.map[triggers].flatten.map[event].filter(SignalEvent).forEach[
			TriggerableSignalHolder.singleton.addSignal(signal)
		]
		
		return '''
			MODULE «stm.name»(container)
				VAR
					«stm.generateVarSection»
				
				DEFINE
					«stm.generateDefineSection»
				
				ASSIGN
					«stm.generateASSIGNSection»
		'''
	}
	
	static def generateVarSection(StateMachine stm) {
		'''
			«FOR r : stm.eContents.filter(Region)»
				«r.name» : {«r.states»};
			«ENDFOR»
		'''
	}
	
	static def states(Region r) {
		var vertex = r.eContents.filter(Vertex)
		var notState = vertex.filter[!(it instanceof State || it instanceof Pseudostate)]
		if(! notState.empty) {
			ThrowHelper.customThrow(ThrowType.WARN, new IllegalArgumentException('''
				Unknown vertex Type(s) should be State or Pseudostate
				«FOR vert : notState»
					«vert.name»: «vert.class.canonicalName»
				«ENDFOR»
			'''))
		}
		
		return vertex
				.filter[it instanceof State || it instanceof Pseudostate]
				.map[name].join(", ") + 
				(r.needsErrorSignal?", error":"")
	}
	
	static def generateDefineSection(StateMachine stm) {
		'''
			«FOR r : stm.regions.filter[needsErrorSignal]»
				no_signal_«r.name» := !(«r.transitions.map[triggers].flatten.map[event].filter(SignalEvent).map['''container.trig«UIDGenerator.getUID(signal)»'''].distinct.join(" | ")»);
			«ENDFOR»
			«IF stm.regions.filter[needsErrorSignal].empty»
				ERROR := FALSE;
			«ELSE»
				ERROR := «stm.regions.filter[needsErrorSignal].map['''(«name» = error)'''].join(" | ")»;
			«ENDIF»
		'''
	}
	
	static def needsErrorSignal(Region r) {
		return r.name.endsWith("_error")
	}
	
	static def generateASSIGNSection(StateMachine stm) {
		'''
			«FOR region : stm.eContents.filter(Region)»
				«region.generateImplementation»
				
			«ENDFOR»
		'''
	}
	
	static def generateImplementation(Region r) {
		var startStates = r.eContents.filter(Pseudostate).filter[incomings.empty]
		if(startStates.size > 1) {
			ThrowHelper.customThrow(ThrowType.WARN, new IllegalArgumentException('''
				Multiple possible start nodes found at «r.name»
				«FOR start : startStates»
					«start.name»: «start.class.canonicalName»
				«ENDFOR»
			'''))
		}
		if(startStates.size < 1) {
			ThrowHelper.customThrow(ThrowType.WARN, new IllegalArgumentException('''
				No start Nodes found at «r.name»
				A start Node must not contain any incoming Transitions
			'''))
		}
		'''
			init(«r.name») := «r.eContents.filter(Pseudostate).filter[incomings.empty].get(0).name»;
			next(«r.name») :=
				case
					«FOR trans : r.eContents.filter(Transition)»
						(«r.name» = «trans.source.name») «trans.getConstraints»: «trans.target.name»;
					«ENDFOR»
					«IF r.needsErrorSignal»
						no_signal_«r.name» : «r.name»;
						TRUE : error;
					«ELSE»
						TRUE : «r.name»;
					«ENDIF»
				esac;
		'''
	}
	
	static def getConstraints(Transition t) {
		var triggers = ""
		for(trig : t.triggers) {
			var event = trig.event
			
			if(event instanceof SignalEvent) {
				if(triggers.length > 0) triggers+= " | "
				
				triggers += '''container.trig«UIDGenerator.getUID(event.signal)»'''
			}
		}
		
		var guard = ""
		if(t.guard !== null) {
			guard = t.guard.buildStringForConstraint()
		}
		
		var constraints = triggers
		if(triggers.length > 0 && guard.length > 0) {
			constraints = '''(«constraints») &'''
		}
		constraints += guard
		
		if(constraints.length > 0) {
			return '''& («constraints»)'''
		}
		return ""
	}
	
}
