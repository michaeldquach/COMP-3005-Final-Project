README
Michael Quach
101179729
COMP 3005 Final Project

1. Restore the dumped server: In pgAdmin4 create a new database named "bookstore". Right-click that database and click "Restore". The dumped server file is "bookstore_backup" in the location "\Database Backup".

1.b) Should the database be unable to be restored this way, an alternate method of restoring the server is first running "DDL.sql", then "Functions and Triggers.sql", then "Populate Database.sql". These files are located in the location "\SQL".

2. Configure the Java application. Add "postgresql-42.3.1.jar" as a referenced library/dependency. It is included in the location "\Bookstore Application". 

3. Configure the login credentials. These can be found in "Bookstore.java" in "\Bookstore Application", under the class attributes "url", "userid", "password".

4. Compile and run "Application.java".

For further information, all SQL and java files have been commented to explain their functionality.