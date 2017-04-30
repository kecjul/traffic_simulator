package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import onlab.Driver.Status;

public class Car extends RoadObject{
	private float maxSpeed = 50;
	private float maxAcc = 2;
	private Color color = Color.GREEN;
	private float currentSpeed;
	private Driver driver;
	private LinkedList<RoadObject> sights = new LinkedList<RoadObject>();
	private LinkedList<Float> times = new LinkedList<Float>();
	private String name;

	Car(){setDriver(new Driver());}
	
	Car(Point2D.Float startPosition){
		setPosition(startPosition);
		setDriver(new Driver());
		count++;
		id = count;
		setCurrentSpeed(100);
	}
	
	Car(Point2D.Float startPosition, float maxSpeed, float maxAcc, Color color, float prefSpeed, float range, float safety){
		count++;
		this.id = count;
		this.setPosition(startPosition);
		this.setMaxSpeed(maxSpeed);
		this.setMaxAcc(maxAcc);
		this.setColor(color);
		this.setCurrentSpeed(0);
		this.setDriver(new Driver(prefSpeed, range, safety));
	}
	
	public Car(java.awt.geom.Point2D.Float startPosition, Car car, int lane) {
		count++;
		id = count;
		setPosition(startPosition);
		setDriverProfile(car);
		setLane(lane);
	}

	public void drive() {
		float acc = 0;
		RoadObject inSight = HighWay.getSightForward(this, getLane());
		addSights(inSight);
		addTimes((float) System.nanoTime());
		if ((getTimes().peekLast() - getTimes().peekFirst()) > secToNano(getDriver().getReactionTime())) {
			getTimes().pop();
			acc = getDriver().drive(getSights().pop(), this);
		}
		// (100/getMaxAcc) ->km/h/s, (100/getMaxAcc)/100 ->km/h/tick  
		if (acc > 1/getMaxAcc()) {
			acc = 1/getMaxAcc();
		}
		
		setCurrentSpeed(getCurrentSpeed() + acc);
		
		if (getCurrentSpeed() <= 0.0001){
			setCurrentSpeed(0);
			getDriver().s = Status.STOPPED;
		}
		if (getCurrentSpeed() > getMaxSpeed()){
			setCurrentSpeed(getMaxSpeed());
			getDriver().s = Status.DRIVING;
		}		
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
			result = HighWay.getSightForward(this, lane);
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

	float getRange(){
		return getDriver().getRangeOfView();
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

	public LinkedList<RoadObject> getSights() {
		return sights;
	}

	public void setSights(LinkedList<RoadObject> sights) {
		this.sights = sights;
	}
	
	public void addSights(RoadObject ro) {
		sights.add(ro);
	}

	public LinkedList<Float> getTimes() {
		return times;
	}

	public void setTimes(LinkedList<Float> times) {
		this.times = times;
	}	
	
	public void addTimes(Float time) {
		times.add(time);
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public void setDriverProfile(Car car) {
		this.setName(car.getName());
		this.setMaxSpeed(car.getMaxSpeed());
		this.setMaxAcc(car.getMaxAcc());
		this.setColor(car.getColor());
		this.setCurrentSpeed(0);
		Driver d = car.getDriver();
		this.setDriver(new Driver(d.getPrefSpeed(), d.getRangeOfView(), d.getSafetyGap()));
	}
}
