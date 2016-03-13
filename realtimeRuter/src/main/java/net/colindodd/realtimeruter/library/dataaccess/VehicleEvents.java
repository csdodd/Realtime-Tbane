package net.colindodd.realtimeruter.library.dataaccess;

import net.colindodd.realtimeruter.library.model.RuterEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VehicleEvents {
	
	private final HashMap<String, RuterEvents> vehicles;
	
	public VehicleEvents() {
		this.vehicles = new HashMap<>();
	}
	
	public void put(final String vehicleRef, final RuterEvent vehicleEvent) {
		if(vehicles.containsKey(vehicleRef)) {
			vehicles.get(vehicleRef).put(vehicleEvent);
		} else {
			final RuterEvents newRuterEvents = new RuterEvents(vehicleEvent);
			vehicles.put(vehicleRef, newRuterEvents);
		}
	}
	
	public void putAll(final VehicleEvents vehicleEvents) {
		if(vehicleEvents == null) {
			return;
		}
		
		final Iterator<?> iter = vehicleEvents.getIterator();
		
		while(iter.hasNext()) {
			@SuppressWarnings("unchecked")
			final Map.Entry<String,RuterEvents> mEntry = (Map.Entry<String,RuterEvents>) iter.next();
			final String entryVehicleRef = (String)mEntry.getKey();
			final RuterEvents entryRuterEvents = (RuterEvents)mEntry.getValue();
			
			if (vehicles.containsKey(entryVehicleRef)) {
				vehicles.get(entryVehicleRef).putAll(entryRuterEvents);
			} else {
				vehicles.put(entryVehicleRef, entryRuterEvents);
			}
		}
	}
	
	public ArrayList<RuterEvent> getEvents() {
		final ArrayList<RuterEvent> events = new ArrayList<>();
		final Iterator<?> iter = getIterator();
		
		while (iter.hasNext()) {
			@SuppressWarnings("unchecked")
			final Map.Entry<String,RuterEvents> mEntry = (Map.Entry<String,RuterEvents>) iter.next();
			final RuterEvents ruterEvents = mEntry.getValue();
			
			if (ruterEvents.refresh()) {
				events.add(ruterEvents.getNextEvent());
			}
		}
		return events;
	}
	
	public String toString(final String vehicleRef) {
		if (!vehicles.containsKey(vehicleRef)) {
			return vehicleRef + " does not exist";
		}

		return vehicles.get(vehicleRef).toString();
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final Iterator<?> iter = getIterator();
		
		while (iter.hasNext()) {
			@SuppressWarnings("unchecked")
			final Map.Entry<String,RuterEvents> mEntry = (Map.Entry<String,RuterEvents>) iter.next();
			final String entryVehicleRef = (String)mEntry.getKey();
			final RuterEvents entryVehicleEventMap = (RuterEvents)mEntry.getValue();
			
			if (entryVehicleEventMap.refresh()) {
				sb.append(entryVehicleRef + "\n");
				sb.append(entryVehicleEventMap.allToString() + "\n");
				sb.append(entryVehicleEventMap.toString() + "\n");
			}
		}
		sb.append("###################################################\n\n");
		return sb.toString();
	}
	
	private Iterator<?> getIterator() {
		return vehicles.entrySet().iterator();
	}
}
