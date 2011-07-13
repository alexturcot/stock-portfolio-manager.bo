package com.proserus.stocks.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtils {

	static public BigDecimal setDecimalWithScale(BigDecimal value){
		//TODO the Ifs are only because I'm using the method to display all values as tooltip..
		//It is easier to check for Null once here..
		//And not to round numbers that are already smaller than 5 precision.
		if(value == null){
			return null;
		}
		
//		if(value.precision() <= 5){
//			return value;
//		}
		return value.setScale(5,RoundingMode.HALF_EVEN).stripTrailingZeros();
	}
	
	static public String getString(BigDecimal value){
		return setDecimalWithScale(value).toString();
	}
	
}
