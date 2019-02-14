package sailpoint.seri.tools.ant.catalog;

import java.util.ArrayList;
import java.util.List;

public class CatalogSection {

	private String sectionName;
	private List<CatalogEntry> sectionEntries;
	
	public CatalogSection(String name) {
		this.sectionName=name;
		this.sectionEntries=new ArrayList<CatalogEntry>();
	}
	
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	
	public List<CatalogEntry> getEntries() {
		return sectionEntries;
	}
	
	public void addEntry(CatalogEntry entry) {
		sectionEntries.add(entry);
	}
	
	
}
