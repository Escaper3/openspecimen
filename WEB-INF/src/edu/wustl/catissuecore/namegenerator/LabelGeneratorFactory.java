package edu.wustl.catissuecore.namegenerator;

import java.util.HashMap;

/**
 * This is the factory Class to retrieve singleton instance of LabelGenerator
 * 
 * @author Falguni_Sachde
 */
public class LabelGeneratorFactory 
{
	
	/**
	 * Singleton instance of SpecimenLabelGenerator
     */
	 private static HashMap labelgeneratorMap = new HashMap() ;
	
	/**
	 * Get singleton instance of SpecimenLabelGenerator. The class name of an instance is picked up from properties file
	 * @param generatorType Property key name for specific Object's  Label generator class (eg.specimenLabelGeneratorClass)
	 * @return LabelGenerator
	 * @throws NameGeneratorException
	 */
	public static LabelGenerator getInstance(String generatorType) throws NameGeneratorException
	{
		try
		{

			if(labelgeneratorMap.get(generatorType) == null)
			{
				
				String className = PropertyHandler.getValue(generatorType);
				if(className!=null)
				{
					labelgeneratorMap.put(generatorType,Class.forName(className).newInstance());
				}
				else
				{	
					return null;
				}
			}
			
			return (LabelGenerator)labelgeneratorMap.get(generatorType);
		}
		catch(IllegalAccessException e)
		{
			throw new NameGeneratorException("Could not create LabelGenerator instance: " +e.getMessage());			
		}
		catch(InstantiationException e)
		{
			throw new NameGeneratorException("Could not create LabelGenerator instance: " +e.getMessage());			
		}
		catch(ClassNotFoundException e)
		{
			throw new NameGeneratorException("Could not create LabelGenerator instance: " +e.getMessage());			
		}catch(Exception ex)
		{
			throw new NameGeneratorException(ex.getMessage(),ex);
		}
	}
}
