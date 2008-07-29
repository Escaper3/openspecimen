package edu.wustl.catissuecore.querysuite.metadata;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddSpecimenRequirementMetaData
{	
	private Connection connection = null;
	private static Statement stmt = null;
	private static HashMap<String, List<String>> entityNameAttributeNameMap = new HashMap<String, List<String>>();
	private static HashMap<String, String> attributeColumnNameMap = new HashMap<String, String>();
	private static HashMap<String, String> attributeDatatypeMap = new HashMap<String, String>();
	private static HashMap<String, String> attributePrimarkeyMap = new HashMap<String, String>();
	private static List<String> entityList = new ArrayList<String>();
	
	public void addSpecimenRequirementMetaData() throws SQLException, IOException
	{
		populateEntityList();
		populateEntityAttributeMap();
		populateAttributeColumnNameMap();
		populateAttributeDatatypeMap();
		populateAttributePrimaryKeyMap();
		
		Statement stmt = connection.createStatement();
		AddEntity addEntity = new AddEntity(connection);
		addEntity.addEntity(entityList, "CATISSUE_CP_REQ_SPECIMEN", "edu.wustl.catissuecore.domain.AbstractSpecimen", 3);
		
		AddAttribute addAttribute = new AddAttribute(connection,entityNameAttributeNameMap,attributeColumnNameMap,attributeDatatypeMap,attributePrimarkeyMap,entityList);
		addAttribute.addAttribute();
		
		AddAssociations addAssociations = new AddAssociations(connection);
		String entityName = "edu.wustl.catissuecore.domain.CollectionProtocolEvent";
		String targetEntityName = "edu.wustl.catissuecore.domain.SpecimenRequirement";
		addAssociations.addAssociation(entityName,targetEntityName,"collectionProtocolEvent_specimenRequirement","CONTAINTMENT","specimenRequirementCollection",true,"CollectionProtocolEvent","COLLECTION_PROTOCOL_EVENT_ID",2,1);
		addAssociations.addAssociation(targetEntityName,entityName,"specimenRequirement_collectionProtocolEvent","ASSOCIATION","collectionProtocolEvent",false,"","COLLECTION_PROTOCOL_EVENT_ID",2,1);
		
		entityName = "edu.wustl.catissuecore.domain.SpecimenRequirement";
	    targetEntityName = "edu.wustl.catissuecore.domain.Specimen";
	    
	    addAssociations.addAssociation(entityName,targetEntityName,"specimenRequirement_specimen","ASSOCIATION","specimenCollection",true,"specimenRequirement","REQ_SPECIMEN_ID",2,1);
	    addAssociations.addAssociation(targetEntityName,entityName,"specimen_specimenRequirement","ASSOCIATION","specimenRequirement",false,"","REQ_SPECIMEN_ID",2,1);
	}
		
		private void populateEntityAttributeMap() 
		{
			List<String> attributes = new ArrayList<String>();
			attributes.add("id");
			attributes.add("storageType");
			entityNameAttributeNameMap.put("edu.wustl.catissuecore.domain.SpecimenRequirement",attributes);		
		}
		
		private void populateAttributeColumnNameMap()
		{
			attributeColumnNameMap.put("id", "IDENTIFIER");
			attributeColumnNameMap.put("storageType", "STORAGE_TYPE");
		}
		
		private void populateAttributeDatatypeMap()
		{
			attributeDatatypeMap.put("id", "long");
			attributeDatatypeMap.put("storageType", "string");
		}
		private void populateAttributePrimaryKeyMap() 
		{
			attributePrimarkeyMap.put("id", "1");
			attributePrimarkeyMap.put("storageType", "0");
		}
		private void populateEntityList()
		{
			entityList.add("edu.wustl.catissuecore.domain.SpecimenRequirement");
		}

		public AddSpecimenRequirementMetaData(Connection connection) throws SQLException
		{
			super();
			this.connection = connection;
		}
}
