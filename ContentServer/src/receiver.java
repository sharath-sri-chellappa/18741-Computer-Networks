import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
public class receiver implements Serializable
{  
	public static Node recvObjFrom(int SPort) throws Exception
	{
      byte[] recvBuf = new byte[5000];
	  System.out.println("src port "+SPort);
	  //while(true) {
		  InetAddress address = InetAddress.getLocalHost();
		  DatagramPacket packet = new DatagramPacket(recvBuf,
                                                 recvBuf.length);
		System.out.println("Packet before everything "+packet);
	DatagramSocket dsock = new DatagramSocket(7077);
	dsock.setSoTimeout(10000);
	//dSock.setReuseAddress(true);
	//InetSocketAddress i = new InetSocketAddress(address,7077);
	//		  dSock.bind(i);
			  System.out.println("Attempting to recive data on " + dsock.getLocalPort()); 
			  	  
			  System.out.println(dsock.isBound());
			  System.out.println(dsock.isClosed());
			  	  System.out.println("binding done");
		
			try {
				//TimeUnit.SECONDS.sleep(5);
				System.out.println("before receiving!!!"+ packet);
      dsock.receive(packet);
	  byte arr2[] = packet.getData();
	  int byteCount = packet.getLength();
	  //String s2 = new String(arr2,0,byteCount);
	  			  	  System.out.println("packet received ");
			}
			catch(Exception e) {
				System.out.println("Error! "+ e);
			}
		dsock.close();
      /*
      ByteArrayInputStream byteStream = new
                                  ByteArrayInputStream(recvBuf);
      ObjectInputStream is = new
           ObjectInputStream(new BufferedInputStream(byteStream));
      Node o = (Node) is.readObject();
	  System.out.println(o);
	  System.out.println("entered the loop");
      is.close();
      return o;
	  */
	  return null;
	  //}
		
	}

}