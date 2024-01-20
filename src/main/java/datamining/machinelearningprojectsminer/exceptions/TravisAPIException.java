/**
 * 
 */
package datamining.machinelearningprojectsminer.exceptions;

public class TravisAPIException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TravisAPIException(String message){
		super(message);
	}
	
	public TravisAPIException(Exception e){
		super(e);
	}

}