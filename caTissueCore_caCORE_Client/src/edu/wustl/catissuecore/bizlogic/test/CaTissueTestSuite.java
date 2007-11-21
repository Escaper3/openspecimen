package edu.wustl.catissuecore.bizlogic.test;

import junit.framework.TestSuite;

public class CaTissueTestSuite
{
	
	public static void main(String[] args)
	{
		junit.swingui.TestRunner.run(CaTissueTestSuite.class);
	}
	
	public static junit.framework.Test suite() 
	{
		TestSuite suite = new TestSuite("Test for edu.wustl.catissuecore.bizlogic.test");
		//$JUnit-BEGIN$
		suite.addTestSuite(InstitutionTestCases.class);
		suite.addTestSuite(DepartmentTestCases.class);
		suite.addTestSuite(CancerResearchGrpTestCases.class);
		suite.addTestSuite(UserTestCases.class);
		suite.addTestSuite(SiteTestCases.class);
		suite.addTestSuite(BioHazardTestCases.class);
		suite.addTestSuite(CollectionProtocolTestCases.class);
		suite.addTestSuite(ParticipantTestCases.class);
		suite.addTestSuite(SpecimenCollectGroupTestCases.class);
		suite.addTestSuite(SpecimenTestCases.class);
		suite.addTestSuite(StorageTypeTestCases.class);
		suite.addTestSuite(StorageContainerTestCases.class);
		suite.addTestSuite(DistributionProtocolTestCases.class);
		suite.addTestSuite(IdentifiedSurgicalPathologyReportTestCases.class);
		suite.addTestSuite(DeIdentifiedSurgicalPathologyReportTestCases.class);
		//$JUnit-END$
		return suite;
	}
	
}
