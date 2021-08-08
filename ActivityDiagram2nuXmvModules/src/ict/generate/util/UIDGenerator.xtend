package ict.generate.util

import java.util.HashMap

/**
 * Helper class that assigns every object given an ID
 */
class UIDGenerator {
	static long currentID = 0
	static var mappings = new HashMap<Object, Long>()
	
	static def getUID(Object obj) {
		if(mappings.containsKey(obj)) {
			return mappings.get(obj)
		}
		mappings.put(obj, currentID)
		return currentID++
	}
	
	static def getxmiUID(Object obj) {
		var uid = obj.UID
		
		return '''_«String.format("%020d", uid)»'''
	}
	
	static def getNewxmiUID() {
		return (new Object()).getxmiUID
	}
}
