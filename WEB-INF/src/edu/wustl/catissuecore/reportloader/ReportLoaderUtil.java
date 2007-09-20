package edu.wustl.catissuecore.reportloader;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.wustl.catissuecore.caties.util.CaCoreAPIService;
import edu.wustl.catissuecore.domain.Participant;
import edu.wustl.catissuecore.domain.Site;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.common.lookup.DefaultLookupResult;

/**
 * @author sandeep_ranade
 * Utility class containing all general purpose methods.
 */
public class ReportLoaderUtil
{
	/**
	 * @param participant object of Participant
	 * @return number of matching participants
	 * @throws Exception throws exception 
	 */
	public static Set<Participant> checkForParticipant(Participant participant)throws Exception
	{
		Set<Participant> result=null;
		List participantList=(List)CaCoreAPIService.getAppServiceInstance().getParticipantMatchingObects(participant);
		// check for matching participant list
   		if(participantList!=null && participantList.size()>0)
		{
   			result=new HashSet<Participant>();
   			// prepare list of participant object out of DefaultLookupResult List
   			for(int i=0;i < participantList.size();i++)
   			{
				DefaultLookupResult participantResult=(DefaultLookupResult)participantList.get(i);
				result.add((Participant)participantResult.getObject());
			}
		}
		return result;
	}
	
	/**
	 * This method creates new directory.
	 * @param dirStr directory name  
	 * @throws IOException IOException occured while creating directories
	 */
	public static void createDir(String dirStr)throws IOException
	{
			File dir= new File(dirStr); 
			if(dir==null || !dir.exists())
			{	
				dir.mkdir();
			}
	}
	
	
	 /**
     * 
     * @param ssn Social Security Number to check
     * @return boolean depending on the value of ssn.
     */
    public static String getValidSSN(String ssn)
    {
    	boolean result = true;
    	String validSSN=null;
    	Pattern re=null;
    	Matcher  mat=null;
    	StringBuffer buff=null;
    	try
		{
    		re = Pattern.compile("[0-9]{3}-[0-9]{2}-[0-9]{4}", Pattern.CASE_INSENSITIVE);
    	    mat =re.matcher(ssn); 
    	    result = mat.matches();
    	    if(!result)
    	    {
    	    	re = Pattern.compile("[0-9]{9}", Pattern.CASE_INSENSITIVE);
    	    	mat =re.matcher(ssn); 
        	    result = mat.matches();
        	    if(result)
        	    {
        	    	buff=new StringBuffer();
        	    	buff.append(ssn.substring(0,3))
        	    	     .append("-")
        	    	     .append(ssn.substring(3,5))
        	    	     .append("-")
        	    	     .append(ssn.substring(5,9));
        	    	validSSN=buff.toString();
        	    }
            }
    	    else
    	    {
    	    	validSSN=ssn;
    	    }
    	}
    	catch(Exception exp)
		{
			return validSSN;
		}
    	return validSSN;
    }
    
    /**
	 * @param reportText
	 * @return
	 */
	public static String getLineFromReport(String reportText,String lineToExtract)
	{
		String[] lines = reportText.split("\n");		
		for(int i = 0; i < lines.length ; i++)
		{
			String line = lines[i];
			if(line.startsWith(lineToExtract))
			{				
				return line;
			}
		}	
		return "";
	}
	
	public static SpecimenCollectionGroup checkForSpecimenCollectionGroup(Participant participant, Site site, String surgicalPathologyNumber)throws Exception
	{
		SpecimenCollectionGroup scg=getExactMatchingSCG(participant, site, surgicalPathologyNumber);
		if(scg!=null)
		{
			return scg;
		}
		else
		{
			if(isPartialMatchingSCG(participant, site))
			{
				return new SpecimenCollectionGroup();
			}
		}
		return null;
	}
	
	public static SpecimenCollectionGroup getExactMatchingSCG(Participant participant, Site site, String surgicalPathologyNumber)
	{
		String scgHql = "select scg"+
	    " from edu.wustl.catissuecore.domain.SpecimenCollectionGroup as scg, " +
		" edu.wustl.catissuecore.domain.CollectionProtocolRegistration as cpr,"+
		" edu.wustl.catissuecore.domain.Participant as p "+
		" where p.id = " +participant.getId()+ 
		" and p.id = cpr.participant.id " +
		" and scg.id in elements(cpr.specimenCollectionGroupCollection)" +
		" and scg.specimenCollectionSite.name='"+site.getName()+"' "+
		" and scg.surgicalPathologyNumber='"+surgicalPathologyNumber+"'";
		
		List resultList=(List)CaCoreAPIService.executeQuery(scgHql, SpecimenCollectionGroup.class.getName());
		if(resultList!=null && resultList.size()==1)
		{
			return (SpecimenCollectionGroup)resultList.get(0);
		}
		return null;
	}
	
	public static boolean isPartialMatchingSCG(Participant participant, Site site)
	{
		String scgHql = "select scg"+
	    " from edu.wustl.catissuecore.domain.SpecimenCollectionGroup as scg, " +
		" edu.wustl.catissuecore.domain.CollectionProtocolRegistration as cpr,"+
		" edu.wustl.catissuecore.domain.Participant as p "+
		" where p.id = " +participant.getId()+ 
		" and p.id = cpr.participant.id " +
		" and scg.id in elements(cpr.specimenCollectionGroupCollection)" +
		" and scg.specimenCollectionSite.name='"+site.getName()+"' "+
		" and (scg.surgicalPathologyNumber="+null+
		" or scg.surgicalPathologyNumber='')";
		
		List resultList=(List)CaCoreAPIService.executeQuery(scgHql, SpecimenCollectionGroup.class.getName());
		if(resultList!=null && resultList.size()>0)
		{
			return true;
		}
		return false;
	}
	
}
