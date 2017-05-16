package onlab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.jfree.data.time.Millisecond;

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
	public static float tickTime = (float) 10.0; 
	private float prevTime;
	
	private float newCarTime = 5000.0f;
	private int carTickCount = 0;
	
	public static float timeWarp = 10f;

	enum Status{RUNNING, PAUSED};
	Status s = Status.RUNNING;

	@SuppressWarnings("rawtypes")
	Map<Millisecond, ArrayList> carSpeedChartData = new HashMap<Millisecond, ArrayList>();
	Map<Millisecond, ArrayList> profileSpeedChartData = new HashMap<Millisecond, ArrayList>();
	Map<Date, ArrayList> driverStatusChartData = new TreeMap<Date, ArrayList>();
	private int chartTickCount = 0;
	Date start = new Date();
	Date past = new Date();
	
	Controller(){
		this.w = new Window(this);
		w.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		w.surface.screenWidth = w.getBounds().width;
//		w.surface.screenHeight = w.getBounds().height;
		Road road = w.initUI();
		this.hw = new HighWay(road, getTimeWarp());
		this.hw.addDriverProfiles(LoadDefaultDriverProfiles());
		w.setProfilesPanel();
		w.setVisible(true);
		w.setResizable(false);		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		w.setMinimumSize(new Dimension(screenSize.width, screenSize.height-50));
		
		deltaTime = 0;
		prevTime = 0;
		carSpeedChartData = new HashMap<Millisecond, ArrayList>();
		profileSpeedChartData = new HashMap<Millisecond, ArrayList>();
		driverStatusChartData = new TreeMap<Date, ArrayList>();
		start = new Date();
		past = new Date();
	}
		
	void trafficLoop(){
		float delta = getDelta();
		if(delta>0){
			deltaTime+=delta;
			
			//tickTime = 10 ms
			if(deltaTime>=tickTime){
				if(s == Status.RUNNING){
					if(carTickCount * tickTime >=getNewCarTime()/getTimeWarp()){
						hw.newCar();
						carTickCount = 0;
						setStatusChartData();
					}
					hw.move();
					w.setLog();
					if(HighWay.getRoadObjects() != null){
						setChartData();						
					}
					chartTickCount++;
				}
				w.setRoadObjects(HighWay.getRoadObjects());
				w.reFresh();
				deltaTime=0;
				carTickCount++;
			}
		}
	}
	
	private void setChartData(){
		Millisecond now = getCurrentChartTime(); 		
		ArrayList<Object> temp = new ArrayList<>(); 
		
		ArrayList<String> names = new ArrayList<>();
		ArrayList<Float> carSpeedDatas = new ArrayList<>();
		ArrayList<Color> carColors = new ArrayList<>();
		
		ArrayList<String> driverProfiles = HighWay.getListDriverProfileNames();
		ArrayList<Integer> profileCount = new ArrayList<>();
		ArrayList<Float> profileSpeedDatas = new ArrayList<>();
		for (String string : driverProfiles) {
			profileCount.add(0);
			profileSpeedDatas.add(0f);
		}	
		
		for (int i = 0; i < HighWay.getRoadObjects().size(); i++) {
			if(HighWay.getRoadObjects().get(i) instanceof Car){
				Car c = (Car) HighWay.getRoadObjects().get(i);
				names.add("Car " + c.id);
				carSpeedDatas.add(c.getCurrentSpeed());
				carColors.add(c.getColor());
				
				int profileIndex = driverProfiles.indexOf(c.getName());
				profileCount.set(profileIndex, profileCount.get(profileIndex)+1);
				profileSpeedDatas.set(profileIndex, profileSpeedDatas.get(profileIndex)+c.getCurrentSpeed());
			}
		}
		for (int i = 0; i < profileCount.size(); i++) {
			profileSpeedDatas.set(i, profileSpeedDatas.get(i)/profileCount.get(i));
		}

		if(chartTickCount % 5 == 0){
			temp.add(names);
			temp.add(carSpeedDatas);
			temp.add(carColors);
			carSpeedChartData.put(now, temp);
		}
		temp = new ArrayList<>();
		temp.add(driverProfiles);
		temp.add(profileSpeedDatas);		
		profileSpeedChartData.put(now, temp);

		deleteOldChartData(carSpeedChartData);
		deleteOldChartData(profileSpeedChartData);

//		w.newChartData(names.toArray(), datas.toArray(), colors.toArray());
	}
	
	private void setStatusChartData(){
		Millisecond now = getCurrentChartTime(); 	
		
		ArrayList<Object> temp = new ArrayList<>(); 
		
		onlab.Driver.Status[] driverStatus = Driver.Status.values();
		ArrayList<String> status = new ArrayList<>();
		ArrayList<Integer> driverStatusCount = new ArrayList<>();
		for (int i = 0; i < driverStatus.length; i++) {
			status.add(driverStatus[i].toString());
			driverStatusCount.add(0);
		}		
		
		for (int i = 0; i < HighWay.getRoadObjects().size(); i++) {
			if(HighWay.getRoadObjects().get(i) instanceof Car){
				Car c = (Car) HighWay.getRoadObjects().get(i);
				int statusIndex = status.indexOf(c.getDriver().getStatus().toString());
				driverStatusCount.set(statusIndex, driverStatusCount.get(statusIndex)+1);
			}
		}
		
		temp = new ArrayList<>();
		temp.add(status);
		temp.add(driverStatusCount);		
		driverStatusChartData.put(now.getStart(), temp);
		

		ArrayList<Date> del = new ArrayList<>();
		for(Date date : driverStatusChartData.keySet()){
			if(date.compareTo(start) < 0){
				del.add(date);
			}
		}
		for(Date date : del){
			if(driverStatusChartData.get(date) != null){
				driverStatusChartData.remove(date);
			}
		}
	}

	private void deleteOldChartData(Map<Millisecond, ArrayList> chartData) {
		ArrayList<Millisecond> del = new ArrayList<>();
		for(Millisecond ms : chartData.keySet()){
			if(ms.getStart().compareTo(start) < 0){
				del.add(ms);
			}
		}
		for(Millisecond ms : del){
			if(chartData.get(ms) != null){
				chartData.remove(ms);
			}
		}
	}

	private Millisecond getCurrentChartTime() {
		Date nowDate = new Date();
		nowDate.setTime(start.getTime() + chartTickCount * 10);
		if(chartTickCount>=6000){
			start.setTime(start.getTime()+10);
		}
		return new Millisecond(nowDate);
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
			deltaTime=0;
		}
	}

	@SuppressWarnings("unchecked")
	public void SaveGame(File file){
		StringBuffer sb  = new StringBuffer();

		sb.append("newCarTimer: ").append(Float.toString(getNewCarTime())).append(System.lineSeparator());
		sb.append("timeWarp: ").append(Float.toString(hw.getTimeWarp())).append(System.lineSeparator());
		sb.append("lanes: ").append(hw.getLaneCount()).append(System.lineSeparator());
		if(HighWay.getDriverProfiles().size()>0){
			for (Car driverProfile : HighWay.getDriverProfiles()) {
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
		if(HighWay.getRoadObjects().size()>0){
			for (RoadObject roadObject :(ArrayList<RoadObject>) HighWay.getRoadObjects().clone()) {
				if(roadObject instanceof Car){
					Car c = (Car)roadObject;
					sb.append("Car ").append(c.id).append(System.lineSeparator());
					sb.append("profile: ").append(c.getName()).append(System.lineSeparator());
					sb.append("position: ").append(Double.toString(c.getPosition().getX())).append(" ").append(Double.toString(c.getPosition().getY())).append(System.lineSeparator());					
					sb.append("actualSpeed: ").append(Float.toString(c.getCurrentSpeed())).append(System.lineSeparator());					
					sb.append("lane: ").append(c.getLane()).append(System.lineSeparator());					
					sb.append("status: ").append(c.getDriver().getStatus().toString()).append(System.lineSeparator());					
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
						Surface.setLaneCount(lanes);
						w.setLanes();
						HighWay.setDriverProfiles(null);
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
						HighWay.getRoadObjects().add(c);
						break;
						
					case "lane:":
						if(isCar)
							c.setLane(Integer.parseInt(name[1]));
						else
							b.setLane(Integer.parseInt(name[1]));
						break;
						
					case "status:":
						c.getDriver().setStatus(getStatus(name[1]));
						break;
	
					case "duration:":
						b.duration =  Float.parseFloat(name[1]);
						break;
	
					case "currentTime:":
						b.currentTime = Float.parseFloat(name[1]);
						HighWay.getRoadObjects().add(b);
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
	
	private Driver.Status getStatus(String string){
		for (Driver.Status s : Driver.Status.values()) {
			if(string.equals(s.toString())){
				return s;
			}
		}
		return null;
	}

	public ArrayList<Car> LoadDefaultDriverProfiles(){
		ArrayList<Car> driverprofiles = new ArrayList<>();
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

		deltaTime = 0;
		prevTime = 0;
		carSpeedChartData = new HashMap<Millisecond, ArrayList>();
		profileSpeedChartData = new HashMap<Millisecond, ArrayList>();
		driverStatusChartData = new TreeMap<Date, ArrayList>();
		start = new Date();
		past = new Date();
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


