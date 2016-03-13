package net.colindodd.realtimeruter.library.model;

import java.util.HashMap;

public final class Location {

    private final HashMap<Station, GeographicalLocation> locations;

    public Location() {
        locations = new HashMap<Station, GeographicalLocation>();
        populateLocationData();
    }

    private void populateLocationData() {
        locations.put(Station.Ammerud, new GeographicalLocation(59.957932, 10.871208));
        locations.put(Station.Bekkestua, new GeographicalLocation(59.916516, 10.587881));
        locations.put(Station.Berg, new GeographicalLocation(59.951153, 10.744715));
        locations.put(Station.Bergkrystallen, new GeographicalLocation(59.867099, 10.821512));
        locations.put(Station.Besserud, new GeographicalLocation(59.957691, 10.673491));
        locations.put(Station.Bjørnsletta, new GeographicalLocation(59.926876, 10.635349));
        locations.put(Station.Blindern, new GeographicalLocation(59.940025, 10.716037));
        locations.put(Station.Bogerud, new GeographicalLocation(59.875828, 10.842218));
        locations.put(Station.Borgen, new GeographicalLocation(59.934581, 10.696006));
        locations.put(Station.Brattlikollen, new GeographicalLocation(59.887449, 10.801202));
        locations.put(Station.Brynseng, new GeographicalLocation(59.909007, 10.812027));
        locations.put(Station.Bøler, new GeographicalLocation(59.884332, 10.845501));
        locations.put(Station.CarlBernersPlass, new GeographicalLocation(59.925969, 10.778371));
        locations.put(Station.Eiksmarka, new GeographicalLocation(59.946672, 10.622363));
        locations.put(Station.Ekraveien, new GeographicalLocation(59.950756, 10.636117));
        locations.put(Station.Ellingsrudåsen, new GeographicalLocation(59.936311, 10.916612));
        locations.put(Station.Ensjø, new GeographicalLocation(59.913326, 10.787115));
        locations.put(Station.Forskningsparken, new GeographicalLocation(59.94344, 10.720285));
        locations.put(Station.Frognerseteren, new GeographicalLocation(59.979196, 10.675707));
        locations.put(Station.Frøen, new GeographicalLocation(59.934062, 10.708762));
        locations.put(Station.Furuset, new GeographicalLocation(59.941879, 10.897279));
        locations.put(Station.Gaustad, new GeographicalLocation(59.945643, 10.709959));
        locations.put(Station.Gjønnes, new GeographicalLocation(59.914646, 10.581363));
        locations.put(Station.Godlia, new GeographicalLocation(59.908502, 10.835438));
        locations.put(Station.Grorud, new GeographicalLocation(59.961563, 10.881572));
        locations.put(Station.Gråkammen, new GeographicalLocation(59.955037, 10.701681));
        locations.put(Station.Grønland, new GeographicalLocation(59.912869, 10.759778));
        locations.put(Station.Gulleråsen, new GeographicalLocation(59.955762, 10.696478));
        locations.put(Station.Hasle, new GeographicalLocation(59.924678, 10.794454));
        locations.put(Station.Haugerud, new GeographicalLocation(59.922866, 10.855072));
        locations.put(Station.Hellerud, new GeographicalLocation(59.910309, 10.830073));
        locations.put(Station.Helsfyr, new GeographicalLocation(59.9116, 10.803766));
        locations.put(Station.Holmen, new GeographicalLocation(59.946401, 10.666475));
        locations.put(Station.Holmenkollen, new GeographicalLocation(59.960489, 10.662575));
        locations.put(Station.Holstein, new GeographicalLocation(59.960451, 10.74052));
        locations.put(Station.Hovseter, new GeographicalLocation(59.946308, 10.65485));
        locations.put(Station.Høyenhall, new GeographicalLocation(59.905984, 10.819645));
        locations.put(Station.Jar, new GeographicalLocation(59.926582, 10.621805));
        locations.put(Station.Jernbanetorget, new GeographicalLocation(59.912181, 10.751967));
        locations.put(Station.Kalbakken, new GeographicalLocation(59.954194, 10.866766));
        locations.put(Station.Karlsrud, new GeographicalLocation(59.880539, 10.805311));
        locations.put(Station.Kringsjå, new GeographicalLocation(59.963776, 10.734973));
        locations.put(Station.Lambertseter, new GeographicalLocation(59.873583, 10.810204));
        locations.put(Station.Lijordet, new GeographicalLocation(59.941116, 10.61665));
        locations.put(Station.Lillevann, new GeographicalLocation(59.980495, 10.652897));
        locations.put(Station.Lindeberg, new GeographicalLocation(59.933, 10.882215));
        locations.put(Station.Linderud, new GeographicalLocation(59.941062, 10.839257));
        locations.put(Station.Majorstuen, new GeographicalLocation(59.93053, 10.714309));
        locations.put(Station.Makrellbekken, new GeographicalLocation(59.942266, 10.674205));
        locations.put(Station.Manglerud, new GeographicalLocation(59.897806, 10.812478));
        locations.put(Station.Midtstuen, new GeographicalLocation(59.961348, 10.683303));
        locations.put(Station.Montebello, new GeographicalLocation(59.933903, 10.664678));
        locations.put(Station.Mortensrud, new GeographicalLocation(59.849094, 10.828657));
        locations.put(Station.Munkelia, new GeographicalLocation(59.868844, 10.812693));
        locations.put(Station.Nationaltheater, new GeographicalLocation(59.915537, 10.733213));
        locations.put(Station.Nydalen, new GeographicalLocation(59.94909, 10.765207));
        locations.put(Station.Oppsal, new GeographicalLocation(59.892747, 10.840201));
        locations.put(Station.Ringstabekk, new GeographicalLocation(59.920226, 10.602107));
        locations.put(Station.Ris, new GeographicalLocation(59.948091, 10.704675));
        locations.put(Station.Risløkka, new GeographicalLocation(59.932226, 10.822906));
        locations.put(Station.Rommen, new GeographicalLocation(59.961671, 10.90805));
        locations.put(Station.Romsås, new GeographicalLocation(59.962294, 10.890799));
        locations.put(Station.Ryen, new GeographicalLocation(59.895761, 10.805569));
        locations.put(Station.Røa, new GeographicalLocation(59.946694, 10.644336));
        locations.put(Station.Rødtvet, new GeographicalLocation(59.951551, 10.856767));
        locations.put(Station.Sinsen, new GeographicalLocation(59.938117, 10.781364));
        locations.put(Station.Skogen, new GeographicalLocation(59.973999, 10.64808));
        locations.put(Station.Skullerud, new GeographicalLocation(59.866786, 10.839225));
        locations.put(Station.Skådalen, new GeographicalLocation(59.961939, 10.690996));
        locations.put(Station.Skøyenåsen, new GeographicalLocation(59.899025, 10.836607));
        locations.put(Station.Slemdal, new GeographicalLocation(59.950089, 10.695448));
        locations.put(Station.Smestad, new GeographicalLocation(59.937279, 10.683517));
        locations.put(Station.Sognsvann, new GeographicalLocation(59.967229, 10.733879));
        locations.put(Station.Steinerud, new GeographicalLocation(59.939343, 10.703945));
        locations.put(Station.Storo, new GeographicalLocation(59.94405, 10.778757));
        locations.put(Station.Stortinget, new GeographicalLocation(59.912934, 10.741968));
        locations.put(Station.Stovner, new GeographicalLocation(59.962186, 10.923371));
        locations.put(Station.Trosterud, new GeographicalLocation(59.927087, 10.864062));
        locations.put(Station.Tveita, new GeographicalLocation(59.914139, 10.84166));
        locations.put(Station.Tåsen, new GeographicalLocation(59.953334, 10.752225));
        locations.put(Station.Tøyen, new GeographicalLocation(59.915236, 10.774627));
        locations.put(Station.Ullernåsen, new GeographicalLocation(59.930786, 10.65485));
        locations.put(Station.UllevålStadion, new GeographicalLocation(59.946764, 10.732194));
        locations.put(Station.Ulsrud, new GeographicalLocation(59.88982, 10.849128));
        locations.put(Station.Veitvet, new GeographicalLocation(59.944373, 10.84681));
        locations.put(Station.Vestli, new GeographicalLocation(59.971959, 10.929336));
        locations.put(Station.Vettakollen, new GeographicalLocation(59.960231, 10.695491));
        locations.put(Station.Vinderen, new GeographicalLocation(59.942739, 10.704761));
        locations.put(Station.Voksenkollen, new GeographicalLocation(59.980108, 10.665407));
        locations.put(Station.Voksenlia, new GeographicalLocation(59.966936, 10.654517));
        locations.put(Station.Vollebekk, new GeographicalLocation(59.935903, 10.830889));
        locations.put(Station.Økern, new GeographicalLocation(59.928076, 10.80411));
        locations.put(Station.Østerås, new GeographicalLocation(59.939396, 10.608244));
        locations.put(Station.Østhorn, new GeographicalLocation(59.956622, 10.750036));
        locations.put(Station.Åsjordet, new GeographicalLocation(59.928743, 10.647254));
        locations.put(Station.Haslum, new GeographicalLocation(59.914978, 10.561829));
        locations.put(Station.Avløs, new GeographicalLocation(59.913042, 10.548193));
        locations.put(Station.Gjettum, new GeographicalLocation(59.908701, 10.531308));
        locations.put(Station.Hauger, new GeographicalLocation(59.910884, 10.511222));
        locations.put(Station.Kolsås, new GeographicalLocation(59.914815, 10.501072));
        locations.put(Station.Løren, new GeographicalLocation(59.930340, 10.793192));
    }

    public GeographicalLocation getStationLocation(final Station station) {
        return locations.get(station);
    }

    public GeographicalLocation getVehicleLocation(final Station previousStation, final Station nextStation, final double percentComplete) {
        if (previousStation == null || nextStation == null) {
            throw new IllegalArgumentException("Null station arguments");
        }
        if (previousStation.name().equals(nextStation.name())) {
            return locations.get(nextStation);
        }

        final GeographicalLocation prevLocation = locations.get(previousStation);
        final GeographicalLocation nextLocation = locations.get(nextStation);

        final GeographicalLocation currentLocation = new GeographicalLocation(prevLocation.getLatitude(), prevLocation.getLongitude());

        // Simple linear interpolation
        currentLocation.setLatitude(prevLocation.getLatitude() + (nextLocation.getLatitude() - prevLocation.getLatitude()) * percentComplete);
        currentLocation.setLongitude(prevLocation.getLongitude() + (nextLocation.getLongitude() - prevLocation.getLongitude()) * percentComplete);

        return currentLocation;
    }
}