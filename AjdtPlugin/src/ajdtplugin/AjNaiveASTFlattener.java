package ajdtplugin;

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.aspectj.org.eclipse.jdt.core.dom.*;

import com.adviceConstructs.AdviceAfter;
import com.adviceConstructs.AdviceAfterThrowing;
import com.adviceConstructs.AdviceBefore;
import com.declarations.MethodInvoke;
import com.declarations.Pointcut;
import com.declarations.VariableDeclaration;

/**
 * Internal AST visitor for serializing an AST in a quick and dirty fashion. For
 * various reasons the resulting string is not necessarily legal Java code; and
 * even if it is legal Java code, it is not necessarily the string that
 * corresponds to the given AST. Although useless for most purposes, it's fine
 * for generating debug print strings.
 * <p>
 * Example usage: <code>
 * <pre>
 *    NaiveASTFlattener p = new NaiveASTFlattener();
 *    node.accept(p);
 *    String result = p.getResult();
 * </pre>
 * </code> Call the <code>reset</code> method to clear the previous result
 * before reusing an existing instance.
 * </p>
 * 
 * @since 2.0
 */
public class AjNaiveASTFlattener extends AjASTVisitor {

	/**
	 * The string buffer into which the serialized representation of the AST is
	 * written.
	 */
	protected StringBuffer buffer;

	protected int indent = 0;

	// HAKAN
	private File file = null;
	protected String adviceType = "";
	public AdviceAfter instAfter = null;
	public AdviceBefore instBefore = null;
	public AdviceAfterThrowing instAftThrow = null;
	public List<Pointcut> points = null;

	/**
	 * Creates a new AST printer.
	 */
	public AjNaiveASTFlattener() {
		this.buffer = new StringBuffer();
		points = new ArrayList<Pointcut>();
	}

	/**
	 * Returns the string accumulated in the visit.
	 * 
	 * @return the serialized
	 */
	public String getResult() {
		return this.buffer.toString();
	}

	/**
	 * Resets this printer so that it can be used again.
	 */
	public void reset() {
		this.buffer.setLength(0);
	}

	void printIndent() {
		for (int i = 0; i < this.indent; i++)
			this.buffer.append("  "); //$NON-NLS-1$
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Appends the text representation of the given modifier flags, followed by
	 * a single space. Used for 3.0 modifiers and annotations.
	 * 
	 * @param ext
	 *            the list of modifier and annotation nodes (element type:
	 *            <code>IExtendedModifiers</code>)
	 */
	void printModifiers(List<?> ext) {
		for (Iterator<?> it = ext.iterator(); it.hasNext();) {
			ASTNode p = (ASTNode) it.next();
			p.accept(this);
			this.buffer.append(" ");//$NON-NLS-1$
		}
	}

	/**
	 * Appends the text representation of the given modifier flags, followed by
	 * a single space. Used for JLS2 modifiers.
	 * 
	 * @param modifiers
	 *            the modifier flags
	 */
	void printModifiers(int modifiers) {
		if (Modifier.isPublic(modifiers)) {
			this.buffer.append("public ");//$NON-NLS-1$
		}
		if (Modifier.isProtected(modifiers)) {
			this.buffer.append("protected ");//$NON-NLS-1$
		}
		if (Modifier.isPrivate(modifiers)) {
			this.buffer.append("private ");//$NON-NLS-1$
		}
		if (Modifier.isStatic(modifiers)) {
			this.buffer.append("static ");//$NON-NLS-1$
		}
		if (Modifier.isAbstract(modifiers)) {
			this.buffer.append("abstract ");//$NON-NLS-1$
		}
		if (Modifier.isFinal(modifiers)) {
			this.buffer.append("final ");//$NON-NLS-1$
		}
		if (Modifier.isSynchronized(modifiers)) {
			this.buffer.append("synchronized ");//$NON-NLS-1$
		}
		if (Modifier.isVolatile(modifiers)) {
			this.buffer.append("volatile ");//$NON-NLS-1$
		}
		if (Modifier.isNative(modifiers)) {
			this.buffer.append("native ");//$NON-NLS-1$
		}
		if (Modifier.isStrictfp(modifiers)) {
			this.buffer.append("strictfp ");//$NON-NLS-1$
		}
		if (Modifier.isTransient(modifiers)) {
			this.buffer.append("transient ");//$NON-NLS-1$
		}
	}

	/*
	 * @see ASTVisitor#visit(AnnotationTypeDeclaration)
	 * 
	 * @since 3.1
	 */
	public boolean visit(AnnotationTypeDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printIndent();
		printModifiers(node.modifiers());
		this.buffer.append("@interface ");//$NON-NLS-1$
		node.getName().accept(this);
		this.buffer.append(" {");//$NON-NLS-1$
		for (Iterator<?> it = node.bodyDeclarations().iterator(); it.hasNext();) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			d.accept(this);
		}
		this.buffer.append("}\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AnnotationTypeMemberDeclaration)
	 * 
	 * @since 3.1
	 */
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printIndent();
		printModifiers(node.modifiers());
		node.getType().accept(this);
		this.buffer.append(" ");//$NON-NLS-1$
		node.getName().accept(this);
		this.buffer.append("()");//$NON-NLS-1$
		if (node.getDefault() != null) {
			this.buffer.append(" default ");//$NON-NLS-1$
			node.getDefault().accept(this);
		}
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AnonymousClassDeclaration)
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		this.buffer.append("{\n");//$NON-NLS-1$
		this.indent++;
		for (Iterator<?> it = node.bodyDeclarations().iterator(); it.hasNext();) {
			BodyDeclaration b = (BodyDeclaration) it.next();
			b.accept(this);
		}
		this.indent--;
		printIndent();
		this.buffer.append("}\n");//$NON-NLS-1$
		return false;
	}

	// HAKAN
	public boolean visit(AfterAdviceDeclaration node) {

		instAfter = new AdviceAfter();
		instAfter.setAdviceTypeName(this.adviceType);

		List<String> param = new ArrayList<String>();
		printIndent();
		this.buffer.append("after");
		this.buffer.append("(");
		List<?> parameters = node.parameters();
		for (Iterator<?> iter = parameters.iterator(); iter.hasNext();) {
			SingleVariableDeclaration element = (SingleVariableDeclaration) iter.next();
			buffer.append(element.getType().toString() + " "+ element.getName());
			instAfter.getParameterTypes().add(element.getType().toString());
			param.add(element.getName().toString());
			if (iter.hasNext())
				buffer.append(", ");
		}

		this.buffer.append("):");
		instAfter.setPointcut((ReferencePointcut) node.getPointcut());
		instAfter.setPointcutName(((ReferencePointcut) node.getPointcut()).getName().toString());

		node.getPointcut().accept(this);
		this.buffer.append("(");
		for (Iterator<String> iter = param.iterator(); iter.hasNext();) {
			String para = iter.next();
			this.buffer.append(para);
			if (iter.hasNext())
				buffer.append(", ");
		}
		this.buffer.append(")");
		node.getBody().accept(this);
		return false;
	}
	//
	public boolean visit(AfterThrowingAdviceDeclaration node) {
		
		instAftThrow = new AdviceAfterThrowing();
		instAftThrow.setAdviceTypeName(this.adviceType);

		List<String> param = new ArrayList<String>();
		printIndent();
		
		this.buffer.append("after");
		this.buffer.append("(");
		List<?> parameters = node.parameters();
		for (Iterator<?> iter = parameters.iterator(); iter.hasNext();) {
			SingleVariableDeclaration element = (SingleVariableDeclaration) iter.next();
			buffer.append(element.getType().toString() + " "+ element.getName());
			instAftThrow.getParameterTypes().add(element.getType().toString());
			param.add(element.getName().toString());
			if (iter.hasNext())
				buffer.append(", ");
		}
		this.buffer.append(") throwing(");
		//throwing(Exception ex)
		node.getThrowing().accept(this);
		
		instAftThrow.ExceptionType = node.getThrowing().getType().toString();
		instAftThrow.ExceptionName = node.getThrowing().getName().toString();
//		System.out.println(node.getThrowing().getType()+"*** "+ node.getThrowing().getName().toString());
		
		this.buffer.append("): ");
		
		instAftThrow.setPointcut((ReferencePointcut) node.getPointcut());
		instAftThrow.setPointcutName(((ReferencePointcut) node.getPointcut()).getName().toString());
		
		node.getPointcut().accept(this);
		this.buffer.append("(");
		for (Iterator<String> iter = param.iterator(); iter.hasNext();) {
			String para = iter.next();
			this.buffer.append(para);
			if (iter.hasNext())
				buffer.append(", ");
		}
		this.buffer.append(")");
		node.getBody().accept(this);
		return false;
	}
	// HAKAN
	public boolean visit(BeforeAdviceDeclaration node) {
		//
		instBefore = new AdviceBefore();
		instBefore.setAdviceTypeName(this.adviceType);
		
		List<String> param = new ArrayList<String>();
		printIndent();
		this.buffer.append("before");
		this.buffer.append("(");
		List<?> parameters = node.parameters();
		for (Iterator<?> iter = parameters.iterator(); iter.hasNext();) {
			SingleVariableDeclaration element = (SingleVariableDeclaration) iter.next();
			buffer.append(element.getType().toString() + " "+ element.getName() + "");
			//
			instBefore.getParameterTypes().add(element.getType().toString());
			param.add(element.getName().toString());
			if (iter.hasNext())
				buffer.append(", ");
		}
		this.buffer.append("):");
		//
		instBefore.setPointcut((ReferencePointcut) node.getPointcut());
		instBefore.setPointcutName(((ReferencePointcut) node.getPointcut()).getName().toString());
		
		node.getPointcut().accept(this);
		this.buffer.append("(");
		for (Iterator<String> iter = param.iterator(); iter.hasNext();) {
			String para = iter.next();
			this.buffer.append(para);
			if (iter.hasNext())
				buffer.append(", ");
		}
		this.buffer.append(")");
		node.getBody().accept(this);		
		return false;
	}
	
	public boolean visit(CflowPointcut node) {
		Pointcut pnt = points.get(points.size() - 1);
		node.getBody().accept(this);
		if(node.getBody() instanceof DefaultPointcut){
			pnt.setCflow("cflow(\""+((DefaultPointcut)node.getBody()).getDetail()+"\")");
		}
//		points.add(points.size()-1, pnt);
		return false;
	}
	
	// HAKAN
	public boolean visit(AndPointcut node) {
		node.getLeft().accept(this);
		this.buffer.append(" && ");
		node.getRight().accept(this);
		return false;
	}

	// HAKAN
	public boolean visit(OrPointcut node) {
		node.getLeft().accept(this);
		this.buffer.append(" || ");
		node.getRight().accept(this);
		return false;
	}

	public boolean visit(AnyWithAnnotationTypePattern node) {
		this.buffer.append("***************");
		return true;
	}

	public boolean visit(AnyTypePattern node) {
		this.buffer.append("***************");
		return true;
	}

	public boolean visit(SignaturePattern node) {
		this.buffer.append("***************");
		return true;
	}

	public boolean visit(DeclareAnnotationDeclaration node) {
		this.buffer.append("***************");
		return true;
	}

	/*
	 * @see ASTVisitor#visit(ArrayAccess)
	 */
	public boolean visit(ArrayAccess node) {
		node.getArray().accept(this);
		this.buffer.append("[");//$NON-NLS-1$
		node.getIndex().accept(this);
		this.buffer.append("]");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayCreation)
	 */
	public boolean visit(ArrayCreation node) {
		this.buffer.append("new ");//$NON-NLS-1$
		ArrayType at = node.getType();
		int dims = at.getDimensions();
		Type elementType = at.getElementType();
		elementType.accept(this);
		for (Iterator<?> it = node.dimensions().iterator(); it.hasNext();) {
			this.buffer.append("[");//$NON-NLS-1$
			Expression e = (Expression) it.next();
			e.accept(this);
			this.buffer.append("]");//$NON-NLS-1$
			dims--;
		}
		// add empty "[]" for each extra array dimension
		for (int i = 0; i < dims; i++) {
			this.buffer.append("[]");//$NON-NLS-1$
		}
		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayInitializer)
	 */
	public boolean visit(ArrayInitializer node) {
		this.buffer.append("{");//$NON-NLS-1$
		for (Iterator<?> it = node.expressions().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append("}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayType)
	 */
	public boolean visit(ArrayType node) {
		node.getComponentType().accept(this);
		this.buffer.append("[]");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AssertStatement)
	 */
	public boolean visit(AssertStatement node) {
		printIndent();
		this.buffer.append("assert ");//$NON-NLS-1$
		node.getExpression().accept(this);
		if (node.getMessage() != null) {
			this.buffer.append(" : ");//$NON-NLS-1$
			node.getMessage().accept(this);
		}
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Assignment)
	 */
	public boolean visit(Assignment node) {
		node.getLeftHandSide().accept(this);
		this.buffer.append(node.getOperator().toString());
		node.getRightHandSide().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Block)
	 */
	public boolean visit(Block node) {
		this.buffer.append("{ \n");//$NON-NLS-1$
		this.indent++;
		for (Iterator<?> it = node.statements().iterator(); it.hasNext();) {
			Statement s = (Statement) it.next();
			// if(s instanceof VariableDeclarationStatement) {
			// variState.add((VariableDeclarationStatement) s);
			// }
			s.accept(this);
		}
		this.indent--;
		printIndent();
		this.buffer.append("} \n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BlockComment)
	 * 
	 * @since 3.0
	 */
	public boolean visit(BlockComment node) {
		printIndent();
		node.accept(this);
		this.buffer.append("/* comment */");//$NON-NLS-1$
		return true;
	}

	/*
	 * @see ASTVisitor#visit(BooleanLiteral)
	 */
	public boolean visit(BooleanLiteral node) {
		if (node.booleanValue() == true) {
			this.buffer.append("true");//$NON-NLS-1$
		} else {
			this.buffer.append("false");//$NON-NLS-1$
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BreakStatement)
	 */
	public boolean visit(BreakStatement node) {
		printIndent();
		this.buffer.append("break");//$NON-NLS-1$
		if (node.getLabel() != null) {
			this.buffer.append(" ");//$NON-NLS-1$
			node.getLabel().accept(this);
		}
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CastExpression)
	 */
	public boolean visit(CastExpression node) {
		this.buffer.append("(");//$NON-NLS-1$
		node.getType().accept(this);
		this.buffer.append(")");//$NON-NLS-1$
		node.getExpression().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CatchClause)
	 */
	public boolean visit(CatchClause node) {
		this.buffer.append("catch (");//$NON-NLS-1$
		node.getException().accept(this);
		this.buffer.append(") ");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CharacterLiteral)
	 */
	public boolean visit(CharacterLiteral node) {
		this.buffer.append(node.getEscapedValue());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ClassInstanceCreation)
	 */
	public boolean visit(ClassInstanceCreation node) {
		this.buffer.append("ClassInstanceCreation ");
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append("new ");//$NON-NLS-1$

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.buffer.append("<");//$NON-NLS-1$
				for (Iterator<?> it = node.typeArguments().iterator(); it
						.hasNext();) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(",");//$NON-NLS-1$
					}
				}
				this.buffer.append(">");//$NON-NLS-1$
			}
			node.getType().accept(this);
		}
		this.buffer.append("(");//$NON-NLS-1$
		for (Iterator<?> it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(")");//$NON-NLS-1$
		if (node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		if (node.getPackage() != null) {
			node.getPackage().accept(this);
		}
		for (Iterator<?> it = node.imports().iterator(); it.hasNext();) {
			ImportDeclaration d = (ImportDeclaration) it.next();
			d.accept(this);
		}
		for (Iterator<?> it = node.types().iterator(); it.hasNext();) {
			AbstractTypeDeclaration d = (AbstractTypeDeclaration) it.next();
			d.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConditionalExpression)
	 */
	public boolean visit(ConditionalExpression node) {
		node.getExpression().accept(this);
		this.buffer.append(" ? ");//$NON-NLS-1$
		node.getThenExpression().accept(this);
		this.buffer.append(" : (line 428");//$NON-NLS-1$
		node.getElseExpression().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConstructorInvocation)
	 */
	public boolean visit(ConstructorInvocation node) {
		printIndent();
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.buffer.append("<");//$NON-NLS-1$
				for (Iterator<?> it = node.typeArguments().iterator(); it
						.hasNext();) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(",");//$NON-NLS-1$
					}
				}
				this.buffer.append(">");//$NON-NLS-1$
			}
		}
		this.buffer.append("this(");//$NON-NLS-1$
		for (Iterator<?> it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(");\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ContinueStatement)
	 */
	public boolean visit(ContinueStatement node) {
		printIndent();
		this.buffer.append("continue");//$NON-NLS-1$
		if (node.getLabel() != null) {
			this.buffer.append(" ");//$NON-NLS-1$
			node.getLabel().accept(this);
		}
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(DoStatement)
	 */
	public boolean visit(DoStatement node) {
		printIndent();
		this.buffer.append("do ");//$NON-NLS-1$
		node.getBody().accept(this);
		this.buffer.append(" while (");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(");\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EmptyStatement)
	 */
	public boolean visit(EmptyStatement node) {
		printIndent();
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnhancedForStatement)
	 * 
	 * @since 3.1
	 */
	public boolean visit(EnhancedForStatement node) {
		printIndent();
		this.buffer.append("for (");//$NON-NLS-1$
		node.getParameter().accept(this);
		this.buffer.append(" : ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(") ");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	public boolean visit(PointcutDeclaration node) {
		//
		Pointcut point = new Pointcut();
		
		printIndent();
		buffer.append(" pointcut ");
		node.getName().accept(this);
		
		point.pointcutName = node.getName().toString();
		
		buffer.append("(");
		List<?> parameters = node.parameters();
		for (Iterator<?> iter = parameters.iterator(); iter.hasNext();) {
			SingleVariableDeclaration element = (SingleVariableDeclaration) iter.next();
			buffer.append(element.getType().toString() + " "+ element.getName() + "");
			//
			point.getParameterTypes().add(element.getType().toString());
			if (iter.hasNext())
				buffer.append(", ");
		}
		buffer.append(") :");
		//add to list of pointcuts 
		points.add(point);		
		
		node.getDesignator().accept(this);
		buffer.append(";\n");				
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnumConstantDeclaration)
	 * 
	 * @since 3.1
	 */
	public boolean visit(EnumConstantDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printIndent();
		printModifiers(node.modifiers());
		node.getName().accept(this);
		if (!node.arguments().isEmpty()) {
			this.buffer.append("(");//$NON-NLS-1$
			for (Iterator<?> it = node.arguments().iterator(); it.hasNext();) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.buffer.append(",");//$NON-NLS-1$
				}
			}
			this.buffer.append(")");//$NON-NLS-1$
		}
		if (node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnumDeclaration)
	 * 
	 * @since 3.1
	 */
	public boolean visit(EnumDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printIndent();
		printModifiers(node.modifiers());
		this.buffer.append("enum ");//$NON-NLS-1$
		node.getName().accept(this);
		this.buffer.append(" ");//$NON-NLS-1$
		if (!node.superInterfaceTypes().isEmpty()) {
			this.buffer.append("implements ");//$NON-NLS-1$
			for (Iterator<?> it = node.superInterfaceTypes().iterator(); it
					.hasNext();) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					this.buffer.append(", ");//$NON-NLS-1$
				}
			}
			this.buffer.append(" ");//$NON-NLS-1$
		}
		this.buffer.append("{");//$NON-NLS-1$
		for (Iterator<?> it = node.enumConstants().iterator(); it.hasNext();) {
			EnumConstantDeclaration d = (EnumConstantDeclaration) it.next();
			d.accept(this);
			// enum constant declarations do not include punctuation
			if (it.hasNext()) {
				// enum constant declarations are separated by commas
				this.buffer.append(", ");//$NON-NLS-1$
			}
		}
		if (!node.bodyDeclarations().isEmpty()) {
			this.buffer.append("; ");//$NON-NLS-1$
			for (Iterator<?> it = node.bodyDeclarations().iterator(); it
					.hasNext();) {
				BodyDeclaration d = (BodyDeclaration) it.next();
				d.accept(this);
				// other body declarations include trailing punctuation
			}
		}
		this.buffer.append("}\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ExpressionStatement)
	 */
	public boolean visit(ExpressionStatement node) {
		this.buffer.append(" ExpressionStatement ");
		printIndent();
		// if(node.getExpression() instanceof MethodInvocation){
		// expState.add((MethodInvocation) node.getExpression());
		// }
		node.getExpression().accept(this);
		this.buffer.append(" ExpressionStatement end ;\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldAccess)
	 */
	public boolean visit(FieldAccess node) {
		node.getExpression().accept(this);
		this.buffer.append(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printIndent();
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType().accept(this);
		this.buffer.append(" ");//$NON-NLS-1$
		for (Iterator<?> it = node.fragments().iterator(); it.hasNext();) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it
					.next();
			f.accept(this);
			if (it.hasNext()) {
				this.buffer.append(", ");//$NON-NLS-1$
			}
		}
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ForStatement)
	 */
	public boolean visit(ForStatement node) {
		printIndent();
		this.buffer.append("for (");//$NON-NLS-1$
		for (Iterator<?> it = node.initializers().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext())
				buffer.append(", ");//$NON-NLS-1$
		}
		this.buffer.append("; ");//$NON-NLS-1$
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
		this.buffer.append("; ");//$NON-NLS-1$
		for (Iterator<?> it = node.updaters().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext())
				buffer.append(", ");//$NON-NLS-1$
		}
		this.buffer.append(") ");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(IfStatement)
	 */
	public boolean visit(IfStatement node) {
		printIndent();
		this.buffer.append("if (");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(") ");//$NON-NLS-1$
		node.getThenStatement().accept(this);
		if (node.getElseStatement() != null) {
			printIndent();
			this.buffer.append("else ");//$NON-NLS-1$
			node.getElseStatement().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
		printIndent();
		this.buffer.append("import ");//$NON-NLS-1$
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.isStatic()) {
				this.buffer.append("static ");//$NON-NLS-1$
			}
		}
		node.getName().accept(this);
		if (node.isOnDemand()) {
			this.buffer.append(".*");//$NON-NLS-1$
		}
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InfixExpression)
	 */
	public boolean visit(InfixExpression node) {
		this.buffer.append("InfixExpression");
		node.getLeftOperand().accept(this);
		this.buffer.append(' '); // for cases like x= i - -1; or x= i++ + ++i;
		this.buffer.append(node.getOperator().toString());
		this.buffer.append(' ');
		node.getRightOperand().accept(this);
		final List<?> extendedOperands = node.extendedOperands();
		if (extendedOperands.size() != 0) {
			this.buffer.append(' ');
			for (Iterator<?> it = extendedOperands.iterator(); it.hasNext();) {
				this.buffer.append(node.getOperator().toString()).append(' ');
				Expression e = (Expression) it.next();
				e.accept(this);
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InstanceofExpression)
	 */
	public boolean visit(InstanceofExpression node) {
		node.getLeftOperand().accept(this);
		this.buffer.append(" instanceof ");//$NON-NLS-1$
		node.getRightOperand().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Initializer)
	 */
	public boolean visit(Initializer node) {
		this.buffer.append("Initializer");
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Javadoc)
	 */
	public boolean visit(Javadoc node) {
		printIndent();
		this.buffer.append("/** ");//$NON-NLS-1$
		for (Iterator<?> it = node.tags().iterator(); it.hasNext();) {
			ASTNode e = (ASTNode) it.next();
			e.accept(this);
		}
		this.buffer.append("\n */\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LabeledStatement)
	 */
	public boolean visit(LabeledStatement node) {
		this.buffer.append("LabeledStatement");
		printIndent();
		node.getLabel().accept(this);
		this.buffer.append(": ");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LineComment)
	 * 
	 * @since 3.0
	 */
	public boolean visit(LineComment node) {
		this.buffer.append("// LineComment\n");//$NON-NLS-1$
		return true;
	}

	/*
	 * @see ASTVisitor#visit(MarkerAnnotation)
	 * 
	 * @since 3.1
	 */

	public boolean visit(MarkerAnnotation node) {
		this.buffer.append("@***");//$NON-NLS-1$
		node.getTypeName().accept(this);
		if (node.getTypeName().equals("AdviceName")) { // should give you the
														// name of the type of
														// annotation
			node.properties();
			this.buffer.append("");
		}
		// explore properties to find out the value of first one. Not sure if
		// this field holds the parameters, but it should be.
		return true;
	}

	/*
	 * @see ASTVisitor#visit(MemberRef)
	 * 
	 * @since 3.0
	 */
	public boolean visit(MemberRef node) {
		this.buffer.append("MemberRef");
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
		}
		this.buffer.append("#");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MemberValuePair)
	 * 
	 * @since 3.1
	 */
	public boolean visit(MemberValuePair node) {
		node.getName().accept(this);
		this.buffer.append("=");//$NON-NLS-1$
		node.getValue().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRef)
	 * 
	 * @since 3.0
	 */
	public boolean visit(MethodRef node) {

		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
		}
		this.buffer.append("#");//$NON-NLS-1$
		node.getName().accept(this);
		this.buffer.append("(");//$NON-NLS-1$
		for (Iterator<?> it = node.parameters().iterator(); it.hasNext();) {
			MethodRefParameter e = (MethodRefParameter) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRefParameter)
	 * 
	 * @since 3.0
	 */
	public boolean visit(MethodRefParameter node) {
		this.buffer.append("MethodRefParameter");
		node.getType().accept(this);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.isVarargs()) {
				this.buffer.append("...");//$NON-NLS-1$
			}
		}
		if (node.getName() != null) {
			this.buffer.append(" ");//$NON-NLS-1$
			node.getName().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration node) {
		this.buffer.append("MethodDeclaration");
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printIndent();
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
			if (!node.typeParameters().isEmpty()) {
				this.buffer.append("<");//$NON-NLS-1$
				for (Iterator<?> it = node.typeParameters().iterator(); it
						.hasNext();) {
					TypeParameter t = (TypeParameter) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(",");//$NON-NLS-1$
					}
				}
				this.buffer.append(">");//$NON-NLS-1$
			}
		}
		if (!node.isConstructor()) {
			if (node.getReturnType2() != null) {
				node.getReturnType2().accept(this);
			} else {
				// methods really ought to have a return type
				this.buffer.append("void");//$NON-NLS-1$
			}
			this.buffer.append(" ");//$NON-NLS-1$
		}
		node.getName().accept(this);
		this.buffer.append("(");//$NON-NLS-1$
		for (Iterator<?> it = node.parameters().iterator(); it.hasNext();) {
			SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
			v.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(")");//$NON-NLS-1$
		for (int i = 0; i < node.getExtraDimensions(); i++) {
			this.buffer.append("[]"); //$NON-NLS-1$
		}
		if (!node.thrownExceptions().isEmpty()) {
			this.buffer.append(" throws ");//$NON-NLS-1$
			for (Iterator<?> it = node.thrownExceptions().iterator(); it
					.hasNext();) {
				Name n = (Name) it.next();
				n.accept(this);
				if (it.hasNext()) {
					this.buffer.append(", ");//$NON-NLS-1$
				}
			}
			this.buffer.append(" ");//$NON-NLS-1$
		}
		if (node.getBody() == null) {
			this.buffer.append(";\n");//$NON-NLS-1$
		} else {
			node.getBody().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodInvocation)
	 */
	public boolean visit(MethodInvocation node) {
		
		int methodlineNumber = getLineNumberForNode(node);		
		MethodInvoke invoke = new MethodInvoke();
		//man.LoginPage(); line 7
		//man.starts();  line 14
		this.buffer.append("MethodInvocation ");// man.LoginPage("asda","asdsad");
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			if (node.getExpression() instanceof SimpleName) {
				invoke.optionalName = ((SimpleName) node.getExpression()).getIdentifier().toString();
			}
			this.buffer.append(".");//$NON-NLS-1$
		}

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.buffer.append("<");//$NON-NLS-1$
				for (Iterator<?> it = node.typeArguments().iterator(); it.hasNext();) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(",");//$NON-NLS-1$
					}
				}
				this.buffer.append(">");//$NON-NLS-1$
			}
		}
		node.getName().accept(this);
		invoke.methodName = node.getName().toString();

		this.buffer.append("(");//$NON-NLS-1$
		for (Iterator<?> it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			e.accept(this);
			invoke.parameters.add(e);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		
		this.buffer.append(")");//$NON-NLS-1$
		
		//save HAKAN
		//for source code. 
		//
		if (this.instAfter != null && methodlineNumber > 0) {
//			for (VariableDeclaration dec : this.instAfter.getDeclaration()) {
				if(instAfter.getSourceLineNumber() == methodlineNumber){
					invoke.MethodInvocationLineNumber = methodlineNumber;
					this.instAfter.getMethodInvoke().add(invoke);
				}
//				if(dec.getInstanceTypeName().equals(invoke.optionalName)){ 
//					this.instAfter.getMethodInvoke().add(invoke);
//				}
//			}
		}
		else if(this.instAftThrow != null && methodlineNumber > 0){
			if(instAftThrow.getSourceLineNumber() == methodlineNumber){
				invoke.MethodInvocationLineNumber = methodlineNumber;
				this.instAftThrow.getMethodInvoke().add(invoke);
			}
		}
//		else{
//			expState.add(invoke);
//		}

		return false;
	}
	//HAKAN
	private int getLineNumberForNode(ASTNode node) {
		if(getFile() !=  null){
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setCompilerOptions(new HashMap<Object, Object>());
			try {
				parser.setSource(readFileAsString(this.getFile()).toCharArray());
			} catch (IOException e) {
				e.printStackTrace();
			}
			CompilationUnit cu2 = (CompilationUnit) parser.createAST(null);
			int lineNumber = cu2.getLineNumber(node.getStartPosition());			
			return lineNumber;
		}
		return -1;
	}
	
	private static String readFileAsString(File file) throws java.io.IOException{
		byte[] buffer = new byte[(int) file.length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(file));
			f.read(buffer);
		} finally {
			if (f != null) try { f.close(); } catch (IOException ignored) { }
		}
		return new String(buffer);
	}
	
	/*
	 * @see ASTVisitor#visit(Modifier)
	 * 
	 * @since 3.1
	 */
	public boolean visit(Modifier node) {
		this.buffer.append("Modifier");
		this.buffer.append(node.getKeyword().toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NormalAnnotation)
	 * 
	 * @since 3.1
	 */
	public boolean visit(NormalAnnotation node) {
		this.buffer.append("@");//$NON-NLS-1$
		node.getTypeName().accept(this);
		this.buffer.append("(");//$NON-NLS-1$
		for (Iterator<?> it = node.values().iterator(); it.hasNext();) {
			MemberValuePair p = (MemberValuePair) it.next();
			p.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NullLiteral)
	 */
	public boolean visit(NullLiteral node) {
		this.buffer.append("null");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NumberLiteral)
	 */
	public boolean visit(NumberLiteral node) {
		this.buffer.append(node.getToken());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			for (Iterator<?> it = node.annotations().iterator(); it.hasNext();) {
				Annotation p = (Annotation) it.next();
				p.accept(this);
				this.buffer.append(" ");//$NON-NLS-1$
			}
		}
		printIndent();
		this.buffer.append("package ");//$NON-NLS-1$
		node.getName().accept(this);
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParameterizedType)
	 * 
	 * @since 3.1
	 */
	public boolean visit(ParameterizedType node) {
		this.buffer.append("ParameterizedType");
		node.getType().accept(this);
		this.buffer.append("<");//$NON-NLS-1$
		for (Iterator<?> it = node.typeArguments().iterator(); it.hasNext();) {
			Type t = (Type) it.next();
			t.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(">");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParenthesizedExpression)
	 */
	public boolean visit(ParenthesizedExpression node) {
		this.buffer.append("(");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PostfixExpression)
	 */
	public boolean visit(PostfixExpression node) {
		node.getOperand().accept(this);
		this.buffer.append(node.getOperator().toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrefixExpression)
	 */
	public boolean visit(PrefixExpression node) {
		this.buffer.append(node.getOperator().toString());
		node.getOperand().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrimitiveType)
	 */
	public boolean visit(PrimitiveType node) {
		this.buffer.append("PrimitiveType");
		this.buffer.append(node.getPrimitiveTypeCode().toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedName)
	 */
	public boolean visit(QualifiedName node) {
		node.getQualifier().accept(this);
		this.buffer.append(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedType)
	 * 
	 * @since 3.1
	 */
	public boolean visit(QualifiedType node) {
		node.getQualifier().accept(this);
		this.buffer.append(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ReturnStatement)
	 */
	public boolean visit(ReturnStatement node) {
		printIndent();
		this.buffer.append("return");//$NON-NLS-1$
		if (node.getExpression() != null) {
			this.buffer.append(" ");//$NON-NLS-1$
			node.getExpression().accept(this);
		}
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleName)
	 */
	public boolean visit(SimpleName node) {
		this.buffer.append("SimpleName->");
		this.buffer.append(node.getIdentifier());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleType)
	 */
	public boolean visit(SimpleType node) {
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SingleMemberAnnotation)
	 * 
	 * @since 3.1
	 */
	public boolean visit(SingleMemberAnnotation node) {
		this.buffer.append("@");//$NON-NLS-1$
		node.getTypeName().accept(this);
		this.buffer.append("(");//$NON-NLS-1$
		node.getValue().accept(this);
		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SingleVariableDeclaration)
	 */
	public boolean visit(SingleVariableDeclaration node) {
		printIndent();
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType().accept(this);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.isVarargs()) {
				this.buffer.append("...");//$NON-NLS-1$
			}
		}
		this.buffer.append(" ");//$NON-NLS-1$
		node.getName().accept(this);
		for (int i = 0; i < node.getExtraDimensions(); i++) {
			this.buffer.append("[]"); //$NON-NLS-1$
		}
		if (node.getInitializer() != null) {
			this.buffer.append("=");//$NON-NLS-1$
			node.getInitializer().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(StringLiteral)
	 */
	public boolean visit(StringLiteral node) {
		this.buffer.append("StringLiteral->");
		this.buffer.append(node.getEscapedValue());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperConstructorInvocation)
	 */
	public boolean visit(SuperConstructorInvocation node) {
		printIndent();
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.buffer.append("<");//$NON-NLS-1$
				for (Iterator<?> it = node.typeArguments().iterator(); it
						.hasNext();) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(",");//$NON-NLS-1$
					}
				}
				this.buffer.append(">");//$NON-NLS-1$
			}
		}
		this.buffer.append("super(");//$NON-NLS-1$
		for (Iterator<?> it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(");\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperFieldAccess)
	 */
	public boolean visit(SuperFieldAccess node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append("super.");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperMethodInvocation)
	 */
	public boolean visit(SuperMethodInvocation node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append("super.");//$NON-NLS-1$
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.buffer.append("<");//$NON-NLS-1$
				for (Iterator<?> it = node.typeArguments().iterator(); it
						.hasNext();) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(",");//$NON-NLS-1$
					}
				}
				this.buffer.append(">");//$NON-NLS-1$
			}
		}
		node.getName().accept(this);
		this.buffer.append("(");//$NON-NLS-1$
		for (Iterator<?> it = node.arguments().iterator(); it.hasNext();) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.buffer.append(",");//$NON-NLS-1$
			}
		}
		this.buffer.append(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchCase)
	 */
	public boolean visit(SwitchCase node) {
		if (node.isDefault()) {
			this.buffer.append("default :\n");//$NON-NLS-1$
		} else {
			this.buffer.append("case ");//$NON-NLS-1$
			node.getExpression().accept(this);
			this.buffer.append(":\n");//$NON-NLS-1$
		}
		this.indent++; // decremented in visit(SwitchStatement)
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchStatement)
	 */
	public boolean visit(SwitchStatement node) {
		this.buffer.append("switch (");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(") ");//$NON-NLS-1$
		this.buffer.append("{\n");//$NON-NLS-1$
		this.indent++;
		for (Iterator<?> it = node.statements().iterator(); it.hasNext();) {
			Statement s = (Statement) it.next();
			s.accept(this);
			this.indent--; // incremented in visit(SwitchCase)
		}
		this.indent--;
		printIndent();
		this.buffer.append("}\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SynchronizedStatement)
	 */
	public boolean visit(SynchronizedStatement node) {
		this.buffer.append("synchronized (");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(") ");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TagElement)
	 * 
	 * @since 3.0
	 */
	public boolean visit(TagElement node) {
		if (node.isNested()) {
			// nested tags are always enclosed in braces
			this.buffer.append("{");//$NON-NLS-1$
		} else {
			// top-level tags always begin on a new line
			this.buffer.append("\n * ");//$NON-NLS-1$
		}
		boolean previousRequiresWhiteSpace = false;
		if (node.getTagName() != null) {
			this.buffer.append(node.getTagName());
			previousRequiresWhiteSpace = true;
		}
		boolean previousRequiresNewLine = false;
		for (Iterator<?> it = node.fragments().iterator(); it.hasNext();) {
			ASTNode e = (ASTNode) it.next();
			// assume text elements include necessary leading and trailing
			// whitespace
			// but Name, MemberRef, MethodRef, and nested TagElement do not
			// include white space
			boolean currentIncludesWhiteSpace = (e instanceof TextElement);
			if (previousRequiresNewLine && currentIncludesWhiteSpace) {
				this.buffer.append("\n * ");//$NON-NLS-1$
			}
			previousRequiresNewLine = currentIncludesWhiteSpace;
			// add space if required to separate
			if (previousRequiresWhiteSpace && !currentIncludesWhiteSpace) {
				this.buffer.append(" "); //$NON-NLS-1$
			}
			e.accept(this);
			previousRequiresWhiteSpace = !currentIncludesWhiteSpace
					&& !(e instanceof TagElement);
		}
		if (node.isNested()) {
			this.buffer.append("}");//$NON-NLS-1$
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TextElement)
	 * 
	 * @since 3.0
	 */
	public boolean visit(TextElement node) {
		this.buffer.append("TextElement");
		this.buffer.append(node.getText());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThisExpression)
	 */
	public boolean visit(ThisExpression node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.buffer.append(".");//$NON-NLS-1$
		}
		this.buffer.append("this");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThrowStatement)
	 */
	public boolean visit(ThrowStatement node) {
		printIndent();
		this.buffer.append("throw ");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TryStatement)
	 */
	public boolean visit(TryStatement node) {
		printIndent();
		this.buffer.append("try ");//$NON-NLS-1$
		node.getBody().accept(this);
		this.buffer.append(" ");//$NON-NLS-1$
		for (Iterator<?> it = node.catchClauses().iterator(); it.hasNext();) {
			CatchClause cc = (CatchClause) it.next();
			cc.accept(this);
		}
		if (node.getFinally() != null) {
			this.buffer.append(" finally ");//$NON-NLS-1$
			node.getFinally().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}

		adviceType = node.getName().toString();
//		System.out.println("Type : " + node.getName());

		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}

		//		this.buffer.append(node.isInterface() ? "interface " : "class");//$NON-NLS-2$//$NON-NLS-1$
		if (node.isInterface()) {
			this.buffer.append("interface ");//$NON-NLS-1$
		} else if (((AjTypeDeclaration) node).isAspect()) {
			// HAKAN
			if (((AspectDeclaration) node).isPrivileged()) {
				this.buffer.append("privileged ");
			}
			this.buffer.append("aspect ");//$NON-NLS-1$
		} else {
			this.buffer.append("class ");//$NON-NLS-1$
		}

		node.getName().accept(this);

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeParameters().isEmpty()) {
				this.buffer.append("<");//$NON-NLS-1$
				for (Iterator<?> it = node.typeParameters().iterator(); it
						.hasNext();) {
					TypeParameter t = (TypeParameter) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(",");//$NON-NLS-1$
					}
				}
				this.buffer.append(">");//$NON-NLS-1$
			}
		}
		this.buffer.append(" ");//$NON-NLS-1$

		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.getSuperclassType() != null) {
				this.buffer.append("extends ");//$NON-NLS-1$
				node.getSuperclassType().accept(this);
				this.buffer.append(" ");//$NON-NLS-1$
			}
			if (!node.superInterfaceTypes().isEmpty()) {
				//				this.buffer.append(node.isInterface() ? "extends " : "implements ");//$NON-NLS-2$//$NON-NLS-1$
				if (node.isInterface()) {
					this.buffer.append("extends ");//$NON-NLS-1$
				} else {
					this.buffer.append("implements ");//$NON-NLS-1$
				}
				for (Iterator<?> it = node.superInterfaceTypes().iterator(); it
						.hasNext();) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.buffer.append(", ");//$NON-NLS-1$
					}
				}
				this.buffer.append(" ");//$NON-NLS-1$
			}
		}
		this.buffer.append("{\n");//$NON-NLS-1$
		this.indent++;
		BodyDeclaration prev = null;
		for (Iterator<?> it = node.bodyDeclarations().iterator(); it.hasNext();) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			if (prev instanceof EnumConstantDeclaration) {
				// enum constant declarations do not include punctuation
				if (d instanceof EnumConstantDeclaration) {
					// enum constant declarations are separated by commas
					this.buffer.append(", ");//$NON-NLS-1$
				} else {
					// semicolon separates last enum constant declaration from
					// first class body declarations
					this.buffer.append("; ");//$NON-NLS-1$
				}
			}
			d.accept(this);
		}
		this.indent--;
		printIndent();
		this.buffer.append("}--\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclarationStatement)
	 */
	public boolean visit(TypeDeclarationStatement node) {
		this.buffer.append("TypeDeclarationStatement");
		if (node.getAST().apiLevel() >= AST.JLS3) {
			node.getDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeLiteral)
	 */
	public boolean visit(TypeLiteral node) {
		node.getType().accept(this);
		this.buffer.append(".class");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeParameter)
	 * 
	 * @since 3.1
	 */
	public boolean visit(TypeParameter node) {

		this.buffer.append("TypeParameter");
		node.getName().accept(this);
		if (!node.typeBounds().isEmpty()) {
			this.buffer.append(" extends ");//$NON-NLS-1$
			for (Iterator<?> it = node.typeBounds().iterator(); it.hasNext();) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					this.buffer.append(" & ");//$NON-NLS-1$
				}
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationExpression)
	 */
	public boolean visit(VariableDeclarationExpression node) {
		this.buffer.append("VariableDeclarationExpression");
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType().accept(this);
		this.buffer.append(" ");//$NON-NLS-1$
		for (Iterator<?> it = node.fragments().iterator(); it.hasNext();) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it
					.next();
			f.accept(this);
			if (it.hasNext()) {
				this.buffer.append(", ");//$NON-NLS-1$
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {
		this.buffer.append("VariableDeclarationFragment ");
		node.getName().accept(this);
		for (int i = 0; i < node.getExtraDimensions(); i++) {
			this.buffer.append("[]");//$NON-NLS-1$
		}
		if (node.getInitializer() != null) {
			this.buffer.append("=");//$NON-NLS-1$
			node.getInitializer().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationStatement)
	 */
	public boolean visit(VariableDeclarationStatement node) {

		VariableDeclaration dec = new VariableDeclaration();

		this.buffer.append("VariableDeclarationStatement ");
		printIndent();
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		this.buffer.append(" **1 ");

		node.getType().accept(this);
		if (node.getType() instanceof SimpleType) {
			dec.setDeclarationType(((SimpleType) node.getType()).getName()
					.toString());
		}
		this.buffer.append(" **2 ");//$NON-NLS-1$
		for (Iterator<?> it = node.fragments().iterator(); it.hasNext();) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it
					.next();
			if (f instanceof VariableDeclarationFragment) {
				dec.setInstanceTypeName(f.getName().toString());
				if (f.getInitializer() instanceof ClassInstanceCreation) {

					for (Iterator<?> it2 = ((ClassInstanceCreation) f
							.getInitializer()).arguments().iterator(); it2
							.hasNext();) {
						Expression e = (Expression) it2.next();
						dec.getParameters().add(e);
					}

				}
			}

			f.accept(this);

			if (it.hasNext()) {
				this.buffer.append(", ");//$NON-NLS-1$
			}
		}

		if (this.instAfter != null) {
			this.instAfter.getDeclaration().add(dec);
		}
		this.buffer.append("**;\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WildcardType)
	 * 
	 * @since 3.1
	 */
	public boolean visit(WildcardType node) {
		this.buffer.append("?");//$NON-NLS-1$
		Type bound = node.getBound();
		if (bound != null) {
			if (node.isUpperBound()) {
				this.buffer.append(" extends ");//$NON-NLS-1$
			} else {
				this.buffer.append(" super ");//$NON-NLS-1$
			}
			bound.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WhileStatement)
	 */
	public boolean visit(WhileStatement node) {
		printIndent();
		this.buffer.append("while (");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.buffer.append(") ");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	public boolean visit(DeclareParentsDeclaration node) {
		printIndent();
		this.buffer.append("declare parents: ");
		node.getChildTypePattern().accept(this);

		if (node.isExtends()) {
			this.buffer.append(" extends ");
		} else {
			this.buffer.append(" implements ");
		}

		for (Iterator<?> it = node.parentTypePatterns().iterator(); it
				.hasNext();) {
			TypePattern typePat = (TypePattern) it.next();
			typePat.accept(this);
			if (it.hasNext()) {
				this.buffer.append(", ");
			}
		}

		this.buffer.append(";\n");

		return false;
	}

	public boolean visit(DeclareWarningDeclaration node) {
		printIndent();

		this.buffer.append("declare warning: ");
		node.getPointcut().accept(this);
		this.buffer.append(" : ");
		node.getMessage().accept(this);
		this.buffer.append(" ;\n");
		return false;
	}

	public boolean visit(DeclareErrorDeclaration node) {
		printIndent();

		this.buffer.append("declare error: ");
		node.getPointcut().accept(this);
		this.buffer.append(" : ");
		node.getMessage().accept(this);
		this.buffer.append(" ;\n");
		return false;
	}

	public boolean visit(DeclareSoftDeclaration node) {
		printIndent();

		this.buffer.append("declare soft: ");
		node.getTypePattern().accept(this);
		this.buffer.append(" : ");
		node.getPointcut().accept(this);
		this.buffer.append(" ;\n");
		return false;
	}

	public boolean visit(DeclarePrecedenceDeclaration node) {
		printIndent();

		this.buffer.append("declare precedence: ");
		for (Iterator<?> it = node.typePatterns().iterator(); it.hasNext();) {
			TypePattern typePat = (TypePattern) it.next();
			typePat.accept(this);
			if (it.hasNext()) {
				this.buffer.append(", ");
			}
		}

		this.buffer.append(";\n");

		return false;
	}

	public boolean visit(DefaultTypePattern node) {
		this.buffer.append(node.getDetail());
		return false;
	}

	public boolean visit(DefaultPointcut node) {
		this.buffer.append(node.getDetail());
		return false;
	}
}
