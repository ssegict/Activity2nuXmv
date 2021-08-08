package ict.generate.util

import org.eclipse.xtend.lib.annotations.Accessors

class Pair<V1, V2> {
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) var V1 first
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) var V2 second
	
	new(V1 first, V2 second) {
		this.first = first
		this.second = second
	}
	
	override equals(Object other) {
		if (other === null)
			return false;
		if (this === other)
			return true;
		if (this.getClass().equals(other.getClass())) {
			var otherPair = other as Pair<?, ?>;
			var isEqual = (first === null) ? otherPair.first === null : first.equals(otherPair.first);

			if (!isEqual)
				return false;

			return (second === null) ? otherPair.getSecond() === null : second.equals(otherPair.getSecond());
		}
		return false;
	}

	override hashCode() {
		return first === null ? 0 : first.hashCode() + 17 * (second === null ? 0 : second.hashCode());
	}

	override toString() {
		return '''Pair(«first», «second»)''';
	}
}
