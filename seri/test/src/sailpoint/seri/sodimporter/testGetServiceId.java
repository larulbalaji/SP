package sailpoint.seri.sodimporter;

import sailpoint.sdk.Common;
import sailpoint.sdk.Service;


public class testGetServiceId {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String pod="cook";
		String org="org30";
		String apiuser="sailpointdemo";
		String apipass="s@ilp0intdem030hio";
		String user="kevin.james";
		String pass="101pass1";
		
		String token=Common.getToken(pod, org, apiuser, apipass, user, pass);
		
		String id=Service.getIdByName(pod, org, token, "Amazon AWS");
		System.out.println("id="+id);
	}

}
