package ict.generate.mwe

import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext
import org.eclipse.xtend.lib.annotations.Accessors

class GenericExecutor implements IWorkflowComponent {
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) String inSlot = null
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) String outSlot = null
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) IExecutable executable = null
	
	override invoke(IWorkflowContext ctx) {
		var data = ctx.get(inSlot)
		executable.run(data, new ISlotWriteback {
			override putSlot(Object o) {
				ctx.put(outSlot, o)
			}
		})
	}
	
	override postInvoke() {
	}
	
	override preInvoke() {
	}
	
	
	static interface IExecutable {
		def void run(Object source, ISlotWriteback writeback)
	}
}
