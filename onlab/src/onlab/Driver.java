package onlab;

import java.awt.geom.Point2D;

public class Driver {
	float prefSpeed = 50;
	float rangeOfView = 50;
	float safetyGap;
	float reactionTime = 2;
	
	Driver(){}
	
	
	public float drive(RoadObject inSight, Car thisCar){
		if(inSight == null){
			return (prefSpeed - thisCar.actualSpeed);
		}
		
		float distance = getDistance(inSight.position, thisCar.position) - safetyGap;
		if(distance <= 0){
			return thisCar.actualSpeed*-1;
		}
		
		if(inSight instanceof Block){
			return (thisCar.actualSpeed/distance)*-1;
		}
		if(inSight instanceof Car){
			Car car = (Car) inSight;
			if(prefSpeed > car.actualSpeed)
				return (car.actualSpeed - thisCar.actualSpeed)/distance;
			else if (car.actualSpeed > prefSpeed)
				return (prefSpeed - thisCar.actualSpeed)/distance;
		}
		return 0;
	}
	
	public float getDistance(Point2D.Float a, Point2D.Float b){
		return (float) Math.sqrt(square(b.x-a.x)+square(b.y-a.y));
	}
	public float square(float number){
		return (number*number);
	}
}
