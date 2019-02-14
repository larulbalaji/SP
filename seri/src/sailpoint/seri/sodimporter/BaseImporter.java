package sailpoint.seri.sodimporter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import sailpoint.tools.GeneralException;
import sailpoint.tools.RFC4180LineIterator;
import sailpoint.tools.RFC4180LineParser;
import sailpoint.tools.Util;

/**
 * @author kevin.james@sailpoint.com
 * 
 * Simple Importer
 * This is a class that represents the functionality of importing a CSV or XLS file containing
 * a matrix of possible 'somethings'. The matrix is read and if a cell contains a value in the
 * active list, the header of the row and column are put into the a map of hashlists.
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


public class BaseImporter implements Importer {

	private static Log log = LogFactory.getLog(BaseImporter.class);

	protected String filename=null;
	protected Filetype type=Importer.Filetype.INVALID;

	protected int nameColIndex=-1;
	protected int nameRowIndex=-1;

	protected int startCol=-1;
	protected int startRow=-1;
	protected int numCombinations=-1;

	protected int sheetNumber=-1;
	protected String sheetName=null;

	protected FormulaEvaluator evaluator;

	/**
	 * 
	 * Constructor
	 * 
	 * @param filename String representing the name of the file
	 * @param type Either Filetype.CSV or Filetype.XLS
	 */
	public BaseImporter(String filename, Filetype type) throws SODImporterException {

		if(filename==null) {
			throw new SODImporterException("Filename must be specified in constructor");
		}

		if(type==null) {
			throw new SODImporterException("File type must be specified in constructor");
		}

		this.type=type;
		this.filename=filename;

	}

	public int getSheetNumber() {
		return sheetNumber;
	}

	public void setSheetNumber(int sheetNumber) {
		this.sheetNumber = sheetNumber;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	/**
	 * 
	 * Set the source of the names for the entitlements/roles, in column format
	 * 
	 * @param idx the number of the row containing the names
	 */
	public void setNameColumn(int idx) {
		this.nameColIndex=idx-1; // we expose this as starting from one
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

	public ArrayList<ArrayList<String>> getSimpleMap() throws SODImporterException {

		checkParms();

		// get the lowest and highest row and column number we need to see
		int minRow=Integer.MAX_VALUE;
		int maxRow=0;
		int minCol=Integer.MAX_VALUE;
		int maxCol=0;

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

		ArrayList<ArrayList<String>> rowList=new ArrayList<ArrayList<String>>();


		for (int row=startRow-minRow; row<(strtR-minRow);row++) {

			ArrayList<String>valueRow=new ArrayList<String>();
			for (int col=startCol-minCol; col<(strtC-minCol);col++) {
				String value=data[row][col];
				valueRow.add(value);
			}
			rowList.add(valueRow);

		}
		return rowList;
	}


	protected String[][] getFileData(int minCol, int maxCol, int minRow,
			int maxRow) throws SODImporterException{

		if (type.equals(Filetype.CSV)) {
			return getCSVFileData(minCol, maxCol, minRow, maxRow);
		} else if (type.equals(Filetype.XLS)) {
			return getXLSFileData(minCol, maxCol, minRow, maxRow);
		}

		throw new SODImporterException("Filetype "+type+" Not yet implemented");
	}

	private String[][] getCSVFileData(int minCol, int maxCol, int minRow,
			int maxRow) throws SODImporterException{

		String[][] retval=new String[maxRow-minRow][maxCol-minCol];

		try {
			BufferedReader reader=new BufferedReader(new InputStreamReader(getFileStream()));

			// Now we set up a RFC4180 Line Iterator and Line Parser
			// Note: this will need identityiq.jar on the ClassPath
			RFC4180LineIterator iter=new RFC4180LineIterator(reader);
			int row=0;
			while (row<minRow) {
				String s=iter.readLine();
				row++;
			}
			while(row<maxRow) {
				String line=iter.readLine();
				RFC4180LineParser parser=new RFC4180LineParser(',');
				List<String> cells=parser.parseLine(line);
				String[] theRow=new String[maxCol-minCol];
				for(int i=minCol;i<maxCol;i++) {
					theRow[i-minCol]=cells.get(i);
				}
				retval[row-minRow]=theRow;
				row++;
			}
			return retval;

		} catch (FileNotFoundException fnfe) {
			throw new SODImporterException("Can't find source file "+filename);
		} catch (IOException ioe) {
			throw new SODImporterException("IOException reading from '"+filename+"' : "+ioe);
		} catch (GeneralException ge) {
			throw new SODImporterException("GeneralException reading from '"+filename+"' : "+ge);
		}
	}

	private String[][] getXLSFileData(int minCol, int maxCol, int minRow,
			int maxRow) throws SODImporterException{

		String[][] retval=new String[maxRow-minRow][maxCol-minCol];

		try{
			InputStream fileIn = getFileStream();
			Workbook wb = WorkbookFactory.create(fileIn);
			evaluator = wb.getCreationHelper().createFormulaEvaluator();

			if (wb.getNumberOfSheets()<sheetNumber) {
				throw new SODImporterException("Invalid sheet number "+sheetNumber);
			}

			Sheet sht=null;
			if(sheetNumber!=-1) {
				sht=wb.getSheetAt(sheetNumber);
			} else {
				sht=wb.getSheet(sheetName);
			}
			if(sht==null) {
				String msg="Unable to find sheet ";
				if(sheetNumber==-1) {
					msg+="'"+sheetName+"'";
				} else {
					msg+="number "+sheetNumber;
				}
				throw new SODImporterException(msg);
			}

			for(int rowCounter=minRow; rowCounter<maxRow; rowCounter++) {
				String[] theRow=new String[maxCol-minCol];
				Row row=sht.getRow(rowCounter);
				for(int colCounter=minCol; colCounter<maxCol; colCounter++) {
					String celValue = "";
					Cell cel=row.getCell(colCounter);
					if(cel!=null) celValue = getValue(cel);
					theRow[colCounter-minCol]=celValue;
				}
				retval[rowCounter-minRow]=theRow;
			}

			return retval;

		} catch (FileNotFoundException fnfe) {
			throw new SODImporterException("Can't find source file "+filename);
		} catch (InvalidFormatException ife ) {
			throw new SODImporterException("Invalid XLS source file "+filename);
		} catch (IOException ioe) {
			throw new SODImporterException("IOException reading from '"+filename+"' : "+ioe);
		}
	}

	protected ArrayList<ArrayList<String>> getSODFileColumns(int startRow, int[] columns) throws SODImporterException{

		if (type.equals(Filetype.CSV)) {
			return getCSVColumns(filename, startRow, columns);
		} else if (type.equals(Filetype.XLS)) {
			return getXLSColumns(filename, sheetName, startRow, columns);
		}

		throw new SODImporterException("Filetype "+type+" Not yet implemented");
	}

	protected ArrayList<ArrayList<String>> getCSVColumns(String filename, int startRow, int[]columns) throws SODImporterException{

		ArrayList<ArrayList<String>> retval=new ArrayList<ArrayList<String>>();

		try {
			BufferedReader reader=new BufferedReader(new InputStreamReader(getFileStream()));

			// Now we set up a RFC4180 Line Iterator and Line Parser
			// Note: this will need identityiq.jar on the ClassPath
			RFC4180LineIterator iter=new RFC4180LineIterator(reader);
			int row=0;
			while (row<startRow) {
				String s=iter.readLine();
				row++;
			}
			String line=null;
			while((line=iter.readLine())!=null) {
				RFC4180LineParser parser=new RFC4180LineParser(',');
				ArrayList<String> out=new ArrayList<String>();
				List<String> cells=parser.parseLine(line);
				for(int col: columns) {
					if(col<cells.size()-1){ // remember, col is 1-rooted whereas the LIST is 0-rooted
						out.add(cells.get(col-1));
					} else {
						out.add("");
					}
				}
				retval.add(out);
				row++;
			}
			return retval;

		} catch (FileNotFoundException fnfe) {
			throw new SODImporterException("Can't find source file "+filename);
		} catch (IOException ioe) {
			throw new SODImporterException("IOException reading from '"+filename+"' : "+ioe);
		} catch (GeneralException ge) {
			throw new SODImporterException("GeneralException reading from '"+filename+"' : "+ge);
		}
	}

	protected ArrayList<ArrayList<String>> getXLSColumns(String filename, String sheetName, int startRow, int[] columns) throws SODImporterException{

		ArrayList<ArrayList<String>> retval=new ArrayList<ArrayList<String>>();

		try{
			InputStream fileIn = getFileStream();
			Workbook wb = WorkbookFactory.create(fileIn);
			evaluator = wb.getCreationHelper().createFormulaEvaluator();

			if (wb.getNumberOfSheets()<sheetNumber) {
				throw new SODImporterException("Invalid sheet number "+sheetNumber);
			}

			Sheet sht=null;
			if(sheetNumber!=-1) {
				sht=wb.getSheetAt(sheetNumber);
			} else {
				sht=wb.getSheet(sheetName);
			}
			if(sht==null) {
				String msg="Unable to find sheet ";
				if(sheetNumber==-1) {
					msg+="'"+sheetName+"'";
				} else {
					msg+="number "+sheetNumber;
				}
				throw new SODImporterException(msg);
			}

			for(int rowCounter=startRow; rowCounter<=sht.getLastRowNum(); rowCounter++) {
				ArrayList<String> out=new ArrayList<String>();
				Row row=sht.getRow(rowCounter);
				for(int col: columns) {
					String celValue = "";
					Cell cel=row.getCell(col);
					if(cel!=null) celValue = getValue(cel);
					out.add(celValue);
				}
				retval.add(out);
			}

			return retval;

		} catch (FileNotFoundException fnfe) {
			throw new SODImporterException("Can't find source file "+filename);
		} catch (InvalidFormatException ife ) {
			throw new SODImporterException("Invalid XLS source file "+filename);
		} catch (IOException ioe) {
			throw new SODImporterException("IOException reading from '"+filename+"' : "+ioe);
		}
	}

	private String getValue(Cell cell){
		String cellContents="";


		if(cell!=null) {
			CellValue cv=evaluator.evaluate(cell);
			if(cv!=null) {
				switch(cv.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC: cellContents=Double.toString(cv.getNumberValue()); break;
				case Cell.CELL_TYPE_BOOLEAN: cellContents=Boolean.toString(cv.getBooleanValue()); break;
				default: cellContents=cv.getStringValue();
				}
			}
		}
		return cellContents;
	}

	protected void checkParms() throws SODImporterException {

		if(type==Importer.Filetype.INVALID) {
			throw new SODImporterException("SODImporter: Invalid type specified");
		}
		if(filename==null) {
			throw new SODImporterException("SODImporter: no input file specified");
		}
		try {
			getFileStream();
		} catch (IOException ioe) {
			throw new SODImporterException("SODImporter: input file '"+filename+"' does not exist");
		}
		if(type==Filetype.XLS && sheetNumber==-1 && sheetName==null) {
			throw new SODImporterException("SODImporter: no sheet specified");
		}


	}

	/**
	 * Get the input File Stream.
	 */
	private InputStream getFileStream() throws IOException {
		InputStream stream = null;

		if (filename == null) {
			throw new IOException("Filename cannot be null.");
		}
		try {
			File file = new File(filename);
			if (!file.exists()) {
				// sniff the file see if its relative if it is
				// see if we can append sphome to find it
				if (!file.isAbsolute()) {
					String appHome = getAppHome();
					if (appHome != null) {
						file = new File(appHome + File.separator + filename);
						if (!file.exists()) {
							file = new File(filename);
						}
					}
				}
			}
			// This will throw an exception if the file cannot be found
			stream = new BufferedInputStream(new FileInputStream(file));
		} catch (Exception e) {
			throw new IOException(e);
		}

		return stream;
	}

	   /**
	    * Try to get the app home to be smart about locating relative paths.
	    */
	   private String getAppHome() {
	      String home = null;
	      try {
	         home = Util.getApplicationHome();
	      } catch (Exception e) {
	         log.error("Unable to find application home.");
	      }
	      return home;
	   }

}


