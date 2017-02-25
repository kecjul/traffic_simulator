package onlab;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;

public class Controller{

	public static void main(String[] args) {
		Controller c = new Controller();
		while(true){			
			c.trafficLoop();
		}
	}
	
	Window w; 
	HighWay hw; 
	private float deltaTime;
	private float tickTime = (float) 10.0; 
	private float prevTime;
	enum Status{RUNNING, PAUSED};
	Status s = Status.RUNNING;

	Controller(){
		this.hw = new HighWay(1000);
		this.w = new Window(this);
		w.setVisible(true);
		deltaTime = 0;
		prevTime = 0;
	}
		
	void trafficLoop(){
		deltaTime+=getDelta();
		
		//tickTime = 10 ms
		if(deltaTime>=tickTime){
			if(s == Status.RUNNING){
				hw.move();
			}
			w.setRoadObjects(hw.getRoadObjects());
			w.reFresh();
			deltaTime=0;
		}
	}

	private float getTime() {
		return (float)(System.nanoTime() / 1000000);
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

		sb.append("lenght: ").append(Float.toString(hw.lenght)).append(System.lineSeparator());
		if(hw.roadObjects.size()>0){
			for (RoadObject roadObject : hw.roadObjects) {
				if(roadObject instanceof Car){
					Car c = (Car)roadObject;
					sb.append("Car ").append(c.id).append(System.lineSeparator());
					sb.append("position: ").append(Double.toString(c.position.getX())).append(" ").append(Double.toString(c.position.getY())).append(System.lineSeparator());
					sb.append("maxSpeed: ").append(Float.toString(c.maxSpeed)).append(System.lineSeparator());
					sb.append("maxAcc: ").append(Float.toString(c.maxAcc)).append(System.lineSeparator());
					sb.append("color: ").append(Integer.toString(c.color.getRed())).append(" ")
										.append(Integer.toString(c.color.getGreen())).append(" ")
										.append(Integer.toString(c.color.getBlue())).append(" ").append(System.lineSeparator());
					sb.append("actualSpeed: ").append(Float.toString(c.actualSpeed)).append(System.lineSeparator());
					sb.append("prefSpeed: ").append(Float.toString(c.driver.prefSpeed)).append(System.lineSeparator());
					sb.append("rangeOfView: ").append(Float.toString(c.driver.rangeOfView)).append(System.lineSeparator());
				}
				if(roadObject instanceof Block){
					Block b = (Block)roadObject;
					sb.append("Block ").append(b.id).append(System.lineSeparator());
					sb.append("position: ").append(Double.toString(b.position.getX())).append(" ").append(Double.toString(b.position.getY())).append(System.lineSeparator());
					sb.append("duration: ").append(Float.toString(b.duration)).append(System.lineSeparator());
					sb.append("currentTime: ").append(Float.toString(b.currentTime)).append(System.lineSeparator());
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
					case "lenght:":
						hw = new HighWay(Float.parseFloat(name[1]));
						break;
	
					case "ObjectCount:":
						RoadObject.count = Integer.parseInt(name[1]);
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
						
					case "position:":
						if(isCar)
							c.position = new Point2D.Float(Float.parseFloat(name[1]), Float.parseFloat(name[2]));
						else
							b.position = new Point2D.Float(Float.parseFloat(name[1]), Float.parseFloat(name[2]));
						break;
	
					case "maxSpeed:":
						c.maxSpeed = Float.parseFloat(name[1]);
						break;
	
					case "maxAcc:":
						c.maxAcc = Float.parseFloat(name[1]);
						break;
	
					case "color:":
						Color col = new Color(Integer.parseInt(name[1]), Integer.parseInt(name[2]), Integer.parseInt(name[3]));
						c.color = col;
						break;
	
					case "actualSpeed:":
						c.actualSpeed = Float.parseFloat(name[1]);
						break;
	
					case "prefSpeed:":
						c.driver.prefSpeed = Float.parseFloat(name[1]);
						break;
	
					case "rangeOfView:":
						c.driver.rangeOfView = Float.parseFloat(name[1]);
						hw.roadObjects.add(c);
						break;
	
					case "duration:":
						b.duration =  Float.parseFloat(name[1]);
						break;
	
					case "currentTime:":
						b.currentTime = Float.parseFloat(name[1]);
						hw.roadObjects.add(b);
						break;
	
					default:
						System.out.println("Hiba!");
						break;
					}
				}
			}
		}
	}

}


