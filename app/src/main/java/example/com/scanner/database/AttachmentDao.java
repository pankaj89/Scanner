package example.com.scanner.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class AttachmentDao {

    @Query("SELECT * FROM Attachment WHERE id IN (:userIds)")
    abstract List<Attachment> loadAllByIds(int[] userIds);

    /*@Query("SELECT * FROM Attachment WHERE first_name LIKE :first AND "
            + "last_name LIKE :last LIMIT 1")
    abstract Attachment findByName(String first, String last);*/

    @Insert
    abstract void insertAll(Attachment... users);

    public void insertAllAsync(final Attachment[] users, final Callback callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                insertAll(users);
                return null;
            }

            @Override
            protected void onPostExecute(Void list) {
                super.onPostExecute(list);
                if (callback != null)
                    callback.done();
            }
        }.execute();
    }

    @Update
    abstract void update(Attachment attachment);

    public void updateAsync(final Attachment attachment, final Callback callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                update(attachment);
                return null;
            }

            @Override
            protected void onPostExecute(Void list) {
                super.onPostExecute(list);
                if (callback != null)
                    callback.done();
            }
        }.execute();
    }


    @Delete
    abstract void delete(Attachment user);

    interface Callback {
        void done();
    }

    public interface GetAllCallback {
        void getAll(ArrayList<Attachment> list);
    }

    @Query("SELECT * FROM Attachment")
    abstract List<Attachment> getAll();

    public void getAllAsync(final GetAllCallback callback) {
        new AsyncTask<Void, Void, ArrayList<Attachment>>() {
            @Override
            protected ArrayList<Attachment> doInBackground(Void... params) {
                return new ArrayList<Attachment>(getAll());
            }

            @Override
            protected void onPostExecute(ArrayList<Attachment> list) {
                super.onPostExecute(list);
                if (callback != null)
                    callback.getAll(list);
            }
        }.execute();
    }
}