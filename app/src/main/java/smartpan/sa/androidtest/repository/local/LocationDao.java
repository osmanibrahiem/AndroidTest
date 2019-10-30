package smartpan.sa.androidtest.repository.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Maybe;
import smartpan.sa.androidtest.model.Location;

@Dao
public interface LocationDao {

    @Query("SELECT * FROM location")
    Maybe<List<Location>> getAll();

    @Insert
    void insert(Location... locations);

    @Delete
    void delete(Location location);

    @Update
    void update(Location location);
}
