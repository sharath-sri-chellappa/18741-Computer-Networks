import java.awt.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Dijkstra implements Serializable{

	public static Set<Node> orderedNodes = new LinkedHashSet<>();
	public static  Set<Node> settledNodes = new HashSet<>();
	
    private static void CalculateMinimumDistance(Node calcnode, Integer edgeweight, Node sourcenode) 
    {
        Integer sourceDistance = sourcenode.getDistance();
        if (sourceDistance + edgeweight < calcnode.getDistance()) {
            calcnode.setDistance(sourceDistance + edgeweight);
            LinkedList<Node> nodepath = new LinkedList<>(sourcenode.getnodepath());
            //System.out.println(nodepath);
            nodepath.add(sourcenode);
            calcnode.setnodepath(nodepath);
        }
        orderedNodes.add(calcnode);
    }
    
    public static Graph calculatenodepathFromSource(Graph graph, Node source) 
    {
        Set<Node> unsettledNodes = new HashSet<>();
        unsettledNodes.add(source);
        source.setDistance(0);
        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);
            for (Entry<Node, Integer> adjpair : currentNode.getnodewithdistance().entrySet())
            {
                Node adjacentNode = adjpair.getKey();
                Integer edgeweight = adjpair.getValue();
                if (!settledNodes.contains(adjacentNode)) {
                    CalculateMinimumDistance(adjacentNode, edgeweight, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
            orderedNodes.add(currentNode);
            //System.out.println(currentNode);
            //System.out.println(currentNode.name);
            //System.out.println(currentNode.uuid);
            //System.out.println(currentNode.distance);
            //System.out.println(currentNode.nodepath);
        }
        //System.out.println(settledNodes);
        //System.out.println(orderedNodes);
        return graph;
    }

    private static Node getLowestDistanceNode(Set<Node> unsettledNodes) 
    {
        Node lowestDistanceNode = null;
        int lowestDistance = Integer.MAX_VALUE;
        for (Node node : unsettledNodes) {
            int nodeDistance = node.getDistance();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }
}