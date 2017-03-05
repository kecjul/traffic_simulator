package onlab;

import java.awt.geom.Point2D;

import onlab.Controller.Status;

public class Driver {
	private float prefSpeed = 50;
	private float rangeOfView = 50;
	private float safetyGap;
	private float reactionTime = (float) 0.02;

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
		},
		OVERTAKING {
			@Override
			public String toString() {
				return "overtaking";
			}
		}
	};

	Status s = Status.ACCELERATING;

	Driver() {
	}

	public Driver(float prefSpeed, float range, float safety) {
		this.setPrefSpeed(prefSpeed);
		this.setRangeOfView(range);
		this.setSafetyGap(safety);
	}

	public float drive(RoadObject inSight, Car thisCar) {
		float change = 0;
		if (inSight == null) {
			change = normalDrive(thisCar.getActualSpeed(), thisCar.getMaxAcc());
		} else {
			change = reactiveDrive(inSight, thisCar);
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

	private float reactiveDrive(RoadObject inSight, Car thisCar) {
		float change = 0;
		float breakDistance = getDistance(inSight.getPosition(), thisCar.getPosition()) - getSafetyGap();

		if (breakDistance <= 0) {
			change = closeDrive(inSight, thisCar.getActualSpeed(), breakDistance);
			
		} else {
			change = farDrive(inSight, thisCar.getActualSpeed(), breakDistance);
		}
		return change;
	}

	private float farDrive(RoadObject inSight, float actualSpeed, float breakDistance) {
		float change = 0;
		if (inSight instanceof Block) {
			change = (actualSpeed / breakDistance) * -1;
			s = Status.STOPPING;
		}
		if (inSight instanceof Car) {
			Car car = (Car) inSight;
			if (getPrefSpeed() >= car.getActualSpeed()) {
				change = (car.getActualSpeed() - actualSpeed) / breakDistance;
			} else if (car.getActualSpeed() > getPrefSpeed()) {
				change = (getPrefSpeed() - actualSpeed);
			}
			if(Math.abs(car.getActualSpeed() - actualSpeed) <0.1){
				actualSpeed = car.getActualSpeed();
				change = 0;
			}
		}
		return change;
	}

	private float closeDrive(RoadObject inSight, float actualSpeed, float breakDistance) {
		float change = 0;
		if (inSight instanceof Block) {
			change = actualSpeed * (-1);
		}
		if (inSight instanceof Car) {
			Car car = (Car) inSight;
			if (actualSpeed > car.getActualSpeed()) {
				change = (car.getActualSpeed() - actualSpeed) + breakDistance;
			} else if (car.getActualSpeed() > actualSpeed) {
				change = (getPrefSpeed() - actualSpeed) + breakDistance;
			}
		}
		return change;
	}

	private float normalDrive(float actualSpeed, float maxAcc) {
		float change = 0;
		change = (getPrefSpeed() - actualSpeed);
		if ((change * -1) > maxAcc)
			change = maxAcc * -1;
		return change;
	}

	public float getDistance(Point2D.Float a, Point2D.Float b) {
		return (float) Math.sqrt(square(b.x - a.x) + square(b.y - a.y));
	}

	public float square(float number) {
		return (number * number);
	}

	public float getPrefSpeed() {
		return prefSpeed;
	}

	public void setPrefSpeed(float prefSpeed) {
		this.prefSpeed = prefSpeed;
	}

	public float getRangeOfView() {
		return rangeOfView;
	}

	public void setRangeOfView(float rangeOfView) {
		this.rangeOfView = rangeOfView;
	}

	public float getSafetyGap() {
		return safetyGap;
	}

	public void setSafetyGap(float safetyGap) {
		this.safetyGap = safetyGap;
	}

	public float getReactionTime() {
		return reactionTime;
	}

	public void setReactionTime(float reactionTime) {
		this.reactionTime = reactionTime;
	}
}
