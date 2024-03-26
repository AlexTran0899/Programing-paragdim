import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class Model {
  boolean is_trash_can_showing;
  boolean is_tutorial_modal_showing;
  int curr_selection_window_item_idx;
  public int curr_tutorial_idx;
  int curr_system_cursor;
  ArrayList<Item> items;
  Item curr_selected_item;

  Model() {
    this.is_trash_can_showing = false;
    this.is_tutorial_modal_showing = true;
    this.curr_selection_window_item_idx = 0;
    this.curr_tutorial_idx = 0;
    this.curr_system_cursor = Cursor.DEFAULT_CURSOR;
    this.items = new ArrayList<Item>();
    this.curr_selected_item = null;
  }

  Model(Json json_object) {
    this.is_trash_can_showing = json_object.getBool("is_trash_can_showing");
    this.is_tutorial_modal_showing = json_object.getBool("is_tutorial_modal_showing");
    this.curr_selection_window_item_idx = (int) json_object.getLong("curr_selection_window_item_idx");
    this.curr_tutorial_idx = (int)json_object.getLong("curr_tutorial_idx");
    this.curr_selected_item = null;
    this.items = new ArrayList<Item>();
    Json tmpList = json_object.get("items");
    for (int i = 0; i < tmpList.size(); i++) {
      var kind = tmpList.get(i).getString("name");
      if(kind.equals("turtle")) {
        this.items.add(new Jumper(tmpList.get(i)));
      } else {
        this.items.add(new Item(tmpList.get(i)));
      }
    }
    this.curr_selected_item = null;
  }

  Json marshal() {
    Json json_object = Json.newObject();
    json_object.add("curr_selection_window_item_idx", curr_selection_window_item_idx);
    json_object.add("is_trash_can_showing", is_trash_can_showing);
    json_object.add("is_tutorial_modal_showing",is_tutorial_modal_showing);
    json_object.add("curr_tutorial_idx",curr_tutorial_idx);
    Json tmpList = Json.newList();
    json_object.add("items", tmpList);
    for (Item item : items) {
      tmpList.add(item.marshal());
    }
    return json_object;
  }

  public void loadFile() {
    try {
      Json json_object = Json.load("map.json");
      this.is_trash_can_showing = json_object.getBool("is_trash_can_showing");
      this.is_tutorial_modal_showing = json_object.getBool("is_tutorial_modal_showing");
      this.curr_selection_window_item_idx = (int) json_object.getLong("curr_selection_window_item_idx");
      this.curr_tutorial_idx = (int)json_object.getLong("curr_tutorial_idx");
      this.items.clear();
      Json tmpList = json_object.get("items");
      for (int i = 0; i < tmpList.size(); i++) {
        var kind = tmpList.get(i).getString("name");
        if(kind.equals("turtle")) {
          this.items.add(new Jumper(tmpList.get(i)));
        } else {
          this.items.add(new Item(tmpList.get(i)));
        }
      }
      this.curr_selected_item = null;
    } catch (Exception exception) {
      exception.printStackTrace();
      System.exit(1);
    }
  }

  public void saveFile() {
    try {
      FileWriter writer = new FileWriter("map.json");
      String formatted_json = Json.prettyPrintJson(this.marshal().toString());
      writer.write(formatted_json);
      writer.close();
    } catch (IOException exception) {
      exception.printStackTrace();
      System.exit(1);
    }
  }

  public void update() {

  }

  public void reset() {

  }

  public void updateImagePosition(int mouse_position_x, int mouse_position_y) {
    if (curr_selected_item == null) return;
    this.curr_selected_item.setPosition_x(mouse_position_x);
    this.curr_selected_item.setPosition_y(mouse_position_y);
  }

  public void updateImagePosition(Position mouse_position) {
    this.updateImagePosition(mouse_position.x, mouse_position.y);
  }

  public Item getCurrHighlightedItem() {
    return curr_selected_item;
  }

  public void setCurrSelectedItem(int mouse_position_x, int mouse_position_y) {
    int closest_item_idx = getClosestItemIndex(mouse_position_x, mouse_position_y);

    if (closest_item_idx == -1) return;

    Item curr_item = items.get(closest_item_idx);
    double distance_from_mouse = calculateDistanceBetweenPointsWithHypot(
      mouse_position_x,
      mouse_position_y,
      curr_item.getPosition_x(),
      curr_item.getPosition_y());

    if (distance_from_mouse < calculateImageBoundary()) {
      this.curr_selected_item = curr_item;
      curr_system_cursor = Cursor.HAND_CURSOR;
    } else {
      this.curr_selected_item = null;
      curr_system_cursor = Cursor.DEFAULT_CURSOR;
    }
  }

  public void setCurrSelectedItem(Position mouse_position) {
    this.setCurrSelectedItem(mouse_position.x, mouse_position.y);
  }


  public void cycleCurrSelectionWindowImage() {
    this.curr_selection_window_item_idx += 1;
    this.curr_selection_window_item_idx %= Game.ITEMS.length;
  }

  public void addItem(int position_x, int position_y, String item_name) {
    if (curr_selected_item != null) return;
    if (item_name.equals("turtle")) {
      Jumper temp = new Jumper(this.curr_selection_window_item_idx, position_x, position_y, item_name);
      this.items.add(temp);
      this.curr_selected_item = temp;
    } else {
      Item temp = new Item(this.curr_selection_window_item_idx, position_x, position_y, item_name);
      this.items.add(temp);
      this.curr_selected_item = temp;
    }
  }

  public void addItem(Position mouse_position, String item_name) {
    this.addItem(mouse_position.x, mouse_position.y, item_name);
  }

  public void removeItem() {
    if (curr_selected_item == null) return;
    this.items.remove(curr_selected_item);
  }

  public String getCurrSelectedItemName() {
    return Game.ITEMS[curr_selection_window_item_idx];
  }

  private int getClosestItemIndex(double mouse_position_x, double mouse_position_y) {
    if (this.items.isEmpty()) {
      return -1;
    }

    double min_distance = Double.MAX_VALUE;
    int min_distance_idx = 0;

    for (int i = 0; i < this.items.size(); i++) {
      Item item = this.items.get(i);
      double distance = this.calculateDistanceBetweenPointsWithHypot(
        mouse_position_x,
        mouse_position_y,
        item.getPosition_x(),
        item.getPosition_y());

      if (distance < min_distance) {
        min_distance = distance;
        min_distance_idx = i;
      }
    }

    return min_distance_idx;
  }

  private double calculateDistanceBetweenPointsWithHypot(
    double x1,
    double y1,
    double x2,
    double y2) {

    double ac = Math.abs(y2 - y1);
    double cb = Math.abs(x2 - x1);

    return Math.hypot(ac, cb);
  }

  private double calculateImageBoundary() {
    return Math.sqrt(Math.pow(Constants.SCALED_IMAGE_SIZE.height, 2) +
      Math.pow(Constants.SCALED_IMAGE_SIZE.width, 2)) / 4;
  }
}

class Item {
  public final int item_id;
  protected int position_x;
  protected int position_y;
  String name;

  Item(int item_id, int position_x, int position_y, String name) {
    this.item_id = item_id;
    this.setPosition_x(position_x);
    this.setPosition_y(position_y);
    this.setName(name);
  }

  Item(Json json_object) {
    this.item_id = (int) json_object.getLong("item_id");
    this.setPosition_x((int) json_object.getLong("position_x"));
    this.setPosition_y((int) json_object.getLong("position_y"));
    this.setName(json_object.getString("name"));
  }

  Json marshal() {
    Json j = Json.newObject();
    j.add("item_id", item_id);
    j.add("position_x", position_x);
    j.add("position_y", position_y);
    j.add("name", name);
    return j;
  }
  public void setPosition_x(int position_x) {
    this.position_x = position_x;
  }

  public void setPosition_y(int position_y) {
    this.position_y = position_y;
  }

  public void setName(String name) {
    this.name = name == null ? "" : name;
  }

  public int getPosition_x() {
    return position_x;
  }

  public int getPosition_y() {
    return position_y;
  }

  public String getName() {
    return name;
  }
}

class Jumper extends Item {
  int jump_time_offset;

  Jumper(int item_id, int position_x, int position_y, String name) {
    super(item_id, position_x, position_y, name);
    this.jump_time_offset = (int) View.TIME % 1000;
    System.out.println(jump_time_offset);
  }

  Jumper(Json json_object) {
    super(json_object);
    this.jump_time_offset = (int)json_object.getLong("jump_time_offset");
  }

  @Override
  Json marshal() {
    Json j =  super.marshal();
    j.add("jump_time_offset", this.jump_time_offset);
    return j;
  }

  @Override
  public int getPosition_y() {
    return this.position_y - (int)Math.max(0., 50 * Math.sin(((double)View.TIME + jump_time_offset) / 1000));
  }
}
