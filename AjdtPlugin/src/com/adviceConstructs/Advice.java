package com.adviceConstructs;

import java.util.List;

import org.aspectj.org.eclipse.jdt.core.dom.ReferencePointcut;

import com.declarations.MethodInvoke;
import com.declarations.VariableDeclaration;

public interface Advice {
		
		String getAdviceName();
		List<String> getParameterTypes();
		ReferencePointcut getPointcut();
		int getAdviceLineNumber();
		String getAdviceTypeName();
		String getPointcutName();
		List<VariableDeclaration> getDeclaration();
		List<MethodInvoke> getMethodInvoke();
		int getSourceLineNumber();
		String getSourceElementName();
		String getAdviceFileName();

}
