/**
 *  Peanut Plug
 *
 *  Copyright 2017 pakmanw@sbcglobal.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Peanut Plug
 *
 *  Author: pakmanw@sbcglobal.net
 *
 *  Change Log
 *  2017-09-17 - v01.01 Created
 *  2018-03-01 - v01.02 fix power accuracy issue
 *  2018-12-23 - v01.03 merging jamesham change to get the calibrated attr from peanut plug,
 *                      add support for new smartthings app
 *  2019-01-17 - v01.04 merging jamesham retain state code
 *  2019-05-24 - V02.00 Converted to run on Hubitat.  Modified to display power correctly.
 *  2019-06-14 - V02.50 Added user variables Power Change Report Value, Power Reporting Interval,
 *                      Current Change Report Value, Current Reporting Interval, Voltage Reporting Interval, and
 *                      Debug Logging?
 *  2019-06-25 - V02.60 Removed deprecated ST functions and tiles.  Enabled Energy reporting (in W*h). *
 */

import hubitat.zigbee.zcl.DataType

metadata
{
	definition (name: "Peanut Plug", namespace: "pakmanwg", author: "pakmanw@sbcglobal.net", ocfDeviceType: "oic.d.switch",
		vid: "generic-switch-power-energy") 
    {
		capability "Configuration"
        capability "Energy Meter"
        capability "Polling"
        capability "Power Meter"
        capability "Refresh"
		capability "Switch"		
		capability "Voltage Measurement"
        
		attribute "current","number"

		command "reset"
       
		fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0004, 0005, 0006, 0B04, 0B05",
			        outClusters: "0000, 0001, 0003, 0004, 0005, 0006, 0019, 0B04, 0B05"
    }

	preferences 
    {
        section 
        {
		    input (
                name: "RetainState",
                type: "bool",
                title: "Retain State?",
                description: "Retain switch state on power loss?", 
                required: false, 
                displayDuringSetup: false, 
                defaultValue: true
            )

		    input (
                name: "MinPowerReportTime",
                type: "number",
                title: "Power Reporting Interval",
                description: "Report Power no more than every XXX seconds. (1 - 7200)",
                submitOnChange: true,
                required: true,
                range: "1..7200",
                defaultValue: 5
            )
            
            input (
                name: "ReportablePowerChange",
                type: "number",
                title: "Minimum Power Change Report Value",
                description: "Report Power change greater than XXX watts. (0 - 1000)",
                submitOnChange: true,
                required: true,
                range: "0..1000",
                defaultValue: 1
            )
          
		    input (
                name: "MinCurrentReportTime",
                type: "number",
                title: "Current Reporting Interval",
                description: "Report Current no more than every XXX seconds. (1 - 7200)",
                submitOnChange: true,
                required: true,
                range: "1..7200",
                defaultValue: 5
            )
            
            input (
                name: "ReportableCurrentChange",
                type: "number",
                title: "Minimum Current Change Report Value",
                description: "Report Current change greater than XXX amps. (0 - 1000)",
                submitOnChange: true,
                required: true,
                range: "0..1000",
                defaultValue: 1
            )
            
		    input (
                name: "MinVoltageReportTime",
                type: "number",
                title: "Voltage Reporting Interval",
                description: "Report Voltage no more than every XXX seconds. (1 - 7200)",
                submitOnChange: true,
                required: true,
                range: "1..7200",
                defaultValue: 5
            )
            
            input (
                name: "ReportableVoltageChange",
                type: "number",
                title: "Minimum Voltage Change Report Value",
                description: "Report Voltage change greater than XXX volts. (0 - 1000)",
                submitOnChange: true,
                required: true,
                range: "0..1000",
                defaultValue: 1
            )
            
		    input (
                name: "DebugLogging",
                type: "bool",
                title: "Debug Logging",
                description: "Enable Debug Logging?", 
                required: true, 
                displayDuringSetup: false, 
                defaultValue: true
            )
        }
	}
}

def log(msg) 
{
	if (DebugLogging)
    {
		log.debug(msg)	
	}
}

// Parse incoming device messages to generate events
def parse(String description) 
{
    //	zigbee.ELECTRICAL_MEASUREMENT_CLUSTER is 0x0B04
	log "description is: $description"
	def event = zigbee.getEvent(description)
	if (event) 
    {
	    log "event name is $event.name"
       	sendEvent(event)
	}
    else if (description?.startsWith("read attr -")) 
    {
		def descMap = zigbee.parseDescriptionAsMap(description)
		log "Desc Map: $descMap"
		if (descMap.clusterInt == zigbee.ELECTRICAL_MEASUREMENT_CLUSTER)
        {
			def intVal = Integer.parseInt(descMap.value,16)
            
            /*
            http://www.zigbee.org/wp-content/uploads/2014/10/07-5123-06-zigbee-cluster-library-specification.pdf

            Identifier Name            Type  Range                   Access Default    M/O
            0x0000     MeasurementType map32 0x00000000 – 0xFFFFFFFF R      0x00000000 M

            Id     Name             Type     Range         Acc   Default M/O
            0x0505 RMSVoltage       uint16 0x0000 – 0xFFFF R     0xFFFF O
            0x0506 RMSVoltageMin    uint16 0x0000 – 0xFFFF R     0xFFFF O
            0x0507 RMSVoltageMax    uint16 0x0000 – 0xFFFF R     0xFFFF O
            0x0508 RMSCurrent       uint16 0x0000 – 0xFFFF R     0xFFFF O
            0x0509 RMSCurrentMin    uint16 0x0000 – 0xFFFF R     0xFFFF O
            0x050A RMSCurrentMax    uint16 0x0000 – 0xFFFF R     0xFFFF O
            0x050B ActivePower      int16 -32768 – 32767   R     0x8000 O

            Id     Name                 Type     Range           Acc   Default M/O
            0x0600 ACVoltageMultiplier  uint16   0x0001 – 0xFFFF R     0x0001 O
            0x0601 ACVoltageDivisor     uint16   0x0001 – 0xFFFF R     0x0001 O
            0x0602 ACCurrentMultiplier  uint16   0x0001 – 0xFFFF R     0x0001 O
            0x0603 ACCurrentDivisor     uint16   0x0001 – 0xFFFF R     0x0001 O
            0x0604 ACPowerMultiplier    uint16   0x0001 – 0xFFFF R     0x0001 O
            0x0605 ACPowerDivisor       uint16   0x0001 – 0xFFFF R     0x0001 O
            */
                                          
            switch(descMap.attrInt)
            {
                case 0x0000:
                    log "MeasurementType: ${intVal}"
                    break
                case 0x0600:
                    log.info "ACVoltageMultiplier $intVal"
				    state.voltageMultiplier = intVal
                    break
                case 0x0601:
				    log.info "ACVoltageDivisor $intVal"
				    state.voltageDivisor = intVal
                    break
                case 0x0602:
                	log.info "ACCurrentMultiplier $intVal"
				    state.currentMultiplier = intVal
                    break
                case 0x0603:
                    log.info "ACCurrentDivisor $intVal"
				    state.currentDivisor = intVal
                    break
                case 0x0604:
                    log.info "ACPowerMultiplier $intVal"
				    state.powerMultiplier = intVal
                    break
                case 0x0605:
                    log.info "ACPowerDivisor $intVal"
				    state.powerDivisor = intVal
                    break
                case 0x0505:
                    def voltageValue = ((intVal as Double) * getVoltageMultiplier()).round(4)
                    log "Raw Voltage: ${intVal}"
                    log "Voltage Multiplier: ${getVoltageMultiplier()}"
                    log "RMS Voltage: ${voltageValue}"
				    state.voltage = voltageValue
				    sendEvent(name: "voltage", value: voltageValue)
                    break
                case 0x0508:
                    def currentValue = ((intVal as Double) * getCurrentMultiplier()).round(4)
                    log "Raw Current: ${intVal}"
                    log "Current Multiplier: ${getCurrentMultiplier()}"
                    log "RMS Current: ${currentValue}"
				    state.current = currentValue
				    sendEvent(name: "current", value: currentValue)
                    break
                case 0x050B:
                    // first, calculate and update power
                    def powerValue = ((intVal as Double) * getPowerMultiplier()).round(4)
                    log "Raw Power: ${intVal}"
                    log "Power Multiplier: ${getPowerMultiplier()}"
                    log "Power: ${powerValue}"				    
				    sendEvent(name: "power", value: powerValue)                
                    // then, calculate and update energy (in W*h)
                    def newTime = now()
                    // note that time is Unix epoch in msec, so this converts the difference to seconds and then hours
                    def energyValue = (state.energyValue + ((newTime as Double) - (state.time as Double)) * state.powerValue / (1000*3600)).round(4)
                    log "Energy: ${energyValue} W*h"
                    sendEvent(name: "energy", value: energyValue)
                
                    // finally, update state
                    state.powerValue = powerValue
                    state.energyValue = energyValue
                    state.time = newTime
                    break
                default:
                    log "Unknown ELECTRICAL_MEASUREMENT_CLUSTER entry ${descMap.attrInt}"
            }
		}
        else
        {
			log.warn "Not an electrical measurement"
		}
        
	} 
    else 
    {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log zigbee.parseDescriptionAsMap(description)
	}
}

def installed()
{
	reset()
	configure()
	refresh()
}

def off()
{
	zigbee.off()
}

def on()
{
	zigbee.on()
}

def refresh()
{
    //log "in Peanut Plug refresh()"
	Integer reportIntervalMinutes = 5
	setRetainState() +
	zigbee.onOffRefresh() +
	zigbee.electricMeasurementPowerRefresh() +
	zigbee.onOffConfig(0, reportIntervalMinutes * 60) +
	electricMeasurementPowerConfig() +
	voltageMeasurementRefresh() +
	voltageMeasurementConfig() +
	currentMeasurementRefresh() +
	currentMeasurementConfig() +
    zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0600) +
	zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0601) +
	zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0602) +
	zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0603) +
	zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0604) +
	zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0605)  
}

def voltageMeasurementConfig()
{
    log.info "Voltage Report Time: ${MinVoltageReportTime}"
	zigbee.configureReporting(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 
                            0x0505, 
                            DataType.UINT16, 
                            MinVoltageReportTime as Integer,    // Min Voltage reporting time in seconds
                            7200,                               // Max Voltage reporting time in seconds
                            ReportableVoltageChange as Integer) // Min change to report.  Units unknown.
}

def voltageMeasurementRefresh()
{
	zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0505);
}

def currentMeasurementConfig()
{
    log.info "Current Report Time: ${MinCurrentReportTime}"
    zigbee.configureReporting(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 
                            0x0508, 
                            DataType.UINT16, 
                            MinCurrentReportTime as Integer,    // Min Current reporting time in seconds
                            7200,                               // Max Current reporting time in seconds
                            ReportableCurrentChange as Integer) // Min change to report.  Units unknown.
}

def currentMeasurementRefresh()
{
    zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0508);
}

def electricMeasurementPowerConfig()
{
    log.info "Power Report Time: ${MinPowerReportTime}"
	zigbee.configureReporting(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 
                            0x050B, 
                            DataType.INT16, 
                            MinPowerReportTime as Integer,              // Min Power reporting time in seconds
                            7200,                                       // Max Power reporting time in seconds
                            ReportablePowerChange as Integer)           // Min change to report.  Units unknown.
}


def getCurrentMultiplier() 
{
	if (state.currentMultiplier && state.currentDivisor) 
    {
		return (state.currentMultiplier / state.currentDivisor)
	} 
    else
    {
        // typical value
		return 0.001831
	}
}

def getVoltageMultiplier() 
{
	if (state.voltageMultiplier && state.voltageDivisor)
    {
		return (state.voltageMultiplier / state.voltageDivisor)
	}
    else
    {
        // typical value
		return 0.0045777
	}
}

def getPowerMultiplier() 
{
	if (state.powerMultiplier && state.powerDivisor) 
    {
		return (state.powerMultiplier / state.powerDivisor)
	} 
    else 
    {
        // typical value
		return 0.277
	}
}

def configure() 
{
	//log "in Peanut Plug configure()"
	setRetainState()
}

def updated()
{
    //log "in Peanut Plug updated()"
    log.info "${device.displayName}.updated()"
	// updated() doesn't have it's return value processed as hub commands, so we have to send them explicitly
	def cmds = setRetainState()
	cmds.each{ sendHubCommand(new hubitat.device.HubAction(it)) }
}

def ping() 
{
	return zigbee.onOffRefresh()
}

def setRetainState() 
{
	log "Setting Retain State: $RetainState"
	if (RetainState == null || RetainState) 
    {
		if (RetainState == null) 
        {
			log.warn "RetainState is null, defaulting to 'true' behavior"
		}
		return zigbee.writeAttribute(0x0003, 0x0000, DataType.UINT16, 0x0000)
	} 
    else 
    {
		return zigbee.writeAttribute(0x0003, 0x0000, DataType.UINT16, 0x1111)
	}
}

def reset() 
{
	log "in Peanut Plug reset()"
    
    state.voltageMultiplier = 0.0
    state.voltageDivisor = 0.0
    state.voltage = 0.0
    
    state.currentMultiplier = 0.0
    state.currentDivisor = 0.0
    state.current = 0.0
        
    state.powerMultiplier = 0.0
    state.powerDivisor = 0.0
    state.powerValue = 0.0
	state.energyValue = 0.0

	state.time = now()
    
    sendEvent(name: "voltage", value: 0.0)
    sendEvent(name: "current", value: 0.0)
    sendEvent(name: "power", value: 0.0)
	sendEvent(name: "energy", value: 0.0)
    
    // sanity check MeasurementType, for debugging only
    zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0000)  
}
