package sailpoint.plugin.siqstats.rest;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class SIQStatsDTO {
    private String _statName  = new String();
    private String _statScore = new String();
    private String _statValue = new String();
    private String _statArrow = new String();
    private String _statUrl   = new String();
    private String _statColor = new String();
    

    public String getStatName() {
        return _statName;
    }

    public void setStatName(String _statName) {
        this._statName = _statName;
    }

    public String getStatScore() {
        return _statScore;
    }

    public void setStatScore(String _statScore) {
        this._statScore = _statScore;
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

    public String getStatValue() {
        return _statValue;
    }

    public void setStatValue(String _statValue) {
        this._statValue = _statValue;
    }

    public String getStatColor() {
        return _statColor;
    }

    public void setStatColor(String _statColor) {
        this._statColor = _statColor;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\tStatName = " + _statName);
        sb.append("\tStatValue = " + _statValue);
        sb.append("\tStatScore = " + _statScore);
        sb.append("\tStatColor = " + _statColor);
        
        return sb.toString();
    }

}