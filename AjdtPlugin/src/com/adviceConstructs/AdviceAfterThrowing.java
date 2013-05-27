package com.adviceConstructs;

import java.util.ArrayList;
import java.util.List;
import org.aspectj.org.eclipse.jdt.core.dom.ReferencePointcut;

import com.declarations.MethodInvoke;
import com.declarations.VariableDeclaration;

/**
 * @author HAKAN
 *
 */
public class AdviceAfterThrowing implements Advice {
	
	private String AdviceFileName = "";
	private List<String> parameterTypes = null;
	private final String adviceName = "after";
	private ReferencePointcut pointcut = null;
	private String pointcutName = "";
	private int AdviceLineNumber = 0;
	private String AdviceTypeName = "";
	//source values
	private int SourceLineNumber = 0;
	private String SourceElementName = "";
	
	//for source
	private List<VariableDeclaration> declaration = null;
	private List<MethodInvoke> methodInvoke = null;
	//for exception
	public String ExceptionType = "";
	public String ExceptionName = "";
	
	public AdviceAfterThrowing() {
		setParameterTypes(new ArrayList<String>());
		declaration = new ArrayList<VariableDeclaration>();
		methodInvoke = new ArrayList<MethodInvoke>();
	}

	public String getAdviceName() {
		return adviceName;
	}

	public List<String> getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(List<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public ReferencePointcut getPointcut() {
		return pointcut;
	}

	public void setPointcut(ReferencePointcut pointcut) {
		this.pointcut = pointcut;
	}

	public int getAdviceLineNumber() {
		return AdviceLineNumber;
	}

	public void setAdviceLineNumber(int adviceLineNumber) {
		AdviceLineNumber = adviceLineNumber;
	}

	public String getAdviceTypeName() {
		return AdviceTypeName;
	}

	public void setAdviceTypeName(String adviceTypeName) {
		AdviceTypeName = adviceTypeName;
	}

	public String getPointcutName() {
		return pointcutName;
	}

	public void setPointcutName(String pointcutName) {
		this.pointcutName = pointcutName;
	}

	public List<VariableDeclaration> getDeclaration() {
		return declaration;
	}

	public void setDeclaration(List<VariableDeclaration> declaration) {
		this.declaration = declaration;
	}

	public List<MethodInvoke> getMethodInvoke() {
		return methodInvoke;
	}

	public void setMethodInvoke(List<MethodInvoke> methodInvoke) {
		this.methodInvoke = methodInvoke;
	}

	public int getSourceLineNumber() {
		return SourceLineNumber;
	}

	public void setSourceLineNumber(int sourceLineNumber) {
		SourceLineNumber = sourceLineNumber;
	}

	public String getSourceElementName() {
		return SourceElementName;
	}

	public void setAdviceFileName(String adviceFileName) {
		AdviceFileName = adviceFileName;
	}

	public void setSourceElementName(String sourceElementName) {
		SourceElementName = sourceElementName;
	}

	@Override
	public String getAdviceFileName() {
		return AdviceFileName;
	}

	public String getExceptionType() {
		return ExceptionType;
	}

	public void setExceptionType(String exceptionType) {
		ExceptionType = exceptionType;
	}

	public String getExceptionName() {
		return ExceptionName;
	}

	public void setExceptionName(String exceptionName) {
		ExceptionName = exceptionName;
	}

}
