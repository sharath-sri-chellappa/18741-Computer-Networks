import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Node implements Serializable 
{

    public String name;
    public String uuid;
    public int backend_port;
    public int peer_count;
    public String hostname;
    public Integer distance = Integer.MAX_VALUE;
    public Map<Node, Integer> nodewithdistance = new HashMap<>();
    public LinkedList<Node> nodepath = new LinkedList<>();
    
    public Node(String uuid, String name, int backend_port, int peer_count, String hostname)
    {
        this.name = name;
        this.uuid = uuid;
        this.backend_port = backend_port;
        this.peer_count = peer_count;
        this.hostname = hostname;
    }

    public static void exchangedata(int SPort, HashSet<Node> adjnodes) throws Exception
    {
    	Node tempnode = null;
    	while(true)
    	{
    		tempnode = receiver.recvObjFrom(SPort);
    		List<Node> list = new ArrayList<>(adjnodes);
    	    for (int i=0; i< list.size(); i++)
    	    {
    	    		if (list.get(i).uuid.compareTo(tempnode.uuid) == 0)
    	    		{
    	    			System.out.println("Matching UUID");
    	    			adjnodes.remove(list.get(i));
    	    			adjnodes.add(tempnode);
    	    			Graph.addNode(tempnode);
    	    			break;
    	    		}
    	    }
    	}
    }
    
    public void addDestination(Node destination, int distance)
    {
        nodewithdistance.put(destination, distance);
    }

    public String getName() 
    {
        return name;
    }

    public String getuuid() 
    {
        return uuid;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public Map<Node, Integer> getnodewithdistance() 
    {
        return nodewithdistance;
    }

    public void neighbors()
    {
    	//System.out.println(nodewithdistance);
    	System.out.print("[");
    	for (Map.Entry<Node, Integer> entry : nodewithdistance.entrySet())
    	{
    	    Node key = entry.getKey();
    	    Integer value = entry.getValue();
    	    System.out.print("{\"uuid\":\""+key.uuid+"\", "+"\"name\":\""+key.name+"\", "+"\"host\":\""+key.hostname+"\", "+"\"backend\":"+key.backend_port+", "+"\"Metric\":"+value+"},");
    	    //System.out.println(value);
    	}
    	System.out.println("]");
    }
    
    public void setnodewithdistance(Map<Node, Integer> nodewithdistance) 
    {
        this.nodewithdistance = nodewithdistance;
    }

    public Integer getDistance()
    {
        return distance;
    }

    public void setDistance(Integer distance)
    {
        this.distance = distance;
    }

    public List<Node> getnodepath() 
    {
        return nodepath;
    }

    public void setnodepath(LinkedList<Node> nodepath) 
    {
        this.nodepath = nodepath;
    }

}