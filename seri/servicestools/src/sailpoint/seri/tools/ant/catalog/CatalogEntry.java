package sailpoint.seri.tools.ant.catalog;

public class CatalogEntry {

	private String ceName;
	private boolean ceValid;
	
	public CatalogEntry(String ceName) {
		this.ceName=ceName;
		this.ceValid=true;
	}
	
	public void setValid(boolean valid) {
		this.ceValid=valid;
	}

	public String getName() {
		return ceName;
	}

	public boolean isValid() {
		return ceValid;
	}
	
	
	
}
