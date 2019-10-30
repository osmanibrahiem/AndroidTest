package smartpan.sa.androidtest.repository.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import smartpan.sa.androidtest.model.Location;

@Database(entities = {Location.class}, version = 1, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase {

    public abstract LocationDao getLocationTable();
}
