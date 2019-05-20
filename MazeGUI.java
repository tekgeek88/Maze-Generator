import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * MazeGUI class provides an interactive user interface to allow the user 
 * a visual representation of the graph traversal and solution. The user has the 
 * ability to manipulate the speed and desired algorithm used to solve the maze.
 *  
 * @author Carl Argabright
 * @author Luke Gillmore
 * @version May 18th, 2018
 * 
 */
public class MazeGUI extends JFrame implements ActionListener {

	/** An automatically generated serialUID. */
	private static final long serialVersionUID = 8355454359476512258L;

	/** The Abstract Window ToolKit used to help position the GUI. */
	private static final Toolkit KIT = Toolkit.getDefaultToolkit();

	/** The Dimension of the current displays screen. */
	private static final Dimension SCREEN_SIZE = KIT.getScreenSize();

	/** The size in pixels of a side of one "square" on the grid. */
	private static int SQUARE_SIZE = 40;

	/** The offset in pixels of the debug messages drawn for each square. */
	private static final int DEBUG_OFFSET = 20;

	/** The initial frames per second at which the simulation will run. */
	private static final int INITIAL_FRAMES_PER_SECOND = 10;

	/** The minimum frames per second at which the simulation will run. */
	private static final int MIN_FRAMES_PER_SECOND = 0;

	/** The maximum frames per second at which the simulation will run. */
	private static final int MAX_FRAMES_PER_SECOND = 100;

	/** The numerator for delay calculations. */
	private static final int MY_DELAY_NUMERATOR = 1000;

	/** The minor tick spacing for the FPS slider. */
	private static final int MINOR_TICK_SPACING = 25;

	/** The major tick spacing for the FPS slider. */
	private static final int MAJOR_TICK_SPACING = 25;

	/** The Start command. */
	private static final String START_COMMAND = "Start";

	/** The Stop command. */
	private static final String STOP_COMMAND = "Stop";

	/** The Step command. */
	private static final String STEP_COMMAND = "Step";

	/** The Reset command. */
	private static final String RESET_COMMAND = "Reset";

	private static final String COMMAND_CREATE_PRIMS_RANDOM = "Prim's Algorithm";

	private static final String COMMAND_CREATE_PRIM_HORIZONTAL_BIAS = "Prim's Algorithm (horizontal bias)";

	private static final String COMMAND_CREATE_RECURSIVE_DEPTH_FIRST = "Recursive depth first";

	private static final String COMMAND_CREATE_RECURSIVE_BACKTRACK_STACK = "Recursive Backtracker";


	/** The stroke used for painting. vertexis */
	private static final BasicStroke STROKE = new BasicStroke(SQUARE_SIZE/4, BasicStroke.CAP_SQUARE,
			BasicStroke.JOIN_MITER, 2,
			new float[] {2, 2, 2, 2}, 0);

	/** The stroke used for painting solutions. */
	private static final BasicStroke STROKE_SOLUTION = new BasicStroke(SQUARE_SIZE/6, BasicStroke.CAP_SQUARE,
			BasicStroke.JOIN_MITER, 2,
			new float[] {2, 2, 2, 2}, 0);

	/** The panel we use to draw the maze on. */
	private MazePanel mazePanel;

	/** The maze used for display. */
	private Maze maze;

	/** The maze used for display. */
	private int MAX_NUMBER_OF_STEPS;    ////////////////////// marked for deletion.. 

	/** A boolean used to represent the current debug mode state.*/
	private boolean debugFlag;

	/** The delay between updates, based on the frames per second setting. */
	private int myDelay;

	/** A timer used to update the state of the simulation. */
	private final Timer myTimer;

	/** The slider for "frames per second". */
	private JSlider mySlider;

	boolean isMazeRecieved = false;
	boolean isMazeGenerated = false;
	boolean isAnimating = false;

	Queue<Vertex<Cell>> vertexPrinterQueue;
	LinkedList<Vertex<Cell>> vertexSolutionPrinterQueue;

	private ArrayList<Vertex<Cell>> vertexPrinter;

	private ArrayList<Vertex<Cell>> vertexSolutionPrinter;

	/**
	 * MazeGUI constructor. Accepts a maze to be solved and illustrates the 
	 * the process of traversing the maze to find the shortest path using the users
	 * desired algorithm. 
	 * @param maze to be illustrated. 
	 */
	public MazeGUI(final Maze maze) {
		super("The amazing maze generator!");
		this.maze = maze;
		debugFlag = maze.debug;
		vertexPrinter = new ArrayList<Vertex<Cell>>();
		vertexSolutionPrinter = new ArrayList<Vertex<Cell>>();
		myDelay = MY_DELAY_NUMERATOR / INITIAL_FRAMES_PER_SECOND;
		myTimer = new Timer(myDelay, this);

		SQUARE_SIZE = SCREEN_SIZE.height/4/maze.depth*2+1;
		// 1800*.25/(5*2+1)
		vertexPrinterQueue = new LinkedList<Vertex<Cell>>();
		vertexSolutionPrinterQueue = new LinkedList<Vertex<Cell>>();
		initializeGUI();
		MAX_NUMBER_OF_STEPS = Integer.MAX_VALUE;
		setVisible(true);
	}

	/**
	 * Private helper method used to setup and generate the display.
	 */
	private void initializeGUI(){

		// Create the JSlider that can be used to speed up or slow down the maze creation
		mySlider = createJSlider();

		final JCheckBox box = new JCheckBox("Debug Mode", maze.debug);
		box.addActionListener(this);
		final Container northPanel = new JPanel(new FlowLayout());
		northPanel.add(makeButton(START_COMMAND));
		northPanel.add(makeButton(STOP_COMMAND));
		northPanel.add(makeButton(STEP_COMMAND));
		northPanel.add(makeButton(RESET_COMMAND));

		Container eastPanel = new JPanel(new BorderLayout());
		Container buttonPanel = new JPanel(new GridLayout(0, 1));
		eastPanel.add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.add(new JLabel("Generate Mazes:"));
		buttonPanel.add(makeButton(COMMAND_CREATE_PRIMS_RANDOM));
		buttonPanel.add(makeButton(COMMAND_CREATE_PRIM_HORIZONTAL_BIAS));
		buttonPanel.add(makeButton(COMMAND_CREATE_RECURSIVE_BACKTRACK_STACK));
		buttonPanel.add(makeButton(COMMAND_CREATE_RECURSIVE_DEPTH_FIRST));


		final Container southPanel = new JPanel(new FlowLayout());
		southPanel.add(new JLabel("FPS: "));
		southPanel.add(mySlider);
		southPanel.add(box);

		// set up graphical components
		mazePanel = new MazePanel();
		maze.addObserver(mazePanel);

		final JPanel masterPanel = new JPanel(new BorderLayout());
		masterPanel.add(mazePanel);
		masterPanel.add(northPanel, BorderLayout.NORTH);
		masterPanel.add(southPanel, BorderLayout.SOUTH);
		masterPanel.add(eastPanel, BorderLayout.EAST);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		add(masterPanel);
		pack();

		// position the frame in the center of the screen
		setLocation(SCREEN_SIZE.width / 2 - getWidth() / 2,
				SCREEN_SIZE.height / 2 - getHeight() / 2);
	}

	/**
	 * Private helper method used to create a JButton with the given text.
	 * 
	 * @param theText The text.
	 * @return a new JButton.
	 */
	private JButton makeButton(final String theText) {
		final JButton button = new JButton(theText);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.addActionListener(this);
		return button;
	}

	/**
	 *<p> Private helper method used to create the JSlider in maze.
	 * The JSlider sends notifications to observers the increase the Frames per second 
	 * at which the graph is animated.  
	 * 
	 * @return
	 */
	private JSlider createJSlider() {
		JSlider jSlider;

		jSlider = new JSlider(SwingConstants.HORIZONTAL, MIN_FRAMES_PER_SECOND, MAX_FRAMES_PER_SECOND,
				INITIAL_FRAMES_PER_SECOND);
		jSlider.setMajorTickSpacing(MAJOR_TICK_SPACING);
		jSlider.setMinorTickSpacing(MINOR_TICK_SPACING);
		jSlider.setPaintLabels(true);
		jSlider.setPaintTicks(true);
		jSlider.setPreferredSize(new Dimension(maze.width * SQUARE_SIZE, 100));
		jSlider.addChangeListener(new ChangeListener() {
			/** Called in response to slider events in this window. */
			@Override
			public void stateChanged(final ChangeEvent theEvent) {
				final int value = jSlider.getValue();
				if (value > 0) {
					myDelay = MY_DELAY_NUMERATOR / value;
					myTimer.setDelay(myDelay);
				}
			}
		});
		return jSlider;

	}
	/**
	 *<p> Method the provides actions to the GUI buttons 
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent theEvent) {
		/**
		 * A notification method called in response to action events in this window.
		 * 
		 * @param theEvent The action event that triggered the call.
		 */
		final Object source = theEvent.getSource();
		if (source.equals(myTimer)) {
			// event came from the timer
			if (isAnimating) {
				advanceAnimation();
			} else {
				myTimer.stop();
			}
		} else if (source instanceof JCheckBox) {
			// event came from the debug box
			final JCheckBox box = (JCheckBox) source;
			debugFlag = maze.debug = box.isSelected();
			mazePanel.repaint();
		} else {
			// event came from one of the buttons
			final String command = theEvent.getActionCommand().intern();
			if (command.equals(START_COMMAND)) {
				reset();
				maze.createMazeRecursiveBacktracker();
				isAnimating = true;
				myTimer.start();
			} else if (command.equals(STOP_COMMAND)) {
				isAnimating = false;
				myTimer.stop();
			} else if (command.equals(STEP_COMMAND)) {
				advanceAnimation();
			} else if (command.equals(RESET_COMMAND)) {
				isAnimating = false;
				reset();
			} else if (command.equals(COMMAND_CREATE_PRIMS_RANDOM)) {
				reset();
				//                maze.createMazePrims();
				maze.createMazePrims();;
				isAnimating = true;
				myTimer.start();
			} else if (command.equals(COMMAND_CREATE_RECURSIVE_BACKTRACK_STACK)) {
				reset();
				maze.createMazeRecursiveBacktracker();
				isAnimating = true;
				myTimer.start();
			} else if (command.equals(COMMAND_CREATE_PRIM_HORIZONTAL_BIAS)) {
				reset();
				maze.createMazePrimsHorizontalBias();
				isAnimating = true;
				myTimer.start();
			} else if (command.equals(COMMAND_CREATE_RECURSIVE_DEPTH_FIRST)) {
				reset();
				maze.createMazeDepthFirstRecursive();
				isAnimating = true;
				myTimer.start();
			}

		}
	}

	/**
	 * Advances the simulation by one frame of animation, moving each vehicle
	 * once and checking collisions.
	 */
	private void advanceAnimation() {
		if (isMazeRecieved) {
			if(!vertexPrinterQueue.isEmpty()) {
				vertexPrinter.add(vertexPrinterQueue.poll());
			} else {
				isMazeGenerated = true;
			}
			if(isMazeGenerated && !vertexSolutionPrinterQueue.isEmpty()) {
				vertexSolutionPrinter.add(vertexSolutionPrinterQueue.poll());
			}
			if (isMazeGenerated && vertexSolutionPrinterQueue.isEmpty()) {
				isAnimating = false;
				myTimer.stop();
			}
		}
		mazePanel.repaint();
	}

	/**
	 * Resets all the vehicles to their initial locations, resets the tick
	 * counter, and stops the simulation.
	 */
	private void reset() {
		myTimer.stop();
		isAnimating = false;
		isMazeGenerated = false;
		vertexPrinter.clear();
		vertexPrinterQueue.clear();
		vertexSolutionPrinterQueue.clear();
		vertexSolutionPrinter.clear();
		mazePanel.initializePanel();
	}


	/**
	 * A drawing panel for the maze.
	 */
	private class MazePanel extends JPanel implements Observer {

		/** An automatically generated serialUID. */
		private static final long serialVersionUID = 726349612L;

		/** The font used by this panel. */
		private final Font myFont = new Font("SansSerif", Font.BOLD, 9);

		private final Color COLOR_WALL = Color.BLACK;

		private final Color COLOR_FLOOR = Color.WHITE;

		private final Color COLOR_PATH = Color.BLUE.darker().darker();

		private BufferedImage buffimage;


		Vertex<Cell> start;
		Vertex<Cell> finish;
		boolean isSolution;
		private HashMap<Shape, Shape> shapes;

		/**
		 * Constructs a new panel.
		 */
		MazePanel() {
			setMinimumSize((new Dimension(4 * SQUARE_SIZE,
					4 * SQUARE_SIZE)));
			setPreferredSize(new Dimension(maze.width * SQUARE_SIZE,
					maze.depth * SQUARE_SIZE));
			setBackground(COLOR_FLOOR);
			setFont(myFont);
			shapes = new HashMap<Shape, Shape>(maze.width*maze.depth);
			initializePanel();

		}

		/**
		 * Initialize the panel displaying the graph ensuring that it is blank
		 * and all fields are not holding erroneous values. 
		 */
		public void initializePanel() {
			isSolution = false;
			shapes.clear();
			start = null;
			finish = null;
			repaint();
		}

		// Instance Methods

		/**
		 * Paints this panel on the screen with the specified Graphics object.
		 * 
		 * @param theGraphics The Graphics object.
		 */
		@Override
		public void paintComponent(final Graphics theGraphics) {
			super.paintComponent(theGraphics);
			final Graphics2D g2 = (Graphics2D) theGraphics;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			if (buffimage == null) {
				// Compute the grid only one time
				int w = this.getWidth();
				int h = this.getHeight();
				buffimage = (BufferedImage)(this.createImage(w,h));
				Graphics2D gc = buffimage.createGraphics();
				gc.setColor(Color.WHITE);
				for (int x=0; x < w; x += 1) {
					gc.drawLine(x, 0, x, h);
				}
				for (int y=0; y<h; y+=1) {
					gc.drawLine(0, y, w, y);
				}
			}
			g2.drawImage(buffimage, null, 0, 0);
			// #########################################################
			// ##                 Draw the walls
			// #########################################################
			if (isMazeRecieved && !vertexPrinter.isEmpty()) {
				while (!vertexPrinter.isEmpty()) {
					Vertex<Cell> currentVertex = vertexPrinter.remove(0);
					if (currentVertex != null) {
						// Ordinate ourselves with the coordin8tes            
						final int topy = (currentVertex.value.getY() * SQUARE_SIZE);
						final int bottomy = topy + SQUARE_SIZE;
						final int leftx = currentVertex.value.getX() * SQUARE_SIZE;
						final int rightx = leftx + SQUARE_SIZE;

						Cell currentCell = new Cell(currentVertex.value.getX(), currentVertex.value.getY());
						drawWall(g2, topy, bottomy, leftx, rightx, currentCell, currentVertex);
					}
				}
//				for(Vertex<Cell> currentVertex: vertexPrinter) {
//					if (currentVertex != null) {
//						// Ordinate ourselves with the coordin8tes            
//						final int topy = (currentVertex.value.getY() * SQUARE_SIZE);
//						final int bottomy = topy + SQUARE_SIZE;
//						final int leftx = currentVertex.value.getX() * SQUARE_SIZE;
//						final int rightx = leftx + SQUARE_SIZE;
//
//						Cell currentCell = new Cell(currentVertex.value.getX(), currentVertex.value.getY());
//						drawWall(g2, topy, bottomy, leftx, rightx, currentCell, currentVertex);
//					}
//				}
			}// End of draw a single vertex

			// #########################################################
			// ##                 Draw Solution
			// #########################################################
			if (isMazeGenerated ) {
				drawSolution(g2);

				// #########################################################
				// ##           Draw the start and finish lines
				// #########################################################


				if (start == null && finish == null && !vertexSolutionPrinterQueue.isEmpty()) {
					setFinish(vertexSolutionPrinterQueue.removeFirst());
					setStart(vertexSolutionPrinterQueue.removeFirst());
				}

				if (vertexSolutionPrinter.size() > 2) {
					g2.setStroke(new BasicStroke(SQUARE_SIZE/16));
					g2.setColor(Color.GREEN);
					g2.fill(new Ellipse2D.Double((maze.mazeStart.getX() * SQUARE_SIZE) + (SQUARE_SIZE/4), ((maze.mazeStart.getY() * SQUARE_SIZE)) + (SQUARE_SIZE/4),
							(SQUARE_SIZE/2), (SQUARE_SIZE/2)));
					g2.setColor(Color.RED);
					g2.fill(new Ellipse2D.Double((maze.mazeFinish.getX() * SQUARE_SIZE) + (SQUARE_SIZE / 4), ((maze.mazeFinish.getY() * SQUARE_SIZE)) + (SQUARE_SIZE/4),
							(SQUARE_SIZE/2), (SQUARE_SIZE/2)));

				}
			}

			// #########################################################
			// ##                 Draw Debug info
			// #########################################################
			if (debugFlag) {
				for (int y = 0; y < maze.depth; y++) {
					for (int x = 0; x < maze.width; x++) {
						g2.setColor(Color.BLACK);
						drawDebugInfo(g2, x, y);
					}
				}
			}


		}

		private void drawSolution(final Graphics2D g2) {
			if (vertexSolutionPrinter.size() > 2) {
				g2.setColor(COLOR_PATH);
				g2.setStroke(STROKE_SOLUTION);
				Vertex<Cell> prev = null;
				for(Vertex<Cell> v1: vertexSolutionPrinter) {
					if (v1 != null) {
						g2.setStroke(new BasicStroke(SQUARE_SIZE/4));
						g2.setColor(COLOR_PATH);
						//                            g2.drawLine(leftx, topy, leftx, bottomy);
						//                            g2.draw(new Line2D.Double(leftx, topy, leftx, bottomy));
						if (prev != null) {
							
							g2.draw(new Line2D.Double((prev.value.getX() * SQUARE_SIZE)-(SQUARE_SIZE/2) + SQUARE_SIZE, ((prev.value.getY() * SQUARE_SIZE)) - (SQUARE_SIZE/2) + SQUARE_SIZE,
									(v1.value.getX() * SQUARE_SIZE)-(SQUARE_SIZE/2) + SQUARE_SIZE, ((v1.value.getY() * SQUARE_SIZE)) - (SQUARE_SIZE/2) + SQUARE_SIZE));
							
						}
						//                            g2.fill3DRect(leftx+(SQUARE_SIZE/2), topy-(SQUARE_SIZE/2), 10, 10, true);
						prev = v1;
					}

				}
			}
		}

		private void drawWall(final Graphics2D g2, final int topy, final int bottomy,
				final int leftx, final int rightx, Cell currentCell,
				Vertex<Cell> currentVertex) {
			Graphics2D gc = buffimage.createGraphics();
//			g2.setColor(COLOR_WALL);
//			g2.setStroke(STROKE);
			gc.setColor(COLOR_WALL);
			gc.setStroke(STROKE);
			Cell northNeighbor = maze.getNorthNeighbor(currentCell);
			Cell eastNeighbor = maze.getEastNeighbor(currentCell);
			Cell southNeighbor = maze.getSouthNeighbor(currentCell);
			Cell westNeighbor = maze.getWestNeighbor(currentCell);

			if(northNeighbor == null || currentVertex == null || !maze.graph.isAdjacent(currentCell, northNeighbor)) {
				//                if (!currentVertex.value.equals(maze.mazeStart)) {
				Shape line = new Line2D.Double(leftx, topy, rightx, topy);
//				if (!shapes.containsKey(line)) {
//					shapes.put(line, line);
//				}
//				g2.draw(shapes.get(line));
				gc.draw(line);
				//                }
			}
			if(eastNeighbor == null || currentVertex == null || !currentVertex.containsEdge(currentCell, eastNeighbor)) {
				Shape line = new Line2D.Double(rightx, topy, rightx, bottomy);
//				if (!shapes.containsKey(line)) {
//					shapes.put(line, line);
//				}
//				g2.draw(shapes.get(line));
				gc.draw(line);
			} 
			if(southNeighbor == null || currentVertex == null || !currentVertex.containsEdge(currentCell, southNeighbor)) {
				//                if (!currentVertex.value.equals(maze.mazeFinish)) {
				Shape line = new Line2D.Double(leftx, bottomy, rightx, bottomy);
//				if (!shapes.containsKey(line)) {
//					shapes.put(line, line);
//				}
//				g2.draw(shapes.get(line));
				gc.draw(line);
				//                }
			} 
			if(westNeighbor == null || currentVertex == null || !currentVertex.containsEdge(currentCell, westNeighbor)) {
				Shape line = new Line2D.Double(leftx, topy, leftx, bottomy);
//				if (!shapes.containsKey(line)) {
//					shapes.put(line, line);
//				}
//				g2.draw(shapes.get(line));
				gc.draw(line);
			}
			g2.drawImage(buffimage, null, 0, 0);
		}

		/**
		 * Draws debugging information, if necessary.
		 * 
		 * @param theGraphics The Graphics context to use for drawing.
		 * @param theX The x-coordinate of the street.
		 * @param theY The y-coordinate of the street.
		 */
		private void drawDebugInfo(final Graphics2D theGraphics, final int theX, final int theY) {

			if (debugFlag && maze.depth * maze.width < 200) {
				// draw numbers for the row and column
				final Paint oldPaint = theGraphics.getPaint();
				theGraphics.setPaint(Color.BLACK);

				final int leftx = theX * SQUARE_SIZE;
				final int topy = theY * SQUARE_SIZE;
				Font prevFont = getFont();
				setFont(new Font(getFont().getFontName(), getFont().getStyle(), SQUARE_SIZE/3));
				theGraphics.drawString("  (" + theX + ", " + theY + ")", leftx, topy + DEBUG_OFFSET);
				theGraphics.setPaint(oldPaint);
				setFont(prevFont);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void update(Observable o, Object arg) {
			if (arg instanceof String) {
				String message = (String) arg;
				if (message.equals(Maze.COMMAND_IS_SOLUTION)) {
					isSolution = true;
				} else if (message.equals(Maze.COMMAND_MAZE_COMPLETE)) {
					isMazeRecieved = true;
					isAnimating = true;

				}
			}

			if (arg instanceof Vertex) {
				if (isSolution) {
					Vertex<Cell> vertex = (Vertex<Cell>) arg;
					vertexSolutionPrinterQueue.offer(vertex);
					isSolution = false;
				} else {
					Vertex<Cell> vertex = (Vertex<Cell>) arg;
					vertexPrinterQueue.offer(vertex);
				}
			} 

		}

		public void setStart(Vertex<Cell> start) {
			this.start = start;
		}

		public void setFinish(Vertex<Cell> finish) {
			this.finish = finish;
		}
	} // end of MazeGuiPanel
}
