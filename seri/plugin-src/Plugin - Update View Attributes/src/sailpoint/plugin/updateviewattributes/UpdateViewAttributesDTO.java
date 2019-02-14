package sailpoint.plugin.updateviewattributes;

import java.util.List;
import java.util.Map;

/**
 */
public class UpdateViewAttributesDTO {
    private List<Map<String, String>> current;
    private List<Map<String, String>> available;
    private String newAttrs;
    
    public List<Map<String, String>> getCurrent() {
      return current;
    }
    public void setCurrent(List<Map<String, String>> current) {
      this.current = current;
    }
    public List<Map<String, String>> getAvailable() {
      return available;
    }
    public void setAvailable(List<Map<String, String>> available) {
      this.available = available;
    }
    public String getNewAttrs() {
      return newAttrs;
    }
    public void setNewAttrs(String newAttrs) {
      this.newAttrs = newAttrs;
    }

}
