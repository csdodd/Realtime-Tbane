package net.colindodd.realtimeruter.library.model;

import com.google.gson.annotations.SerializedName;

import net.colindodd.realtimeruter.library.RealtimeRuterLibrary;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class RuterEvent implements Comparable<RuterEvent> {
    @SerializedName("AimedArrivalTime") private String aimedArrivalTime;
    @SerializedName("AimedDepartureTime") private String aimedDepartureTime;
    @SerializedName("DestinationDisplay") private String destinationDisplay;
    @SerializedName("DirectionName") private String directionName;
    @SerializedName("ExpectedArrivalTime") private String expectedArrivalTime;
    @SerializedName("ExpectedDepartureTime") private String expectedDepartureTime;
    @SerializedName("PublishedLineName") private int publishedLineName;
    @SerializedName("VehicleRef") private String vehicleRef;
    @SerializedName("VehicleAtStop") private boolean vehicleAtStop;

    private DateTime aimedArrivalTimeAsDate;
    private DateTime expectedArrivalTimeAsDate;
    private DateTime aimedDepartureTimeAsDate;
    private DateTime expectedDepartureTimeAsDate;
    private Station station;
    private GeographicalLocation vehicleLocation;

    public String getAimedArrivalTime() {
        return aimedArrivalTime;
    }

    public String getAimedDepartureTime() {
        return aimedDepartureTime;
    }

    public String getDestinationDisplay() {
        return destinationDisplay;
    }

    public String getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    public String getExpectedDepartureTime() {
        return expectedDepartureTime;
    }

    public int getPublishedLineName() {
        return publishedLineName;
    }

    public String getVehicleRef() {
        return vehicleRef;
    }

    public String getDirectionName() {
        return directionName;
    }

    public boolean isVehicleAtStop() {
        return vehicleAtStop;
    }

    public void setVehicleIsAtStop(final boolean vehicleAtStop) {
        this.vehicleAtStop = vehicleAtStop;
    }

    // DateTime helper functions
    public DateTime getAimedArrivalTimeAsDate() {
        if (aimedArrivalTimeAsDate == null) {
            aimedArrivalTimeAsDate = trimDate(getAimedArrivalTime());
        }
        return aimedArrivalTimeAsDate;
    }

    public DateTime getExpectedArrivalTimeAsDate() {
        if (expectedArrivalTimeAsDate == null) {
            expectedArrivalTimeAsDate = trimDate(getExpectedArrivalTime());
        }
        return expectedArrivalTimeAsDate;
    }

    public void setExpectedArrivalTimeAsDate(final DateTime expectedArrivalTimeAsDate) {
        this.expectedArrivalTimeAsDate = expectedArrivalTimeAsDate;
    }


    public DateTime getAimedDepartureTimeAsDate() {
        if (aimedDepartureTimeAsDate == null) {
            aimedDepartureTimeAsDate = trimDate(getAimedDepartureTime());
        }
        return aimedDepartureTimeAsDate;
    }

    public DateTime getExpectedDepartureTimeAsDate() {
        if (expectedDepartureTimeAsDate == null) {
            expectedDepartureTimeAsDate = trimDate(getExpectedDepartureTime());
        }
        return expectedDepartureTimeAsDate;
    }

    public void setExpectedDepartureTime(final DateTime expectedDepartureTimeAsDate) {
        this.expectedDepartureTimeAsDate = expectedDepartureTimeAsDate;
    }

    private DateTime trimDate(final String dateToTrim) {
        final String timeAsString = dateToTrim.substring(6);
        final String millisseconds = timeAsString.substring(0, timeAsString.length() - 7);
        final Long millis = Long.parseLong(millisseconds);
        return new DateTime(millis);
    }

    public Duration getDurationUntilExpectedArrival() {
        return new Duration(RealtimeRuterLibrary.getAppTime(), getExpectedArrivalTimeAsDate());
    }

    public Duration getDurationUntilExpectedDeparture() {
        return new Duration(RealtimeRuterLibrary.getAppTime(), getExpectedDepartureTimeAsDate());
    }

    public String getDurationUntilExpectedDepartureAsString() {
        return formatter.print(getDurationUntilExpectedDeparture().toPeriod());
    }

    public String getDurationUntilExpectedArrivalAsString() {
        String retVal = formatter.print(getDurationUntilExpectedArrival().toPeriod());
        if (retVal.trim().length() == 0) return "1s";

        return retVal;
    }

    public String getStationName() {
        return station == null ? "null" : station.name();
    }

    public Station getStation() {
        return station;
    }

    public void setStation(final Station station) {
        this.station = station;
    }

    public boolean isValidForMap() {
        return getVehicleRef() != null && getGeographicalLocation() != null && !isInThePast();
    }

    public GeographicalLocation getGeographicalLocation() {
        return vehicleLocation;
    }

    public void setGeographicalLocation(final GeographicalLocation vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }


    // When first loading the data we have no information about the previous event - so find it!
    public RuterEvent createPreviousEvent() {
        final RuterEvent prevEvent = new RuterEvent();
        final Station previousStation = Lines.findPreviousStation(getStation(), getPublishedLineName(), getDirectionName());
        prevEvent.setStation(previousStation);

        // if - we're at the end of a line
        if (prevEvent.getStationName().equals(getStationName())) {
            prevEvent.setExpectedArrivalTimeAsDate(getExpectedArrivalTimeAsDate());
            prevEvent.setExpectedDepartureTime(getExpectedDepartureTimeAsDate());
        } else {
            final int DefaultJourneyLengthMillis = 240000; // 4 minutes
            DateTime pastEventDateTime = getExpectedArrivalTimeAsDate();
            pastEventDateTime = pastEventDateTime.minusMillis(Math.max((int) getDurationUntilExpectedArrival().getMillis(),
                    DefaultJourneyLengthMillis - (int) getDurationUntilExpectedArrival().getMillis()));
            prevEvent.setExpectedArrivalTimeAsDate(pastEventDateTime);
            prevEvent.setExpectedDepartureTime(pastEventDateTime);
        }
        return prevEvent;
    }

    public String previousStationName() {
        return Lines.findPreviousStation(getStation(), getPublishedLineName(), getDirectionName()).name();
    }

    // Some logic functions

    public boolean isInThePast() {
        return getDurationUntilExpectedArrival().isShorterThan(Duration.ZERO) || getDurationUntilExpectedArrival().isEqual(Duration.ZERO);
    }

    public boolean isFarInTheFuture() {
        final Duration TenMinutes = new Duration(600000);
        return getDurationUntilExpectedArrival().isLongerThan(TenMinutes);
    }

    public boolean isAtStation() {
        if (getDurationUntilExpectedArrival().isEqual(getDurationUntilExpectedDeparture()))
            forceStationStay();
        return (getDurationUntilExpectedArrival().isShorterThan(Duration.ZERO) || getDurationUntilExpectedArrival().isEqual(Duration.ZERO)) &&
                (getDurationUntilExpectedDeparture().isLongerThan(Duration.ZERO) || getDurationUntilExpectedDeparture().isEqual(Duration.ZERO));
    }


    private void forceStationStay() {
        final int StationFudgeSeconds = 20; //Hack to make it look like trains stop at stations!
        expectedDepartureTimeAsDate = expectedDepartureTimeAsDate.plusSeconds(StationFudgeSeconds);
        expectedArrivalTimeAsDate = expectedArrivalTimeAsDate.minusSeconds(StationFudgeSeconds);
    }

    @Override
    public String toString() {
        final StringBuilder retVal = new StringBuilder();
        retVal.append("Next Stop:\t" + getStationName() + "\n");
        if (isAtStation()) {
            retVal.append("Departing in ");
            retVal.append(getDurationUntilExpectedDepartureAsString());
        } else {
            retVal.append("Arriving in ");
            retVal.append(getDurationUntilExpectedArrivalAsString());
            retVal.append("\n" + getDelayInfo());
        }

        return retVal.toString();
    }

    private String getDelayInfo() {
        return getDelayInfo("late", "early");
    }

    public String getDelayInfo(final String late, final String early) {
        if (!getExpectedArrivalTimeAsDate().equals(getAimedArrivalTimeAsDate())) {
            Duration difference = new Duration(getAimedArrivalTimeAsDate(), getExpectedArrivalTimeAsDate());
            if (difference.isLongerThan(new Duration(60000))) {
                return formatter.print(difference.toPeriod()) + " " + late;
            } else if (difference.isShorterThan(new Duration(-60000))) {
                return (formatter.print(difference.toPeriod()) + " " + early).substring(1);
            }
        }
        return "";
    }

    private static PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendMinutes()
            .appendSuffix("m")
            .appendSeconds()
            .appendSuffix("s")
            .toFormatter();

    // Comparable functions
    @Override
    public int compareTo(final RuterEvent other) {
        if (getExpectedArrivalTimeAsDate().isBefore(other.getExpectedArrivalTimeAsDate()))
            return -1;
        return equals(other) ? 0 : 1;
    }

    @Override
    public boolean equals(final Object other) {
        RuterEvent otherEvent = (RuterEvent) other;
        return getExpectedArrivalTimeAsDate().isEqual(otherEvent.getExpectedArrivalTimeAsDate());
    }

    @Override
    public int hashCode() {
        return (int) getDurationUntilExpectedArrival().getMillis();
    }

    public boolean isValid() {
        return 1 <= publishedLineName && publishedLineName <= 5;
    }
}
