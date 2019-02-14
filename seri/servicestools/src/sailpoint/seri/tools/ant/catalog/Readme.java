package sailpoint.seri.tools.ant.catalog;

import java.util.ArrayList;
import java.util.List;

public class Readme {

	private String name;
	private String version;
	
	private List<Section> sections;
	
	public class Section {
		private List<String>title;
		private List<String> contents;
		
		public Section(List<String> title, List<String> contents) {
			this.title=title;
			this.contents=contents;
		}
		
		public List<String>getTitle() {
			return title;
		}
		public void setTitle(ArrayList<String> title) {
			this.title = title;
		}
		public List<String> getContents() {
			return contents;
		}
		public void setContents(List<String> contents) {
			this.contents = contents;
		}
		
		}
	
	public Readme() {
		sections=new ArrayList<Section>();
	}
	
	public void setName(String name) {
		this.name=name;
	}

	public void setVersion(String version) {
		this.version=version;
	}

	public void addContent(List<String> title, List<String> content) {
		// TODO Auto-generated method stub
		Section s=new Section(title, content);
		sections.add(s);
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public List<Section> getSections() {
		return sections;
	}

}	

