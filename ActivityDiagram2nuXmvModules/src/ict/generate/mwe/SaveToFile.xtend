package ict.generate.mwe

import ict.generate.mwe.GenericExecutor.IExecutable
import org.eclipse.xtend.lib.annotations.Accessors

/**
 * Slot contents need to have a function
 * save(String path)
 * otherwise noSuchMethodError will be thrown
 */
class SaveToFile extends GenericExecutor implements IExecutable {
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) var String saveTo = ""
	
	new() {
		super()
		executable = this
	}
	
	override run(Object source, ISlotWriteback writeback) {
		if(source === null) {
			throw new NullPointerException("Cannot save a null object to disk")
		}
		//This will throw an error if method does not exist
		//-> no additional checking needed
		var saveMeth = source.class.getMethod("save", #[typeof(String)])
		saveMeth.invoke(source, saveTo)
	}
}
