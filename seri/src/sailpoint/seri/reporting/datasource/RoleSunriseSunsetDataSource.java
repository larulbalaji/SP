package sailpoint.seri.reporting.datasource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.Identity;
import sailpoint.object.RoleAssignment;
import sailpoint.object.LiveReport;
import sailpoint.object.ManagedAttribute;
import sailpoint.object.QueryOptions;
import sailpoint.object.Sort;
import sailpoint.reporting.datasource.AbstractDataSource;
import sailpoint.reporting.datasource.JavaDataSource;
import sailpoint.tools.GeneralException;

import java.sql.*;

@SuppressWarnings("unused")
public class RoleSunriseSunsetDataSource extends AbstractDataSource implements JavaDataSource {

    private Iterator<Record>  resultIterator;
    private ArrayList<Record> results = new ArrayList<RoleSunriseSunsetDataSource.Record>();
    private Record            currentRecord;
    private static final Log  log     = LogFactory.getLog(RoleSunriseSunsetDataSource.class);

    @Override
    public int getSizeEstimate() throws GeneralException {

        return 0;
    }

    @Override
    public QueryOptions getBaseQueryOptions() {

        return null;
    }

    @Override
    public String getBaseHql() {
        return null;
    }

    @Override
    public Object getFieldValue(String field) throws GeneralException {
        switch (field) {
        case "identity":
            return currentRecord.getIdentityName();
        case "roleName":
            return currentRecord.getRoleName();
        case "sunriseDate":
            return currentRecord.getSunriseDate();
        case "sunsetDate":
            return currentRecord.getSunsetDate();
        }
        return "";
    }

    @Override
    public Object getFieldValue(JRField arg0) throws JRException {
        log.debug("getFieldValue (JRValue): ");
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(SailPointContext context, LiveReport report, Attributes<String, Object> arguments, String groupBy, List<Sort> sort)
            throws GeneralException {
        log.trace("Enter Initialize");

        // dump arguments
        for (String attr : arguments.keySet()) {
            log.debug("Argument: " + attr + ", value: " + arguments.get(attr));
        }

        ArrayList<String> identitiesWithRoles = new ArrayList<String>();

        // String sunrise = (String) arguments.get("sunriseDate");
        // String sunset = (String) arguments.get("sunsetDate");

        // We have to search via SQL since role startDate/endDate are stored in
        // the preferences of an Identity
        // Preferences is a CLOB and not a hibernate indexed column.
        // The SQL will return a list of identity IDs that have RoleAssignments.
        StringBuffer sqlbuffer = new StringBuffer("select id from spt_identity where ");
        // if no identities, we will use all identities.
        List<String> identitiesId = (List<String>) arguments.get("identities");
        if (null == identitiesId || identitiesId.size() == 0) {
            log.debug("No identities passed to the report. Defaulting to all");
            sqlbuffer.append(" preferences like '%startDate%' OR preferences like '%endDate%';");
        } else {
            //log.debug("Identities passed in to report");
            sqlbuffer.append("id in (");
            String joined = "'" + StringUtils.join(identitiesId, "','") + "'";
            sqlbuffer.append(joined);
            sqlbuffer.append(")");
            sqlbuffer.append("AND (preferences like '%startDate%' OR preferences like '%endDate%');");

        }

        log.debug("SQLString = " + sqlbuffer.toString());

        try {
            log.debug("Querying DB");
            Connection conn = context.getJdbcConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlbuffer.toString());
            while (rs.next()) {
                String id = rs.getString("id");
                identitiesWithRoles.add(id);
            }

            log.debug("Size of identitiesWithRoles = " + identitiesWithRoles.size());
            // String pattern = "MM/dd/yyyy hh:mm:ss a";
            // SimpleDateFormat format = new SimpleDateFormat(pattern);

            // Now we use the list of Identity IDs to query for those Identities
            // and then grab the RoleAssignments.
            for (String identityId : identitiesWithRoles) {
                Identity cube = context.getObjectById(Identity.class, identityId);
                // String name = cube.getName();
                List<RoleAssignment> roleAssignments = cube.getRoleAssignments();
                for (RoleAssignment ra : roleAssignments) {
                    String roleName = ra.getRoleName();
                    java.util.Date endDate = ra.getEndDate();
                    java.util.Date startDate = ra.getStartDate();
                    String endDateString = "";
                    String startDateString = "";
                    if (endDate != null) {
                        endDateString = endDate.toString();
                    }
                    if (startDate != null) {
                        startDateString = startDate.toString();
                    }

                    if (startDateString != null && endDate != null) {
                        Record record = new Record(cube, roleName, startDateString, endDateString);
                        results.add(record);
                    }

                }

            }

        } catch (Exception ex) {
            log.error(ex.toString());

        }

        log.debug("Results Size = " + results.size());
        for (Record r : results) {
            log.debug("Record:" + r.toString());
        }
        log.trace("Exit Initialize");
        resultIterator = results.iterator();

    }

    @Override
    public void setLimit(int startRow, int pageSize) {

    }

    @Override
    public boolean next() throws JRException {
        boolean hasNext = resultIterator.hasNext();
        log.debug("next: " + hasNext);
        if (hasNext)
            currentRecord = resultIterator.next();
        return hasNext;
        // return false;
    }

    private class Record {
        String roleName;
        String identityName;
        String SunriseDate;
        String SunsetDate;

        public Record(Identity i, String roleName, String SunriseDate, String SunsetDate) {
            identityName = i.getName();
            this.SunriseDate = SunriseDate;
            this.SunsetDate = SunsetDate;
            this.roleName = roleName;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public String getSunriseDate() {
            return SunriseDate;
        }

        public void setSunriseDate(String sunriseDate) {
            this.SunriseDate = sunriseDate;
        }

        public String getIdentityName() {
            return identityName;
        }

        public void setIdentityName(String identityName) {
            this.identityName = identityName;
        }

        public String getSunsetDate() {
            return SunsetDate;
        }

        public void setSunsetDate(String sunsetDate) {
            this.SunsetDate = sunsetDate;
        }

        @Override
        public String toString() {
            return "Record [identityName =" + identityName + ", roleName=" + roleName + ", sunriseDate =" + SunriseDate + ", sunsetDate =" + SunriseDate + "]";
        }

    }
}
