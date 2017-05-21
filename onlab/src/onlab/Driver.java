package onlab;

import java.util.ArrayList;

public class Driver {
	private float prefSpeed = 50;
	private float rangeOfView = 50;
	private float safetyGap = 20;
	private float changingPlusSpeed = 5;


	float changingSpace = (float) (HighWay.road.getLaneSize()/Math.sin(Util.toRadian(15)));
	float changingSpaceStraight = (float) Math.sqrt(Util.square(changingSpace) - Util.square(HighWay.road.getLaneSize()));

	float changingDistance =  (Surface.carSize / (float)Math.tan(Util.toRadian(15)));
		
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
		logList(inSightList, thisCar);

		RoadObject inSight = inSightList.get(0);
		RoadObject behind = inSightList.get(1);
		RoadObject leftForward = inSightList.get(2);
		RoadObject leftBackward = inSightList.get(3);
		RoadObject rightForward = inSightList.get(4);
		RoadObject rightBackward = inSightList.get(5);		
		
		float change = 0;
		if(s == Status.CHANGELEFT){
			change = changingDrive(thisCar, leftForward, leftBackward, inSight);				
		} else if( s == Status.CHANGERIGHT){
			change = changingDrive(thisCar, rightForward, rightBackward, inSight);	
		} else {
			//changeRight?
			if(HighWay.isInnerMostLane(thisCar.getLane()) && canChangeRight(thisCar, inSight, behind, rightForward, rightBackward)){
				s = Status.CHANGERIGHT;
				change = changingDrive(thisCar, rightForward, rightBackward, inSight);
			} else if(HighWay.isLaneToTheSide(Util.Direction.RIGHT, thisCar.getLane()) && isNoneToTheRight(thisCar, rightForward, rightBackward)){
				s = Status.CHANGERIGHT;
				change = changingDrive(thisCar, rightForward, rightBackward, inSight);
				
			//changeLeft?
			} else if(inSight != null && isOvertaking(inSight, thisCar) && canChangeLeft(thisCar, inSight, behind, leftForward, leftBackward)){
				s = Status.CHANGELEFT;
				changeAngle(thisCar, inSight);				
				change = changingDrive(thisCar, leftForward, leftBackward, inSight);				
			} else {				
				if (inSight == null) {
					change = normalDrive(thisCar.getCurrentSpeed(), thisCar.getMaxAcc());
				} else{
					change = reactiveDrive(thisCar, inSight, behind, leftForward, leftBackward);
				}
				if(!isChanging(s)){
					setStatus(inSight, change);
				}
			}			
		}
		return change;
	}


	private void changeAngle(Car thisCar, RoadObject inSight) {
		if(inSight instanceof Block){
			float cd = (float) ((Surface.blockSize/2f + thisCar.getSize()/2f) / Math.tan(Util.toRadian(15)));
			if(Util.getDistance(inSight.getPosition(), thisCar.getPosition()) <= cd){
				float angle = Util.toAngle((float) Math.atan((Surface.blockSize/2f + thisCar.getSize()/2f) /Util.getDistance(inSight.getPosition(), thisCar.getPosition())));
				if(Math.abs(angle)<=75){
					thisCar.setChangingAngle((int) Math.abs(angle));
				}
			} 
		} else if(inSight instanceof Car){
			Car car = (Car) inSight;
			float changingTime = pixelToKm(changingSpace)/(thisCar.getCurrentSpeed() + changingPlusSpeed);
			float cd = (float) (changingDistance - kmToPixel(car.getCurrentSpeed()*changingTime/2));
			if(Util.getDistance(inSight.getPosition(), thisCar.getPosition()) <= cd){
				float angle = Util.toAngle((float) Math.atan(Surface.carSize /Util.getDistance(inSight.getPosition(), thisCar.getPosition())));
				if(Math.abs(angle)<=75){
					thisCar.setChangingAngle((int) Math.abs(angle));
				}
			} 
		}		
	}

	private int getSafetyGap(float currentSpeed) {
		int result = (int) (currentSpeed/getPrefSpeed() * getSafetyGapinPixels());
		result = result < getSafetyGapMinimum() ? getSafetyGapMinimum() : result;
		return result;
	}
	
	private int getSafetyGapMinimum() {
		return 5;
	}

	private float normalDrive(float currentSpeed, float maxAcc) {
		float change = 0;
		change = (getPrefSpeed() - currentSpeed);
		if (Math.abs(change) > 1/maxAcc)
			change = 1/maxAcc * (change < 0 ? (-1) : 1);
		return change;
	}
	
	private float reactiveDrive(Car thisCar, RoadObject inSight, RoadObject behind, RoadObject forward, RoadObject backward) {
		float change = 0;
		float breakDistance = getBreakDistance(inSight, thisCar);
		float optimalDistance = getOptimalDistance(inSight, thisCar);
		float changingDistance = getChangingDistance(inSight, thisCar);
		if (optimalDistance <= 0) {
			change = closeDrive(inSight, thisCar.getCurrentSpeed(), thisCar.getMaxAcc(), breakDistance, optimalDistance);				
		} else if (canChangeLeft(thisCar, inSight, behind, forward, backward)){
			if(Util.getDistance(inSight.getPosition(), thisCar.getPosition()) < changingDistance + 5){
				s = Status.CHANGELEFT;
			} else{
				change = normalDrive(thisCar.getCurrentSpeed(), thisCar.getMaxAcc());
			}
		} else {
			change = farDrive(inSight, thisCar.getCurrentSpeed(), thisCar.getMaxAcc(), optimalDistance);
		}
		return change;
	}


	private float closeDrive(RoadObject inSight, float currentSpeed, float maxAcc, float breakDistance, float optimalDistance) {
		float change = 0;
		if(breakDistance < inSight.getSize()){
			if (inSight instanceof Block) {
				change = currentSpeed * (-1);
			}
			
			if (inSight instanceof Car) {
				Car car = (Car) inSight;
				float otherCarSpeed = car.getCurrentSpeed();
				
				if (currentSpeed < otherCarSpeed) {
					change = 0;				
				} else {
					change = (otherCarSpeed - currentSpeed) + breakDistance;
				}
			}	
			if(breakDistance > 0 && currentSpeed <= 10){
				change = 10 - currentSpeed;					
			} 
		} else {
			change = farDrive(inSight, currentSpeed, maxAcc, breakDistance + optimalDistance);
		}
		return change;
	}

	private float farDrive(RoadObject inSight, float currentSpeed, float maxAcc, float breakDistance) {
		float change = 0;	
		float time = pixelToKm(breakDistance) / currentSpeed;
		float tickTime = 10f /1000f /60f /60f; 
		float tickCount = time / tickTime;
		
		if (inSight instanceof Block) {
			change = (currentSpeed / tickCount) * -1;
			s = Status.STOPPING;
		}
		
		if (inSight instanceof Car) {
			Car car = (Car) inSight;
			float otherCarSpeed = car.getCurrentSpeed();
			
			if(Math.abs(otherCarSpeed - currentSpeed) < 0.001){
				currentSpeed = otherCarSpeed;
				change = 0;
			} else if (getPrefSpeed() > otherCarSpeed && currentSpeed > otherCarSpeed) {
				change = (otherCarSpeed - currentSpeed) / tickCount;
			} else if (getPrefSpeed() <= otherCarSpeed || currentSpeed < otherCarSpeed) {
				change = normalDrive(currentSpeed, maxAcc);
			}			
		}
		if(currentSpeed <= 10 && change <= 0){
			change = 10 - currentSpeed;
		}
		return change;
	}

	private float changingDrive(Car thisCar, RoadObject forward, RoadObject backward, RoadObject inSight) {
		float tempPrefSpeed = this.prefSpeed;
		
		this.prefSpeed = (prefSpeed + changingPlusSpeed) > thisCar.getMaxSpeed()? thisCar.getMaxSpeed() : (prefSpeed + changingPlusSpeed);

		if(forward != null){
			float distanceForward = Util.pitagorasAC(HighWay.road.getLaneSize(), Util.getDistance(thisCar.getPosition(), forward.getPosition()));
					
			if(distanceForward < thisCar.getSize()/2f+forward.getSize()/2f){
				thisCar.setChangingAngle(75);
			} else if(distanceForward < thisCar.getSize()/2f+forward.getSize()/2f + 5 					
					&& thisCar.getChangingAngle() < 75){
				thisCar.setChangingAngle(thisCar.getChangingAngle() + 1);
			} 
			
		}
		if (inSight != null){
			float inSightDistance = Util.getDistance(inSight.getPosition(), thisCar.getPosition());
			if(inSightDistance < thisCar.getSize()/2f+inSight.getSize()/2f){
				thisCar.setChangingAngle(75);
			} else if(inSightDistance < thisCar.getSize()/2f+inSight.getSize()/2f + 5 
					&& thisCar.getChangingAngle() < 75){
				thisCar.setChangingAngle(thisCar.getChangingAngle() + 1);
			} 
		}	

		if(backward != null){
			float distanceForward = Util.pitagorasAC(HighWay.road.getLaneSize(), Util.getDistance(thisCar.getPosition(), backward.getPosition()));
					
			if(distanceForward < thisCar.getSize()/2f+backward.getSize()/2f){
				thisCar.setChangingAngle(15);
			} else if(distanceForward < thisCar.getSize()/2f+backward.getSize()/2f + 5 					
					&& thisCar.getChangingAngle() > 15){
				thisCar.setChangingAngle(thisCar.getChangingAngle() - 1);
			} 
			
		}
		
		float change = normalDrive(thisCar.getCurrentSpeed(), thisCar.getMaxAcc());
		
		this.prefSpeed = tempPrefSpeed;		
		return change;
	}

	private void setStatus(RoadObject inSight, float change) {
		if (change == 0 || Math.abs(change) < 0.001 ) {
			if (inSight != null && (inSight instanceof Block || (inSight instanceof Car && ((Car) inSight).getCurrentSpeed() <=10))) {
				s = Status.STOPPING;
			} else{
				s = Status.DRIVING;
			}
		} else if (change > 0) {
			s = Status.ACCELERATING;
		} else if (change < 0) {
			if (inSight != null && (inSight instanceof Block || (inSight instanceof Car && ((Car) inSight).getCurrentSpeed() <=10))) {
				s = Status.STOPPING;
			} else {
				s = Status.DECELERATING;
			}
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
		float distance = Util.getDistance(inSight.getPosition(), thisCar.getPosition());
		if(inSight instanceof Block) {
			if(breakDistance <= 5){
				return true;
			}
			float cd = (float) ((Surface.blockSize/2f + thisCar.getSize()/2f) / Math.tan(Util.toRadian(15)));
			if(distance <= cd){
				return true;
			}
		} 
		if(inSight instanceof Car){
			Car car = (Car) inSight;
			if( !isChanging(car.getDriver().getStatus()) && car.getCurrentSpeed() < getPrefSpeed()){
				if (breakDistance <= 5){
					return true;
				}
				float cd = getChangingDistance(inSight, thisCar);
				if(distance <= (cd + 5) ){
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
	
	private boolean canChangeLeft(Car thisCar, RoadObject inSight, RoadObject behind, RoadObject forward, RoadObject backward){
		if(!HighWay.isLaneToTheSide(Util.Direction.LEFT, thisCar.getLane())){
			return false;
		}
		if(inSight instanceof Car && isChanging(((Car)inSight).getDriver().getStatus())){
			return false;
		}
		if(behind != null && behind.getLane() == thisCar.getLane() && isChangingLeft(behind)){
			return false;			
		}

		float changingTime = pixelToKm(changingSpace)/(thisCar.getCurrentSpeed() + changingPlusSpeed);		
		float breakDistance = 0;
		if(forward != null){
			float distance = Util.getDistance(forward.getPosition(), thisCar.getPosition());
			float distanceForward = (float) Math.sqrt(Util.square(distance) - Util.square(HighWay.road.getLaneSize()));
			breakDistance = thisCar.getSize()/2 + forward.getSize()/2 + getSafetyGapinPixels();
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
				float forwardSpace = kmToPixel(changingTime * forwardCar.getCurrentSpeed());
				
				boolean forwardStopping = forwardCar.getDriver().getStatus() == Status.STOPPED || forwardCar.getDriver().getStatus() == Status.STOPPING;				
				enoughSpace = (distanceForward + forwardSpace - changingSpaceStraight) > breakDistance;
//				enoughSpace = (distanceForward - changingSpaceStraight) > breakDistance;
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
			breakDistance = thisCar.getSize()/2 + backward.getSize()/2 + getSafetyGap();
			float worstChangingSpace = (float) (HighWay.road.getLaneSize()/Math.sin(Util.toRadian(75)));
			float worstChangingSpaceStraight = (float) Math.sqrt(Util.square(worstChangingSpace) - Util.square(HighWay.road.getLaneSize()));
			float distance = Util.getDistance(backwardCar.getPosition(), thisCar.getPosition());
			float distanceBack = (float) Math.sqrt(Util.square(distance) - Util.square(HighWay.road.getLaneSize()));
			float backwardTime = pixelToKm(distanceBack + worstChangingSpaceStraight) / backwardCar.getCurrentSpeed();
			float backwardSpace = kmToPixel(changingTime * backwardCar.getCurrentSpeed());
			boolean enoughSpace = (distanceBack + worstChangingSpaceStraight - backwardSpace) > breakDistance;
			
			if(backwardCar.getCurrentSpeed() > thisCar.getCurrentSpeed() && distanceBack < changingSpaceStraight * 2) {
				return false;
			}
			
			if(changingTime > backwardTime
					|| !enoughSpace
					|| distanceBack < breakDistance){
				return false;
			}
		}
		
		if(forward != null && backward != null){
			float distance = Util.getDistance(forward.getPosition(), backward.getPosition());
			breakDistance = thisCar.getSize() + getSafetyGap();	
			if(distance < 2 * breakDistance ){
				return false;
			}
		}
		return true;
	}
	
	
	private boolean isChangingLeft(RoadObject behind) {
		if(behind instanceof Car){
			Car car = (Car) behind;
			if(car.getDriver().getStatus() == Status.CHANGELEFT){
				return true;
			}
		}
		return false;
	}

	private boolean canChangeRight(Car thisCar, RoadObject inSight, RoadObject behind, RoadObject forward, RoadObject backward){
		if(!HighWay.isLaneToTheSide(Util.Direction.RIGHT, thisCar.getLane())){
			return false;
		}
		if(inSight != null && inSight instanceof Car && isChanging(((Car)inSight).getDriver().getStatus())){
			return false;
		}
		if(behind != null && behind.getLane() == thisCar.getLane() && isChangingRight(behind)){
			return false;			
		}
		
		float changingTime = pixelToKm(changingSpace)/(thisCar.getCurrentSpeed() + changingPlusSpeed);		
		float breakDistance = thisCar.getSize() + getSafetyGapinPixels();		
		
		if(forward != null){
			if(forward instanceof Block){
				return false;
			} else if(forward instanceof Car){
				Car forwardCar = (Car) forward;
				float distance = Util.getDistance(forward.getPosition(), thisCar.getPosition());
				float distanceForward = (float) Math.sqrt(Util.square(distance) - Util.square(HighWay.road.getLaneSize()));
				float forwardSpace = kmToPixel(changingTime * forwardCar.getCurrentSpeed());
				
				boolean forwardStopping = forwardCar.getDriver().getStatus() == Status.STOPPED || forwardCar.getDriver().getStatus() == Status.STOPPING;
//				boolean enoughSpace = (distanceForward + forwardSpace - changingSpaceStraight) > breakDistance;
				boolean enoughSpace = (distanceForward - changingSpaceStraight) > breakDistance;
				if(forwardStopping || !enoughSpace || distanceForward < breakDistance || forwardCar.getCurrentSpeed() < getPrefSpeed()){
					return false;
				}	
			}
		}
		
		if(backward != null && backward instanceof Car){
			float worstChangingSpace = (float) (HighWay.road.getLaneSize()/Math.sin(Util.toRadian(75)));
			float worstChangingSpaceStraight = (float) Math.sqrt(Util.square(worstChangingSpace) - Util.square(HighWay.road.getLaneSize()));
			
			Car backwardCar = (Car) backward;
			float distance = Util.getDistance(backwardCar.getPosition(), thisCar.getPosition());
			float distanceBack = (float) Math.sqrt(Util.square(distance) - Util.square(HighWay.road.getLaneSize()));
			float backwardTime = pixelToKm(distanceBack + worstChangingSpaceStraight) / backwardCar.getCurrentSpeed();
			float backwardSpace = kmToPixel(changingTime * backwardCar.getCurrentSpeed());
			boolean enoughSpace = (distanceBack + worstChangingSpaceStraight - backwardSpace) > breakDistance;
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
	
	private boolean isChangingRight(RoadObject behind) {
		if(behind instanceof Car){
			Car car = (Car) behind;
			if(car.getDriver().getStatus() == Status.CHANGERIGHT){
				return true;
			}
		}
		return false;
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
		float breakDistance =  distance - getSafetyGapMinimum() - thisCar.getSize()/2f - inSight.getSize()/2f;
		return breakDistance;
	}

	private float getOptimalDistance(RoadObject inSight, Car thisCar){
		float distance = Util.getDistance(inSight.getPosition(), thisCar.getPosition());
		float breakDistance =  distance - getSafetyGap(thisCar.getCurrentSpeed()) - thisCar.getSize()/2f - inSight.getSize()/2f;
		return breakDistance;
	}

	private float getChangingDistance(RoadObject inSight, Car thisCar) {
		float cd = (float) changingDistance;
		if(inSight instanceof Car){
			Car car = (Car) inSight;
			float changingTime = pixelToKm(changingSpace)/(thisCar.getCurrentSpeed() + changingPlusSpeed);
			cd = (float) (changingDistance - kmToPixel(car.getCurrentSpeed()*changingTime/2));
		}		
		return cd;
	}

	private void logList(ArrayList<RoadObject> inSightList, Car thisCar) {
		RoadObject inSight = inSightList.get(0);
		RoadObject behind = inSightList.get(1);
		RoadObject leftForward = inSightList.get(2);
		RoadObject leftBackward = inSightList.get(3);
		RoadObject rightForward = inSightList.get(4);
		RoadObject rightBackward = inSightList.get(5);

		System.out.println("Car" + thisCar.id);
		if(inSight == null){
			System.out.println("    inSight: " + "null");			
		} else {
			System.out.println("    inSight: " + inSight.id);						
		}
		if(behind == null){
			System.out.println("    behind: " + "null");			
		} else {
			System.out.println("    behind: " + behind.id);						
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
	
	private float kmToPixel(float advance){
		return HighWay.getKmPixelRatio() * advance;
	}	
	
	private float pixelToKm(float advance){
		return 1 / HighWay.getKmPixelRatio() * advance;
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

	public float getSafetyGapinPixels() {
		return HighWay.getKmPixelRatio() * safetyGap;
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
