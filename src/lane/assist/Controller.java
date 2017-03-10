package lane.assist;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SystemUtils;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lane.assist.polito.utils.Utils;
import lane.assist.processing.Processing;


public class Controller {
	private static String OS;

	private VideoCapture capture;
	private ScheduledExecutorService timer;

	@FXML
	private ImageView imgView_frame;
	@FXML
	private Label lbl_VideoTitle;

	@FXML
	private Slider slider_ROIwidth;
	@FXML
	private Slider slider_ROIheight;
	@FXML
	private Slider slider_ROIHorizontalPosition;
	@FXML
	private Slider slider_ROIVerticalPosition;
	@FXML
	private Rectangle rect_ROI;
	private final Double ROIWIDTH = 80.0;
	private final Double ROIHEIGHT = 35.0;

	@FXML
	private StackPane stackPane_ROI;
	private Insets spROI_padding;
	private Double hPaddingMax;
	private Double vPaddingMax;
	private Double curStackPaneWidth;
	private Double curStackPaneHeight;

	@FXML
	private Slider slider_VideoSpeed;

	public void InitializeGUI(){
		OS = System.getProperty("os.name").toLowerCase();
		hPaddingMax = stackPane_ROI.getWidth()-rect_ROI.getWidth();
		vPaddingMax = stackPane_ROI.getHeight()-rect_ROI.getHeight();
		Double padding = hPaddingMax/2;
		spROI_padding = stackPane_ROI.getPadding();
		stackPane_ROI.setPadding(new Insets(spROI_padding.getTop(),spROI_padding.getRight(), 0.0, padding));
		curStackPaneWidth = stackPane_ROI.getWidth();
		curStackPaneHeight = stackPane_ROI.getHeight();
	}

	@FXML
	void ActionMenuOpen(){
		String userDir = System.getProperty("user.home");
		FileChooser fileChooser = new FileChooser();
		if(SystemUtils.IS_OS_LINUX)
			fileChooser.setInitialDirectory(new File(userDir + "/Video"));
		else if(SystemUtils.IS_OS_WINDOWS)
			fileChooser.setInitialDirectory(new File(userDir + "/Videos"));
		fileChooser.setTitle("Open File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Video Files", "*.avi", "*.mp4"));
		File video = fileChooser.showOpenDialog(null);
		if(video != null){
			// grab a frame every 33 ms (30 frames/sec)
			Runnable frameGrabber = new Runnable() {
				@Override
				public void run(){
					try{
						capture = new VideoCapture();
						capture.open(video.getPath());
					}catch(Exception e){
						System.err.println("Exception in VideoCapture");
						return;
					}
					Mat frame = new Mat();
					if (!capture.isOpened()) {
				          System.out.println("media failed to open");
				      } else {
				          while (capture.grab()) {
				              capture.retrieve(frame);
				              Image imageToShow = Utils.mat2Image(frame);
				              lane.assist.processing.Processing.updateImageView(imgView_frame, imageToShow);
				          }
				          capture.release();
				      }

					// effectively grab and process a single frame
					//Mat frame = new Mat();
					//if(!capture.read(frame))
						//System.out.println("Cattura sbagliata");
					// convert and show the frame
					//Image imageToShow = Utils.mat2Image(frame);
					//lane.assist.processing.Processing.updateImageView(imgView_frame, imageToShow);
				}
			};
			lbl_VideoTitle.setText(video.getPath());
			//this.timer = Executors.newSingleThreadScheduledExecutor();
			//this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
		}
	}

	@FXML
	void DragROIWidth(){
		Double width = ROIWIDTH + (slider_ROIwidth.getValue()*(stackPane_ROI.getWidth()-ROIWIDTH))/100;
		if(width >= curStackPaneWidth){
			width = curStackPaneWidth;
			slider_ROIHorizontalPosition.setDisable(true);
		}else
			slider_ROIHorizontalPosition.setDisable(false);
		rect_ROI.setWidth(width);

		hPaddingMax = stackPane_ROI.getWidth()-rect_ROI.getWidth();

		spROI_padding = stackPane_ROI.getPadding();
		Double padding = slider_ROIHorizontalPosition.getValue()*hPaddingMax/100;
		stackPane_ROI.setPadding(new Insets(spROI_padding.getTop(), spROI_padding.getRight(), spROI_padding.getBottom(), padding));
	}

	@FXML
	void DragROIHeight(){
		Double height = ROIHEIGHT + (slider_ROIheight.getValue()*(stackPane_ROI.getHeight()-ROIHEIGHT))/100;
		if(height >= curStackPaneHeight){
			height = curStackPaneHeight;
			slider_ROIVerticalPosition.setDisable(true);
		}else
			slider_ROIVerticalPosition.setDisable(false);
		rect_ROI.setHeight(height);

		vPaddingMax = stackPane_ROI.getHeight()-rect_ROI.getHeight();

		spROI_padding = stackPane_ROI.getPadding();
		Double padding = slider_ROIVerticalPosition.getValue()*vPaddingMax/100;
		stackPane_ROI.setPadding(new Insets(spROI_padding.getTop(), spROI_padding.getRight(), padding, spROI_padding.getLeft()));
	}

	@FXML
	void DragROIHorizontalPosition(){
		Double padding = slider_ROIHorizontalPosition.getValue()*hPaddingMax/100;
		spROI_padding = stackPane_ROI.getPadding();
		stackPane_ROI.setPadding(new Insets(spROI_padding.getTop(), spROI_padding.getRight(), spROI_padding.getBottom(), padding));
	}

	@FXML
	void DragROIVerticalPosition(){
		Double padding = slider_ROIVerticalPosition.getValue()*vPaddingMax/100;
		spROI_padding = stackPane_ROI.getPadding();
		stackPane_ROI.setPadding(new Insets(spROI_padding.getTop(), spROI_padding.getRight(), padding, spROI_padding.getLeft()));
	}

	@FXML
	void DragVideoSpeed(){

	}
}
