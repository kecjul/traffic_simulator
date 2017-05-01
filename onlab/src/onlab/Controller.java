package onlab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Controller{

	public static void main(String[] args) {
		Controller c = new Controller();
		while(true){			
			c.trafficLoop();
			try {
				Thread.sleep(0, 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	Window w; 
	HighWay hw; 
	private float deltaTime;
	private float tickTime = (float) 10.0; 
	private float prevTime;
	
	private float deltaCarTime;
	private float newCarTime = 500.0f;
	
	private float timeWarp = 20;

	enum Status{RUNNING, PAUSED};
	Status s = Status.RUNNING;
	Controller(){
		this.w = new Window(this);
		Road road = w.initUI();
		this.hw = new HighWay(road, getTimeWarp());
		this.hw.addDriverProfiles(LoadDefaultDriverProfiles());
		w.setProfilesPanel();
		w.setVisible(true);
		deltaTime = 0;
		prevTime = 0;
		deltaCarTime = 0;
	}
		
	void trafficLoop(){
		float delta = getDelta();
		deltaTime+=delta;
		deltaCarTime+=delta;
		
		//tickTime = 10 ms
		if(deltaTime>=tickTime){
			if(s == Status.RUNNING){
				if(deltaCarTime>=getNewCarTime()){
					hw.newCar();
					deltaCarTime = 0;
				}
				hw.move();
				w.setLog();
				if(hw.getRoadObjects() != null){
					String[] names = new String[RoadObject.count];
					Float[] datas = new Float[RoadObject.count];
					Color[] colors = new Color[RoadObject.count];
					for (int i = 0; i < hw.getRoadObjects().size(); i++) {
						if(hw.getRoadObjects().get(i) instanceof Car){
							Car c = (Car) hw.getRoadObjects().get(i);
							names[c.id-1] = "Car " + c.id;
							datas[c.id-1] = c.getCurrentSpeed();
							colors[c.id-1] = c.getColor();
						}
					}
//					w.newChartData(names, datas, colors);
				}
			}
			w.setRoadObjects(hw.getRoadObjects());
			w.reFresh();
			deltaTime=0;
		}
	}

	private float getTime() {
		long time = System.nanoTime() / 1000000;
		float time2 = time % 1000000;
		//return (float)(System.nanoTime() / 1000000);
		return time2;
	}

	private float getDelta() {
		float currentTime = getTime();
		float delta = (float) (currentTime - prevTime);
		prevTime = getTime();
		return delta;
	}
	
	public void stop(){
		if(s == Status.RUNNING){
			s = Status.PAUSED;
		}else{
			s = Status.RUNNING;
		}
	}

	public void SaveGame(File file){
		StringBuffer sb  = new StringBuffer();

		sb.append("newCarTimer: ").append(Float.toString(getNewCarTime())).append(System.lineSeparator());
		sb.append("timeWarp: ").append(Float.toString(hw.getTimeWarp())).append(System.lineSeparator());
		sb.append("lanes: ").append(hw.getLaneCount()).append(System.lineSeparator());
		if(hw.getDriverProfiles().size()>0){
			for (Car driverProfile : hw.getDriverProfiles()) {
				sb.append("DriverProfile").append(System.lineSeparator());
				sb.append("name: ").append(driverProfile.getName()).append(System.lineSeparator());
				sb.append("maxSpeed: ").append(Float.toString(driverProfile.getMaxSpeed())).append(System.lineSeparator());
				sb.append("maxAcc: ").append(Float.toString(driverProfile.getMaxAcc())).append(System.lineSeparator());
				sb.append("color: ").append(Integer.toString(driverProfile.getColor().getRed())).append(" ")
									.append(Integer.toString(driverProfile.getColor().getGreen())).append(" ")
									.append(Integer.toString(driverProfile.getColor().getBlue())).append(" ").append(System.lineSeparator());
				sb.append("prefSpeed: ").append(Float.toString(driverProfile.getDriver().getPrefSpeed())).append(System.lineSeparator());
				sb.append("rangeOfView: ").append(Float.toString(driverProfile.getDriver().getRangeOfView())).append(System.lineSeparator());
				sb.append("safetyGap: ").append(Float.toString(driverProfile.getDriver().getSafetyGap())).append(System.lineSeparator());
			}
		}
		if(hw.getRoadObjects().size()>0){
			for (RoadObject roadObject : hw.getRoadObjects()) {
				if(roadObject instanceof Car){
					Car c = (Car)roadObject;
					sb.append("Car ").append(c.id).append(System.lineSeparator());
					sb.append("profile: ").append(c.getName()).append(System.lineSeparator());
					sb.append("position: ").append(Double.toString(c.getPosition().getX())).append(" ").append(Double.toString(c.getPosition().getY())).append(System.lineSeparator());					
					sb.append("actualSpeed: ").append(Float.toString(c.getCurrentSpeed())).append(System.lineSeparator());					
					sb.append("lane: ").append(c.getLane()).append(System.lineSeparator());					
				}
				if(roadObject instanceof Block){
					Block b = (Block)roadObject;
					sb.append("Block ").append(b.id).append(System.lineSeparator());
					sb.append("position: ").append(Double.toString(b.getPosition().getX())).append(" ").append(Double.toString(b.getPosition().getY())).append(System.lineSeparator());
					sb.append("duration: ").append(Float.toString(b.duration)).append(System.lineSeparator());
					sb.append("currentTime: ").append(Float.toString(b.currentTime)).append(System.lineSeparator());					
					sb.append("lane: ").append(b.getLane()).append(System.lineSeparator());
				}
			}
			sb.append("ObjectCount: ").append(RoadObject.count).append(System.lineSeparator());
		}
		byte dataToWrite[] = String.valueOf(sb).getBytes();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(dataToWrite);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if(out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	public void LoadGame(File file){
		w.chartFrame.removeAll();
		w.chartFrame.setVisible(false);
		w.setCharts();
		BufferedReader br = null;
		String everything = null;
		try {
			br = new BufferedReader(new FileReader(file));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    everything = sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    try {
		    	if (br != null)
		    		br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(everything != null){
			String[] data = everything.split(System.lineSeparator());
			Car c = null;
			Block b = null;
			boolean isCar = false;
			for (String row : data) {
				String[] name = row.split(" ");
				if(name != null){
					switch (name[0]) {
					
					case "newCarTimer:":
						Float nct = Float.parseFloat(name[1]);
						w.tNewCarTimer.setText(Float.toString(nct));
						setNewCarTime(nct);
						break;
					case "timeWarp:":
						Float tw = Float.parseFloat(name[1]);
						w.tTimeWarp.setText(Float.toString(tw));
						setTimeWarp(tw);
						break;
					case "lanes:":
						int lanes = Integer.parseInt(name[1]);
						w.surface.setLaneCount(lanes);
						w.setLanes();
						hw.setDriverProfiles(null);
						break;
	
					case "ObjectCount:":
						RoadObject.count = Integer.parseInt(name[1]);
						break;
	
					case "DriverProfile":
						c = new Car();						
						break;
						
					case "name:":
						c.setName(name[1]);
						break;
						
					case "maxSpeed:":
						c.setMaxSpeed(Float.parseFloat(name[1]));
						break;
	
					case "maxAcc:":
						c.setMaxAcc(Float.parseFloat(name[1]));
						break;
	
					case "color:":
						Color col = new Color(Integer.parseInt(name[1]), Integer.parseInt(name[2]), Integer.parseInt(name[3]));
						c.setColor(col);
						break;
	
					case "prefSpeed:":
						c.getDriver().setPrefSpeed(Float.parseFloat(name[1]));
						break;
						
					case "rangeOfView:":
						c.getDriver().setRangeOfView(Float.parseFloat(name[1]));
						break;
						
					case "safetyGap:":
						c.getDriver().setSafetyGap(Float.parseFloat(name[1]));
						hw.addDriverProfile(c);
						break;
						
					case "Car":
						isCar = true;
						c = new Car();
						c.id = Integer.parseInt(name[1]);
						break;
						
					case "Block":
						isCar = false;
						b = new Block();
						b.id = Integer.parseInt(name[1]);
						break;
						
					case "profile:":
						c.setDriverProfile(name[1]);
						break;
						
					case "position:":
						if(isCar)
							c.setPosition(new Point2D.Float(Float.parseFloat(name[1]), Float.parseFloat(name[2])));
						else
							b.setPosition(new Point2D.Float(Float.parseFloat(name[1]), Float.parseFloat(name[2])));
						break;
	
					case "actualSpeed:":
						c.setCurrentSpeed(Float.parseFloat(name[1]));
						hw.getRoadObjects().add(c);
						break;
						
					case "lane:":
						if(isCar)
							c.setLane(Integer.parseInt(name[1]));
						else
							b.setLane(Integer.parseInt(name[1]));
						break;
	
					case "duration:":
						b.duration =  Float.parseFloat(name[1]);
						break;
	
					case "currentTime:":
						b.currentTime = Float.parseFloat(name[1]);
						hw.getRoadObjects().add(b);
						break;
	
					default:
						System.out.println("Hiba!");
						break;
					}
				}
			}
			w.setProfilesPanel();
		}
	}

	public ArrayList<Car> LoadDefaultDriverProfiles(){
		ArrayList<Car> driverprofiles = new ArrayList<>();
		w.chartFrame.removeAll();
		w.chartFrame.setVisible(false);
		w.setCharts();
		BufferedReader br = null;
		String everything = null;
		try {
			br = new BufferedReader(new FileReader("defaultProfiles.txt"));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    everything = sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    try {
		    	if (br != null)
		    		br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(everything != null){
			String[] data = everything.split(System.lineSeparator());
			Car temp = null;
			for (String row : data) {
				String[] name = row.split(" ");
				if(name != null){
					switch (name[0]) {
					case "DriverProfile":
						temp = newDriverProfile(name[1]);
						break;
	
					case "name:":
						temp.setName(name[1]);
						break;
						
					case "maxSpeed:":
						temp.setMaxSpeed(Float.parseFloat(name[1]));
						break;
	
					case "maxAcc:":
						temp.setMaxAcc(Float.parseFloat(name[1]));
						break;
	
					case "color:":
						Color col = new Color(Integer.parseInt(name[1]), Integer.parseInt(name[2]), Integer.parseInt(name[3]));
						temp.setColor(col);
						break;
	
					case "actualSpeed:":
						temp.setCurrentSpeed(Float.parseFloat(name[1]));
						break;
	
					case "prefSpeed:":
						temp.getDriver().setPrefSpeed(Float.parseFloat(name[1]));
						break;
						
					case "rangeOfView:":
						temp.getDriver().setRangeOfView(Float.parseFloat(name[1]));
						break;
						
					case "safetyGap:":
						temp.getDriver().setSafetyGap(Float.parseFloat(name[1]));
						driverprofiles.add(temp);
						break;
					default:
						System.out.println("Hiba!");
						break;
					}
				}
			}
		}
		return driverprofiles;
	}
	
	private Car newDriverProfile(String id) {
		Car c = new Car();
		c.id = Integer.parseInt(id);
		return c;
	}

	public void restart() {
		Road road = w.getRoad();
		this.hw = new HighWay(road, getTimeWarp());
	}

	public float getNewCarTime() {
		return newCarTime;
	}

	public void setNewCarTime(float newCarTime) {
		this.newCarTime = newCarTime;
	}

	public float getTimeWarp() {
		return timeWarp;
	}

	public void setTimeWarp(float f) {
		this.timeWarp = f;
		this.hw.setTimeWarp(f);
	}

}


