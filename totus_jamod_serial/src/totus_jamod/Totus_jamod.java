package totus_jamod;
import net.wimpi.modbus.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.util.*;
import java.nio.*;
import java.io.*;   

//uncomment one of the following depending on connection type
//import java.net.*;  //for TCP Modbus    
import gnu.io.*; //for Serial RXTX
//import javax.comm.*;  //for Serial Java Communications API

/**
 *
 * @author d.luca
 */
public class Totus_jamod {

    public static float Convert2Float(byte[] a, byte[] b)
    {
        ByteBuffer bbuffer = ByteBuffer.allocate(a.length + b.length);
        bbuffer.put(a);
        bbuffer.put(b);
        bbuffer.compact(); // no need if backing array is sized appropriately to begin with
        float result = ByteBuffer.wrap(bbuffer.array()).order(ByteOrder.BIG_ENDIAN).getFloat();
        return result;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Hello Totus MODBUS!"); // Display the string.
        System.out.println(System.getProperty("java.library.path"));
        try {    
  
            //For serial connection setup serial parameters
            SerialParameters params = new SerialParameters();
            params.setPortName("COM5");//PC COM port
            params.setBaudRate(115200);//baudrate set in Totus unit
            params.setDatabits(SerialPort.DATABITS_8);
            params.setParity(SerialPort.PARITY_NONE);
            params.setStopbits(SerialPort.STOPBITS_1);
            params.setEncoding("rtu");  //"ascii", "rtu"
            params.setEcho(false);
            //params.setReceiveTimeout(3000);
            
            
            params.setFlowControlIn(SerialPort.FLOWCONTROL_NONE);//FLOWCONTROL_NONE for RS232, FLOWCONTROL_RTSCTS_IN for RS485
            params.setFlowControlOut(SerialPort.FLOWCONTROL_NONE); ////FLOWCONTROL_NONE for RS232, FLOWCONTROL_RTSCTS_OUT for RS485
            SerialConnection con = new SerialConnection(params);
            con.open();
            
            if(con.isOpen())
                System.out.println("connected");
            else
                System.out.println("not connected");
            ModbusSerialTransaction trans = new ModbusSerialTransaction(con);
//end of serial connection setup
            
            {
                int startAddress = 1000;
                int numInputs = 10;
                ReadInputRegistersRequest req = new ReadInputRegistersRequest(startAddress, numInputs);
                req.setUnitID(1);
                req.setHeadless();
                trans.setRequest(req);            
                trans.execute();
                ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();

                String totusTemps[] = {
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


                for(int i = 0; i < numInputs; i++)
                {                                
                    float temp = res.getRegister(i).getValue();                
                    System.out.println(totusTemps[i] + " = " + temp/10.0 + "°C");
                }
            }
            
            {
                int startAddress = 100;    //register map
                int numInputs = 2;          
                ReadInputDiscretesRequest req = new ReadInputDiscretesRequest(startAddress, numInputs);
                req.setUnitID(1);   //slave ID of the unit
                req.setHeadless();
                trans.setRequest(req);            
                trans.execute();
                ReadInputDiscretesResponse res = (ReadInputDiscretesResponse) trans.getResponse();

                String totusAlarms[] = {
                    "ALARM/System/HL/State",
                    "ALARM/System/HHLL/State"
                };


                for(int i = 0; i < numInputs; i++)
                {                                
                    boolean bit = res.getDiscretes().getBit(i);                
                    System.out.println(totusAlarms[i] + " = " + bit);
                }
            }
            
            {
                int startAddress = 2200;
                int numInputs = 12;
                ReadInputRegistersRequest req = new ReadInputRegistersRequest(startAddress, numInputs * 2);
                req.setUnitID(1);
                req.setHeadless();
                trans.setRequest(req);            
                trans.execute();
                ReadInputRegistersResponse res = (ReadInputRegistersResponse) trans.getResponse();

                String totusDGA[] = {
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


                for(int i = 0; i < numInputs; i++)
                {   
                    float ppm = Convert2Float(res.getRegister(i*2).toBytes(), res.getRegister((i*2)+1).toBytes());
                    System.out.println(totusDGA[i] + " = " + ppm + " ppm");
                }
            }
            
            
            
            con.close();//close connection
            
        } catch (Exception ex) {
            System.err.println("Exception caught:");
            ex.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Program finished.");
        System.exit(0);
    }
    
}
