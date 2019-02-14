package sailpoint.seri.sodimporter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import sailpoint.seri.sodimporter.Importer.Filetype;
import sailpoint.seri.sodimporter.Importer.Source;

public class SODImporterTest {
	
	public static List<SODPair> result1;
	public static List<SODPair> result2;
	
	@BeforeClass
    public static void testSetup() {
		
		ClassLoader classloader =
				   org.apache.poi.poifs.filesystem.POIFSFileSystem.class.getClassLoader();
		URL res = classloader.getResource(
		             "org/apache/poi/poifs/filesystem/POIFSFileSystem.class");
		String path = res.getPath();
		System.out.println("Core POI came from " + path);
			
		result1=new ArrayList<SODPair>();
		SODPair pr=new SODPair("L1", "L2", "x");
		result1.add(pr);
		pr=new SODPair("L2", "L3", "x");
		result1.add(pr);
		
		result2=new ArrayList<SODPair>();
		pr=new SODPair("L1", "L2", "x");
		result2.add(pr);
		pr=new SODPair("L2", "L3", "x");
		result2.add(pr);
		pr=new SODPair("L2", "L4", "TRUE");
		result2.add(pr);
		pr=new SODPair("L3", "L4", "yes");
		result2.add(pr);
		
	}


	@Test(expected= SODImporterException.class) 
	public void checkNullFilename() throws SODImporterException{
		SODListImporter si=new SODListImporter(null, SODListImporter.Filetype.CSV);
	}

	@Test(expected= SODImporterException.class) 
	public void checkNullFiletype() throws SODImporterException{
		SODListImporter si=new SODListImporter("/tmp/test.csv", null);
	}
	
	@Test(expected= SODImporterException.class)
	public void testSODStringAvailability() throws SODImporterException {
		SODListImporter si=new SODListImporter("test/resources/test1.csv", Filetype.CSV);
		si.setLabel(Source.COLUMN, 1);
		si.setNameColumn(2);
		si.setNameRow(1);
		si.setData(2, 3, 4);
		List<SODPair> pairs=si.getSODMap();
	}

	@Test
	public void testCSVFileImportSingleFlag() throws SODImporterException {
		SODMatrixImporter si=new SODMatrixImporter("test/resources/test1.csv", Filetype.CSV);
		si.setLabel(Source.COLUMN, 1);
		si.setNameColumn(2);
		si.setNameRow(1);
		si.setData(2, 3, 4);
		si.setActiveSODcsvString("x");
		List<SODPair> pairs=si.getSODMap();
		Assert.assertEquals(result1, pairs);		
	}
	
	@Test
	public void testCSVFileImportMultiFlag() throws SODImporterException {
		SODMatrixImporter si=new SODMatrixImporter("test/resources/test1.csv", Filetype.CSV);
		si.setLabel(Source.COLUMN, 1);
		si.setNameColumn(2);
		si.setNameRow(1);
		si.setData(2, 3, 4);
		si.setActiveSODcsvString("x,TRUE,yes");
		List<SODPair> pairs=si.getSODMap();
		Assert.assertEquals(result2, pairs);		
	}
	
	@Test
	public void testXLSFileImportSingleFlag() throws SODImporterException {
		SODMatrixImporter si=new SODMatrixImporter("test/resources/test1.xlsx", Filetype.XLS);
		si.setLabel(Source.COLUMN, 1);
		si.setNameColumn(3);
		si.setNameRow(1);
		si.setData(2, 4, 4);
		si.setActiveSODcsvString("x");
		si.setSheetNumber(0);
		List<SODPair> pairs=si.getSODMap();
		Assert.assertEquals(result1, pairs);		
	}
	
	@Test
	public void testXLSFileImportMultiFlag() throws SODImporterException {
		SODMatrixImporter si=new SODMatrixImporter("test/resources/test1.xlsx", Filetype.XLS);
		si.setLabel(Source.COLUMN, 2);
		si.setNameColumn(3);
		si.setNameRow(1);
		si.setData(2, 4, 4);
		si.setActiveSODcsvString("x,TRUE,yes");
		si.setSheetNumber(0);
		List<SODPair> pairs=si.getSODMap();
		Assert.assertEquals(result2, pairs);		
	}
	
}
