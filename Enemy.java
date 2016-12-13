
package robot;

import robocode.ScannedRobotEvent;


public class Enemy {
    public ScannedRobotEvent e;
    public double threadLevel,x,y;
    public Enemy(ScannedRobotEvent e, double threadLevel){
        this.e = e;
        this.threadLevel = threadLevel;
        
    }
    public void calculateXY(double heading, double cx, double cy){
       double b = e.getBearing();
       double a = Math.toRadians(heading + b % 360);
       x = cx + Math.sin(a) *e.getDistance();
       y = cy + Math.cos(a) *e.getDistance();
    }
    public String getName(){
        if(e!=null){
            return e.getName();
        }
        return "";
    }
}
