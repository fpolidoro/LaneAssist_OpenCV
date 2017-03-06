package lane.assist.processing;


import lane.assist.polito.utils.*;
import org.opencv.core.Mat;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lane.assist.polito.utils.Utils;

/**
 * 
 * @author fab
 * Class containing all the processing and detection methods
 */
public class Processing {
	
	public static void updateImageView(ImageView view, Image image){
		Utils.onFXThread(view.imageProperty(), image);
	}
	/*
	public static float getFrameRate(Player player){
		return (float)noOfFrames(player)/(float)player.getDuration().getSeconds();
	}


	public static int noOfFrames(Player player){
	        FramePositioningControl fpc = (FramePositioningControl)player.getControl("javax.media.control.FramePositioningControl");

	  	Time duration = player.getDuration();
	        int i = fpc.mapTimeToFrame(duration);
	        if (i != FramePositioningControl.FRAME_UNKNOWN) return i;
		else return -1;
	}*/
}
