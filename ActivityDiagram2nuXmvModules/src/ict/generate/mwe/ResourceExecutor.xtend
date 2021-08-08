package ict.generate.mwe

import java.util.ArrayList
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext
import org.eclipse.xtend.lib.annotations.Accessors

class ResourceExecutor implements IWorkflowComponent {
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) String inSlot = null
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) String outSlot = null
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) IResourceExecutable executable = null
	
	override invoke(IWorkflowContext ctx) {
		var data = ctx.get(inSlot)
		
		if (data === null) {
			throw new IllegalStateException("Slot '" + inSlot + "' was empty!");
		}
		var resources = new ArrayList<Resource>()
		if (data instanceof Iterable) {
			for (Object object2 : data) {
				if (object2 instanceof Resource) {
					resources.add(object2);
				} else {
					throw new IllegalStateException('''
						Slot contents was not a Resource but a '«object2.class.simpleName»'
					''')
				}
			}
		} else if (data instanceof Resource) {
			resources.add(data);
		} else {
			throw new IllegalStateException('''
				Slot contents was not a Resource but a '«data.class.simpleName»'
			''')
		}
		
		executable.run(resources.filter[URI.file].toList, new ISlotWriteback {
			override putSlot(Object o) {
				ctx.put(outSlot, o)
			}
		})
	}
	
	override postInvoke() {
	}
	
	override preInvoke() {
	}
	
	
	static interface IResourceExecutable {
		def void run(List<Resource> resources, ISlotWriteback writeback)
	}
}
