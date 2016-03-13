package net.colindodd.realtimeruter.library.model;

public class GeographicalLocation {
	private double latitude;
	private double longitude;
	
	public GeographicalLocation(final double latitude, final double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	@Override
	public String toString() {
		return getLatitude() + " - " + getLongitude();
	}
}
