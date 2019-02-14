package sailpoint.seri.tools.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public class AddColumnTask extends Task{


	/*
	 * 	 takes a csv file and adds a column to the header line, and then a value to all other lines
	 */	



	private Vector<FileSet> filesets = new Vector<FileSet>();
	private String columnName = null;
	private String columnValue = null;

	public void execute() throws BuildException {
		if (columnName==null) {
			throw new BuildException("Column name must be specified");
		}
		if (columnValue==null) {
			throw new BuildException("Column value must be specified");
		}
		if (filesets.size() == 0 )
			throw new BuildException("You must include some files");
		else { // lets modify some files! 

			int numFiles=0;
			for (FileSet fs: filesets) {
				numFiles+=fs.size();
			}

			log("Adding column to "+numFiles+" files ");

			for (FileSet fs: filesets) {
				addColumn(fs);
			}

		}
	}

	public void addFileSet(FileSet fileset) {
		if (!filesets.contains(fileset)) {
			filesets.add(fileset);
		}
	}

	public void setName(String name) {
		this.columnName=name;
	}

	public void setValue(String value) {
		this.columnValue=value;
	}

	private void addColumn(FileSet fs) {
		DirectoryScanner ds = fs.getDirectoryScanner(getProject());
		String[] files = ds.getIncludedFiles();
		// Loop through files
		for (String file: files) {

			String newFile=ds.getBasedir().getAbsolutePath()+File.separatorChar+file;
			File theFile=new File(newFile);

			log("addColumn: " + newFile, Project.MSG_DEBUG);

			try {
				FileReader bis=new FileReader(theFile);
				BufferedReader br=new BufferedReader(bis);
				StringBuilder sb=new StringBuilder();

				String s=null;
				boolean first=true;
				boolean notAlreadyAdded=true;

				while((s=br.readLine())!=null && notAlreadyAdded) {
					sb.append(s);
					sb.append(",");
					if(first) {
						String[] columns=s.split(",");
						for(String col: columns) {
							if(columnName.equals(col)) {
								log("File "+file+" already has column "+columnName);
								notAlreadyAdded=false;
							}
						}
						sb.append(columnName);
						first=false;
					} else {
						sb.append(columnValue);
					}
					sb.append("\n");
				}
				br.close();
				bis.close();
				if (notAlreadyAdded) {
					FileWriter fw=new FileWriter(theFile);
					fw.write(sb.toString());
					fw.close();
				}
			} catch (FileNotFoundException e) {
				throw new BuildException("File not found: "+e);
			} catch (IOException e) {
				throw new BuildException("IOException: "+e);
			}
		}
	}
}
