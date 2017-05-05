package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JLabel;

import onlab.Driver.Status;
import onlab.Util.Direction;

public class HighWay {
	private float pixelLenght = 0;
	private float kmLenght = 5;
	private static ArrayList<RoadObject> roadObjects = new ArrayList<RoadObject>();
	public static Road road;
	private static ArrayList<Car> driverProfiles = new ArrayList<Car>();
	private static ArrayList<Float> driverProfilesPercentages = new ArrayList<Float>();
	public static ArrayList<Integer> driverProfilesCount = new ArrayList<>();
	private float timeWarp = 100;
	
	HighWay(float lenght) {
		this.pixelLenght = lenght;
	}

	public HighWay(Road road, float tl) {
		this.roadObjects = new ArrayList<RoadObject>();		
		RoadObject.count = 0;
		this.road = road;
		setTimeWarp(tl);
		setLenght((float) (road.getRectPartSize()*2 + road.getOvalPartSize()*Math.PI/2));
		setDriverProfilesCount();
	}

	public float getLenght() {
		return pixelLenght;
	}
	public void setLenght(float lenght) {
		this.pixelLenght = lenght;
	}

	public static ArrayList<RoadObject> getRoadObjects() {
		return roadObjects;
	}
	
	public void setRoadObjects(ArrayList<RoadObject> roadObjects) {
		this.roadObjects = roadObjects;
	}

	public float getTimeWarp() {
		return timeWarp;
	}

	public void setTimeWarp(float timeWarp) {
		this.timeWarp = timeWarp;
	}
	
	public int getLaneCount(){
		return road.getLaneCount();
	}

	/* 
	 * WINDOW
	 */
	
	public void deleteObject(RoadObject roadObject) {
		if(roadObject instanceof Car){
			decrementCount(getDriverProfileIndex(((Car)roadObject).getName()));
		}
		this.getRoadObjects().remove(roadObject);
		
	}
	
	public void addBlock(Point2D.Float click, int lane, float duration) {
		Block newBlock = new Block(click, lane, duration);
		getRoadObjects().add(newBlock);		
	}

	public void modifyCar(Car selected) {
		for (RoadObject roadObject : getRoadObjects()) {
			if (roadObject.id == selected.id) {
				((Car) roadObject).setDriverProfile(selected);
				break;
			}
		}
	}

	public void modifyBlock(int id, float duration) {
		for (RoadObject roadObject : getRoadObjects()) {
			if (roadObject.id == id) {
				((Block) roadObject).duration = duration;
				break;
			}
		}
	}
	
	/* 
	 * LOG
	 */
	
	public String getType(int i){
		String type = null;
		for (RoadObject roadObject : getRoadObjects()) {
			if (i == (roadObject.id)) {
				if (roadObject instanceof Car) {
					type = "Car";
				} else if (roadObject instanceof Block) {
					type = "Block";
				}
			}
		}
		return type;
	}
	public String getStatus(int i) {
		String status = null;
		for (RoadObject roadObject : getRoadObjects()) {
			if (i == (roadObject.id)) {
				if (roadObject instanceof Car) {
					Car thisCar = (Car) roadObject;
					status = thisCar.getDriver().s.toString();
				} else if (roadObject instanceof Block) {
					Block thisBlock = (Block) roadObject;
					status = Float.toString(thisBlock.duration);
				}
				break;
			}
		}
		return status;
	}
	public String getSpeed(int i){
		String speed = null;
		for (RoadObject roadObject : getRoadObjects()) {
			if (i == (roadObject.id)) {
				if (roadObject instanceof Car) {
					Car thisCar = (Car) roadObject;
					speed = Float.toString(thisCar.getCurrentSpeed());
				}else{
					System.out.println("Blocks don't have speed!");
				}
				break;
			}
		}
		return speed;
	}

	public void newCar() {
		int profileIndex = getBiggestDifference();		
		int index = getRoadObjects().size();
		int laneIndex = (index % road.getLaneCount()) + 1;
		Point2D.Float startPosition = getStartPosition(laneIndex);
		if(getObject(startPosition) == null){
			getRoadObjects().add(new Car(getStartPosition(laneIndex), getDriverProfiles().get(profileIndex), laneIndex));
			incrementCount(profileIndex);			
		}
	}
	
	public RoadObject getObject(Point2D.Float newPoint){
		for (RoadObject roadObject : roadObjects) {
			if(Util.getDistance(newPoint, roadObject.getPosition()) < roadObject.getSize()){
				return roadObject;
			}
		}	
		return null;
	}

	/* 
	 * MOVE
	 */	
	
	@SuppressWarnings("unchecked")
	public void move() {
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		ArrayList<RoadObject> deletables = new ArrayList<RoadObject>();
		for (RoadObject roadObject : ros) {
			if(isOut(roadObject.getPosition())){
				deletables.add(roadObject);
			} else if (roadObject instanceof Car) {
				Car thisCar = (Car) roadObject;
				thisCar.drive();				
				newCarPosition(thisCar);
				System.out.println("ID: " + thisCar.id + " " + thisCar.getDriver().getStatus().toString());
			} else if (roadObject instanceof Block) {
				Block thisBlock = (Block) roadObject;
				if (thisBlock.tick()) {
					deletables.add(roadObject);
				}
			}
		}
		deleteObjects(deletables);
	}
	
	private boolean isOut(Point2D.Float position) {
		if(position.x > road.getStartPosition().x){
			return true;
		}
		return false;
	}

	static public RoadObject getSightForward(Car asker, int lane, boolean sameLane) {
		RoadObject result = null;
		if(isROInOvalPart(asker.getPosition())){
			result = getSightInOvalPart(asker, lane, Util.Direction.FORWARD, sameLane);			
		} else {
			result = getSightInRectPart(asker, lane, Util.Direction.FORWARD, sameLane);
		}
		return result;
	}
	
	public static Point2D.Float getBorderPoint(Point2D.Float position, int lane, Direction dir) {
		Point2D.Float north = new Point2D.Float(road.getBorderPoint(), road.getBorderSize() + (lane-1)*road.getLaneSize() + road.getLaneSize()/2);
		Point2D.Float south = new Point2D.Float(road.getBorderPoint(), road.getBorderSize() + road.getOvalPartSize() - (lane-1)*road.getLaneSize() - road.getLaneSize()/2);
		if(position == north || position == south){
			return null;
		}else if((isROInOvalPart(position) && dir == Util.Direction.BACKWARD)
				|| (!isROInOvalPart(position) && isROInUpperRectPart(position) && dir == Util.Direction.FORWARD)){
			return north;
		} else if((isROInOvalPart(position) && dir == Util.Direction.FORWARD)
				|| (!isROInOvalPart(position) && !isROInUpperRectPart(position) && dir == Util.Direction.BACKWARD)){
			return south;
		} else {
			return null;
		}
	}
	

	static public RoadObject getSightBackward(Car asker, int lane) {
		RoadObject result = null;
		if(isROInOvalPart(asker.getPosition())){
			result = getSightInOvalPart(asker, lane, Util.Direction.BACKWARD, false);
		} else {
			result = getSightInRectPart(asker, lane, Util.Direction.BACKWARD, false);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static RoadObject getSightInOvalPart(Car asker, int lane, Util.Direction dir, boolean sameLane) {
		RoadObject result = null;
		float minAngle = 360;
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		for (RoadObject roadObject : ros) {		
			if((sameLane && isChangingHere(roadObject, lane)) || roadObject.getLane() == lane){
				float angle = (getAngle(roadObject.getPosition(), asker.getLane()) + 360 - getAngle(asker.getPosition(), asker.getLane())) % 360;
				if((angle < 180 && dir == Util.Direction.FORWARD) 
						|| (angle > 180 && dir == Util.Direction.BACKWARD)){
					if (angle < minAngle && asker != roadObject) {
						minAngle = angle;
						result = roadObject;
					}
				}
			}
		}
		if (result != null && getDistanceFromAngle(minAngle) >= asker.getRange()) {
			result = null;
		}
		
		if(result == null){
			result = getFurtherSight(asker, lane, dir, sameLane);
		}
		
		return result;
	}
	
	private static RoadObject getFurtherSight(Car asker, int lane, Util.Direction dir, boolean sameLane){
		RoadObject result = null;
		Point2D.Float bp = getBorderPoint(asker.getPosition(), lane, dir);
		if(bp != null){
			float remainder = asker.getRange() - Util.getDistance(asker.getPosition(), bp);
			if(remainder > 0 && remainder < asker.getRange()){
				Car temp = asker.clone();
				temp.setPosition(bp);
				temp.setRange(remainder);
				if(isROInOvalPart(asker.getPosition())){
					result = getSightInRectPart(temp, lane, dir, sameLane);
				} else {
					result = getSightInOvalPart(temp, lane, dir, sameLane);
				}
			}
		}
		return result;
	}

	private static float getDistanceFromAngle(float minAngle) {
		float ovalPart = (float) (road.getOvalPartSize()*Math.PI/2);
		return (minAngle * ovalPart / 180);
	}

	@SuppressWarnings("unchecked")
	private static RoadObject getSightInRectPart(Car asker, int lane, Util.Direction dir, boolean sameLane) {
		RoadObject result = null;
		float minDistance = asker.getRange();
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		for (RoadObject roadObject : ros) {
			if(asker != roadObject){
				if(isROInUpperRectPart(asker.getPosition()) && isROInUpperRectPart(roadObject.getPosition())) {
					if((isROOnLeftSide(asker, roadObject) && dir == Util.Direction.FORWARD) 
						|| (!isROOnLeftSide(asker, roadObject) && dir == Util.Direction.BACKWARD)){	
						if((sameLane && isChangingHere(roadObject, lane)) || roadObject.getLane() == lane){
							float distance = Util.getDistance(asker.getPosition(), roadObject.getPosition());
							if(distance < minDistance){
								minDistance = distance;
								result = roadObject;
							}	
						}
					}
				} else if(!isROInUpperRectPart(asker.getPosition()) && !isROInUpperRectPart(roadObject.getPosition())){
					if((!isROOnLeftSide(asker, roadObject) && dir == Util.Direction.FORWARD) 
						|| (isROOnLeftSide(asker, roadObject) && dir == Util.Direction.BACKWARD)){	
						if((sameLane && isChangingHere(roadObject, lane)) || roadObject.getLane() == lane){
							float distance = Util.getDistance(asker.getPosition(), roadObject.getPosition());
							if(distance < minDistance){
								minDistance = distance;
								result = roadObject;
							}
						}
					}
				}	
			}
		}
		if(result == null){
			result = getFurtherSight(asker, lane, dir, sameLane);
		}
		return result;
	}
	
	private static boolean isChangingHere(RoadObject roadObject, int lane) {
		if(roadObject instanceof Car){
			Car car = (Car) roadObject;
			if(car.getDriver().getStatus() == Status.CHANGELEFT && car.getLane() == (lane-1)){
				return true;
			} else if(car.getDriver().getStatus() == Status.CHANGERIGHT && car.getLane() == (lane+1)){
				return true;
			}
		}
		return false;
	}

	private void newCarPosition(Car thisCar) {
		Point2D.Float newPosition;
		Status status = thisCar.getDriver().getStatus();
		if(status == Status.CHANGELEFT || status == Status.CHANGERIGHT  ){
			if(isChangeComplete(thisCar)){
				int lane = thisCar.getLane();
				if(thisCar.getDriver().getStatus() == Status.CHANGELEFT ){
					thisCar.setLane(++lane);
				} else if(thisCar.getDriver().getStatus() == Status.CHANGERIGHT ){
					thisCar.setLane(--lane);
				}
				thisCar.getDriver().setStatus(Status.DRIVING);
			}
		}		
		if(isROInOvalPart(thisCar.getPosition())){
			newPosition = ovalPosition(thisCar.getPosition(), thisCar.getAdvance(timeWarp), thisCar.getLane(), status);
		} else {
			newPosition = rectPosition(thisCar.getPosition(), thisCar.getAdvance(timeWarp), thisCar.getLane(), status);
		}		
		thisCar.setPosition(newPosition);
	}

	private boolean isChangeComplete(Car thisCar) {
		int lane = thisCar.getLane();
		if(thisCar.getDriver().getStatus() == Status.CHANGELEFT ){
			lane++;
		} else if(thisCar.getDriver().getStatus() == Status.CHANGERIGHT ){
			lane--;
		}
		if(thisCar.getPosition().x < road.getBorderPoint()){
			if(Util.getDistance(getCirclePoint(thisCar.getPosition(), lane), thisCar.getPosition()) < kmToPixel(thisCar.getAdvance(timeWarp))){
				return true;
			}
		} else {
			if(Math.abs(thisCar.getPosition().y - getRectY(thisCar.getPosition(), lane)) < getYChange(kmToPixel(thisCar.getAdvance(timeWarp)))){
				return true;
			}
		}
		return false;
	}

	private Point2D.Float ovalPosition(Point2D.Float position, Float advance, int lane, Status status){	
		float angle = getAngle(position, lane);
		float plusAngle = getAngleFromAdvance(kmToPixel(advance), lane);
		if(status == Status.CHANGELEFT || status == Status.CHANGERIGHT ){
			//TODO
			float pixelAdvance = kmToPixel(advance);
			plusAngle = (float) (plusAngle * (pixelAdvance/getXChange(pixelAdvance)));
		}
		angle += plusAngle;
		angle%=360;
		if(status == Status.CHANGELEFT ){
			return getChangePos(position, angle, lane, (-1) * getYChange(kmToPixel(advance)));
		} else if(status == Status.CHANGERIGHT ){
			return getChangePos(position, angle, lane, getYChange(kmToPixel(advance)));			
		} else {
			return getPos(angle, lane);
		}
	}

	
	private Point2D.Float rectPosition(Point2D.Float position, float advance, int lane, Status status){
		Point2D.Float newPosition = null;
		float xChange = kmToPixel(advance);
		float yChange = 0;
		if(status == Status.CHANGELEFT ){
			xChange = getXChange(kmToPixel(advance));
			yChange = (-1) * getYChange(kmToPixel(advance));
		} else if(status == Status.CHANGERIGHT ){
			xChange = getXChange(kmToPixel(advance));
			yChange = getYChange(kmToPixel(advance));
		} 
		
		if(isROInUpperRectPart(position)){
			xChange = xChange * -1;
			yChange = yChange * -1;
		}
		if(status == Status.CHANGELEFT ||status == Status.CHANGERIGHT){
			newPosition = new Point2D.Float(position.x + xChange, position.y + yChange);
		} else {
			newPosition = new Point2D.Float(position.x + xChange, getRectY(position, lane));
		}
		return newPosition;
	}

	
	//---------//

	private static float getAngle(Point2D.Float position, int lane) {
		Point2D.Float circlePosition = circleCenteredCoords(getCirclePoint(position));
		float angle = (float) (Math.atan2(circlePosition.y, circlePosition.x));
		angle = Util.toAngle(angle);
		angle = Util.normalAngle(angle);
		return angle;
	}

	private Point2D.Float getPos(float angle, int lane) {
		float radian = Util.toRadian(angle);
		Point2D.Float pos = new Point2D.Float();
		float diameter = getDiameter(lane);
		pos.x = (float) ((Math.cos(radian) * diameter));
		pos.y = (float) ((Math.sin(radian) * diameter));
		return getCirclePoint(normalCoords(pos), lane);		
	}
	
	private Point2D.Float getChangePos(Point2D.Float position, float angle, int lane, float yChange) {
		float diameter = Util.getDistance(position, road.getCircleCenter()) + yChange;
		float radian = Util.toRadian(angle);
		Point2D.Float pos = new Point2D.Float();
		pos.x = (float) (Math.cos(radian) * diameter);
		pos.y = (float) (Math.sin(radian) * diameter);
		return normalCoords(pos);
		
	}	

	private static Point2D.Float circleCenteredCoords(Point2D.Float position) {
		return new Point2D.Float(position.x - road.getBorderSize() - road.getOvalPartSize()/2,
				(road.getOvalPartSize()/2) - position.y + road.getBorderSize());
	}
	
	private Point2D.Float normalCoords(Point2D.Float circlePosition) {
		return new Point2D.Float(circlePosition.x + road.getOvalPartSize()/2 + road.getBorderSize(), 
				road.getOvalPartSize()/2 - circlePosition.y + road.getBorderSize());
	}
	
	private static Point2D.Float getCirclePoint(Point2D.Float newPoint) {
		float r = road.getOvalPartSize()/2;
		float x = road.getCircleCenter().x + r*(newPoint.x-road.getCircleCenter().x)/Util.getDistance(newPoint,road.getCircleCenter());
		float y = road.getCircleCenter().y + r*(newPoint.y-road.getCircleCenter().y)/Util.getDistance(newPoint,road.getCircleCenter());
		
		Point2D.Float circlePoint = new Point2D.Float(x, y);		
		return circlePoint;
	}
	
	private Point2D.Float getCirclePoint(Point2D.Float newPoint, int lane) {
		float r = getDiameter(lane)/2;
		float x = road.getCircleCenter().x + r*(newPoint.x-road.getCircleCenter().x)/Util.getDistance(newPoint,road.getCircleCenter());
		float y = road.getCircleCenter().y + r*(newPoint.y-road.getCircleCenter().y)/Util.getDistance(newPoint,road.getCircleCenter());
		
		Point2D.Float circlePoint = new Point2D.Float(x, y);		
		return circlePoint;
	}
	
	private Point2D.Float getCirclePoint(Point2D.Float newPoint, float r) {
		float x = road.getCircleCenter().x + r*(newPoint.x-road.getCircleCenter().x)/Util.getDistance(newPoint,road.getCircleCenter());
		float y = road.getCircleCenter().y + r*(newPoint.y-road.getCircleCenter().y)/Util.getDistance(newPoint,road.getCircleCenter());	
		Point2D.Float circlePoint = new Point2D.Float(x, y);		
		return circlePoint;
	}

	private float getDiameter(int lane) {
		return road.getOvalPartSize() - (lane-1+0.5f)*road.getLaneSize()*2;
	}
	
	private float getAngleFromAdvance(Float advance, int lane) {
		return advance / (getOvalPartRoadLength(lane)*2) * 360;
	}
	
	private float getOvalPartRoadLength(int lane){
		float d = getDiameter(lane);
		return (float) (d*Math.PI)/2;
	}

	//------------//
	
	private static boolean isROInUpperRectPart(Point2D.Float position) {
		if(position.y < (road.getOvalPartSize()/2 + road.getBorderSize())){
			return true;
		}
		return false;
	}	
	
	private static boolean isROInOvalPart(Point2D.Float position) {
		if(position.x  < road.getBorderPoint()){
			return true;
		}
		return false;
	}
	
	private static boolean isROOnLeftSide(Car asker, RoadObject roadObject) {
		if(asker.getPosition().x > roadObject.getPosition().x){
			return true;
		}
		return false;
	}

	
	private float getRectY(Point2D.Float position, int lane) {
		float result = 0;
		if(isROInUpperRectPart(position)){
			result = (road.getBorderSize() + (lane-1)*road.getLaneSize() + road.getLaneSize()/2);
		} else {
			result = (road.getBorderSize() + road.getOvalPartSize() - (lane-1)*road.getLaneSize() - road.getLaneSize()/2);
		}
		return result;
	}
	
	private float kmToPixel(float advance){
		return pixelLenght/kmLenght * advance;
	}
	
	private void deleteObjects(ArrayList<RoadObject> roadObjects) {
		for (RoadObject roadObject : roadObjects) {
			if(roadObject instanceof Car){
				decrementCount(getDriverProfileIndex(((Car)roadObject).getName()));
			}
			this.getRoadObjects().remove(roadObject);
		}
	}
	
	private float getXChange(float kmToPixel) {
		return kmToPixel*(float) (Math.sin(Util.toRadian(60)));
	}
	
	private float getYChange(float kmToPixel) {
		return kmToPixel*(float) (Math.cos(Util.toRadian(60)));
	}
	
	private Point2D.Float getStartPosition(int laneIndex) {
		Float y = road.getStartPosition().y + (laneIndex-1)*road.getLaneSize();
		Point2D.Float result = new Point2D.Float(road.getStartPosition().x, y);
		return result;
	}

	public static boolean isInnerMostLane(int lane) {
		if(road.getLaneCount() == lane){
			return true;
		}
		return false;
	}

	public static boolean isOutterMostLane(int lane) {
		if(lane == 1){
			return true;
		}
		return false;
	}

	public static boolean isLaneToTheSide(Util.Direction dir, int lane) {
		if(dir == Util.Direction.LEFT && !isInnerMostLane(lane)){
			return true;
		} else if(dir == Util.Direction.RIGHT && !isOutterMostLane(lane)){
			return true;
		}
		return false;
	}

	public static String[] getDriverProfileNames() {
		if(!getDriverProfiles().isEmpty()){
			String[] names = new String[getDriverProfiles().size()];
			for (int i = 0; i < names.length; i++) {
				names[i] = getDriverProfiles().get(i).getName();
			}				
			return names;
		} else {
			String[] s = {"none"};
			return s;
		}
	}

	public static Car getDriverProfile(String profile) {
		for(Car dp : getDriverProfiles()){
			if(profile.equals(dp.getName())){
				return dp;
			}
		}
		return null;
	}

	public void addDriverProfiles(ArrayList<Car> loadDefaultDriverProfiles) {
		setDriverProfiles(loadDefaultDriverProfiles);	
		setDriverProfilesPercentages();		
		setDriverProfilesCount();
	}

	public void addDriverProfile(Car c){
		addDriverProfile(c.getName(), c.getMaxAcc(), c.getMaxAcc(), c.getColor(), 
				c.getDriver().getPrefSpeed(), c.getDriver().getRangeOfView(), c.getDriver().getSafetyGap());
	}
	
	public void addDriverProfile(String name, Float maxSpeed, Float maxAcc, Color color, 
			Float prefSpeed, Float range, Float safetyGap) {
		Car c = new Car();
		c.setName(name);
		c.setMaxSpeed(maxSpeed);
		c.setMaxAcc(maxAcc);
		c.setColor(color);
		Driver d = new Driver(prefSpeed, range, safetyGap);
		c.setDriver(d);
		getDriverProfiles().add(c);
		setDriverProfilesPercentages();
		addDriverProfilesCount();
	}

	private void addDriverProfilesCount() {
		driverProfilesCount.add(0);		
	}

	public static ArrayList<Float> getDriverProfilesPercentages() {
		return driverProfilesPercentages;
	}

	public static void setDriverProfilesPercentages(ArrayList<Float> driverProfilesPercentages) {
		HighWay.driverProfilesPercentages = driverProfilesPercentages;
	}
	
	public static void setDriverProfilesPercentages(){
		driverProfilesPercentages = new ArrayList<>();
		for (Car car : getDriverProfiles()) {
			driverProfilesPercentages.add((1f/getDriverProfiles().size())*100);
		}
	}

	public static void setDriverProfilesPercentage(int id, Float p) {	
		float change = (driverProfilesPercentages.get(id) - p) / (driverProfilesPercentages.size()-1);
				
		ArrayList<Float> temp = new ArrayList<>();
		for (int i = 0; i < driverProfilesPercentages.size(); i++) {
			if(i==id){
				temp.add(p);
			} else {
				temp.add(driverProfilesPercentages.get(i) + change);
			}
		}
		
		setDriverProfilesPercentages(temp);
		
		float teszt = 0;
		for (Float float1 : temp) {
			teszt = teszt + float1;
		}
		System.out.println("TESZT: " + teszt);
	}
	
	public static Integer[] getDriverProfilePercentages() {
		if(!driverProfilesPercentages.isEmpty()){
			Integer[] percentages = new Integer[driverProfilesPercentages.size()];
			for (int i = 0; i < percentages.length; i++) {
				percentages[i] = (int) driverProfilesPercentages.get(i).shortValue();
			}				
			return percentages;
		} else {
			Integer[] p = {0};
			return p;
		}
	}

	public static Color[] getDriverProfileColors() {
		if(!getDriverProfiles().isEmpty()){
			Color[] colors = new Color[getDriverProfiles().size()];
			for (int i = 0; i < colors.length; i++) {
				colors[i] = getDriverProfiles().get(i).getColor();
			}				
			return colors;
		} else {
			Color[] s = {Color.WHITE};
			return s;
		}
	}

	public static ArrayList<Car> getDriverProfiles() {
		return driverProfiles;
	}

	public static void setDriverProfiles(ArrayList<Car> driverProfiles) {
		HighWay.driverProfiles = new ArrayList<>();
		if(driverProfiles != null){
			HighWay.driverProfiles=driverProfiles;
		}
		setDriverProfilesPercentages();
		setDriverProfilesCount();
	}

	private int getCarCount(){
		int result = 0;
		for (RoadObject car : roadObjects) {
			if(car instanceof Car){
				result++;
			}
		}
		return result;
	}

	public ArrayList<Integer> getDriverProfilesCount() {
		return driverProfilesCount;
	}

	public static void setDriverProfilesCount(ArrayList<Integer> driverProfilesCount) {
		HighWay.driverProfilesCount = driverProfilesCount;
	}
	
	public static void setDriverProfilesCount() {
		driverProfilesCount = new ArrayList<>();
		Integer[] count = new Integer[driverProfiles.size()];
		for (RoadObject ro : roadObjects) {
			if(ro instanceof Car){
				int index = getDriverProfileIndex(((Car)ro).getName());
				count[index] = count[index]+1;
			}
		}
		for (int i = 0; i < count.length; i++) {
			if(count[i] == null){
				driverProfilesCount.add(0);
			} else {
				driverProfilesCount.add(count[i]);
			}
		}		
	}

	private int getBiggestDifference() {
		Float[] difference = getPercentageDifferences();
		int result = 0;
		Float biggest = 0f;
		for (int i = 0; i < difference.length; i++) {
			if(difference[i] > biggest){
				result = i;
				biggest = difference[i]; 
			}
		}
		return result;
	}

	private Float[] getPercentageDifferences() {
		Float[] percentages = getPercentages();	
		Float[] differences = new Float[driverProfiles.size()];
		for (int i = 0; i < percentages.length; i++) {
			differences[i] = driverProfilesPercentages.get(i) - percentages[i];
		}		
		return differences;
	}


	private Float[] getPercentages() {
		Float[] percentages = new Float[driverProfiles.size()];
		int all = getCarCount();
		for (int i = 0; i < driverProfiles.size(); i++) {
			int j = driverProfilesCount.get(i);
			Float f = (float)j;
			percentages[i] = (Float) f / (float)all * 100f;
			System.out.println(driverProfiles.get(i).getName() + ": " + percentages[i]);
		}
		return percentages;
	}	
	
	public static int getDriverProfileIndex(String profile){
		Car dp = getDriverProfile(profile);
		return driverProfiles.indexOf(dp);
	}

	public static void incrementCount(int profileIndex) {
		ArrayList<Integer> driverProfilesCount2 = new ArrayList<>();
		for (int i = 0; i < driverProfilesCount.size(); i++) {			
			if(i == profileIndex){
				driverProfilesCount2.add(driverProfilesCount.get(i)+1); 				
			} else{
				driverProfilesCount2.add(driverProfilesCount.get(i));
			}
		}
		setDriverProfilesCount(driverProfilesCount2);
	}
	
	public static void decrementCount(int profileIndex) {
		ArrayList<Integer> driverProfilesCount2 = new ArrayList<>();
		for (int i = 0; i < driverProfilesCount.size(); i++) {			
			if(i == profileIndex){
				driverProfilesCount2.add(driverProfilesCount.get(i)-1); 				
			} else{
				driverProfilesCount2.add(driverProfilesCount.get(i));
			}
		}
		setDriverProfilesCount(driverProfilesCount2);
	}

	public static boolean isFarther(RoadObject forward, RoadObject inSight, Car thisCar) {
		if(inSight == null){
			return false;
		}
		int forwardLane = forward.getLane();
		int inSightLane = inSight.getLane();
		
		float distanceForward = Util.getDistance(forward.getPosition(), thisCar.getPosition());
		float distanceInSight = Util.getDistance(inSight.getPosition(), thisCar.getPosition());
		
		float laneSize = Math.abs(forwardLane - inSightLane) * road.getLaneSize();
		
		if(distanceForward > Util.pitagoras(distanceInSight, laneSize)){
			return true;
		} else {
			return false;
		}
		
	}
}
