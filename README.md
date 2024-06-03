# mosquitto-spring-mTLS
An example how to create a mTLS client to a MQTT server as a bean in spring boot. This allows you to still have your own configured security


There are two files given in this exmaple. Since I extracted it from a Spring
application I added the first file as the executable file of the spring 
application.

The second file is the functions that aid the mutualTLS verification with the 
server.

## Good to knows
First of all, you should have a spring application with the
`spring-integration-mqtt` dependency.

Second, the file paths are of course different from your application.

Third, I do not have this setup running in any live environment so dont worry
about the mentioned settings within the example files ;)
