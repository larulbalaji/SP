package sailpoint.seri.execute;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.object.Attributes;
import sailpoint.object.Custom;
import sailpoint.object.SailPointObject;
import sailpoint.object.TaskDefinition;
import sailpoint.server.ImportExecutor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.xml.AbstractXmlObject;

public class SetAttribute implements ImportExecutor{

  /*
   * Custom ImportExecutor task for setting attributes on objects that aren't
   * "mergeable". Task takes a <Custom> object, where the id attribute is the java class
   * of the object, and the name is the name. The Attributes Map of the Custom object
   * is the name/value pairs you want to set on the object.
   * The previous value of the attribute can be referenced with '$value'
   * 
   * e.g.
   * <?xml version='1.0' encoding='UTF-8'?>
   * <!DOCTYPE sailpoint PUBLIC "sailpoint.dtd" "sailpoint.dtd">
   * <sailpoint>
   *   <ImportAction name='execute' value="sailpoint.seri.execute.SetAttribute">
   *     <Custom id="sailpoint.object.UIConfig" name="UIConfig">
   *       <Attributes>
   *         <Map>
   *           <entry key="identityViewAttributes" value="$value,locale"/>
   *         </Map>
   *        </Attributes>
   *     </Custom>
   *   </ImportAction>
   * </sailpoint>
   * 
   * there is also:
   *      <entry key="methodName" value="Argument"/>
   * this is for objects that have different names for getter and setter (but still it's a String/Object Map)
   * e.g. TaskDefinition
   */

  private static Log log = LogFactory.getLog(SetAttribute.class);
  Custom payload;

  @Override
  public boolean requiresConnection() {
    // TODO Auto-generated method stub
    System.out.println("ImportExecutor.requiresConnection:");
    return false;
  }

  @Override
  public void execute(Context context) throws GeneralException {
    if(payload==null) {
      throw new GeneralException("No payload specified");
    }
    String objectClass=payload.getId();
    String objectName=payload.getName();
    Attributes<String,Object> attrs=payload.getAttributes();
    String methodName="Attribute";
    if (attrs.get("methodName")!=null) {
      methodName=(String)attrs.get("methodName");
      attrs.remove("methodName");
    }
    for(String key: attrs.keySet()) {        
      Object newValue = attrs.get(key);      
      setAttribute(context, objectClass, objectName, methodName, key, newValue);
    }

  }

  private void setAttribute(Context context, String objectClass, String objectName, String methodName,String key, Object newValue) throws GeneralException {
    //System.out.println("Checking for "+objectClass+" : "+objectName);
    try {
      // Get the class object
      Class clazz=Class.forName(objectClass);
      // use reflection to find the getter and setter methods
      Method setAttr=null;
      Method getAttr=null;
      boolean useAttributesMethods=false;
      try {
        setAttr=clazz.getMethod("set"+methodName, String.class, Object.class);
        getAttr=clazz.getMethod("get"+methodName, String.class);
      } catch (NoSuchMethodException e) {
        try {
          // this type of object does not have individual getAttribute/setAttribute methods
          // try with getAttributes() and setAttributes();
          getAttr=clazz.getMethod("getAttributes");
          setAttr=clazz.getMethod("setAttributes", Attributes.class);
          useAttributesMethods=true;
        } catch (NoSuchMethodException e1) {
          throw new GeneralException("Can't set attributes on "+objectClass+" via setAttribute() or setAttributes(Attribute)");
        }
      }

      SailPointObject spObj=context.getContext().getObjectByName(clazz, objectName);
      if(spObj==null) {
        throw new GeneralException("Couldn't find object "+objectName);
      }      
      if(newValue instanceof String) {
        // Check if we need to do a substitution of $value
        String sNewValue=(String)newValue;
        if(sNewValue.contains("$value")) {
          String oldValue=getAttribute(spObj, getAttr, useAttributesMethods, key);
          //System.out.println("Old Value: "+oldValue);
          newValue=((String) newValue).replace("$value", oldValue);
        }
      }
      if(useAttributesMethods) {
        Attributes attrs=(Attributes) getAttr.invoke(spObj, null);
        attrs.put(key, newValue);
        setAttr.invoke(spObj, attrs);
      } else {
        setAttr.invoke(spObj, key, newValue);
      }
      
    } catch (ClassNotFoundException cnfe) {
      throw new GeneralException("Could not find class "+objectClass);
    } catch (SecurityException e) {
      throw new GeneralException("SecurityException: "+e);
    } catch (IllegalAccessException e) {
      throw new GeneralException("Can't access setAttribute() method on object");
    } catch (IllegalArgumentException e) {
      throw new GeneralException("Illegal Argument for setAttribute()");
    } catch (InvocationTargetException e) {
      throw new GeneralException(e);
    }
  }

  
  
  private String getAttribute(SailPointObject spObj, Method getAttr,
      boolean useAttributesMethods, String key) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    // TODO Auto-generated method stub
    if(useAttributesMethods) {
      Attributes attrs=(Attributes) getAttr.invoke(spObj, null);
      return (String)attrs.get(key);
    } else {
      Object value=getAttr.invoke(spObj, key);
      return (String)value;
    }
  }

  @Override
  public AbstractXmlObject getArgument() {
    // TODO Auto-generated method stub
    //System.out.println("ImportExecutor.getArgument:");
    return null;
  }

  @Override
  public void setArgument(AbstractXmlObject arg) {
    if (!(arg instanceof Custom)) {
      log.error("SetAttribute import task requires a 'Custom' object as payload");
    } else {
      this.payload=(Custom)arg;
    }
  }

}
