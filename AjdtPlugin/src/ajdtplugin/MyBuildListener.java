package ajdtplugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.IRelationship;
import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ajdt.core.builder.IAJBuildListener;
import org.eclipse.ajdt.core.lazystart.IAdviceChangedListener;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CategorizedProblem;

import com.adviceConstructs.Advice;
import com.adviceConstructs.AdviceAfter;
import com.adviceConstructs.AdviceAfterThrowing;
import com.adviceConstructs.AdviceBefore;
import com.declarations.MarkerText;

public class MyBuildListener implements IAJBuildListener {
	
	public void preAJBuild(int kind, IProject project,
			IProject[] requiredProjects) {
		// Called before building an AspectJ project..
	}

	public void postAJBuild(int kind, IProject project,boolean noSourceChanges,Map<IFile, List<CategorizedProblem>> newProblems) {
		
		if (noSourceChanges) {
			return;
		}
//		System.out.println(kind+"***");// kind = 9 : an automatic build request
		// Called whenever an AspectJ project is built..
		System.out.println("\nBegin...");
		
		AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(project);
		AJRelationshipType[] relTypes = { AJRelationshipManager.ADVISED_BY };

		// check first to see if there is a model
		// will return false if there has not yet been a successful build of
		// this project
		if (model.hasModel()) {

			// all relationships for project
			// can also query for relationships on individual elements or
			// compilation unit
			
			try {
				deleteAllMarkers(project,true);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			List<IRelationship> rels = (List<IRelationship>) model.getRelationshipsForProject(relTypes);
			for (IRelationship rel : rels) {
				
				System.out.println();				
//				AjNaiveASTFlattener visitor = null;				
				System.out.println("AspectJ relation: ");
				
				for (String targetHandle : (Iterable<String>) rel.getTargets()) {
					
					IJavaElement source = model.programElementToJavaElement(rel.getSourceHandle());
					AjNaiveASTFlattener visitor = new AjNaiveASTFlattener();
					
					// Can have multiple targets
					IJavaElement target = model.programElementToJavaElement(targetHandle);
					CompilationUnit cu3 = compileAst(target);
					cu3.accept(visitor);
					
					System.out.println("Target:");
					System.out.println(visitor.getResult());
					
					if (visitor.instAfter != null) {
						visitor.instAfter.setAdviceFileName(target.getResource().getLocation().toFile().getName());
						visitor.instAfter.setAdviceLineNumber(model.getJavaElementLineNumber(target));
						visitor.instAfter.setSourceLineNumber(model.getJavaElementLineNumber(source));
						visitor.instAfter.setSourceElementName(source.getElementName());
					}
					else if(visitor.instBefore != null){
						visitor.instBefore.setAdviceFileName(target.getResource().getLocation().toFile().getName());
						visitor.instBefore.setAdviceLineNumber(model.getJavaElementLineNumber(target));
						visitor.instBefore.setSourceLineNumber(model.getJavaElementLineNumber(source));
						visitor.instBefore.setSourceElementName(source.getElementName());
						visitor.instBefore.setPointcuts(visitor.points);
					}
					else if (visitor.instAftThrow != null) {
						visitor.instAftThrow.setAdviceFileName(target.getResource().getLocation().toFile().getName());
						visitor.instAftThrow.setAdviceLineNumber(model.getJavaElementLineNumber(target));
						visitor.instAftThrow.setSourceLineNumber(model.getJavaElementLineNumber(source));
						visitor.instAftThrow.setSourceElementName(source.getElementName());
					}

					visitor.reset();

					CompilationUnit cu2 = compileAst(source);
					visitor.setFile(source.getResource().getLocation().toFile());
					cu2.accept(visitor);
					
					System.out.println("Source:");
					System.out.println(visitor.getResult());
					
					switch (kind) {		                
		            case IncrementalProjectBuilder.AUTO_BUILD:
		            case IncrementalProjectBuilder.INCREMENTAL_BUILD:
						if (visitor.instAfter != null) {
							createMarker(project,source.getResource(),visitor.instAfter);
						}
						else if(visitor.instBefore != null){
							createMarker(project,source.getResource(),visitor.instBefore);
						}
						else if(visitor.instAftThrow != null){
							createMarker(project,source.getResource(),visitor.instAftThrow);
						}
						break;
					}
					
				}
				
	        	try {
					deleteAllMarkers(project,false);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void createMarker(IProject project,IResource resource, Advice advice) {
	    
        //First generate string message for the advice, afterwards create the marker
        try {        	
        	MarkerText txt = new MarkerText(advice);        	
        	IMarker marker = null;
        	
        	String label;
            label = txt.toString();
        	
        	if(advice instanceof AdviceAfter){
        		marker = resource.createMarker("AjdtPlugin.afteradvicemarker");
        	}
        	else if(advice instanceof AdviceBefore){
        		marker = resource.createMarker("AjdtPlugin.beforeadvicemarker");
        	}
        	else if(advice instanceof AdviceAfterThrowing){
        		marker = resource.createMarker("AjdtPlugin.afterexadvicemarker");
        	}
        	
            marker.setAttribute(IMarker.LINE_NUMBER, advice.getSourceLineNumber());
            marker.setAttribute(IMarker.MESSAGE, label);
            marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);   
            
            
        }
        catch (CoreException e) {
        }        
	}	
	
	private CompilationUnit compileAst(IJavaElement source) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setCompilerOptions(new HashMap<Object, Object>());
		try {
			parser.setSource(readFileAsString(source.getResource().getLocation().toFile()).toCharArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		CompilationUnit cu2 = (CompilationUnit) parser.createAST(null);
		return cu2;
	}
	
	private void deleteAllMarkers(IProject project,boolean check) throws CoreException{
		// Delete all the existing markers
      
            project.deleteMarkers(IAJModelMarker.ADVICE_MARKER,
                    true, IResource.DEPTH_INFINITE);           
    
            project.deleteMarkers(
                    IAJModelMarker.SOURCE_ADVICE_MARKER, true,
                    IResource.DEPTH_INFINITE);        
       
            project.deleteMarkers(
                    IAJModelMarker.DECLARATION_MARKER, true,
                    IResource.DEPTH_INFINITE);       
       
            if(check){
	            project.deleteMarkers(IAJModelMarker.CUSTOM_MARKER,
	                    true, IResource.DEPTH_INFINITE);                
	        
	            project.deleteMarkers("AjdtPlugin.afteradvicemarker",
	                    true, IResource.DEPTH_INFINITE);
	            
	            project.deleteMarkers("AjdtPlugin.beforeadvicemarker",
	                    true, IResource.DEPTH_INFINITE);
	            
	            project.deleteMarkers("AjdtPlugin.afterexadvicemarker",
	                    true, IResource.DEPTH_INFINITE);
            }
       
	}
	/**
	 * Reads an entire text file into a String
	 * 
	 * @param file
	 *            the text file to be read
	 * @throws IOException
	 *             if the file cannot be read
	 */
	private static String readFileAsString(File file)
			throws java.io.IOException {
		byte[] buffer = new byte[(int) file.length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(file));
			f.read(buffer);
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		return new String(buffer);
	}

	public void addAdviceListener(IAdviceChangedListener adviceListener) {
		// Doesn't seem to work..

	}

	@Override
	public void removeAdviceListener(IAdviceChangedListener adviceListener) {
		// Doesn't seem to work..

	}

	public void postAJClean(IProject project) {
		// Called after cleaning an AsppectJ project..
	}

}
