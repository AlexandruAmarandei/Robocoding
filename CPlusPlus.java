package robot;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;
import robocode.*;

import robocode.RobotStatus;
import robocode.util.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html
/**
 * C - a robot by (your name here)
 */
public class CPlusPlus extends AdvancedRobot {

    /**
     * run: C's default behavior
     */
    private static ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Point> possiblePoints = new ArrayList<>();
    private boolean moving = true, gettingNewMove = false, skirmishing = true, rotation = true;
    private double moveX = 0, moveY = 0;
    private int highestThreat = 0;
    private double h, w;
    private double firePower = 3;
    private double startingThreatLvl, wallForce;

    public void run() {

        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);
        h = getBattleFieldHeight();
        w = getBattleFieldWidth();
        startingThreatLvl = h * w * 75;

        wallForce = startingThreatLvl / getOthers();
        goToNewPosition();
        while (true) {
            getOutOfCorner();
            if (getGunHeat() == 0 && getGunTurnRemaining() < 3) {
                fireBullet(firePower);
                goToNewPosition();
            }
            skirmisMostDangerousTarget();

        }

    }

    public boolean checkIfThisIsInCorner() {
        double x = getX();
        double y = getY();
        Random rand = new Random();
        int r = rand.nextInt(4) + 1;
        if (x < 25 && y < 25) {
            if (r % 2 == 0) {
                moveToLocation(25, 100);
            } else {
                moveToLocation(100, 25);
            }
            return true;
        }
        if (x < 25 && y > h - 25) {
            if (r % 2 == 0) {
                moveToLocation(25, h - 100);
            } else {
                moveToLocation(100, h - 25);
            }
            return true;
        }
        if (x > w - 25 && y > h - 25) {
            if (r % 2 == 0) {
                moveToLocation(w - 25, h - 100);
            } else {
                moveToLocation(w - 100, h - 25);
            }
            return true;
        }

        if (x > w - 25 && y < 25) {
            if (r % 2 == 0) {
                moveToLocation(w - 25, 100);
            } else {
                moveToLocation(w - 100, 25);
            }
            return true;
        }
        return false;

    }

    public void getOutOfCorner() {
        checkIfThisIsInCorner();
    }

    public void scanForHighestThread() {
        noscope();

    }

    public Point2D.Double[] createPoints() {
        Point2D.Double[] points = new Point2D.Double[40];
        for (int i = 0; i < 40; i++) {
            points[i] = null;
        }
        Enemy target = enemies.get(highestThreat);
        double heading = target.e.getHeading();
        double radians = Math.toRadians(heading);
        double factX = 0, factY = 0;
        double dx, dy;
        if (heading == 0) {
            factX = 0;
            factY = 1;
        }
        if (heading == 90) {
            factX = 1;
            factY = 0;
        }
        if (heading == 180) {
            factX = 0;
            factY = -1;
        }
        if (heading == 270) {
            factX = -1;
            factY = 0;
        }

        if (heading > 0 && heading < 90) {
            factX = Math.sin(radians);
            factY = Math.cos(radians);
        }

        if (heading > 90 && heading < 180) {
            factX = Math.cos(Math.toRadians(heading - 90));
            factY = -Math.sin(Math.toRadians(heading - 90));
        }
        if (heading > 180 && heading < 270) {
            factX = -Math.sin(Math.toRadians(heading - 180));
            factY = -Math.cos(Math.toRadians(heading - 180));
        }
        if (heading > 270 && heading < 360) {
            factX = -Math.cos(Math.toRadians(heading - 270));
            factY = Math.sin(Math.toRadians(heading - 270));
        }
        out.println(heading + " " + factX + " " + factY);
        double currentDistance = 0;
        for (int i = 0; i < 40; i++) {
            currentDistance = i * target.e.getVelocity();
            dx = factX * currentDistance;
            dy = factY * currentDistance;
            points[i] = new Point2D.Double(dx + target.x, dy + target.y);
            out.println((dx + target.x) + " " + (dy + target.y));

        }
        return points;
    }

    public void skirmisMostDangerousTarget() {
        skirmishing = true;
        //setTurnRadarRight(360);
        scanForHighestThread();
        setTurnRight(enemies.get(highestThreat).e.getBearing() - getHeading() + 90);

    }

    public double reduceRadians(double angle) {
        return (angle - Math.PI * 2 * Math.floor((angle + Math.PI) / (Math.PI * 2)));
    }

    public void shootHighestThread() {
        //lock on this target until i get shot
        double finalAngle;
        int timeForTurn;
        firePower = 1;
        double turnAngle = 0;
        int bestDifference = 1000000, shootingTime;
        double heat = getGunHeat();
        double timeToCool = heat / getGunCoolingRate();
        boolean movement = true;
        if (skirmishing) {

            Point2D.Double points[] = createPoints();
            out.println(points.length + " " + points[0]);
            for (int time = 0; time < 40 && points[time] != null && getGunTurnRemaining() == 0; time++) {

                double angle;

                double print = Math.atan2(points[time].getX() - getX(), points[time].getY() - getY());
                angle = Math.toDegrees(print);
                //out.println(print+ "  " + points[time].getX() + " "+ getX()+ " " + points[time].getY() + " " + getY());
                angle = getGunHeading() - angle;
                angle = normalRelativeAngleDegrees(angle);

                out.println(angle);
                if (angle < 0) {
                    finalAngle = -angle;
                } else {
                    finalAngle = angle;
                }
                timeForTurn = (int) finalAngle / 20;
                if (timeForTurn * 20 != finalAngle) {
                    timeForTurn++;
                }

                double distance = points[time].distance(getX(), getY());
                if (distance < 300) {
                    firePower = 3;
                } else {
                    firePower = 2;
                }
                double timeForBullet = distance / (20 - 3 * firePower);
                timeForTurn += (int) timeForBullet;
                timeForTurn++;
                out.println("Time:" + time + " " + timeForBullet + " " + angle);
                if (timeForTurn < time) {
                    if (time - timeForTurn < bestDifference) {
                        bestDifference = timeForTurn - time;
                        shootingTime = time;
                        turnAngle = angle;
                    }
                } else if (timeForTurn - time < bestDifference) {
                    bestDifference = timeForTurn - time;
                    shootingTime = time;
                    turnAngle = angle;
                }

                // }
            }

        }

        setTurnGunLeft(turnAngle);
       
    }

    public void goToNewPosition() {
        skirmishing = false;
        if (gettingNewMove == true) {

        } else {
            gettingNewMove = true;
            moveX = 0;
            moveY = 0;
            noscope();
        }
        moveX += wallForce / Math.pow(distance(getX(), getY(), w, getY()), 2);
        moveX -= wallForce / Math.pow(distance(getX(), getY(), 0, getY()), 2);
        moveY += wallForce / Math.pow(distance(getX(), getY(), getX(), h), 2);
        moveY -= wallForce / Math.pow(distance(getX(), getY(), getX(), 0), 2);
        out.println("going to" + (getX() - moveX) + " " + (getY() - moveY));
        double newX = getX() - moveX;
        double newY = getY() - moveY;
        if (newX > w - 24) {
            newX = w - 24.0;
        }
        if (newX < 24) {
            newX = 24.0;
        }
        if (newY > h - 24) {
            newY = h - 24.0;

        }
        if (newY < 24) {
            newY = 24.0;
        }
        moveToLocation(newX, newY);
        gettingNewMove = false;

    }

    public double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public double getAngleTo(double x, double y) {
        double angle = Math.atan2(x, y);
        double a = angle - getHeadingRadians();
        double angleX = Utils.normalRelativeAngle(a);
        return angleX;
    }

    public void moveToLocation(double x, double y) {
        x = x - getX();
        y = y - getY();

        double d = Math.hypot(x, y);
        double angleX = getAngleTo(x, y);
        double angle2 = Math.atan(Math.tan(angleX));
        turnRightRadians(angle2);

        if (angleX == angle2) {
            setAhead(d);
        } else {
            setBack(d);
        }

    }

    public int findNameInArray(String name) {
        for (int index = 0; index < enemies.size(); index++) {
            if (enemies.get(index).getName().equals(name)) {
                return index;
            }
        }
        return -1;
    }

    public void addRobotThreat(ScannedRobotEvent e) {

        int pos = findNameInArray(e.getName());
        double x = enemies.get(pos).x;
        double y = enemies.get(pos).y;
        double threat = (double) enemies.get(pos).threadLevel;
        double distance = e.getDistance();
        double antiGrav = threat / (distance * distance);
        double angle;
        angle = reduceRadians(Math.atan2(getY() - y, getX() - x));
        out.println(e.getName() + " " + x + " " + y);

        moveX = moveX - Math.sin(angle) * antiGrav;
        out.println(e.getName() + " " + moveX);

        moveY = moveY - Math.cos(angle) * antiGrav;
        out.println(e.getName() + " " + moveY);

    }

    public int addRobotToArray(ScannedRobotEvent e) {
        enemies.add(new Enemy(e, startingThreatLvl));
        enemies.get(enemies.size() - 1).calculateXY(getHeading(), getX(), getY());
        return enemies.size() - 1;
    }

    public void updateRobotArray(ScannedRobotEvent e, int pos) {
        double t = enemies.get(pos).threadLevel;
        enemies.set(pos, new Enemy(e, t));
        enemies.get(pos).calculateXY(getHeading(), getX(), getY());
    }

    public void noscope() {
        rotation = true;
        turnRadarRight(360);
        rotation = false;
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        int pos = findNameInArray(e.getName());
        if (pos == -1) {
            pos = addRobotToArray(e);
        } else {
            double energyDif = enemies.get(pos).e.getEnergy() - e.getEnergy();
            
            updateRobotArray(e, pos);
        }
        if (highestThreat == -1) {
            highestThreat = pos;
        }
        if (e.getDistance() < enemies.get(highestThreat).e.getDistance() - 30 && skirmishing == false) {
            highestThreat = pos;
        }
        if (pos == highestThreat) {

            shootHighestThread();

        }
        if (gettingNewMove == true) {
            addRobotThreat(e);
        }

    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        int pos = findNameInArray(e.getName());
        enemies.get(pos).threadLevel *= 1.001;
        turnRadarRight(normalRelativeAngleDegrees(e.getBearing() + getHeading() - getRadarHeading()));
        double angle = e.getHeading();
        
        if (angle >= 0 && angle <= 90) {
            moveToLocation(getX() - 30, getY() + 30);
        }

        if (angle > 90 && angle <= 180) {
            moveToLocation(getX() + 30, getY() - 30);
        }
        if (angle > 180 && angle <= 270) {
            moveToLocation(getX() - 30, getY() + 30);
        }
        if (angle > 270 && angle <= 360) {
            moveToLocation(getX() + 30, getY() + 30);
        }
        
        goToNewPosition();
    }

    public void onBulletHit(BulletHitEvent e) {
        int pos = findNameInArray(e.getName());
        enemies.get(pos).threadLevel *= 9.999;
        goToNewPosition();
    }

    public void onHitRobot(HitRobotEvent e) {
        if (e.isMyFault() == false) {
            int pos = findNameInArray(e.getName());
            enemies.get(pos).threadLevel *= 1.05;
            goToNewPosition();
        }
        turnRadarRight(normalRelativeAngleDegrees(e.getBearing() + getHeading() - getRadarHeading()));
    }

    public void onHitWall(HitWallEvent e) {

        goToNewPosition();
    }

    public void onDeath(DeathEvent e) {
        enemies.get(highestThreat).threadLevel = enemies.get(highestThreat).threadLevel * 101 / 100;
        // update values to get better results
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (enemies.get(highestThreat).getName().equals(e.getName())) {
            highestThreat = -1;
        }
        wallForce = startingThreatLvl / getOthers();
    }
}
