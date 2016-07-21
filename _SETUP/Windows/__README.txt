To build the installer, you'll need Inno Setup (http://www.jrsoftware.org/isinfo.php).

Steps to build installer:
-Compile StickyNotes
-Inside the project's directory, you'll find a folder named dist: copy StickyNotes.jar into setupFiles, overwrite existing files
-Extract a Java JRE into the jre folder
-Create the launcher using launch4j (http://launch4j.sourceforge.net/) and the project in the launch4j folder, then copy the .exe file into setupFiles
-At this point, setupFiles should contain StickyNotes.exe, StickyNotes.jar, and a jre folder with a java runtime into it (bin, lib, ...)
-Compile setup.iss with Inno Setup Compiler
-OPTIONAL: sign the installer exe file using your pkf certificate
