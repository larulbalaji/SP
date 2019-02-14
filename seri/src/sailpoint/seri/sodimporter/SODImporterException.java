package sailpoint.seri.sodimporter;

public class SODImporterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1854485192844362742L;

	private String message=null;
	
	public SODImporterException(String msg) {
		this.message=msg;
	}
	
	@Override
	public String getMessage() {
		return message;
	}

	
}
