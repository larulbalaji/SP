package sailpoint.seri.sodimporter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class XMLPolicyBuilder {

	/**
	 * Builds an XML import file to import policies into IIQ
	 * Assumes that roles already exist for each named item in the policy violation pairs
	 * 
	 * Syntax: java XMLPolicyBuilder [-type {csv|xls}] -i <input file> -active <csv of 'active' values> -o <output file>");
	 * Type defaults to CSV
	 */

	private enum ParameterType {
		INVALID,
		TYPE,
		INPUT,
		OUTPUT,
		ACTIVE,
		LABELSOURCE, /* Column/Row */
		LABEL,
		NAMECOL,
		NAMEROW,
		DATACOL,
		DATAROW,
		DATASIZE,
		CREATEROLES,
		SHEET
	};

	public static SODMatrixImporter.Filetype fileType=SODMatrixImporter.Filetype.INVALID;
	public static SODMatrixImporter.Roletype roleType=SODMatrixImporter.Roletype.INVALID;
	public static String inputFilename=null;
	public static String outputFilename=null;
	public static String activeSODcsvString=null;
	public static SODMatrixImporter.Source labelSource=SODMatrixImporter.Source.INVALID;
	// SODImporter assumes that we start numbering columns/rows from 1
	// and adjusts accordingly. So we set these to 0 to make sure they are
	// invalid if not set
	public static int label=0;
	public static int namecol=0;
	public static int namerow=0;
	public static int datacol=0;
	public static int datarow=0;
	public static int datasize=0;
	public static String sheetName=null;
	
	public static void main(String[] args) {

		boolean syntaxOK=checkArgs(args);

		if (!syntaxOK) {
			System.out.println("XMLPolicyBuilder options:");
			System.out.println("Mandatory:");
			System.out.println("-i <input file>");
			System.out.println("-active <active flags>");
			System.out.println("-o <output file>");
			System.out.println("-nr <name row>");
			System.out.println("-nc <name column>");
			System.out.println("-dr <data start row>");
			System.out.println("-dc <data start column>");
			System.out.println("-ds <data size>");
			System.out.println("");
			System.out.println("Mandatory for type XLS:");
			System.out.println("-s <Excel sheet name>");
			System.out.println("");
			System.out.println("Optional:");
			System.out.println("-ls <column|row>");
			System.out.println("-l <label column/row number>");
			System.out.println("-cr <IT|Business>");
			System.out.println("");
			System.out.println(" type defaults to CSV");
			System.exit(-1);
		}

		try {
			SODMatrixImporter importer=new SODMatrixImporter(inputFilename, fileType);

			System.out.println("setting active policies to "+activeSODcsvString);
			importer.setActiveSODcsvString(activeSODcsvString);
			importer.setData(datarow, datacol, datasize);
			importer.setNameColumn(namecol);
			importer.setNameRow(namerow);
			importer.setRoletype(roleType);
			importer.setSheetName(sheetName);
			// Get the inputs
			List<SODPair> pairs=importer.getSODMap();

			if(outputFilename==null) {
				System.out.println("XMLPolicyBuilder: no output file specified");
				System.exit(-1);
			}
			// setup from inputs
			File fOut=new File(outputFilename);
			PrintStream fwOut=new PrintStream(fOut);

			doPrologue(fwOut);

			int numpolicies=0;
			
			if(roleType!=SODMatrixImporter.Roletype.INVALID) {
				String[] roleNames=importer.getNames();
				doRoles(fwOut, roleNames, roleType);
			}

			for(SODPair pair: pairs) {
				doPolicy(fwOut, pair);
				numpolicies++;
			}
			doEpilogue(fwOut);

			fwOut.close();

			System.out.println(numpolicies+" policies written to "+outputFilename);

		} catch (SODImporterException sie) {
			System.out.println("XMLPolicyBuilder: "+sie);
			System.exit(-1);
		} catch (IOException ioe) {
			System.out.println("XMLPolicyBuilder: IOException "+ioe);
			System.exit(-1);
		}


	}

	private static void doRoles(PrintStream fw, String[] roleNames, SODMatrixImporter.Roletype rType) {

		String roletype="it";
		if(rType==SODMatrixImporter.Roletype.BUSINESS) roletype="business";
		int numRoles=0;
		
		for(String rolename: roleNames) {
			fw.println("<Bundle name=\""+rolename+"\" type=\""+roletype+"\">");
			fw.println("  <Attributes>");
			fw.println("    <Map>");
			fw.println("      <entry key=\"mergeTemplates\" value=\"false\"/>");
			fw.println("    </Map>");
			fw.println("  </Attributes>");
			fw.println("  <Owner>");
			fw.println("    <Reference class=\"sailpoint.object.Identity\" name=\"spadmin\"/>");
			fw.println("  </Owner>");
			fw.println("</Bundle>");
			numRoles++;
		}
		System.out.println(numRoles + " "+roletype+" roles written.");

	}

	private static void doPolicy(PrintStream fw, SODPair pair) {

		fw.print("<Policy certificationActions=\"Remediated,Mitigated,Delegated\" configPage=\"sodpolicy.xhtml\"");
		fw.print(" executor=\"sailpoint.policy.SODPolicyExecutor\" name=\"Test SOD Policy "+pair.left+"-"+pair.right+"\" state=\"Active\"");
		fw.println(" type=\"SOD\" typeKey=\"policy_type_sod\" violationOwnerType=\"Manager\">");
		fw.println("  <PolicyAlert disabled=\"true\" escalationStyle=\"none\"/>");
		fw.println("  <Description>Separation of Duty policy between roles "+pair.left+" and "+pair.right+"</Description>");
		fw.println("  <SODConstraints>");
		fw.println("    <SODConstraint name=\"rule summary\" violationOwnerType=\"Manager\">");
		fw.println("      <LeftBundles>");
		fw.println("        <Reference class=\"sailpoint.object.Bundle\" name=\""+pair.left+"\"/>");
		fw.println("      </LeftBundles>");
		fw.println("      <RightBundles>");
		fw.println("        <Reference class=\"sailpoint.object.Bundle\" name=\""+pair.right+"\"/>");
		fw.println("      </RightBundles>");
		fw.println("    </SODConstraint>");
		fw.println("  </SODConstraints>");
		fw.println("</Policy>");


	}

	private static void doPrologue(PrintStream fw) {

		fw.println("<?xml version='1.0' encoding='UTF-8'?>");
		fw.println("<!DOCTYPE sailpoint PUBLIC \"sailpoint.dtd\" \"sailpoint.dtd\">");
		fw.println("<sailpoint>");

	}

	private static void doEpilogue(PrintStream fw) {

		fw.println("</sailpoint>");

	}

	private static boolean checkArgs(String[] args) {

		if(args.length==0) {
			return false;
		}

		for(int paramNum=0;paramNum<args.length;paramNum++) {

			ParameterType pType=ParameterType.INVALID;
			String paramName=args[paramNum];
			if("-type".equalsIgnoreCase(paramName)) pType=ParameterType.TYPE;
			if("-i".equalsIgnoreCase(paramName)) pType=ParameterType.INPUT;
			if("-o".equalsIgnoreCase(paramName)) pType=ParameterType.OUTPUT;
			if("-active".equalsIgnoreCase(paramName)) pType=ParameterType.ACTIVE;
			if("-ls".equalsIgnoreCase(paramName)) pType=ParameterType.LABELSOURCE;
			if("-l".equalsIgnoreCase(paramName)) pType=ParameterType.LABEL;
			if("-nr".equalsIgnoreCase(paramName)) pType=ParameterType.NAMEROW;
			if("-nc".equalsIgnoreCase(paramName)) pType=ParameterType.NAMECOL;
			if("-dr".equalsIgnoreCase(paramName)) pType=ParameterType.DATAROW;
			if("-dc".equalsIgnoreCase(paramName)) pType=ParameterType.DATACOL;
			if("-ds".equalsIgnoreCase(paramName)) pType=ParameterType.DATASIZE;
			if("-cr".equalsIgnoreCase(paramName)) pType=ParameterType.CREATEROLES;
			if("-s".equalsIgnoreCase(paramName)) pType=ParameterType.SHEET;

			switch(pType) {

			case INVALID: {
				System.out.println("XMLPolicyBuilder: Invalid parameter "+paramName);
				return false;
			}
			case TYPE: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no type specified with -type");
					return false; // param name without param at the end of the command line
				}
				String parm=args[paramNum];
				if("csv".equalsIgnoreCase(parm)) {
					fileType=SODMatrixImporter.Filetype.CSV;
				} else  if("xls".equalsIgnoreCase(parm)) {
					fileType=SODMatrixImporter.Filetype.XLS;
				}
				break;
			}
			case INPUT: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no Input specified with -i");
					return false; // param name without param at the end of the command line
				}
				inputFilename=args[paramNum];
				break;
			}
			case OUTPUT: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no Output specified with -o");
					return false; // param name without param at the end of the command line
				}
				outputFilename=args[paramNum];
				break;
			}
			case ACTIVE: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no Active values specified with -active");
					return false; // param name without param at the end of the command line
				}
				activeSODcsvString=args[paramNum];
				break;
			}
			case LABELSOURCE: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no label source value specified with -ls");
					return false; // param name without param at the end of the command line
				}
				String s=args[paramNum];
				if("column".equalsIgnoreCase(s)) labelSource=SODMatrixImporter.Source.COLUMN;
				else if("row".equalsIgnoreCase(s)) labelSource=SODMatrixImporter.Source.ROW;
				else {
					System.out.println("XMLPolicyBuilder: invalid label source '"+s+"'");
					return false; // param name without param at the end of the command line
				}
				break;
			}
			case LABEL: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no label value specified with -l");
					return false; // param name without param at the end of the command line
				}
				String num=args[paramNum];
				int iNum=-1;
				try {
					iNum=Integer.parseInt(num);						
				} catch (NumberFormatException nfe) {
					System.out.println("XMLPolicyBuilder: Invalid number specified for label ('"+num+"')");
					return false;
				}

				break;
			}
			case NAMECOL: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no name column value specified with -l");
					return false; // param name without param at the end of the command line
				}
				String num=args[paramNum];
				int iNum=-1;
				try {
					iNum=Integer.parseInt(num);						
				} catch (NumberFormatException nfe) {
					System.out.println("XMLPolicyBuilder: Invalid number specified for name column ('"+num+"')");
					return false;
				}
				namecol=iNum;
				break;
			}
			case NAMEROW: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no name row value specified with -l");
					return false; // param name without param at the end of the command line
				}
				String num=args[paramNum];
				int iNum=-1;
				try {
					iNum=Integer.parseInt(num);						
				} catch (NumberFormatException nfe) {
					System.out.println("XMLPolicyBuilder: Invalid number specified for name row ('"+num+"')");
					return false;
				}
				namerow=iNum;
				break;
			}
			case DATACOL: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no data column value specified with -l");
					return false; // param name without param at the end of the command line
				}
				String num=args[paramNum];
				int iNum=-1;
				try {
					iNum=Integer.parseInt(num);						
				} catch (NumberFormatException nfe) {
					System.out.println("XMLPolicyBuilder: Invalid number specified for data column ('"+num+"')");
					return false;
				}
				datacol=iNum;
				break;
			}
			case DATAROW: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no data row value specified with -l");
					return false; // param name without param at the end of the command line
				}
				String num=args[paramNum];
				int iNum=-1;
				try {
					iNum=Integer.parseInt(num);						
				} catch (NumberFormatException nfe) {
					System.out.println("XMLPolicyBuilder: Invalid number specified for data row ('"+num+"')");
					return false;
				}
				datarow=iNum;
				break;
			}
			case DATASIZE: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no data size value specified with -l");
					return false; // param name without param at the end of the command line
				}
				String num=args[paramNum];
				int iNum=-1;
				try {
					iNum=Integer.parseInt(num);						
				} catch (NumberFormatException nfe) {
					System.out.println("XMLPolicyBuilder: Invalid number specified for data size ('"+num+"')");
					return false;
				}
				datasize=iNum;
				break;
			}
			case CREATEROLES: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no role type specified with -cr");
					return false; // param name without param at the end of the command line
				}
				String parm=args[paramNum];
				if("it".equalsIgnoreCase(parm)) {
					roleType=SODMatrixImporter.Roletype.IT;
				} else  if("business".equalsIgnoreCase(parm)) {
					roleType=SODMatrixImporter.Roletype.BUSINESS;
				} else {
					System.out.println("XMLPolicyBuilder: invalid role type '"+parm+"' specified with -cr");
					return false; // param name without param at the end of the command line
				}
				break;
			}
			case SHEET: {
				paramNum++;
				if(paramNum>=args.length) {
					System.out.println("XMLPolicyBuilder: no sheet name specified with -s");
					return false; // param name without param at the end of the command line
				}
				sheetName=args[paramNum];
				break;
			}
			}

		}

		return true;
	}


}
