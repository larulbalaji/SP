package sailpoint.seri.sodimporter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kevin.james@sailpoint.com
 * 
 * SOD List Importer
 * This is a class that represents the functionality of importing a CSV or XLS file containing
 * a list of possible policy violations. The list is read and pairs are added as rules to a Policy object
 * if Entitlements are used, we look those up from a second sheet/file
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

public class SODListImporter extends BaseImporter{

	private Roletype roletype=Importer.Roletype.INVALID;

	private Source labelSource=Importer.Source.INVALID;

	private boolean ignoreCase=true;

	int leftCol=-1;
	int rightCol=-1;

	boolean entitlementsSeparate=false;
	String entitlementFilename=null;
	Importer.Filetype entType=Importer.Filetype.INVALID;
	private String entitlementSheetName=null;
	private int labelIndex=-1;
	private int entRefCol=-1;
	private int entValueCol=-1;
	private int entStartRow=-1;

	private String[] names;
	private boolean namesInitialized=false;


	/**
	 * 
	 * Constructor
	 * 
	 * @param filename String representing the name of the file
	 * @param type Either Filetype.CSV or Filetype.XLS
	 */
	public SODListImporter(String filename, Filetype type) throws SODImporterException {

		super(filename, type);

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

		// we are subtracting one here because we expose through the UI that columns/rows start at 1

		int[] columns={leftCol-1, rightCol-1};
		ArrayList<ArrayList<String>> data=getSODFileColumns(startRow-1, columns);

		List<SODPair> violations=new ArrayList<SODPair>();

		for (ArrayList<String> row: data) {
			// at this point I know the row has two entries
			violations.add(new SODPair(row.get(0), row.get(1), null));
		}
		return violations;
	}

	protected ArrayList<ArrayList<String>> getEntitlementFileColumns(int startRow, int[] columns) throws SODImporterException{

		if (type.equals(Filetype.CSV)) {
			return getCSVColumns(entitlementFilename, startRow, columns);
		} else if (type.equals(Filetype.XLS)) {
			return getXLSColumns(entitlementFilename, entitlementSheetName, startRow, columns);
		}

		throw new SODImporterException("Filetype "+type+" Not yet implemented");
	}

	public Map<String, List<String>> getEntitlementsMap() throws SODImporterException {

		checkParms();

		// we are subtracting one here because we expose through the UI that columns/rows start at 1

		int[] columns={entRefCol-1, entValueCol-1};
		ArrayList<ArrayList<String>> data=getEntitlementFileColumns(startRow-1, columns);

		Map<String, List<String>> violations=new HashMap<String, List<String>>();

		for (ArrayList<String> row: data) {
			// at this point I know the row has two entries
			// ref (name in first file/sheet), entitlement value
			String ref=row.get(0);
			String val=row.get(1);
			List<String> reference=violations.get(ref);
			if(reference==null) reference=new ArrayList<String>();
			reference.add(val);
			violations.put(ref, reference);
		}

		return violations;
	}

	public void checkParms() throws SODImporterException {

		super.checkParms();

		if(leftCol<1) {
			throw new SODImporterException("Left Column must be greater than zero");
		}
		if(rightCol<1) {
			throw new SODImporterException("Left Column must be greater than zero");
		}
		if(startRow<1) {
			throw new SODImporterException("Start Row must be specified and greater than zero");
		}
		if(entitlementsSeparate) {
			if(entitlementFilename==null) {
				throw new SODImporterException("With separate entitlements, must specify an entitlements file");
			}
			File f=new File(entitlementFilename);
			if(!f.exists()) {
				throw new SODImporterException("separate entitlements file '"+filename+"' does not exist");
			}
			if(entType==Importer.Filetype.INVALID) {
				throw new SODImporterException("separate entitlements file type must be specified");
			}
			if(entType==Importer.Filetype.XLS) {
				if (entitlementSheetName==null) {
					throw new SODImporterException("With separate entitlements in XLS, must specify an entitlements sheet name");
				}
				if(entRefCol==-1) {
					throw new SODImporterException("With separate entitlements in XLS, must specify an entitlement reference column");
				}
				if(entValueCol==-1) {
					throw new SODImporterException("With separate entitlements in XLS, must specify an entitlement value column");
				}
				if(entStartRow==-1) {
					throw new SODImporterException("With separate entitlements in XLS, must specify an entitlement start row");
				}
			}
		}

	}

	public void setLeftColumn(int leftCol) {
		this.leftCol=leftCol;		
	}
	public void setRightColumn(int rightCol) {
		this.rightCol=rightCol;		
	}
	public void setStartRow(int sr) {
		this.startRow=sr;		
	}

	public void setEntitlementsSeparate(boolean entitlementsSeparate) {
		this.entitlementsSeparate = entitlementsSeparate;
	}

	public void setEntitlementFilename(String entitlementFilename) {
		this.entitlementFilename = entitlementFilename;
	}

	public void setEntitlementType(Filetype eType) {
		this.entType=eType;
	}

	public void setEntitlementSheetName(String string) {
		this.entitlementSheetName=string;
	}

	public void setEntRefCol(int entRefCol) {
		this.entRefCol = entRefCol;
	}

	public void setEntValueCol(int entValueCol) {
		this.entValueCol = entValueCol;
	}

	public void setEntStartRow(int entStartRow) {
		this.entStartRow = entStartRow;
	}

}

