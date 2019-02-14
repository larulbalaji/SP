package sailpoint.seri.sodimporter;

import java.io.File;

import junitx.framework.FileAssert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class XMLPolicyBuilderTest {

	@Rule
	public final ExpectedSystemExit exit = ExpectedSystemExit.none();

	private void resetMain() {
		// SODImporter assumes that we start numbering columns/rows from 1
		// and adjusts accordingly. So we set these to 0 to make sure they are
		// invalid if not set

		XMLPolicyBuilder.fileType=Importer.Filetype.INVALID;
		XMLPolicyBuilder.roleType=Importer.Roletype.INVALID;
		XMLPolicyBuilder.inputFilename=null;
		XMLPolicyBuilder.outputFilename=null;
		XMLPolicyBuilder.activeSODcsvString=null;
		XMLPolicyBuilder.labelSource=Importer.Source.INVALID;
		XMLPolicyBuilder.label=0;
		XMLPolicyBuilder.namecol=0;
		XMLPolicyBuilder.namerow=0;
		XMLPolicyBuilder.datacol=0;
		XMLPolicyBuilder.datarow=0;
		XMLPolicyBuilder.datasize=-1; // datasize is not a column, it's a size
		XMLPolicyBuilder.sheetName=null;
		

	}


	@Test
	public void testNoArgs() {
		resetMain();
		String[] args=new String[0];
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}

	// Skeleton test definition:
	//@Test
	//public void testSkeleton() {
	//  resetMain();
	//  String[] args=new String[] {
	//    "-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csv.out.xml",
	//    "-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
	//  };
	//  exit.expectSystemExitWithStatus(-1);
	//  XMLPolicyBuilder.main(args);
	//}
	@Test
	public void testNoType() {
		resetMain();
		String[] args=new String[] {
				"-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testInvalidType() {
		resetMain();
		String[] args=new String[] {
				"-type", "xxx", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testNoInput() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testNoActive() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testNoOutput() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	//  No test for labels yet
	//	@Test
	//	public void testNoLabelSource() {
	//		resetMain();
	//		String[] args=new String[] {
	//				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
	//				"-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
	//		};
	//		exit.expectSystemExitWithStatus(-1);
	//		XMLPolicyBuilder.main(args);
	//	}	

	@Test
	public void testInvalidLabelSource() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "xxx", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	// No test for labels yet	
	//	@Test
	//	public void testNoLabelValue() {
	//		resetMain();
	//		String[] args=new String[] {
	//				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
	//				"-ls", "column", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
	//		};
	//		exit.expectSystemExitWithStatus(-1);
	//		XMLPolicyBuilder.main(args);
	//	}	

	// No test for labels yet	
	//	@Test
	//	public void testInvalidLabelValue() {
	//		resetMain();
	//		String[] args=new String[] {
	//				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
	//				"-ls", "column", "-l", "x", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
	//		};
	//		exit.expectSystemExitWithStatus(-1);
	//		XMLPolicyBuilder.main(args);
	//	}	

	@Test
	public void testNoNameRow() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testInvalidNameRow() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "x", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testNoNameColumn() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testInvalidNameColumn() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "x", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testNoDataRow() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testInvalidDataRow() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "x", "-dc", "3", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testNoDataColumn() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testInvalidDataColumn() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "x", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testNoDataSize() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testInvalidDataSize() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "x"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testInvalidParameter() {
		resetMain();
		String[] args=new String[] {
				"-stupid", "option"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	

	@Test
	public void testCSVSuccess() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csv.out.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4"
		};
		XMLPolicyBuilder.main(args);
		FileAssert.assertEquals(new File("test/resources/test1csv.ref.xml"), new File("test/resources/test1csv.out.xml"));
		// delete if test successful; otherwise leave for review
		new File("test/resources/test1csv.out.xml").delete();
	}	

	@Test
	public void testXLSNoSheet() {
		resetMain();
		String[] args=new String[] {
				"-type", "xls", "-i", "test/resources/test1.xlsx", "-active", "x,yes,true", "-o", "test/resources/test1xls.out.xml",
				"-ls", "column", "-l", "2", "-nr", "1", "-nc", "3", "-dr", "2", "-dc", "4", "-ds", "4"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}	
	
	@Test
	public void testXLSSuccess() {
		resetMain();
		String[] args=new String[] {
				"-type", "xls", "-i", "test/resources/test1.xlsx", "-active", "x,yes,true", "-o", "test/resources/test1xls.out.xml",
				"-ls", "column", "-l", "2", "-nr", "1", "-nc", "3", "-dr", "2", "-dc", "4", "-ds", "4", "-s", "Sheet1"
		};
		XMLPolicyBuilder.main(args);
		FileAssert.assertEquals(new File("test/resources/test1xls.ref.xml"), new File("test/resources/test1xls.out.xml"));
		// delete if test successful; otherwise leave for review
		new File("test/resources/test1xls.out.xml").delete();
	}	
	
	@Test
	public void testInvalidRoletype() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csvout.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4", "-cr", "xxx"
		};
		exit.expectSystemExitWithStatus(-1);
		XMLPolicyBuilder.main(args);
	}
 
	@Test
	public void testITRoles() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csv.IT.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4", "-cr", "IT"
		};
		XMLPolicyBuilder.main(args);
		FileAssert.assertEquals(new File("test/resources/test1csv.IT.ref.xml"), new File("test/resources/test1csv.IT.xml"));
		// delete if test successful; otherwise leave for review
		new File("test/resources/test1csv.IT.xml").delete();
	}
	
	@Test
	public void testBusinessRoles() {
		resetMain();
		String[] args=new String[] {
				"-type", "csv", "-i", "test/resources/test1.csv", "-active", "x,yes,true", "-o", "test/resources/test1csv.Business.xml",
				"-ls", "column", "-l", "1", "-nr", "1", "-nc", "2", "-dr", "2", "-dc", "3", "-ds", "4", "-cr", "Business"
		};
		XMLPolicyBuilder.main(args);
		FileAssert.assertEquals(new File("test/resources/test1csv.Business.ref.xml"), new File("test/resources/test1csv.Business.xml"));
		// delete if test successful; otherwise leave for review
		new File("test/resources/test1csv.Business.xml").delete();
	}
	
}

