/**
 * 
 */
package no.systema.z.main.maintenance.service;

import java.io.PrintWriter;
import java.io.StringWriter;

import no.systema.z.main.maintenance.mapper.jsonjackson.JsonCustomerL1Mapper;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfContainer;



/**
 * 
 * @author oscardelatorre
 * @date Oct 2019
 * 
 * 
 */
public class MaintMainCustomerL1ServiceImpl implements MaintMainCustomerL1Service {

	/**
	 * 
	 */
	public JsonMaintMainKundfContainer getContainer(String utfPayload) {
		JsonMaintMainKundfContainer container = null;
		try{
			JsonCustomerL1Mapper mapper = new JsonCustomerL1Mapper();
			container = mapper.getContainer(utfPayload);
		}catch(Exception e){
			e.printStackTrace();
		}
		return container;
	}
	
	
}
