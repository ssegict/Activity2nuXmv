package ict.generate.modelToSMV;

import ict.generate.util.Pair
import ict.generate.util.ThrowHelper
import ict.generate.util.ThrowHelper.ThrowType
import ict.generate.util.UIDGenerator
import java.util.ArrayList
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.uml2.uml.Action
import org.eclipse.uml2.uml.Activity
import org.eclipse.uml2.uml.ActivityNode
import org.eclipse.uml2.uml.CallBehaviorAction
import org.eclipse.uml2.uml.Constraint
import org.eclipse.uml2.uml.ExceptionHandler
import org.eclipse.uml2.uml.ExecutableNode
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.NamedElement
import org.eclipse.uml2.uml.OpaqueExpression
import org.eclipse.uml2.uml.Property
import org.eclipse.uml2.uml.Region
import org.eclipse.uml2.uml.State
import org.eclipse.uml2.uml.StateMachine
import org.eclipse.uml2.uml.Vertex

class CodeGeneratorUtil {
	static def <T> distinct(Iterable<T> in) {
		var unique = new ArrayList<T>()
		for(T elm : in) {
			if(! unique.contains(elm)) {
				unique.add(elm)
			}
		}
		return unique
	}

	val static AND_CONSTRAINT = "AndConstraint"
	val static OR_CONSTRAINT = "OrConstraint"
	val static NOT_CONSTRAINT = "NotConstraint"
	val static CONSTRAINT = "Constraint"
	val static VALUE_CONSTRAINT = "ValueConstraint"
	val static OBJECTLIFECYCLE_CONSTRAINT = "ObjectLifeCycleConstraint"
	val static STATE_STRUCTURAL_FEATURE = "vertex"
	val static CONSTRAINTS_STRUCTURAL_FEATURE = "constraints"
	val static ATTRIBUTE_STRUCTURAL_FEATURE = "attribute"
	val static VALUE_STRUCTURAL_FEATURE = "value"
	val static COMPARATOR_STRUCTURAL_FEATURE = "comperator"

	static def String buildStringForConstraint(EObject obj) {
		switch obj.eClass.name {
			case OBJECTLIFECYCLE_CONSTRAINT: {
				// the value of the structural feature is always a vertex
				var state = obj.eGet(obj.eClass.getEStructuralFeature(STATE_STRUCTURAL_FEATURE)) as Vertex;
				return buildConditionForState(state)
			}
			case VALUE_CONSTRAINT: {
				var property = obj.eGet(obj.eClass().getEStructuralFeature(ATTRIBUTE_STRUCTURAL_FEATURE)) as Property;
				var value = obj.eGet(obj.eClass().getEStructuralFeature(VALUE_STRUCTURAL_FEATURE)) as String;
				var comparatorObj = obj.eGet(obj.eClass().getEStructuralFeature(COMPARATOR_STRUCTURAL_FEATURE));
				var comp = ComparatorEnum.getEnum(comparatorObj.toString());
				return '''(«property.generatePath»«property.name»«UIDGenerator.getUID(property)» «comp» «value»)''';
			}
			case AND_CONSTRAINT: {
				var andConstraints = obj.eGet(obj.eClass().getEStructuralFeature(CONSTRAINTS_STRUCTURAL_FEATURE)) as EList<EObject>;
				return '''(«andConstraints.map[buildStringForConstraint].join(" & ")»)'''
			}
			case OR_CONSTRAINT: {
				//TODO test this
				var orConstraints = obj.eGet(obj.eClass().getEStructuralFeature(CONSTRAINTS_STRUCTURAL_FEATURE)) as EList<EObject>;
				return '''(«orConstraints.map[buildStringForConstraint].join(" | ")»)'''
			}
			case NOT_CONSTRAINT: {
				var notConstraint = obj.eGet(obj.eClass().getEStructuralFeature(CONSTRAINTS_STRUCTURAL_FEATURE)) as EObject;
				return '''!(«notConstraint.buildStringForConstraint»)'''
			}
			case CONSTRAINT: {
				var constr = obj as Constraint
				var element = constr.constrainedElements.get(0) as State
				var spec = constr.specification as OpaqueExpression
				return '''(«element.container.generatePath»«element.container.name» = «element.name») = «spec.bodies.get(0).toUpperCase»'''
			}
			default: {
				//Should never reach this
				ThrowHelper.customThrow(ThrowType.ERROR, new UnsupportedOperationException(
					'''Unknown class name "«obj.eClass.name»"'''
				))
				return "";
			}
		}
	}
	
	static def String buildConditionForState(Vertex state) {
		'''(«state.container.generatePath»«state.container.name» = «state.name»)'''
	}
	
	static def dispatch generatePath(Region r) {
		'''container.olc.«r.containingStateMachine.name»obj.'''
	}
	
	static def dispatch generatePath(Property p) {
		//seems that this can only be inside a class -> always olc
		'''container.olc.'''
	}
	
	static def dispatch String generatePath(Action a) {
		//do path traversal forwards because this is easier to implement
		var path = recursiveFindPath(getMainActivity(a), a);
		if(path === null) {
			ThrowHelper.customThrow(ThrowType.ERROR, new IllegalArgumentException(
				'''Unable to generate path for activity «a.name»'''
			))
		}
		'''container.mainAct«recursiveFindPath(getMainActivity(a), a)»'''
	}
	
	static def containsModules(Model m) {
		return ! m.ownedElements.filter(Activity).empty
	}
	
	static def containsOLC(Model m) {
		return ! m.ownedElements.filter(StateMachine).empty
	}
	
	static def String recursiveFindPath(Activity findIn, Action toFind) {
		for(an : findIn.nodes.distinct) {
			if(an == toFind) return '''.«an.name»'''
			
			if(an instanceof CallBehaviorAction) {
				var subFind = recursiveFindPath(an.behavior as Activity, toFind)
				if(subFind !== null) {
					return '''.«an.name»«subFind»'''
				}
			}
		}
		return null
	}
	
	def static getMainActivity(EObject anyObject) {
		// go up to resourceSet and from there on down till mainActivity
		var resources = anyObject.eResource.resourceSet.resources
		return getMainActivity(resources)
	}
	
	def static getMainActivity(Iterable<Resource> resources) {
		// go up to resourceSet and from there on down till mainActivity
		for(r : resources) {
			var mainAct = r.contents.filter(Model).map[eContents].flatten.filter(Activity).filter[name.equals("MainActivity")]
			if(!mainAct.empty) {
				return mainAct.get(0)
			}
		}
		ThrowHelper.customThrow(ThrowType.ERROR, new IllegalArgumentException(
			'''No Activity named MainActivity found in given resources'''
		))
	}
	
	static def EObject findByNamePath(Iterable<EObject> haystack, String needlePath) {
		findByNamePath(haystack, needlePath, true)
	}
	
	static def EObject findByNamePath(Iterable<EObject> haystack, String needlePath, boolean canReturnNull) {
		var nextSep = needlePath.indexOf('.')
		if(nextSep == -1) {
			nextSep = needlePath.length
		}
		val nextElm = needlePath.substring(0, nextSep)
		var remaining = (nextSep < needlePath.length - 2) ? (needlePath.substring(nextSep + 1)) : ("")
		var possible = haystack.filter(NamedElement).filter[name == nextElm]
		if(possible.empty) {
			if(canReturnNull) {
				return null
			}
			ThrowHelper.customThrow(ThrowType.ERROR, new IllegalArgumentException('''
				Element needs to be found, but has not been found
				Elements in stack:
				«haystack.filter(NamedElement).map[name].join(", ")»
				Searched for: «nextElm»
			'''))
		}
		if(remaining.length == 0) {
			return possible.get(0)
		}
		return findByNamePath(possible.map[eContents].flatten, remaining)
	}
	
	static def findIncomings(ActivityNode an) {
		var incomings = new ArrayList<Pair<ActivityNode, ExceptionHandler>>()
		
		if(an instanceof ExecutableNode) {
			var act = an.activity
			for(node : act.nodes.filter(ExecutableNode)) {
				for(handler : node.handlers) {
					if(handler.handlerBody == an) {
						incomings.add(new Pair(node, handler))
					}
				}
			}
		}
		return incomings
	}
}
