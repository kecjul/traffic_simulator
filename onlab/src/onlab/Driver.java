package onlab;

public class Driver {
	private float prefSpeed = 50;
	private float rangeOfView = 50;
	private float safetyGap = 20;
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
		CHANGELEFT {
			@Override
			public String toString() {
				return "changeleft";
			}
		},
		CHANGERIGHT {
			@Override
			public String toString() {
				return "changeright";
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
		if(inSight != null && inSight.getLane() != thisCar.getLane()){
			inSight = null;
		} 
		
		if(s == Status.CHANGELEFT || s == Status.CHANGERIGHT ){
			change = changingDrive(thisCar);			
		} else {
			//changeRight?
			if(HighWay.isInnerMostLane(thisCar.getLane()) && canChange(Util.Direction.RIGHT, thisCar)){
				s = Status.CHANGERIGHT;
				change = changingDrive(thisCar);
			} else if(HighWay.isLaneToTheSide(Util.Direction.RIGHT, thisCar.getLane()) && isNoneToTheRight(thisCar)){
				s = Status.CHANGERIGHT;
				change = changingDrive(thisCar);
			//changeLeft?
			} else if(inSight != null && isOvertaking(inSight, thisCar) && canChange(Util.Direction.LEFT, thisCar)){
				s = Status.CHANGELEFT;
				change = changingDrive(thisCar);
			} else {
				if (inSight == null) {
					change = normalDrive(thisCar.getCurrentSpeed(), thisCar.getMaxAcc());
				} else{
					change = reactiveDrive(inSight, thisCar);
				}
				if (inSight != null && inSight instanceof Block) {
					s = Status.STOPPING;
				} else if (change == 0 || Math.abs(change) < 0.001 ) {
					s = Status.DRIVING;
				} else if (change > 0) {
					s = Status.ACCELERATING;
				} else if (change < 0) {
					s = Status.DECELERATING;
				}
			}			
		}
		return change;
	}

	private boolean isNoneToTheRight(Car thisCar) {
		RoadObject forward = thisCar.getSight(Util.Direction.RIGHT, Util.Direction.FORWARD);
		RoadObject backward = thisCar.getSight(Util.Direction.RIGHT, Util.Direction.BACKWARD);
		if(forward == null && backward == null){
			return true;
		} else{
			return false;
		}
	}

	private boolean isOvertaking(RoadObject inSight, Car thisCar) {
		float breakDistance = Util.getDistance(inSight.getPosition(), thisCar.getPosition()) - getSafetyGap();
		if (breakDistance <= 10) {
			if(inSight instanceof Block) {
				return true;
			} else {
				Car car = (Car) inSight;
				if(car.getDriver().getStatus() == Status.DRIVING && car.getCurrentSpeed() < getPrefSpeed()){
					return true;
				}
			}
		}
		return false;
	}

	private float changingDrive(Car thisCar) {
		float tempPrefSpeed = this.prefSpeed;
		this.prefSpeed = (prefSpeed +5) > thisCar.getMaxSpeed()? thisCar.getMaxSpeed() : (prefSpeed +20);
		float change = normalDrive(thisCar.getCurrentSpeed(), thisCar.getMaxAcc());
		this.prefSpeed = tempPrefSpeed;
		return change;
	}
	
	private float reactiveDrive(RoadObject inSight, Car thisCar) {
		float change = 0;
		float breakDistance = Util.getDistance(inSight.getPosition(), thisCar.getPosition()) - getSafetyGap();
		
		if (breakDistance <= 0) {
			change = closeDrive(inSight, thisCar.getCurrentSpeed(), breakDistance);				
		} else if (canChange(Util.Direction.LEFT, thisCar)){
			change = normalDrive(thisCar.getCurrentSpeed(), thisCar.getMaxAcc());
		} else {
			change = farDrive(inSight, thisCar.getCurrentSpeed(), breakDistance);
		}
		return change;
	}

	private boolean canChange(Util.Direction dir, Car thisCar) {
		boolean result = false;		
		RoadObject forward = thisCar.getSight(dir, Util.Direction.FORWARD);
		RoadObject backward = thisCar.getSight(dir, Util.Direction.BACKWARD);
		
		if(!HighWay.isLaneToTheSide(dir, thisCar.getLane())){
			return false;
		}else if(dir == Util.Direction.LEFT){
			result = canChange(forward, backward, thisCar.getCurrentSpeed());
		} else if(dir == Util.Direction.RIGHT){
			result = canChange(forward, backward, thisCar.getDriver().getPrefSpeed());
		}
		return result;
	}

	private boolean canChange(RoadObject forward, RoadObject backward, float speed) {
		if(forward instanceof Car){
			Car forwardCar = (Car) forward;
			if(forwardCar.getCurrentSpeed() < speed){
				return false;
			}
		} else if(forward instanceof Block){
			return false;
		}
		if(backward instanceof Car){
			Car backwardCar = (Car) backward;
			if(backwardCar.getCurrentSpeed() > speed){
				return false;
			}
		}		
		return true;
	}
	
	private float farDrive(RoadObject inSight, float actualSpeed, float breakDistance) {
		float change = 0;
		if (inSight instanceof Block) {
			change = (actualSpeed / breakDistance) * -1;
			s = Status.STOPPING;
		}
		if (inSight instanceof Car) {
			Car car = (Car) inSight;
			if (getPrefSpeed() >= car.getCurrentSpeed()) {
				change = (car.getCurrentSpeed() - actualSpeed) / breakDistance;
			} else if (car.getCurrentSpeed() > getPrefSpeed()) {
				change = (getPrefSpeed() - actualSpeed);
			}
			if(Math.abs(car.getCurrentSpeed() - actualSpeed) <0.1){
				actualSpeed = car.getCurrentSpeed();
				change = 0;
			}
		}
		return change;
	}

	private float closeDrive(RoadObject inSight, float currentSpeed, float breakDistance) {
		float change = 0;
		if (inSight instanceof Block) {
			change = currentSpeed * (-1);
		}
		if (inSight instanceof Car) {
			Car car = (Car) inSight;
			if (currentSpeed > car.getCurrentSpeed()) {
				change = (car.getCurrentSpeed() - currentSpeed) + breakDistance;
			} else if (car.getCurrentSpeed() > currentSpeed) {
				change = (getPrefSpeed() - currentSpeed) + breakDistance;
			}
		}
		
		return change;
	}

	private float normalDrive(float currentSpeed, float maxAcc) {
		float change = 0;
		change = (getPrefSpeed() - currentSpeed);
		if ((change * -1) > 1/maxAcc)
			change = 1/maxAcc * -1;
		return change;
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
	
	public Status getStatus() {
		return s;
	}		
	public void setStatus(Status s) {
		this.s = s ;
	}	

	public void setReactionTime(Status s) {
		this.s = s;
	}
}
