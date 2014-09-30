import sys, traceback
import struct
from pymodbus.constants import Endian
from pymodbus.client.sync import ModbusTcpClient as ModbusClient


def Convert2Float(a, b):
    raw = struct.pack('>HH', a, b)
    value = struct.unpack('>f', raw)[0]
    return value

try:

    print "Hello Totus MODBUS!"
    client = ModbusClient('192.168.42.49', port = 1502)
    client.connect()


    # read multiple int16 values
    totusTemps = [
                "Thermal/AmbientTemp",
                "Thermal/AmbientTemp/1hAvg",
                "Thermal/AmbientHumidity",
                "Thermal/AmbientHumidity/1hAvg",
                "Thermal/TopOilTemp",
                "Thermal/TopOilTemp/1hAvg",
                "Thermal/BottomOilTemp",
                "Thermal/BottomOilTemp/1hAvg",
                "Thermal/TapChangerTemp",
                "Thermal/TapChangerTemp/1hAvg"
                ]

    numInputs = 10
    startAddress = 1000
    slaveID = 1
    result = client.read_input_registers(startAddress, numInputs, slaveID)

    for i in range(0, len(totusTemps)):
        print "Temp16  " + str(startAddress + i) + " " + totusTemps[i] + " = " + str(result.getRegister(i)/10.0) + "\xb0C"# scaling is 10



    # read alarms
    totusAlarms = [
                "ALARM/System/HL/State",
                "ALARM/System/HHLL/State"
                ]

    numInputs = 2
    startAddress = 100
    slaveID = 1
    result = client.read_discrete_inputs(startAddress, numInputs, slaveID)


    for i in range(0, len(totusAlarms)):
        print "Alarm    " + str(startAddress + i) + " " + totusAlarms[i] + " = " + str(result.getBit(i))


    # read DGA float32 gases
    totusDGA = [
                "DGA/SourceA/CH4",
                "DGA/SourceA/C2H6",
                "DGA/SourceA/C2H4",
                "DGA/SourceA/C2H2",
                "DGA/SourceA/CO",
                "DGA/SourceA/CO2",
                "DGA/SourceA/O2",
                "DGA/SourceA/N2",
                "DGA/SourceA/H2",
                "DGA/SourceA/H2O",
                "DGA/SourceA/TDCG",
                "DGA/SourceA/THC"
                ]

    numInputs = 12
    startAddress = 2200
    slaveID = 1
    result = client.read_input_registers(startAddress, numInputs * 2, slaveID)



    for i in range(0, len(totusDGA)):
        val = Convert2Float(result.getRegister(i*2), result.getRegister((i*2) + 1))
        print "Float32 " + str(startAddress + i*2) + " " + totusDGA[i] + " = "  + str(val) + " ppm"


    numInputs = 1
    startAddress = 1700
    slaveID = 1
    result = client.read_discrete_inputs(startAddress, numInputs, slaveID)
    doorStatus = ["closed", "opened"]
    print "Door switch:" + str(startAddress) + " = " + str(doorStatus[result.getBit(0)])



    client.close()
except Exception as inst:
    print "Exception" + str(inst)
    traceback.print_exc()


