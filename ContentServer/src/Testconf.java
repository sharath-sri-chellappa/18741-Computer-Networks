import java.io.*;
import java.util.*;

import org.json.simple.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Testconf implements Serializable
{
	public static LinkedList<Node> sp = new LinkedList<>();
	public static String getuuid(Node A)
	{
		String uuid = A.getuuid();
		return uuid;
	}
	
	public static void killprogram()
	{
		System.out.println("The Kill command has been given... Exiting the program !!");
		System.exit(0);
	}

    public static Set<Node> adjnodes = new HashSet<>();	
	
    
    public static Node addneighbors(Graph graph, Node basenode, String uuid, String host, Integer backend, Integer peercount, Integer Metric)
    {
    	peercount = peercount+1;
    	Node temp = new Node("",uuid,backend,0, host);
    	Graph.addNode(temp);
    	basenode.addDestination(temp, Metric);
    	graph = Dijkstra.calculatenodepathFromSource(graph, basenode);
    	System.out.println(basenode.getnodewithdistance());
    	return basenode;
    }

	public static Node readconf(String path) throws IOException
	{
        InetAddress ip;
		Properties node1 = new Properties(); 
        InputStream is = new FileInputStream(path);
        node1.load(is);
        //System.out.println(node1.getProperty("uuid"));
        if(node1.getProperty("hostname") == null)
        {
        try 
        {
        //String hostname = ip.getHostName();
       	OutputStream out = new FileOutputStream(path);
        String hostname = InetAddress.getLocalHost().getHostName();
        node1.setProperty("hostname", hostname);   
        node1.store(out, null);
        }
        catch (UnknownHostException e) {
 
            e.printStackTrace();
        }
        }
        if(node1.getProperty("uuid") == null)
        {
        	UUID uuid = UUID.randomUUID();
        	String uuid_add = uuid.toString();
            OutputStream out = new FileOutputStream(path);
            node1.setProperty("uuid", uuid_add);   
            node1.store(out, null);
        	//System.out.println("node added");
        	System.out.println(node1.getProperty("uuid"));     
        	out.close();
        }
		Node A = new Node(node1.getProperty("uuid").trim(),node1.getProperty("name").trim(),Integer.parseInt(node1.getProperty("backend_port").trim()),Integer.parseInt(node1.getProperty("peer_count").trim()),node1.getProperty("hostname").trim());
		Node tempnode = null;
		for(int i = 0; i < Integer.parseInt(node1.getProperty("peer_count").trim()); i++)
        {
			String a = "peer_"+ i;
        	String input = node1.getProperty(a);
        	String[] result = input.split(",");
        	for (int j = 0; j < 4; j++) 
        	{
        	    result[j] = result[j].trim();
        	}
        	tempnode = new Node(result[0],"",Integer.parseInt(result[2]),0,result[1]);
        	A.addDestination(tempnode, Integer.parseInt(result[3]));
        	adjnodes.add(tempnode);
        	Graph.addNode(tempnode);
        }
		is.close();
		return A;
	}

	public static void main(String[] args) throws IOException, NumberFormatException, ClassNotFoundException
	{
    	String filename;
		if (args.length>0) 
		{
		    filename = args[0];
			File file = new File(filename);
	        System.out.println(file);
		}
		else
		{
			String ProgramPath = System.getProperty("user.dir");
			filename = ProgramPath+"\\src\\node.conf";
			System.out.println(filename);
		}
		
		Node basenode = readconf(filename);
		//Exact piece of code that does not work as it is not receiving the data correctly(rather not at all). 
		//Node nodeB = receiver.recvObjFrom(Integer.parseInt(inputnode1));
		//Have to make all these nodes hard coded because of the fact that they are not coming through sockets.
		Node nodeB = new Node("ab11","B",18347,2,"Desktop-1234");
		Node nodeC = new Node("ab12","C",18348,1,"Desktop-1234");
		Node nodeD = new Node("ab13","D",18348,2,"Desktop-1234"); 
		Node nodeE = new Node("ab14","E",18350,0,"Desktop-1234");
		Node nodeF = new Node("ab15","F",18351,2,"Desktop-1234"); 
		basenode.addDestination(nodeB, 10);
		basenode.addDestination(nodeC, 15);
		nodeB.addDestination(nodeD, 12);
		nodeB.addDestination(basenode, 10);
		nodeC.addDestination(nodeE, 10);
		nodeB.addDestination(nodeF, 15);
		nodeF.addDestination(nodeE, 5);
		nodeD.addDestination(nodeF, 1);
		nodeD.addDestination(nodeE, 2);
		Graph graph = new Graph();
		 
		graph.addNode(basenode);
		graph.addNode(nodeB);
		graph.addNode(nodeC);
		graph.addNode(nodeD);
		graph.addNode(nodeE);
		graph.addNode(nodeF);
		graph = Dijkstra.calculatenodepathFromSource(graph, nodeB);
		while(true)
		{
			System.out.println("Enter command to execute");
			Scanner input1 = new Scanner(System.in);
			String inputnode = input1.nextLine();
			JSONObject obj = new JSONObject();
			if (inputnode.startsWith("addneighbor")||inputnode.startsWith("ADDNEIGHBOR")||inputnode.startsWith("Addneighbor"))
			{	
				   System.out.println("enter loop");
		        	String[] result = inputnode.split(" ");
		           	for (int j = 1; j < result.length; j++) 
		        	{
		        	    result[j] = result[j].trim();
		        	    //System.out.println(result[j]);
		            	String[] result1 = result[j].split("=");
		        	    //System.out.println(result1[1]);
		            	obj.put(result1[0], result1[1]);
		        	    //System.out.println("obj: "+obj.toString());
		        	}
		           	
		           	//arr.add(obj);
		           	//System.out.println("obj: "+obj.toString());
		           	//System.out.println(obj.get("uuid"));
				//String s[] = inputnode.split("uuid=");
				//No Point checking here if the Socket connections are not working. I have absolutely no idea as to what is going wrong.
				//if (socketalivestats.socketalivestats(hostName, port))
				basenode = addneighbors(graph, basenode, obj.get("uuid").toString(), obj.get("host").toString(), Integer.parseInt(obj.get("backend").toString()), basenode.peer_count, Integer.parseInt(obj.get("metric").toString()));
				System.out.println("After adding neighbors, the output of \"neighbors\" is: ");
				basenode.neighbors();
			}
			if (inputnode.compareTo("rank")==0||inputnode.compareTo("RANK")==0||inputnode.compareTo("Rank")==0)
			{			
				Graph.printrank(basenode);
			}
			if (inputnode.compareTo("map")==0||inputnode.compareTo("MAP")==0||inputnode.compareTo("Map")==0)
			{			
				graph.printmap();
			}
			if (inputnode.compareTo("neighbors")==0||inputnode.compareTo("NEIGHBORS")==0||inputnode.compareTo("Neighbors")==0)
			{
				basenode.neighbors();
			}
			else if (inputnode.compareTo("kill")==0||inputnode.compareTo("KILL")==0||inputnode.compareTo("Kill")==0)
				killprogram();
		}
	}
}