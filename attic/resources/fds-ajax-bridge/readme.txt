To use AJAX Data Services in an AJAX application, you must include the following JavaScript
libraries in script tags on the HTML page:

* FDMSLib.js Include this file in any HTML page that requires BlazeDS. It contains 
the definition of BlazeDS APIs as JavaScript.  This file requires the
FABridge.js file.

* FABridge.js This file provides the Flex AJAX Bridge (FABridge) library, the JavaScript gateway
to Flash Player. 

The following ActionScript libaries are required to compile the SWF file:
* FABridge.as
* FDMSBase.as

You use the following command line (on a single line) to compile the SWF file:

mxmlc.exe --verbose-stacktraces -services=<path_to_services-config.xml_for_your_app>\services-config.xml  -o=<path_to_store_compiled_swf>\FDMSBridge.swf <path_to_FDMSBridge.as>\FDMSBridge.as

This command line assumes that you have added mxmlc to the system path, or run the command from
the sdk\bin directory. To initialize the library, you call a convenience method, FDMSLib.load().
This method creates the appropriate HTML to embed Flash Player and load the specified shim SWF file. This SWF file is typically compiled using the FDMSBridge.as application file, but can be any SWF file or MXML application that contains the FABridge and references the appropriate BlazeDS classes. Once Flash Player loads the shim SWF file containing the bridge the specified callback will be invoked to notify the application Data Services are available and ready for use.

If your services-config.xml changes, you'll need to recompile FDMSBridge.swf.

It is also possible to specify ChannelSets directly from JavaScript.  In this case, you would not need to compile FDMSBridge.swf with the -services argument.