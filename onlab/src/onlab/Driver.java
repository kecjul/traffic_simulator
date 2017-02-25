package onlab;

import java.awt.geom.Point2D;

import onlab.Controller.Status;

public class Driver {
	float prefSpeed = 50;
	float rangeOfView = 50;
	float safetyGap;
	float reactionTime = (float) 0.02;

	enum Status {
		ACCELERATING {
			@Override
			public String toString() {
				return "accelerating";
			}
		},
		DECELERATING {
			@Override
			public String toString() {
				return "decelerating";
			}
		},
		STOPPING {
			@Override
			public String toString() {
				return "stopping";
			}
		},
		STOPPED {
			@Override
			public String toString() {
				return "stopped";
			}
		},
		DRIVING {
			@Override
			public String toString() {
				return "driving";
			}
		}
	};

	Status s = Status.ACCELERATING;

	Driver() {
	}

	public Driver(float prefSpeed, float range, float safety) {
		this.prefSpeed = prefSpeed;
		this.rangeOfView = range;
		this.safetyGap = safety;
	}

	public float drive(RoadObject inSight, Car thisCar) {
		float change = 0;
		if (inSight == null) {
			change = (prefSpeed - thisCar.actualSpeed);
			if ((change * -1) > thisCar.maxAcc)
				change = thisCar.maxAcc * -1;
		} else {
			float distance = getDistance(inSight.position, thisCar.position) - safetyGap;

			if (distance <= 0) {
				if (inSight instanceof Block) {
					change = thisCar.actualSpeed * (-1);
				}
				if (inSight instanceof Car) {
					Car car = (Car) inSight;
					if (thisCar.actualSpeed > car.actualSpeed) {
						change = (car.actualSpeed - thisCar.actualSpeed) + distance;
					} else if (car.actualSpeed > thisCar.actualSpeed) {
						change = (prefSpeed - thisCar.actualSpeed) + distance;
					}
				}
			} else {
				if (inSight instanceof Block) {
					change = (thisCar.actualSpeed / distance) * -1;
					s = Status.STOPPING;
				}
				if (inSight instanceof Car) {
					Car car = (Car) inSight;
					if (prefSpeed >= car.actualSpeed) {
						change = (car.actualSpeed - thisCar.actualSpeed) / distance;
					} else if (car.actualSpeed > prefSpeed) {
						change = (prefSpeed - thisCar.actualSpeed);
					}
					if(Math.abs(car.actualSpeed - thisCar.actualSpeed) <0.1){
						thisCar.actualSpeed = car.actualSpeed;
						change = 0;
					}
				}
			}
		}
		if (change > 0)
			s = Status.ACCELERATING;
		if (change < 0)
			s = Status.DECELERATING;
		if (change == 0 || Math.abs(change) < 0.001 )
			s = Status.DRIVING;
		if (inSight != null && inSight instanceof Block)
			s = Status.STOPPING;
		return change;
	}

	public float getDistance(Point2D.Float a, Point2D.Float b) {
		return (float) Math.sqrt(square(b.x - a.x) + square(b.y - a.y));
	}

	public float square(float number) {
		return (number * number);
	}
}
