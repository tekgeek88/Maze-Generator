import java.awt.EventQueue;

/**
 * <h1>TCSS 342 ­ Data Structures Assignment 5 ­- Maze Generator.</h1>
 * <p>
 * A controller class that uses the Maze class to generate 2d mazes with
 * solutions<br>
 * </p>
 * Sources:
 * <ul>
 * <li>Data Structures and Problem Solving Using Java 4th Edition by Mark Allen Weiss
 * </li>
 * <li>https://en.wikipedia.org/wiki/Maze_generation_algorithm</li>
 * </ul>
 * 
 * @author Carl Argabright
 * @author Luke Gillmore
 * @version May 18th, 2018
 */
public class Main {

    /**
     * Controller class for the maze generator.
     * @param args Command line args are ignored.
     */
    public static void main(String[] args) {

        Maze maze = new Maze( 25, 25, false);
        
        // Example of using the optional constructor to generate a maze with a custom start

        // Maze largerMaze = new Maze( 4, 4, false);
        // largerMaze.createMazeDepthFirstRecursive();
        
        launchMazeGui(maze);
        
        
        //      testMaze();
    }

    /**
     * <p>Opens a new window and visually draws the given maze using the javax.swing library.
     * 
     * @param maze
     *          The desired maze to be launched.
     */
    private static void launchMazeGui(final Maze maze) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MazeGUI(maze);     
            }
        });
    }

    public static void testMaze() {

    	System.out.println("********** Start Maze Tests *************");
        
        Maze maze = new Maze(5, 5, true);
        
        System.out.println("Prims maze: \n Dimensions: 5 by 5 \n Random start location: Debug on \n Visual print out is padded for appearance.");
        maze.createMazePrims();
    
        System.out.println("Prims Horizontal bias maze: \n Dimensions: 5 by 5\n Random start location: Debug on \n Visual print out is padded for appearance.");   
        maze.createMazePrimsHorizontalBias();
        System.out.println("Recursive back tracker maze 5 by 5 random start location: Debug on \n Visual print out is padded for appearance.");     
        maze.createMazeRecursiveBacktracker();
        
        Maze maze2 = new Maze(5, 5, false);
        System.out.println("Launching new maze without debug");
        maze2.createMazeRecursiveBacktracker();
        System.out.println("Maze complete: \n" + maze2.toString());
        
        
        System.out.println("Now testing mazes with diffferent dimensions");
        System.out.println("Maze entered with dimensions of (-1, -1) with debug true, expected result is a (4 , 4) maze ");
        Maze negative = new Maze(-1, -1, true);
        negative.createMazeRecursiveBacktracker();
        System.out.println("Now testing larger dimension maze that is (15, 15) debug set to true.");
        Maze larger = new Maze(15, 15, true);
        larger.createMazeRecursiveBacktracker();
        System.out.println("Now tesing maze that has uneven dimensions (10, 5)");
        Maze uneven1 = new Maze(10, 5, true);
        uneven1.createMazeRecursiveBacktracker();
        System.out.println("Now testing maze two with uyneven dimensions (7, 11)");
        Maze uneven2 = new Maze(7, 11, true);
        uneven2.createMazeRecursiveBacktracker();
        
    }
}
