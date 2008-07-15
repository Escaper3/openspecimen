package edu.wustl.catissuecore.namegenerator;

import edu.wustl.catissuecore.domain.Specimen;

/**
 * This is the Specimen Label Generator for Indiana University.
 * 
 * @author falguni_sachde
 */
public class SpecimenLabelGeneratorForIU extends DefaultSpecimenLabelGenerator
{
	
	/**
	 * 
	 */
	public SpecimenLabelGeneratorForIU()
	{
		super();
		
		
	}
	/**
	 * This function is overridden as per IU requirement. 
	 */
	@Override
	synchronized  void setNextAvailableAliquotSpecimenlabel(Specimen parentObject,Specimen specimenObject) {
		

		String parentSpecimenLabel = (String) parentObject.getLabel();
		long aliquotChildCount = 0;
		if(labelCountTreeMap.containsKey(parentObject))
		{
			 aliquotChildCount= Long.parseLong(labelCountTreeMap.get(parentObject).toString());	
		}
		else
		{
		 
			aliquotChildCount = parentObject.getChildSpecimenCollection().size();	
			
		}
		
		StringBuffer buffy = null;
		StringBuffer prefixBuffy = new StringBuffer();
		String sp = null;
		
		if (parentSpecimenLabel != null) 
		{
			parentSpecimenLabel = (String) parentSpecimenLabel;
			int dash = parentSpecimenLabel.lastIndexOf("-");
			prefixBuffy.append(parentSpecimenLabel.substring(0, dash + 1));
			sp = parentSpecimenLabel.substring(dash + 1, dash + 2);
		
			buffy = new StringBuffer();
			buffy.append(prefixBuffy);
			buffy.append(++aliquotChildCount);
			buffy.append(determineSerumPlasma(sp, aliquotChildCount));
			specimenObject.setLabel(buffy.toString());
			labelCountTreeMap.put(parentObject,aliquotChildCount);	
			labelCountTreeMap.put(specimenObject,0);	
		}
		
	
	}
	
	/**
	 * @param type
	 * @param num
	 * @return Specific method of IU
	 */
	private String determineSerumPlasma(String type, long num) 
	{
		String sp = null;
		if (type.equals("S"))
			sp = type;
		else if (num > 5)
			sp = "PNP";
		else
			sp = "P";
		return sp;
	}

}