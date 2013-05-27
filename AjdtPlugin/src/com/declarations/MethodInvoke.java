package com.declarations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.aspectj.org.eclipse.jdt.core.dom.Expression;

public class MethodInvoke {
	
	public String optionalName = "";
	public String methodName = "";
	public List<Expression> parameters = null;
	public int MethodInvocationLineNumber = 0;
	
	public MethodInvoke() {
		parameters = new ArrayList<Expression>();
	}

	@Override
	public String toString() {
		String result = "";
		result += (optionalName != "" ? optionalName+'.' : "");
		result += methodName;
		result += '(';
		for (Iterator<?> it = parameters.iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			result += e.toString();
			if (it.hasNext()) {
				result += ',';
			}
		}
		result += ')';
		return result;
	}
	
	
}
