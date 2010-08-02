== Setup Notes for Eclipse==

The project is built with Maven which automatically will download dependencies,
that will take time if you don't have Maven already installed.

Two code repositories are needed to build this project:

* git+ssh://git@gitorious.process-one.net/wave-api/wave-api.git

Contains Google Wave Robot API and Wave Model projects backported to Java 5 
(Android only accepts Java 5 files and original code is written for Java 6).

From this repository we need the "backport" branch. So this command may be
necessary:
git checkout -b backport origin/backport

* git+ssh://git@gitorious.process-one.net/wave-api/android-wave-client.git
The Android client itself. Requires the APIs from wave-api project listed above.


= Installing Wave Google APIs

1) Be sure you have maven2 installed.  If not:
   $sudo apt-get install maven2

2) $git checkout -b backport origin/backport
   (backport to android)

3) Execute
   $mvn clean install

   If the build fails, probably you need to specify the path to the JDK, in this
   way:

   $mvn clean install -DJAVA_1_5_HOME=/usr/lib/jvm/java-6-sun

   (it is that way,  JAVA_1_5_HOME pointing to a java6 sdk)

   on MacOSX, command is:
   $mvn clean install -DJAVA_1_5_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home

   This is a workaround in fact, in order to guarantee that the project will run
   on Android it is required that this project can be compiled and its tests can
   run on a Java 5 JDK. Thus this path is required as a reminder to install and
   use the proper JDK for the work.

   At this point, the google wave api should be installed in your local maven
   repository. Now we can move and configure the android app.

= Configuring the Wave client for Android in Eclipse

1) Execute
   $mvn clean install

   This step requires an Android SDK already installed in the system and the
   ANDROID_HOME environment variable already published.

2) Generate the eclipse project files, executing:
   $mvn eclipse:eclipse

3) Now you can import the project into Eclipse.

   Eclipse can be memory intensive, so you may find convenient to increase its
   memory in eclipse.ini, using -Xmx512m  (default one in eclipse.ini is 256m).

   It will give errors because of an undefined variable "M2_REPO". Add it:
   windows -> Preferences. Java->Buildpath-> classpath variables
   M2_REPO = $USER_HOME/.m2/repository (replace $USER_HOME with your real user
   home directory.

4) You need to remove one path from the project classpath, the one named "JRE
   System Library"
        right click -> remove from builpath
   Not absolutely required.

5) From eclipse menu, Project->clean, to force rebuild the project. Not required
   but can help.

Done, at this point you should have the project ready to go.

