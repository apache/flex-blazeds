After compiling the test case for BLZ-594 with Flex SDK 3.5 and targetting Flash Player 9 you will get an "Index out of bounds" or "Unsupported AMF type: 17" or similar exception when you run the SWF since it doesn't know how to handle Dictionary/Hashtable type. 

The fix for the bug was to add legacy Dictionary support to the server serialization via a legacy-dictionary serialization flag. 

To test the bug fix, compile the test case with Flex SDK 3.5 and target Flash Player 9 as above only this time add the following serialization property to the data-amf channel and restart the BlazeDS server.  

<!-- legacy-dictionary is false by default. When true, during server to
client serialization, instances of java.util.Dictionary are serialized
as anonymous Object rather than flash.utils.Dictionary type.
-->
<legacy-dictionary>false</legacy-dictionary>

When you run the test case this time you shouldn't get any exceptions on the client. 