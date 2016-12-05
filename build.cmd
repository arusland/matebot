del %~dp0dist\*.jar
call mvn clean package
SET JARFILE=
FOR %%I in (%~dp0matebot-core\target\matebot-core-*-dependencies.jar) DO SET JARFILE=%%I
copy %JARFILE% .\dist\matebot.jar /Y