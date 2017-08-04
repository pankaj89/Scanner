package example.com.scanner.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Attachment.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AttachmentDao attachmentDao();
}