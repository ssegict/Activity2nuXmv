package ict.generate.modelToSMV;

import ict.generate.mwe.ISlotWriteback
import ict.generate.mwe.ResourceExecutor.IResourceExecutable
import ict.generate.util.FileAccess
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.uml2.uml.Activity
import org.eclipse.uml2.uml.Model
import org.eclipse.xtend.lib.annotations.Accessors

import static extension ict.generate.modelToSMV.CodeGeneratorUtil.*

class SMVGenerator implements IResourceExecutable {
	@Accessors var String  outputPath = ""
	
	override run(List<Resource> resources, ISlotWriteback writeback) {
		for(resource : resources) {
			var splited = resource.URI.path.split("/")
			val name = splited.get(splited.size - 1)
			println(name);
			
			var generated = ""
			for(m : resource.contents.filter(Model)) {
				if(generated.length > 0) {
					generated += "\n\n"
				}
				
				generated += m.compile(name)
			}
			if(generated.length > 0) {
				var resPath = '''«outputPath»/«name».smv'''
				FileAccess.writeFile(resPath, generated)
			}
		}
	}
	
	def compile(Model m, String name) {
		var data = ""
		
		if(m.containsModules) {
			data += SoftwareCodeGenerator.compileActivityModel(name, m)
		}
		
		if(m.containsOLC) {
			data += OLCCodeGenerator.compileSTMModel(m)
		}
		
		return data
	}
	
	def void recursivePrint(EObject obj, String prefix, String seperator) {
		print(prefix)
		println(obj)
		if(obj instanceof Activity) {
			var a = obj as Activity;
			for(EObject m : a.getNodes()) {
				recursivePrint(m, prefix + seperator, seperator);
			}
		}
		for(EObject m : obj.eContents) {
			recursivePrint(m, prefix + seperator, seperator);
		}
	}
}
