package ict.generate.modelToSMV

import ict.generate.mwe.ISlotWriteback
import ict.generate.mwe.ResourceExecutor.IResourceExecutable
import ict.generate.util.FileAccess
import ict.generate.util.UIDGenerator
import java.io.File
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Property
import org.eclipse.uml2.uml.StateMachine
import org.eclipse.xtend.lib.annotations.Accessors

import static extension ict.generate.modelToSMV.CodeGeneratorUtil.*
import java.util.ArrayList

class MainCodeGenerator implements IResourceExecutable {
	@Accessors var String basePath = ""
	
	override run(List<Resource> resources, ISlotWriteback writeback) {
		var stateMachines = resources.map[contents].flatten().filter(Model).map[eContents].flatten().filter(StateMachine)
		
		FileAccess.writeFile(basePath + "/main.smv", '''
			#include "../modules.h"
			#include "../Property_state_machine.h"
			«FOR r : resources.filter[new File(basePath + "/" + URI.path.split("/").last + ".smv").file]»
				#include "«r.URI.path.split("/").last».smv"
			«ENDFOR»
			
			«FOR include : includes»
				#include "«include»"
			«ENDFOR»
			
			MODULE ObjectLifeCycle(container)
				VAR
					«FOR stm : stateMachines»
						«stm.name»obj : «stm.name»(container);
					«ENDFOR»
					«FOR attr : resources.map[contents].flatten().filter(Model).map[eContents].flatten().filter(Class)
					.map[eContents].flatten().filter(Property).filter[!readOnly]»
						«attr.name»«UIDGenerator.getUID(attr)»: «attr.lowerValue.integerValue»..«attr.upperValue.integerValue»;
					«ENDFOR»
				
				FROZENVAR
					«FOR attr : resources.map[contents].flatten().filter(Model).map[eContents].flatten().filter(Class)
					.map[eContents].flatten().filter(Property).filter[readOnly]»
						«attr.name»«UIDGenerator.getUID(attr)»: «attr.lowerValue.integerValue»..«attr.upperValue.integerValue»;
					«ENDFOR»
				
				DEFINE
					«IF stateMachines.empty»
						ERROR := FALSE;
					«ELSE»
						ERROR := «stateMachines.map[name + "obj.ERROR"].join(" | ")»;
					«ENDIF»
			
			MODULE main
				VAR
					reset : boolean;
					forward : ControlFlow();
					back : ControlFlow();
					
					activator : MainActivator(forward);
					mainAct : MainActivity(forward, back, self, reset);
					terminator : MainTerminator(back);
					
					olc : ObjectLifeCycle(self);
					
					  	
				ASSIGN
					reset := FALSE;
				
				DEFINE
					«FOR s : TriggerableSignalHolder.singleton.getMainSignals(resources.getMainActivity)»
						«s.name» := «s.expression»;
					«ENDFOR»
		''')
	}
	
	var includes = new ArrayList<String>();
	def addInclude(String include) {
		if(! includes.contains(include))
			includes.add(include)
	}
}
