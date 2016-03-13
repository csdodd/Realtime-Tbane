package net.colindodd.realtimeruter.library.dataaccess;

import net.colindodd.realtimeruter.library.model.Location;
import net.colindodd.realtimeruter.library.model.RuterEvent;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class RuterEvents {
    private Set<RuterEvent> ruterEvents;

    private RuterEvent prevEvent = null;
    private RuterEvent nextEvent = null;

    private static Location locations;

    public RuterEvents(final RuterEvent firstEvent) {
        if (locations == null) locations = new Location();
        ruterEvents = new TreeSet<RuterEvent>();
        put(firstEvent);
    }

    public void put(final RuterEvent event) {
        ruterEvents.add(event);
    }

    public void putAll(final RuterEvents ruterEvents) {
        final Iterator<?> iter = ruterEvents.getIterator();
        while (iter.hasNext()) {
            final RuterEvent newEvent = (RuterEvent) iter.next();
            put(newEvent);
        }
    }

    private Iterator<?> getIterator() {
        return ruterEvents.iterator();
    }

    public RuterEvent getNextEvent() {
        return nextEvent;
    }

    public boolean refresh() {
        if (nextEvent == null || nextEvent.isInThePast()) refreshNeighbouringEvents();
        if (nextEvent == null || nextEvent.isFarInTheFuture()) return false;
        if (prevEvent == null) prevEvent = nextEvent.createPreviousEvent();

        final Duration durationToNextEvent = nextEvent.getDurationUntilExpectedArrival();
        final Duration durationToPrevEvent = prevEvent.getDurationUntilExpectedDeparture();
        final boolean atStation = prevEvent.isAtStation();

        double percentageOfJourneyBetweenStations = 0;
        if (!atStation) {
            percentageOfJourneyBetweenStations = durationToNextEvent.getMillis() + Math.abs(durationToPrevEvent.getMillis());
            percentageOfJourneyBetweenStations = Math.abs(durationToPrevEvent.getMillis()) / percentageOfJourneyBetweenStations;
        }
        nextEvent.setVehicleIsAtStop(atStation);
        prevEvent.setVehicleIsAtStop(atStation);

        nextEvent.setGeographicalLocation(locations.getVehicleLocation(prevEvent.getStation(), nextEvent.getStation(), percentageOfJourneyBetweenStations));
        return true;
    }

    private void refreshNeighbouringEvents() {
        nextEvent = null;
        prevEvent = null;

        final Iterator<?> iter = getIterator();
        while (iter.hasNext() && nextEvent == null) {
            final RuterEvent event = (RuterEvent) iter.next();
            nextEvent = event.isInThePast() ? null : event;
            prevEvent = nextEvent == null ? event : prevEvent; //Works due to the fact that events are sorted. Basically prevEvent will be set to the event before nextEvent.
        }
    }

    @Override
    public String toString() {
        final String timeToNextEvent = formatter.print(nextEvent.getDurationUntilExpectedArrival().toPeriod());
        final String timeToPrevEvent = formatter.print(prevEvent.getDurationUntilExpectedDeparture().toPeriod());

        return nextEvent.getPublishedLineName() + " " + nextEvent.getDestinationDisplay() + "\nNext:\t" + nextEvent.getStationName() + ",\t" + timeToNextEvent +
                "\nPrev:\t" + prevEvent.getStationName() + ",\t" + timeToPrevEvent;
    }

    public String allToString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<?> iter = ruterEvents.iterator();
        while (iter.hasNext()) {
            final RuterEvent event = (RuterEvent) iter.next();
            sb.append(event.getStationName() + ":\t" + formatter.print(event.getDurationUntilExpectedArrival().toPeriod()) + "\n");
        }
        return sb.toString();
    }

    private PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendMinutes()
            .appendSuffix("m")
            .appendSeconds()
            .appendSuffix("s")
            .toFormatter();
}
