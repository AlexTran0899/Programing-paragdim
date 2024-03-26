import java.awt.*;
import java.awt.event.*;
import java.awt.Robot;

class Controller implements ActionListener, MouseListener, KeyListener, MouseMotionListener {
  View view;
  Model model;
  Direction Direction;
  Robot robot;

  Controller(Model m) {
    model = m;
    try {
      this.robot = new Robot();
    } catch (AWTException exception) {
      exception.printStackTrace();
      System.exit(1);
    }
  }

  void setView(View v) {
    view = v;
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == view.load_button){
      System.out.println("loading file");
      model.loadFile();
    }
    if (e.getSource() == view.save_button) {
      System.out.println("saving file");
      model.saveFile();
    }
    if(e.getSource() == view.next_button) {
      model.curr_tutorial_idx += 1;
      model.curr_tutorial_idx %= Game.TUTORIAL_GIFS.length;
    }

    if(e.getSource() == view.prev_button) {
      model.curr_tutorial_idx -= 1;
      model.curr_tutorial_idx = Math.max(model.curr_tutorial_idx, 0);
    }
    if(e.getSource() == view.close_button) {
      model.is_tutorial_modal_showing = false;
    }
  }

  public void mousePressed(MouseEvent e) {
    if (this.is_mouseWithinSelectionWindow(e.getX(),e.getY())) {
      model.cycleCurrSelectionWindowImage();
      return;
    }

    model.is_trash_can_showing = true;

    switch (e.getButton()) {
      case MouseEvent.BUTTON1:
        String curr_item_name = model.getCurrSelectedItemName();
        Position true_mouse_position = view.getTrueMousePositionWithOffset(e.getX(),e.getY());
        if(!model.is_tutorial_modal_showing) {
          model.addItem(true_mouse_position, curr_item_name);
        }
        break;
      case MouseEvent.BUTTON2:
        break;
      case MouseEvent.BUTTON3:
        model.removeItem();
    }
  }

  public void mouseReleased(MouseEvent e) {
    model.is_trash_can_showing = false;
    model.curr_selected_item = null;
  }

  public void mouseEntered(MouseEvent e) {
    model.curr_system_cursor = Cursor.DEFAULT_CURSOR;
  }

  public void mouseExited(MouseEvent e) {
    model.curr_system_cursor = Cursor.HAND_CURSOR;
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_RIGHT:
        Direction = Direction.RIGHT;
        view.moveWindow(Direction.RIGHT);
        break;
      case KeyEvent.VK_LEFT:
        Direction = Direction.LEFT;
        view.moveWindow(Direction.LEFT);
        break;
      case KeyEvent.VK_UP:
        Direction = Direction.UP;
        view.moveWindow(Direction.UP);
        break;
      case KeyEvent.VK_DOWN:
        Direction = Direction.DOWN;
        view.moveWindow(Direction.DOWN);
        break;
    }
  }

  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_RIGHT:
        Direction = Direction.RIGHT;
        break;
      case KeyEvent.VK_LEFT:
        Direction = Direction.LEFT;
        break;
      case KeyEvent.VK_UP:
        Direction = Direction.UP;
        break;
      case KeyEvent.VK_DOWN:
        Direction = Direction.DOWN;
        break;
      case KeyEvent.VK_ESCAPE:
        System.exit(0);
    }
    char c = Character.toLowerCase(e.getKeyChar());
    if (c == 'q')
      System.exit(0);
    if (c == 'r')
      model.reset();
  }

  public void keyTyped(KeyEvent e) {
  }

  void update() {

  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if(!this.is_mouseWithinSelectableZone(e.getX(), e.getY())){
      scrollingViaMouse(e.getX(),e.getY());
    }

    Position true_mouse_position = view.getTrueMousePositionWithOffset(e.getX(),e.getY());
    if(this.is_mouseWithinSelectionWindow(e.getX(),e.getY())){
      model.removeItem();
      return;
    }
      model.is_trash_can_showing = true;
			model.updateImagePosition(true_mouse_position);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    if(!this.is_mouseWithinSelectableZone(e.getX(), e.getY())){
      scrollingViaMouse(e.getX(),e.getY());
    }
    Position true_mouse_position = view.getTrueMousePositionWithOffset(e.getX(),e.getY());
    model.setCurrSelectedItem(true_mouse_position);
  }

  private void scrollingViaMouse(int mouse_position_x, int mouse_position_y) {
    Point p = MouseInfo.getPointerInfo().getLocation();
    Rectangle main_widow_frame = view.getBounds();
    int main_window_curr_height = main_widow_frame.height;
    int main_window_curr_width = main_widow_frame.width;

    if(mouse_position_x < Constants.WINDOW_BORDER_AMOUNT) {
      var diff = Constants.WINDOW_BORDER_AMOUNT - mouse_position_x;
      robot.mouseMove(p.x + diff, p.y);
      view.updateWindowOffsetByAmount_x(diff);
    } else if(mouse_position_x > main_window_curr_width - Constants.WINDOW_BORDER_AMOUNT) {
      var diff = mouse_position_x - (main_window_curr_width - Constants.WINDOW_BORDER_AMOUNT);
      robot.mouseMove(p.x - diff, p.y);
      view.updateWindowOffsetByAmount_x(-diff);
    }

    if(mouse_position_y < Constants.WINDOW_BORDER_AMOUNT) {
      var diff = Constants.WINDOW_BORDER_AMOUNT - mouse_position_y;
      robot.mouseMove(p.x, p.y + diff);
      view.updateWindowOffsetByAmount_Y(diff);
    } else if (mouse_position_y > main_window_curr_height - Constants.WINDOW_BORDER_AMOUNT){
      var diff = mouse_position_y - (main_window_curr_height - Constants.WINDOW_BORDER_AMOUNT);
      robot.mouseMove(p.x, p.y - diff);
      view.updateWindowOffsetByAmount_Y(-diff);
    }
  }

  private boolean is_mouseWithinSelectableZone(int mouse_position_x, int mouse_position_y) {
    if(is_mouseWithinSelectionWindow(mouse_position_x, mouse_position_y)) {
      return true;
    }
    if (is_mouseWithinMenuBar(mouse_position_x, mouse_position_y)) {
      return true;
    }
    return false;
  }

  private boolean is_mouseWithinSelectionWindow(int mouse_position_x, int mouse_position_y) {
    if (mouse_position_x <= Constants.SELECTION_WINDOW_SIZE.width && mouse_position_y <= Constants.SELECTION_WINDOW_SIZE.height) {
      return true;
    }
    return false;
  }

  private boolean is_mouseWithinMenuBar(int mouse_position_x, int mouse_position_y) {
    var center = view.getBounds().width / 2;
    var left_corner = center - 100;
    var right_corner = center + 100;
    var menu_bar_height = Constants.WINDOW_BORDER_AMOUNT + 100;
    if(mouse_position_y < menu_bar_height) {
      if(mouse_position_x > left_corner && mouse_position_x < right_corner){
        return true;
      }
    }
    return false;
  }
}

class Position {
  public final int x;
  public final int y;

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

