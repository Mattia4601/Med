package it.polito.med;

import java.util.Set;
import java.util.TreeMap;

public class Doctor {
	
	private String id;
	private String name;
	private String surname;
	private String speciality;
	// collection for doctor slots, key date, value set of strings
	private TreeMap<String,Set<String>> slots = new TreeMap<>();
	
	

	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getSpeciality() {
		return speciality;
	}
	public void setSpeciality(String speciality) {
		this.speciality = speciality;
	}
	//scrrds
	// constructorfdgd
	public Doctor(String id, String name, String surname, String speciality) {
		super();
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.speciality = speciality;
		
	}
	
	
}

