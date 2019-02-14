package sailpoint.seri.sodimporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kevin.james@sailpoint.com
 * 
 * SOD Importer
 * This is a class that represents the functionality of importing a CSV or XLS file containing
 * a matrix of possible policy violations. The matrix is read and if a cell contains a value in the
 * activeSOD list, the header of the row and column are put into the SOD list.
 * 
 * Dependencies: sailpoint.tools.RFC4180Line{Iterator,Parser} - i.e. identityiq.jar
 * 
 * It is the responsibility of the calling code to:
 * 
 * - Instantiate with a filename and filetype
 * - Set the column/row (and indicate which) that contains the labels of the entitlements/roles
 * - Set the column containing the names of the entitlements/roles
 * - Set the row containing the names of the entitlements/roles
 * - Set the column where the table starts
 * - Set the row where the table starts
 * - Set the number of combinations (width of table)
 * - Set the values that represent a policy violation in the table
 * - retrieve the SOD Map (a list of String, String pairs)
 * 
 * For example:
 * 
 * SODImporter si=new SODImporter ("/tmp/sod.xls", Filetype.CSV);
 * si.setLabel(Source.COLUMN, 0);
 * si.setNameColumn(1);
 * si.setNameRow(1);
 * si.setData(2, 2, 20); // start at 2,2 with 20 combinations
 * si.setActiveSODcsvString("x,true,yes");
 * 
 * List<SODPair> violations=si.getSODMap();
 * 
 * Notes:
 * All numbering for data files starts at 1 (not 0).
 *
 */

public class SODMatrixImporter extends BaseImporter{

	private Importer.Roletype roletype=Importer.Roletype.INVALID;

	private Source labelSource=Importer.Source.INVALID;

	private String activeSODcsvString=null; 

	private boolean ignoreCase=true;

	private int labelIndex=-1;

	private String[] names;
	private boolean namesInitialized=false;

	/**
	 * 
	 * Constructor
	 * 
	 * @param filename String representing the name of the file
	 * @param type Either Filetype.CSV or Filetype.XLS
	 */
	public SODMatrixImporter(String filename, Filetype type) throws SODImporterException {

		super(filename, type);

	}

	public String getActiveSODcsvString() {
		return activeSODcsvString;
	}

	public void setRoletype(Roletype r) {
		this.roletype=r;
	}

	/*
	 * Set the source of the Labels for the entitlements/roles
	 * 
	 * @param src either Source.COLUMN or Source.ROW
	 * @param idx the number of the column/row containing the labels (names)
	 */
	public void setLabel(Source src, int idx) {
		this.labelSource=src;
		this.labelIndex=idx-1; // we expose this as starting from one
	}

	public String getRoleTypeString() {
		if(roletype==Roletype.BUSINESS) {
			return "business";
		}
		if(roletype==Roletype.IT) {
			return "it";
		}
		return "invalid";
	}

	/**
	 * The SODImporter has to check the values in the cells to see if that value indicates this combination
	 * is a policy violation. Pass in a comma separated string of possible values
	 * 
	 * @param activeSODcsvString the possible values that indicate a policy violation (e.g "x,yes,true")
	 * 
	 */
	public void setActiveSODcsvString(String activeSODcsvString) {
		this.activeSODcsvString = activeSODcsvString;
	}

	/**
	 * 
	 * Set the source of the names for the entitlements/roles, in row format
	 * 
	 * @param idx the number of the row containing the names
	 */
	public void setNameRow(int idx) {
		this.nameRowIndex=idx-1; // we expose this as starting from one
	}

	public void setData(int startRow, int startCol, int numCombinations) {
		this.startRow=startRow-1; // we expose this as starting from one
		this.startCol=startCol-1; 
		this.numCombinations=numCombinations;
	}

	public String[] getNames() throws SODImporterException {
		if (!namesInitialized) {
			if(roletype!=Roletype.INVALID) {
				throw new SODImporterException("Column/Row names not initialized; getSODMap not called");
			} else {
				throw new SODImporterException("Column/Row names not initialized; role type not set");
			}
		}
		return names;
	}

	public List<SODPair> getSODMap() throws SODImporterException {

		checkParms();

		// get the lowest and highest row and column number we need to see
		int minRow=Integer.MAX_VALUE;
		int maxRow=0;
		int minCol=Integer.MAX_VALUE;
		int maxCol=0;

		if(labelSource==Source.COLUMN) {
			if(labelIndex<minCol && labelIndex>0) minCol=labelIndex;
			if(labelIndex>maxCol) maxCol=labelIndex;
		} else {
			if(labelIndex<minRow && labelIndex>0) minRow=labelIndex;
			if(labelIndex>maxRow) maxRow=labelIndex;
		}
		if(nameColIndex<minCol) minCol=nameColIndex;
		if(nameColIndex>maxCol) maxRow=nameColIndex;
		if(nameRowIndex<minRow) minRow=nameRowIndex;
		if(nameRowIndex>maxRow) maxRow=nameRowIndex;

		int strtC = startCol+numCombinations; // -1 since we *start* at startCol
		int strtR = startRow+numCombinations; // -1 since we *start* at startRow;
		if( strtC<minCol ) minCol=strtC;
		if( strtC>maxCol ) maxCol=strtC;
		if( strtR<minRow ) minRow=strtR;
		if( strtR>maxRow ) maxRow=strtR;

		String[][] data=getFileData(minCol, maxCol, minRow, maxRow);

		List<String> violationValues=Arrays.asList(activeSODcsvString.split(","));

		List<SODPair> violations=new ArrayList<SODPair>();

		Set<String> nameset=new HashSet<String>();

		for (int row=startRow-minRow; row<(strtR-minRow);row++) {

			for (int col=startCol-minCol; col<(strtC-minCol);col++) {
				String value=data[row][col];
				for(String vValue: violationValues) {
					String nameRow=data[row][nameColIndex-minCol];
					String nameCol=data[nameRowIndex-minRow][col];
					// log.debug("SODImporter.getSODMap: ["+nameRow+","+nameCol+"] compare "+vValue+" and "+value);
					if(vValue.equalsIgnoreCase(value)) {
						violations.add(new SODPair(nameRow, nameCol,value));
						if(roletype!=Roletype.INVALID) {
							nameset.add(nameRow);
							nameset.add(nameCol);
						}
					}
				}
			}
		}

		if(roletype!=Roletype.INVALID) {
			names=nameset.toArray(new String[0]);
			namesInitialized=true;
		}
		return violations;
	}

	public void checkParms() throws SODImporterException {

		super.checkParms();

		if(nameColIndex==-1) {
			throw new SODImporterException("SODImporter: no name columnspecified");
		}
		if(nameRowIndex==-1) {
			throw new SODImporterException("SODImporter: no name row specified");
		}
		if(startCol==-1) {
			throw new SODImporterException("SODImporter: no data start column specified");
		}
		if(startRow==-1) {
			throw new SODImporterException("SODImporter: no data start row specified");
		}
		if(numCombinations==-1) {
			throw new SODImporterException("SODImporter: no data size specified");
		}
		if(activeSODcsvString==null) {
			throw new SODImporterException("SODImporter: no active SOD identifiers specified");
		}

	}
}

