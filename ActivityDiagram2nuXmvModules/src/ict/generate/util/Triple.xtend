package ict.generate.util

import org.eclipse.xtend.lib.annotations.Accessors

class Triple<V1, V2, V3> extends Pair<V1, V2> {
	@Accessors(#[PUBLIC_GETTER, PUBLIC_SETTER]) var V3 third
	
	new(V1 first, V2 second, V3 third) {
		super(first, second)
		this.third = third
	}
	
	override equals(Object other) {
		if (other === null)
			return false;
		if (this === other)
			return true;
		if (this.getClass().equals(other.getClass())) {
			var r = other as Triple<?,?,?>;
			if (!super.equals(other))
				return false;
			return third === null ? r.getThird() === null : third.equals(r.getThird());
		}
		return false;
	}

	override hashCode() {
		return super.hashCode()+ 17*(third === null? 0 : third.hashCode());
	}

	override toString() {
		return '''Triple(«first», «second», «third»)''';
	}
}
