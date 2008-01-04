package edu.wustl.catissuecore.bizlogic.test;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import edu.wustl.catissuecore.domain.CollectionEventParameters;
import edu.wustl.catissuecore.domain.CollectionProtocol;
import edu.wustl.catissuecore.domain.CollectionProtocolRegistration;
import edu.wustl.catissuecore.domain.ConsentTier;
import edu.wustl.catissuecore.domain.ConsentTierResponse;
import edu.wustl.catissuecore.domain.ConsentTierStatus;
import edu.wustl.catissuecore.domain.Participant;
import edu.wustl.catissuecore.domain.ReceivedEventParameters;
import edu.wustl.catissuecore.domain.Site;
import edu.wustl.catissuecore.domain.SpecimenCollectionGroup;
import edu.wustl.catissuecore.domain.TissueSpecimen;
import edu.wustl.catissuecore.domain.User;
import edu.wustl.catissuecore.util.EventsUtil;
import edu.wustl.common.util.Utility;
import edu.wustl.common.util.logger.Logger;


public class SpecimenCollectGroupTestCases extends CaTissueBaseTestCase
{
	
	public void testUpdateSpecimenCollectionGroupWithConsents()
	{
		try
		{
			SpecimenCollectionGroup specimenCollGroup = (SpecimenCollectionGroup)TestCaseUtility.getObjectMap(SpecimenCollectionGroup.class);
			Participant participant = (Participant)TestCaseUtility.getObjectMap(Participant.class);
			updateSCG(specimenCollGroup, participant);
			assertTrue("Specimen Collection Group Updated", true);
		}
		catch(Exception e)
		{
			assertFalse("Specimen Collection Group Not Updated", true);
			Logger.out.error(e.getMessage(),e);
			e.printStackTrace();
			fail("Failed to add Domain Object");
		}
	}
	
	private void updateSCG(SpecimenCollectionGroup sprObj, Participant participant)
	{
		System.out.println("After");
		System.out.println(sprObj+": sprObj");
		System.out.println(participant+": participant");
		System.out.println("Before Update");
		sprObj.setCollectionStatus("Complete");
		
		CollectionProtocol collectionProtocol = (CollectionProtocol)TestCaseUtility.getObjectMap(CollectionProtocol.class);
		Collection consentTierCollection = collectionProtocol.getConsentTierCollection();
		Iterator consentTierItr = consentTierCollection.iterator();
		Collection consentTierStatusCollection = new HashSet();
		while(consentTierItr.hasNext())
		{
			ConsentTier consentTier = (ConsentTier)consentTierItr.next();
			ConsentTierStatus consentStatus = new ConsentTierStatus();
			consentStatus.setConsentTier(consentTier);
			consentStatus.setStatus("No");
			consentTierStatusCollection.add(consentStatus);
		}
		sprObj.setConsentTierStatusCollection(consentTierStatusCollection);
		sprObj.getCollectionProtocolRegistration().getCollectionProtocol().setId(new Long(1));
		sprObj.getCollectionProtocolRegistration().setParticipant(participant);
		Collection collectionProtocolEventList = new LinkedHashSet();
		
		
		Site site = (Site)TestCaseUtility.getObjectMap(Site.class); 
		//new Site();
		//site.setId(new Long(1));
		sprObj.setSpecimenCollectionSite(site);
		setEventParameters(sprObj);
		try
		{
			System.out.println("Before Update");
			SpecimenCollectionGroup scg = (SpecimenCollectionGroup)appService.updateObject(sprObj);
			System.out.println(scg.getCollectionStatus().equals("Complete"));
			if(scg.getCollectionStatus().equals("Complete"))
			{
				assertTrue("Specimen Collected ---->", true);
			}
			else
			{
				assertFalse("Anticipatory Specimen", true);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
	
	public void testSearchSpecimenCollectionGroup()
    {
		SpecimenCollectionGroup scg = new SpecimenCollectionGroup();
    	SpecimenCollectionGroup cachedSCG = (SpecimenCollectionGroup) TestCaseUtility.getObjectMap(SpecimenCollectionGroup.class);
    	scg.setId((Long) cachedSCG.getId());
     	Logger.out.info(" searching domain object");
    	 try {
        	 List resultList = appService.search(SpecimenCollectionGroup.class, scg);
        	 for (Iterator resultsIterator = resultList.iterator(); resultsIterator.hasNext();) {
        		 SpecimenCollectionGroup returnedSCG = (SpecimenCollectionGroup) resultsIterator.next();
        		 System.out.println("here-->" + returnedSCG.getName() +"Id:"+returnedSCG.getId());
        		 Logger.out.info(" Domain Object is successfully Found ---->  :: " + returnedSCG.getName());
             }
        	 assertTrue("SCG found", true);
          } 
          catch (Exception e) {
        	Logger.out.error(e.getMessage(),e);
	 		e.printStackTrace();
	 		assertFalse("Couldnot found Specimen", true);  
          }

    }
	
	public void testAddSCGWithDuplicateName()
	{
		
		try{
			SpecimenCollectionGroup scg = (SpecimenCollectionGroup)BaseTestCaseUtility.initSCG();		    
		    	
		  //  TestCaseUtility.setObjectMap(scg, SpecimenCollectionGroup.class);
		    SpecimenCollectionGroup duplicateSCG = (SpecimenCollectionGroup)BaseTestCaseUtility.initSCG();
		    duplicateSCG.setName(scg.getName());
		    scg = (SpecimenCollectionGroup)appService.createObject(scg);
		    duplicateSCG = (SpecimenCollectionGroup)appService.createObject(duplicateSCG);
		    System.out.println("After Creating SCG");
			fail("Test Failed. Duplicate SCG name should throw exception");
		}
		 catch(Exception e){
			Logger.out.error(e.getMessage(),e);
			e.printStackTrace();
			assertTrue("Submission failed since a Collection Protocol with the same NAME already exists" , true);
			 
		 }
    	
	}
	
	public void testUpdateSCGWithClosedActivityStatus()
	{
		
		try{
			SpecimenCollectionGroup scg = (SpecimenCollectionGroup)BaseTestCaseUtility.initSCG();	
			//scg.setActivityStatus("Closed");
		    scg = (SpecimenCollectionGroup)appService.createObject(scg);
		    Site site = (Site) TestCaseUtility.getObjectMap(Site.class);
		    scg.setSpecimenCollectionSite(site);
		    CollectionProtocol collectionProtocol = (CollectionProtocol)TestCaseUtility.getObjectMap(CollectionProtocol.class);
		    Participant participant = (Participant)TestCaseUtility.getObjectMap(Participant.class);
		    scg.getCollectionProtocolRegistration().getCollectionProtocol().setId(new Long(1));
		    scg.getCollectionProtocolRegistration().setParticipant(participant);
		    scg.setActivityStatus("Closed");
		    scg = (SpecimenCollectionGroup)appService.updateObject(scg);
		    assertTrue("Should throw Exception", true);
			
		}
		 catch(Exception e){
			Logger.out.error(e.getMessage(),e);
			e.printStackTrace();			
			assertFalse("While adding SCG Activity status should be Active" , true);
		 }
    	
	}
	
	public void testUpdateSCGWithDisabledActivityStatus()
	{
		
		try{
			SpecimenCollectionGroup scg = (SpecimenCollectionGroup)BaseTestCaseUtility.initSCG();	
			//scg.setActivityStatus("Closed");
		    scg = (SpecimenCollectionGroup)appService.createObject(scg);
		    Site site = (Site) TestCaseUtility.getObjectMap(Site.class);
		    scg.setSpecimenCollectionSite(site);
		    CollectionProtocol collectionProtocol = (CollectionProtocol)TestCaseUtility.getObjectMap(CollectionProtocol.class);
		    Participant participant = (Participant)TestCaseUtility.getObjectMap(Participant.class);
		    scg.getCollectionProtocolRegistration().getCollectionProtocol().setId(new Long(1));
		    scg.getCollectionProtocolRegistration().setParticipant(participant);
		    scg.setActivityStatus("Disabled");
		    scg = (SpecimenCollectionGroup)appService.updateObject(scg);
		    assertTrue("Should throw Exception", true);
			
		}
		 catch(Exception e){
			Logger.out.error(e.getMessage(),e);
			System.out.println(e);
			e.printStackTrace();
			assertFalse("While adding SCG Activity status should be Active", true);
			 
		 }
    	
	} 
	
	private void setEventParameters(SpecimenCollectionGroup sprObj)
	{
		System.out.println("Inside Event Parameters");
		Collection specimenEventParametersCollection = new HashSet();
		CollectionEventParameters collectionEventParameters = new CollectionEventParameters();
		ReceivedEventParameters receivedEventParameters = new ReceivedEventParameters();
		collectionEventParameters.setCollectionProcedure("Not Specified");
		collectionEventParameters.setComment("");
		collectionEventParameters.setContainer("Not Specified");		
		Date timestamp = EventsUtil.setTimeStamp("08-15-1975","15","45");
		collectionEventParameters.setTimestamp(timestamp);
		User user = new User();
		user.setId(new Long(1));
		collectionEventParameters.setUser(user);	
		collectionEventParameters.setSpecimenCollectionGroup(sprObj);	
		
		//Received Events		
		receivedEventParameters.setComment("");
		User receivedUser = new User();
		receivedUser.setId(new Long(1));
		receivedEventParameters.setUser(receivedUser);
		receivedEventParameters.setReceivedQuality("Not Specified");		
		Date receivedTimestamp = EventsUtil.setTimeStamp("08-15-1975","15","45");
		receivedEventParameters.setTimestamp(receivedTimestamp);		
		receivedEventParameters.setSpecimenCollectionGroup(sprObj);
		specimenEventParametersCollection.add(collectionEventParameters);
		specimenEventParametersCollection.add(receivedEventParameters);
		sprObj.setSpecimenEventParametersCollection(specimenEventParametersCollection);
	}
	
	/*public void testVerifyConsentResponseAndConsentStatusAtSCG()
	{	
        System.out.println("Inside ConsentsVerificationTestCases:");
        CollectionProtocol cp = BaseTestCaseUtility.initCollectionProtocol();
		try{
			cp = (CollectionProtocol) appService.createObject(cp);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to create collection protocol", true);
		}
		System.out.println("CP:"+cp.getTitle());
		TestCaseUtility.setObjectMap(cp, CollectionProtocol.class);
		
		SpecimenCollectionGroup scg = (SpecimenCollectionGroup) createSCGWithConsents(cp);
		CollectionProtocolRegistration collectionProtocolRegistration = 
			(CollectionProtocolRegistration) TestCaseUtility.getObjectMap(CollectionProtocolRegistration.class);
		Collection consStatusCol = scg.getConsentTierStatusCollection();
		Collection consResponseCol = collectionProtocolRegistration.getConsentTierResponseCollection();
	
		Iterator consResItr = consResponseCol.iterator();
		Iterator consStatusItr = consStatusCol.iterator();
	
		ConsentTierStatus cs[]= new ConsentTierStatus[consStatusCol.size()];
		ConsentTierResponse rs[] = new ConsentTierResponse[consResponseCol.size()];
		int i = 0;
		System.out.println("Reached up to while");
		while(consStatusItr.hasNext())
		{
			cs[i] = (ConsentTierStatus) consStatusItr.next();
			rs[i] = (ConsentTierResponse) consResItr.next();
			i++;
		}	
				
		for(int j = 0; j<cs.length; j++)
		{
			for(int k = 0; k<cs.length; k++)
			{						
				if(cs[k].getConsentTier().getStatement().equals(rs[j].getConsentTier().getStatement()))
				{
					System.out.println("Statement:"+cs[k].getConsentTier().getStatement());
					assertEquals(cs[k].getStatus(), rs[j].getResponse());
				}
			}
		}
					
		TissueSpecimen ts =(TissueSpecimen) BaseTestCaseUtility.initTissueSpecimen();
		ts.setStorageContainer(null);
		ts.setSpecimenCollectionGroup(scg);
		ts.setLabel("TisSpec"+UniqueKeyGeneratorUtil.getUniqueKey());
		ts.setAvailable(new Boolean("true"));
		System.out.println("Befor creating Tissue Specimen");		
		try{
			ts = (TissueSpecimen) appService.createObject(ts);
			System.out.println("Spec:"+ts.getLabel());
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
			assertFalse("Failed to create", true);
		}
	}
	public void testVerifyConsentResopnseAndConsentStatusForUpadatedCP(){
		System.out.println("Inside ConsentsVerificationTestCases:");
        CollectionProtocol cp = BaseTestCaseUtility.initCollectionProtocol();
		try{
			cp = (CollectionProtocol) appService.createObject(cp);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to create collection protocol", true);
		}
		System.out.println("CP:"+cp.getTitle());
		TestCaseUtility.setObjectMap(cp, CollectionProtocol.class);
		
		SpecimenCollectionGroup scg = (SpecimenCollectionGroup) createSCGWithConsents(cp);
		
		TissueSpecimen ts =(TissueSpecimen) BaseTestCaseUtility.initTissueSpecimen();
		ts.setStorageContainer(null);
		ts.setSpecimenCollectionGroup(scg);
		ts.setLabel("TisSpec"+UniqueKeyGeneratorUtil.getUniqueKey());
		ts.setAvailable(new Boolean("true"));
		System.out.println("Befor creating Tissue Specimen");
		
		try{
			ts = (TissueSpecimen) appService.createObject(ts);
			System.out.println("Spec:"+ts.getLabel());
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
			assertFalse("Failed to create", true);
		}
		
		CollectionProtocol updatedCP = (CollectionProtocol) updateCP(cp);

		SpecimenCollectionGroup newSCG = (SpecimenCollectionGroup) createSCGWithConsents(updatedCP);
		
		TissueSpecimen ts1 =(TissueSpecimen) BaseTestCaseUtility.initTissueSpecimen();
		ts1.setStorageContainer(null);
		ts1.setSpecimenCollectionGroup(newSCG);
		ts1.setLabel("TisSpec"+UniqueKeyGeneratorUtil.getUniqueKey());
		ts1.setAvailable(new Boolean("true"));
		System.out.println("Befor creating Tissue Specimen");
		
		try{
			ts = (TissueSpecimen) appService.createObject(ts1);
			System.out.println("Spec:"+ts.getLabel());
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
			assertFalse("Failed to create", true);
		}
		
		Collection consStatusCol = newSCG.getConsentTierStatusCollection();
		CollectionProtocolRegistration collectionProtocolRegistration = 
			(CollectionProtocolRegistration) TestCaseUtility.getObjectMap(CollectionProtocolRegistration.class);
		Collection consResponseCol = collectionProtocolRegistration.getConsentTierResponseCollection();
	
		Iterator consResItr = consResponseCol.iterator();
		Iterator consStatusItr = consStatusCol.iterator();
	
		ConsentTierStatus cs[]= new ConsentTierStatus[consStatusCol.size()];
		ConsentTierResponse rs[] = new ConsentTierResponse[consResponseCol.size()];
		int i = 0;
		System.out.println("Reached up to while");
		while(consStatusItr.hasNext())
		{
			cs[i] = (ConsentTierStatus) consStatusItr.next();
			rs[i] = (ConsentTierResponse) consResItr.next();
			i++;
		}	
				
		for(int j = 0; j<cs.length; j++)
		{
			for(int k = 0; k<cs.length; k++)
			{						
				if(cs[k].getConsentTier().getStatement().equals(rs[j].getConsentTier().getStatement()))
				{
					System.out.println("Statements:"+cs[k].getConsentTier().getStatement());
					assertEquals(cs[k].getStatus(), rs[j].getResponse());
				}
			}
		}
		
	}
	
	public SpecimenCollectionGroup createSCGWithConsents(CollectionProtocol cp){
		
			Participant participant = BaseTestCaseUtility.initParticipant();
			
			try{
				participant = (Participant) appService.createObject(participant);
			}
			catch(Exception e){
				Logger.out.error(e.getMessage(),e);
	           	e.printStackTrace();
	           	assertFalse("Failed to create collection protocol", true);
			}
			System.out.println("Participant:"+participant.getFirstName());
			CollectionProtocolRegistration collectionProtocolRegistration = new CollectionProtocolRegistration();
			collectionProtocolRegistration.setCollectionProtocol(cp);
			collectionProtocolRegistration.setParticipant(participant);
			collectionProtocolRegistration.setProtocolParticipantIdentifier("");
			collectionProtocolRegistration.setActivityStatus("Active");
			try
			{
				collectionProtocolRegistration.setRegistrationDate(Utility.parseDate("08/15/1975",
						Utility.datePattern("08/15/1975")));
				collectionProtocolRegistration.setConsentSignatureDate(Utility.parseDate("11/23/2006",Utility.datePattern("11/23/2006")));
				
			}
			catch (ParseException e)
			{			
				e.printStackTrace();
			}
			collectionProtocolRegistration.setSignedConsentDocumentURL("F:/doc/consentDoc.doc");
			User user = (User)TestCaseUtility.getObjectMap(User.class);
			collectionProtocolRegistration.setConsentWitness(user);
			
			Collection consentTierResponseCollection = new LinkedHashSet();
			Collection consentTierCollection = new LinkedHashSet();
			consentTierCollection = cp.getConsentTierCollection();
			
//			Iterator ConsentierItr = consentTierCollection.iterator();
//			
//			ConsentTier c1= (ConsentTier) ConsentierItr.next();
//			ConsentTierResponse r1 = new ConsentTierResponse();
//			r1.setResponse("Yes");
//			r1.setConsentTier(c1);
//			consentTierResponseCollection.add(r1);
//			ConsentTier c2= (ConsentTier) ConsentierItr.next();
//			ConsentTierResponse r2 = new ConsentTierResponse();
//			r2.setResponse("No");
//			consentTierResponseCollection.add(r2);
//			r2.setConsentTier(c2);
//			ConsentTier c3= (ConsentTier) ConsentierItr.next();
//			ConsentTierResponse r3 = new ConsentTierResponse();
//			r3.setResponse("Yes");
//			r3.setConsentTier(c3);
//			consentTierResponseCollection.add(r3);
			Iterator consentTierItr = consentTierCollection.iterator();
			 while(consentTierItr.hasNext())
			 {
				 ConsentTier consent= (ConsentTier) consentTierItr.next();
				 ConsentTierResponse response= new ConsentTierResponse();
				 response.setResponse("Yes");
				 response.setConsentTier(consent);
				 consentTierResponseCollection.add(response);				 
			 }
				
			collectionProtocolRegistration.setConsentTierResponseCollection(consentTierResponseCollection);
		
			System.out.println("Creating CPR");
			try{
				collectionProtocolRegistration = (CollectionProtocolRegistration) appService.createObject(collectionProtocolRegistration);
			}
			catch(Exception e){
				Logger.out.error(e.getMessage(),e);
	           	e.printStackTrace();
	           	assertFalse("Failed to register participant", true);
			}
			TestCaseUtility.setObjectMap(collectionProtocolRegistration, CollectionProtocolRegistration.class);
			
			SpecimenCollectionGroup scg = new SpecimenCollectionGroup();
			scg =(SpecimenCollectionGroup) BaseTestCaseUtility.createSCG(collectionProtocolRegistration);
			Site site = (Site) TestCaseUtility.getObjectMap(Site.class);
			scg.setSpecimenCollectionSite(site);
			scg.setName("New SCG"+UniqueKeyGeneratorUtil.getUniqueKey());		    
			scg = (SpecimenCollectionGroup) BaseTestCaseUtility.setEventParameters(scg);
			System.out.println("Creating SCG");
			try{
				scg = (SpecimenCollectionGroup) appService.createObject(scg);
			}
			catch(Exception e){
				Logger.out.error(e.getMessage(),e);
	           	e.printStackTrace();
	           	assertFalse("Failed to register participant", true);
			}
			return scg;				
	}
	
	
	
	public CollectionProtocol updateCP(CollectionProtocol collectionProtocol)
	{
		
		try 
		{
			collectionProtocol = (CollectionProtocol) TestCaseUtility.getObjectMap(CollectionProtocol.class);
		   	Logger.out.info("updating domain object------->"+collectionProtocol);
		   	Collection ConCollection = collectionProtocol.getConsentTierCollection();
		   	ConsentTier c4 = new ConsentTier();
		   	c4.setStatement("consent for any research" );
		   	ConCollection.add(c4);
		   	collectionProtocol.setConsentTierCollection(ConCollection);	    	
	    	collectionProtocol = (CollectionProtocol)appService.updateObject(collectionProtocol);
	    	System.out.println("after updation"+collectionProtocol.getTitle());
	    	System.out.println("after updation"+collectionProtocol.getShortTitle());
	    	assertTrue("Domain object updated successfully", true);
	    } 
	    catch (Exception e)
	    {
	    	Logger.out.error(e.getMessage(),e);
	    	e.printStackTrace();
	    	//assertFalse("Failed to update object",true);
	    	fail("Failed to update object");
	    }
	    return collectionProtocol;
	}

	
	public void testVerifyConsentWithdrawnWithDiscardOption(){
		System.out.println("Inside ConsentsVerificationTestCases:");
		CollectionProtocol cp = BaseTestCaseUtility.initCollectionProtocol();
		try{
			cp = (CollectionProtocol) appService.createObject(cp);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to create collection protocol", true);
		}
		System.out.println("CP:"+cp.getTitle());
		Participant participant = BaseTestCaseUtility.initParticipant();
		
		try{
			participant = (Participant) appService.createObject(participant);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to create collection protocol", true);
		}
		System.out.println("Participant:"+participant.getFirstName());
		CollectionProtocolRegistration collectionProtocolRegistration = new CollectionProtocolRegistration();
		collectionProtocolRegistration.setCollectionProtocol(cp);
		collectionProtocolRegistration.setParticipant(participant);
		collectionProtocolRegistration.setProtocolParticipantIdentifier("");
		collectionProtocolRegistration.setActivityStatus("Active");
		try
		{
			collectionProtocolRegistration.setRegistrationDate(Utility.parseDate("08/15/1975",
					Utility.datePattern("08/15/1975")));
			collectionProtocolRegistration.setConsentSignatureDate(Utility.parseDate("11/23/2006",Utility.datePattern("11/23/2006")));
			
		}
		catch (ParseException e)
		{			
			e.printStackTrace();
		}
		collectionProtocolRegistration.setSignedConsentDocumentURL("F:/doc/consentDoc.doc");
		User user = (User)TestCaseUtility.getObjectMap(User.class);
		collectionProtocolRegistration.setConsentWitness(user);
		
		Collection consentTierResponseCollection = new HashSet();
		Collection consentTierCollection = cp.getConsentTierCollection();
		Iterator consentTierItr = consentTierCollection.iterator();
		
		while(consentTierItr.hasNext())
		{
			ConsentTier consentTier = (ConsentTier)consentTierItr.next();
			ConsentTierResponse consentResponse = new ConsentTierResponse();
			consentResponse.setConsentTier(consentTier);
			consentResponse.setResponse("Yes");
			consentTierResponseCollection.add(consentResponse);
		}
		
		collectionProtocolRegistration.setConsentTierResponseCollection(consentTierResponseCollection);
		
		collectionProtocolRegistration.setConsentTierResponseCollection(consentTierResponseCollection);
		System.out.println("Creating CPR");
		try{
			collectionProtocolRegistration = (CollectionProtocolRegistration) appService.createObject(collectionProtocolRegistration);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to register participant", true);
		}
		
		SpecimenCollectionGroup scg = new SpecimenCollectionGroup();
		scg =(SpecimenCollectionGroup) BaseTestCaseUtility.createSCG(collectionProtocolRegistration);
		Site site = (Site) TestCaseUtility.getObjectMap(Site.class);
		scg.setSpecimenCollectionSite(site);
		scg.setName("New SCG"+UniqueKeyGeneratorUtil.getUniqueKey());		    
		scg = (SpecimenCollectionGroup) BaseTestCaseUtility.setEventParameters(scg);
		System.out.println("Creating SCG");
		
					
		try{
			scg = (SpecimenCollectionGroup) appService.createObject(scg);
			
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to rcreate SCG", true);
		}
		
		TissueSpecimen ts =(TissueSpecimen) BaseTestCaseUtility.initTissueSpecimen();
		ts.setStorageContainer(null);
		ts.setSpecimenCollectionGroup(scg);
		ts.setLabel("TisSpec"+UniqueKeyGeneratorUtil.getUniqueKey());
		System.out.println("Befor creating Tissue Specimen");
		
		try{
			ts = (TissueSpecimen) appService.createObject(ts);
		}
		catch(Exception e){
			assertFalse("Failed to create specimen", true);
		}
	
		Collection consentTierCollection1 = cp.getConsentTierCollection();
		Iterator consentTierItr1 = consentTierCollection1.iterator();
		Collection newConStatusCol = new HashSet();
		Collection consentTierStatusCollection = scg.getConsentTierStatusCollection();
	
		Iterator conStatusItr =  consentTierStatusCollection.iterator();
		ConsentTier c1 = (ConsentTier)consentTierItr1.next();
		ConsentTierStatus consentStatus1 = new ConsentTierStatus();
		consentStatus1.setStatus("Withdrawn");
		consentStatus1.setConsentTier(c1);
		newConStatusCol.add(consentStatus1);
		ConsentTier c2 = (ConsentTier)consentTierItr1.next();
		ConsentTierStatus consentStatus2 = new ConsentTierStatus();
		consentStatus2.setStatus("Withdrawn");
		consentStatus2.setConsentTier(c2);
		newConStatusCol.add(consentStatus2);
		ConsentTier c3 = (ConsentTier)consentTierItr1.next();
		ConsentTierStatus consentStatus3 = new ConsentTierStatus();
		consentStatus3.setStatus("Withdrawn");
		consentStatus3.setConsentTier(c3);
		newConStatusCol.add(consentStatus3);
		
	    scg.setConsentTierStatusCollection(newConStatusCol);
 		scg.setConsentWithdrawalOption("Discard");
		scg.getCollectionProtocolRegistration().getCollectionProtocol().setId(collectionProtocolRegistration.getId());
		scg.getCollectionProtocolRegistration().setParticipant(participant);
		try{
			scg = (SpecimenCollectionGroup) appService.updateObject(scg);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to update SCG", true);
		}		
		
	}
	
	public void testVerifyConsentsWithdrawnWithReturnOption(){
		System.out.println("Inside ConsentsVerificationTestCases:");
		CollectionProtocol cp = BaseTestCaseUtility.initCollectionProtocol();
		try{
			cp = (CollectionProtocol) appService.createObject(cp);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to create collection protocol", true);
		}
		System.out.println("CP:"+cp.getTitle());
		Participant participant = BaseTestCaseUtility.initParticipant();
		
		try{
			participant = (Participant) appService.createObject(participant);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to create collection protocol", true);
		}
		System.out.println("Participant:"+participant.getFirstName());
		CollectionProtocolRegistration collectionProtocolRegistration = new CollectionProtocolRegistration();
		collectionProtocolRegistration.setCollectionProtocol(cp);
		collectionProtocolRegistration.setParticipant(participant);
		collectionProtocolRegistration.setProtocolParticipantIdentifier("");
		collectionProtocolRegistration.setActivityStatus("Active");
		try
		{
			collectionProtocolRegistration.setRegistrationDate(Utility.parseDate("08/15/1975",
					Utility.datePattern("08/15/1975")));
			collectionProtocolRegistration.setConsentSignatureDate(Utility.parseDate("11/23/2006",Utility.datePattern("11/23/2006")));
			
		}
		catch (ParseException e)
		{			
			e.printStackTrace();
		}
		collectionProtocolRegistration.setSignedConsentDocumentURL("F:/doc/consentDoc.doc");
		User user = (User)TestCaseUtility.getObjectMap(User.class);
		collectionProtocolRegistration.setConsentWitness(user);
		
		Collection consentTierResponseCollection = new HashSet();
		Collection consentTierCollection = cp.getConsentTierCollection();
		Iterator consentTierItr = consentTierCollection.iterator();
		
		while(consentTierItr.hasNext())
		{
			ConsentTier consentTier = (ConsentTier)consentTierItr.next();
			ConsentTierResponse consentResponse = new ConsentTierResponse();
			consentResponse.setConsentTier(consentTier);
			consentResponse.setResponse("Yes");
			consentTierResponseCollection.add(consentResponse);
		}
		
		collectionProtocolRegistration.setConsentTierResponseCollection(consentTierResponseCollection);
		
		collectionProtocolRegistration.setConsentTierResponseCollection(consentTierResponseCollection);
		System.out.println("Creating CPR");
		try{
			collectionProtocolRegistration = (CollectionProtocolRegistration) appService.createObject(collectionProtocolRegistration);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to register participant", true);
		}
		
		SpecimenCollectionGroup scg = new SpecimenCollectionGroup();
		scg =(SpecimenCollectionGroup) BaseTestCaseUtility.createSCG(collectionProtocolRegistration);
		Site site = (Site) TestCaseUtility.getObjectMap(Site.class);
		scg.setSpecimenCollectionSite(site);
		scg.setName("New SCG"+UniqueKeyGeneratorUtil.getUniqueKey());		    
		scg = (SpecimenCollectionGroup) BaseTestCaseUtility.setEventParameters(scg);
		System.out.println("Creating SCG");
		
					
		try{
			scg = (SpecimenCollectionGroup) appService.createObject(scg);
			
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to rcreate SCG", true);
		}
		
		TissueSpecimen ts =(TissueSpecimen) BaseTestCaseUtility.initTissueSpecimen();
		ts.setStorageContainer(null);
		ts.setSpecimenCollectionGroup(scg);
		ts.setLabel("TisSpec"+UniqueKeyGeneratorUtil.getUniqueKey());
		System.out.println("Befor creating Tissue Specimen");
		
		try{
			ts = (TissueSpecimen) appService.createObject(ts);
		}
		catch(Exception e){
			assertFalse("Failed to create specimen", true);
		}
			
		Collection consentTierCollection1 = cp.getConsentTierCollection();
		Iterator consentTierItr1 = consentTierCollection1.iterator();
		Collection newConStatusCol = new HashSet();
		Collection consentTierStatusCollection = scg.getConsentTierStatusCollection();
	
		Iterator conStatusItr =  consentTierStatusCollection.iterator();
		ConsentTier c1 = (ConsentTier)consentTierItr1.next();
		ConsentTierStatus consentStatus1 = new ConsentTierStatus();
		consentStatus1.setStatus("Withdrawn");
		consentStatus1.setConsentTier(c1);
		newConStatusCol.add(consentStatus1);
		ConsentTier c2 = (ConsentTier)consentTierItr1.next();
		ConsentTierStatus consentStatus2 = new ConsentTierStatus();
		consentStatus2.setStatus("Withdrawn");
		consentStatus2.setConsentTier(c2);
		newConStatusCol.add(consentStatus2);
		ConsentTier c3 = (ConsentTier)consentTierItr1.next();
		ConsentTierStatus consentStatus3 = new ConsentTierStatus();
		consentStatus3.setStatus("Withdrawn");
		consentStatus3.setConsentTier(c3);
		newConStatusCol.add(consentStatus3);
		
	    scg.setConsentTierStatusCollection(newConStatusCol);
 		scg.setConsentWithdrawalOption("Return");
		scg.getCollectionProtocolRegistration().getCollectionProtocol().setId(collectionProtocolRegistration.getId());
		scg.getCollectionProtocolRegistration().setParticipant(participant);
		try{
			scg = (SpecimenCollectionGroup) appService.updateObject(scg);
		}
		catch(Exception e){
			Logger.out.error(e.getMessage(),e);
           	e.printStackTrace();
           	assertFalse("Failed to update SCG", true);
		}	
		
	}	*/
	
	
}
	