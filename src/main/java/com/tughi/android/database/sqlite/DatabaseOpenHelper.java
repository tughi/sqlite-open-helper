package com.tughi.android.database.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A {@link SQLiteOpenHelper} that uses asset SQL scripts to create/update the database.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private final Context context;
    private final int version;

    public DatabaseOpenHelper(Context context, String name, int version) {
        super(context.getApplicationContext(), name, null, version);

        this.context = context.getApplicationContext();
        this.version = version;
    }

    @Override
    public final void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, version);
    }

    @Override
    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            for (int version = oldVersion + 1; version <= newVersion; version++) {
                migrate(db, version);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to migrate the database", exception);
        }
    }

    private void migrate(SQLiteDatabase db, int version) throws IOException {
        db.beginTransaction();
        try {
            // open migration script
            ScriptParser parser = new ScriptParser(context.getAssets().open("db/migrate_" + version + ".sql"));
            try {
                String statement;
                while ((statement = parser.next()) != null) {
                    db.execSQL(statement);
                }
            } finally {
                parser.close();
            }

            db.setVersion(version);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * A line-based parser that splits an SQL script into SQL statements.
     * The SQL statements are separated by double-dash SQL comments.
     */
    private class ScriptParser {

        private StringBuilder builder;
        private BufferedReader reader;

        private ScriptParser(InputStream input) {
            builder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(input));
        }

        private String next() throws IOException {
            builder.setLength(0);
            boolean ignoreLine = true;

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    if (!ignoreLine) {
                        break;
                    }
                } else if (trimmedLine.length() > 0) {
                    ignoreLine = false;
                    builder.append(line).append('\n');
                }
            }

            return builder.length() != 0 ? builder.toString() : null;
        }

        private void close() throws IOException {
            reader.close();
        }

    }

}
