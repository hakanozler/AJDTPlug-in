package com.declarations;

import java.util.ArrayList;
import java.util.List;

public class Pointcut {
	
	public String pointcutName = "";
	private List<String> parameterTypes = null;
	private String cflowPoint = "";
	
	public Pointcut() {
		parameterTypes = new ArrayList<String>();
	}
	public String getPointcutName() {
		return pointcutName;
	}
	public void setPointcutName(String pointcutName) {
		this.pointcutName = pointcutName;
	}
	public List<String> getParameterTypes() {
		return parameterTypes;
	}
	public void setParameterTypes(List<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	public String getCflow() {
		return cflowPoint;
	}
	public void setCflow(String cflow) {
		this.cflowPoint = cflow;
	}
	
	
}
