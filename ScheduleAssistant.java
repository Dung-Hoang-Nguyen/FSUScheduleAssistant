package various;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.*;
import java.awt.event.MouseListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

public class ScheduleAssistant extends JFrame{
/**
 * 
 */
private static final long serialVersionUID = 1L;
Color backColor,textColor;
Scanner blockScan;
JTextArea text;
JButton generate;
JPanel classesPanel, schedulesPanel, FiltrPanel, panel;
JTabbedPane tabs;
JScrollPane scroll;
ArrayList<String> infoTypes;
ArrayList<Class> classes,allClasses;
public ArrayList<Schedule> schedules;
public static void main(String[] args) {
	SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            new ScheduleAssistant(Color.WHITE,new File("ScheduleBlocks.txt")).setVisible(true);
	        }
	    });
	}
public ScheduleAssistant(Color c, File f){
	setTitle("Schedule Assistant");
	//setIcon();
	infoTypes = new ArrayList<String>(Arrays.asList("[A-Z]{3}[0-9]{4}L?","[0-9]{4}","[A-Z]{3}","([1-9]|[1-9][0-9]|[1-9][0-9][0-9])"
			,"[A-Za-z/-]+","(M|Th|W|T|F)+",/*"[A-Z]{3}","[0-9]{4}",*/"([A-Za-z]+,)|Online","[A-Za-z]+","[0-6]"));
	setLayout(new BorderLayout());
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(25, 100, 1200, 1000);
	backColor=c;
	setBackground(backColor);
    getContentPane().setBackground(backColor);
    //inverse of screen color
    textColor = new Color(255-backColor.getRed(),255-backColor.getGreen(),255-backColor.getBlue());
    try {allClasses=toList(f);} catch (FileNotFoundException e) {
//		e.printStackTrace();
		System.out.print("File not found");
	}
    classes=new ArrayList<Class>(allClasses);
    tabs=new JTabbedPane();
    classesPanel = new JPanel(new GridLayout(0, 1));
    classesPanel.setOpaque(false);
    panel=new JPanel(new BorderLayout());
    scroll = new JScrollPane(panel);
    Border bord = BorderFactory.createLineBorder(backColor, 20);
    panel.setBackground(backColor);
    panel.add(classesPanel,BorderLayout.LINE_START);
    scroll.setPreferredSize(new Dimension(450, 110));
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    tabs.addTab("Schedule Assistant",scroll);
    scroll.setFocusable(false);
    scroll.setBorder(BorderFactory.createEtchedBorder());
    tabs.setOpaque(false);
    add(tabs, BorderLayout.CENTER);
    for(int i=0;i<allClasses.size();i++) {
    	final int index=i;
    	JCheckBox box = new JCheckBox(allClasses.get(i).info(),true);
    	box.addItemListener(new ItemListener() {    
            public void itemStateChanged(ItemEvent e) {
	        	if(e.getStateChange()==1){
	        		classes.add(allClasses.get(index));
	        	}
	        	else {
	        		classes.remove(allClasses.get(index));
	        	}
            }    
         });
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        box.setOpaque(false);
        box.setContentAreaFilled(false);;
        row.add(box);
        row.setOpaque(false);
        classesPanel.add(row);
    }
    classesPanel.setBorder(bord);
    generate=new JButton("Generate Schedules");
    generate.setContentAreaFilled(false);
    generate.setFocusable(false);
    generate.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			if(classes.isEmpty()) {System.out.println("Error: Please select a class");return;}
			if(schedulesPanel!=null &&schedulesPanel.getParent()!=null) {panel.remove(schedulesPanel);}
			schedules=new ArrayList<Schedule>();
		    possibleSchedules(new Schedule(),0);
		    schedulesPanel=new JPanel(new GridLayout(0,1));
		    schedulesPanel.setOpaque(false);
		    schedulesPanel.add(new JLabel("Found "+schedules.size()+" schedules"));
		    for(int i=0;i<schedules.size();i++) {
		    	final int index=i;
		    	JButton but = new JButton("peek");
		    	but.addActionListener( new ActionListener(){
		    		@Override
		    		public void actionPerformed(ActionEvent e) {
		    			JPanel sche = new JPanel(new BorderLayout());
		    			JScrollPane tabScroll=new JScrollPane(sche);
		    		    tabScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		    		    tabScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		    		    tabScroll.setFocusable(false);
		    		    tabScroll.setBorder(BorderFactory.createEtchedBorder());
		    			sche.setBackground(backColor);
		    			JPanel top=new JPanel(new GridLayout(0, 1));
		    			Schedule s=schedules.get(index);
		    		    for(Class c:s.keySet()) { //add info to top pane
		    		        JPanel row = new JPanel(new BorderLayout());
		    		        JLabel title=new JLabel(c.info());
		    		        JTextArea info=new JTextArea(c.get(s.get(c)).toString());
		    		        info.setOpaque(false);
		    	            info.setFocusable(false);
		    		        title.setOpaque(false);
		    		        info.setOpaque(false);
		    		        row.setOpaque(false);
		    		        row.add(title);
		    		        row.add(info);
		    		        top.add(row);
		    		    }
		    		    JPanel times=new JPanel(new GridLayout(0,1));
		    		    JPanel daysPane=new JPanel();
	    		        top.setOpaque(false);
		    			String days[]= {"Monday","Tuesday","Wednessday","Thursday","Friday"};
		    			JPanel bot=new JPanel(){
		    				private static final long serialVersionUID = 1L;
		    				@Override
		    				public void paint(Graphics g) {
		    					super.paint(g);
		    					Graphics2D g2 = (Graphics2D) g;
		    					int width=getBounds().width;
		    					int center=(width)/2;
		    					int textWidth = (int) Math.floor((double)(width-200) / 5)-10;
		    			        for (Component comp : daysPane.getComponents()) {
		    			            if (comp instanceof JTextField) {
		    			                comp.setPreferredSize(new Dimension(textWidth, 25));
		    			            }
		    			        }
				    			
				    			daysPane.setBounds(0,0,width,25);
		    			        revalidate();
		    					g2.setColor(textColor);
		    					g2.setStroke(new BasicStroke(2));
		    					g2.drawRect(center-(int)(2.5*textWidth+15),0,5*textWidth+30,1000);
		    					g2.drawRect(center-(int)(1.5*textWidth+7.5),0,3*textWidth+15,1000);
		    					g2.drawRect(center-(int)(0.5*textWidth+2.5),0,textWidth+5,1000);
		    				}
		    			};
		    			bot.setLayout(null);
		    			times.setBounds(75,25,100,1000);
		    			for(int i=8;i<=20;i++) {
    						for(int j=0;j<=3;j++) {
    							JLabel temp=new JLabel(String.format("%02d",i)+":"+String.format("%02d", (j*15)));
    							temp.setAlignmentX(SwingConstants.RIGHT);
    							times.add(temp);
    							
    						}
    					}
		    			times.setOpaque(false);
    					bot.add(times);
		    			for(String day:days) {
		    				JTextField temp=new JTextField();
		    				temp.setText(day);
		    				temp.setPreferredSize(new Dimension(200,25));
		    				temp.setHorizontalAlignment(JTextField.CENTER);
		    				temp.setOpaque(false);
		    				temp.setBorder(BorderFactory.createEmptyBorder());
			                temp.setFocusable(false);
		    				daysPane.add(temp);
		    			}
		    			daysPane.setOpaque(false);
		    			bot.add(daysPane);
		    			bot.setPreferredSize(new Dimension(500,1000));
	    		        bot.setOpaque(false);
		    		    
	    		        sche.add(top,BorderLayout.PAGE_START);
		    		    sche.add(bot,BorderLayout.PAGE_END);
		    		    
		    			JPanel title=new JPanel();
		    			JLabel la = new JLabel("Schedule"+(index+1));
		    			JButton remove = new JButton("x"); 
		    			remove.setUI(new BasicButtonUI());
		    			remove.setToolTipText("close tab");
		                remove.setContentAreaFilled(false);
		                remove.setFocusable(false);
		                remove.setBorder(BorderFactory.createEtchedBorder());
		                remove.setBorderPainted(false);
		                remove.addMouseListener(new MouseAdapter() {
		                    public void mouseEntered(MouseEvent e) {
		                        Component component = e.getComponent();
		                        if (component instanceof AbstractButton) {
		                            AbstractButton button = (AbstractButton) component;
		                            button.setBorderPainted(true);
		                        }
		                    }

		                    public void mouseExited(MouseEvent e) {
		                        Component component = e.getComponent();
		                        if (component instanceof AbstractButton) {
		                            AbstractButton button = (AbstractButton) component;
		                            button.setBorderPainted(false);
		                        }
		                    }
		                });
		                remove.setRolloverEnabled(true);
		    	        remove.addActionListener( 
		    	            new ActionListener() { 
		    	                @Override
		    	                public void actionPerformed(ActionEvent e) {
		    	                    int i = tabs.indexOfTabComponent(title);
		    	                    if (i != -1) {
		    	                    tabs.remove(i);
		    	                    }
		    	                }
		    	        });
		    	        title.add(la);
		    	        title.add(remove);
		    	        title.setOpaque(false);
		    			tabs.addTab("Schedule"+(index+1),new ImageIcon(), tabScroll, schedules.get(index).info1());
		    			tabs.setTabComponentAt(tabs.indexOfComponent(tabScroll), title);
		    		}
		    	});
		    	but.setContentAreaFilled(false);
		        JLabel schedule = new JLabel(index+1+". "+schedules.get(index).info1());
		        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
		        row.add(but);
		        row.add(schedule);
		        //row.setAlignmentX(Component.LEFT_ALIGNMENT);
		        row.setOpaque(false);
		        schedulesPanel.add(row);
		    }
		    schedulesPanel.setBorder(bord);
		    panel.add(schedulesPanel,BorderLayout.PAGE_END);
		    revalidate();
		    repaint();
		}
	});
    classesPanel.add(generate);
    revalidate();
    repaint();
}
public String getPossibleSchedules(){
	String out="";
	schedules=new ArrayList<Schedule>();
    possibleSchedules(new Schedule(),0);
    out+=("FOUND: "+schedules.size()+" schedules\n");
    for(int i=0;i<schedules.size();i++) {
    	out+=(i+1+"\n"+schedules.get(i).info());
    }
    return out;
}
class Tab extends JPanel{
	public Tab(int index) {
	}
}
class TimeBlock{
	String day;
	int startMin, startHour;
	int endMin, endHour;
	String room;
	public TimeBlock(){}
	public TimeBlock(String d, String s, String e, String r) {
		day=d;
		//12:00am
		startHour=Integer.valueOf(s.substring(0,s.length()-5));
		if (startHour==12) {
			startHour=0;
		}
		startMin=Integer.valueOf(s.substring(s.length()-4,s.length()-2));
		if (s.substring(s.length()-2).equals("pm")) {
			startHour+=12;
		}
		endHour=Integer.valueOf(e.substring(0,e.length()-5));
		if (endHour==12) {
			endHour=0;
		}
		endMin=Integer.valueOf(e.substring(e.length()-4,e.length()-2));
		if (e.substring(e.length()-2).equals("pm")) {
			endHour+=12;
		}
		room=r;
	}
	public TimeBlock(String d, String s, String e) {
		this(d,s,e,"");
	}
	public String toString() {
		return day+" "+String.format("%02d",startHour)+":"+String.format("%02d",startMin)+"-"+String.format("%02d",endHour)+":"+String.format("%02d",endMin)+" "+room;
	}
	public boolean overlaps(TimeBlock other) {
		double thisStart=this.startHour+(double)this.startMin/60;
		double otherStart=other.startHour+(double)other.startMin/60;
		double thisEnd=this.endHour+(double)this.endMin/60;
		double otherEnd=other.endHour+(double)other.endMin/60;
		//System.out.println(thisStart+"-"+thisEnd+" and "+otherStart+"-"+otherEnd);
		if(this.day.equals(other.day)&&((thisStart<=otherEnd&&thisStart>=otherStart)||(thisEnd<=otherEnd&&thisEnd>=otherStart)||
				(otherStart<=thisEnd&&otherStart>=thisStart)||(otherEnd<=thisEnd&&otherEnd>=thisStart)||
				thisStart==otherStart||thisEnd==otherEnd)) {
			return true;
		}
		return false;
	}
}
class Block extends ArrayList<TimeBlock>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int section;
	String type;
	int seats;
	String intructor="";
	String note="";
	public Block() {}
	public Block(int s, String t, int a, String i) {
		section=s;
		type=t;
		seats=a;
		intructor=i;
	}
	public void section(int s) {
		section=s;
	}
	public void type(String t) {
		type=t;
	}
	public void seats(int s) {
		seats=s;
	}
	public void intructor(String i) {
		intructor+=i;
	}
	public void note(String n) {
		note=n;
	}
	public String toString() {
		String out=section+" "+type+" "+seats+" "+intructor;
		for(TimeBlock t:this) {
			out+="\n"+t;
		}
		return out+ ((note.equals(""))? "":"\n"+note);
	}
	public boolean overlaps(Block other) {
		for(TimeBlock i:this) {
			for(TimeBlock j:other) {
				if(i.overlaps(j)) {
					return true;
				}
			}
		}
		return false;
	}
}
class Class extends ArrayList<Block>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String group;
	String number;
	String name;
	public Class(String n) {
		group=n.substring(0,3);
		number=n.substring(3);
	}
	public String toString() {
		String out=group+number+"\n";
		for(Block b:this) {
			out+=b+"\n\n";
		}
		return out;
	}
	public String info() {
		return group+number;
	}
	public boolean blockOverlaps(Class other, int thisBlock,int otherBlock) {
		return this.get(thisBlock).overlaps(other.get(otherBlock));
	}
}
class Schedule extends HashMap<Class,Integer>{
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public boolean isValid() {
		for(Class c:this.keySet()) {
			for (Class cl:this.keySet()) {
				if(c.equals(cl)) {continue;}
				if(c.blockOverlaps(cl,this.get(c),this.get(cl))) {
					return false;
				}
			}
		}
		return true;
	}
	public String info() {
		String out="";
		for(Class c:this.keySet()) {
			out+=c.info()+" - "+c.get(this.get(c))+"\n";
		}
		return out;
	}
	public String info1() {
		String out="";
		for(Class c:this.keySet()) {
			out+=c.info()+"-"+String.format("%04d",c.get(this.get(c)).section)+", ";
		}
		return out.substring(0,out.length()-2);
	}
	public String toString() {
		String out="";
		for(Class c:this.keySet()) {
			out+=c+"\n"+c.get(this.get(c));
		}
		return out;
	}
}
public void possibleSchedules(Schedule s, int i){
	if(s.isValid()){
		if(i==classes.size()) {
			Schedule newSchedule = new Schedule();
		    newSchedule.putAll(s);
		    schedules.add(newSchedule);
		    //System.out.println("I WORK!"+schedules.size()+"\n"+schedules.get(schedules.size()-1).info());
		}
		else{
			for(int j=0;j<classes.get(i).size();j++){
				s.put(classes.get(i),j);
				possibleSchedules(s,i+1);
				s.remove(classes.get(i));
			}
		}
	}
}
private ArrayList<Class> toList(File f) throws FileNotFoundException {
Scanner scan=new Scanner(f);
ArrayList<Class> out= new ArrayList<Class>();
int sequence = 0;
String temp=scan.next();
while(scan.hasNext()) {
	//System.out.println(sequence+". "+temp);
	if(temp.equals("Special")) {
		out.get(out.size()-1).get(out.get(out.size()-1).size()-1).note(temp+" "+scan.nextLine().trim());
		if(scan.hasNext()) {temp=scan.next();}
		//System.out.println("FoundNote: "+out.get(out.size()-1).get(out.get(out.size()-1).size()-1).note);
	}
	if(temp.equals("Reserve")) {
		out.get(out.size()-1).get(out.get(out.size()-1).size()-1).note(temp+" "+scan.nextLine().trim()+" "+scan.nextLine().trim());
		if(scan.hasNext()) {temp=scan.next();}
		//System.out.println("FoundNote: "+out.get(out.size()-1).get(out.get(out.size()-1).size()-1).note);
	}
	else if (temp.matches(infoTypes.get(sequence))) {
		//System.out.println("BeforeSwitch"+sequence);
		switch (sequence) {
		case 0: out.add(new Class(temp)); temp=scan.next();sequence++;break;
		case 1: 
			out.get(out.size()-1).add(new Block());
			out.get(out.size()-1).get(out.get(out.size()-1).size()-1).section(Integer.valueOf(temp));
			temp=scan.next();sequence++; break;
		case 2: 
			out.get(out.size()-1).get(out.get(out.size()-1).size()-1).type(temp);
			temp=scan.next(); sequence++;break;
		case 3: 
			out.get(out.size()-1).get(out.get(out.size()-1).size()-1).seats(Integer.valueOf(temp));
			temp=scan.next(); sequence++;break;
		case 4: 
			if (temp.matches("(Th|M|T|W|F)+|Online")) {sequence++;continue;}
			out.get(out.size()-1).get(out.get(out.size()-1).size()-1).intructor(temp);
			temp=scan.next(); break;
		case 5: 
			String day=temp;
			Pattern pattern = Pattern.compile("Th|M|T|W|F");
			Matcher matcher = pattern.matcher(day);
			String start=scan.next();
			scan.next();
			String end=scan.next();
			temp=scan.next();
			if (temp.equals("-")) {
				String building=scan.next();
				String roomNum=scan.next();
				while (matcher.find())
				{
					int e=matcher.start();
					//System.out.println(e+day.substring(e,e+1)+start+end+building+roomNum);
					if (e+2<=day.length()&&day.substring(e,e+2).equals("Th")) {
						out.get(out.size()-1).get(out.get(out.size()-1).size()-1).add(new TimeBlock("Th",start,end,building+roomNum));
					}
					else {
						out.get(out.size()-1).get(out.get(out.size()-1).size()-1).add(new TimeBlock(day.substring(e,e+1),start,end,building+roomNum));
					}
				}
				temp=scan.next();
			}
			else {
				while (matcher.find())
				{
					int e=matcher.start();
					//System.out.println(e+day.substring(e,e+1)+start+end);
					if (e+2<=day.length()&&day.substring(e,e+2).equals("Th")) {
						out.get(out.size()-1).get(out.get(out.size()-1).size()-1).add(new TimeBlock("Th",start,end));
					}
					else {
						out.get(out.size()-1).get(out.get(out.size()-1).size()-1).add(new TimeBlock(day.substring(e,e+1),start,end));
					}
				}
			}
			break;
		case 6: 
			temp=scan.next(); break;
		case 7: 
			temp=scan.next(); break;
		case 8: 
			temp=scan.next(); sequence=0;break;
		}
	}
	else if(temp.matches(infoTypes.get(sequence+1))) {
		sequence++;
	}
	else if(temp.matches(infoTypes.get(sequence-1))) {
		sequence--;
	}
	else{
		System.out.println("ERROR: No match in range, invalid formatting");
		System.exit(0);
	}
}
return out;
}

}
