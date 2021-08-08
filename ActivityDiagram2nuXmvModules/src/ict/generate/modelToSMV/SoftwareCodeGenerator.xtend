package ict.generate.modelToSMV;

import ict.generate.util.ThrowHelper
import ict.generate.util.ThrowHelper.ThrowType
import ict.generate.util.UIDGenerator
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.uml2.uml.AcceptEventAction
import org.eclipse.uml2.uml.Activity
import org.eclipse.uml2.uml.ActivityFinalNode
import org.eclipse.uml2.uml.ActivityPartition
import org.eclipse.uml2.uml.ActivityNode
import org.eclipse.uml2.uml.CallBehaviorAction
import org.eclipse.uml2.uml.ControlFlow
import org.eclipse.uml2.uml.DecisionNode
import org.eclipse.uml2.uml.ExceptionHandler
import org.eclipse.uml2.uml.ExecutableNode
import org.eclipse.uml2.uml.FlowFinalNode
import org.eclipse.uml2.uml.ForkNode
import org.eclipse.uml2.uml.InitialNode
import org.eclipse.uml2.uml.JoinNode
import org.eclipse.uml2.uml.MergeNode
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.NamedElement
import org.eclipse.uml2.uml.OpaqueAction
import org.eclipse.uml2.uml.SendSignalAction
import org.eclipse.uml2.uml.SignalEvent

import static extension ict.generate.modelToSMV.CodeGeneratorUtil.*
import org.eclipse.uml2.uml.TimeEvent
import org.eclipse.uml2.uml.Trigger

class SoftwareCodeGenerator {
	static def compileActivityModel(String fileName, Model m) {
		'''
			-- Module Compilation of «m.name»
			«FOR a : m.eContents.filter(Activity)»
				«a.compileActivity»
			«ENDFOR»
		'''
	}
	
	static def compileActivity(Activity a) {
		var regionMap = new HashMap<ActivityPartition, List<String>>()
		
		if(a.nodes.filter(InitialNode).size > 1) {
			ThrowHelper.customThrow(ThrowType.ERROR, new IllegalArgumentException('''
				Currently there is only one InitialNode per Activity supported
				«a.nodes.filter(InitialNode).size» found at "«a.name»"
				Please use Fork Nodes
			'''))
		}
		if(a.nodes.filter(ActivityFinalNode).empty) {
			ThrowHelper.customThrow(ThrowType.WARN, new IllegalArgumentException('''
				The activity "«a.name»" does not contain any AcitivityFinalNodes
			'''))
		}
		if(a.nodes.filter(ActivityFinalNode).size > 1) {
			ThrowHelper.customThrow(ThrowType.ERROR, new IllegalArgumentException('''
				Currently there is only one ActivityFinalNode per Activity supported
				«a.nodes.filter(ActivityFinalNode).size» found at "«a.name»"
				Please use Merge Nodes
			'''))
		}
		
		'''
			MODULE «a.name»(controlFlowIn, controlFlowOut, container, resetExt)
				VAR
					«FOR partition : a.groups.filter(ActivityPartition)»
						«partition.name»Decision: 0..«partition.nodes.filter(ExecutableNode).map[handlers].flatten.size»;
					«ENDFOR»
					«FOR contFlow : a.eContents.filter(ControlFlow)»
						«contFlow.source.name»to«contFlow.target.name»: ControlFlow();
					«ENDFOR»
					«FOR node : a.nodes.filter(ExecutableNode)»
						«FOR handler : node.handlers»
							«node.name»to«node.name»Intercepter«UIDGenerator.getUID(handler)»: ControlFlow();
							«node.name»Intercepter«UIDGenerator.getUID(handler)»to«handler.handlerBody.name»: ControlFlow();
						«ENDFOR»
					«ENDFOR»
					
					«FOR an : a.nodes.distinct»
						«an.moduleInitialisation»
					«ENDFOR»
					«FOR node : a.nodes.filter(ExecutableNode)»
						«FOR handler : node.handlers»
							«generateFlowInterceptor(node, handler, regionMap)»
						«ENDFOR»
					«ENDFOR»
					«FOR trig : a.nodes.filter(AcceptEventAction).map[triggers].flatten.filter[event instanceof TimeEvent]»
						«generateTimeTrigger(trig)»
					«ENDFOR»
				
				«FOR entry : regionMap.entrySet»
					INVAR («entry.key.name»Decision != 0 | !«entry.key.name»ExceptionFlow) & («entry.key.name»Decision = 0 | «entry.key.name»ExceptionFlow)
					«FOR inter : entry.value»
						INVAR («entry.key.name»Decision != «entry.value.indexOf(inter) + 1» | «inter».WAITING);
					«ENDFOR»
				«ENDFOR»
				
				ASSIGN
					controlFlowIn.backwardSignal := controlFlowIn.forwardSignal & «a.nodes.findFirst[it instanceof InitialNode].name».ACTIVE;
					«IF a.nodes.findFirst[it instanceof ActivityFinalNode] !== null»
						controlFlowOut.forwardSignal := «a.nodes.findFirst[it instanceof ActivityFinalNode].name».FINISHED;
					«ELSE»
						controlFlowOut.forwardSignal := FALSE;
					«ENDIF»
			
				DEFINE
					«FOR s : TriggerableSignalHolder.singleton.getSignals(a)»
						«s.name» := «s.expression»;
					«ENDFOR»
					«FOR partition : a.groups.filter(ActivityPartition)»
						«partition.name»Reset := reset | «partition.nodes.filter(ExecutableNode)
						.filter[! handlers.empty].map[name + ".RESET_OUT"].join(" | ")»;
						«partition.name»IntercepterReset := reset | «partition.nodes.filter(ExecutableNode)
						.map[handlers].flatten.map[(eContainer as NamedElement).name + "Intercepter" +
						 UIDGenerator.getUID(it) + ".RESET_OUT"].join(" | ")»;
						«partition.name»ExceptionFlowContinue := «partition.nodes.filter(ExecutableNode)
 						.map[handlers].flatten.map[(eContainer as NamedElement).name + "Intercepter" +
 						 UIDGenerator.getUID(it) + ".ALLOWED"].join(" | ")»;
						«partition.name»ExceptionFlow := !«partition.name»ExceptionFlowContinue &( «partition.nodes
						.filter(ExecutableNode).map[handlers].flatten.map[(eContainer as NamedElement).name +
						"Intercepter" + UIDGenerator.getUID(it) + ".WAITING"].join(" | ")»);
						«partition.name»Active := «partition.nodes
						.filter[!(it instanceof ExecutableNode && !(it as ExecutableNode).handlers.empty)]
						.toActive.join(" | ")»;
					«ENDFOR»
					SUB_ACTIVE := «a.allActiveElmsNames.join(" | ")»;
					reset := resetExt | controlFlowOut.backwardSignal;
			
		'''
	}
	
	static def dispatch moduleInitialisation(InitialNode an) {
		an.checkInOut(0, 1);
		'''«an.name»: InitialPoint(controlFlowIn.forwardSignal, «an.outputSignal(0)», «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(OpaqueAction an) {
		an.checkInOut(1, 1);
		return '''«an.name»: Action(«an.preCondition», «an.postCondition», «an.inputSignal(0)», «an.outputSignal(0)», «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(ActivityFinalNode an) {
		an.checkInOut(1, 0);
		'''«an.name»: EndPoint(controlFlowOut.backwardSignal, «an.inputSignal(0)», «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(FlowFinalNode an) {
		an.checkInOut(1, 0);
		'''«an.name»: FlowEndPoint(«an.inputSignal(0)», «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(MergeNode an) {
		an.checkInOut(2, 1);
		'''«an.name»: Merge(«an.inputSignal(0)», «an.inputSignal(1)», «an.outputSignal(0)», «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(ForkNode an) {
		an.checkInOut(1, 2);
		'''«an.name»: Fork(«an.inputSignal(0)», «an.outputSignal(0)», «an.outputSignal(1)», «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(JoinNode an) {
		an.checkInOut(2, 1);
		'''«an.name»: Join(«an.inputSignal(0)», «an.inputSignal(1)», «an.outputSignal(0)», «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(CallBehaviorAction an) {
		an.checkInOut(1, 1);
		'''«an.name»: «an.behavior.name»(«an.inputSignal(0)», «an.outputSignal(0)», container, «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(SendSignalAction an) {
		an.checkInOut(1, 1);
		an.checkSignal
		return '''«an.name»: SendAction(«an.preCondition», «an.postCondition», «an.inputSignal(0)», «an.outputSignal(0)», «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(AcceptEventAction an) {
		var partitions = an.inPartitions
		if(partitions.size > 1) {
			ThrowHelper.customThrow(ThrowType.ERROR, new UnsupportedOperationException('''
				Action seems to be in multiple partions "«an.name»"
				currently only one is supported
			'''))
			return ""
		} else if(partitions.size == 1) {
			if(an.incomings.size == 0) {
				an.checkInOut(0, 1);
				if(an.preCondition != "TRUE") {
					ThrowHelper.customThrow(ThrowType.ERROR, new UnsupportedOperationException('''
						Preconditions are not supported when combined with Exception handlers at "«an.name»"
					'''))
				}
				if(an.postCondition != "TRUE") {
					ThrowHelper.customThrow(ThrowType.ERROR, new UnsupportedOperationException('''
						Postconditions are not supported when combined with Exception handlers at "«an.name»"
					'''))
				}
				//this is inside a region so incomings are not really needed
				var partition = partitions.get(0)
				return '''«an.name»: LeaveRegionAction(«partition.name»Active, «an.outputSignal(0)», «an.triggerSigs»);'''
			}
		}
		an.checkInOut(1, 1);
		return '''«an.name»: ReceiveAction(«an.preCondition», «an.postCondition», «an.triggerSigs», «an.inputSignal(0)», «an.outputSignal(0)», «an.reset»);'''
	}
	
	static def dispatch moduleInitialisation(DecisionNode an) {
		an.checkInOut(1, 2);
		return '''«an.name»: Decision(«an.conditionFor(0)», «an.conditionFor(1)», «an.inputSignal(0)», «an.outputSignal(0)», «an.outputSignal(1)», «an.reset»);'''
	}
	
	static def inputSignal(ActivityNode an, int index) {
		var internalIdx = index
		var handlerIncomings = an.findIncomings
		if(internalIdx >= handlerIncomings.size) {
			internalIdx -= handlerIncomings.size
		} else {
			var inc = handlerIncomings.get(internalIdx)
			return '''«inc.first.name»Intercepter«UIDGenerator.getUID(inc.second)»to«inc.second.handlerBody.name»'''
		}
		return (an.incomings.get(index) as ControlFlow).source.name + "to" + an.name
	}
	
	static def outputSignal(ActivityNode an, int index) {
		var internalIdx = index
		if(an instanceof ExecutableNode) {
			if(internalIdx >= an.handlers.size) {
				internalIdx -= an.handlers.size
			} else {
				return '''«an.name»to«an.name»Intercepter«UIDGenerator.getUID(an.handlers.get(internalIdx))»'''
			}
		}
		return an.name + "to" + (an.outgoings.get(internalIdx) as ControlFlow).target.name
	}
	
	static def preCondition(ActivityNode an) {
		//use Pre and Post Condition here
		var semspec = an.getAppliedStereotype("semanticSepcification::ActionWithSemanticSpecification")
		var preString = "TRUE"
		
		if(semspec !== null) {
			var preCondition = an.getValue(semspec, "preCondition") as EObject
			if(preCondition !== null) {
				preString = preCondition.buildStringForConstraint()
			}
		}
		return preString
	}
	
	static def postCondition(ActivityNode an) {
		//use Pre and Post Condition here
		var semspec = an.getAppliedStereotype("semanticSepcification::ActionWithSemanticSpecification")
		var postString = "TRUE"
		
		if(semspec !== null) {
			var postCondition = an.getValue(semspec, "postCondition") as EObject
			if(postCondition !== null) {
				postString = postCondition.buildStringForConstraint()
			}
		}
		return postString
	}
	
	static def triggerSigs(AcceptEventAction an) {
		var triggers = ""
		for(trig : an.triggers) {
			var event = trig.event
			
			if(event instanceof SignalEvent) {
				if(triggers.length > 0) triggers+= " | "
				TriggerableSignalHolder.singleton.addSignal(event.signal)
				
				triggers += '''container.trig«UIDGenerator.getUID(event.signal)»'''
			} else if(event instanceof TimeEvent) {
				if(triggers.length > 0) triggers+= " | "
				triggers += '''«an.name»Time«UIDGenerator.getUID(event)».TRIG_OUT'''
			} else {
				ThrowHelper.customThrow(ThrowType.WARN, new UnsupportedOperationException('''
					Unsupported Event found at «an.name» type «event.class.canonicalName»
				'''))
			}
		}
		return triggers
	}
	
	def static generateTimeTrigger(Trigger trigger) {
		var an = trigger.eContainer as AcceptEventAction
		var trigName = '''«an.name»Time«UIDGenerator.getUID(trigger.event)»'''
		
		if(an.inPartitions.empty) {
			ThrowHelper.customThrow(ThrowType.ERROR, new UnsupportedOperationException('''
				AcceptEventAction must be inside partition in order to use TimeoutEvent
			'''))
			return ""
		}
		var partitions = an.inPartitions
		if(partitions.size > 1) {
			ThrowHelper.customThrow(ThrowType.ERROR, new UnsupportedOperationException('''
				Action seems to be in multiple partions "«an.name»"
				currently only one is supported
			'''))
			return ""
		}
		var partition = partitions.get(0)
		var event = trigger.event as TimeEvent
		
		return '''«trigName»: TimeTrigger(«partition.name»Active, «event.when.expr.integerValue»);'''
	}
	
	static def conditionFor(DecisionNode an, int index) {
		val flow = an.outgoings.get(index) as ControlFlow
		
		var stereotype = flow.getAppliedStereotype("semanticSepcification::ControlFlowWithConstraint")
		if(stereotype === null) {
			return "TRUE"
		}
		
		var constraint = flow.getValue(stereotype, "constraint") as EObject
		return constraint.buildStringForConstraint
	}
	
	static def checkInOut(ActivityNode an, int incomings, int outgoings) {
		if(an.incomings.size + an.findIncomings.size != incomings) {
			ThrowHelper.customThrow(ThrowType.WARN, new IllegalArgumentException('''
				Wrong number of incomings found at «an.name»
				expoected «incomings» got «an.incomings.size» for type «an.class.canonicalName»
			'''))
		}
		
		var exceptionOutgoings = 0
		if(an instanceof ExecutableNode) {
			exceptionOutgoings += an.handlers.size
		}
		
		if(an.outgoings.size + exceptionOutgoings != outgoings) {
			ThrowHelper.customThrow(ThrowType.WARN, new IllegalArgumentException('''
				Wrong number of outgoings found at «an.name»
				expoected «outgoings» got «an.outgoings.size» for type «an.class.canonicalName»
			'''))
		}
	}
	
	static def getReset(ActivityNode an) {
		if(an.inPartitions.empty) {
			return "reset"
		}
		var partitions = an.inPartitions
		if(partitions.size > 1) {
			ThrowHelper.customThrow(ThrowType.ERROR, new UnsupportedOperationException('''
				Action seems to be in multiple partions "«an.name»"
				currently only one is supported
			'''))
			return ""
		}
		var partition = partitions.get(0)
		return '''«partition.name»Reset'''
	}
	
	/**
	 * Check that an signal is attached to the SendSignalAction
	 */
	static def checkSignal(SendSignalAction an) {
		if(an.signal === null) {
			ThrowHelper.customThrow(ThrowType.WARN, new IllegalArgumentException('''
				No Signal found for SendSignalAction at «an.name» type «an.class.canonicalName»
			'''))
		}
	}
	
	def static generateFlowInterceptor(ExecutableNode node, ExceptionHandler handler, Map<ActivityPartition, List<String>> lastRegion) {
		if(node.inPartitions.empty) {
			return "reset"
		}
		var partitions = node.inPartitions
		if(partitions.size > 1) {
			ThrowHelper.customThrow(ThrowType.ERROR, new UnsupportedOperationException('''
				Action seems to be in multiple partions "«node.name»"
				currently only one is supported
			'''))
			return ""
		}
		var partition = partitions.get(0)
		
		var mapData = lastRegion.get(partition)
		if(mapData === null) {
			mapData = new ArrayList<String>()
			lastRegion.put(partition, mapData)
		}
		
		var interName = '''«node.name»Intercepter«UIDGenerator.getUID(handler)»'''
		mapData.add(interName)
		var srcFlow = '''«node.name»to«interName»'''
		var targetFlow = '''«interName»to«handler.handlerBody.name»'''
		var allow = '''«partition.name»Decision = «mapData.size»'''
		var reset = '''«partition.name»IntercepterReset'''
		
		return '''
			«interName»: ControlFlowIntercepter(«srcFlow», «targetFlow», «allow», «reset»);
		'''
	}
	
	def static allActiveElmsNames(Activity a) {
		var names = new ArrayList<String>()
		
		for(node : a.nodes) {
			if(node instanceof CallBehaviorAction) {
				names.add(node.name + ".SUB_ACTIVE")
			} else {
				names.add(node.name + ".ACTIVE")
			}
			if(node instanceof ExecutableNode) {
				for(handler : node.handlers) {
					names.add('''«node.name»Intercepter«UIDGenerator.getUID(handler)».ACTIVE''')
				}
			}
		}
		return names
	}
	
	def static toActive(Iterable<ActivityNode> nodes) {
		var results = new ArrayList<String>()
		
		for(node : nodes) {
			if(node instanceof CallBehaviorAction) {
				results.add(node.name + ".SUB_ACTIVE")
			} else {
				results.add(node.name + ".ACTIVE")
			}
		}
		
		return results
	}
}
