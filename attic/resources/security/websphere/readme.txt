To set up custom authentication for BlazeDS samples on WebSphere Application Server:

1) Enable custom authentication per the WebSphere Application Server documentation.

2) Create "sampleusers" group by adding the following to the groups.props:
sampleusers:777:sampleuser:

3) Create "sampleuser" user by adding the following to the user.props:
sampleuser:sampleuser:988:777:


