package it.polito.med;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MedManager {

	// specialities collection
	private TreeSet<String> specialitiesColl = new TreeSet<>();
	// doctors collection
	private TreeMap<String,Doctor> doctorsColl = new TreeMap<>();
	// appointments collection
	private TreeMap<String,Appointment> appColl = new TreeMap<>();
	// accepted appointments for each doctor coll --> map key=docID value=Set of appointments accepted
	private TreeMap<String,TreeSet<Appointment>> acceptedAppPerDocColl = new TreeMap<>();
	private String currentDate;
	/**
	 * add a set of medical specialities to the list of specialities
	 * offered by the med centre.
	 * Method can be invoked multiple times.
	 * Possible duplicates are ignored.
	 * 
	 * @param specialities the specialities
	 */
	public void addSpecialities(String... specialities) {
		
		// for each specialty add it to the collection
		for (String spec : specialities) {
			specialitiesColl.add(spec);
		}
		
	}

	/**
	 * retrieves the list of specialities offered in the med centre
	 * 
	 * @return list of specialities
	 */
	public Collection<String> getSpecialities() {
		return this.specialitiesColl;
	}
	
	
	/**
	 * adds a new doctor with the list of their specialities
	 * 
	 * @param id		unique id of doctor
	 * @param name		name of doctor
	 * @param surname	surname of doctor
	 * @param speciality speciality of the doctor
	 * @throws MedException in case of duplicate id or non-existing speciality
	 */
	public void addDoctor(String id, String name, String surname, String speciality) throws MedException {
		
		// check if the id has already been entered in the doctors coll
		if (this.doctorsColl.containsKey(id)) {
			throw new MedException();
		}
		
		// check if the speciality exists
		if (!this.specialitiesColl.contains(speciality)) {
			throw new MedException();
		}
		
		// create a new doctor
		Doctor d = new Doctor(id,name,surname,speciality);
		
		this.doctorsColl.put(id, d);
		
	}

	/**
	 * retrieves the list of doctors with the given speciality
	 * 
	 * @param speciality required speciality
	 * @return the list of doctor ids
	 */
	public Collection<String> getSpecialists(String speciality) {
		
		Collection<String> res = this.doctorsColl.values().stream()
				.filter(d->d.getSpeciality().equals(speciality))
				.map(d->d.getId())
				.collect(Collectors.toList());
		return res;
	}

	/**
	 * retrieves the name of the doctor with the given code
	 * 
	 * @param code code id of the doctor 
	 * @return the name
	 */
	public String getDocName(String code) {
		
		Doctor d = this.doctorsColl.get(code);
		
		return d.getName();
	}

	/**
	 * retrieves the surname of the doctor with the given code
	 * 
	 * @param code code id of the doctor 
	 * @return the surname
	 */
	public String getDocSurname(String code) {
		
		Doctor d = this.doctorsColl.get(code);
		
		return d.getSurname();
	}

	/**
	 * Define a schedule for a doctor on a given day.
	 * Slots are created between start and end hours with a 
	 * duration expressed in minutes.
	 * 
	 * @param code	doctor id code
	 * @param date	date of schedule
	 * @param start	start time
	 * @param end	end time
	 * @param duration duration in minutes
	 * @return the number of slots defined
	 */
	public int addDailySchedule(String code, String date, String start, String end, int duration) {
		
		// get the doc obj
		Doctor doc = this.doctorsColl.get(code);
		
		// get the start and end time hours and minute
		String[] startFields = start.split(":");
		int hStart = Integer.parseInt(startFields[0]),
				mStart = Integer.parseInt(startFields[1]);
		
		String[] endFields = end.split(":");
		int hEnd = Integer.parseInt(endFields[0]),
				mEnd = Integer.parseInt(endFields[1]);
		
		// set for the slots in string format
		TreeSet<String> slots_set = new TreeSet<>();
		int countSlot=0;
		while (true) {
			
			// if the current start time reached the end time stop the while
			if (hStart == hEnd && mEnd == mStart)
				break;
			
			// get the slot 
			String slotString = toStringSlot(hStart,mStart,duration);
			// add into the set
			slots_set.add(slotString);
			countSlot++;
			// update the new start time for the next slot
			String[] fields = slotString.split("-");
			String[] fields2 = fields[1].split(":");
			hStart = Integer.parseInt(fields2[0]); mStart = Integer.parseInt(fields2[1]);
		}
		
		// add to doctor slots collection
		doc.addSlot(date, slots_set);
		// update the number of total slots for a doctor
		doc.updTotSlots(countSlot);
		return countSlot;
	}
	
	// this function receive two integers representing the hour and minutes of the start time 
	// another integer which represents the duration of the slot
	// returns the slot in the format "hh:mm-hh:mm"
	public String toStringSlot(int hs, int ms, int duration) {
		int me = ms + duration;
		int he = hs;
		
		if (me == 60) {
			me=0;
			he++;
		}
		else if (me>60) {
			me = me - 60;
			he++;
		}
		
		String output = String.format("%02d:%02d-%02d:%02d",
				hs,ms,he,me);
		
		return output;
	}
	/**
	 * retrieves the available slots available on a given date for a speciality.
	 * The returned map contains an entry for each doctor that has slots scheduled on the date.
	 * The map contains a list of slots described as strings with the format "hh:mm-hh:mm",
	 * e.g. "14:00-14:30" describes a slot starting at 14:00 and lasting 30 minutes.
	 * 
	 * @param date			date to look for
	 * @param speciality	required speciality
	 * @return a map doc-id -> list of slots in the schedule
	 */
	public Map<String, List<String>> findSlots(String date, String speciality) {
		
		return this.doctorsColl.values().stream()
				.filter(d->d.getSpeciality().equals(speciality))
				.filter(d->d.hasDate(date))
				.collect(Collectors.toMap(Doctor::getId, 
						d->new ArrayList<>(d.getSlotsPerDate(date))));
	}

	/**
	 * Define an appointment for a patient in an existing slot of a doctor's schedule
	 * 
	 * @param ssn		ssn of the patient
	 * @param name		name of the patient
	 * @param surname	surname of the patient
	 * @param code		code id of the doctor
	 * @param date		date of the appointment
	 * @param slot		slot to be booked
	 * @return a unique id for the appointment
	 * @throws MedException	in case of invalid code, date or slot
	 */
	public String setAppointment(String ssn, String name, String surname, String code, String date, String slot) throws MedException {
		
		// check the doc's code
		if (!this.doctorsColl.containsKey(code))
			throw new MedException();
		// check the date
		Doctor doc = this.doctorsColl.get(code);
		if (!doc.hasDate(date))
			throw new MedException();
		// check the slot
		Set<String> slotForDate = doc.getSlotsPerDate(date);
		if (!slotForDate.contains(slot))
			throw new MedException();
		
		// get the start time
		// creo il pattern identificando 2 gruppi di cattura, \\d{2} significa due cifre decimali
		Pattern pattern = Pattern.compile("(\\d{2}:\\d{2})-(\\d{2}:\\d{2})");
		// distingui questi 2 gruppi con un matcher su slot
		Matcher matcher = pattern.matcher(slot); 
		String startTime;
		if (matcher.find()) {
			// estraggo lo start time, i gruppi partono da 1
			startTime = matcher.group(1);	
		}
		else {
			System.out.println("Matcher failed!");
			return null;
		}

		// create a new appointment object
		Appointment app = new Appointment(ssn,name,surname,code,date,startTime,slot);
		// get the current size of appColl
		int nApp = this.appColl.size();
		// set the appointment id
		app.setAppID(Integer.toString(nApp+1));
		
		// add the new appointment to our collection
		this.appColl.put(Integer.toString(nApp+1), app);
		doc.updScheduledApp();
		return Integer.toString(nApp+1);
	}

	/**
	 * retrieves the doctor for an appointment
	 * 
	 * @param idAppointment id of appointment
	 * @return doctor code id
	 */
	public String getAppointmentDoctor(String idAppointment) {
		return this.appColl.get(idAppointment).getDocID();
	}

	/**
	 * retrieves the patient for an appointment
	 * 
	 * @param idAppointment id of appointment
	 * @return doctor patient ssn
	 */
	public String getAppointmentPatient(String idAppointment) {
		return this.appColl.get(idAppointment).getSsn();
	}

	/**
	 * retrieves the time for an appointment
	 * 
	 * @param idAppointment id of appointment
	 * @return time of appointment
	 */
	public String getAppointmentTime(String idAppointment) {
		return this.appColl.get(idAppointment).getStartTime();
	}

	/**
	 * retrieves the date for an appointment
	 * 
	 * @param idAppointment id of appointment
	 * @return date
	 */
	public String getAppointmentDate(String idAppointment) {
		return this.appColl.get(idAppointment).getDate();
	}

	/**
	 * retrieves the list of a doctor appointments for a given day.
	 * Appointments are reported as string with the format
	 * "hh:mm=SSN"
	 * 
	 * @param code doctor id
	 * @param date date required
	 * @return list of appointments
	 */
	public Collection<String> listAppointments(String code, String date) {
		
		return this.appColl.values().stream()
				.filter(a->a.getDocID().equals(code))
				.map(Appointment::toStringAppointment)
				.collect(Collectors.toList());
			
	}

	/**
	 * Define the current date for the medical centre
	 * The date will be used to accept patients arriving at the centre.
	 * 
	 * @param date	current date
	 * @return the number of total appointments for the day
	 */
	public int setCurrentDate(String date) {
		this.currentDate=date;
		
		return (int) this.appColl.values().stream()
				.filter(a->a.getDate().equals(date))
				.count();
	}

	/**
	 * mark the patient as accepted by the med centre reception
	 * 
	 * @param ssn SSN of the patient
	 */
	public void accept(String ssn) {
		this.appColl.values().stream()
			.filter(a->a.getDate().equals(currentDate))
			.filter(a->a.getSsn().equals(ssn))
			.forEach(a->{
				// se non abbiamo ancora un entry per questo dottore ne aggiungiamo una
				if (!this.acceptedAppPerDocColl.containsKey(a.getDocID())) {
					TreeSet<Appointment> accApp = new TreeSet<>();
					accApp.add(a);
					this.acceptedAppPerDocColl.put(a.getDocID(), accApp);
				}
				else {
					// altrimenti se c'è già aggiorno soltanto il set relativo agli appuntamenti accettati dal dottore
					this.acceptedAppPerDocColl.get(a.getDocID()).add(a);
				}
				// in ogni caso poi setta il paziente come accettato
				a.setAccepted(true);
				
			});
	}

	/**
	 * returns the next appointment of a patient that has been accepted.
	 * Returns the id of the earliest appointment whose patient has been
	 * accepted and the appointment not completed yet.
	 * Returns null if no such appointment is available.
	 * 
	 * @param code	code id of the doctor
	 * @return appointment id
	 */
	public String nextAppointment(String code) {
		TreeSet<Appointment> accApp = this.acceptedAppPerDocColl.get(code);
		
		if (accApp == null || accApp.size()==0)
			return null;
		
		return accApp.first().getAppID();
	}

	/**
	 * mark an appointment as complete.
	 * The appointment must be with the doctor with the given code
	 * the patient must have been accepted
	 * 
	 * @param code		doctor code id
	 * @param appId		appointment id
	 * @throws MedException in case code or appointment code not valid,
	 * 						or appointment with another doctor
	 * 						or patient not accepted
	 * 						or appointment not for the current day
	 */
	public void completeAppointment(String code, String appId)  throws MedException {
		
		if (!this.doctorsColl.containsKey(code))
			throw new MedException();
		Doctor doc = this.doctorsColl.get(code);
		if (!this.appColl.containsKey(appId))
			throw new MedException();
		Appointment app = this.appColl.get(appId);
		
		if (!this.acceptedAppPerDocColl.get(code).contains(app))
			throw new MedException();
		
		// appointment completed we can remove it from our collection
		this.acceptedAppPerDocColl.get(code).remove(app);
	}

	/**
	 * computes the show rate for the appointments of a doctor on a given date.
	 * The rate is the ratio of accepted patients over the number of appointments
	 *  
	 * @param code		doctor id
	 * @param date		reference date
	 * @return	no show rate
	 */
	public double showRate(String code, String date) {
		
		double totNoApp = this.appColl.values().stream()
				.filter(a->a.getDocID().equals(code) && a.getDate().equals(date)).count();
		System.out.println(totNoApp);
		double accPatNo = this.appColl.values().stream()
				.filter(a->a.getDocID().equals(code) && a.getDate().equals(date))
				.filter(a->a.isAccepted())
				.count();
		
		return accPatNo/totNoApp;
	}

	/**
	 * computes the schedule completeness for all doctors of the med centre.
	 * The completeness for a doctor is the ratio of the number of appointments
	 * over the number of slots in the schedule.
	 * The result is a map that associates to each doctor id the relative completeness
	 * 
	 * @return the map id : completeness
	 */
	public Map<String, Double> scheduleCompleteness() {
		
		
		Map<String, Double> res = this.doctorsColl.values().stream()
		.collect(Collectors.toMap(d->d.getId(),
				d->{
					System.out.println("Doc: "+((Doctor) d).getId());

					int totSlotsNo = ((Doctor) d).getTotalSlots();
					int totAcceptedApp = ((Doctor) d).getScheduledAppointments();
					System.out.println("totSlotsNo: "+totSlotsNo);
					System.out.println("totAcceptedApp: "+totAcceptedApp);
					
					return (double)totAcceptedApp/totSlotsNo;
				}));
		
		return res;
	}

}