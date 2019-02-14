
package openconnector.connector.okta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import openconnector.AbstractConnector;
import openconnector.AuthenticationFailedException;
import openconnector.Connector;
import openconnector.ConnectorException;
import openconnector.ExpiredPasswordException;
import openconnector.Filter;
import openconnector.Item;
import openconnector.ObjectAlreadyExistsException;
import openconnector.ObjectNotFoundException;
import openconnector.Result;
import openconnector.Schema;
import openconnector.Schema.Attribute;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.bulk.WriteRequest.Type;

import sailpoint.connector.SAPPortalSOAPConnector.SchemaType;
import sailpoint.integration.JsonUtil;
import sailpoint.object.AttributeDefinition;

/
 *
 */

public class OktaConnector extends AbstractConnector {

	//private static boolean enableDisableFlag = false;
	private static List<String> schemaAttributes = null;
	private static Log logger = LogFactory.getLog ("openconnector.connector.okta.OktaConnector");
	private static String user[] = {"firstName","lastName","login","displayName","mobilePhone","email","secondEmail","middleName","honorificPrefix",
		"honorificSuffix","title","nickName","profileUrl","primaryPhone","streetAddress","city","state","zipCode","countryCode","postalAddress",
		"preferredLanguage","locale","timezone","userType","employeeNumber","costCenter","organization","division","department","managerId","manager"};
	private List<String> userProfile = Arrays.asList(user);
	private static String group[] = {"name","description"};
	private List<String> groupProfile = Arrays.asList(group);
	private static Map<String, Object> userAccount = null;
	private static final String OBJECT_TYPE_APP = "app";
	private static final String OBJECT_TYPE_ROLE = "role";
	//private static List<Map<String,Object>> roles= null;
	// //////////////////////////////////////////////////////////////////////////
	//
	// INNER CLASSES - Used for aggregation which iterate through the List of
	// all the users to generate
	// userMap which gets converted to ResourceObject during aggregation.
	// //////////////////////////////////////////////////////////////////////////
	private class AccountIterator implements Iterator<Map<String, Object>> {
		private Iterator<Map<String, Object>> iterator;

		public AccountIterator(Iterator<Map<String, Object>> it) {
			// TODO Auto-generated constructor stub
			this.iterator = it;
		}

		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		public Map<String, Object> next() {
			Map<String, Object> userObj = this.iterator.next();
			return generateSingleUserMap(userObj);
		}

		public void remove() {
			this.iterator.remove();
		}

	}
	
	private class GroupIterator implements Iterator<Map<String, Object>> {
		private Iterator<Map<String, Object>> iterator;

		public GroupIterator(Iterator<Map<String, Object>> it) {
			// TODO Auto-generated constructor stub
			this.iterator = it;
		}

		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		public Map<String, Object> next() {
			Map<String, Object> groupObj = this.iterator.next();
			return generateSingleGroupMap(groupObj);
		}

		public void remove() {
			this.iterator.remove();
		}

	}
	
	private class AppIterator implements Iterator<Map<String, Object>> {
		private Iterator<Map<String, Object>> iterator;

		public AppIterator(Iterator<Map<String, Object>> it) {
			// TODO Auto-generated constructor stub
			this.iterator = it;
		}

		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		public Map<String, Object> next() {
			Map<String, Object> appObj = this.iterator.next();
			return generateSingleAppMap(appObj);
		}

		public void remove() {
			this.iterator.remove();
		}

	}
	
	private class RoleIterator implements Iterator<Map<String, Object>> {
		private Iterator<Map<String, Object>> iterator;

		public RoleIterator(Iterator<Map<String, Object>> it) {
			// TODO Auto-generated constructor stub
			this.iterator = it;
		}

		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		public Map<String, Object> next() {
			Map<String, Object> roleObj = this.iterator.next();
			return generateSingleRoleMap(roleObj);
		}

		public void remove() {
			this.iterator.remove();
		}

	}
	
	// 1. ================================PARTS OF AGGREGATION and ACCOUNT
	// READ============================================================
	// //////////////////////////////////////////////////////////////////////////
	//
	// ITERATE and READ methods:
	// iterate() - used for account aggregation.
	// read() - used to read user object details from end system during updates
	// and object perusal.
	//
	// //////////////////////////////////////////////////////////////////////////
	@Override
	public Iterator<Map<String, Object>> iterate(Filter arg0)
			throws ConnectorException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		if (schema.getObjectType().equals(OBJECT_TYPE_ACCOUNT)) {
			List<Map<String, Object>> listOfAllUsers = getAllUserList();
			Iterator<Map<String, Object>> iterator = listOfAllUsers.iterator();
			return new AccountIterator(iterator);
		} 
		else if (schema.getObjectType().equals(OBJECT_TYPE_GROUP))
		{
			List<Map<String, Object>> listOfAllGroups = getAllGroupList();
			Iterator<Map<String, Object>> iterator = listOfAllGroups.iterator();
			return new GroupIterator(iterator);			
		}
		else if (schema.getObjectType().equals(OBJECT_TYPE_APP))
		{
			List<Map<String, Object>> listOfAllApps = getAllAppList();
			Iterator<Map<String, Object>> iterator = listOfAllApps.iterator();
			return new AppIterator(iterator);			
		}
		/*else if (schema.getObjectType().equals(OBJECT_TYPE_ROLE))
		{
			if(roles!=null)
			{
				System.out.println(roles);
				List<Map<String, Object>> listOfAllRoles = roles;
				Iterator<Map<String, Object>> iterator = listOfAllRoles.iterator();
				return new RoleIterator(iterator);
			}
			else return null;
		}*/
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getAllUserList() {
		// TODO Auto-generated method stub
		String result = "";
		String line = null;
		HttpURLConnection connection = null;
		String hostUrl = config.getString("host");
		List<Map<String, Object>> rootObject = null;
		String url = hostUrl + "/api/v1/users"; // Need to change URL each time to fetch the users from different "pageNumber"
		try {
			connection = getConnection(url); // New connection each time for
												// different "pageNumber"
			connection.setRequestMethod("GET");
			InputStream is = connection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null)
				result += line;
			rootObject = (List<Map<String,Object>>) JsonUtil.parse(result);
			connection.disconnect(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("IO Exception occurred during aggreagating account!!!"
					+ e.getMessage());
			// //e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug("Exception occurred during aggreagating account!!!"
					+ e.getMessage());
			// e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		return rootObject;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getAllGroupList() 
	{
		String result = "";
		String line = null;
		HttpURLConnection connection = null;
		String hostUrl = config.getString("host");
		List<Map<String, Object>> rootObject = null;
		String url = hostUrl + "/api/v1/groups?filter=type%20eq%20\"OKTA_GROUP\""; // Need to change URL each time to fetch the users from different "pageNumber"
		try {
			connection = getConnection(url); // New connection each time for
												// different "pageNumber"
			connection.setRequestMethod("GET");
			InputStream is = connection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null)
				result += line;
			rootObject = (List<Map<String,Object>>) JsonUtil.parse(result);			
			connection.disconnect(); 
			
			url = hostUrl + "/api/v1/groups?filter=type%20eq%20\"BUILT_IN\"";
			result="";
			// BELOW Connection is to get the BUILT IN ROLES
			connection = getConnection(url);
			connection.setRequestMethod("GET");
			is = connection.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			while ((line = br.readLine()) != null)
				result += line;
			if(rootObject == null)
				rootObject = (List<Map<String,Object>>) JsonUtil.parse(result);
			else
				rootObject.addAll((List<Map<String,Object>>) JsonUtil.parse(result));
			connection.disconnect(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("IO Exception occurred during aggreagating group!!!"
					+ e.getMessage());
			// //e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug("Exception occurred during aggreagating group!!!"
					+ e.getMessage());
			// e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		return rootObject;
	}
	
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getAllAppList() 
	{
		String result = "";
		String line = null;
		HttpURLConnection connection = null;
		String hostUrl = config.getString("host");
		List<Map<String, Object>> rootObject = null;
		String url = hostUrl + "/api/v1/apps?filter=status%20eq%20\"ACTIVE\""; // Need to change URL each time to fetch the users from different "pageNumber"
		try {
			connection = getConnection(url); // New connection each time for
												// different "pageNumber"
			connection.setRequestMethod("GET");
			InputStream is = connection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null)
				result += line;
			rootObject = (List<Map<String,Object>>) JsonUtil.parse(result);
			connection.disconnect(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("IO Exception occurred during aggreagating group!!!"
					+ e.getMessage());
			// //e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug("Exception occurred during aggreagating group!!!"
					+ e.getMessage());
			// e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		} finally {
			if (connection != null)
				connection.disconnect();
		}
		return rootObject;
	}
	@SuppressWarnings("unchecked")
	
	private Map<String, Object> generateSingleUserMap(
			Map<String, Object> userObj) {
		Map<String, Object> user = new HashMap<String, Object>();
		Map<String,Object> userDetail = (Map<String,Object>)userObj.get("profile");
		schemaAttributes = schema.getAttributeNames();
		for (String attrName : schemaAttributes) {
			if (userProfile.contains(attrName))// && attrName != "id"
			{
				if(userDetail.get(attrName) != null)
					user.put(attrName, userDetail.get(attrName));				
			}
			else if(attrName.equals(schema.getGroupAttribute()) || attrName.contains("group"))
			{
				List<String> groups = generateGroupList(userObj.get("id").toString());
				user.put(attrName, groups);
			}
			else if(attrName.equals("app"))
			{
				List<String> apps = generateAppList(userObj.get("id").toString());
				user.put(attrName, apps);
			}
			else if(attrName.equals("role"))
			{
				List<String> roleIds = generateRoleList(userObj.get("id").toString());
				user.put(attrName, roleIds);
			}			
		}
		user.put("id", userObj.get("id"));
		return user;
	}
	
	@SuppressWarnings("unchecked")
	List<String> generateRoleList(String id)
	{
		List<String> roleIds = new ArrayList<String>();
		InputStreamReader isr = null;
		BufferedReader br = null;
		String result="";
		//api/v1/users/00ub17m7e0uwy2Qn20h7/roles
		String url = config.getString("host")+"/api/v1/users/"+id+"/roles";
		HttpURLConnection connection = getConnection(url);
		
		try
		{
			connection.setRequestMethod("GET");
			connection.getInputStream();
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				String line = "";
				isr = new InputStreamReader(connection.getInputStream());
				br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					result += line;
				}	
				/*if(roles==null)
					roles = (List<Map<String,Object>>)JsonUtil.parse(result);
				else
					roles.addAll((List<Map<String,Object>>)JsonUtil.parse(result));*/
				List <Map<String,Object>>temps = (List<Map<String,Object>>)JsonUtil.parse(result);
				for(Map<String,Object> temp: temps)
				{
					roleIds.add(temp.get("id").toString());
				}			
			}
			else
			{
				String line = "";
				isr = new InputStreamReader(connection.getErrorStream());
				br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					result += line;
				}
			}
		}
		catch(Exception e)
		{			
			logger.debug("Error getting groups for user "+e.getMessage());			
		}
		finally
		{
			if(connection!=null)
				connection.disconnect();
		}
		return roleIds;
	}
	
	@SuppressWarnings("unchecked")
	List<String> generateGroupList(String id)
	{
		List<String> groupIds = new ArrayList<String>();
		InputStreamReader isr = null;
		BufferedReader br = null;
		String result="";
		
		String url = config.getString("host")+"/api/v1/users/"+id+"/groups";
		HttpURLConnection connection = getConnection(url);
		try
		{
			connection.setRequestMethod("GET");
			connection.getInputStream();
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				String line = "";
				isr = new InputStreamReader(connection.getInputStream());
				br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					result += line;
				}	
				List<Map<String,Object>> temps = (List<Map<String,Object>>)JsonUtil.parse(result);
				for(Map<String,Object> temp: temps)
				{
					groupIds.add(temp.get("id").toString());
				}
			}
			else
			{
				String line = "";
				isr = new InputStreamReader(connection.getErrorStream());
				br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					result += line;
				}
			}
		}
		catch(Exception e)
		{			
			logger.debug("Error getting groups for user "+e.getMessage());			
		}
		finally
		{
			if(connection!=null)
				connection.disconnect();
		}
		return groupIds;
	}
	
	@SuppressWarnings("unchecked")
	List<String> generateAppList(String id)
	{
		List<String> appIds = new ArrayList<String>();
		InputStreamReader isr = null;
		BufferedReader br = null;
		String result="";
		
		String url = config.getString("host")+"/api/v1/apps/?filter=user.id+eq+\""+id+"\"";
		HttpURLConnection connection = getConnection(url);
		try
		{
			connection.setRequestMethod("GET");
			connection.getInputStream();
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				String line = "";
				isr = new InputStreamReader(connection.getInputStream());
				br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					result += line;
				}	
				List<Map<String,Object>> temps = (List<Map<String,Object>>)JsonUtil.parse(result);
				for(Map<String,Object> temp: temps)
				{
					appIds.add(temp.get("id").toString());
				}
			}
			else
			{
				String line = "";
				isr = new InputStreamReader(connection.getErrorStream());
				br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					result += line;
				}
			}
		}
		catch(Exception e)
		{			
			logger.debug("Error getting groups for user "+e.getMessage());			
		}
		finally
		{
			if(connection!=null)
				connection.disconnect();
		}
		return appIds;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> generateSingleGroupMap(
			Map<String, Object> groupObj) {
		// TODO Auto-generated method stub
		Map<String, Object> group = new HashMap<String, Object>();
		Map<String,Object> groupDetail = (Map<String,Object>)groupObj.get("profile");
		schemaAttributes = schema.getAttributeNames();
		for (String attrName : schemaAttributes) {
			if (groupProfile.contains(attrName))// && attrName != "id"
			{
				group.put(attrName, groupDetail.get(attrName));				
			}
			else if(group.get(attrName)==null)
				group.put(attrName, groupObj.get(attrName));
		}
		return group;
	}

	private Map<String, Object> generateSingleAppMap(
			Map<String, Object> appObj) {
		// TODO Auto-generated method stub
		Map<String, Object> app = new HashMap<String, Object>();
		schemaAttributes = schema.getAttributeNames();
		for (String attrName : schemaAttributes) 
		{
			app.put(attrName, appObj.get(attrName));
		}
		return app;
	}
	
	private Map<String, Object> generateSingleRoleMap(
			Map<String, Object> roleObj) {
		// TODO Auto-generated method stub
		Map<String, Object> role = new HashMap<String, Object>();
		schemaAttributes = schema.getAttributeNames();
		for (String attrName : schemaAttributes) 
		{
			role.put(attrName, roleObj.get(attrName));
		}
		return role;
	}


	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> read(String id) throws ConnectorException,
			ObjectNotFoundException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		// logger.debug("@@@@@@@=========Inside DB Read Account==========@@@@@@");
		Map<String, Object> object = new HashMap<String, Object>();				
		String jsonString = getObject(id);
		try {
			if(schema.getObjectType().equals(OBJECT_TYPE_ACCOUNT))
			{
				logger.debug("Account Read "+jsonString);
				object = (Map<String, Object>) JsonUtil.parse(jsonString);
				String status = object.get("status").toString();
				if(status.equalsIgnoreCase("DEPROVISIONED") || status.equalsIgnoreCase("SUSPENDED") || status.equalsIgnoreCase("STAGED"))
				{
					object.put("IIQDisabled",true);
				}			
				else if (status.equalsIgnoreCase("LOCKED_OUT"))
				{
					object.put("IIQLocked", true);
					object.put("IIQDisabled",false);
				}				
				else
					object.put("IIQDisabled",false);
				
				object = generateSingleUserMap(object);
			}
			else if(schema.getObjectType().equals(OBJECT_TYPE_GROUP))
			{
				logger.debug("Group Read");
				object = (Map<String, Object>) JsonUtil.parse(jsonString);
				object = generateSingleGroupMap(object);
			}
			else if(schema.getObjectType().equals("app"))
			{
				logger.debug("App Read");
				object = (Map<String, Object>) JsonUtil.parse(jsonString);
				object = generateSingleAppMap(object);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug("Exception occurred during read() account!!!"
					+ e.getMessage());
			throw new ConnectorException(e.getMessage());
			// e.printStackTrace();
		}
		return object;
	}
	
	// 1. ================================PARTS OF AGGREGATION and ACCOUNT READ COMPLETE===================================

	// //////////////////////////////////////////////////////////////
	//
	// 2. ==================PART TEST CONNECTION =========
	//
	// /////////////////////////////////////////////////////////////
	@Override
	public void testConnection() throws ConnectorException {
		// TODO Auto-generated method stub
		try {
			String url = config.getString("host");
			url+="/api/v1/users?limit=1";
			HttpURLConnection connection = getConnection(url);
			connection.setRequestMethod("GET");
			connection.getInputStream();
			if (connection != null)
				connection.disconnect();			
		} catch (UnknownHostException uhe) {
			throw new ConnectorException("Host cannot be reached at this time!!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new ConnectorException(e.getMessage());
		}
	}

	public HttpURLConnection getConnection(String url) {
		HttpURLConnection urlConnection = null;
		try {			 
			URL urls = new URL(url);
			String key = config.getString("apiKey");
			key= "SSWS "+key;
			urlConnection = (HttpURLConnection) urls.openConnection();
			urlConnection.setRequestProperty("Authorization", key);
			urlConnection.setRequestProperty("content-type", "application/json");
		} catch (MalformedURLException mfue) {
			logger.debug(mfue.getMessage() + " Malformed URL Exception!!");
		} catch (Exception e) {
			logger.debug(e.getMessage());
		}
		return urlConnection;
	}

	// 2. ================================PART TEST CONNECTION COMPELETE==========================================================

	// 3. ================================PART OF CRUD OPERATIONS - PROVISIONING
	// and UPDATES==================================
	// ////////////////////////////////////////////
	//
	// create(): invoked during account creation from SailPoint to Okta
	// API.
	// update(): All attribute updates during aggregation and attribute sync
	// from SailPoint to Okta API via target mappings use update() method
	// delete(): Account deletion from SailPoint to Okta API uses this
	// method.
	//
	// ///////////////////////////////////////////
	@Override
	public Result create(String id, List<Item> items)
			throws ConnectorException, ObjectAlreadyExistsException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		// logger.debug("@@@@@@@=========Inside DB Create Account==========@@@@@@");
		Result result = new Result();
		HttpURLConnection connection = null;

		String url = null;
		if (schema.getObjectType().equals(OBJECT_TYPE_ACCOUNT)) 
		{
			url = config.getString("host") + "/api/v1/users/";
			result = createAccount(url, id, items);
		} 
		else if (schema.getObjectType().equals(OBJECT_TYPE_GROUP)) 
		{
			url = config.getString("host") + "/api/v1/groups/";
			result = createGroup(url, id, items);		
		}
		return result;
	}
	
	private Result createAccount(String url, String id, List<Item> items) {
		// TODO Auto-generated method stub
		OutputStream os = null;
		JSONObject newObject = null;
		Result result = new Result();
		String line = null;
		String login = null;
		HttpURLConnection connection = null;
		String output = "";		
		BufferedReader br = null;
		List<Item> appsNGroupsNRoles = new ArrayList<Item>();
		Map<String,Object> profileMap = new HashMap<String,Object>();	
		
		try
		{
			if (items != null) {
				userAccount = new HashMap<String, Object>();
				for (Item item : items) {
					String itemName = item.getName();
					logger.debug(itemName+"******"+item.getValue());
					if(item.getValue()!=null)
					{
						if(userProfile.contains(itemName))
						{
							profileMap.put(itemName, item.getValue());
						}
						else if(itemName.contains("password"))
						{
							Map creds = new HashMap();
							Map password = new HashMap();
							password.put("value", item.getValue().toString());
							creds.put("password", password);
							userAccount.put("credentials", creds);	
						}
						else if (itemName.equals("group") || itemName.equals("app") || itemName.equals("role"))
						{
							appsNGroupsNRoles.add(item);
						}						
						else
							userAccount.put(itemName, item.getValue());
					}						
				}
			}
			if (userAccount != null) {
				userAccount.put("profile", profileMap);					
				newObject = new JSONObject(userAccount);
			}
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type","application/json; charset=utf8");
			connection.setRequestMethod("POST");
			os = connection.getOutputStream();
			//logger.debug("Okta Create Mehtod: JSON STRING OBJECT : "+newObject.toString());
			os.write(newObject.toString().getBytes("UTF-8"));
			os.close();
			
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;
				}
				Map user = (Map<String,Object>)JsonUtil.parse(output);
				id = user.get("id").toString();
				logger.debug("Okta ID created for the user : "+user.get("profile")+ " as : "+id);					
				if(appsNGroupsNRoles.size()>0)
					performGroupEntitlementOperations(id,appsNGroupsNRoles);
				logger.debug("Okta ID : "+id);
				userAccount = read(id);
				result.setObject(userAccount);
				result.setStatus(Result.Status.Committed);
			}
			else
			{		
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;										
				}				
				if(output.contains("field already exists"))
				{
					connection.disconnect();
					Map<String,Object> options = new HashMap<String,Object>();
					options.put("fromCreateEnable",true);
					options.put("login",profileMap.get("login"));
					result = enable(login, options);
				}
				else 
				{
					logger.debug("Error during account create for user : ("+userAccount.get("profile")+")"+ " : "+output);
					throw new IOException(output);
				}
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("IO Exception occurred during CREATE!!!"
					+ e.getMessage());
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.debug("JSON Exception occurred during CREATE!!!"
					+ e.getMessage());
			e.printStackTrace();			
			throw new ConnectorException(e.getMessage());
		}		
		catch (ObjectAlreadyExistsException oae)
		{
			logger.debug(oae.getMessage());
		}
		catch (Exception e)
		{
			logger.debug("Exception : "+e.getMessage());
			throw new ConnectorException(e.getMessage());
		}
		finally {
			if (connection != null)
				connection.disconnect();
		}
		
		return result;
	}
	
	private Result createGroup(String url, String id, List<Item> items) {
		// TODO Auto-generated method stub
		Map<String, Object> group = null;
		OutputStream os = null;
		JSONObject newObject = null;
		Result result = new Result();
		String line = null;
		HttpURLConnection connection = null;
		String output = "";
		BufferedReader br = null;
		Map<String, Object> profileMap = new HashMap<String, Object>();

		try {
			if (items != null) 
			{
				group = new HashMap<String, Object>();
				for (Item item : items) 
				{
					String itemName = item.getName();
					if (item.getValue() != null) 
					{
						if (groupProfile.contains(itemName)) 
						{
							profileMap.put(itemName, item.getValue());
						} 
						else
							group.put(itemName, item.getValue());
					}
				}
			}
			if (group != null) {
				group.put("profile", profileMap);
				newObject = new JSONObject(group);
			}
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type","application/json; charset=utf8");
			connection.setRequestMethod("POST");
			os = connection.getOutputStream();
			//logger.debug("Okta Create Mehtod: JSON STRING OBJECT : "+ newObject.toString());
			os.write(newObject.toString().getBytes("UTF-8"));
			os.close();

			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300){
				br = new BufferedReader(new InputStreamReader(
						connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;
				}
				Map obj = (Map<String, Object>) JsonUtil.parse(output);
				id = obj.get("id").toString();
				logger.debug("Okta GROUP created : " + obj.get("profile")
						+ " as : " + id);
				result.setObject(generateSingleGroupMap(obj));
				result.setStatus(Result.Status.Committed);
			} else {
				br = new BufferedReader(new InputStreamReader(
						connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;
				}
				if (output.contains("field already exists")) {
					logger.debug("Group Exists Already");
					throw new ObjectAlreadyExistsException(output);
				} else {
					logger.debug("Error during group create " + output);
					throw new IOException(output);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("IO Exception occurred during CREATE!!!"
					+ e.getMessage());
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.debug("JSON Exception occurred during CREATE!!!"
					+ e.getMessage());
			e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		} catch (ObjectAlreadyExistsException oae) {
			logger.debug(oae.getMessage());
		} catch (Exception e) {
			logger.debug("Exception : " + e.getMessage());
			throw new ConnectorException(e.getMessage());
		} finally {
			if (connection != null)
				connection.disconnect();
		}

		return result;
	}	
	
	private boolean performGroupEntitlementOperations(String id, List<Item> items) throws Exception {
		// TODO Auto-generated method stub
		String url = null;
		String groupId = null;
		String appId = null;
		String roleId = null;
		boolean performedOperations = true;
		userAccount = read(id);
		try
		{
			for(Item item : items)
			{
				if(item.getOperation().equals(Item.Operation.Add) || item.getOperation().equals(Item.Operation.Set))
				{					
					if(item.getName().equals("group"))
					{
						groupId = item.getValue().toString();
						url = config.getString("host")+"/api/v1/groups/"+groupId+"/users/"+id;
						logger.debug("ENTITLEMENT ADDITION URL " +url);
						performGroupOperation(url,"PUT");
					}
					else if(item.getName().equals("app"))
					{
						appId = item.getValue().toString();
						url = config.getString("host")+"/api/v1/apps/"+appId+"/users/";
						logger.debug("ENTITLEMENT ADDITION URL " +url);
						performAppsOperation(url,"POST");
					}
					else if(item.getName().equals("role"))
					{
						roleId = item.getValue().toString();
						url = config.getString("host")+"/api/v1/users/"+id+"/roles/";
						logger.debug("ENTITLEMENT ADDITION URL " +url);
						logger.debug("=================VALUE ITEM FOR ROLE===============\n"+item.getValue());
						performRoleOperation(url,"POST",item);
					}
				}
				else if(item.getOperation().equals(Item.Operation.Remove))
				{		
					if(item.getName().equals("group"))
					{
						groupId = item.getValue().toString();
						url = config.getString("host")+"/api/v1/groups/"+groupId+"/users/"+id;
						logger.debug("ENTITLEMENT REMOVAL GROUP URL " +url);
						performGroupOperation(url,"DELETE");
					}
					else if(item.getName().equals(OBJECT_TYPE_APP))
					{
						appId = item.getValue().toString();
						url = config.getString("host")+"/api/v1/apps/"+appId+"/users/"+id;
						logger.debug("ENTITLEMENT REMOVAL APP URL " +url);
						performAppsOperation(url,"DELETE");
					}
					else if(item.getName().equals("role"))
					{
						roleId = item.getValue().toString();
						url = config.getString("host")+"/api/v1/users/"+id+"/roles/"+roleId;
						logger.debug("ENTITLEMENT REMOVAL URL " +url);
						logger.debug("=================VALUE ITEM FOR ROLE===============\n"+item.getValue());
						performRoleOperation(url,"DELETE",item);
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.debug("Exeption in entitlment addition "+e.getMessage());
			performedOperations = false;
			throw new Exception(e.getMessage());
		}
		
		return performedOperations;
	}
	private void performGroupOperation(String url, String operation) throws Exception
	{
		HttpURLConnection connection = null;
		OutputStream os = null;
		JSONObject newObject = new JSONObject();
		BufferedReader br = null;
		String line = "";
		String output = "";		
		
		connection = getConnection(url);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-Type", "application/json; charset=utf8");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestMethod(operation);
		os = connection.getOutputStream();
		os.write(newObject.toString().getBytes("UTF-8"));
		os.close();
		if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
		{
			br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));			
			while ((line = br.readLine()) != null) {
				output += line;
			}
		}
		else
		{		
			br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
			while ((line = br.readLine()) != null) {
				output += line;										
			}
			logger.debug("Error while adding/removing groups \n"+output);
			throw new IOException(output);				
		}
	}
	
	private void performRoleOperation(String url, String operation,Item item) throws Exception
	{
		HttpURLConnection connection = null;
		OutputStream os = null;
		JSONObject newObject = new JSONObject();
		BufferedReader br = null;
		String line = "";
		String output = "";		
		
		connection = getConnection(url);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-Type", "application/json; charset=utf8");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestMethod(operation);
		os = connection.getOutputStream();
		if(operation.equals("POST"))
			newObject.put("type", item.getValue());
		logger.debug("ROLE ADD JSON OBJECT=="+newObject.toString().getBytes("UTF-8"));
		os.write(newObject.toString().getBytes("UTF-8"));
		os.close();
		if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
		{
			br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));			
			while ((line = br.readLine()) != null) {
				output += line;
			}
		}
		else
		{		
			br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
			while ((line = br.readLine()) != null) {
				output += line;										
			}
			logger.debug("Error while adding/removing groups \n"+output);
			throw new IOException(output);				
		}
	}
	
	private void performAppsOperation(String url, String operation) throws Exception
	{
		HttpURLConnection connection = null;
		OutputStream os = null;
		JSONObject newObject = new JSONObject();
		BufferedReader br = null;
		String line = "";
		String output = "";	
		
		if(operation.equals("POST"))
		{
			Map<String,Object> credentials = new HashMap<String,Object>();
			Map<String,Object> appMap = new HashMap<String,Object>();
			credentials.put("userName", userAccount.get("login"));
			appMap.put("id", userAccount.get("id"));
			appMap.put("scope", "USER");
			appMap.put("credentials", credentials);
			newObject = new JSONObject(appMap);
		}
		
		connection = getConnection(url);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-Type", "application/json; charset=utf8");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestMethod(operation);
		os = connection.getOutputStream();
		logger.debug("APP JSON OBJECT : "+newObject.toString());
		os.write(newObject.toString().getBytes("UTF-8"));
		os.close();
		
		if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
		{
			br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));			
			while ((line = br.readLine()) != null) {
				output += line;
			}
			logger.debug("APP ADDITION :\n"+output);
		}
		else
		{		
			br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
			while ((line = br.readLine()) != null) {
				output += line;										
			}
			logger.debug("Error while adding/removing apps \n"+output);
			throw new IOException(output);				
		}
	}
	@Override
	public Result update(String id, List<Item> items)
			throws ConnectorException, ObjectNotFoundException,
			IllegalArgumentException, UnsupportedOperationException {
		// TODO Auto-generated method stub
		// logger.debug("@@@@@@@=========Inside DB Update\"S\" Account==========@@@@@@");
		Result result = new Result();		
		HttpURLConnection connection = null;		
		String url = null;
		
		try {
			if (schema.getObjectType().equals(OBJECT_TYPE_ACCOUNT))
			{
				url = config.getString("host") + "/api/v1/users/" + id;
				result = updateAccount(url,id,items);
			}
			else if (schema.getObjectType().equals(OBJECT_TYPE_GROUP))
			{
				url = config.getString("host") + "/api/v1/groups/" + id;
				result = updateGroup(url, id, items);
			}			
		}
		catch (Exception e)
		{
			logger.debug("General Exception occurred during updating account!!!"
					+ e.getMessage());
			throw new ConnectorException(e.getMessage());
		}
		finally {
			if (connection != null)
				connection.disconnect();
		}
		return result;
	}

	Result updateAccount(String url, String id, List<Item> items)
	{
		Result result = new Result();		
		Map<String, Object> account = null;
		Map<String,Object> updates = new HashMap<String,Object>();
		OutputStream os = null;
		String line = null;
		String object = "";
		JSONObject jsonObject = null;
		HttpURLConnection connection = null;		
		BufferedReader br = null;		
		List<Item> appsNGroupsNRoles = new ArrayList<Item>();
		
		try
		{
			account = (Map<String,Object>)JsonUtil.parse(getObject(id));
			Map<String,Object> profileOkta = (Map<String,Object>)account.get("profile");
			Map<String,Object> credentialsOkta = (Map<String,Object>)account.get("credentials");
			for (Item item : items) {
				if(item.getValue()!=null)
				{	
					String itemName = item.getName();
					logger.debug(itemName+"******"+item.getValue());
					if(userProfile.contains(itemName))
					{
						profileOkta.put(itemName, item.getValue());						
					}
					else if(itemName.contains("password"))
					{							
						Map password = new HashMap();
						password.put("value", item.getValue().toString());
						credentialsOkta.put("password", password);
						updates.put("credentials", credentialsOkta);	
					}
					else if(itemName.equalsIgnoreCase("profile"))
					{
						Map<String,Object> profileSP = (Map<String,Object>)item.getValue();
						Iterator iter = profileSP.keySet().iterator();
						while(iter.hasNext())
						{
							String key = (String) iter.next();
							profileOkta.put(key, profileSP.get(key));							
						}
						updates.put(itemName, profileOkta);
					}
					else if(itemName.equalsIgnoreCase("credentials"))
					{
						Map<String,Object> credentialsSP = (Map<String,Object>)item.getValue();
						Iterator iter = credentialsSP.keySet().iterator();
						while(iter.hasNext())
						{
							String key = (String) iter.next();
							credentialsOkta.put(key, credentialsSP.get(key));							
						}
						updates.put(itemName, credentialsOkta);
					}	
					else if (itemName.equals("group") || itemName.equals("app") || itemName.equals("role"))
					{
						logger.debug("ADDING GROUP, APP, ROLE :"+item.getValue());
						appsNGroupsNRoles.add(item);
					}
					else
						updates.put(itemName, item.getValue());
				}				
			}
			if (updates != null) {
				updates.put("profile", profileOkta);
				jsonObject = new JSONObject(updates);				
			}
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type","application/json; charset=utf8");
			connection.setRequestProperty("Accept", "application/json");			
			connection.setRequestMethod("PUT");
			os = connection.getOutputStream();
			os.write(jsonObject.toString().getBytes("UTF-8"));
			os.close();
			
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{	
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					object += line;
				}					
				if(appsNGroupsNRoles.size()>0)
				{
					logger.debug("Entitlement updates pending");					
					performGroupEntitlementOperations(id,appsNGroupsNRoles);
					userAccount = read(id);
				}		
				logger.debug("Update result for user : "+id+" : \n"+object);
				
				result.setObject(userAccount);
				result.setStatus(Result.Status.Committed);				
			}
			else
			{				
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					object += line;					
				}
				logger.debug("Error occured for user : "+id+" during update: \n"+object);
				throw new IOException(object);
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("IO Exception occurred during updating account!!!"
					+ e.getMessage());
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} 
		catch (Exception e)
		{
			logger.debug("General Exception occurred during updating account!!!"
					+ e.getMessage());
			throw new ConnectorException(e.getMessage());
		}
		finally {
			if (connection != null)
				connection.disconnect();
		}
		return result;
	}
	
	Result updateGroup(String url, String id, List<Item> items)
	{
		Result result = new Result();		
		Map<String, Object> account = null;
		Map<String, Object> group = null;
		Map<String,Object> updates = new HashMap<String,Object>();
		OutputStream os = null;
		String line = null;
		String object = "";
		JSONObject jsonObject = null;
		HttpURLConnection connection = null;		
		BufferedReader br = null;
		
		try
		{
			group = (Map<String,Object>)JsonUtil.parse(getObject(id));
			Map<String,Object> profileOkta = (Map<String,Object>)group.get("profile");
			for (Item item : items) {
				if(groupProfile.contains(item.getName()))
				{
					logger.debug("Profile item name:" +item.getName());
					profileOkta.put(item.getName(), item.getValue());						
				}
			}
			if (updates != null) {
				logger.debug("FINAL UPDATES TO PROFILE!!");
				updates.put("profile", profileOkta);
				jsonObject = new JSONObject(updates);
				logger.debug("Update Group JSON Object: "+jsonObject);
			}
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type",
					"application/json; charset=utf8");
			connection.setRequestProperty("Accept", "application/json");			
			connection.setRequestMethod("PUT");
			os = connection.getOutputStream();
			os.write(jsonObject.toString().getBytes("UTF-8"));
			os.close();	
			
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					object += line;
				}
				logger.debug("Update result for group : "+id+" : \n"+object);
				result.setObject(generateSingleGroupMap(read(id)));
				result.setStatus(Result.Status.Committed);
			}
			else
			{				
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					object += line;					
				}
				logger.debug("Error occured for user : "+id+" during update: \n"+object);
				throw new IOException(object);
			}
	
		}
		 catch (IOException e) {
				// TODO Auto-generated catch block
				logger.debug("IO Exception occurred during updating account!!!"
						+ e.getMessage());
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} 
			catch (Exception e)
			{
				logger.debug("General Exception occurred during updating account!!!"
						+ e.getMessage());
				throw new ConnectorException(e.getMessage());
			}
		return result;
	}
	
	@Override
	public Result delete(String id, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		// logger.debug("@@@@@@@=========Inside DB Delete Account==========@@@@@@");
		Result result = new Result();
		String object = "";
		String line = null;
		// OutputStream os = null;
		String url = null;
		Map<String,Object> user = read(id);
		String status = user.get("status").toString();
		HttpURLConnection connection = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			if (schema.getObjectType().equals(OBJECT_TYPE_ACCOUNT))
			{
				url = config.getString("host") + "/api/v1/users/" + id;
				result = deleteAccount(url, id, options);
						
			}
			else if (schema.getObjectType().equals(OBJECT_TYPE_GROUP))
			{
				url = config.getString("host") + "/api/v1/groups/" + id;
				result = deleteGroup(url, id, options);
			}
			
		} 
		catch (Exception e)
		{
			logger.debug("General Exception occurred during DELETE!!!"
					+ e.getMessage());
			throw new ConnectorException(e.getMessage());
		}
		
		finally {
			if (connection != null)
				connection.disconnect();
		}
		return result;
	}
	
	Result deleteAccount(String url, String id, Map<String, Object> options)
	{
		Result result = new Result();
		String object = "";
		String line = null;
		Map<String,Object> user = read(id);
		String status = user.get("status").toString();
		HttpURLConnection connection = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try
		{
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type",
					"application/json; charset=utf8");
			connection.setRequestProperty("Accept", "application/json");			
			connection.setRequestMethod("DELETE");			
			
			isr = new InputStreamReader(connection.getInputStream());
			br = new BufferedReader(isr);
			if(status.equalsIgnoreCase("PROVISIONED") || status.equalsIgnoreCase("ACTIVE"))
			{
				connection.disconnect();
				connection = getConnection(url);
				connection.setRequestProperty("Content-Type","application/json; charset=utf8");
				connection.setRequestProperty("Accept", "application/json");			
				connection.setRequestMethod("DELETE");			
				
				isr = new InputStreamReader(connection.getInputStream());
				br = new BufferedReader(isr);
			}
			while ((line = br.readLine()) != null) {
				object += line;
			}
			logger.debug("Account "+id+" is deleted from Okta.");
			
			result.setStatus(Result.Status.Committed);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("IO Exception occurred during DELETE!!!"
					+ e.getMessage());
			throw new ConnectorException(e.getMessage());
			// e.printStackTrace();
		} catch (Exception e)
		{
			logger.debug("General Exception occurred during DELETE!!!"
					+ e.getMessage());
			throw new ConnectorException(e.getMessage());
		}
		
		finally {
			if (connection != null)
				connection.disconnect();
		}
		return result;
	}
	Result deleteGroup(String url, String id, Map<String, Object> options)
	{
		Result result = new Result();
		String object = "";
		String line = null;
		Map<String,Object> user = read(id);
		String status = user.get("status").toString();
		HttpURLConnection connection = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try
		{
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type","application/json; charset=utf8");
			connection.setRequestProperty("Accept", "application/json");			
			connection.setRequestMethod("DELETE");
			isr = new InputStreamReader(connection.getInputStream());
			br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				object += line;
			}
			logger.debug("Group "+id+" is deleted from Okta.");
			
			result.setStatus(Result.Status.Committed);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("IO Exception occurred during DELETE!!!"
					+ e.getMessage());
			throw new ConnectorException(e.getMessage());
			// e.printStackTrace();
		} catch (Exception e)
		{
			logger.debug("General Exception occurred during DELETE!!!"
					+ e.getMessage());
			throw new ConnectorException(e.getMessage());
		}
		
		finally {
			if (connection != null)
				connection.disconnect();
		}
		return result;
	}
	/*@Override
	public Result enable(String id, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		HttpURLConnection connection = null;
		InputStreamReader isr = null;	
		OutputStream os = null;
		JSONObject newObject = new JSONObject();
		Result result = new Result();
		BufferedReader br = null;
		String line = "";
		String output = "";
		String object="";
		String url = null;
		
		try
		{
			if((boolean)options.get("fromCreateEnable"))
			{
				url = config.getString("host") + "/api/v1/users?filter=profile.login%20eq%20\""+id+"\""; //"/api/v1/users?filter=profile.login eq \""+id+"\"";
				connection = getConnection(url);
				connection.setRequestMethod("GET");
			    isr = new InputStreamReader(connection.getInputStream());
				br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					output += line;
				}				
				List<Map<String,Object>> users = (List<Map<String,Object>>)JsonUtil.parse(output);
				id = users.get(0).get("id").toString();
				line=""; output="";
				if(connection!=null)
				connection.disconnect();
			}
			url = config.getString("host") + "/api/v1/users/"+id+"/lifecycle/activate?sendEmail=false";
			
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type",
					"application/json; charset=utf8");
			connection.setRequestMethod("POST");
			os = connection.getOutputStream();
			logger.debug("Okta Enable Mehtod: JSON STRING OBJECT : "+newObject.toString());
			os.write(newObject.toString().getBytes("UTF-8"));
			os.close();
			
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;					
				}
				connection.disconnect();
				Map<String,Object> user = read(id);
				logger.debug("Okta ID enabled for the user : "+user.get("profile")+ " as : "+id);
				result.setObject(generateSingleObjectMap(user));
				result.setStatus(Result.Status.Committed);
			}
			else
			{				
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					object += line;					
				}
				logger.debug("Error occured for user : "+id+" during enable: \n"+object);
				throw new IOException(object);
			}
		
			//===============================
		
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug("IO Exception occurred during enabling account!!!"
					+ e.getMessage());
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} 
		catch (Exception e)
		{
			logger.debug("General Exception occurred during enabling account!!!"
					+ e.getMessage());
			throw new ConnectorException(e.getMessage());
		}			
		return result;
	}
	
	@Override
	public Result disable(String id, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		Result result = delete(id, options);
		return result;
	}*/
	
	@Override
	public Result enable(String id, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		HttpURLConnection connection = null;
		OutputStream os = null;
		JSONObject newObject = new JSONObject();
		Result result = new Result();
		BufferedReader br = null;
		String line = "";
		String output = "";		
		String url = config.getString("host")+"/api/v1/users/"+id+"/lifecycle/unsuspend";
				
		try
		{
			if(options.get("fromCreateEnable")!=null)
			{		
				id = options.get("login").toString();
				url = config.getString("host")+"/api/v1/users/"+id+"/lifecycle/activate";
			}
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type","application/json; charset=utf8");
			connection.setRequestMethod("POST");
			os = connection.getOutputStream();
			os.write(newObject.toString().getBytes("UTF-8"));
			os.close();
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;
				}
				result.setObject(read(id));
				result.setStatus(Result.Status.Committed);
			}
			else
			{		
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;										
				}				
				logger.debug("Error during account enable for user : "+id);
				throw new IOException(output);				
			}
		}
		catch(IOException io)
		{
			logger.debug("IOException occurred during enable account");
			throw new ConnectorException(io.getMessage());
		}
		
		return result;
	}
	@Override
	public Result disable(String id, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		HttpURLConnection connection = null;
		OutputStream os = null;
		JSONObject newObject = new JSONObject();
		Result result = new Result();
		BufferedReader br = null;
		String line = "";
		String output = "";
		String url = config.getString("host")+"/api/v1/users/"+id+"/lifecycle/suspend";
				
		try
		{
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type","application/json; charset=utf8");
			connection.setRequestMethod("POST");
			os = connection.getOutputStream();
			os.write(newObject.toString().getBytes("UTF-8"));
			os.close();
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;
				}
				result.setObject(read(id));
				result.setStatus(Result.Status.Committed);
			}
			else
			{		
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;										
				}				
				logger.debug("Error during account disable for user : "+id);
				throw new IOException(output);				
			}
		}
		catch(IOException io)
		{
			logger.debug("IOException occurred during disable account");
			throw new ConnectorException(io.getMessage());
		}
		
		return result;
	}
	
	public String getObject(String id) // Similar to HTTP GET Method
	{
		String url = "";
		String result = "";
		HttpURLConnection connection = null;
		InputStreamReader isr = null;		
		if(schema.getObjectType().equals(OBJECT_TYPE_ACCOUNT))
			url = config.getString("host") + "/api/v1/users/" + id; //?idType=externalId
		else if(schema.getObjectType().equals(OBJECT_TYPE_GROUP))
			url = config.getString("host") + "/api/v1/groups/" + id; //?idType=externalId	
		else if(schema.getObjectType().equals("app"))
			url = config.getString("host") + "/api/v1/apps/" + id;
		
		logger.debug("GET OBJECT URL "+url);
		try {
			connection = getConnection(url);
			connection.setRequestMethod("GET");
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				String line = "";
				isr = new InputStreamReader(connection.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					result += line;
				}
			}
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage()+" error in getUser() Method");
			e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage()+" error in getUser() Method");
			e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		}
		catch (Exception e)
		{
			logger.debug(e.getMessage()+" error in getUser() Method");
			e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		}
		finally {
			if (connection != null)
				connection.disconnect();
		}		
		return result;		
	}	
	
	public String getGroup(String id) // Similar to HTTP GET Method
	{
		String url = "";
		String result = "";
		HttpURLConnection connection = null;
		InputStreamReader isr = null;		
		
		url = config.getString("host") + "/api/v1/groups/" + id; //?idType=externalId	
		try {
			connection = getConnection(url);
			connection.setRequestMethod("GET");
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				String line = "";
				isr = new InputStreamReader(connection.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				while ((line = br.readLine()) != null) {
					result += line;
				}				
			}
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage()+" error in getUser() Method");
			e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage()+" error in getUser() Method");
			e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		}
		catch (Exception e)
		{
			logger.debug(e.getMessage()+" error in getUser() Method");
			e.printStackTrace();
			throw new ConnectorException(e.getMessage());
		}
		finally {
			if (connection != null)
				connection.disconnect();
		}		
		return result;		
	}	
	
	@Override
	public Result unlock(String id, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		HttpURLConnection connection = null;
		OutputStream os = null;
		JSONObject newObject = new JSONObject();
		Result result = new Result();
		BufferedReader br = null;
		String line = "";
		String output = "";
		String url = config.getString("host")+"/api/v1/users/"+id+"/lifecycle/unlock";
		
		try
		{
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type","application/json; charset=utf8");
			connection.setRequestMethod("POST");
			os = connection.getOutputStream();
			os.write(newObject.toString().getBytes("UTF-8"));
			os.close();
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;
				}
				result.setObject(read(id));
				result.setStatus(Result.Status.Committed);
			}
			else
			{		
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;										
				}				
				logger.debug("Error during account unlock for user : "+id);
				throw new IOException(output);				
			}
		}
		catch(IOException io)
		{
			logger.debug("IOException occurred during unlock account");
			throw new ConnectorException(io.getMessage());
		}
		finally {
			if (connection != null)
				connection.disconnect();
		}
		return result;
	}
	
	@Override
	public Map<String, Object> authenticate(String identity, String password)
			throws ConnectorException, AuthenticationFailedException,
			ExpiredPasswordException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		HttpURLConnection connection = null;
		OutputStream os = null;
		Map<String,Object> details = new HashMap<String,Object>();
		BufferedReader br = null;
		String line = "";
		String output = "";
		String url = config.getString("host")+"/api/v1/authn";
		
		Map<String,Object> user = read(identity);		
		if(user.get("status").toString().equalsIgnoreCase("PASSWORD_EXPIRED"))
		{
			throw new ExpiredPasswordException(identity);
		}
		details.put("username", identity);
		details.put("password", password);
		Map<String,Object> options = new HashMap<String,Object>();
		options.put("multiOptionalFactorEnroll", true);
		options.put("warnBeforePasswordExpired", true);
		details.put("options", options);
				
		try
		{
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type",
					"application/json; charset=utf8");
			connection.setRequestMethod("POST");
			os = connection.getOutputStream();
			JSONObject obj = new JSONObject(details);
			os.write(obj.toString().getBytes("UTF-8"));
			os.close();
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;
				}
				logger.debug("Authentication Successful : "+identity);
			}
			else
			{		
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;										
				}				
				logger.debug("Error during account auth for user : "+output);
				throw new AuthenticationFailedException(output);				
			}
		}
		catch(IOException io)
		{
			logger.debug("IOException occurred during auth account");
			throw new AuthenticationFailedException(io.getMessage());
		}	
		finally {
			if (connection != null)
				connection.disconnect();
		}
		return user;
	}
	
	@Override
	public Result setPassword(String id, String newPassword,
			String currentPassword, Date expiration, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		HttpURLConnection connection = null;
		OutputStream os = null;
		Result result = new Result();
		BufferedReader br = null;
		String line = "";
		String output = "";
		String url = config.getString("host")+"/api/v1/users/"+id;
		
		Map<String,Object> finalMap = new HashMap<String,Object>();
		Map<String,Object> credentials = new HashMap<String,Object>();
		Map<String,Object> password = new HashMap<String,Object>();
		
		password.put("value", newPassword);
		credentials.put("password", password);
		finalMap.put("credentials", credentials);
		
		try
		{
			connection = getConnection(url);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type","application/json; charset=utf8");
			connection.setRequestMethod("PUT");
			os = connection.getOutputStream();
			JSONObject obj = new JSONObject(finalMap);
			os.write(obj.toString().getBytes("UTF-8"));
			os.close();
			if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
			{
				br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;
				}
				logger.debug("Password Set Success!!");
				result.setObject(read(id));
				result.setStatus(Result.Status.Committed);
			}
			else
			{		
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
				while ((line = br.readLine()) != null) {
					output += line;										
				}				
				logger.debug("Error during account setPassword for user : "+id);
				throw new IOException(output);				
			}
		}
		catch(IOException io)
		{
			logger.debug("IOException occurred during setPassword");
			throw new ConnectorException(io.getMessage());
		}
		finally {
			if (connection != null)
				connection.disconnect();
		}
		return result;
	}	
	
	@Override
	public Schema discoverSchema() throws ConnectorException,

			UnsupportedOperationException {
		// TODO Auto-generated method stub
		//Schema schemas = new Schema();
		HttpURLConnection connection = null;
		String url = null;
		InputStreamReader isr = null;
		String result = "";
		String line = "";
		try
		{
			if(schema.getObjectType().equals(OBJECT_TYPE_ACCOUNT))
			{
				schema = new Schema();
				schema.addAttribute("id");
				schema.addAttribute("status");
				schema.setNativeObjectType("user");
				url = config.getString("host")+"/api/v1/meta/schemas/user/default/";
				connection = getConnection(url);
				connection.setRequestMethod("GET");
				if(connection.getResponseCode()>=200 && connection.getResponseCode()<300)
				{	
					isr = new InputStreamReader(connection.getInputStream());
					BufferedReader br = new BufferedReader(isr);
					while ((line = br.readLine()) != null) {
						result += line;
					}
					Map<String,Object> object = (Map<String,Object>) JsonUtil.parse(result);
					object = (Map<String,Object>) object.get("definitions");
					object = (Map<String,Object>) object.get("base");
					object = (Map<String,Object>) object.get("properties");
					Iterator iterator = object.keySet().iterator();
					while(iterator.hasNext())
						schema.addAttribute(iterator.next().toString());					
					schema.addAttribute("password");
					schema.addAttribute("group");
					schema.addAttribute("app");
					schema.addAttribute("role");
				}					
			}
			else if(schema.getObjectType().equals(OBJECT_TYPE_GROUP))
			{
				schema = new Schema();
				schema.setNativeObjectType("group");
				schema.addAttribute("id");
				schema.addAttribute("name");
				schema.addAttribute("type");
				schema.addAttribute("description");				
			}
		}
		catch(IOException ioe)
		{
			logger.debug("IOException in discover Schema");			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug("Exception : "+e.getMessage());
			e.printStackTrace();
		}
		finally {
			if(connection!=null)
				connection.disconnect();
		}
		return schema;
	}
	
	@Override
	public List<Feature> getSupportedFeatures(String objectType) {
		// TODO Auto-generated method stub
		List<Feature> features = super.getSupportedFeatures(objectType);
		//features.add(Connector.Feature.DISCOVER_SCHEMA);				
		return features;
	}
	
	@Override
	public List<String> getSupportedObjectTypes() {
		// TODO Auto-generated method stub
		List<String> types = super.getSupportedObjectTypes();
		types.add(OBJECT_TYPE_APP);
		//types.add("group");
		return types;		
	}
}