package no.systema.z.main.maintenance.controller.kund;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import lombok.Data;

/**
 * Object for transfering data between jsp in L1-Kundregister
 * 
 * @author oscardelatorre
 * @date Okt 2019
 *
 */
@Data
public class KundeL1SessionParams implements Serializable {
	
	private String kundnr;
	private String knavn;
	private String firma;
	private String sonavn;
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
