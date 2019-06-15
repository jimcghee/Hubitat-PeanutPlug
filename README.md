# PeanutPlug
Securifi Peanut Plug driver originally by pakmanw@sbcglobal.net for SmartThings ported to Hubitat by JIMcGhee

Current version 2.50 with changes by JiMcGhee.

PREAMBLE  
Most importantly, this is not my original work.  It was originally written by pakmanwg for use on SmartThings.  
The original code is on GitHub at:

https://github.com/pakmanwg/smartthings-peanut-plug/tree/master/devicetypes/pakmanwg/peanut-plug.src

FUNCTION  
This plugin is used to control a Securifi Peanut Plug Power Meter.  It also reports power, voltage, and current.

Note: 
The peanut plugs required a firmware update to report power, even on ST and you can only update the firmware
using an Almond Securifi router.

Known Problems:   
1. The plugs I tested would not read a power value of less than about 2.6 watts.  
2. The reporting criteria have not been thoroughly tested.  If anyone spots any problems  
   I would love to hear about then.

GET THE UPDATED FILES  
The project is at:  
https://github.com/jimcghee/Hubitat-PeanutPlug  

Click the green 'Clone or Download' button and then the 'Download ZIP'.  
Extract the files just downloaded to prepare for the installation.  
or  
Simply copy/paste the code directly from the GitHub file.  

INSTALLATION  
1. Open your Hubitat Elevation web page and goto 'Drivers Code'. 
2. Click 'New Driver'.
3. Paste the contents of PeanutPlug.groovy in the form and click 'Save'.

The new driver will be named 'Peanut Plug' and will now be available in Device Information/Type/User.

SET UP DISCOVERED PLUGS  
1. Install the plug on a Securifi Almond router and update the firmware. 
   If your plug does not report power/voltage/current it probably needs a firmware update. 
2. Exclude the plug from the Almond router.
3. Discover/Include the plug on your Hubitat as a ZigBee device in the normal way.  If it fails to include, you might try to reset it:  
        a. Reset the Plug by pressing the pairing button (button with wifi symbol) for 10 seconds and then releasing it.  
        b. Remove the Plug from the power outlet and plug it back in  
4. Name the plug and click 'Save'.
5. Go to the 'Devices' list, find your new device and open it.
6. Scroll down to 'Device Information' and click on 'Type'.
7. Scroll down past 'User' and click 'Peanut Plug'.
8. Scroll up to 'Preferences' and adjust the values to your liking.
9. Enjoy your new Peanut Plug (optional).

Of course, this comes with the standard disclaimers:  
Use this at your own risk.  
YMMV.  
Performed on a closed track by professional drivers.  
Kids, don't try this at home,  
AND  
if your dog gets eaten by Zombies tonight, don't blame me, you were WARNED!!!  ;-)  
