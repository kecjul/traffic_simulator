package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import onlab.Driver.Status;

public class HighWay {
	private float pixelLenght = 0;
	private float kmLenght = 5;
	private static ArrayList<RoadObject> roadObjects = new ArrayList<RoadObject>();
	private static Road road;
	private float timeLapse = 100;
	
	HighWay(float lenght) {
		this.pixelLenght = lenght;
	}

	public HighWay(Road road, float tl) {
		this.road = road;
		setTimeLapse(tl);
		setLenght((float) (road.getRectPartSize()*2 + road.getOvalPartSize()*Math.PI/2));
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

	public float getTimeLapse() {
		return timeLapse;
	}

	public void setTimeLapse(float timeLapse) {
		this.timeLapse = timeLapse;
	}

	/* 
	 * WINDOW
	 */
	
	public void deleteObject(RoadObject roadObject) {
		this.getRoadObjects().remove(roadObject);
	}
	
	public void addBlock(Point2D.Float click, int lane, float duration) {
		Block newBlock = new Block(click, lane, duration);
		getRoadObjects().add(newBlock);
	}

	public void modifyCar(int id, float maxSpeed, float maxAcc, Color color,
			float prefSpeed, float range, float safety) {
		for (RoadObject roadObject : getRoadObjects()) {
			if (roadObject.id == id) {
				((Car) roadObject).setMaxSpeed(maxSpeed);
				((Car) roadObject).setMaxAcc(maxAcc);
				((Car) roadObject).setColor(color);
				((Car) roadObject).getDriver().setPrefSpeed(prefSpeed);
				((Car) roadObject).getDriver().setRangeOfView(range);
				((Car) roadObject).getDriver().setSafetyGap(safety);
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
		if(getRoadObjects().size() == 1){
			Car c = (Car) getRoadObjects().get(0);
			c.getDriver().setPrefSpeed(5);
			getRoadObjects().add(new Car(road.getStartPosition()));
		} else{
			getRoadObjects().add(new Car(road.getStartPosition()));
		}
	}

	/* 
	 * MOVE
	 */	
	
	@SuppressWarnings("unchecked")
	public void move() {
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		ArrayList<RoadObject> deletables = new ArrayList<RoadObject>();
		for (RoadObject roadObject : ros) {
			if (roadObject instanceof Car) {
				Car thisCar = (Car) roadObject;
				thisCar.drive();				
				newCarPosition(thisCar);
			} else if (roadObject instanceof Block) {
				Block thisBlock = (Block) roadObject;
				if (thisBlock.tick()) {
					deletables.add(roadObject);
				}
			}
		}
		deleteObjects(deletables);
	}
	
	static public RoadObject getSightForward(Car asker, int lane) {
		//TODO
		RoadObject result = null;
		if(asker.getPosition().x < road.getOvalPartSize()/2 + road.getBorderSize()){
			result = getSightInOvalPart(asker, lane, Util.Direction.FORWARD);
		} else {
			result = getSightInRectPart(asker, lane, Util.Direction.FORWARD);
		}
		return result;
	}
	

	static public RoadObject getSightBackward(Car asker, int lane) {
		//TODO
		RoadObject result = null;
		if(asker.getPosition().x < road.getOvalPartSize()/2 + road.getBorderSize()){
			result = getSightInOvalPart(asker, lane, Util.Direction.BACKWARD);
		} else {
			result = getSightInRectPart(asker, lane, Util.Direction.BACKWARD);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static RoadObject getSightInOvalPart(Car asker, int lane, Util.Direction dir) {
		RoadObject result = null;
		float minAngle = 360;
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		for (RoadObject roadObject : ros) {			
			float angle = (getAngle(roadObject.getPosition(), asker.getLane()) + 360 - getAngle(asker.getPosition(), asker.getLane())) % 360;
			if (angle < minAngle && asker != roadObject  && roadObject.getLane() == lane) {
				minAngle = angle;
				result = roadObject;
			}
		}
		if (result != null && getDistanceFromAngle(minAngle) >= asker.getRange()) {
			result = null;
		}
		return result;
	}

	private static float getDistanceFromAngle(float minAngle) {
		float ovalPart = (float) (road.getOvalPartSize()*Math.PI/2);
		return (minAngle * ovalPart / 180);
	}

	@SuppressWarnings("unchecked")
	private static RoadObject getSightInRectPart(Car asker, int lane, Util.Direction dir) {
		RoadObject result = null;
		float minDistance = road.getRectPartSize();
		ArrayList<RoadObject> ros = (ArrayList<RoadObject>) getRoadObjects().clone();
		for (RoadObject roadObject : ros) {
			if(asker != roadObject){
				if(isROInUpperRectPart(asker) && isROInUpperRectPart(roadObject) 
						&& isROOnLeftSide(asker, roadObject)){				
					float distance = Util.getDistance(asker.getPosition(), roadObject.getPosition());
					if(distance < minDistance && roadObject.getLane() == lane){
						minDistance = distance;
						result = roadObject;
					}				
				} else if(!isROInUpperRectPart(asker) && !isROInUpperRectPart(roadObject)
						&& !isROOnLeftSide(asker, roadObject)){
					float distance = Util.getDistance(asker.getPosition(), roadObject.getPosition());
					if(distance < minDistance && roadObject.getLane() == lane){
						minDistance = distance;
						result = roadObject;
					}
				}	
			}
		} 		
		return result;
	}
	
	private void newCarPosition(Car thisCar) {
		Point2D.Float newPosition;
		Status status = thisCar.getDriver().getStatus();
		if(status == Status.CHANGELEFT || status == Status.CHANGERIGHT  ){
			System.out.println("ID " + thisCar.id);
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
		if(thisCar.getPosition().x < road.getBorderPoint()){
			newPosition = ovalPosition(thisCar.getPosition(), thisCar.getAdvance(timeLapse), thisCar.getLane(), status);
		} else {
			newPosition = rectPosition(thisCar.getPosition(), thisCar.getAdvance(timeLapse), thisCar.getLane(), status);
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
			if(Util.getDistance(getCirclePoint(thisCar.getPosition(), lane), thisCar.getPosition()) < kmToPixel(thisCar.getAdvance(timeLapse))){
				return true;
			}
		} else {
			System.out.println("ychange " + getYChange(kmToPixel(thisCar.getAdvance(timeLapse))));
			System.out.println("needed " + Math.abs(thisCar.getPosition().y - getRectY(thisCar.getPosition(), lane)));
			if(Math.abs(thisCar.getPosition().y - getRectY(thisCar.getPosition(), lane)) < getYChange(kmToPixel(thisCar.getAdvance(timeLapse)))){
				return true;
			}
		}
		return false;
	}

	private Point2D.Float ovalPosition(Point2D.Float position, Float advance, int lane, Status status){	
		float angle = getAngle(position, lane);
		float plusAngle = getAngleFromAdvance(kmToPixel(advance), lane);
		if(status == Status.CHANGELEFT ){
			plusAngle = (float) (plusAngle * (kmToPixel(advance)/Math.sqrt(Util.square(kmToPixel(advance))+Util.square(road.getLaneSize()))));
		}
		angle += plusAngle;
		angle%=360;
		if(status == Status.CHANGELEFT ){
//			angle = (float) (angle * (Math.sqrt(Util.square(kmToPixel(advance))+Util.square(road.getLaneSize()))/kmToPixel(advance)));
			return getChangePos(position, angle, lane, (-1) * getYChange(kmToPixel(advance)));
		} else if(status == Status.CHANGERIGHT ){
			angle = (float) (angle / (Math.sqrt(Util.square(kmToPixel(advance)+Util.square(road.getLaneSize())))));
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
		
		if(isPosInUpperRectPart(position)){
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
		pos.x = (float) ((Math.cos(radian) * road.getOvalPartSize()/2));
		pos.y = (float) ((Math.sin(radian) * road.getOvalPartSize()/2));
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
				(road.getOvalPartSize()/2) - (position.y - road.getBorderSize()));
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
	
	private static boolean isROInUpperRectPart(RoadObject asker) {
		if(asker.getPosition().y < (road.getOvalPartSize()/2 + road.getBorderSize())){
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

	private boolean isPosInUpperRectPart(Point2D.Float position) {
		if(position.y < (road.getOvalPartSize()/2 + road.getBorderSize())){
			return true;
		}
		return false;
	}
	
	private float getRectY(Point2D.Float position, int lane) {
		float result = 0;
		if(isPosInUpperRectPart(position)){
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
			this.getRoadObjects().remove(roadObject);
		}
	}
	
	private float getXChange(float kmToPixel) {
		return kmToPixel*(float) (Math.sin(Util.toRadian(45)));
	}
	
	private float getYChange(float kmToPixel) {
		return kmToPixel*(float) (Math.cos(Util.toRadian(45)));
	}

	public static boolean isInnerMostLane(int lane) {
		if(road.getLaneCount() == lane){
			return true;
		}
		return false;
	}

	public static boolean isLaneToTheSide(Util.Direction dir, int lane) {
		if(dir == Util.Direction.LEFT && !isInnerMostLane(lane)){
			return true;
		} else if(dir == Util.Direction.RIGHT && lane != 1){
			return true;
		}
		return false;
	}
}
