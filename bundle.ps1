mvn clean package
Copy-Item target/sunrise-launcher-jp.jar .
Compress-Archive -Path launch.bat, sunrise-launcher-jp.jar, runtime -DestinationPath SunriseLauncher.zip -Force
Remove-Item sunrise-launcher-jp.jar
