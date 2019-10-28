/**
 * 
 */
package no.systema.z.main.maintenance.service;

import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfContainer;

/**
 * @author oscardelatorre
 * @date Oct 2019
 * 
 */
public interface MaintMainCustomerL1Service {
	public JsonMaintMainKundfContainer getContainer(String utfPayload);
	
}
