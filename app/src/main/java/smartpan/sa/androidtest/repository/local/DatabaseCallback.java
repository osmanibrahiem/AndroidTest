package smartpan.sa.androidtest.repository.local;

import java.util.List;

import smartpan.sa.androidtest.model.Location;

public interface DatabaseCallback {

    void onLocationsLoaded(List<Location> locations);

    void onLocationAdded();

    void onDataNotAvailable();
}
