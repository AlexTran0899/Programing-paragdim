import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import javax.swing.ImageIcon;

class View extends JPanel
{
	Model model;
	private int window_offset_x = 0;
	private int window_offset_y = 0;
	static long TIME = 0;
	JButton save_button, load_button, next_button, prev_button,close_button;
	BufferedImage[] images;
	BufferedImage trash_can_image;
	final private ImageIcon[] tutorial_gif;

	View(Controller c, Model m)
	{
		this.tutorial_gif = new ImageIcon[Game.TUTORIAL_GIFS.length];
		try {
			for(int i = 0; i < Game.TUTORIAL_GIFS.length; i++) {
				this.tutorial_gif[i] = new ImageIcon("images/"+ Game.TUTORIAL_GIFS[i] + ".gif");
				Dimension scaled_dimension = GlobalHelper.scaleToFitDimension(tutorial_gif[i].getImage().getWidth(null),
					tutorial_gif[i].getImage().getHeight(null), 400, 400);
				Image scaledGif = tutorial_gif[i].getImage().getScaledInstance(scaled_dimension.width, scaled_dimension.height, Image.SCALE_DEFAULT);
				tutorial_gif[i] = new ImageIcon(scaledGif);
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}


		// Make a button
		save_button = new JButton("Save");
		save_button.addActionListener(c);
		this.add(save_button);

		load_button = new JButton("Load");
		load_button.addActionListener(c);
		this.add(load_button);

		next_button = new JButton("Next");
		next_button.addActionListener(c);
		this.add(next_button);

		prev_button = new JButton("Previous");
		prev_button.addActionListener(c);
		this.add(prev_button);

		close_button = new JButton("Close Tutorial");
		close_button.addActionListener(c);
		this.add(close_button);

		save_button.setFocusable(false);
		load_button.setFocusable(false);

		// Link up to other objects
		c.setView(this);
		model = m;

		// Send mouse events to the controller
		this.addMouseListener(c);
		this.addMouseMotionListener(c);

		// Initialize images array with size
		images = new BufferedImage[Game.ITEMS.length];

		// Load the all images to images array
		for(int i = 0; i < Game.ITEMS.length; i++) {
			try
			{
				var base_path = "images/";
				var title = Game.ITEMS[i];
				var file_extension = ".png";
				var full_path = base_path + title + file_extension;
				images[i] = ImageIO.read(new File(full_path));
			} catch(Exception e) {
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}

		// Load trash can image
		try
		{
			trash_can_image = ImageIO.read(new File("images/trashcan.png"));
		} catch(Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	public void updateWindowOffsetByAmount_x(int window_offset_x) {
		this.window_offset_x += window_offset_x;
	}

	public void updateWindowOffsetByAmount_Y(int window_offset_y) {
		this.window_offset_y += window_offset_y;
	}

	public Position getTrueMousePositionWithOffset(int mouse_position_x, int mouse_position_y) {
		var true_mouse_position_x = mouse_position_x - window_offset_x;
		var true_mouse_position_y = mouse_position_y - window_offset_y;
		return new Position(true_mouse_position_x,true_mouse_position_y);
	}


  public void moveWindow(Direction direction) {
    switch (direction) {
      case UP:
				window_offset_y -= 2;
        break;
      case DOWN:
				window_offset_y += 2;
        break;
      case LEFT:
				window_offset_x -= 2;
        break;
      case RIGHT:
				window_offset_x += 2;
        break;
      default:
        break;
    }
  }

	public void paintComponent(Graphics g)
	{
		// hide menu button if modal is showing
		save_button.setVisible(false);
		load_button.setVisible(false);
		prev_button.setVisible(false);
		close_button.setVisible(false);
		next_button.setVisible(false);

		if(!model.is_tutorial_modal_showing) {
			save_button.setVisible(true);
			load_button.setVisible(true);
		}
		// update button position
		save_button.setBounds(this.getWidth() / 2, 20, 50, 40);
		load_button.setBounds(this.getWidth() / 2  - 60, 20, 50, 40);
		// update the number of time the screen refresh;
		TIME += 1;
		// update cursor
		this.setCursor(new Cursor(model.curr_system_cursor));
		// Clear the background
		g.setColor(Constants.BACKGROUND_COLOR);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		// display all added images
		for(int i = 0; i < model.items.size(); i++) {
			try
			{
				var curr_item = model.items.get(i);
				BufferedImage curr_buffered_image = images[curr_item.item_id];
				Dimension scaled_dimension;

				if(curr_item == model.getCurrHighlightedItem()) {
					scaled_dimension = GlobalHelper.scaleToFitDimension(curr_buffered_image, 250, 250);
				} else {
					scaled_dimension = GlobalHelper.scaleToFitDimension(curr_buffered_image, 150, 150);
				}

				int center_x = window_offset_x + curr_item.getPosition_x() - scaled_dimension.width / 2;
				int center_y = window_offset_y + curr_item.getPosition_y() - scaled_dimension.height / 2;

				g.drawImage(curr_buffered_image, center_x, center_y,
					scaled_dimension.width,scaled_dimension.height, null);

			} catch(Exception e) {
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}

		// show currently selected image along with selection window border
		g.setColor(Constants.SECONDARY_COLOR);
		g.fillRect(0, 0, Constants.SELECTION_WINDOW_SIZE.width, Constants.SELECTION_WINDOW_SIZE.height);

		var curr_selected_image_buffered = model.is_trash_can_showing ? trash_can_image : images[model.curr_selection_window_item_idx];
		Dimension curr_selected_image_scaled_dimension = GlobalHelper.scaleToFitDimension(curr_selected_image_buffered,
			Constants.SCALED_IMAGE_SIZE.width,
			Constants.SCALED_IMAGE_SIZE.height);
		int curr_selected_center_x = 25 + (150 - curr_selected_image_scaled_dimension.width) / 2;
		int curr_selected_center_y = 25 + (150 - curr_selected_image_scaled_dimension.height) / 2;

		g.drawImage(curr_selected_image_buffered,
			curr_selected_center_x,
			curr_selected_center_y,
			curr_selected_image_scaled_dimension.width,
			curr_selected_image_scaled_dimension.height, null);

		if (model.is_tutorial_modal_showing) {
			// show instruction
			var curr_gif_video = tutorial_gif[model.curr_tutorial_idx];
			var curr_tutorial_text = Game.TUTORIAL_TEXT[model.curr_tutorial_idx];
			g.setColor(new Color(20, 20, 20, 123));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			Graphics2D g2d = (Graphics2D) g;


			// Rectangle parameters
			int modal_width = 510;
			int modal_height = 630;
			int arcWidth = 10;
			int arcHeight = 10;

			var modal_center_x = (this.getWidth() / 2) - (modal_width / 2);
			var modal_center_y = (this.getHeight() / 2) - (modal_height / 2);

			g2d.setColor(Color.WHITE);
			g2d.fillRoundRect(modal_center_x, modal_center_y, modal_width, modal_height, arcWidth, arcHeight);

			var video_width = curr_gif_video.getIconWidth();
			var video_height = curr_gif_video.getIconHeight();
			var video_center_x = (this.getWidth() / 2) - (video_width / 2);
			var video_center_y = (this.getHeight() / 2) - (video_height / 2) - 45;
			curr_gif_video.paintIcon(this, g, video_center_x, video_center_y + 20);

			g.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 30));
			g.setColor(Color.BLACK);
			g.drawString(curr_tutorial_text, video_center_x , video_center_y - 10);
			if(model.curr_tutorial_idx != 0) {
				prev_button.setVisible(true);
			}
				prev_button.setBounds(video_center_x, video_center_y + video_height + 55, 100, 50);
			if(model.curr_tutorial_idx == Game.TUTORIAL_GIFS.length - 1) {
				next_button.setVisible(false);
				close_button.setVisible(true);
				close_button.setBounds(video_center_x + video_width - 120, video_center_y + video_height + 55, 120, 50);
			} else {
				next_button.setVisible(true);
				next_button.setBounds(video_center_x + video_width - 100, video_center_y + video_height + 55, 100, 50);
			}
		}
	}
}
