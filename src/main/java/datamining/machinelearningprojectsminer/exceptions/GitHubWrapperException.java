/**
 * 
 */
package datamining.machinelearningprojectsminer.exceptions;

public class GitHubWrapperException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public GitHubWrapperException(String message){
		super(message);
	}
	
	public GitHubWrapperException(Exception e){
		super(e);
	}

}