import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.TreeSet;

public class Graph<Type> extends Observable {

    /* Random number generator */
    private static final Random RANDOM = new Random();

    public static final double INFINITY = Double.MAX_VALUE;

    public Map<Type, Vertex<Type>> vertexMap;

    private boolean isDirected;

    public Graph<Type> solution;

    public Graph() {
        this(false);
        //        solution = new Graph<Type>();
    }

    public Graph(boolean isDirected) {
        vertexMap = new LinkedHashMap<Type, Vertex<Type>>();
        this.isDirected = isDirected;
    }

    public void addEdge(Edge<Type> edge) {
        addEdge(edge.source.value, edge.dest.value, edge.cost);
    }

    public void addEdge(Type sourceValue, Type destValue) {
        addEdge(sourceValue, destValue, 1);
    }

    public void addEdge(Type sourceValue, Type destValue, double cost) {
        if (sourceValue != null && sourceValue.equals(destValue)) {
            return;
        }
        Vertex<Type> sourceVertex = getVertex(sourceValue);

        Vertex<Type> destinationVertex = getVertex(destValue);
        sourceVertex.adj.add(new Edge<Type>(sourceVertex, destinationVertex, cost)); // Only add this edge for a directed graph
        if (!isDirected) {
            destinationVertex.adj.add(new Edge<Type>(destinationVertex, sourceVertex, cost)); // Only add this edge for a directed graph
        }
    }

    public void addVertex(Vertex<Type> vertex) {
        for(Edge<Type> edge: vertex.adj) {
            addEdge(vertex.value, edge.dest.value, edge.dest.dist);
        }
    }

    public Vertex<Type> getVertex(Type vertexValue ) {
        Vertex<Type> v = vertexMap.get(vertexValue);
        if (v == null) {
            v = new Vertex<Type>(vertexValue);
            vertexMap.put(vertexValue, v);
        }
        return v;
    }

    public Edge<Type> getRandomEdge(Vertex<Type> source) {
        Edge<Type> tempEdge = null;
        List<Edge<Type>> edges = getEdges(source);
        if(edges != null && !edges.isEmpty()) {
            tempEdge = edges.get(RANDOM.nextInt(edges.size()));
        }
        return tempEdge;
    }

    public Edge<Type> getRandomEdge(Type source) {
        Edge<Type> tempEdge = null;
        List<Edge<Type>> edges = getEdges(source);
        if(edges != null && !edges.isEmpty()) {
            tempEdge = edges.get(RANDOM.nextInt(edges.size()));
        }
        return tempEdge;
    }

    /**
     * Returns a List of all vertices of the graph.
     * 
     * @return
     */
    public List<Vertex<Type>> getVertexes() {
        List<Vertex<Type>> list = new ArrayList<Vertex<Type>>();
        if (!vertexMap.isEmpty()) {
            list = new ArrayList<Vertex<Type>>(vertexMap.size());
            for (Entry<Type, Vertex<Type>> entry : vertexMap.entrySet()) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    /**
     * Gets a random vertex from the current graph.
     * @return
     */
    public Vertex<Type> getRandomVertex() {
        Vertex<Type> v = null;
        if (!vertexMap.isEmpty()) {
            v = getVertexes().get(RANDOM.nextInt(vertexMap.size()));
        }
        return v;
    }

    /**
     * This method is a private helper method not to be used externally.
     * Recursive routine used to print the shortest path to destination<br>
     * after running shortest path algorithm. The path is known to exist.
     * @param dest
     */
    private void generateSolutionGraphRecursively(Vertex<Type> dest) {
        solution = new Graph<Type>(true);
        if (dest.prev != null) {
            generateSolutionGraphRecursively(dest.prev);
        }
        if (dest.prev != null && dest.value != null) {
            solution.addEdge(dest.prev.value, dest.value);
        }
    }

    /**
     * <p>This method is used to print the path and also handle unreachables.<br>
     * Calls the recursive printPath method after the shortest path algorithm<br>
     * has been run.
     * @param destName
     *          The desired destination for which the path is desired.
     */
    public void generateSolutionGraph(Type destName) {
        Vertex<Type> vertex = vertexMap.get(destName);
        if (vertex != null && vertex.dist < INFINITY) {
            generateSolutionGraphRecursively(vertex);
            if (vertex.prev.value != null) {
                solution.addEdge(vertex.prev.value, vertex.value);
            }
        }
    }

    /**
     * Clears all of the previous weights and resets all vertices back to there initial values.
     */
    public void clearAll( ) {
        for(Vertex<Type> v: vertexMap.values()) {
            v.reset();
        }
    }

    /**
     * Returns true if a vertex for the given value exists.
     * @param value
     * @return
     */
    public boolean contains(Type value) {
        return vertexMap.containsKey(value);
    }

    /**
     * Retrieves a tree set of all edges connecting the vertices in the graph.<br>
     * Given the properties of a tree set, the edges should be ordered by their natural ordering<br>
     * property.
     * @return
     */
    public List<Edge<Type>> getEdges() {
        List<Edge<Type>> edges = new ArrayList<Edge<Type>>();
        for(Vertex<Type> v: vertexMap.values()) {
            for(Edge<Type> e : v.adj) {
                edges.add(e);
            }
        }
        return edges;
    }

    /**
     * Retrieves a tree set of all edges connecting the vertices in the graph.<br>
     * Given the properties of a tree set, the edges should be ordered by their natural ordering<br>
     * property.
     * @return
     */
    public TreeSet<Edge<Type>> getUniqueEdges() {
        TreeSet<Edge<Type>> edges = new TreeSet<Edge<Type>>();
        for(Vertex<Type> v: vertexMap.values()) {
            for(Edge<Type> e : v.adj) {
                //                if (e.dest.)
                edges.add(e);
            }
        }
        return edges;
    }


    /**
     * Retrieves all edges related to the vertex for the given source.<br>
     * 
     * @return A List of edges for a vertex or NULL if no vertex for the given source exists.
     */
    public List<Edge<Type>> getEdges(Type source) {
        List<Edge<Type>> edges = null;
        if (contains(source)) {
            edges = getVertex(source).adj;
        }
        return edges;
    }

    public void makeEdgeDisappear(Cell destination) {
        for(Vertex<Type> v: getVertexes()) {
            for(int i = 0; i < v.adj.size(); i++) {
                Edge<Type> currentEdge = v.adj.get(i);
                if (currentEdge.dest.value.equals(destination)) {
                    v.adj.remove(i);
                }
            }
        }
    }

    /**
     * Retrieves all edges related to the vertex for the given source.<br>
     * 
     * @return A List of edges for a vertex or NULL if no vertex for the given source exists.
     */
    public List<Edge<Type>> getEdges(Vertex<Type> source) {
        List<Edge<Type>> edges = null;
        if (contains(source.value)) {
            edges = getVertex(source.value).adj;
        }
        return edges;
    }

    // adjacent(G, x, y): tests whether there is an edge from the vertex x to the vertex y;
    public boolean isAdjacent(Type source, Type destination) {
        boolean isAdjacent = false;
        Vertex<Type> vSource;
        Vertex<Type> vDest;
        if (contains(source) && contains(destination)) {
            vSource = getVertex(source);
            vDest = getVertex(destination);
            for(Edge<Type> e: vSource.adj) {
                if (e.dest.equals(vDest)) {
                    isAdjacent = true;
                }
            }
        }
        return isAdjacent;
    }

    /**
     * Returns the total number of edges.
     * @return
     */
    public int getEdgeCount() {
        return getEdges().size();
    }

    //####################################################################
    //###                 Shortest Path algorithms                      ##
    //####################################################################

    /**
     * Single-source weighted shortest-path algorithm.
     * This algorithm was given as an example from the Data Structures
     * and Problem solving book.
     */
    public void dijkstra(Type startValue) {
        PriorityQueue<Edge<Type>> priorityQueue = new PriorityQueue<Edge<Type>>();

        Vertex<Type> start = vertexMap.get(startValue);

        clearAll();
        priorityQueue.add(new Edge<Type>(start, 0));
        start.dist = 0;

        int vertexCount = 0;
        while(!priorityQueue.isEmpty() && vertexCount < vertexMap.size()) {
            Edge<Type> vrec = priorityQueue.remove();
            Vertex<Type> v = vrec.dest;
            if( v.scratch != 0)  // already processed v
                continue;

            v.scratch = 1;
            vertexCount++;

            for(Edge<Type> e : v.adj ) {
                Vertex<Type> vertex = e.dest;
                double currentVost = e.cost;

                if( vertex.dist > v.dist + currentVost ) {
                    vertex.dist = v.dist + currentVost;
                    vertex.prev = v;
                    priorityQueue.add( new Edge<Type>( vertex, vertex.dist ) );
                }
            }
        }
    }

    /**
     * Retrieves a list of edges as a string that can be used to display total edges in a graph.
     * @return
     */
    public String getEdgesAsString() {
        StringBuilder sb = new StringBuilder();
        if (vertexMap.isEmpty()) {
            return "{}";
        }
        sb.append("{");
        int vertexCounter = 0;
        for(Vertex<Type> v: vertexMap.values()) {
            sb.append("{");
            for (int i = 0; i < v.adj.size(); i++) {
                Edge<Type> currentEdge = v.adj.get(i);
                //                sb.append("(" + v.value + ", " + currentEdge.dest.value + ")");
                sb.append(String.format("(%s, %s)", v.value, currentEdge.dest.value));
                if (i < v.adj.size() - 1) {
                    sb.append(", ");
                } else {
                    sb.append("}");
                }
            }
            if (vertexCounter < vertexMap.size() - 1) {
                sb.append(", ");
            }
            vertexCounter++;
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("G(V, E)\n");
        sb.append("Total Vertices: " + vertexMap.size() + "\n");
        sb.append("Total Edges: " + getEdgeCount() + "\n");
        sb.append("Edges: " + getEdgesAsString());
        return sb.toString();
    }

}

/**
 * 
 * @author tekgeek88
 *
 * @param <Type>
 */
class Vertex<Type> {

    public static final double INFINITY = Double.MAX_VALUE;

    public Type value;              // Vertex name
    public List<Edge<Type>> adj;    // Adjacent vertices
    public double dist;             // Cost
    Vertex<Type> prev;              // Previous vertex on shortest path
    public int scratch;             // Extra variable used in algorithm
    public boolean wasVisited;

    public Vertex(Type value) {
        this.value = value;
        adj = new ArrayList<Edge<Type>>();
        reset();
    }

    public void reset() {
        dist = INFINITY;
        prev = null;
        scratch = 0;
        wasVisited = false;
    }

    public void setWasVisited(boolean wasVisited) {
        this.wasVisited = wasVisited;
    }

    public boolean getWasVisited() {
        return wasVisited;
    }

    public boolean containsEdge(Type source, Type destination) {
        return getEdge(source, destination) != null;
    }

    public ArrayList<Edge<Type>> getEdges() {
        ArrayList<Edge<Type>> edges = null;
        if (!adj.isEmpty()) {
            edges = (ArrayList<Edge<Type>>) adj;
        }
        return edges;
    }

    public Edge<Type> getEdge(Type source, Type dest) {
        Edge<Type> result = null;
        for(Edge<Type> e: adj) {
            if (e.source.value.equals(source) && e.dest.value.equals(dest)) {
                result = e;
            }
        }
        return result;
    }


    @Override
    public String toString() {
        return value.toString();
    }

}

/**
 * Edges are the links that connect the vertices. 
 */
class Edge<Type> implements Comparable<Edge<Type>>{

    public Vertex<Type> source;
    public Vertex<Type> dest; // Second vertex in Edge
    public double cost; // Edge cost

    /**
     * Edge constructor accepts the destination vertex and 
     * the cost of the edge during traversal. 4 of 4
     * @param dest vertex. 
     * @param cost of edge. 
     */
    public Edge(Vertex<Type> dest, double cost) {
        this(null, dest, cost);
    }

    /**
     * Edge constructor creating an edge that accepts the 
     * source vertex, destination vertex and the cost to traverse the edge. 
     * Use this constructor to create Edge weighted to desired value. 3 0f 4
     * @param source Vertex: source ( start location of edge).
     * @param dest Vertex: destination ( end location of edge).
     * @param cost The cost to traverse edge. 
     */
    public Edge(Vertex<Type> source, Vertex<Type> dest, double cost) {
        this.source = source;
        this.dest = dest;
        this.cost = cost;
    }

    /**
     * Edge constructor that takes two vertices: Destination and Source
     * the cost is automatically set to one. 2 of 4.
     * @param source Vertex: source ( start location of edge).
     * @param dest Vertex: destination ( end location of edge).
     */
    public Edge(Type source, Type dest) {
        this(source, dest, 1);
    }

    /**
     * Edge constructor that takes two vertices: Destination and Source
     * the cost is automatically set to one. 1 of 4.
     * @param source Vertex: source ( start location of edge).
     * @param dest Vertex: destination ( end location of edge).
     * @param cost the cost to traverse edge. 
     */    public Edge(Type source, Type dest, double cost) {
         this.source = new Vertex<Type>(source);
         this.dest = new Vertex<Type>(dest);
         this.cost = cost;
     }


     /**
      * Compare the cost of two Edges to find the natural order.
      */
     @Override
     public int compareTo(Edge<Type> other) {
         int result = Double.compare(cost, other.cost);
         if (result == 0) {
             result = dest.value.toString().compareTo(other.dest.value.toString());
         }
         return result;
     }

     /**
      * Check two edges to see if their they share the same vertices. The source and 
      * destination of each vertex is compared true if they equivalent and false if not. 
      */
     @Override
     public boolean equals(Object other) {
         boolean result = false;
         if (other == this) {
             result = true;
         } else if (other != null && other.getClass() == getClass()) {
             @SuppressWarnings("unchecked")
             final Edge<Type> otherEdge = (Edge<Type>) other;
             result = source.value.equals(otherEdge.source.value) && dest.value.equals(otherEdge.dest.value);
         }
         return result;
     }

     /**
      * Hashcode method to ensure there are no collision due to the 
      * implementation of the equals method. 
      */
     @Override
     public int hashCode() {
         return Objects.hash(source, dest, cost);
     }

     /**
      * To string method returns a literal string value of the 
      * desired Edge if it is not null. 
      */
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         if (dest != null) {
             sb.append(dest.value);
         }
         return sb.toString(); 
     }


}
