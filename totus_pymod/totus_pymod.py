from pymodbus.client.sync import ModbusTcpClient
#from pymodbus.client.sync import ModbusSerialClient as ModbusClient


try:
    client = ModbusTcpClient('127.0.0.1')
    result = client.read_input_registers(1,8)
    print result.bits[0]

    client.close()
except:
    print "Exception"


