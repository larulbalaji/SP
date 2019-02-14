package sailpoint.services.tools.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileSet;


public class BuildInitTask extends Task {
	private String _initFile;
	private String _prefix;
	private List<FileSet> filesets = new ArrayList<FileSet>();
	private List<FileList> filelists = new ArrayList<FileList>();
	private static final String _header = "<?xml version='1.0' encoding='UTF-8'?>\n"
		+ "<!DOCTYPE sailpoint PUBLIC 'sailpoint.dtd' 'sailpoint.dtd'>\n"
		+ "<sailpoint>\n";
	private static final String _importTag = "<ImportAction name='include' value='";
	private static final String _footer = "</sailpoint>\n";
	
	public void execute() throws BuildException {
		if (_prefix == null)
			_prefix = "";
		if (_initFile == null)
			throw new BuildException("You must sepecify init file name.");
		else if (filesets.size() == 0 )
			throw new BuildException("You must include a file set to create a init file for");
		else { // lets create an init file 
			log("Creating init file " + _initFile);
			try {
				BufferedWriter output = new BufferedWriter(new FileWriter(new File(_initFile)));
				output.write(_header);
				// iterate over the filset and write the files
				for (FileSet fs: filesets) {
					DirectoryScanner ds = fs.getDirectoryScanner(getProject());
					String[] files = ds.getIncludedFiles();
	                // Loop through files
	                for (String file: files) {
	                	output.write("  " + _importTag + _prefix + file + "'/>\n");
	                	log("Including file " + file);
	                }
				}
				for (FileList fl: filelists) {
			 	  String[] files=fl.getFiles(getProject());
				  if (files==null) {
				    log("fileList returns null files");				   
		 		  } else {
	 			    // Loop through files
 				    for (String file: files) {
				      output.write("  " + _importTag + _prefix + file + "'/>\n");
				      log("Including file " + file);
				    }
				  }
				}
				output.write(_footer);
				output.close();
			} catch (IOException e) {
				throw new BuildException(e);
			}
		}
	}
	
  public void addFileSet(FileSet fileset) {
  	if (!filesets.contains(fileset)) {
  		filesets.add(fileset);
  	}
  }
  
  public void addFileList(FileList filelist) {
    if (!filelists.contains(filelist)) {
      filelists.add(filelist);
    }
  }
  
	public void setInitFile (String file) {
		_initFile = file;
	}
	
	public void setPrefix (String prefix) {
		_prefix = prefix;
	}
	
}
