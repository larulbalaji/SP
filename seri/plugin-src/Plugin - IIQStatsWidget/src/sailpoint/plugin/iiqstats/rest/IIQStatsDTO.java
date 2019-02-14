package sailpoint.plugin.iiqstats.rest;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class IIQStatsDTO {
    private String _statName  = new String();
    private String _statCount = new String();
    private String _statArrow = new String();
    private String _statUrl   = new String();

    public String getStatName() {
        return _statName;
    }

    public void setStatName(String _statName) {
        this._statName = _statName;
    }

    public String getStatCount() {
        return _statCount;
    }

    public void setStatCount(String _statCount) {
        this._statCount = _statCount;
    }

    public String getStatArrow() {
        return _statArrow;
    }

    public void setStatArrow(String _statArrow) {
        this._statArrow = _statArrow;
    }

    public String getStatUrl() {
        return _statUrl;
    }

    public void setStatUrl(String _statUrl) {
        this._statUrl = _statUrl;
    }

}