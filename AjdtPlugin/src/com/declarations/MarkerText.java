package com.declarations;

import java.util.Iterator;
import java.util.List;

import com.adviceConstructs.Advice;
import com.adviceConstructs.AdviceAfter;
import com.adviceConstructs.AdviceAfterThrowing;
import com.adviceConstructs.AdviceBefore;


public class MarkerText {
	Advice advice = null;
	public MarkerText(Advice advice) {
		this.advice = advice;
	}
	
	
	@Override
	public String toString() {
		String result = "";
		
		if(advice instanceof AdviceAfter){
			//E.g: AdviseAfter.loginStatus(man); //(AdviseAfter.aj: line 7)
			
			result += advice.getAdviceTypeName()+'.';
			result += advice.getPointcutName()+advice.getAdviceName().toUpperCase();
			result += '(';
		
			for (String it : advice.getParameterTypes()) {
				if(advice.getSourceElementName().indexOf(it.toString()) > -1){
					//we already know the location of methodinvocation from ast
					for (MethodInvoke met : advice.getMethodInvoke()) {
						if (met.optionalName != "" ){
							result += met.optionalName;
						}
						else{
							//back soon!
							for (MethodInvoke invoke : advice.getMethodInvoke()) {
								if(invoke.methodName.indexOf(it.toString())>-1){
									result += invoke.optionalName;
									break;
								}
							}
						}
					}
				}				
			}			
			result += ')';
			result += "; //(";
			result += advice.getAdviceFileName()+" : ";
			result += "line "+advice.getAdviceLineNumber();
			result += ')';
		}
		else if(advice instanceof AdviceBefore){
			/*if ( cflow("call(void getwelcome(..))")) {
			  Pointcutcflow.cflow();
			} //(Pointcutcflow.aj: line 14)*/
			/*	AdviceBefore.beforelogin("Hakan", "123pass"); //(AdviceBefore.aj: line 10)	*/
			boolean iscflow = false;
			List<Pointcut> pointcuts = ((AdviceBefore)advice).getPointcuts();
			
			//pointcutlarin içinden adviceda ref olarak gozuken pointcutin ismi aranip cflow varmi diye kontrol edilicek!
			for (Pointcut pointcut : pointcuts) {
//				System.out.println("*****"+pointcut.pointcutName+"***"+pointcut.getCflow()+"***"+advice.getPointcutName());
				if(pointcut.pointcutName.equals(advice.getPointcutName()) && pointcut.getCflow() != ""){
					result += "if ( ";
					result += pointcut.getCflow();
					result += " ){\n";
					iscflow = true;
					break;
				}
			}
			result += "	  "+advice.getAdviceTypeName()+'.'+advice.getPointcutName()+advice.getAdviceName().toUpperCase()+'(';
			for (Iterator<?> it = advice.getParameterTypes().iterator(); it.hasNext();) {
//				System.out.println("***"+str);
				String str = (String)it.next();
				if(advice.getSourceElementName().indexOf(str) > -1){
					//back soon					
					for (MethodInvoke met : advice.getMethodInvoke()) {
//						System.out.println(met.toString());	
						if(str.equals("String")){
								if(met.parameters.size() > 0){									
									result += met.parameters.get(0);
									met.parameters.remove(0);
								}
						}
						 //...
					}
					for (VariableDeclaration declaration : advice.getDeclaration()) {
						if(str.equals(declaration.getDeclarationType())){
							result += declaration.getInstanceTypeName();
						}
//						System.out.println(declaration.toString());
					}
					
					if (it.hasNext()) {
						result += ',';
					}
				}
			}
			result += ");\n";
			if(iscflow){
				result += '}';
			}
			result += "//(";
			result += advice.getAdviceFileName()+" : ";
			result += "line "+advice.getAdviceLineNumber();
			result += ')';
			
		}
		else if(advice instanceof AdviceAfterThrowing){
		/*try {
			man.checkEmail();
		} catch(Exception ex) { 
			  AfterThrowing.exceptionEmail(ex);
			  throw ex; 
	    } //(AfterThrowing.aj: line 10)*/
			String exceptionType = ((AdviceAfterThrowing)advice).getExceptionType();
			String exceptionName = ((AdviceAfterThrowing)advice).getExceptionName();
			
			result += "try{\n";
			for (MethodInvoke met : advice.getMethodInvoke()) {
				result += "   "+met.toString()+";\n";
			}
			result += "} catch("+exceptionType+' '+exceptionName+"){\n";
			result += "   ";
			result += advice.getAdviceTypeName()+'.';
			result += advice.getPointcutName()+advice.getAdviceName().toUpperCase();
			result += '('+exceptionName+");";
			result += "throw "+exceptionName+";\n";
			result += "}//(";
			result += advice.getAdviceFileName()+" : ";
			result += "line "+advice.getAdviceLineNumber();
			result += ')';			
		}
		
		return result;
	}
	
}
