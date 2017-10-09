package com.krishagni.catissueplus.core.administrative.domain;

import java.util.List;

import org.hibernate.envers.Audited;

import com.krishagni.catissueplus.core.administrative.domain.factory.StorageContainerErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.BaseEntity;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.biospecimen.repository.SpecimenListCriteria;
import com.krishagni.catissueplus.core.common.OpenSpecimenAppCtxProvider;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;

@Audited
public class ShipmentContainer extends BaseEntity {
	private Shipment shipment;

	private StorageContainer container;

	private Shipment.ItemReceiveQuality receivedQuality;

	public Shipment getShipment() {
		return shipment;
	}

	public void setShipment(Shipment shipment) {
		this.shipment = shipment;
	}

	public StorageContainer getContainer() {
		return container;
	}

	public void setContainer(StorageContainer container) {
		this.container = container;
	}

	public Shipment.ItemReceiveQuality getReceivedQuality() {
		return receivedQuality;
	}

	public void setReceivedQuality(Shipment.ItemReceiveQuality receivedQuality) {
		this.receivedQuality = receivedQuality;
	}

	public void ship() {
		getContainer().moveTo(getShipment().getReceivingSite().getContainer());
		shipSpecimens();
	}

	public void receive(ShipmentContainer other) {
		setReceivedQuality(other.getReceivedQuality());
		updatePosition(other);
	}

	private void shipSpecimens() {
		DaoFactory daoFactory = OpenSpecimenAppCtxProvider.getBean("biospecimenDaoFactory");

		int startAt = 0, maxSpmns = 100;
		SpecimenListCriteria crit = new SpecimenListCriteria()
			.ancestorContainerId(getContainer().getId())
			.maxResults(maxSpmns);

		boolean endOfSpmns = false;
		while (!endOfSpmns) {
			List<Specimen> spmns = daoFactory.getSpecimenDao().getSpecimens(crit.startAt(startAt));
			for (Specimen spmn : spmns) {
				ShipmentSpecimen shipmentSpmn = getShipment().addShipmentSpecimen(spmn);
				shipmentSpmn.setShipmentContainer(this);
				shipmentSpmn.ship();
			}

			startAt += spmns.size();
			endOfSpmns = (spmns.size() < maxSpmns);
			spmns.clear();
		}
	}

	private void updatePosition(ShipmentContainer other) {
		StorageContainer parentContainer = null;
		StorageContainerPosition position = other.getContainer().getPosition();
		if (position != null) {
			parentContainer = position.getContainer();
			if (parentContainer.isPositionOccupied(position.getPosOne(), position.getPosTwo())) {
				throw OpenSpecimenException.userError(StorageContainerErrorCode.NO_FREE_SPACE, parentContainer.getName());
			}
		}

		getContainer().moveTo(other.getContainer().getSite(), parentContainer, position);
	}
}
