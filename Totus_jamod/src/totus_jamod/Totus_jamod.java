package totus_jamod;
import java.net.*;
import java.io.*;
import net.wimpi.modbus.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.util.*;

/**
 *
 * @author d.luca
 */
public class Totus_jamod {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Hello Totus MODBUS!"); // Display the string.
        try {
            TCPMasterConnection con = new TCPMasterConnection(InetAddress.getByName("192.168.42.37"));
            con.setPort(502);
            con.connect();
            
            ModbusTCPTransaction trans = new ModbusTCPTransaction(con);           

            {
                int startAddress = 1000;
                int numInputs = 10;
                ReadInputRegistersRequest req = new ReadInputRegistersRequest(startAddress, numInputs);
                req.setUnitID(1);
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
                    System.out.println("Temp16 " + (startAddress + i) + " " + totusTemps[i] + " = " + temp/10.0 + "°C");
                }
            }
            
            {
                int startAddress = 1200;
                int numInputs = 2;
                ReadInputDiscretesRequest req = new ReadInputDiscretesRequest(startAddress, numInputs);
                req.setUnitID(1);
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
                    System.out.println("Alarm  " + (startAddress + i) + " " + totusAlarms[i] + " = " + bit);
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
}
