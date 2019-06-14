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
 */

import hubitat.zigbee.zcl.DataType

metadata {
	definition (name: "Peanut Plug", namespace: "pakmanwg", author: "pakmanw@sbcglobal.net", ocfDeviceType: "oic.d.switch",
		vid: "generic-switch-power-energy") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"
		capability "Light"
		capability "Health Check"
		capability "Voltage Measurement"
        
		attribute "current","number"

		command "reset"
       
		fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0004, 0005, 0006, 0B04, 0B05",
			outClusters: "0000, 0001, 0003, 0004, 0005, 0006, 0019, 0B04, 0B05"
	}

	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		valueTile("power", "device.power") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy") {
			state "default", label:'${currentValue} kWh'
		}
		valueTile("voltage", "device.voltage") {
			state "default", label:'${currentValue} V'
		}
		valueTile("current", "device.current") {
			state "default", label:'${currentValue} A'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch","power","energy","voltage","current"])
		details(["switch","power","energy","voltage","current","refresh","reset"])
	}

	preferences {
        section {
		    input (
                name: "RetainState",
                type: "bool",
                title: "Retain State?",
                description: "Retain state on power loss?", 
                required: false, 
                displayDuringSetup: false, 
                defaultValue: true
            )
i/*		    input (
                name: "PowerReportValueChange",
                type: "enum",
                title: "Power Report Value Change",
                submitOnChange: true,
                options: ["No Selection","No Report",".1 Watt",".2 Watts",".3 Watts",".4 Watts",".5 Watts",
                            "1 Watt","2 Watts","3 Watts","4 Watts","5 Watts","10 Watts","25 Watts",
                            "50 Watts","75 Watts","100 Watts","150 Watts","200 Watts","250 Watts","300 Watts","400 Watts",
                            "500 Watts","750 Watts","1000 Watts"],
                required: true,
                Multiple: false
            )
		    input (
                name: "PowerReportPercentChange",
                type: "enum",
                title: "Power Report Percentage Change",
                submitOnChange: true,
                options: ["No Selection","No Report","1%","2%","3%","4%","5%","10%","15%","20","25%","30%","40%","50%","75%","100%"],
                required: true,
                Multiple: false
            )
		    input (
                name: "PowerReportingInterval",
                type: "enum",
                title: "Power Reporting Interval",
                submitOnChange: true,
                options: ["No Selection","No Report","5 Seconds","10 Seconds","15 Seconds","30 Seconds","45 Seconds","1 Minute",
                            "2 Minutes","3 Minutes","4 Minutes","5 Minutes","10 Minutes","15 Minutes","30 Minutes","45 Minutes",
                            "1 Hour","2 Hours","3 Hours","5 Hours"],
                required: true,
                Multiple: false
            ) */
		    input (
                name: "ReportablePowerChange",
                type: "number",
                title: "Power Change Report Value",
                description: "Report Power change greater than XXX watts. (.1 - 1000)",
                submitOnChange: true,
                required: true,
                range: "0..1000",
                defaultValue: 5
            )
		    input (
                name: "MinPowerReportTime",
                type: "number",
                title: "Power Reporting Interval",
                description: "Report Power no more than every XXX seconds. (1 - 7200)",
                submitOnChange: true,
                required: true,
                range: "1..7200",
                defaultValue: 60
            )
		    input (
                name: "ReportableCurrentChange",
                type: "number",
                title: "Current Change Report Value",
                description: "Report Current change greater than XXX milliamps. (1 - 1000)",
                submitOnChange: true,
                required: true,
                range: "1..1000",
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
                defaultValue: 60
            )
		    input (
                name: "MinVoltageReportTime",
                type: "number",
                title: "Voltage Reporting Interval",
                description: "Report Voltage no more than every XXX seconds. (1 - 7200)",
                submitOnChange: true,
                required: true,
                range: "1..7200",
                defaultValue: 60
            )
		    input (
                name: "DebugLogging",
                type: "bool",
                title: "Debug Logging",
//                description: "Enable Debug Logging", 
                required: true, 
                displayDuringSetup: false, 
                defaultValue: true
            )
        }
	}
}

def log(msg) {
	if (DebugLogging) {
		log.debug(msg)	
	}
}

// Parse incoming device messages to generate events
def parse(String description) {

//	zigbee.ELECTRICAL_MEASUREMENT_CLUSTER is 2820
	log "description is: $description"
	def event = zigbee.getEvent(description)
	if (event) {
	    log "event name is $event.name"
		if (event.name == "power") {
			def powerValue
			powerValue = (event.value as Integer) * getPowerMultiplier()
			sendEvent(name: "power", value: powerValue)
			def time = (now() - state.time) / 3600000 / 1000
			state.time = now()
			log "powerValues is $state.powerValue"
			state.energyValue = state.energyValue + (time * state.powerValue)
			state.powerValue = powerValue
			// log "energyValue is $state.energyValue"
			sendEvent(name: "energy", value: state.energyValue)
		} else {
			sendEvent(event)
		}
	} else if (description?.startsWith("read attr -")) {
		def descMap = zigbee.parseDescriptionAsMap(description)
		log "Desc Map: $descMap"
		if (descMap.clusterInt == zigbee.ELECTRICAL_MEASUREMENT_CLUSTER) {
			def intVal = Integer.parseInt(descMap.value,16)
			if (descMap.attrInt == 0x0600) {
				log.info "ACVoltageMultiplier $intVal"
				state.voltageMultiplier = intVal
			} else if (descMap.attrInt == 0x0601) {
				log.info "ACVoltageDivisor $intVal"
				state.voltageDivisor = intVal
			} else if (descMap.attrInt == 0x0602) {
				log.info "ACCurrentMultiplier $intVal"
				state.currentMultiplier = intVal
			} else if (descMap.attrInt == 0x0603) {
				log.info "ACCurrentDivisor $intVal"
				state.currentDivisor = intVal
			} else if (descMap.attrInt == 0x0604) {
				log.info "ACPowerMultiplier $intVal"
				state.powerMultiplier = intVal
			} else if (descMap.attrInt == 0x0605) {
				log.info "ACPowerDivisor $intVal"
				state.powerDivisor = intVal
			} else if (descMap.attrInt == 0x0505) {
				def voltageValue = intVal * getVoltageMultiplier()
				log "Voltage ${voltageValue}"
				state.voltage = $voltageValue
				sendEvent(name: "voltage", value: voltageValue)
			} else if (descMap.attrInt == 0x0508) {
				def currentValue = String.format("%.4f", (intVal * getCurrentMultiplier()))
				log "Current ${currentValue}"
				state.current = $currentValue
				sendEvent(name: "current", value: currentValue)
			} else if (descMap.attrInt == 0x050B) {
				def powerValue = String.format("%.4f", (intVal * getPowerMultiplier()))
//				log "Power ${intVal}, ${getPowerMultiplier()}"
				log "Power ${powerValue}"
				state.powerValue = $powerValue
				sendEvent(name: "power", value: powerValue)
			}
		} else {
			log.warn "Not an electrical measurement"
		}
	} else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log zigbee.parseDescriptionAsMap(description)
	}
}

def installed() {
	reset()
	configure()
	refresh()
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def refresh() {
	Integer reportIntervalMinutes = 5
	setRetainState() +
	zigbee.onOffRefresh() +
//	zigbee.simpleMeteringPowerRefresh() +
//	simpleMeteringPowerRefresh() +
	zigbee.electricMeasurementPowerRefresh() +
	zigbee.onOffConfig(0, reportIntervalMinutes * 60) +
//	zigbee.simpleMeteringPowerConfig() +
//	simpleMeteringPowerConfig() +
//	zigbee.electricMeasurementPowerConfig() +
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

//def electricMeasurementPowerConfig(
//                                minReportTime=10,           // in seconds
//                                maxReportTime=600,          // in seconds
//                                reportableChange=0x0005)    // in .1 Watts 
def electricMeasurementPowerConfig()    // in .1 Watts 
{
    def MinPowerValueA
    MinPowerValue = (((ReportablePowerChange as float) * 10) as Integer)
	log.info "Power Report Time: $MinPowerReportTime, Power Report Value: $MinPowerValue"
	zigbee.configureReporting(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 
                            0x050B, 
                            DataType.INT16, 
                            MinPowerReportTime as Integer,              // Min Power reporting time in seconds
                            7200,                                       // Max Power reporting time in seconds
                            MinPowerValue as Integer)                   // Min Reportable Power Change in Tenths of Watts
}

//def currentMeasurementConfig(minReportTime=60, maxReportTime=600, reportableChange=0x0005) {
//	zigbee.configureReporting(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0508, DataType.UINT16, minReportTime, maxReportTime, reportableChange)
//}

def currentMeasurementConfig() {
	log.info "Current Report Time: $MinCurrentReportTime, Current Report Value: $ReportableCurrentChange"
	zigbee.configureReporting(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 
                            0x0508, 
                            DataType.INT16, 
                            MinCurrentReportTime as Integer,    // Min Current reporting time in seconds
                            7200,                               // Max Current reporting time in seconds
                            ReportableCurrentChange as Integer) // Min Reportable Current Change in Tenths of Watts
}

def currentMeasurementRefresh() {
	zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0508);
}

def voltageMeasurementConfig() {
	log.info "Voltage Report Time: $MinVoltageReportTime"
	zigbee.configureReporting(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 
                            0x0505, 
                            DataType.INT16, 
                            MinVoltageReportTime as Integer,    // Min Voltage reporting time in seconds
                            7200,                               // Max Voltage reporting time in seconds
                            0x0030)                             // Min Reportable Voltage Change in Tenths of Volts
}

def voltageMeasurementRefresh() {
	zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x0505);
}

def getCurrentMultiplier() {
	if (state.currentMultiplier && state.currentDivisor) {
		return (state.currentMultiplier / state.currentDivisor)
	} else {
		return 0.001831
	}
}

def getVoltageMultiplier() {
	if (state.voltageMultiplier && state.voltageDivisor) {
		return (state.voltageMultiplier / state.voltageDivisor)
	} else {
		return 0.0045777
	}
}

def getPowerMultiplier() {
	if (state.powerMultiplier && state.powerDivisor) {
		return (state.powerMultiplier / state.powerDivisor)
	} else {
		return 0.277
	}
}

def configure() {
	log "in configure()"
	return configureHealthCheck() + setRetainState()
}

def configureHealthCheck() {
	Integer hcIntervalMinutes = 12
	sendEvent(name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	return refresh()
}

def updated() {
    log.info "${device.displayName}.updated()"
	log "in updated()"
	// updated() doesn't have it's return value processed as hub commands, so we have to send them explicitly
	def cmds = configureHealthCheck() + setRetainState()
	cmds.each{ sendHubCommand(new hubitat.device.HubAction(it)) }
}

def ping() {
	return zigbee.onOffRefresh()
}

def setRetainState() {
	log "Setting Retain State: $RetainState"
	if (RetainState == null || RetainState) {
		if (RetainState == null) {
			log.warn "RetainState is null, defaulting to 'true' behavior"
		}
		return zigbee.writeAttribute(0x0003, 0x0000, DataType.UINT16, 0x0000)
	} else {
		return zigbee.writeAttribute(0x0003, 0x0000, DataType.UINT16, 0x1111)
	}
}

def reset() {
	log "Reset"
	state.energyValue = 0.0
	state.powerValue = 0.0
	state.voltage = 0.0
	state.current = 0.0
	state.time = now()
	sendEvent(name: "energy", value: state.energyValue)
}
