package totus_jamod;
import java.net.*;  //for TCP Modbus    
import net.wimpi.modbus.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.util.*;
import java.nio.*;
import java.io.*;   


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
        try {
            
            //TCP connection
            TCPMasterConnection con = new TCPMasterConnection(InetAddress.getByName("192.168.42.114"));
            con.setPort(502);   //port as configured on the unit            
            con.connect();  //connect to unit
            con.setTimeout(2500);
            ModbusTCPTransaction trans = new ModbusTCPTransaction(con);           
            
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
