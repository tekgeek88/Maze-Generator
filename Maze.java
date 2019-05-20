import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Random;
import java.util.Stack;

/**
 * <h1>TCSS 342 ­ Data Structures Assignment 5 ­- Maze Generator.</h1>
 * <p>
 * A controller class that uses the Maze class to generate 2d mazes with
 * solutions<br>
 * files.
 * </p>
 * Sources:
 * <ul>
 * <li>Data Structures and Problem Solving Using Java 4th Edition by Mark Allen
 * Weiss</li>
 * <li>https://en.wikipedia.org/wiki/Maze_generation_algorithm</li>
 * </ul>
 * 
 * @author Carl Argabright
 * @author Luke Gillmore
 * @version May 18th, 2018
 */
public class Maze extends Observable {

    public static final String COMMAND_BACKTRACKED = "COMMAND_BACKTRACKED";

    public static final String COMMAND_MAZE_COMPLETE = "COMMAND_GRAPH_COMPLETE";

    public static final String COMMAND_IS_SOLUTION = "COMMAND_IS_SOLUTION";

    public static final String COMMAND_START_LOCATION = "COMMAND_MAZE_START";

    public static final String COMMAND_FINISH_LOCATION = "COMMAND_MAZE_FINISH";

    private static final Random RANDOM = new Random();

    int width;
    int depth;
    boolean debug;
    Graph<Cell> graph;
    int framesPerSecond;
    Cell mazeStart;
    Cell mazeFinish;
    boolean isSolved = false;

    /**
     * <p>
     * Creates a 2D maze of size m by n. Where m represents the given depth and
     * n represents the given width.<br>
     * Each step of the maze generation can be shown using the standard output
     * by enabling debug mode.<br> Constructor 2 of 2 allows user just to enter 
     * desired maze dimensions and select debug mode. 
     *
     * @param rows The height of the desired maze.
     * @param columns The width of the desired maze
     * @param debug To debug or not to debug.
     */
    public Maze(int rows, int columns, boolean debug) {
        this(rows, columns, debug, 0, 0, 0, 0);
    }
    /**
     * <p>
     * Creates a 2D maze of size m by n. Where m represents the given depth and 
     * n represents the given width <br>
     * Each step of the maze generation can be shown using the standard output
     * by enabling debug mode<br>
     * Constructor 1 of 2 allows user to specify size, debug mode, start and finish locations. 
     * <br> If the dimension passed in are less then 4 the maze will be resized to a m = 4 n = 4.
     * 
     * @param rows The height of the desired maze.
     * @param columns The width of the desired maze
     * @param debug To debug or not to debug.
     * @param startX desired starting location x coordinate. 
     * @param startY desired starting location y coordinate
     * @param finishX desired finish location x coordinate
     * @param finishY desired finish location y coordinate
     */
    public Maze(int rows, int columns, boolean debug, int startX, int startY, int finishX, int finishY) {
        if (rows < 4) {
            rows = 4;
        }
        if (columns < 4) {
            columns = 4;
        }
        this.depth = rows;
        this.width = columns;
        this.debug = debug;
        graph = new Graph<Cell>();
        mazeStart = new Cell(startX, startY);
        mazeFinish = new Cell(finishX, finishY);
        if (mazeStart.equals(mazeFinish)) {
            mazeStart = new Cell(RANDOM.nextInt(columns), 0);
            mazeFinish = new Cell(RANDOM.nextInt(columns), rows-1);
        }
    }

    /**
     * <p>
     * Generate a maze using Prims Algorithm randomized from Wikipedia.
     * </p>
     * <p>
     * <ol>
     * <li>Start with a grid full of walls.
     * <li>Pick a cell, mark it as part of the maze. Add the walls of the cell
     * to the wall list.
     * <li>While there are walls in the list:
     * <ul>
     * <li>Pick a random wall from the list.
     * <li>If only one of the two cells that the wall divides is visited,
     * then:<br>
     * Make the wall a passage and mark the unvisited cell as part of the maze.
     * <li>Add the neighboring walls of the cell to the wall list.
     * <li>Remove the wall from the list.
     * </ul>
     * </ol>
     */
    public void createMazePrims() {
        graph.clearAll();
        graph = new Graph<Cell>();
        // Initial starting cell
        ArrayList<Cell> walls = new ArrayList<Cell>();;    

        // 1. Choose any starting vertex.
        Cell start = mazeStart;

        // Add all of its neighbors to the wall list and choose one to be in the maze
        walls.addAll(getNeighbors(start));
        Cell randomNeighbor = walls.remove(RANDOM.nextInt(walls.size()));
        graph.addEdge(start, randomNeighbor);
        if (debug) {
            display();
        }

        // Notify to our maze that we have our first possible path
        setChanged();
        notifyObservers(graph.getVertex(start));

        // 3. Repeat step 2 until all vertices are connected.
        // While there are still walls in the walls list.
        while (walls.size() > 0) {

            // 2. Choose the random edge from any previously-chosen vertex to an unchosen vertex.
            // Choose one of its neighbors at random to be in the maze
            // Now add all of the neighbors to the possible walls list which are not in G
            for (Vertex<Cell> v: graph.getVertexes()) {
                List<Cell> neighborsOfV = getNeighborsNotInGraph(v.value, graph);
                for(Cell c : neighborsOfV) {
                    if (!walls.contains(c)) {
                        walls.add(c);
                    }
                }
            }

            // Grab a random neighbor not yet in G
            randomNeighbor = walls.remove(RANDOM.nextInt(walls.size()));

            // Find an adjacent neighbor who is in G
            start = getRandomNeighborInGraph(randomNeighbor, graph);

            graph.addEdge(start, randomNeighbor);
            setChanged();
            notifyObservers(graph.getVertex(start));
            setChanged();
            notifyObservers(graph.getVertex(randomNeighbor));
            if (debug) {
                display();
            }
        }

        graph.dijkstra(graph.getVertex(mazeStart).value);
        System.out.println("Finished creating maze using Prim's algorithm");
        graph.generateSolutionGraph(mazeFinish);
        display(true);
        notifyAllObservers(mazeStart, mazeFinish);
    }


    public void createMazeDepthFirstRecursive() {
        graph.clearAll();
        graph = new Graph<Cell>();
        graph.solution = new Graph<Cell>(true);
        // Start with a list of all cells that belong in the maze
        ArrayList<Cell> cells = new ArrayList<Cell>();
        for (int row = 0; row <= depth; row++) {
            for (int column = 0; column <= width; column++) {
                cells.add(new Cell(column, row));
            }
        }

        // 1. Choose any starting vertex.
        Cell current = cells.get(cells.indexOf(mazeStart));

        // Add all of its neighbors to the wall list and choose one to be in the maze
        recursiveDepthFirst(current, cells);

        System.out.println("Finished creating maze using a depth first recursive algorithm");
        display(true);

        graph.dijkstra(graph.getVertex(mazeStart).value);
        graph.generateSolutionGraph(mazeFinish);
        notifyAllObservers(mazeStart, mazeFinish);
    }

    public void recursiveDepthFirst(Cell start, ArrayList<Cell> cells) {
        List<Cell> neighbors = new ArrayList<Cell>();
        Cell vStart = null;
        if (cells.contains(start)) {
            vStart = cells.get(cells.indexOf(start));
            setChanged();
            notifyObservers(graph.getVertex(vStart));
        } else {return;}

        vStart.setWasVisited(true);
        neighbors = getNeighbors(vStart);
        // If the current cell has any neighbors which have not been visited
        // Choose randomly one of the unvisited neighbors
        Cell randomNeighbor = null;

        while (!neighbors.isEmpty()) {
            randomNeighbor = cells.get(cells.indexOf(neighbors.remove(RANDOM.nextInt(neighbors.size()))));
            if (!randomNeighbor.wasVisisted()) {
                if (!isSolved) {
                    graph.solution.addEdge(start, randomNeighbor);
                }
                if (randomNeighbor.equals(mazeFinish)) {
                    isSolved = true;
                }
                graph.addEdge(vStart, randomNeighbor);
                setChanged();
                notifyObservers(graph.getVertex(vStart));
                if (debug) {
                    display();
                }
                recursiveDepthFirst(randomNeighbor, cells);
            }
        }

        if (!isSolved) {
            Vertex<Cell> source = graph.solution.getVertex(start);
            Vertex<Cell> neighbor = graph.solution.getVertex(randomNeighbor);
            if (source.adj.size() > 0) {
                Edge<Cell> sourceEdge = source.adj.remove(source.adj.size()-1);
            }
            if(neighbor.adj.size() > 0){
                Edge<Cell> destEdge =  neighbor.adj.remove(neighbor.adj.size()-1);
                graph.solution.addEdge(destEdge.source.value, destEdge.dest.value);
            }
            if (source.adj.size() == 0) {
                graph.solution.vertexMap.remove(source.value);
            }
            if(neighbor.adj.size() == 0){
                graph.solution.vertexMap.remove(neighbor.value);
            }
        }
    }

    /**
     * <p>Generates a maze using the recursive backtracker algorithm
     */
    public void createMazeRecursiveBacktracker() {
        graph.clearAll();
        graph = new Graph<Cell>();
        // Initial starting cell
        ArrayList<Cell> cells = new ArrayList<Cell>();
        List<Cell> currentNeighbors;    
        Stack<Cell> stack = new Stack<Cell>();
        Stack<Cell> solutionStack = new Stack<Cell>();

        // Start with a list of all cells that belong in the maze
        for (int row = 0; row <= depth; row++) {
            for (int column = 0; column <= width; column++) {
                cells.add(new Cell(column, row));
            }
        }

        // Make the initial cell the current cell and mark it as visited
        int unvisitedCells = cells.size();
        Cell start = new Cell(cells.get(cells.indexOf(mazeStart)).getX(), cells.get(cells.indexOf(mazeStart)).getY());

        start.setWasVisited(true);
        unvisitedCells--;
        stack.push(start);
        solutionStack.push(start);
        //While there are unvisited cells
        while(unvisitedCells > 0 && start != null) {
            currentNeighbors = getNeighbors(start);
            // If the current cell has any neighbors which have not been visited
            // Choose randomly one of the unvisited neighbors
            Cell randomNeighbor = null;
            if (!currentNeighbors.isEmpty()) { 
                for(Cell c: cells) {
                    if (currentNeighbors.contains(c) && c.wasVisisted()) {
                        currentNeighbors.remove(c);
                    }
                }
            }
            //Push the current cell to the stack
            //          Remove the wall between the current cell and the chosen cell
            //          Make the chosen cell the current cell and mark it as visited
            if (!currentNeighbors.isEmpty()) {
                randomNeighbor = cells.get(cells.indexOf(currentNeighbors.get(RANDOM.nextInt(currentNeighbors.size()))));
                randomNeighbor.setWasVisited(true);
                graph.addEdge(start, randomNeighbor);
                setChanged();
                notifyObservers(graph.getVertex(start));
                setChanged();
                notifyObservers(graph.getVertex(randomNeighbor));
                if (debug) {
                    display();
                }
                unvisitedCells--;
                stack.push(start);
            } else {
                if (!stack.isEmpty()) {
                    randomNeighbor = stack.pop();
                    setChanged();
                    notifyObservers(graph.getVertex(randomNeighbor));
                }

            }
            start = randomNeighbor;
        }
        graph.dijkstra(graph.getVertex(mazeStart).value);
        graph.generateSolutionGraph(mazeFinish);

        // Maze is complete, time to let everyone know!
        notifyAllObservers(mazeStart, mazeFinish);

        System.out.println("Finished creating maze using a recursive backtracking algorithm");
        display(true);
    }

    /**
     * <p> Helper method to notify observers of the starting and ending coordinates
     * once the graph has been solved. 
     * @param startingCell Origin of maze path. 
     * @param finishCell Exit location of maze path.
     */
    private void notifyAllObservers(Cell startingCell, Cell finishCell) {
        if (graph.solution != null && graph.solution.contains(startingCell) && graph.solution.contains(finishCell)) {
            setChanged();
            notifyObservers(COMMAND_IS_SOLUTION);
            setChanged();
            notifyObservers(graph.solution.getVertex(startingCell));

            setChanged();
            notifyObservers(COMMAND_IS_SOLUTION);
            setChanged();
            notifyObservers(graph.solution.getVertex(finishCell));

            // Send over the solutions
            for (Vertex<Cell> v: graph.solution.getVertexes()) {
                setChanged();
                notifyObservers(COMMAND_IS_SOLUTION);
                setChanged();
                notifyObservers(v);
            }
        }

        setChanged();
        notifyObservers(COMMAND_MAZE_COMPLETE);
    }

    /**
     * <p>
     * Generate a maze using Prims Algorithm randomized from Wikipedia.
     * </p>
     * 
     * <p>
     * <ol>
     * <li>Start with a grid full of walls.
     * <li>Pick a cell, mark it as part of the maze. Add the walls of the cell
     * to the wall list.
     * <li>While there are walls in the list:
     * <ul>
     * <li>Pick a random wall from the list.
     * <li>If only one of the two cells that the wall divides is visited,
     * then:<br>
     * Make the wall a passage and mark the unvisited cell as part of the maze.
     * <li>Add the neighboring walls of the cell to the wall list.
     * <li>Remove the wall from the list.
     * </ul>
     * </ol>
     */
    public void createMazePrimsHorizontalBias() {
        graph.clearAll();
        graph = new Graph<Cell>();
        // Initial starting cell
        ArrayList<Cell> cells = new ArrayList<Cell>();
        ArrayList<Cell> walls = new ArrayList<Cell>();;    

        // Start with a list of all cells that belong in the maze
        for (int row = 0; row < depth; row++) {
            for (int column = 0; column < width; column++) {
                cells.add(new Cell(column, row));
            }
        }

        // 3. Repeat step 2 until all vertices are connected.

        // 1. Choose any starting vertex.
        Cell start = mazeStart;

        // Add all of its neighbors to the wall list and choose one to be in the maze
        walls.addAll(getNeighbors(start));
        Cell randomNeighbor = walls.remove(RANDOM.nextInt(walls.size()));
        graph.addEdge(start, randomNeighbor);

        // Notify to our maze that we have our first possible path
        setChanged();
        notifyObservers(graph.getVertex(start));
        if (debug) {
            display();
        }

        // While there are still walls in the walls list.
        while (walls.size() > 0) {

            // 2. Choose the random edge from any previously-chosen vertex to an unchosen vertex.
            // Choose one of its neighbors at random to be in the maze
            // Now add all of the neighbors to the possible walls list which are not in G
            for (Vertex<Cell> v: graph.getVertexes()) {
                List<Cell> neighborsOfV = getNeighborsNotInGraph(v.value, graph);
                for(Cell c : neighborsOfV) {
                    if (!walls.contains(c)) {
                        walls.add(c);
                    }
                }
            }

            // Grab a random neighbor not yet in G
            randomNeighbor = walls.remove(RANDOM.nextInt(walls.size()));

            // Find an adjacent neighbor who is in G
            start = getNeighborInGraph(randomNeighbor, graph);
            graph.addEdge(start, randomNeighbor);
            setChanged();
            notifyObservers(graph.getVertex(start));
            setChanged();
            notifyObservers(graph.getVertex(randomNeighbor));
            if (debug) {
                display();
            }
        }

        graph.dijkstra(graph.getVertex(mazeStart).value);
        System.out.println("Finished creating maze using Prim's algorithm");
        graph.generateSolutionGraph(mazeFinish);
        notifyAllObservers(mazeStart, mazeFinish);
        display(true);
    }

    /**
     * <p>Helper method to return neighbor of a desired cell that is 
     * in the graph. 
     * @param cell desired cell to find neighbors of.
     * @param graph that contains cell. 
     * @return a neighbor of the cell. 
     */
    private Cell getNeighborInGraph(Cell cell, Graph<Cell> graph) {
        List<Cell> neighbors = new ArrayList<Cell>();
        Cell possibleNeighbor = null;
        
        // Add east and west neighbors only
        for(Cell neighbor: getNeighbors(cell)) {
            if(graph.contains(neighbor)  && neighbor.equals(getEastNeighbor(cell))) {
                neighbors.add(graph.getVertex(neighbor).value);
            }else if(graph.contains(neighbor)  && neighbor.equals(getWestNeighbor(cell))) {
                neighbors.add(graph.getVertex(neighbor).value);
            }
        }
        
        if (neighbors.size() < 1) {
            for(Cell neighbor: getNeighbors(cell)) {
                if(graph.contains(neighbor)) {
                    neighbors.add(graph.getVertex(neighbor).value);
                }
            }
        }
        
        if (!neighbors.isEmpty()) {
            // If east neighbor go east, if west neighbor go west else pick randomly north or south
            possibleNeighbor = neighbors.get(RANDOM.nextInt(neighbors.size())); /// ##########################
        }
        return possibleNeighbor;
    }

    /**
     * <p>Helper method that takes a cell and the graph containing it as a parameter. Returns a randomly 
     * selects and returns a neighboring cell. 
     * 
     * @param cell that to retrieve a random neighbor.
     * @param graph containing the cell.
     * @return randomly selected neighboring cell. 
     */
    private Cell getRandomNeighborInGraph(Cell cell, Graph<Cell> graph) {
        List<Cell> neighbors = new ArrayList<Cell>();
        Cell possibleNeighbor = null;
        for(Cell neighbor: getNeighbors(cell)) {
            if(graph.contains(neighbor)) {
                neighbors.add(graph.getVertex(neighbor).value);
                //                possibleNeighbor = graph.getVertex(neighbor).value;
            }
        }
        if (!neighbors.isEmpty()) {
            possibleNeighbor = neighbors.get(RANDOM.nextInt(neighbors.size()));
        }
        return possibleNeighbor;
    }

    /**
     * <p>Helper method to retrieve a selected cell's possible neighbors. 
     * @param cell desired to retrieve neighbors not located in graph. 
     * @param graph containing the cell. 
     * @return the list of cells not located in the graph. 
     */
    private List<Cell> getNeighborsNotInGraph(Cell cell, Graph<Cell> graph) { //// ######################################
        List<Cell> allPossibleNeighbors = new ArrayList<Cell>();
        for(Vertex<Cell> v: graph.getVertexes()) {
            for(Cell neighbor: getNeighbors(v.value)) {
                // Add north neighbor
                if (!graph.contains(neighbor)) {
                    allPossibleNeighbors.add(neighbor);
                }
            }
        }
        return allPossibleNeighbors;
    }

    /**
     * <p>Method tasked with retrieving a list of all existing neighbors of particular cell. 
     * @param cell desired to retrieve list containing all existing neighbors.
     * @return list containing all neighboring cells. 
     */
    public ArrayList<Cell> getNeighbors(Cell cell) {
        ArrayList<Cell> allPossibleNeighbors = new ArrayList<Cell>();
        int x = cell.getX(), y = cell.getY();
        // All possible neighbors within reach from our current position
        Cell northNeighbor = new Cell(x, y - 1);
        Cell eastNeighbor = new Cell(x + 1, y);
        Cell southNeighbor = new Cell(x, y + 1);
        Cell westNeighbor = new Cell(x - 1, y);

        // Add north neighbor
        if (northNeighbor.getY() >= 0) {
            allPossibleNeighbors.add(northNeighbor);
        }

        // Add east neighbor
        if (eastNeighbor.getX() < width) {
            allPossibleNeighbors.add(eastNeighbor);
        }

        // Add south neighbor
        if (southNeighbor.getY() < depth) {
            allPossibleNeighbors.add(southNeighbor);
        }

        // Add west neighbor
        if (westNeighbor.getX() >= 0) {
            allPossibleNeighbors.add(westNeighbor);
        }
        return allPossibleNeighbors;
    }

    /**
     * <p>Method tasked with retrieving a list of all existing neighbors of particular cell. 
     * @param cell desired to retrieve list containing all existing neighbors.
     * @return list containing all neighboring cells. 
     */
    public boolean isValidNeighbor(Cell cell) {
        boolean isValid = true;
        int x = cell.getX(), y = cell.getY();
        // All possible neighbors within reach from our current position
        Cell northNeighbor = new Cell(x, y - 1);
        Cell eastNeighbor = new Cell(x + 1, y);
        Cell southNeighbor = new Cell(x, y + 1);
        Cell westNeighbor = new Cell(x - 1, y);

        // Add north neighbor
        if (!(northNeighbor.getY() >= 0)) {
            isValid = false;
        }

        // Add east neighbor
        if (!(eastNeighbor.getX() < width)) {
            isValid = false;
        }

        // Add south neighbor
        if (!(southNeighbor.getY() < depth)) {
            isValid = false;
        }

        // Add west neighbor
        if (!(westNeighbor.getX() >= 0)) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * <p>Helper method to retrieve a selected cell's north neighbor. 
     * @param cell desired to retrieve neighbor to the north. 
     * @return the cell located to the north if it exists, else null. 
     */
    public Cell getNorthNeighbor(Cell cell) {
        Cell northNeighbor = null;
        /* The current cell's x and y coordinate */
        int x = cell.getX(), y = cell.getY();
        // All possible neighbors within reach from our current position
        Cell possibleNorthNeighbor = new Cell(x, y - 1);
        // Add north neighbor
        if (possibleNorthNeighbor.getY() >= 0) {
            northNeighbor = possibleNorthNeighbor;
        }
        return northNeighbor;
    }

    /**
     * <p>Helper method to retrieve a selected cell's east neighbor. 
     * @param cell desired to retrieve neighbor to the north. 
     * @return the cell located to the north if it exists, else null. 
     */
    public Cell getEastNeighbor(Cell cell) {
        Cell eastNeighbor = null;
        /* The current cell's x and y coordinate */
        int x = cell.getX(), y = cell.getY();
        // All possible neighbors within reach from our current position
        Cell possibleEastNeighbor = new Cell(x + 1, y);
        // Add north neighbor
        if (possibleEastNeighbor.getX() < width) {
            eastNeighbor = possibleEastNeighbor;
        }
        return eastNeighbor;
    }

    /**
     * <p>Helper method to retrieve a selected cell's south neighbor. 
     * @param cell desired to retrieve neighbor to the north. 
     * @return the cell located to the north if it exists, else null. 
     */
    public Cell getSouthNeighbor(Cell cell) {
        Cell southNeighbor = null;
        /* The current cell's x and y coordinate */
        int x = cell.getX(), y = cell.getY();
        // All possible neighbors within reach from our current position
        Cell possibleSouthNeighbor = new Cell(x, y + 1);
        // Add north neighbor
        if (possibleSouthNeighbor.getY() < depth) {
            southNeighbor = possibleSouthNeighbor;
        }
        return southNeighbor;
    }

    /**
     * <p>Helper method to retrieve a selected cell's west neighbor. 
     * @param cell desired to retrieve neighbor to the north. 
     * @return the cell located to the north if it exists, else null. 
     */
    public Cell getWestNeighbor(Cell cell) {
        Cell westNeighbor = null;
        /* The current cell's x and y coordinate */
        int x = cell.getX(), y = cell.getY();
        // All possible neighbors within reach from our current position
        Cell possibleWestNeighbor = new Cell(x - 1, y);
        // Add north neighbor
        if (possibleWestNeighbor.getX() >= 0) {
            westNeighbor = possibleWestNeighbor;
        }
        return westNeighbor;
    }


    /**
     * <p>
     * Displays a graphical representation of the current maze to the users
     * standard output.<br>
     * <ul>
     * <li>'X' represents a wall
     * <li>'V' represents a possible path.
     * <li>'+' represents the correct path travel the shortest path thru the
     * maze.
     * </ul>
     */
    void display() {
        display(false);
    }

    /**
     * <p>Overloaded method to assist display when debug option is selected the method will
     * print each step of the maze as it is traversed in while locating the shortest path.
     * Once the shortest path is located the solution is printed with the shortest path populated 
     * with a "+" symbol. 
     * @param showSolution
     */
    void display(boolean showSolution) {

        final String WALL = "X ";

        /** The string literal V. */
        final String PATH = "V ";
        final String PADDING = "  ";
        final String SHORTEST_PATH = "+ ";

        StringBuilder sb = new StringBuilder();

        sb.append(WALL);
        for (int i = 0; i < width; i++) {
            Cell cell = new Cell(i, 0);
            if (cell.equals(mazeStart)) {
                sb.append(PADDING + WALL);
            } else {
                sb.append(WALL + WALL);
            }
        }
        sb.append("\n");

        /* For each row */
        for (int row = 0; row < depth; row++) {
            /* For each North and East Neighbor */
            if(row > 0){sb.append(WALL);}
            for (int column = 0; column < width && row > 0; column++) {
                /* The current cell that should be written to a string. */
                Cell cell = new Cell(column, row);
                Cell northNeighbor = getNorthNeighbor(cell);
                // If a vertex exists append a PATH else append some PADDING
                if (graph.isAdjacent(northNeighbor, cell)) {
                    sb.append(PADDING);
                } else {
                    sb.append(WALL);
                }
                sb.append(WALL);

                if (column == width - 1) {
                    sb.append("\n"); // A happy little new line makes things more readable.
                }
            } /* Finished appending North and East Neighbor */


            /* For each vertex and it's east neighbor */
            /* Append the left impenetrable wall of the maze */
            sb.append(WALL);
            for (int column = 0; column < width; column++) {
                /* The current cell that should be written to a string. */
                Cell cell = new Cell(column, row);
                Cell rightNeighbor = getEastNeighbor(cell);
                // If a vertex exists append a PATH else append some PADDING
                if (graph.contains(cell)) {
                    if (showSolution && graph.solution.contains(cell)) {
                        sb.append(SHORTEST_PATH);
                    } else if (showSolution && !graph.solution.contains(cell)) {
                        sb.append(PADDING);
                    } else {
                        sb.append(PATH);
                    }
                } else {
                    sb.append(PADDING);
                }
                // If we have a path to our neighbor we print more padding OR
                // we print an X representing a wall.
                if (graph.isAdjacent(cell, rightNeighbor)) {
                    sb.append(PADDING);
                } else {
                    sb.append(WALL);
                }
                if (column == width - 1) {
                    sb.append("\n"); // A happy little new line makes things more readable.
                }
            }/* Finished appending each vertex and it's east neighbor */


        } /* Finished with row */

        sb.append(WALL);
        for (int i = 0; i < width; i++) {
            Cell cell = new Cell(i, depth - 1);
            if (cell.equals(mazeFinish)) {
                sb.append(PADDING + WALL);
            } else {
                sb.append(WALL + WALL);
            }
        }
        sb.append("\n");
        System.out.println(sb.toString());
    }

    @Override 
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Width: " + width + "\n");
        sb.append("Depth: " + depth + "\n");
        sb.append("Debug: " + debug + "\n");
        sb.append("Maze Start: " + mazeStart + "\n");
        sb.append("Maze Finish: " + mazeFinish + "\n");
        sb.append("Graph: " + graph + "\n\n");
        return sb.toString();
    }// End of toString()

}

/**
 * Cell class, creates cells out of a set of points 
 */
class Cell implements Comparable<Cell> {

    /* The x coordinate of this cell. */
    private int x;

    /* The y coordinate of this cell. */
    private int y;

    /* Boolean to let us know we've been to this cell before. */
    private boolean wasVisited;

    /**
     * Creates a Cell for the given coordinates.<br>
     * 
     * @param x the X coordinate.
     * @param y the Y coordinate.
     */
    public Cell(int x, int y) {
        this(x, y, false);
    }

    /**
     * Creates a Cell for the given coordinates of the given feature type.<br>
     * The boolean lets us know whether this cell has been visited by our maze creation algorithm before.
     * 
     * @param x the X coordinate.
     * @param y the Y coordinate.
     */
    public Cell(int x, int y, boolean wasVisited) {
        this.x = x;
        this.y = y;
        this.wasVisited = wasVisited;
    }

    /**
     * Returns the X coordinate.
     * 
     * @return the X coordinate of the Point.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the Y coordinate.
     * 
     * @return the Y coordinate of the Point.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns true if the cell has been visited while traversing the graph
     * else false. 
     * @return boolean value true of false indicatig visited or not. 
     */
    public boolean wasVisisted() {
        return wasVisited;
    }

    /**
     * Set method to change visited status of cell. True if visited else false.
     * @param wasVisited boolean value to set cell visited status. 
     */
    public void setWasVisited(boolean wasVisited) {
        this.wasVisited = wasVisited;
    }

    /**
     * Equals method the compares cells by there x and y coordinates. 
     * True if equal false otherwise. 
     */
    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other == this) {
            result = true;
        } else if (other != null && other.getClass() == getClass()) {
            final Cell p = (Cell) other;
            result = x == p.x && y == p.y;
        }
        return result;
    }

    /**
     * Hashcode method created to ensure that the objects hashcodes are 
     * correct due to do overriding equals method. 
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Returns the cell as string value. 
     */
    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    /**
     * Compares cells to provide a natural ordering.
     */
    @Override
    public int compareTo(Cell o) {
        int result = 0;
        result = Integer.compare(x, o.x);
        if (result == 0) {
            result = Integer.compare(y, o.y);
        }
        return result;
    }
}