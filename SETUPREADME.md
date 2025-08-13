Copy all of these files and folders from the mdk :
* GRADLE
* SRC (optional)
* build.gradle
* gradle.properties
* gradlew
* gradlew.bat
* settings.gradle

To setup a neoforge workspace in VSCode, you then need to :
* Make sure you have a CMD terminal open (NOT POWERSHELL)
* Run "gradlew build"
* Fix all errors pertaining to the wrong modid and folder structure being used in the src folder.
* Run "gradlew :runData" to generate data (DON'T FORGET TO DO THIS EVERYTIME BEFORE YOU RUN THE NEXT STEP)
* Run "gradlew :runClient"
* If you copied your gradle files from another folder you were doing tests in (because gradle just WON'T cooperate), make sure to rename the test folder so the cache doesn't use files from the test folder instead of files from your actual directory.