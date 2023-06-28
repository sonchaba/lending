 REM script to backup PostgresSQL databases
  @ECHO off

 SET datestr=%date%_%time:~0,8%
 SET datestr=%datestr: =%
 SET datestr=%datestr::=%
 SET datestr=%datestr:/=%
 SET datestr=%datestr:.=%
 SET db1=%1
 SET FIlLENAME1=%db1%_%datestr%.backup
 SET backupFolders=%2
 SET backupFile=%backupFolders%\%FIlLENAME1%
 SET baseDir="%ProgramFiles%"
 SET cmdBackup=C:\"Program Files"\PostgreSQL\15\bin\pg_dump.exe --host localhost --port 5432 --username "postgres" --no-password  --format tar --blobs --verbose --file "%backupFile%" "%db1%"
 call %cmdBackup%
 exit