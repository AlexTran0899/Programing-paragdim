import javax.swing.JFrame;
import java.awt.Toolkit;

public class Game extends JFrame
{
	Model model;
	Controller controller;
	View view;
	public static final String[] ITEMS = {
		"chair",
		"lamp",
		"mushroom",
		"outhouse",
		"pillar",
		"pond",
		"rock",
		"statue",
		"tree",
		"turtle",
	};

	public static final String[] TUTORIAL_GIFS = {
		"addingObject",
		"cycleItem",
		"moveObject",
		"deletingObject",
		"scrolling"
	};

	public static final String[] TUTORIAL_TEXT = {
		"To add items",
		"To cycle image",
		"To move object",
		"To delete item",
		"To reposition window"
	};

	public Game()
	{
		// load data
		try {
			Json json_object = Json.load("map.json");
			model = new Model(json_object);
		} catch (Exception exception) {
			System.out.println("file not found: creating map.json");
			model = new Model();
			model.saveFile();
		}

		// Instantiate the three main objects

		controller = new Controller(model);
		view = new View(controller, model);

		// Set some window properties
		this.setTitle("Turtle Attack!");
		this.setSize(Constants.DEFAULT_MAIN_WINDOW_SIZE.width, Constants.DEFAULT_MAIN_WINDOW_SIZE.height);
		this.setFocusable(true);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.addKeyListener(controller);
	}

	public static void main(String[] args)
	{
		Game g = new Game();
		g.run();
	}
	
	public void run()
	{
		// Main loop
		while(true)
		{
			controller.update();
			model.update();
			view.repaint(); // Indirectly calls View.paintComponent
			Toolkit.getDefaultToolkit().sync(); // Updates screen

			// Go to sleep for a brief moment
			try
			{
				Thread.sleep(16);
			} catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
