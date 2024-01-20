/**
 * 
 */
package datamining.machinelearningprojectsminer.exceptions;

public class GitWrapperException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public GitWrapperException(String message){
		super(message);
	}
	
	public GitWrapperException(Exception e){
		super(e);
	}

}