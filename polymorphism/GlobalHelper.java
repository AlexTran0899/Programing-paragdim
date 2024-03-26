import java.awt.image.BufferedImage;
import java.awt.Dimension;

public class GlobalHelper {
  public static Dimension scaleToFitDimension(int original_width, int original_height, int bound_width, int bound_height) {
    int new_width = original_width;
    int new_height = original_height;

    if(original_width >= original_height) {
      new_width = bound_width;
      new_height = (new_width * original_height) / original_width;
    } else {
      new_height = bound_height;
      new_width = (new_height * original_width) / original_height;
    }
    return new Dimension(new_width,new_height);
  }

  public static Dimension scaleToFitDimension(BufferedImage original_image, int bound_width, int bound_height){
    var original_width = original_image.getWidth();
    int original_height = original_image.getHeight();
    int new_width = original_width;
    int new_height = original_height;

    if(original_width >= original_height) {
      new_width = bound_width;
      new_height = (new_width * original_height) / original_width;
    } else {
      new_height = bound_height;
      new_width = (new_height * original_width) / original_height;
    }
    return new Dimension(new_width,new_height);
  }
}


