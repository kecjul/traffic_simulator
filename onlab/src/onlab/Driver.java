package onlab;

import java.util.ArrayList;

public class Driver {
	private float prefSpeed = 50;
	private float rangeOfView = 50;
	private float safetyGap = 20;
	private float changingPlusSpeed = 10;


	float changingSpace = (float) (HighWay.road.getLaneSize()/Math.sin(Util.toRadian(15)));
	float changingSpaceStraight = (float) Math.sqrt(Util.square(changingSpace) - Util.square(HighWay.road.getLaneSize()));
	float stopDistance = 20 + safetyGap;
	
	
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
				return "changing lanes to the left";
			}
		},
		CHANGERIGHT {
			@Override
			public String toString() {
				return "changing lanes to the right";
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

	public float drive(ArrayList<RoadObject> inSightList, Car thisCar) {
//		logList(inSightList, thisCar);
		
		RoadObject inSight = inSightList.get(0);
		RoadObject leftForward = inSightList.get(1);
		RoadObject leftBackward = inSightList.get(2);
		RoadObject rightForward = inSightList.get(3);
		RoadObject rightBackward = inSightList.get(4);
		
		if(thisCar.getChangingAngle() != 15){
			changingSpace = (float) (HighWay.road.getLaneSize()/Math.sin(Util.toRadian(thisCar.getChangingAngle())));
			changingSpaceStraight = (float) Math.sqrt(Util.square(changingSpace) - Util.square(HighWay.road.getLaneSize()));
		}
		
		stopDistance = thisCar.getSize() + getSafetyGap(thisCar.getCurrentSpeed());
		
		
		float change = 0;
		if(s == Status.CHANGELEFT){
			change = changingDrive(thisCar, leftForward);				
		} else if( s == Status.CHANGERIGHT){
			change = changingDrive(thisCar, rightForward);	
		} else {
			//changeRight?
			if(HighWay.isInnerMostLane(thisCar.getLane()) && canChangeRight(thisCar, inSight, rightForward, rightBackward)){
				s = Status.CHANGERIGHT;
				change = changingDrive(thisCar, rightForward);
			} else if(HighWay.isLaneToTheSide(Util.Direction.RIGHT, thisCar.getLane()) && isNoneToTheRight(thisCar, rightForward, rightBackward)){
				s = Status.CHANGERIGHT;
				change = changingDrive(thisCar, rightForward);
				
			//changeLeft?
			} else if(inSight != null && isOvertaking(inSight, thisCar) && canChangeLeft(thisCar, inSight, leftForward, leftBackward)){
				s = Status.CHANGELEFT;
				change = changingDrive(thisCar, leftForward);
				
			} else {				
				if (inSight == null) {
					change = normalDrive(thisCar.getCurrentSpeed(), thisCar.getMaxAcc());
				} else{
					change = reactiveDrive(thisCar, inSight, leftForward, leftBackward);
				}
				setStatus(inSight, change);				
			}			
		}
		return change;
	}


	private int getSafetyGap(float currentSpeed) {
		int result = (int) (currentSpeed/getPrefSpeed() * getSafetyGap());
		return result < 5 ? 5: result;
	}

	private float normalDrive(float currentSpeed, float maxAcc) {
		float change = 0;
		change = (getPrefSpeed() - currentSpeed);
		if (Math.abs(change) > 1/maxAcc)
			change = 1/maxAcc * (change < 0 ? (-1) : 1);
		return change;
	}
	
	private float reactiveDrive(Car thisCar, RoadObject inSight, RoadObject forward, RoadObject backward) {
		float change = 0;
		float breakDistance = getBreakDistance(inSight, thisCar);
		
		if (breakDistance <= 5) {
			change = closeDrive(inSight, thisCar.getCurrentSpeed(), thisCar.getMaxAcc(), breakDistance);				
		} else if (canChangeLeft(thisCar, inSight, forward, backward)){
			change = normalDrive(thisCar.getCurrentSpeed(), thisCar.getMaxAcc());
		} else {
			change = farDrive(inSight, thisCar.getCurrentSpeed(), thisCar.getMaxAcc(), breakDistance);
		}
		return change;
	}

	private float closeDrive(RoadObject inSight, float currentSpeed, float maxAcc, float breakDistance) {
		float change = 0;
		
		if (inSight instanceof Block) {
			change = currentSpeed * (-1);
		}
		
		if (inSight instanceof Car) {
			Car car = (Car) inSight;
			float otherCarSpeed = car.getCurrentSpeed();
			
			if (currentSpeed > otherCarSpeed || getPrefSpeed() > otherCarSpeed) {
				change = (otherCarSpeed - currentSpeed) + breakDistance;
			} else if (currentSpeed < otherCarSpeed) {
				change = normalDrive(currentSpeed, maxAcc);				
			}
		}
		
		return change;
	}

	private float farDrive(RoadObject inSight, float currentSpeed, float maxAcc, float breakDistance) {
		float change = 0;		
		if(currentSpeed > 10){
			if (inSight instanceof Block) {
				change = (currentSpeed / breakDistance) * -1;
				s = Status.STOPPING;
			}
			
			if (inSight instanceof Car) {
				Car car = (Car) inSight;
				float otherCarSpeed = car.getCurrentSpeed();
				
				if(Math.abs(otherCarSpeed - currentSpeed) < 0.001){
					currentSpeed = otherCarSpeed;
					change = 0;
				}
				
				if (getPrefSpeed() > otherCarSpeed) {
					change = (otherCarSpeed - currentSpeed) / breakDistance;
				} else if (getPrefSpeed() <= otherCarSpeed) {
					change = normalDrive(currentSpeed, maxAcc);
				}			
			}
		} else{
			change = 10 - currentSpeed;
		}
		return change;
	}

	private float changingDrive(Car thisCar, RoadObject forward) {
		float tempPrefSpeed = this.prefSpeed;
		
		this.prefSpeed = (prefSpeed + changingPlusSpeed) > thisCar.getMaxSpeed()? thisCar.getMaxSpeed() : (prefSpeed + changingPlusSpeed);

		if(forward != null){
			float distance = Util.pitagorasAC(HighWay.road.getLaneSize(), Util.getDistance(thisCar.getPosition(), forward.getPosition()));
			if(distance < thisCar.getSize()){
				thisCar.setChangingAngle(75);
			} else if(distance < thisCar.getSize() + getSafetyGap(thisCar.getCurrentSpeed()) && thisCar.getChangingAngle() < 75){
				thisCar.setChangingAngle(thisCar.getChangingAngle() + 5);
			} 
		}
		float change = normalDrive(thisCar.getCurrentSpeed(), thisCar.getMaxAcc());
		
		this.prefSpeed = tempPrefSpeed;		
		return change;
	}

	private void setStatus(RoadObject inSight, float change) {
		if (inSight != null && inSight instanceof Block) {
			s = Status.STOPPING;
		} else if (change == 0 || Math.abs(change) < 0.001 ) {
			s = Status.DRIVING;
		} else if (change > 1) {
			s = Status.ACCELERATING;
		} else if (change < -1) {
			s = Status.DECELERATING;
		} else {
			s = Status.DRIVING;
		}
	}
	
	private boolean isNoneToTheRight(Car thisCar, RoadObject forward, RoadObject backward) {
		if(forward == null && backward == null){
			return true;
		} else if(forward instanceof Car 
				&& ((Car)forward).getCurrentSpeed() > thisCar.getDriver().getPrefSpeed() + changingPlusSpeed 
				&& backward == null) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isOvertaking(RoadObject inSight, Car thisCar) {
		float breakDistance = getBreakDistance(inSight, thisCar);
		if (breakDistance <= 5) {
			if(inSight instanceof Block) {
				return true;
			} else {
				Car car = (Car) inSight;
				if( !isChanging(car.getDriver().getStatus()) && car.getCurrentSpeed() < getPrefSpeed()){
					return true;
				}
			}
		}
		return false;
	}	

//	private boolean canChange(RoadObject inSight, Util.Direction dir, Car thisCar) {
//		boolean result = false;	
//		System.out.println("canChange " + dir.toString());
//		RoadObject forward = thisCar.getSight(dir, Util.Direction.FORWARD);
//		RoadObject backward = thisCar.getSight(dir, Util.Direction.BACKWARD);
//		
//		if(!HighWay.isLaneToTheSide(dir, thisCar.getLane())){
//			return false;
//		}else if(dir == Util.Direction.LEFT){
//			result = canChangeLeft(inSight, forward, backward, thisCar); //, thisCar.getCurrentSpeed());
//		} else if(dir == Util.Direction.RIGHT){
//			result = canChangeRight(forward, backward, thisCar); //, thisCar.getDriver().getPrefSpeed());
//		}
//		return result;
//	}
	
	private boolean canChangeLeft(Car thisCar, RoadObject inSight, RoadObject forward, RoadObject backward){
		if(!HighWay.isLaneToTheSide(Util.Direction.LEFT, thisCar.getLane())){
			return false;
		}
		if(inSight instanceof Car && isChanging(((Car)inSight).getDriver().getStatus())){
			return false;
		}
		
		float changingTime = changingSpace/(thisCar.getCurrentSpeed() + changingPlusSpeed);		
		float breakDistance = thisCar.getSize() + getSafetyGap();		
		
		if(forward != null){
			float distance = Util.getDistance(forward.getPosition(), thisCar.getPosition());
			float distanceForward = (float) Math.sqrt(Util.square(distance) - Util.square(HighWay.road.getLaneSize()));
			
			boolean forwardFarther = false;
			if(inSight != null){
				float distanceInSight =  Util.getDistance(inSight.getPosition(), thisCar.getPosition());
				forwardFarther = distanceForward > distanceInSight;
			}
			boolean enoughSpace = false;
			if(forward instanceof Block){
				if(!forwardFarther){
					return false;
				}
				enoughSpace = distanceForward - changingSpaceStraight > breakDistance;				
			} else if(forward instanceof Car){
				Car forwardCar = (Car) forward;
				float forwardSpace = changingTime * forwardCar.getCurrentSpeed();
				
				boolean forwardStopping = forwardCar.getDriver().getStatus() == Status.STOPPED || forwardCar.getDriver().getStatus() == Status.STOPPING;				
//				enoughSpace = (distanceForward + forwardSpace - changingSpaceStraight) > breakDistance;
				enoughSpace = (distanceForward - changingSpaceStraight) > breakDistance;
				if(forwardStopping || distanceForward < breakDistance){
					return false;	
				}
				
				if(inSight != null && inSight instanceof Car){
					Car inSightCar = (Car) inSight;						
					if(forwardCar.getCurrentSpeed() < inSightCar.getCurrentSpeed()) {								
						return false;
					}
				}
			}
			
			if(!enoughSpace){
				return false;
			}
		}
		
		if(backward != null && backward instanceof Car){
			Car backwardCar = (Car) backward;
			float distance = Util.getDistance(backwardCar.getPosition(), thisCar.getPosition());
			float distanceBack = (float) Math.sqrt(Util.square(distance) - Util.square(HighWay.road.getLaneSize()));
			float backwardTime = (distanceBack + changingSpaceStraight) / backwardCar.getCurrentSpeed();
			float backwardSpace = changingTime * backwardCar.getCurrentSpeed();
			boolean enoughSpace = (distanceBack + changingSpaceStraight - backwardSpace) > breakDistance;
			
			if(backwardCar.getCurrentSpeed() > thisCar.getCurrentSpeed() 
					|| changingTime > backwardTime
					|| !enoughSpace
					|| distanceBack < breakDistance){
				return false;
			}
		}
		
		if(forward != null && backward != null){
			float distance = Util.getDistance(forward.getPosition(), backward.getPosition());
			if(distance < 2 * breakDistance ){
				return false;
			}
		}
		return true;
	}
	
	
	private boolean canChangeRight(Car thisCar, RoadObject inSight, RoadObject forward, RoadObject backward){
		if(!HighWay.isLaneToTheSide(Util.Direction.RIGHT, thisCar.getLane())){
			return false;
		}
		if(inSight != null && inSight instanceof Car && isChanging(((Car)inSight).getDriver().getStatus())){
			return false;
		}
		float changingTime = changingSpace/(thisCar.getCurrentSpeed() + changingPlusSpeed);		
		float breakDistance = thisCar.getSize() + getSafetyGap();		
		
		if(forward != null){
			if(forward instanceof Block){
				return false;
			} else if(forward instanceof Car){
				Car forwardCar = (Car) forward;
				float distance = Util.getDistance(forward.getPosition(), thisCar.getPosition());
				float distanceForward = (float) Math.sqrt(Util.square(distance) - Util.square(HighWay.road.getLaneSize()));
				float forwardSpace = changingTime * forwardCar.getCurrentSpeed();
				
				boolean forwardStopping = forwardCar.getDriver().getStatus() == Status.STOPPED || forwardCar.getDriver().getStatus() == Status.STOPPING;
//				boolean enoughSpace = (distanceForward + forwardSpace - changingSpaceStraight) > breakDistance;
				boolean enoughSpace = (distanceForward - changingSpaceStraight) > breakDistance;
				if(forwardStopping || !enoughSpace || distanceForward < breakDistance || forwardCar.getCurrentSpeed() < getPrefSpeed()){
					return false;
				}	
			}
		}
		
		if(backward != null && backward instanceof Car){
			Car backwardCar = (Car) backward;
			float distance = Util.getDistance(backwardCar.getPosition(), thisCar.getPosition());
			float distanceBack = (float) Math.sqrt(Util.square(distance) - Util.square(HighWay.road.getLaneSize()));
			float backwardTime = (distanceBack + changingSpaceStraight) / backwardCar.getCurrentSpeed();
			float backwardSpace = changingTime * backwardCar.getCurrentSpeed();
			boolean enoughSpace = (distanceBack + changingSpaceStraight - backwardSpace) > breakDistance;
			if(backwardCar.getCurrentSpeed() > thisCar.getCurrentSpeed() 
					|| changingTime > backwardTime
					|| !enoughSpace
					|| distanceBack < breakDistance){
				return false;
			}
		}
		
		if(forward != null && backward != null){
			float distance = Util.getDistance(forward.getPosition(), backward.getPosition());
			if(distance < 2 * breakDistance ){
				return false;
			}
		}
		return true;
	}

	private boolean isChanging(Status s){
		if(s == Status.CHANGELEFT || s == Status.CHANGERIGHT){
			return true;
		}
		return false;
	}
	
	private Util.Direction getDirection(Status s){
		if(s == Status.CHANGELEFT)
			return Util.Direction.LEFT;
		else if( s == Status.CHANGERIGHT){
			return Util.Direction.RIGHT;
		}
		return null;
	}
	
//	private Util.Direction getDirection(Status s){
//		if(s == Status.CHANGERIGHT){
//			return Util.Direction.RIGHT;
//		} else if (s == Status.CHANGELEFT){
//			return Util.Direction.LEFT;
//		}
//		return null;
//	}
	
	private float getBreakDistance(RoadObject inSight, Car thisCar){
		float distance = Util.getDistance(inSight.getPosition(), thisCar.getPosition());
		float breakDistance =  distance - getSafetyGap(thisCar.getCurrentSpeed()) - thisCar.getSize();
		return breakDistance;
	}

	private void logList(ArrayList<RoadObject> inSightList, Car thisCar) {
		RoadObject inSight = inSightList.get(0);
		RoadObject leftForward = inSightList.get(1);
		RoadObject leftBackward = inSightList.get(2);
		RoadObject rightForward = inSightList.get(3);
		RoadObject rightBackward = inSightList.get(4);

		System.out.println("Car" + thisCar.id);
		if(inSight == null){
			System.out.println("    inSight: " + "null");			
		} else {
			System.out.println("    inSight: " + inSight.id);						
		}
		if(leftForward == null){
			System.out.println("    leftForward: " + "null");			
		} else {
			System.out.println("    leftForward: " + leftForward.id);						
		}
		if(leftBackward == null){
			System.out.println("    leftBackward: " + "null");			
		} else {
			System.out.println("    leftBackward: " + leftBackward.id);						
		}
		if(rightForward == null){
			System.out.println("    rightForward: " + "null");			
		} else {
			System.out.println("    rightForward: " + rightForward.id);						
		}
		if(rightBackward == null){
			System.out.println("    rightBackward: " + "null");			
		} else {
			System.out.println("    rightBackward: " + rightBackward.id);						
		}
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
	
	public Status getStatus() {
		return s;
	}		
	public void setStatus(Status s) {
		this.s = s ;
	}	
}
