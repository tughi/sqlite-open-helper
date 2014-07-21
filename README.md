sqlite-open-helper
==================

The **DatabaseOpenHelper** is a **SQLiteOpenHelper** class that uses SQL asset scripts to create/update the Android database. 

The SQL scripts must be stored in `/assets/db/migrate_VERSION.sql` files where `VERSION` is the version to which every script migrates. The script responsible to create the database has the version **1**.

The SQL statements are separated by **double-dash comments** (`--`), and are not allowed to contain any comments. This is because the script loader looks only for double-dash comments to split the script into SQL statements.

Example
-------

Instantiating the helper with the latest database version, somehere in your ContentProvider; usualy in the onCreate() method:

    helper = new DatabaseOpenHelper(context, 'content.db', 2)

*/assets/db/migrate_1.sql*:

    -- address table
    CREATE TABLE address (
      _id PRIMARY KEY,
      address TEXT
    );
    
    -- customer table
    CREATE TABLE customer (
      _id PRIMARY KEY,
      name TEXT NOT NULL,
      address_id INTEGER
    );

*/assets/db/migrate_2.sql*:

    -- add birth date
    ALTER TABLE customer ADD COLUMN birthdate INTEGER;

That's it!
