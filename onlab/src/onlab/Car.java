package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import onlab.Driver.Status;

public class Car extends RoadObject{
	private float maxSpeed = 50;
	private float maxAcc = 2;
	private Color color = Color.GREEN;
	private float currentSpeed;
	private Driver driver;
	private String name;
	private int changingAngle = 15;

	Car(){
		setSize(20);
		setDriver(new Driver());
	}
	
	Car(Point2D.Float startPosition){
		setSize(20);
		setPosition(startPosition);
		setDriver(new Driver());
		count++;
		id = count;
		setCurrentSpeed(getDriver().getPrefSpeed());
	}
	
	Car(Point2D.Float startPosition, float maxSpeed, float maxAcc, Color color, float prefSpeed, float range, float safety){
		count++;
		this.id = count;
		setSize(20);
		this.setPosition(startPosition);
		this.setMaxSpeed(maxSpeed);
		this.setMaxAcc(maxAcc);
		this.setColor(color);
		this.setDriver(new Driver(prefSpeed, range, safety));
		this.setCurrentSpeed(getDriver().getPrefSpeed());
	}
	
	public Car(java.awt.geom.Point2D.Float startPosition, Car car, int lane) {
		count++;
		id = count;
		setSize(20);
		setPosition(startPosition);
		setDriverProfile(car);
		setLane(lane);
		this.setCurrentSpeed(getDriver().getPrefSpeed());
	}

	public void renew(java.awt.geom.Point2D.Float startPosition, Car car, int lane) {
		count++;
		id = count;
		setPosition(startPosition);
		setDriver(new Driver());
		setDriverProfile(car);
		setLane(lane);
		setCurrentSpeed(getDriver().getPrefSpeed());
	}

	public void drive() {
		float change = 0;
		ArrayList<RoadObject> inSight = HighWay.getSight(this);
		change = getDriver().drive(inSight, this);
		change = getMaxChange(change);
		
		setCurrentSpeed(getCurrentSpeed() + change);
		
		if (getCurrentSpeed() <= 0.0001){
			setCurrentSpeed(0);
			getDriver().s = Status.STOPPED;
		}
		if (getCurrentSpeed() > getMaxSpeed()){
			setCurrentSpeed(getMaxSpeed());
		}		
	}
	
	public float getMaxChange(float change){
		// (100/getMaxAcc) ->km/h/s, (100/getMaxAcc)/100 ->km/h/tick			  
		if (change > 1/getMaxAcc()) {
			change = 1/getMaxAcc();
		}
		return change;
	}
	
	public RoadObject getSight(Util.Direction dirLR, Util.Direction dirFB){
		int lane = this.getLane();
		RoadObject result = null;
		if(dirLR == Util.Direction.LEFT){
			lane++;
		} else if(dirLR == Util.Direction.RIGHT){
			lane--;
		}
		
		if(dirFB == Util.Direction.FORWARD){
			result = HighWay.getSightForward(this, lane, false);
		} else if(dirFB == Util.Direction.BACKWARD){
			result = HighWay.getSightBackward(this, lane);			
		}		
		return result;
	}

	public float getAdvance(float timeSpeed){
		double tenMsInHour =  10d/1000/60/60 * timeSpeed;
		return (float) (getCurrentSpeed() * tenMsInHour);
	}
	
	public float secToNano(float time){
		return (float) time * 1000000000;
	}

	public float getRange(){
		return getDriver().getRangeOfView();
	}
	public void setRange(float range){
		driver.setRangeOfView(range);
	}

	public float getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public float getMaxAcc() {
		return maxAcc;
	}

	public void setMaxAcc(float maxAcc) {
		this.maxAcc = maxAcc;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public float getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(float currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public void setDriverProfile(Car car) {
		if(HighWay.getRoadObjects().contains(this)){
			HighWay.decrementCount(HighWay.getDriverProfileIndex(this.getName()));
			HighWay.incrementCount(HighWay.getDriverProfileIndex(car.getName()));			
		}
		this.setName(car.getName());
		this.setMaxSpeed(car.getMaxSpeed());
		this.setMaxAcc(car.getMaxAcc());
		this.setColor(car.getColor());
		this.setCurrentSpeed(0);
		Driver d = car.getDriver();
		this.setDriver(new Driver(d.getPrefSpeed(), d.getRangeOfView(), d.getSafetyGap()));
		this.setCurrentSpeed(d.getPrefSpeed());
	}
	
	@Override
	public Car clone(){
		Car temp = new Car();
		temp.setDriverProfile(this);
		return temp;
	}

	public void setDriverProfile(String name) {
		Car car = HighWay.getDriverProfile(name);
		setDriverProfile(car);
	}

	public int getChangingAngle() {
		return changingAngle;
	}

	public void setChangingAngle(int changingAngle) {
		this.changingAngle = changingAngle;
	}

}
