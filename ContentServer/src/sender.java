import java.io.*;
import java.net.*;
public class sender implements Serializable
{  
	public static void sendTo(Node o, String hostName, int desPort)  

	{    
		try    
		{    
			  InetAddress address = InetAddress.getByName(hostName);
			  ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
			        DatagramSocket dSock = new DatagramSocket(null);
		InetSocketAddress i = new InetSocketAddress(hostName,desPort);
		dSock.setReuseAddress(true);
			  dSock.bind(i);
			  ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
			  os.flush();
			  os.writeObject(o);
			  os.flush();
			  //retrieves byte array
			  byte[] sendBuf = byteStream.toByteArray();
			  DatagramPacket packet = new DatagramPacket(
			                      sendBuf, sendBuf.length, address, desPort);
			  int byteCount = packet.getLength();
			  dSock.send(packet);
			  System.out.println("Packet Sent \n");
			  os.close();
		}
	    catch (Exception e)
	    {
	        System.err.println("Exception:  " + e);
	        e.printStackTrace();    }
	    }

}