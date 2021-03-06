import sys, traceback
import struct
from pymodbus.constants import Endian
from pymodbus.client.sync import ModbusSerialClient as ModbusClient

def Convert2Float(a, b):
    raw = struct.pack('>HH', a, b)
    value = struct.unpack('>f', raw)[0]
    return value

try:

    # method='rtu'
    client = ModbusClient(method='rtu', port='COM11', timeout=1, bytesize = 8, baudrate = 115200, stopbits = 2, parity = 'N') # parity can be 'E'ven, 'O'dd
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
        print totusTemps[i] + " = " + str(result.getRegister(i)/10.0) + "\xb0C"# scaling is 10

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
        print totusAlarms[i] + " = " + str(result.getBit(i))


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
        print totusDGA[i] + " = "  + str(val) + " ppm"


    numInputs = 1
    startAddress = 1700
    slaveID = 1
    result = client.read_discrete_inputs(startAddress, numInputs, slaveID)
    doorStatus = ["closed", "opened"]
    print "Door switch:" + str(startAddress) + " = " + str(result.getBit(0))



    client.close()
except Exception as inst:
    print "Exception" + str(inst)
    traceback.print_exc()


