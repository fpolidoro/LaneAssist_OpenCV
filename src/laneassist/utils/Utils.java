package laneassist.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public final class Utils {
	public static Image mat2Image(Mat frame) {
		try {
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		} catch (Exception e) {
			System.err.println("Cannot convert the Mat obejct: " + e);
			return null;
		}
	}

	public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
		Platform.runLater(() -> {
			property.set(value);
		});
	}

	private static BufferedImage matToBufferedImage(Mat original) {
		// init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		if (original.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}

	public static double EuclideanDistance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
	}

	public static double Clamp(double min, double max, double x) {
		return Math.max(max, Math.min(min, x));
	}

	// Finds the intersection of two lines, or returns false.
	// The lines are defined by (o1, p1) and (o2, p2).
	public static Point Intersection(Point o1, Point p1, Point o2, Point p2) {
		Point res = new Point(0, 0);
		Point p = Sub(o2, o1);
		Point d1 = Sub(p1, o1);
		Point d2 = Sub(p2, o2);

		double cross = d1.x * d2.y - d1.y * d2.x;
		if (Math.abs(cross) < /* EPS */1e-8) {
			return res;
		}

		double t1 = (p.x * d2.y - p.y * d2.x) / cross;
		res = Add(o1, Mul(d1, t1));
		return res;
	}

	public static Point Sub(Point a, Point b) {
		Point res = new Point();
		res.x = a.x - b.x;
		res.y = a.y - b.y;

		return res;
	}

	public static Point Add(Point a, Point b) {
		Point res = new Point();
		res.x = a.x + b.x;
		res.y = a.y + b.y;

		return res;
	}

	public static Point Mul(Point a, double b) {
		Point res = new Point();
		res.x = a.x * b;
		res.y = a.y * b;

		return res;
	}

	public static Mat OvertrayImage(Mat background, Mat foreground) {
		// The background and the foreground are assumed to be of the same size.
		Mat destination = new Mat(background.size(), background.type());

		for (int y = 0; y < (int) (background.rows()); ++y) {
			for (int x = 0; x < (int) (background.cols()); ++x) {
				double b[] = background.get(y, x);
				double f[] = foreground.get(y, x);

				double alpha = f[3] / 255.0;

				double d[] = new double[3];
				for (int k = 0; k < 3; ++k) {
					d[k] = f[k] * alpha + b[k] * (1.0 - alpha);
				}

				destination.put(y, x, d);
			}
		}

		return destination;
	}

	public static void FillWithGradient(Mat background, Point inters, Point leftBottomStripe, Point rightBottomStripe) {
		double two3ds = Math.ceil((inters.y - leftBottomStripe.y) * 2 / 3); // parte
																			// che
																			// verrà
																			// colorata
		Point[] twoThirds = new Point[2];
		twoThirds[0] = new Point(0, two3ds);
		twoThirds[1] = new Point(Double.MAX_VALUE, two3ds);
		MatOfPoint bottom = new MatOfPoint(Intersection(twoThirds[0], twoThirds[1], inters, leftBottomStripe),
				leftBottomStripe, Intersection(twoThirds[0], twoThirds[1], inters, rightBottomStripe),
				rightBottomStripe);
		Imgproc.fillConvexPoly(background, bottom, new Scalar(0, 255, 0, 35));
		// int bottom = (int) Math.ceil(twoThirds*1/3); //avrà l'alpha più scuro
		// in assoluto

	}
}
