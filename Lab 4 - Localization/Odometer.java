package Lab4;

/*
 * Class which controls the odometer for the robot
 * 
 * Odometer defines cooridinate system as such...
 * 
 *      90Deg:pos y-axis
 *        |
 *        |
 *        |
 *        |
 * 180Deg:neg x-axis------------------0Deg:pos x-axis
 *        |
 *        |
 *        |
 *        |
 *      270Deg:neg y-axis
 * 
 * The odometer is initalized to 90 degrees, assuming the robot is facing up the positive y-axis
 * 
 */

import lejos.utility.Timer;
import lejos.utility.TimerListener;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer implements TimerListener {

 private Timer timer;
 private EV3LargeRegulatedMotor leftMotor, rightMotor;
 private final int DEFAULT_TIMEOUT_PERIOD = 20;
 private double leftRadius, rightRadius, width;
 private double x, y, theta;
 private double[] oldDH, dDH;
 
 // constructor
 public Odometer (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int INTERVAL, boolean autostart) {
  
  this.leftMotor = leftMotor;
  this.rightMotor = rightMotor;
  
  // default values, modify for your robot
  this.rightRadius = 2.75;
  this.leftRadius = 2.75;
  this.width = 15.8;
  
  this.x = 0.0;
  this.y = 0.0;
  this.theta = 90.0;
  this.oldDH = new double[2];
  this.dDH = new double[2];

  if (autostart) {
   // if the timeout interval is given as <= 0, default to 20ms timeout 
   this.timer = new Timer((INTERVAL <= 0) ? INTERVAL : DEFAULT_TIMEOUT_PERIOD, this);
   this.timer.start();
  } else
   this.timer = null;
 }
 
 // functions to start/stop the timerlistener
 public void stop() {
  if (this.timer != null)
   this.timer.stop();
 }
 public void start() {
  if (this.timer != null)
   this.timer.start();
 }
 
 
  // Calculates displacement and heading as title suggests
  
 private void getDisplacementAndHeading(double[] data) {
  int leftTacho, rightTacho;
  leftTacho = leftMotor.getTachoCount();
  rightTacho = rightMotor.getTachoCount();

  data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) * Math.PI / 360.0;
  data[1] = (rightTacho * rightRadius - leftTacho * leftRadius) / width;
 }
 
 
  // Recompute the odometer values using the displacement and heading changes
  
 public void timedOut() {
  this.getDisplacementAndHeading(dDH);
  dDH[0] -= oldDH[0];
  dDH[1] -= oldDH[1];

  // Update the position in a critical region
  synchronized (this) {
   theta += dDH[1];
   theta = fixDegAngle(theta);

   x += dDH[0] * Math.cos(Math.toRadians(theta));
   y += dDH[0] * Math.sin(Math.toRadians(theta));
  }

  oldDH[0] += dDH[0];
  oldDH[1] += dDH[1];
 }

 // Return X value
 public double getX() {
  synchronized (this) {
   return x;
  }
 }

 // Return Y value
 public double getY() {
  synchronized (this) {
   return y;
  }
 }

 // Return theta value
 public double getAng() {
  synchronized (this) {
   return theta;
  }
 }

 // Set x,y,theta
 public void setPosition(double[] position, boolean[] update) {
  synchronized (this) {
   if (update[0])
    x = position[0];
   if (update[1])
    y = position[1];
   if (update[2])
    theta = position[2];
  }
 }

 // Return x,y,theta
 public void getPosition(double[] position) {
  synchronized (this) {
   position[0] = x;
   position[1] = y;
   position[2] = theta;
  }
 }

 public double[] getPosition() {
  synchronized (this) {
   return new double[] { x, y, theta };
  }
 }
 
 // Accessors to motors
 public EV3LargeRegulatedMotor [] getMotors() {
  return new EV3LargeRegulatedMotor[] {this.leftMotor, this.rightMotor};
 }
 public EV3LargeRegulatedMotor getLeftMotor() {
  return this.leftMotor;
 }
 public EV3LargeRegulatedMotor getRightMotor() {
  return this.rightMotor;
 }

 // Static 'helper' methods
 public static double fixDegAngle(double angle) {
  if (angle < 0.0)
   angle = 360.0 + (angle % 360.0);

  return angle % 360.0;
 }

 public static double minimumAngleFromTo(double a, double b) {
  double d = fixDegAngle(b - a);

  if (d < 180.0)
   return d;
  else
   return d - 360.0;
 }
}
