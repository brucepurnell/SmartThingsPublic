/**
 *  Nest Thermostat
 *
 *  Author: juano23@gmail.com
 *  Date: 2013-07-11
 */
 
//Nest Login
def nest() {
    def user = "brucepurnell@me.com"
	def password = "14willuse"
	return "user=$user&password=$password"
} 
 
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Nest Thermostat", author: "brucepurnell@me.com") {
	}


    tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit:"F",
				backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
				]
			)
		}
		standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "cool", label:'Cool', action:"thermostat.heat", icon: "st.Home.home1"
			state "heat", label:'Heat', action:"thermostat.cool", icon: "st.Home.home1"
		}    
		valueTile("coolingSetpoint", "device.coolingSetpoint") {
			state "default", label:'${currentValue}°', unit:"F", 
            backgroundColors:[
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]            
		}        
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 3, inactiveLabel: false) {
			state "setCoolingSetpoint", label:'Set temperarure to', action:"thermostat.setCoolingSetpoint", 
            backgroundColors:[
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]               
		}
		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
			state "fanAuto", label:'Fan Auto', action:"thermostat.fanOn", icon: "st.Lighting.light24"
			state "fanOn", label:'Fan On', action:"thermostat.fanAuto", icon: "st.Lighting.light24"
		}
        standardTile("presence", "device.presence", inactiveLabel: false, decoration: "flat") {
            state "present", label:'${name}', action:"away", icon: "st.Home.home2"
            state "away", label:'${name}', action:"present", icon: "st.Transportation.transportation5"
        }
        standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "default", label:'${currentValue}% humidity', unit:"humidity"
		}          
		main "temperature"
		details(["temperature", "coolingSetpoint", "mode", "coolSliderControl", "fanMode", "humidity", "presence","refresh"])
	}
}

// Command Implementations
def poll() {
        def credentials = nest()
        httpGet(uri:"http://api.thinkmakelab.com/nest/report.php?$credentials", contentType:"application/json")
        {resp ->
         if (resp.data['current_state'].manual_away == true) {
            sendEvent(name:"presence", value: 'away')
         }   
         else {
            sendEvent(name:"presence", value: 'present')
         } 
         sendEvent(name:"humidity", value: resp.data['current_state'].humidity)
         def BigDecimal nesttemp = new BigDecimal(resp.data['current_state'].temperature).setScale(0,5)
         def BigDecimal targettemp = new BigDecimal(resp.data['target'].temperature).setScale(0,5)
         sendEvent(name:"temperature", value: nesttemp)
         sendEvent(name:"thermostatMode", value: resp.data['current_state'].mode)
         if (resp.data['current_state'].fan == false) {
            sendEvent(name:"thermostatFanMode", value: "fanAuto")
         }   
         sendEvent(name:"coolSliderControl", value: targettemp)
         sendEvent(name:"coolingSetpoint", value: targettemp)
        }  
}	

def setCoolingSetpoint(degreesF) {
	def tempfinal = degreesF
	def credentials = nest()
	delayBetween([
		httpPost("http://api.thinkmakelab.com/nest/actions.php", "action=settemp&temp=$tempfinal&$credentials"),
		sendEvent(name:"coolingSetpoint", value: tempfinal)
	])         
}

def away() {
	def credentials = nest()
	delayBetween([
		httpPost("http://api.thinkmakelab.com/nest/actions.php", "action=away&$credentials"),
		sendEvent(name:"presence", value: 'away')
	]) 
}

def present() {
	def credentials = nest()
	delayBetween([
    	httpPost("http://api.thinkmakelab.com/nest/actions.php", "action=present&$credentials"),
		sendEvent(name:"presence", value: 'present')
	]) 
}

def heat() {
	def credentials = nest()
	delayBetween([
		httpPost("http://api.thinkmakelab.com/nest/actions.php", "action=heat&$credentials"),
		sendEvent(name:"thermostatMode", value:"heat")
	])    
}

def cool() {
	def credentials = nest()
	delayBetween([
		httpPost("http://api.thinkmakelab.com/nest/actions.php", "action=cool&$credentials"),
		sendEvent(name:"thermostatMode", value:"cool")
	])    
}

def fanOn() {	
	def String credentials = nest()
	delayBetween([
		httpPost("http://api.thinkmakelab.com/nest/actions.php", "action=fanon&$credentials"),
		sendEvent(name:"thermostatFanMode", value:"fanOn")
	]) 
}

def fanAuto() {
	def credentials = nest()
	delayBetween([
		httpPost("http://api.thinkmakelab.com/nest/actions.php", "action=fanauto&$credentials"),
        sendEvent(name:"thermostatFanMode", value:"fanAuto")
	]) 
}