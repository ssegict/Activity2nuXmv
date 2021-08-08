package ict.generate.util

class ThrowHelper {
		
	enum ThrowType {
		WARN,
		ERROR
	}
	
	def static customThrow(ThrowType type, RuntimeException ex) {
		if(type == ThrowType.WARN) {
			ex.printStackTrace
		} else {
			throw ex;
		}
	}
	
}
