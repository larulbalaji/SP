package sailpoint.plugin.sensitivedata.rest;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SensitiveDataUserDTO {
    private String _displayName = new String();
    private String _id = new String();
    private String _riskStatus = new String();
    
    public String getDisplayName() {
        return _displayName;
    }

    public void setDisplayName(String displayName) {
        this._displayName = displayName;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public void setRiskStatus(String riskStatus) {
        this._riskStatus = riskStatus;
    }

    public String getRiskStatus() {
        return _riskStatus;
    }

    

}