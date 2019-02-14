package sailpoint.seri.execute;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.object.Custom;
import sailpoint.object.ObjectAttribute;
import sailpoint.object.ObjectConfig;
import sailpoint.object.SailPointObject;
import sailpoint.server.ImportExecutor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.xml.AbstractXmlObject;

public class MakeSearchable implements ImportExecutor{

  /*
   * Custom ImportExecutor task for making an Identity attribute searchable.
   * Task takes a <Custom> object, where the name attribute is the name
   * of the attribute. The task will then find the next free 'extended' number
   * and add it to the attribute, to make it searchable
   * 
   * e.g.
   * <?xml version='1.0' encoding='UTF-8'?>
   * <!DOCTYPE sailpoint PUBLIC "sailpoint.dtd" "sailpoint.dtd">
   * <sailpoint>
   *   <ImportAction name='execute' value="sailpoint.seri.execute.MakeSearchable">
   *     <Custom name="department"/>
   *   </ImportAction>
   * </sailpoint>
   */

  private static Log log = LogFactory.getLog(MakeSearchable.class);
  Custom payload;

  @Override
  public boolean requiresConnection() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void execute(Context context) throws GeneralException {
    if(payload==null) {
      throw new GeneralException("No payload specified");
    }
    String attrName=payload.getName();
    makeSearchable(context, attrName);
  }

  private void makeSearchable(Context context, String attrName) throws GeneralException {
    
    ObjectConfig spObj=context.getContext().getObjectByName(ObjectConfig.class, "Identity");
    if(spObj==null) {
      throw new GeneralException("Can't find Identity config!!!");
    }
    ObjectAttribute attr=spObj.getObjectAttribute(attrName);
    if(attr==null) {
      throw new GeneralException("Can't find Identity attribute: "+attrName);
    }
    if(attr.isNamedColumn()) {
      throw new GeneralException("Attribute '"+attrName+"' is already searchable via a named column");
    }
    if(attr.isSearchable()) {
      throw new GeneralException("Attribute '"+attrName+"' is already searchable");
    }
    int slot=getAvailableExtendedSlotByType(attr.getType(), spObj.getObjectAttributes());
    if (slot==-1) {
      throw new GeneralException("Unable to find a spare index for attribute: "+attrName);
    }
    attr.setExtendedNumber(slot);

  }

  
  // Copied from BaseAttributeEditBean
  /**
   * Will return the available slot of extended attributes of type identity which
   * are mapped by extendedIdentity1 etc relationships.
   */
  protected int getAvailableExtendedSlotByType(String type, List<ObjectAttribute> allAttrs) {

      int slotNumber = -1;
    
      if (type==null) type=ObjectAttribute.TYPE_STRING; // the default
      
      Set<Integer> usedIndices = new HashSet<Integer>();
      for (ObjectAttribute extendedAttr : allAttrs) {
          if (type.equals(extendedAttr.getType()) && extendedAttr.getExtendedNumber() > 0) {
              usedIndices.add(extendedAttr.getExtendedNumber());
          }
      }
      int max=-1;
      if (type.equals(ObjectAttribute.TYPE_IDENTITY)) {
        max=SailPointObject.MAX_EXTENDED_IDENTITY_ATTRIBUTES;
      } else {
        // Any attribute that's not an identity is stored as a string. According to the hbm.xml file, anyway..
        max=SailPointObject.MAX_EXTENDED_ATTRIBUTES;
      }
      
      for (int i = 1; i<max; ++i) {
          if (!usedIndices.contains(i)) {
              slotNumber = i;
              break;
          }
      }
      
      return slotNumber;
  }
  
  @Override
  public AbstractXmlObject getArgument() {
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
