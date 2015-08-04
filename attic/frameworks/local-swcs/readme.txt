Check in "local" swcs to the FlexSDK3 or FledSDK4 directory that is under this directory to 
override any "accepted" swcs with a matching name in the /enterprise/frameworks/libs directory 
as part of an enterprise build.

SWCs that were compiled against Flex SDK 3 should go in the FlexSDK3 directory and SWCs that were 
compiled against Flex SDK 4 should go in the FlexSDK4 directory. Ideally, if you are checking in a 
"local" version of the SWC, it should be compiled against both Flex SDK 3 and Flex SDK 4 so the "local"
version will be used when LCDS is built with both Flex SDK 3 and Flex SDK 4. 

To enable this override, set the "use.local.swcs" property in the /enterprise/build.properties 
file to true.

The build process unpacks the contents of /resources/flex_sdk/flex_sdk_3.zip into the
frameworks/libs directory as part of a build, but some of your changes may be part of 
rpc.swc in which case you'll need to use a current rpc.swc built from SDK 
mainline for the enterprise build rather than the currently approved rpc.swc in the 
flex_sdk_3.zip or flex_sdk_4.zip files.

While rpc.swc will be the most common swc that this use case applies to, you can override any
of the currently approved swcs by placing your updated version in the frameworks/local-swcs/
directory (or an appropriate sub-directory). Your frameworks/local-swcs/ directory and 
subdirectories need to be symmetrical to the /frameworks/libs directory structure so that the 
overlay process overwrites the proper files. If any local swcs are present in perforce when you 
get to this point, open them for delete, add only the swcs that you need to override for your 
specific changelist and proceed.

After rebuilding and running checkintests with your updated local swcs, submit your changelist
containing your changes to rpc code in SDK mainline along with your changes to fds code in
enterprise mainline as well as your local swc override(s) in the /local-swcs/ directory and the
build.properties file with use.local.swcs=true.

This will allow the build to run and complete successfully on the build machine.

Approval builds will always revert the "use.local.swcs" property to false and will build using 
an approved set of swcs (that will pick up the changes you've now committed for non-enterprise 
swcs).