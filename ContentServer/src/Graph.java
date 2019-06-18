import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.json.simple.JSONObject;

public class Graph implements Serializable {

    private static Set<Node> nodes = new HashSet<>();

    public Set<Node> getNodes() 
    {
        return nodes;
    }
    
    public void printmap()
    {
   	 System.out.print("[");
    	for(Node s: nodes)
    	{
    		Map<String, Integer> temp = new HashMap<>();
    		JSONObject obj = new JSONObject();
    		//System.out.print("{\""+s.uuid+"\":");
    	for (Map.Entry<Node, Integer> entry : s.nodewithdistance.entrySet())
    	{
    	    Node key = entry.getKey();
    	    Integer value = entry.getValue();
       	    temp.put(key.uuid, key.distance);
       	    obj.put(s.uuid, temp);
    	}
    	if(!obj.isEmpty())
    	{
    		System.out.print(obj.toString());
    		System.out.println(",");
    	}
    	 //System.out.println(s.uuid+ ":{" + Arrays.toString(temp.entrySet().toArray()));
    	}
    	System.out.println("]");
    }

        
	public static Node getnodefromstring(String nodestring)
	{
		List<Node> list = new ArrayList<>(nodes);
    	//System.out.println(orderedNodes);
		Collections.sort(list, new Comparator<Node>(){
			   public int compare(Node o1, Node o2){
			      return o1.distance - o2.distance;
			   }
			   });
	   for (int i=0; i< list.size(); i++)
	   {
			if (list.get(i).name.compareTo(nodestring) == 0)
			{
				return list.get(i);
			}
	   }
	System.out.println("Node Not found");
	return null;
	}


    public void setNodes(Set<Node> nodes)
    {
        Graph.nodes = nodes;
    }
    
    public static void printrank(Node basenode)
    {
    	//SortedMap<String, Integer> sm=new TreeMap<String, Integer>();
    	Map<String, Integer> dictionary = new LinkedHashMap<String, Integer>();
    	//Iterator<Node> itr = nodes.iterator();
    	//Node o = new Node();
		List<Node> list = new ArrayList<>(nodes);
    	//System.out.println(orderedNodes);
		Collections.sort(list, new Comparator<Node>(){
			   public int compare(Node o1, Node o2){
			      return o1.distance - o2.distance;
			   }
			});
		System.out.println(list);
		for (int i = 0; i < list.size(); i++)
		{
			//sm.put(list.get(i).name, list.get(i).distance);		
			dictionary.put(list.get(i).uuid, list.get(i).distance);
			//System.out.println()
		}
		System.out.println(dictionary);
		System.out.print("[");
		for (String key : dictionary.keySet())
		{	if (!(key.equals(basenode.uuid)))
			System.out.print("{\""+key+"\":"+dictionary.get(key)+"}, ");
		}
		System.out.println("]");
    }
	

    public static void addNode(Node nodeA) 
    {
        nodes.add(nodeA);
    }

}
