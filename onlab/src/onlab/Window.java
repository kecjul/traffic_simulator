package onlab;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import onlab.Controller.Status;

public class Window extends JFrame implements ActionListener, MouseListener {
	Controller c;
	Surface surface;
	JFrame chartFrame;
	JPanel settings, combo, options, car, block, highWay, select, scar, sblock, log;
	JLabel newObjectTitle, carTitle, driverTitle, blockTitle, aditional, selectedTitle, selectedCarObject, selectedBlockObject;
	JLabel lcarMaxSpeed, lcarMaxAcc, lcarColor, ldriverPrefSpeed, ldriverRange, ldriverSafety, lblockDuration, lhwLenght;
	JTextField tcarMaxSpeed, tcarMaxAcc, tdriverPrefSpeed, tdriverRange, tdriverSafety, tblockDuration, thwLenght;
	JLabel  slcarMaxSpeed, slcarMaxAcc, slcarColor, sldriverPrefSpeed, sldriverRange, sldriverSafety, slblockDuration, slhwLenght;
	JTextField stcarMaxSpeed, stcarMaxAcc, stdriverPrefSpeed, stdriverRange, stdriverSafety, stblockDuration, sthwLenght;
	JComboBox typeChooser , colorChooser, scolorChooser;
	JButton bPause, bSave, bLoad, bcAccept, bbAccept, bcDel, bbDel, bChart;
	JTextArea tLog;
	JScrollPane logScroll;
	JSplitPane split1, split2, split3; 
	JSplitPane settingsSplit, selectSplit, logSplit; 
	final static String CARPANEL = "Car";
	final static String BLOCKPANEL = "Road Block";
	float size = 450;
	String[] types = {CARPANEL, BLOCKPANEL};
	String[] colorsS = {"White", "Yellow", "Green", "Cyan", "Blue", "Pink", "Orange"};
	Color[] colorsC = {Color.WHITE ,Color.YELLOW ,Color.GREEN ,Color.CYAN ,Color.BLUE ,Color.PINK ,Color.ORANGE};
	RoadObject selected;
	ArrayList<TimeSeries> series = new ArrayList<TimeSeries>();
	ArrayList<TimeSeriesCollection> dataset= new ArrayList<TimeSeriesCollection>();
	JFreeChart chart;
	XYPlot plot;
	ChartPanel chartPanel;
	JPanel content;

	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	int screenWidth = screenSize.width;
	int screenHeight = screenSize.height;
		
	public Window(Controller c) {
		initUI();
		this.c = c;
	}

	private void initUI() {
		Dimension surfaceMinimumSize = new Dimension(screenWidth - 280, screenHeight - 150);
		Dimension settingsMinimumSize = new Dimension(280, screenHeight);
		Dimension logMinimumSize = new Dimension(280, screenHeight);
		int settingsSplitPositionY = screenWidth - 280;
		int selectSplitPositionX = screenHeight - 200;
		int logSplitPositionX = screenHeight - 200;
		
		surface = new Surface();
		surface.addMouseListener(this);
		settings = new JPanel();
		settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
		setOptions();		
		log = new JPanel();
		log.setLayout(new BorderLayout());
		setLogPanel();
		setCharts();	
		
		surface.setMinimumSize(surfaceMinimumSize);
		settings.setMinimumSize(settingsMinimumSize);
		log.setMinimumSize(logMinimumSize);	
		
		select = new JPanel();
		setSelectPanel();
		
		logSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		logSplit.setTopComponent(settings);
		logSplit.setBottomComponent(log);
		logSplit.setDividerLocation(logSplitPositionX);
		
		selectSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		selectSplit.setTopComponent(surface);
		selectSplit.setBottomComponent(select);
		selectSplit.setDividerLocation(selectSplitPositionX);
		selectSplit.setEnabled(false);
		
		settingsSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		settingsSplit.setLeftComponent(selectSplit);
		settingsSplit.setRightComponent(logSplit);
		settingsSplit.setDividerLocation(settingsSplitPositionY);
		settingsSplit.setEnabled(false);

		
		add(settingsSplit);
		
		setTitle("Traffic jam simulator");
		
		setMinimumSize(new Dimension(700, 650));
		setSize(screenSize);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
	}
	
	private void setSelectPanel() {
		SpringLayout carLayout = new SpringLayout();
		scar = new JPanel(carLayout);
		
		selectedCarObject = new JLabel("Car");
		scar.add(selectedCarObject);
		carLayout.putConstraint(SpringLayout.WEST, selectedCarObject, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, selectedCarObject, 5, SpringLayout.NORTH, scar);

		slcarMaxSpeed = new JLabel("Max Speed: ");
		slcarMaxAcc = new JLabel("Max Acceleration: ");
		slcarColor = new JLabel("Color: ");
		scar.add(slcarMaxSpeed);
		scar.add(slcarMaxAcc);
		scar.add(slcarColor);
		carLayout.putConstraint(SpringLayout.WEST, slcarMaxSpeed, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, slcarMaxSpeed, 25, SpringLayout.NORTH, selectedCarObject);
		carLayout.putConstraint(SpringLayout.WEST, slcarMaxAcc, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, slcarMaxAcc, 25, SpringLayout.NORTH, slcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, slcarColor, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, slcarColor, 25, SpringLayout.NORTH, slcarMaxAcc);


		stcarMaxSpeed = new JTextField(5);
		stcarMaxAcc = new JTextField(5);
		stcarMaxSpeed.setToolTipText("km/h");
		stcarMaxAcc.setToolTipText("from 0 to 100 km/h in sec");
		scolorChooser = new JComboBox(colorsS);
		scar.add(stcarMaxSpeed);
		scar.add(stcarMaxAcc);
		scar.add(scolorChooser);
		carLayout.putConstraint(SpringLayout.WEST, stcarMaxSpeed, 120, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stcarMaxSpeed, 25, SpringLayout.NORTH, selectedCarObject);
		carLayout.putConstraint(SpringLayout.WEST, stcarMaxAcc, 120, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stcarMaxAcc, 25, SpringLayout.NORTH, slcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, scolorChooser, 120, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, scolorChooser, 25, SpringLayout.NORTH, stcarMaxAcc);
		
		
		sldriverPrefSpeed = new JLabel("Preffered Speed: ");
		sldriverRange = new JLabel("Range of View: ");
		sldriverSafety = new JLabel("Safety gap: ");
		scar.add(sldriverPrefSpeed);
		scar.add(sldriverRange);
		scar.add(sldriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, sldriverPrefSpeed, 200, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, sldriverPrefSpeed, 30, SpringLayout.NORTH, scar);
		carLayout.putConstraint(SpringLayout.WEST, sldriverRange, 200, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, sldriverRange, 25, SpringLayout.NORTH, sldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, sldriverSafety, 200, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, sldriverSafety, 25, SpringLayout.NORTH, sldriverRange);

		stdriverPrefSpeed = new JTextField(5);
		stdriverRange = new JTextField(5);
		stdriverSafety = new JTextField(5);
		stdriverPrefSpeed.setToolTipText("km/h");
		stdriverRange.setToolTipText("km");
		stdriverSafety.setToolTipText("km");
		scar.add(stdriverPrefSpeed);
		scar.add(stdriverRange);
		scar.add(stdriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, stdriverPrefSpeed, 300, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stdriverPrefSpeed, 30, SpringLayout.NORTH, scar);
		carLayout.putConstraint(SpringLayout.WEST, stdriverRange, 300, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stdriverRange, 25, SpringLayout.NORTH, sldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, stdriverSafety, 300, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, stdriverSafety, 25, SpringLayout.NORTH, sldriverRange);
		
		bcAccept = new JButton("Accept");
		bcAccept.addActionListener(this);
		scar.add(bcAccept);
		carLayout.putConstraint(SpringLayout.WEST, bcAccept, 5, SpringLayout.WEST, scar);
		carLayout.putConstraint(SpringLayout.NORTH, bcAccept, 30, SpringLayout.NORTH, scolorChooser);
		bcDel = new JButton("Delete");
		bcDel.addActionListener(this);
		scar.add(bcDel);
		carLayout.putConstraint(SpringLayout.WEST, bcDel, 80, SpringLayout.WEST, bcAccept);
		carLayout.putConstraint(SpringLayout.NORTH, bcDel, 30, SpringLayout.NORTH, scolorChooser);
		
		SpringLayout blockLayout = new SpringLayout();
		sblock = new JPanel(blockLayout);
		
		selectedBlockObject = new JLabel("RoadBlock");
		sblock.add(selectedBlockObject);
		blockLayout.putConstraint(SpringLayout.WEST, selectedBlockObject, 5, SpringLayout.WEST, sblock);
		blockLayout.putConstraint(SpringLayout.NORTH, selectedBlockObject, 5, SpringLayout.NORTH, sblock);
		

		slblockDuration = new JLabel("Duration: ");
		sblock.add(slblockDuration);
		blockLayout.putConstraint(SpringLayout.WEST, slblockDuration, 5, SpringLayout.WEST, sblock);
		blockLayout.putConstraint(SpringLayout.NORTH, slblockDuration, 25, SpringLayout.NORTH, selectedBlockObject);

		stblockDuration = new JTextField(10);
		stblockDuration.setToolTipText("sec");
		sblock.add(stblockDuration);
		blockLayout.putConstraint(SpringLayout.WEST, stblockDuration, 120, SpringLayout.WEST, sblock);
		blockLayout.putConstraint(SpringLayout.NORTH, stblockDuration, 25, SpringLayout.NORTH, selectedBlockObject);
		
		bbAccept = new JButton("Accept");
		bbAccept.addActionListener(this);
		sblock.add(bbAccept);
		blockLayout.putConstraint(SpringLayout.WEST, bbAccept, 240, SpringLayout.WEST, sblock);
		blockLayout.putConstraint(SpringLayout.NORTH, bbAccept, 25, SpringLayout.NORTH, sblock);
		bbDel = new JButton("Delete");
		bbDel.addActionListener(this);
		sblock.add(bbDel);
		blockLayout.putConstraint(SpringLayout.WEST, bbDel, 80, SpringLayout.WEST, bbAccept);
		blockLayout.putConstraint(SpringLayout.NORTH, bbDel, 25, SpringLayout.NORTH, sblock);
		
	}

	public void setOptions(){
		combo = new JPanel();
		newObjectTitle = new JLabel("New Road Object: ");
		typeChooser = new JComboBox(types);
		typeChooser.setSelectedIndex(0);
		typeChooser.addActionListener(this);
		combo.add(newObjectTitle);
		combo.add(typeChooser);
		
		options = new JPanel(new CardLayout());
		setCarOptions();
        setBlockOptions();
		options.add(car, CARPANEL);
		options.add(block, BLOCKPANEL);
		
		SpringLayout hwLayout = new SpringLayout();
		highWay = new JPanel(hwLayout);	
		aditional =  new JLabel("Aditional options: ");
		lhwLenght = new JLabel("HighWay lenght: ");
		thwLenght = new JTextField(10);
		thwLenght.setText("12960000");
		thwLenght.setToolTipText("12.960.000 km is ideal as 100 km/h speed will show as 1 deg/sec");
		thwLenght.addActionListener(this);
		bPause = new JButton("Pause");
		bPause.addActionListener(this);
		bChart = new JButton("Speed chart");
		bChart.addActionListener(this);
		bSave = new JButton("Save");
		bSave.addActionListener(this);
		bLoad = new JButton("Load");
		bLoad.addActionListener(this);

		highWay.add(aditional);
		highWay.add(lhwLenght);
		highWay.add(thwLenght);
		highWay.add(bPause);
		highWay.add(bChart);
		highWay.add(bSave);
		highWay.add(bLoad);
		hwLayout.putConstraint(SpringLayout.WEST, aditional, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, aditional, 25, SpringLayout.NORTH, highWay);
		hwLayout.putConstraint(SpringLayout.WEST, lhwLenght, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, lhwLenght, 25, SpringLayout.NORTH, aditional);
		hwLayout.putConstraint(SpringLayout.WEST, thwLenght, 120, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, thwLenght, 25, SpringLayout.NORTH, aditional);
		hwLayout.putConstraint(SpringLayout.WEST, bPause, 5, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bPause, 50, SpringLayout.NORTH, lhwLenght);
		hwLayout.putConstraint(SpringLayout.WEST, bChart, 80, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bChart, 50, SpringLayout.NORTH, lhwLenght);
		hwLayout.putConstraint(SpringLayout.WEST, bSave, 10, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bSave, 35, SpringLayout.NORTH, bPause);
		hwLayout.putConstraint(SpringLayout.WEST, bLoad, 80, SpringLayout.WEST, highWay);
		hwLayout.putConstraint(SpringLayout.NORTH, bLoad, 35, SpringLayout.NORTH, bPause);
		
		settings.add(combo);
		settings.add(options);
		settings.add(highWay);
	}
	
	public void setCarOptions(){
		SpringLayout carLayout = new SpringLayout();
		car = new JPanel(carLayout);
		
		carTitle = new JLabel("Car");
		car.add(carTitle);
		carLayout.putConstraint(SpringLayout.WEST, carTitle, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, carTitle, 5, SpringLayout.NORTH, car);

		lcarMaxSpeed = new JLabel("Max Speed: ");
		lcarMaxAcc = new JLabel("Max Acceleration: ");
		lcarColor = new JLabel("Color: ");
		car.add(lcarMaxSpeed);
		car.add(lcarMaxAcc);
		car.add(lcarColor);
		carLayout.putConstraint(SpringLayout.WEST, lcarMaxSpeed, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarMaxSpeed, 25, SpringLayout.NORTH, carTitle);
		carLayout.putConstraint(SpringLayout.WEST, lcarMaxAcc, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarMaxAcc, 25, SpringLayout.NORTH, lcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, lcarColor, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, lcarColor, 25, SpringLayout.NORTH, lcarMaxAcc);


		tcarMaxSpeed = new JTextField(10);
		tcarMaxAcc = new JTextField(10);
		tcarMaxSpeed.setText("100");
		tcarMaxAcc.setText("10");
		tcarMaxSpeed.setToolTipText("km/h");
		tcarMaxAcc.setToolTipText("from 0 to 100 km/h in sec");
		colorChooser = new JComboBox(colorsS);
		car.add(tcarMaxSpeed);
		car.add(tcarMaxAcc);
		car.add(colorChooser);
		carLayout.putConstraint(SpringLayout.WEST, tcarMaxSpeed, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tcarMaxSpeed, 25, SpringLayout.NORTH, carTitle);
		carLayout.putConstraint(SpringLayout.WEST, tcarMaxAcc, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tcarMaxAcc, 25, SpringLayout.NORTH, lcarMaxSpeed);
		carLayout.putConstraint(SpringLayout.WEST, colorChooser, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, colorChooser, 25, SpringLayout.NORTH, tcarMaxAcc);
		

		driverTitle = new JLabel("Driver");
		car.add(driverTitle);
		carLayout.putConstraint(SpringLayout.WEST, driverTitle, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, driverTitle, 35, SpringLayout.NORTH, colorChooser);
		
		ldriverPrefSpeed = new JLabel("Preffered Speed: ");
		ldriverRange = new JLabel("Range of View: ");
		ldriverSafety = new JLabel("Safety gap: ");
		car.add(ldriverPrefSpeed);
		car.add(ldriverRange);
		car.add(ldriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, ldriverPrefSpeed, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, ldriverPrefSpeed, 25, SpringLayout.NORTH, driverTitle);
		carLayout.putConstraint(SpringLayout.WEST, ldriverRange, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, ldriverRange, 25, SpringLayout.NORTH, ldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, ldriverSafety, 5, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, ldriverSafety, 25, SpringLayout.NORTH, ldriverRange);
		
		tdriverPrefSpeed = new JTextField(10);
		tdriverRange = new JTextField(10);
		tdriverSafety = new JTextField(10);
		tdriverPrefSpeed.setText("80");
		tdriverRange.setText("50");
		tdriverSafety.setText("20");
		tdriverPrefSpeed.setToolTipText("km/h");
		tdriverRange.setToolTipText("km");
		tdriverSafety.setToolTipText("km");		
		car.add(tdriverPrefSpeed);
		car.add(tdriverRange);
		car.add(tdriverSafety);
		carLayout.putConstraint(SpringLayout.WEST, tdriverPrefSpeed, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tdriverPrefSpeed, 25, SpringLayout.NORTH, driverTitle);
		carLayout.putConstraint(SpringLayout.WEST, tdriverRange, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tdriverRange, 25, SpringLayout.NORTH, ldriverPrefSpeed);
		carLayout.putConstraint(SpringLayout.WEST, tdriverSafety, 120, SpringLayout.WEST, car);
		carLayout.putConstraint(SpringLayout.NORTH, tdriverSafety, 25, SpringLayout.NORTH, ldriverRange);
		
		
	}
	
	public void setBlockOptions(){
		SpringLayout blockLayout = new SpringLayout();
		block = new JPanel(blockLayout);
		
		blockTitle = new JLabel("RoadBlock");
		block.add(blockTitle);
		blockLayout.putConstraint(SpringLayout.WEST, blockTitle, 5, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, blockTitle, 5, SpringLayout.NORTH, block);
		

		lblockDuration = new JLabel("Duration: ");
		block.add(lblockDuration);
		blockLayout.putConstraint(SpringLayout.WEST, lblockDuration, 5, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, lblockDuration, 25, SpringLayout.NORTH, blockTitle);

		tblockDuration = new JTextField(10);
		tblockDuration.setText("10");
		tblockDuration.setToolTipText("sec");
		block.add(tblockDuration);
		blockLayout.putConstraint(SpringLayout.WEST, tblockDuration, 120, SpringLayout.WEST, block);
		blockLayout.putConstraint(SpringLayout.NORTH, tblockDuration, 25, SpringLayout.NORTH, blockTitle);
	}
	
	public void setLogPanel(){
		tLog = new JTextArea();
		tLog.setEditable(false);
		logScroll = new JScrollPane(tLog);
		log.add(logScroll);
	}
	
	public void setCharts(){
		chartFrame = new JFrame();
		chartFrame.setTitle("Traffic jam simulator - Chart of the cars speed");
		chartFrame.setSize(500, 300);
		chartFrame.setDefaultCloseOperation(HIDE_ON_CLOSE);
		series.add(new TimeSeries("Random Data"));
	    dataset.add(new TimeSeriesCollection(series.get(0)));
	    JFreeChart chart = ChartFactory.createTimeSeriesChart(
	            "Speed of the cars", 
	            "Time", 
	            "Speed(m/s)",
	            dataset.get(0), 
	            true, 
	            true, 
	            false
	        );
	    plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000.0);  // 60 seconds
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 200.0); 
	    ChartPanel chartPanel = new ChartPanel(chart);
	    
	    JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartFrame.setContentPane(content);
        
        
		 
	}
	public void newChartData(String[] name, Float[] data, Color[] color){
		Millisecond now = new Millisecond();
		for (int i = 0; i < name.length; i++) {
			if( name[i] != null){
				if(i >= series.size()){
					series.add(new TimeSeries(name[i]));
					dataset.add(new TimeSeriesCollection(series.get(i)));
					plot.setDataset(i, dataset.get(i));
					plot.setRenderer(i, new StandardXYItemRenderer());
					plot.getRenderer().setSeriesPaint(i, color[i]);
				}
				if(!series.get(i).getKey().equals(name[i])){
					series.get(i).setKey(name[i]);
					plot.getRenderer().setSeriesPaint(i, color[i]);
				}
				series.get(i).add(now, data[i]);
				System.out.println("plotColor: " + plot.getRenderer().getSeriesPaint(i) + " carColor: " + color[i]);
				plot.getRenderer().getSeriesPaint(i);
			}
		}
	}
	
	
	
	public void typeSelected(String type) {
	    CardLayout cl = (CardLayout)(options.getLayout());
	    cl.show(options, type);
	}
	
	public void setRoadObjects(ArrayList<RoadObject> ro){
		surface.setRoadObjects(ro);
	}
	public void reFresh(){
		surface.reFresh();
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JComboBox){
			JComboBox cb = (JComboBox)e.getSource();
	        String type = (String)cb.getSelectedItem();
	        typeSelected(type);
		}	
		if(e.getSource() instanceof JTextField){
			JTextField tf = (JTextField)e.getSource();
	        String lenght = (String)tf.getText();
	        if("".equals(lenght))
	        	lenght = new String("1500");
			c.hw.setLenght(Float.parseFloat(lenght));
			if(!Float.toString(c.hw.lenght).equals(thwLenght.getText()))
				thwLenght.setText(Float.toString(c.hw.lenght));
		}
		if(e.getSource() instanceof JButton){
			JButton b = (JButton)e.getSource();
			String text = b.getText();
			if("Pause".equals(text)){
				bPause.setText("Start");
				c.stop();
			} else if("Start".equals(text)){
				bPause.setText("Pause");
				c.stop();
			}else if(b == bChart){
				chartFrame.setVisible(true);
			} else if("Save".equals(text)){
				String extension = "txt";
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("TEXT FILES", "txt"));
				Status s = c.s; 
				if(c.s == Status.RUNNING)
					c.stop();
				int returnVal = fc.showSaveDialog(this);
				if(c.s != s)
					c.stop();
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		        	if(file != null){
			            if(!file.getName().toLowerCase().endsWith('.' + extension.toLowerCase()))
			            	file = new File(file.toString() + '.' + extension);
						c.SaveGame(file);
		        	}
		        } 	
			} else if("Load".equals(text)){
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("TEXT FILES", "txt"));
				Status s = c.s; 
				if(c.s == Status.RUNNING)
					c.stop();
				int returnVal = fc.showOpenDialog(this);
				if(c.s != s)
					c.stop();
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		        	if(file != null)
		        		c.LoadGame(file);
		        } 	
			} else if(b == bcAccept){
				c.hw.modifyCar(selected.id, Float.parseFloat(stcarMaxSpeed.getText()), Float.parseFloat(stcarMaxAcc.getText()), colorsC[scolorChooser.getSelectedIndex()], 
					Float.parseFloat(stdriverPrefSpeed.getText()), Float.parseFloat(stdriverRange.getText()), Float.parseFloat(stdriverSafety.getText()));	
			} else if(b == bbAccept){
				c.hw.modifyBlock(selected.id, Float.parseFloat(stblockDuration.getText()));
			} else if(b == bcDel || b == bbDel){
				c.hw.deleteObject(selected);
			}
		}
	}
	
	public void setLog(){
		String newRow = null;
		String type = null; 
		for (int i = 1; i <= RoadObject.count; i++) {
			type = c.hw.getType(i);
			if(type != null && "Car".equals(type)){
				if("driving".equals(c.hw.getStatus(i)))
					newRow = "Car " + Integer.toString(i) + " is " + c.hw.getStatus(i) + " with speed: " + c.hw.getSpeed(i) +".";
				else
					newRow = "Car " + Integer.toString(i) + " is " + c.hw.getStatus(i) + ".";
			}
			if(type != null && "Block".equals(type)){
				newRow = "Block " + Integer.toString(i) + " has " + c.hw.getStatus(i) + " seconds left.";
			}
			if(newRow != null){
				tLog.append(newRow);
				tLog.append(System.lineSeparator());
			}
		}
	}
	public Point2D.Float reConvertCoords(Point2D.Float position){
		return new Point2D.Float(position.x - (size/2), size - (position.y + (size/2)));
	}
	
	public void setSelected(){
		if(selected != null){
			if(selected instanceof Car){
				selectSplit.setBottomComponent(scar);
//				select.remove(sblock);
//				select.add(scar);
				selectedCarObject.setText("Car " + selected.id);
				stcarMaxSpeed.setText(Float.toString(((Car) selected).maxSpeed));
				stcarMaxAcc.setText(Float.toString(((Car) selected).maxAcc));
				for (int i = 0; i < colorsC.length; i++){
					if(colorsC[i].getRed() == ((Car) selected).color.getRed() &&
						colorsC[i].getGreen() == ((Car) selected).color.getGreen() &&
						colorsC[i].getBlue()== ((Car) selected).color.getBlue())
					{
						scolorChooser.setSelectedIndex(i);
						break;
					}
				}
				stdriverPrefSpeed.setText(Float.toString(((Car) selected).driver.prefSpeed));
				stdriverRange.setText(Float.toString(((Car) selected).driver.rangeOfView));
				stdriverSafety.setText(Float.toString(((Car) selected).driver.safetyGap));
			}
			else if(selected instanceof Block){
				selectSplit.setBottomComponent(sblock);
//				select.remove(scar);
//				select.add(sblock);
				selectedBlockObject.setText("Block " + selected.id);
				stblockDuration.setText(Float.toString(((Block) selected).duration));
			}
			
		}
		SwingUtilities.updateComponentTreeUI(this);
		selectSplit.invalidate();
		selectSplit.repaint();
		selectSplit.revalidate();
	}
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Point2D.Float newPoint = new Point2D.Float();
		newPoint.x = e.getX();
		newPoint.y = e.getY();
		selected = surface.getObject(reConvertCoords(newPoint));
		if(selected == null && typeChooser.getSelectedItem().equals("Car")){
			if(!"".equals(tcarMaxSpeed.getText()) && !"".equals(tcarMaxAcc.getText()) && 
					!"".equals(tdriverPrefSpeed.getText()) && !"".equals(tdriverRange.getText()) && !"".equals(tdriverSafety.getText())){
				c.hw.addCar(reConvertCoords(newPoint), Float.parseFloat(tcarMaxSpeed.getText()), Float.parseFloat(tcarMaxAcc.getText()), colorsC[colorChooser.getSelectedIndex()], 
						Float.parseFloat(tdriverPrefSpeed.getText()), Float.parseFloat(tdriverRange.getText()), Float.parseFloat(tdriverSafety.getText()));	
			}
		}else if (selected == null){
			if(!"".equals(tblockDuration.getText())){
				c.hw.addBlock(reConvertCoords(newPoint), Float.parseFloat(tblockDuration.getText()));
			}
		}
		selected = surface.getObject(reConvertCoords(newPoint));
		setSelected();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
