package com.declarations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.aspectj.org.eclipse.jdt.core.dom.Expression;

public class VariableDeclaration {
	
	private String DeclarationType = "";
	private String instanceTypeName = "";
	private final String nw = "new";
	private List<Expression> parameters = null;
	
	public VariableDeclaration() {
		parameters = new ArrayList<Expression>();
	}

	public String getNw() {
		return nw;
	}

	@Override
	public String toString() {
		String result = "";
		result += DeclarationType+' ';
		result += instanceTypeName +" = ";
		result += DeclarationType+'(';
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

	public String getDeclarationType() {
		return DeclarationType;
	}

	public void setDeclarationType(String declarationType) {
		DeclarationType = declarationType;
	}

	public String getInstanceTypeName() {
		return instanceTypeName;
	}

	public void setInstanceTypeName(String instanceTypeName) {
		this.instanceTypeName = instanceTypeName;
	}

	public List<Expression> getParameters() {
		return parameters;
	}

	public void setParameters(List<Expression> parameters) {
		this.parameters = parameters;
	}
	
}
