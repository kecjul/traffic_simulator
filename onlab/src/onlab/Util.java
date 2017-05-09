package onlab;

import java.awt.geom.Point2D;

public class Util {
	enum Direction {
		LEFT {
			@Override
			public String toString() {
				return "left";
			}
		},
		RIGHT {
			@Override
			public String toString() {
				return "right";
			}
		},
		FORWARD {
			@Override
			public String toString() {
				return "forward";
			}
		},
		BACKWARD {
			@Override
			public String toString() {
				return "backward";
			}
		}
	}
	
	public static float toRadian(float angle) {
		return (float) ((angle / 180) * Math.PI);
	}

	public static float toAngle(float radian) {
		return (float) (radian * (180 / Math.PI));
	}

	public static float normalAngle(float angle) {
		float temp = angle + 360;
		temp = temp % 360;
		return temp;
	}

	public static float square(float number) {
		return (number * number);
	}

	public static float getDistance(Point2D.Float a, Point2D.Float b) {
		return (float) Math.sqrt(square(b.x - a.x) + square(b.y - a.y));
	}

	public static float pitagorasAB(float a, float b) {
		return (float) Math.sqrt(square(a) + square(b));
	}

	public static float pitagorasAC(float a, float c) {
		return (float) Math.sqrt(square(c) - square(a));
	}
}
