package ict.generate.modelToSMV;

import ict.generate.util.ThrowHelper;
import ict.generate.util.ThrowHelper.ThrowType;

public enum ComparatorEnum {
	GREATER_THAN("GreaterThan", ">"), LESS_THAN("LessThan", "<"), EQUAL_TO("EqualTo", "="),
	GREATER_THAN_OR_EQUAL("GreaterThanOrEqualTo", ">="), LESS_THAN_OR_EQUAL("LessThanOrEqualTo", "<=");

	private String comparator = "";
	private String comparatorRep = "";

	private ComparatorEnum(String comparator, String comparatorRep) {
		this.comparator = comparator;
		this.comparatorRep = comparatorRep;
	}

	public static ComparatorEnum getEnum(String value) {
		for (ComparatorEnum v : values())
			if (v.comparator.equalsIgnoreCase(value))
				return v;

		ThrowHelper.customThrow(ThrowType.ERROR,
				new IllegalArgumentException("Value " + value + "has not been found as enum"));
		return null;
	}

	@Override
	public String toString() {
		return this.comparatorRep;
	}
}
