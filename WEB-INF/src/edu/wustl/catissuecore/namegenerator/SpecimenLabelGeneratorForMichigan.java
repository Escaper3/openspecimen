package edu.wustl.catissuecore.namegenerator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import edu.wustl.catissuecore.domain.Specimen;
import edu.wustl.catissuecore.util.global.Constants;

/**
 * This is the Specimen Label Generator for Michigan University.
 *
 *
 */
public class SpecimenLabelGeneratorForMichigan extends DefaultSpecimenLabelGenerator {

	/**
	 * 
	 */
	public SpecimenLabelGeneratorForMichigan() {
		//init();//TODO :Commented by Falguni because we are not using separate table for Michigan ,as not able to persist label count. 
		super();
	}
	/**
	 * Datasource Name
	 */
	String DATASOURCE_JNDI_NAME = "java:/catissuecore";
	/**
	 * This is a init() function it is called from the default constructor of
	 * Base class. When getInstance of base class called then this init function
	 * will be called. This method will first check the Datatbase Name and then
	 * set function name that will convert lable from int to String
	 */
	protected void init() 
	{
		String sql = "select MAX(LABEL_COUNT) from CATISSUE_SPECIMEN_LABEL_COUNT";
		Connection conn = null;
		currentLabel = new Long("0");
		try {
        	conn = getConnection();
        	ResultSet resultSet= conn.createStatement().executeQuery(sql);
        	
        	if(resultSet.next())
        	{
        		currentLabel = new Long (resultSet.getLong(1));
        	}	        
		}
        catch(NamingException e){
        	e.printStackTrace();
        }
        catch(SQLException ex)
        {
        	ex.printStackTrace();
        }
        finally
        {
        	if (conn!=null)
        	{
        		try {
					conn.close();
				} catch (SQLException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
        	}
        }

	}

	/**
	 * @return conn
	 * @throws NamingException
	 * @throws SQLException
	 */
	private Connection getConnection() throws NamingException, SQLException {
		Connection conn;
		InitialContext ctx = new InitialContext();
		DataSource ds = (DataSource)ctx.lookup(DATASOURCE_JNDI_NAME);
		conn = ds.getConnection();
		return conn;
	}

	
	/**
	 * @param input
	 * @param pattern
	 * @return String
	 */
	private String format(long input, String pattern) 
	{
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(input);
	}

	
	/**
	 * 
	 */
	private void persistLabelCount() 
	{
		String sql = "update CATISSUE_SPECIMEN_LABEL_COUNT SET LABEL_COUNT='"
				+ currentLabel + "'";

		try {
			Connection conn = getConnection();
			conn.createStatement().executeUpdate(sql);
		} catch (Exception daoexception) {
			daoexception.printStackTrace();
		}
	}
	/**
	 * Format for specimen: site_AutoIncrementingNumber
	 */
	
	public void setLabel(Object obj) {
		
		Specimen objSpecimen = (Specimen)obj;

		if(!labelCountTreeMap.containsKey(objSpecimen) &&	objSpecimen.getLineage().equals(Constants.NEW_SPECIMEN))				
		{
			String siteName = objSpecimen.getSpecimenCollectionGroup().getGroupName();
			currentLabel = currentLabel + 1;

			String year = new SimpleDateFormat("yy").format(new Date());
			String day = format(Calendar.getInstance().get(Calendar.DAY_OF_YEAR),
					"000");
			String nextNumber = format(currentLabel, "0000");

			//TODO :Commented by Falguni because hibernate session is getting closed by calling this method. 
			//persistLabelCount();
			//String label = siteName + "-" + year + "-" + day + "-" + nextNumber;
			//Modification suggested for Michigan only -as per catissuecore 1.2.0.1
			String label = siteName + "_" + nextNumber;
			objSpecimen.setLabel(label);
			labelCountTreeMap.put(objSpecimen,0);
		}
	
	
		else if(!labelCountTreeMap.containsKey(objSpecimen) && objSpecimen.getLineage().equals(Constants.ALIQUOT))				
		{
			setNextAvailableAliquotSpecimenlabel(objSpecimen.getParentSpecimen(),objSpecimen);
		}
	
	
		else if(!labelCountTreeMap.containsKey(objSpecimen) && objSpecimen.getLineage().equals(Constants.DERIVED_SPECIMEN))				
		{
			setNextAvailableDeriveSpecimenlabel(objSpecimen.getParentSpecimen(),objSpecimen);
		}
		
		if(objSpecimen.getChildrenSpecimen().size()>0)
		{
			Collection specimenCollection = objSpecimen.getChildrenSpecimen();
			Iterator it = specimenCollection.iterator();
			while(it.hasNext())
			{
				Specimen objChildSpecimen = (Specimen)it.next();
				setLabel(objChildSpecimen);
			}
			
		}	
		
	}
	
	/**
	 * This function is overridden as per Michgam requirement. 
	 * Format for derived specimen: parentSpecimenLabel_childCount+1
	 */
	
	synchronized void setNextAvailableDeriveSpecimenlabel(Specimen parentObject,Specimen specimenObject)
	{
						
		String parentSpecimenLabel = (String) parentObject.getLabel();				

		long aliquotCount = parentObject.getChildrenSpecimen().size();
		specimenObject.setLabel(parentSpecimenLabel + "_" + (format((aliquotCount + 1), "00")));
		labelCountTreeMap.put(specimenObject,0);
	}

	/**
	 * This function is overridden as per Michgam requirement. 
	 */
	synchronized void setNextAvailableAliquotSpecimenlabel(Specimen parentObject, Specimen specimenObject) 
	{
		
		String parentSpecimenLabel = (String) parentObject.getLabel();
		long aliquotChildCount = 0;
		if(labelCountTreeMap.containsKey(parentObject))
		{
			 aliquotChildCount= Long.parseLong(labelCountTreeMap.get(parentObject).toString());	
		}
		else
		{
			// biz logic 
			aliquotChildCount = parentObject.getChildrenSpecimen().size();	
			
		}
		specimenObject.setLabel( parentSpecimenLabel + "_"+ format((++aliquotChildCount), "00"));
		labelCountTreeMap.put(parentObject,aliquotChildCount);	
		labelCountTreeMap.put(specimenObject,0);
		
	}
	
}