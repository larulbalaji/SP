package sailpoint.seri.reporting.datasource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.DynamicValuator;
import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.DynamicValue;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.LiveReport;
import sailpoint.object.QueryOptions;
import sailpoint.object.ReportColumnConfig;
import sailpoint.object.Sort;
import sailpoint.reporting.datasource.AbstractDataSource;
import sailpoint.reporting.datasource.DataSourceColumnHelper;
import sailpoint.reporting.datasource.JavaDataSource;
import sailpoint.search.ExternalAttributeFilterBuilder;
import sailpoint.tools.GeneralException;

public class AttributeHistoryDataSource extends AbstractDataSource implements JavaDataSource {

	private static Log log = LogFactory.getLog(AttributeHistoryDataSource.class);
	
	private QueryOptions baseQueryOptions;
	private SailPointContext context;
	private LiveReport report;
	private List<ReportColumnConfig> columns;

	// If any sorting is required, we need to load in the whole results set
	// and sort it "manually"
	// we also need a flag to change the functionality of returning results
	private List<Object[]> sortedList;
	private Iterator<Object[]> preloadedIterator;
	private Object[] preloadedRow;
	private boolean preloaded=false;
	
	private List<AttrHistorySortItem> sort;
	
	private Integer startRow;
	private Integer pageSize;
	
    protected DataSourceColumnHelper columnHelper;

	// Nested iterator
	// From the search, we iterate through the identities
	// From the identity, we iterate through the 'historyAttributes'
	// From the attribute, we iterate through the values
	
	private Iterator<Object[]> identityiterator;
	private Iterator<String> attributeiterator;
	private int aiidx;
	private Iterator<String> valueiterator;

	private Object[] currentIdentity;
	private List<String> currentAttribute;
	private String currentAttributeName;
	private String currentValue;
	
	private List<String> historyAttributes;

	private Locale locale;
	private TimeZone timezone;
	
	@Override
	public int getSizeEstimate() throws GeneralException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public QueryOptions getBaseQueryOptions() {
		return baseQueryOptions;
	}

	@Override
	public String getBaseHql() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getFieldValue(String field) throws GeneralException {
		
        Object out=null;
        
		if ("name".equals(field)){
			if(preloaded) {
				out=preloadedRow[0];
			} else {
				out=currentIdentity[0];
			}
		} else if ("attributeName".equals(field)){
			if(preloaded) {
				out=preloadedRow[1];
			} else {
				out=currentAttributeName;
			}
		} else if ("date".equals(field)) {
	        if(preloaded) {
	        	out=preloadedRow[2];
	        } else {
	        	out = getDateFromValue(currentValue);
	        }
		} else if ("value".equals(field)) {
			if(preloaded) {
				out=preloadedRow[3];
			} else {
              out = getValueFromValue(currentValue);
			}
		} else {
			//throw new GeneralException("Unknown column '"+field+"'");
			out="no such field "+field;
		}
		
		return doRender(field, out);
	}

	private Object getValueFromValue(String currentValue) {
		String out="--";
		  if (currentValue!=null) {
		    int idx=currentValue.indexOf("|");
		    if(idx!=-1  && idx<(currentValue.length()-1)) {
		      out=currentValue.substring(idx+1);
		    }
		  }
		return out;
	}

	private Object getDateFromValue(String currentValue) {
		String out="--";
		if (currentValue!=null) {
		  int idx=currentValue.indexOf("|");
		  if(idx!=-1) {
		    String srcDate=currentValue.substring(0,idx);
		    SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		    try {
		    	Date d=sdf.parse(srcDate);
		    	out=d.toString();
		    } catch (ParseException pe) {
		    	log.error("Parse Exception parsing '"+srcDate+"'");
		    }
		  }
		}
		return out;
	}

	private Object doRender(String field, Object out) throws GeneralException {

		log.trace("doRender: field="+field+" out="+out);
		
        ReportColumnConfig col = report.getGridColumnByFieldName(field);

        DynamicValue renderer = col.getRenderDef();
        if(renderer!=null) {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("value", out);
        	args.put("context", context);
        	args.put("column", col);
        	args.put("scriptArgs", null);
//        args.put("scriptArgs", scriptArgs);
	        args.put("locale", locale);
	        args.put("timezone", timezone);
//        args.put("renderCache", renderCache);
        	
	        // make sure that any render rules are still
	        // attached to the session or we could get a lazy init exception
	        if (col.getRenderRule() != null){
	            context.attach(col.getRenderRule());
	        }
	
	        DynamicValuator valuator = new DynamicValuator(renderer);
	        return valuator.evaluate(context, args);
        } else {
        	return out;
        }

	}
	
	@Override
	public Object getFieldValue(JRField arg0) throws JRException {
		// TODO Auto-generated method stub
		try {
			return getFieldValue(arg0.getName());
		} catch (GeneralException ge) {
			throw new JRException(ge);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void initialize(SailPointContext context, LiveReport report,
			Attributes<String, Object> arguments, String groupBy,
			List<Sort> sort) throws GeneralException {

		this.context=context;
		this.report=report;
		this.setColumns(report.getGridColumns());
		this.locale=getLocale();
		this.timezone=getTimezone();

		baseQueryOptions=new QueryOptions();
		
		
		if(arguments.containsKey("historyAttributes")) {
			this.historyAttributes=arguments.getList("historyAttributes");
			if(historyAttributes.size()>1) {
				List<Filter> ors=new ArrayList<Filter>();
				for(String itm: historyAttributes) {
					log.debug("Adding OR NOT NULL filter for "+itm);
					Filter f=ExternalAttributeFilterBuilder.buildNotNullFilters(ExternalAttributeFilterBuilder.IDENTITY_EXTERNAL,
							ExternalAttributeFilterBuilder.IDENTITY_JOIN,
							itm);
					ors.add(f);
				}
			  	baseQueryOptions.add(Filter.or(ors));
			} else {
				log.debug("Setting NOT NULL filter for "+historyAttributes.get(0));
				baseQueryOptions.add(ExternalAttributeFilterBuilder.buildNotNullFilters(ExternalAttributeFilterBuilder.IDENTITY_EXTERNAL,
						ExternalAttributeFilterBuilder.IDENTITY_JOIN,
						(String)historyAttributes.get(0)));
			}
		}
		// Add the rest of the filter attributes
		for(Map.Entry<String, Object> entry: arguments.entrySet()) {
			if(entry.getKey().startsWith("_attr_")) {
				String key=entry.getKey().substring(6); // Strip _attr_
				Object value=entry.getValue();
				if (value!=null) {
					if(key.equals("manager")) {
						// Identity type attribute - have to search by <attrname>.id
						baseQueryOptions.add(Filter.in("manager.id", (Collection)value));
					} else if(value instanceof String || value instanceof Boolean) {
						baseQueryOptions.add(Filter.eq(key, value));
					} else if (value instanceof Collection) {
						baseQueryOptions.add(Filter.in(key, (Collection)value));
					} else {
						log.error("No handler for attribute '"+key+"' - value type="+value.getClass().getName());
					}
				}
			}
		}
		if (sort != null){
	      this.sort=new ArrayList<AttrHistorySortItem>();
	      for (Sort srt: sort) {
	    	  AttrHistorySortItem aSort=new AttrHistorySortItem();
	    	  switch (srt.getField()) {
	    	  case "name": {
	    		  aSort.columnNumber=0; break;
	    	  }
	    	  case "attributeName": {
	    		  aSort.columnNumber=1; break;
	    	  }
	    	  case "date": {
	    		  aSort.columnNumber=2; break;
	    	  }
	    	  case "value": {
	    		  aSort.columnNumber=3; break;
	    	  }
	    	  }
	    	  aSort.isAscending=srt.isAscending();
	    	  this.sort.add(aSort);
	      }
		}
		if (groupBy != null)
		  baseQueryOptions.setGroupBys(Arrays.asList(groupBy));

	}

	@Override
	public void setLimit(int startRow, int pageSize) {
		this.startRow=startRow;
		this.pageSize=pageSize;
	}

	public boolean next() throws JRException {
		if (identityiterator == null){
			try {
				prepare();
			} catch (GeneralException e) {
				throw new JRException(e);
			}
		}
		return getNext();
	}
	
	@SuppressWarnings("unchecked")
	private void prepare() throws GeneralException{
		log.debug("Preparing..");
		
		for(Filter f: baseQueryOptions.getFilters()) {
			log.trace(f.toXml());
		}
		
		QueryOptions ops = new QueryOptions(baseQueryOptions);
		if (startRow != null && startRow > 0){
			ops.setFirstRow(startRow);
		}
		if (pageSize != null && pageSize > 0){
			ops.setResultLimit(pageSize);
		}
		ops.setDistinct(true);
		
		List<String> attrs=new ArrayList<String>();
		attrs.add("name");
		//attrs.addAll(historyAttributes);
		identityiterator = context.search(Identity.class, ops, attrs);
		if(sort!=null) {
			// Ok, someone wants this list sorting
			// we're going to need to read in the full list
			// and then sort it. Sigh..
			sortedList=new ArrayList<Object[]>();
			while(getNext()) {
				Object[] entry=new Object[4];
				entry[0]=currentIdentity[0];
				entry[1]=currentAttributeName;
				entry[2]=getDateFromValue(currentValue);
				entry[3]=getValueFromValue(currentValue);
				sortedList.add(entry);
			}
			Collections.sort(sortedList, new FieldComparator(sort));
			preloadedIterator=sortedList.iterator();
			preloaded=true;
		}
	}
	
	private boolean getNext() {
		// Having the list preloaded is easy - just iterate to the next row 
		if(preloaded) {
			if(preloadedIterator.hasNext()) {
				preloadedRow=preloadedIterator.next();
				return true;
			}
			return false;
		}
		
		// if we don't have a value iterator, we need one from the next attribute
		// if there are no more attributes, we're done. return false
		// Otherwise, set up an iterator for this attribute's values
		// if it's empty, try the next
		// otherwise, set the 'currentValue'
		if(valueiterator==null) {
			getNextAttr();
			if(currentAttribute==null) {
				return false; // out of attrs and identities
			}
			valueiterator=currentAttribute.iterator();
		}
		if(valueiterator.hasNext()) {
			currentValue=valueiterator.next();
			return true;
		}
		valueiterator=null;
		return getNext();
	}
	
	@SuppressWarnings("unchecked")
	private void getNextAttr() {
		// if we don't have an attribute iterator, get one
		// and set the currentAttribute to the attribute value from the identity
		// if no identity, go get next
		if(attributeiterator==null || !attributeiterator.hasNext()) {
			getNextIdentity();
			if(currentIdentity==null) {
				currentAttribute=null;
				return; // we are done
			}
			attributeiterator=historyAttributes.iterator(); // iterator of attribute names
			aiidx=0;
		}
		if(attributeiterator.hasNext()) {
			currentAttributeName=attributeiterator.next();
			Object vals=currentIdentity[++aiidx]; // same as aiidx+1; aiidx++
			if(vals==null) {
				// skip to next record
				// If there are like, a gabazillion records with no values, could we run out of stack here?
				// There was a similar bug found on CIQ recently
				getNextAttr();
			} else {
				if(vals instanceof String) {
					currentAttribute=new ArrayList<String>();
					currentAttribute.add((String)vals);
				} else {
					currentAttribute=(List<String>)vals;
				}
			}
		}
	}
	
	private void getNextIdentity() {
		if(identityiterator.hasNext()) {
			currentIdentity=identityiterator.next();
			if(historyAttributes.size()>0) {
				int idx=currentIdentity.length;
				currentIdentity=Arrays.copyOf(currentIdentity, currentIdentity.length+historyAttributes.size());
				// get the attributes from the identity
				try {
					Identity theUser=context.getObjectByName(Identity.class, (String)currentIdentity[0]);
					for(String attr: historyAttributes) {
					  Object values=theUser.getAttribute(attr);
					  if(values!=null) {
						  if(values instanceof String) {
							  List<String> vals=new ArrayList<String>();
							  vals.add((String)values);
							  currentIdentity[idx++]=vals;
						  } else {
							  currentIdentity[idx++]=values;
						  }
					  } else currentIdentity[idx++]=null;
					}
				} catch (GeneralException e) {
					log.error("GeneralException getting Identity "+currentIdentity[0]);
				}
			}
		} else {
			currentIdentity=null; // we're done
		}
	}
	
//    private ReportColumnConfig getColumnConfig(String field){
//
//        ReportColumnConfig col = null;
//
//        if (columns != null){
//            for(ReportColumnConfig column : columns){
//                if (column.getField().equals(field)){
//                    col = column;
//                    break;
//                }
//            }
//        }
//        return col;
//    }
    
    public List<ReportColumnConfig> getColumns() {
		return columns;
	}

	public void setColumns(List<ReportColumnConfig> columns) {
		this.columns = columns;
	}

	@SuppressWarnings("rawtypes")
	private class FieldComparator implements Comparator {
    	
    	private List<AttrHistorySortItem> sortOrder;
    	   	
    	public FieldComparator(List<AttrHistorySortItem> sortOrder) {
    		this.sortOrder=sortOrder;
    	}

		@Override
		public int compare(Object o1, Object o2) {
			Object[] first=(Object[])o1;
			Object[] second=(Object[])o2;
			
			// we know these are arrays of strings
			
			for(AttrHistorySortItem sort: sortOrder) {
				// handle incorrectly sized arrays, or null values
				if (first.length<sort.columnNumber || first[sort.columnNumber]==null) return sort.isAscending?-1:1;
				if (second.length<sort.columnNumber || second[sort.columnNumber]==null) return sort.isAscending?1:-1;
				
				// now compare the values
				String firstValue=(String)first[sort.columnNumber];
				String secondValue=(String)second[sort.columnNumber];
				int comp=firstValue.compareTo(secondValue);
				if(comp!=0) return sort.isAscending?comp:-comp;
			}
			// we checked all the sorts and they are the same
			return 0;
		}
    	
    	
    }
    
    private class AttrHistorySortItem {
    	public int columnNumber;
    	public boolean isAscending;
    }
}
