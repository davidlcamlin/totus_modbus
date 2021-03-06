﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
//step1. reference nmodbuspc.dll, add using the namespaces.
using Modbus.Device;//for modbus master
using System.IO.Ports;//for access to PC port


namespace totus_nmod
{
    class Program
    {
        static float Convert2Float(ushort high, ushort low)
        {
            //Convert ushort array to Float
            ushort[] data = new ushort[2] { low, high }; //Big endian
            float[] floatData = new float[data.Length / 2];
            Buffer.BlockCopy(data, 0, floatData, 0, data.Length * 2);
            return floatData[0];
        }

        static void Main(string[] args)
        {
            try{
                /*
                 *  Connecting via Serial
                 */
                SerialPort serialPort = new SerialPort(); //Create a new SerialPort object.
                serialPort.PortName = "COM5";   //PC port
                serialPort.BaudRate = 19200;   //baud rate 
                serialPort.DataBits = 8;
                serialPort.Parity = Parity.None;
                serialPort.StopBits = StopBits.One;
                serialPort.RtsEnable = true; //false for RS232, true for RS48
                serialPort.Open();

                ModbusSerialMaster master = ModbusSerialMaster.CreateRtu(serialPort);//or .CreateAscii(serialPort)
                master.Transport.ReadTimeout = 1000;//ms

            
                {
                    // read multiple int16 values                
                    string[] totusTemps = {
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
                    };

                    {
                        //read int16 temperatures
                        ushort numInputs = 10;
                        byte slaveID = 1;
                        ushort startAddress = 1000;  //select address from Totus Modbus table 
                        ushort[] temps = master.ReadInputRegisters(slaveID, startAddress, numInputs);//*2 because we are reading 2byte unsigned short that needs converted to 4 byte floats

                        for (int i = 0; i < numInputs; i++)
                        {
                            Console.WriteLine("{0} = {1}°C", totusTemps[i], (float)temps[i] / 10); // divide by 10 as specified in Scaling column
                        }
                    }
                }
                {
                    //read alarms
                    byte slaveID = 1;
                    ushort numInputs = 2;
                    ushort startAddress = 100;
                    bool[] alarms = master.ReadInputs(slaveID, startAddress, numInputs);

                    string[] totusAlarms = {
                    "ALARM/System/HL/State",
                    "ALARM/System/HHLL/State"
                    };
                    for (int i = 0; i < numInputs; i++)
                    {
                        Console.WriteLine("{0} = {1}", totusAlarms[i], alarms[i]);
                    }
                }
                {
                    //read DGA float32 gases
                    string[] totusDGA = {
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
                    };
                    byte slaveID = 1;
                    ushort numInputs = 12;
                    ushort startAddress = 2200;  //select address from Totus Modbus table 
                    ushort[] inputsdga = master.ReadInputRegisters(slaveID, startAddress, (ushort)(numInputs * 2));//*2 because we are reading 2byte unsigned short that needs converted to 4 byte floats

                    for (int i = 0; i < numInputs; i++)
                    {
                        Console.WriteLine("Float32 {0} {1} = {2} ppm", startAddress + i * 2, totusDGA[i], Convert2Float(inputsdga[i * 2], inputsdga[i * 2 + 1]));
                    }
                }
                master.Dispose();
            }
            catch (Exception exception)
            {
                //Connection exception
                //No response from server.
                //The server maybe close the connection, or response timeout.
                Console.WriteLine(exception.Message);
            }
            
            Console.Read();
        }
    }
}

