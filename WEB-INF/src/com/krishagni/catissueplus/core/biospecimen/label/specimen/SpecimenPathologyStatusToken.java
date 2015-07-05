package com.krishagni.catissueplus.core.biospecimen.label.specimen;

import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;

public class SpecimenPathologyStatusToken extends AbstractSpecimenLabelToken {
	
	public SpecimenPathologyStatusToken() {
		this.name = "SP_PATH_STATUS";
	}
	
	@Override
	public String getLabel(Specimen specimen) {
		if (specimen.getPathologicalStatus().equals(NON_MALIGNANT) || specimen.getPathologicalStatus().equals(NORMAL)) {
			return "N";
		} else {
			return "T";
		}
	}
	
	public static final String NON_MALIGNANT = "Non-Malignant";
	public static final String NORMAL = "Normal";
}
