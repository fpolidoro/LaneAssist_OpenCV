package laneassist;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import laneassist.utils.Utils;

public class Controller {
	@FXML
	private ImageView currentFrame;
	@FXML
	private Label videoTitle;
	@FXML
	private Slider ROIWidth;
	@FXML
	private Slider ROIHeight;
	@FXML
	private Slider ROIHorizontalPosition;
	@FXML
	private Slider ROIVerticalPosition;
	@FXML
	private Slider videoSpeed;
	@FXML
	private StackPane ROIPane;
	@FXML
	private Rectangle ROI;

	private final Double ROI_WIDTH = 80.0;
	private final Double ROI_HEIGHT = 35.0;

	private Insets ROIPanePadding;
	private Double hPaddingMax;
	private Double vPaddingMax;
	private Double curROIPaneWidth;
	private Double curROIPaneHeight;

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that realizes the video capture
	private VideoCapture capture = new VideoCapture();
	
	Runnable frameGrabber;

	public void InitializeGUI() {
		hPaddingMax = ROIPane.getWidth() - ROIPane.getWidth();
		vPaddingMax = ROIPane.getHeight() - ROIPane.getHeight();
		Double padding = hPaddingMax / 2;
		ROIPanePadding = ROIPane.getPadding();
		ROIPane.setPadding(new Insets(ROIPanePadding.getTop(), ROIPanePadding.getRight(), 0.0, padding));
		curROIPaneWidth = ROIPane.getWidth();
		curROIPaneHeight = ROIPane.getHeight();
	}
	

	@FXML
	protected void ActionMenuOpen() {

		String userDir = System.getProperty("user.home");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(userDir + "/Videos"));
		fileChooser.setTitle("Open File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Video Files", "*.avi", "*.mp4"));
		File video = fileChooser.showOpenDialog(null);

		capture.open(video.getAbsolutePath());

		if (capture.isOpened()) {

			frameGrabber = new Runnable() {

				@Override
				public void run() {
					Mat frame = grabFrame();

					Image imageToShow = Utils.mat2Image(frame);
					updateImageView(currentFrame, imageToShow);
				}
			};

			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

		} else {
			System.err.println("Impossibile aprire il video");
		}
	}
	
	private Mat grabFrame() {

		Mat frame = new Mat();

		if (this.capture.isOpened()) {
			try {
				this.capture.read(frame);

				if (!frame.empty()) {

					// Processa l'immagine
				}

			} catch (Exception e) {
				System.err.println("Errore nel processamento dell'immagine: " + e);
			}
		}

		return frame;
	}

	@FXML
	void DragROIWidth() {
		Double width = ROI_WIDTH + (ROIWidth.getValue() * (ROIPane.getWidth() - ROI_WIDTH)) / 100;
		if (width >= curROIPaneWidth) {
			width = curROIPaneWidth;
			ROIHorizontalPosition.setDisable(true);
		} else {
			ROIHorizontalPosition.setDisable(false);
		}
		ROI.setWidth(width);

		hPaddingMax = ROIPane.getWidth() - ROI.getWidth();

		ROIPanePadding = ROIPane.getPadding();
		Double padding = ROIHorizontalPosition.getValue() * hPaddingMax / 100;
		ROIPane.setPadding(
				new Insets(ROIPanePadding.getTop(), ROIPanePadding.getRight(), ROIPanePadding.getBottom(), padding));
	}

	@FXML
	void DragROIHeight() {
		Double height = ROI_HEIGHT + (ROIHeight.getValue() * (ROIPane.getHeight() - ROI_HEIGHT)) / 100;
		if (height >= curROIPaneHeight) {
			height = curROIPaneHeight;
			ROIVerticalPosition.setDisable(true);
		} else
			ROIVerticalPosition.setDisable(false);
		ROI.setHeight(height);

		vPaddingMax = ROIPane.getHeight() - ROI.getHeight();

		ROIPanePadding = ROIPane.getPadding();
		Double padding = ROIVerticalPosition.getValue() * vPaddingMax / 100;
		ROIPane.setPadding(
				new Insets(ROIPanePadding.getTop(), ROIPanePadding.getRight(), padding, ROIPanePadding.getLeft()));
	}

	@FXML
	void DragROIHorizontalPosition() {
		Double padding = ROIHorizontalPosition.getValue() * hPaddingMax / 100;
		ROIPanePadding = ROIPane.getPadding();
		ROIPane.setPadding(
				new Insets(ROIPanePadding.getTop(), ROIPanePadding.getRight(), ROIPanePadding.getBottom(), padding));
	}

	@FXML
	void DragROIVerticalPosition() {
		Double padding = ROIVerticalPosition.getValue() * vPaddingMax / 100;
		ROIPanePadding = ROIPane.getPadding();
		ROIPane.setPadding(
				new Insets(ROIPanePadding.getTop(), ROIPanePadding.getRight(), padding, ROIPanePadding.getLeft()));
	}

	@FXML
	void DragVideoSpeed() {

	}

	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	protected void setClosed() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println("Errore durante la chiusura");
			}
		}
		if (this.capture.isOpened()) {
			this.capture.release();
		}
	}
}