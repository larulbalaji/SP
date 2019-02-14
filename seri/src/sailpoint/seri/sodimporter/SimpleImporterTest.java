package sailpoint.seri.sodimporter;

import java.util.ArrayList;

public class SimpleImporterTest {

	public static void main(String[] args) throws Exception{
		// Now for the system matrix: parse the parameters
		String smFilename="/vms/POCs/Daimler/Systemmatrix_final.xlsx";
		String smType="XLS";
		Importer.Filetype smFtype=Importer.Filetype.INVALID;
		if("csv".equalsIgnoreCase(smType)) smFtype=Importer.Filetype.CSV;
		if("xls".equalsIgnoreCase(smType)) {
			smFtype=Importer.Filetype.XLS;
		}
		
		BaseImporter simpleImp=new BaseImporter(smFilename, smFtype);
		if(smFtype==Importer.Filetype.XLS) {
			String sName="Systeme";
			simpleImp.setSheetName(sName);
		}
		
		simpleImp.setNameColumn(1);
		simpleImp.setNameRow(1);
		simpleImp.setData(2,2,22);
		String active="X";
		
		ArrayList<ArrayList<String>> map=simpleImp.getSimpleMap();

		System.out.println("SimpleImporterTest.main:\n"+map);
	}
	
}
