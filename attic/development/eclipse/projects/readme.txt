INTRODUCTION

This directory contains Eclipse 3.4 and Flash Builder 4.0 projects that make up your
workspace for the BlazeDS development environment. You can import these projects into
your Eclipse workspace, however you should follow all of these directions before actually
importing any of them.

GETTING STARTED

* IMPORTANT - before you import any projects you should read and complete the
following steps!

1. Install the necessary requirements:
  - installed at least JDK 1.5.0 (or later) (although it is useful to have  JDK 1.4.2 in addition to JDK 1.5.0)
  - established your JAVA_HOME environment variable pointing to this JDK installation
  - download and install Apache Ant 1.6.2 or later (http://ant.apache.org/) along with ant-contrib-1.0b2.
  - established an ANT_HOME environment variable.
  - added %JAVA_HOME%\bin and %ANT_HOME%\bin to your PATH environment variable
    (or equivalent syntax for your operating system)
  - closed and restarted the command line since making any environment variable changes.
  - installed at least Eclipse 3.4.
  - (Optional) install a Subversion (aka SVN) client (http://subversion.tigris.org/)

2. Download a copy of the BlazeDS source code from opensource.adobe.com
   or check out a version of the BlazeDS source tree from Subversion
   i.e.  svn checkout http://opensource.adobe.com/svn/opensource/blazeds/trunk c:\dev

3. Also download a copy of the Flex SDK source code from opensource.adobe.com
   or check out a version of the Flex SDK source tree from Subversion
   i.e. svn checkout http://opensource.adobe.com/svn/opensource/flex/sdk/trunk c:\dev

4. Run "ant clean main" from the BlazeDS home directory.

5. Install Flash Builder 4 plugin for Eclipse.

6. Open Eclipse/Flash Builder, start a new workspace in the location of your choice.

7. Define a "Linked Resource Path Variables" through the following menu
sequence:

  Window > Preferences... > General > Workspace > Linked Resources

BLAZEDS_HOME = the root of the BlazeDS source tree
               For instance: C:\dev\blazeds\trunk

FLEX_SDK        = your sync of the relevant sdk branch, e.g
                      c:\dev\flex\sdk\trunk

Apply your changes.

8. Define a "Java Build Classpath Variables" through the following menu
sequnce:

  Window > Preferences... > Java > Build Path > Classpath Variables

BLAZEDS_HOME = the root of the BlazeDS source tree
               For instance: C:\dev\blazeds\trunk

FLEX_SDK        = your sync of the relevant sdk branch, e.g
                      c:\dev\flex\sdk\trunk

Apply your changes.

9. Ensure that you have the correct JRE configured in Eclipse through the
following menu sequence:

  Window > Preferences... > Java > Installed JREs

At the very least a valid JRE should be pointed to your JDK 1.5.0 installation
(on selecting a location Eclipse should fill in the rest of the information)
and then as a best practice be sure to edit the name so that the particular
update of the JDK isn't considered, e.g.

  Name = jdk1.5.0
  Location = C:\dev\java\jdk1.5.0_07

Ensure that the compile compliance level is 1.4 (we support 1.4 except for selected projects that are compiled with 1.5):

  Window > Preferences... > Java > Compiler:  select 1.4 on the first 3 fields.

Apply your changes.

10. Change all file types to insert 4 spaces for an indent instead of tab
characters. These changes have to be made in several locations depending on
which plug-ins you have installed. At a minimum, check the following locations
by navigating through these menu sequence, all under Window > Preferences ...

Ant > Editor > Formatter
  - ensure Tab size is 4
  - uncheck use tab character instead of spaces
  - apply your changes

Flex > Editors
  - select the Indent using spaces radio button
  - ensure the indent and tab sizes are set to 4 spaces
  - apply your changes

Java > Code Style > Formatter
  - import the formatting settings from the source tree:
        blazeds/trunk/development/eclipse/java/formatter.xml
  - After importing the enterprise formatting rules, double check that in the
    Indentation tab, select the Tab policy to "Spaces only"
  - ensure the tab and indentation sizes sare set to 4 spaces
  - apply your changes

11. Import existing eclipse projects from your enteprise workspace by navigating
to the following menu sequence:

  File > Import... > General > Existing Projects into Workspace

Select the root directory as your sync of the blazeds\trunk\development\eclipse\projects directory
and then select the projects you want to add to your workspace.

(Note that you don't need to select "Copy projects into workspace" if you wish
to work off your sync of the projects... though be prepared to handle any
changes that get checked in for these project definitions).
