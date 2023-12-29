package it.polito.med;

public class Appointment {
	// patient's infos
	private String ssn;
	private String name;
	private String surname;
	// doc's id
	private String docID;
	// more infos about the apointment
	private String date;
	private String startTime;
	private String slotTime;
	private String appID; // appointment id
	
	
	
	public Appointment(String ssn, String name, String surname, String docID, String date, String startTime,
			String slotTime) {
		super();
		this.ssn = ssn;
		this.name = name;
		this.surname = surname;
		this.docID = docID;
		this.date = date;
		this.startTime = startTime;
		this.slotTime = slotTime;
		
	}
	// getters and setters
	public String getSsn() {
		return ssn;
	}
	public void setSsn(String ssn) {
		this.ssn = ssn;
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
	public String getDocID() {
		return docID;
	}
	public void setDocID(String docID) {
		this.docID = docID;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getSlotTime() {
		return slotTime;
	}
	public void setSlotTime(String slotTime) {
		this.slotTime = slotTime;
	}
	public String getAppID() {
		return appID;
	}
	public void setAppID(String appID) {
		this.appID = appID;
	}
	
	
	
}
