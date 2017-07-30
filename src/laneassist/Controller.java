package laneassist;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import laneassist.utils.Utils;

public class Controller {
	@FXML
	private CheckMenuItem menuItemShowDebug;
	@FXML
	private ImageView currentFrame;
	@FXML
	private Pane paneFrame;
	@FXML
	private Label lblVideoTitle;
	@FXML
	private Slider sliROIWidth;
	@FXML
	private Slider sliROIHeight;
	@FXML
	private Slider sliROIHorizontalPosition;
	@FXML
	private Slider sliROIVerticalPosition;
	@FXML
	private Slider sliVideoSpeed;
	@FXML
	private Slider sliCannyThreshold;
	@FXML
	private Label lblCannyThreshold;
	@FXML
	private StackPane stackpROI;
	@FXML
	private ImageView imgMoveROIUpDown;
	@FXML
	private ImageView imgMoveROILeftRight;
	@FXML
	private ImageView imgResizeROIHorizontally;
	@FXML
	private ImageView imgResizeROIVertically;
	@FXML
	private Rectangle rectROI;
	@FXML
	private BorderPane borderPaneROI;
	@FXML
	private BorderPane borderPaneSpeed;
	@FXML
	private GridPane gridPaneDebug;
	@FXML
	private Button btnPlayPause;
	@FXML
	private Button btnForward;
	@FXML
	private Button btnRewind;
	@FXML
	private ImageView imgPlayPause;
	private Image playImg;
	private Image pauseImg;

	private final Double ROI_WIDTH = 120.0;
	private final Double ROI_HEIGHT = 35.0;

	private Insets spROIPadding;
	private Double hPaddingMax;
	private Double vPaddingMax;
	private Double curROIPaneWidth;
	private Double curROIPaneHeight;
	private Double frameWidth;
	private Double frameHeight;
	private Double frameCount;

	private ScheduledExecutorService timer;
	private ScheduledFuture<?> future;
	private VideoCapture capture = new VideoCapture();

	// punti che delineano la ROI che verrà impressa sul frame
	private Point leftTopPointROI;
	private Point rightBottomPointROI;

	Runnable frameGrabber;

	private Rect roi;
	
	private Point[] lastLeftStripe;
	private Point[] lastRightStripe;
	private double lastAlpha;
	private int laneFrameCount;
	private boolean fillLane;

	/**
	 * Inizializza i comandi della GUI
	 */
	protected void initializeGUI() {
		leftTopPointROI = new Point();
		rightBottomPointROI = new Point();
		roi = new Rect();
		setGUIDisabled(true);
		lblCannyThreshold.setText(String.valueOf(sliCannyThreshold.getValue()));
		try {
			playImg = new Image("file:icons/play32x32.png");
			pauseImg = new Image("file:icons/pause32x32.png");
			// btnPlayPause.setGraphic(imgPlayPause);
			// imposto le img per play e pause sul toggle button
			imgPlayPause.setImage(playImg);
		} catch (IllegalArgumentException iae) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Illegal Argument Exception");
			alert.setHeaderText(iae.getMessage());
			alert.showAndWait();
		}
	}

	/**
	 * Azione per il menu "open"
	 */
	@FXML
	private void actionMenuOpen() {

		String userDir = System.getProperty("user.home");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(userDir + "/Videos"));
		fileChooser.setTitle("Open File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Video Files", "*.avi", "*.mp4", ".mkv", ".mov"));
		File video = fileChooser.showOpenDialog(null);

		if (video != null) {
			if (capture.isOpened()) {
				setClosed();
			}

			// carico il video
			capture.open(video.getAbsolutePath());

			if (capture.isOpened()) {
				lblVideoTitle.setText(video.getName());
				// ottengo le dimensioni del frame
				frameWidth = capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
				frameHeight = capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
				frameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
				System.out.println("frameCount = " + frameCount);
				setGUIDisabled(false);
				computeROIDimension();
				computePointsForROI();
				
				lastLeftStripe = new Point[2];
				lastRightStripe = new Point[2];
				lastLeftStripe[0] = new Point(0, roi.height);
				lastLeftStripe[1] = new Point(0, 0);
				lastRightStripe[0] = new Point(roi.width, 0);
				lastRightStripe[1] = new Point(roi.width, roi.height);
				lastAlpha = Math.toRadians(90);
				laneFrameCount = 0;
				fillLane = false;
				
				frameGrabber = new Runnable() {

					@Override
					public void run() {
						Mat frame = grabFrame();
						if (frame != null) {
							Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new Scalar(0, 0, 255), 3);
							Image imageToShow = Utils.mat2Image(frame);
							updateImageView(currentFrame, imageToShow);
						} else {
							setClosed();
						}
					}
				};

				timer = Executors.newSingleThreadScheduledExecutor();
				future = timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				imgPlayPause.setImage(pauseImg);

			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText("The video cannot be opened");
				alert.showAndWait();
			}
		}
	}

	@FXML
	private void actionMenuClose() {
		setClosed();
	}

	@FXML
	private void checkMenuItemDebug() {
		if (menuItemShowDebug.isSelected())
			gridPaneDebug.setVisible(true);
		else
			gridPaneDebug.setVisible(false);
	}

	/**
	 * Comando slider che aumenta/diminuisce la larghezza della Region of
	 * Interest (rettangolo azzurrino).
	 */
	@FXML
	private void dragROIWidth() {
		Double width = ROI_WIDTH + (sliROIWidth.getValue() * (stackpROI.getWidth() - ROI_WIDTH)) / 100;
		if (width >= curROIPaneWidth) {
			width = curROIPaneWidth;
			sliROIHorizontalPosition.setDisable(true);
		} else {
			sliROIHorizontalPosition.setDisable(false);
		}
		imgResizeROIHorizontally.setVisible(false);
		rectROI.setVisible(true);
		rectROI.setWidth(width);

		hPaddingMax = stackpROI.getWidth() - rectROI.getWidth();

		spROIPadding = stackpROI.getPadding();
		Double padding = sliROIHorizontalPosition.getValue() * hPaddingMax / 100;
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), spROIPadding.getBottom(), padding));
		computePointsForROI();
	}

	@FXML
	private void mouseEnteredResizeROIWidth() {
		imgResizeROIHorizontally.setVisible(true);
		rectROI.setVisible(false);
	}

	@FXML
	private void mouseExitedResizeROIWidth() {
		imgResizeROIHorizontally.setVisible(false);
		rectROI.setVisible(true);
	}

	/**
	 * Comando slider che aumenta/diminuisce l'altezza della Region of Interest
	 * (rettangolo azzurrino).
	 */
	@FXML
	private void dragROIHeight() {
		Double height = ROI_HEIGHT + (sliROIHeight.getValue() * (stackpROI.getHeight() - ROI_HEIGHT)) / 100;
		if (height >= curROIPaneHeight) {
			height = curROIPaneHeight;
			sliROIVerticalPosition.setDisable(true);
		} else
			sliROIVerticalPosition.setDisable(false);
		imgResizeROIVertically.setVisible(false);
		rectROI.setVisible(true);
		rectROI.setHeight(height);

		vPaddingMax = stackpROI.getHeight() - rectROI.getHeight();

		spROIPadding = stackpROI.getPadding();
		Double padding = sliROIVerticalPosition.getValue() * vPaddingMax / 100;
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), padding, spROIPadding.getLeft()));
		computePointsForROI();
	}

	@FXML
	private void mouseEnteredResizeROIHeight() {
		imgResizeROIVertically.setVisible(true);
		rectROI.setVisible(false);
	}

	@FXML
	private void mouseExitedResizeROIHeight() {
		imgResizeROIVertically.setVisible(false);
		rectROI.setVisible(true);
	}

	/**
	 * Comando slider che sposta la Region of Interest (rettangolo azzurrino)
	 * orizzontalmente all'interno del frame (contorno nero).
	 */
	@FXML
	private void dragROIHorizontalPosition() {
		Double padding = sliROIHorizontalPosition.getValue() * hPaddingMax / 100;
		rectROI.setVisible(true);
		imgMoveROILeftRight.setVisible(false);
		spROIPadding = stackpROI.getPadding();
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), spROIPadding.getBottom(), padding));
		computePointsForROI();
	}

	@FXML
	private void mouseEnteredMoveROILeftRight() {
		imgMoveROILeftRight.setVisible(true);
		rectROI.setVisible(false);
	}

	@FXML
	private void mouseExitedMoveROILeftRight() {
		imgMoveROILeftRight.setVisible(false);
		rectROI.setVisible(true);
	}

	/**
	 * Comando slider che sposta la Region of Interest (rettangolo azzurrino)
	 * verticalmente all'interno del frame (contorno nero).
	 */
	@FXML
	private void dragROIVerticalPosition() {
		Double padding = sliROIVerticalPosition.getValue() * vPaddingMax / 100;
		spROIPadding = stackpROI.getPadding();
		imgMoveROIUpDown.setVisible(false);
		rectROI.setVisible(true);
		stackpROI.setPadding(
				new Insets(spROIPadding.getTop(), spROIPadding.getRight(), padding, spROIPadding.getLeft()));
		computePointsForROI();
	}

	@FXML
	private void mouseEnteredMoveROIUpDown() {
		// System.out.println("siamo in mouseEnteredROIUpDown");
		imgMoveROIUpDown.setVisible(true);
		rectROI.setVisible(false);
	}

	@FXML
	private void mouseExitedMoveROIUpDown() {
		// System.err.println("siamo in mouseExitedROIUpDown");
		imgMoveROIUpDown.setVisible(false);
		rectROI.setVisible(true);
	}

	/**
	 * Comando slider per aumentare/diminuire la velocità del video.
	 */
	@FXML
	private void dragVideoSpeed() {
		if (!future.isCancelled()) {
			future.cancel(false);
			future = timer.scheduleAtFixedRate(frameGrabber, 0, (long) (33 / sliVideoSpeed.getValue()), TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Comando bottone play/pause
	 */
	@FXML
	private void clickBtnPlayPause() {
		// se l'img corrente è play, allora ero in pausa del video e devo farlo
		// ripartire
		if (imgPlayPause.getImage().equals(playImg)) {
			imgPlayPause.setImage(pauseImg);
			future = timer.scheduleAtFixedRate(frameGrabber, 0, (long) (33 / sliVideoSpeed.getValue()), TimeUnit.MILLISECONDS);
		} else { // l'img corrente è pause, quindi devo mettere in pausa il
					// video
			imgPlayPause.setImage(playImg);
			future.cancel(false);
		}
	}

	/**
	 * Comando bottone rewind
	 */
	@FXML
	private void clickBtnRewind() {

	}

	/**
	 * Comando bottone forward
	 */
	@FXML
	private void clickBtnForward() {

	}

	/**
	 * Comando slider per aumentare/ridurre il canny
	 */
	@FXML
	private void dragCannyThreshold() {
		double value = sliCannyThreshold.getValue();
		lblCannyThreshold.setText(String.valueOf(value));
	}

	protected void setClosed() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error while trying to close the video");
				alert.setHeaderText(e.getMessage());
				alert.showAndWait();
			}
		}
		if (capture != null && capture.isOpened()) {
			capture.release();
			updateImageView(currentFrame, null);
		}

	}

	/**
	 * Per disegnare il rettangolo contenente la rappresentazione della ROI, in
	 * modo che mantenga lo stesso aspect ratio del video
	 * 
	 * @param mat
	 */
	private void computeROIDimension() {
		int dim = (int) Math.round(stackpROI.getWidth() * frameHeight / frameWidth);
		stackpROI.setPrefHeight(dim);
		stackpROI.setMaxHeight(dim);
		System.out.println("dim: " + stackpROI.getWidth() + " * " + frameHeight + " / " + frameWidth + " = " + dim);
	}

	// per abilitare/disabilitare i controlli della GUI relativi al video
	protected void setGUIDisabled(boolean value) {
		borderPaneSpeed.setDisable(value);
		borderPaneROI.setDisable(value);
		if (value) { // schiarisco i colori del rettangolo ROI
			rectROI.setVisible(false);
			stackpROI.setStyle("-fx-border-color: lightgrey ;");
		} else { // attivo il rettangolo azzurro e lo posiziono in base ai
					// cursori
			hPaddingMax = stackpROI.getWidth() - rectROI.getWidth();
			vPaddingMax = stackpROI.getHeight() - rectROI.getHeight();
			Double padding = hPaddingMax / 2;
			spROIPadding = stackpROI.getPadding();
			stackpROI.setPadding(new Insets(spROIPadding.getTop(), spROIPadding.getRight(), 0.0, padding));
			curROIPaneWidth = stackpROI.getWidth();
			curROIPaneHeight = stackpROI.getHeight();
			System.out.println("padding: " + padding);
			rectROI.setVisible(true);
			stackpROI.setStyle("-fx-border-color: grey ;");
		}
	}

	/**
	 * Disegna la ROI sul frame corrente
	 */
	private void computePointsForROI() {
		int x = (int) Math.round(stackpROI.getPadding().getLeft() * frameWidth / stackpROI.getWidth());
		int y = (int) Math
				.round(frameHeight - (stackpROI.getPadding().getBottom() * frameHeight / stackpROI.getHeight()));
		leftTopPointROI.x = x;
		leftTopPointROI.y = y - Math.round(frameHeight * rectROI.getHeight() / stackpROI.getHeight());
		rightBottomPointROI.x = x + Math.round(frameWidth * rectROI.getWidth() / stackpROI.getWidth());
		rightBottomPointROI.y = y;
		// il rettangolo viene disegnato a partire da top-left del frame, che è
		// 0,0
		// Imgproc.rectangle(frame, leftTopPointROI, rightBottomPointROI, new
		// Scalar(0,0,255), 3);
		roi = new Rect(leftTopPointROI, rightBottomPointROI);
	}

	/**
	 * Ottiene il frame corrente
	 * 
	 * @return Mat contenente il frame corrente
	 */
	private Mat grabFrame() {

		Mat frame = new Mat();

		if (this.capture.isOpened()) {
			try {
				this.capture.read(frame);

				if (!frame.empty()) {
					// System.out.println("frame " + count++);

					Mat imageROI = frame.submat(roi);
					Mat workingROI = imageROI.clone();

					Imgproc.cvtColor(imageROI, workingROI, Imgproc.COLOR_BGR2GRAY);
					// Imgproc.equalizeHist(workingROI, workingROI);
					Imgproc.blur(workingROI, workingROI, new Size(3, 3));

					// Canny
					Imgproc.Canny(workingROI, workingROI, sliCannyThreshold.getValue(),
							3 * sliCannyThreshold.getValue(), 3, false);

					// AdaptiveThreshold (meglio con equalizzazione)
					// Imgproc.adaptiveThreshold(workingROI, workingROI, 240,
					// Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
					// Imgproc.THRESH_BINARY_INV, 11, 10);

					// Hough
					Mat lines = new Mat();
					Imgproc.HoughLinesP(workingROI, lines, 1, Math.PI / 180, 30, 30, 5);

					// Solo due strise da disegnare, sinistra e destra
					Point[] leftStripe = new Point[2], rightStripe = new Point[2];
					leftStripe[0] = new Point(0, workingROI.rows());
					leftStripe[1] = new Point(0, 0);

					rightStripe[0] = new Point(workingROI.cols(), 0);
					rightStripe[1] = new Point(workingROI.cols(), workingROI.rows());

					double leftSlope = 0, rightSlope = 0;
					boolean leftFound = false, rightFound = false;

					Point center = new Point(workingROI.cols() / 2, workingROI.rows());
					
					for (int i = 0; i < lines.rows(); i++) {
						double[] val = lines.get(i, 0);
						
						// P2 come punto più basso del segmento
						Point p1, p2;
						if (val[1] < val[3]) {
							p1 = new Point(val[0], val[1]);
							p2 = new Point(val[2], val[3]);
						} else {
							p1 = new Point(val[2], val[3]);
							p2 = new Point(val[0], val[1]);
						}
						Imgproc.line(imageROI, p1, p2, new Scalar(255, 0, 255), 4);
						
						
						double slope = Math.atan2(p2.y - p1.y, p2.x - p1.x);

						if (p2.x <= center.x && slope > Math.toRadians(60) && slope < Math.toRadians(170)
								&& Utils.EuclideanDistance(p2, center) < Utils.EuclideanDistance(leftStripe[0], center)) { 
							// P1 è a destra del centro, ha una pendenza sensata, ed è a minor distanza dal centro rispetto a quello in memoria
							leftStripe[0] = p1;
							leftStripe[1] = p2;
							leftSlope = slope;
							leftFound = true;
							
						} else if (p2.x > center.x && slope > Math.toRadians(10) && slope < Math.toRadians(120) 
								&& Utils.EuclideanDistance(p2, center) < Utils.EuclideanDistance(rightStripe[0], center)) {
							// P1 è a destra del centro, ha una pendenza sensata, ed è a minor distanza dal centro rispetto a quello in memoria
							rightStripe[0] = p1;
							rightStripe[1] = p2;
							rightSlope = slope;
							rightFound = true;
						}						
					}
									
					double alpha = leftSlope - rightSlope;
					
					if (Math.abs(lastAlpha - alpha) < Math.toRadians(5)) {				
						laneFrameCount++;
					} else {
						laneFrameCount = 0;
					}
					
					lastAlpha = alpha;
					
					// Se ha trovato entrambe e insieme non formano troppo grande o troppo piccolo, e sono regolari da n frame
					if (leftFound && rightFound && alpha > Math.toRadians(70) && alpha < Math.toRadians(110) && laneFrameCount >= 3) {					
						
						fillLane = true;
						lastLeftStripe = leftStripe.clone();
						lastRightStripe = rightStripe.clone();
						
						// Estende i segmenti
						double dx = Math.cos(leftSlope) * 10000;				
						double dy = Math.sin(leftSlope) * 10000;
						
						lastLeftStripe[0].x -= dx;
						lastLeftStripe[0].y -= dy;
						lastLeftStripe[1].x += dx;
						lastLeftStripe[1].y += dy;
						
						dx = Math.cos(rightSlope) * 10000;
						dy = Math.sin(rightSlope) * 10000;
						lastRightStripe[0].x -= dx;
						lastRightStripe[0].y -= dy;
						lastRightStripe[1].x += dx;
						lastRightStripe[1].y += dy;

						Rect clipping = new Rect(Integer.MIN_VALUE / 2, 0, Integer.MAX_VALUE, workingROI.rows());
						Imgproc.clipLine(clipping, lastLeftStripe[0], lastLeftStripe[1]);
						Imgproc.clipLine(clipping, lastRightStripe[0], lastRightStripe[1]);
					}

					Imgproc.line(imageROI, lastLeftStripe[0], lastLeftStripe[1], new Scalar(0, 0, 255), 4);
					Imgproc.line(imageROI, lastRightStripe[0], lastRightStripe[1], new Scalar(0, 0, 255), 4);
					
					if (fillLane){
						MatOfPoint lane = new MatOfPoint(lastLeftStripe[0], lastLeftStripe[1], lastRightStripe[1], lastRightStripe[0]);
						Imgproc.fillConvexPoly(imageROI, lane, new Scalar(255, 0, 0));
					}
					
					if (leftFound) Imgproc.line(imageROI, leftStripe[0], leftStripe[1], new Scalar(0, 255, 0), 6);
					if (rightFound) Imgproc.line(imageROI, rightStripe[0], rightStripe[1], new Scalar(0, 255, 0), 6);
										
					Imgproc.line(imageROI, center, new Point(center.x, 0), new Scalar(0, 255, 255), 4);
			
					Imgproc.cvtColor(workingROI, workingROI, Imgproc.COLOR_GRAY2BGR);
					Core.addWeighted(imageROI, 1.0, workingROI, 0.7, 0.0, imageROI);

				} else {
					System.err.println("Video concluso");
					return null;
				}

			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error while processing the image");
				alert.setHeaderText(e.getMessage());
				alert.showAndWait();
			}
		}

		return frame;
	}

	/**
	 * Aggiorna l'immagine mostrata a video
	 * 
	 * @param view
	 *            elemento FXML in cui l'immagine deve essere mostrata
	 * @param image
	 *            frame da mostrare
	 */
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

}