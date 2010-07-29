== Setup Notes for Eclipse==

Be prepared to invest 20' going through these steps.  Maven will download all required dependencies,
but the first time you use it, it will start by downloading lots of jars.


Two projects are needed:

* git+ssh://git@gitorious.process-one.net/wave-api/wave-api.git
* git+ssh://git@gitorious.process-one.net/wave-api/android-wave-client.git

The second one is the real android client. The first one is a library dependency needed
by the client.  


= Installing WAVE-API
1) Be sure you have maven2 installed.  If not:
   $sudo apt-get install maven2

2) $git checkout -b backport origin/backport
   (backport to android)

3) Execute
   $mvn clean install 
   
   If the build fails, probably you need to specify the path to the JDK, in this way:

   $mvn clean install -DJAVA_1_5_HOME=/usr/lib/jvm/java-6-sun

   (it is that way,  JAVA_1_5_HOME pointing to a java6 sdk)

   on MacOSX, command is:
   $mvn clean install -DJAVA_1_5_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home

At this point, the google wave api should be installed in your local maven repository.
Now we can move and configure the android app

= Configuring the wave client for android in Eclipse

1) Execute
    $mvn clean install  

2) Generate the eclipse project files, executing:
    $mvn eclipse:eclipse

Eclipse can be memory-hungry, so you may find convenient to increase its memory in eclipse.ini,
using -Xmx512m  (default one in eclipse.ini is probably 256m)


3) Now you can import the project into eclipse.
   It will give errors because of an undefined variable "M2_REPO".  Add it:
    windows -> Preferences. Java->Buildpath-> classpath variables
         M2_REPO = /home/%USER%/.m2/repository   (replace %USER% with your real user :) )
   (eclipse may crash..)

4) You need to remove one path from the project classpath, the one named "JRE System Library"
        right click -> remove from builpath
   (eclipse may crash..)

5) From  eclipse' menu,  Project->clean , to force rebuild the project.


Done, at this point you should have the project ready to go.


