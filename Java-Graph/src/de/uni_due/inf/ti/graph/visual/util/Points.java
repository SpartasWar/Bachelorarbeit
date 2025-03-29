package de.uni_due.inf.ti.graph.visual.util;

import java.awt.geom.Point2D;

/**
 * Provides utility operations on {@link Point2D}.
 * The idea for most of these operations are that a point is also a vector
 * (going from the origin to the location of the point).
 * TODO: Move to VisiGraph?
 */
public class Points {
	private Points() {
		// prevent instantiation
	}

	/**
	 * Returns a new point that is the vector sum of the two given points.
	 * @param a first point
	 * @param b second point
	 * @return {@code a + b}
	 */
	public static Point2D add(Point2D a, Point2D b) {
		return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
	}

	/**
	 * Returns a new point that is the vector difference of the two given points (subtract second from first).
	 * @param a first point
	 * @param b second point
	 * @return {@code a - b}
	 */
	public static Point2D subtract(Point2D a, Point2D b) {
		return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
	}

	/**
	 * Adds the given offset to the target point. The target point is modified.
	 * @param off offset vector
	 * @param target the target point to be modified
	 */
	public static void addTo(Point2D off, Point2D target) {
		target.setLocation(target.getX() + off.getX(), target.getY() + off.getY());
	}

	/**
	 * Subtracts the given offset from the target point. The target point is modified.
	 * @param off offset vector
	 * @param target the target point to be modified
	 */
	public static void subtractFrom(Point2D off, Point2D target) {
		target.setLocation(target.getX() - off.getX(), target.getY() - off.getY());
	}

	/**
	 * Returns the vector length of the given point, i.e. the distance from the origin.
	 * @param p point
	 * @return distance of p to origin
	 */
	public static double length(Point2D p) {
		return Math.hypot(p.getX(), p.getY());
	}

	/**
	 * Returns a new vector that has the same direction as the given point, but length 1.
	 *
	 * If the zero point is given, then a new zero point is returned instead.
	 * @param p point
	 * @return vector of length 1, unless p is zero, then zero
	 */
	public static Point2D normalize(Point2D p) {
		double len = length(p);
		if (len == 0.) {
			return new Point2D.Double();
		} else {
			return new Point2D.Double(p.getX() / len, p.getY() / len);
		}
	}

	/**
	 * Returns a new vector with the components of the given point multiplied by the given scalar.
	 * @param p point
	 * @param factor scale factor
	 * @return scaled vector
	 */
	public static Point2D scale(Point2D p, double factor) {
		return new Point2D.Double(p.getX() * factor, p.getY() * factor);
	}

	/**
	 * Returns a new vector that has length 1 and points in the direction given by the angle.
	 * @param angle an angle
	 * @return unit length vector of given direction
	 */
	public static Point2D ofAngle(double angle) {
		return new Point2D.Double(Math.cos(angle), Math.sin(angle));
	}

	/**
	 * Returns a new vector that has given length and points in the direction given by the angle.
	 * @param angle an angle
	 * @param length a length
	 * @return vector of given direction and length
	 */
	public static Point2D ofAngle(double angle, double length) {
		return new Point2D.Double(Math.cos(angle) * length, Math.sin(angle) * length);
	}

	/**
	 * Returns the angle of the given vector as computed by {@link Math#atan2(double, double)}.
	 * @param p point
	 * @return {@code atan2(p.y, p.x)}
	 */
	public static double angle(Point2D p) {
		return Math.atan2(p.getY(), p.getX());
	}
}
