package com.adviceConstructs;

import java.util.ArrayList;
import java.util.List;
import org.aspectj.org.eclipse.jdt.core.dom.ReferencePointcut;

import com.declarations.MethodInvoke;
import com.declarations.Pointcut;
import com.declarations.VariableDeclaration;

/**
 * @author HAKAN
 *
 */
public class AdviceBefore implements Advice {
	
	private String AdviceFileName = "";
	private List<String> parameterTypes = null;
	private final String adviceName = "before";
	private ReferencePointcut pointcut = null;
	private String pointcutName = "";
	private int AdviceLineNumber = 0;
	private String AdviceTypeName = "";
	//source values
	private int SourceLineNumber = 0;
	private String SourceElementName = "";
	
	//for source
	private List<Pointcut> pointcuts = null;
	private List<VariableDeclaration> declaration = null;
	private List<MethodInvoke> methodInvoke = null;
	
	public AdviceBefore() {
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

	public List<Pointcut> getPointcuts() {
		return pointcuts;
	}

	public void setPointcuts(List<Pointcut> pointcuts) {
		this.pointcuts = pointcuts;
	}

}
