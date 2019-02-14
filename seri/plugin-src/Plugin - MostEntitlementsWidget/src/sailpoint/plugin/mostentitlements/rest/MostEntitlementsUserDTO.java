package sailpoint.plugin.mostentitlements.rest;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class MostEntitlementsUserDTO {
    private String _displayName = new String();
    private String _count = new String();
    private String _id = new String();

    public String getDisplayName() {
        return _displayName;
    }

    public void setDisplayName(String displayName) {
        this._displayName = displayName;
    }

    public String getCount() {
        return _count;
    }

    public void setCount(String count) {
        this._count = count;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }


}