/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.GamepadBase;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the RobotDrive class, specifically
 * it contains the code necessary to operate a robot with tank drive.
 */
public class Robot extends TimedRobot {

  SpeedControllerGroup leftTrain = new SpeedControllerGroup(new Spark(0), new Spark(1));
  SpeedControllerGroup rightTrain = new SpeedControllerGroup(new Spark(2), new Spark(3));
  private DifferentialDrive drivetrain;

  private VictorSP intake = new VictorSP(4);
  private VictorSP launcher = new VictorSP(5);

  //Controllers
  private Joystick stick;
  private LogitechGamepadController gamepad;

  //SendableChooser for Drive controller
  private static final String kFlightStickDrive = "Flight Stick Drive";
  private static final String kGamePadArcadeDrive = "Game Pad Arcade Drive";
  private static final String kGamePadTankDrive = "Game Pad Tank Drive";
  private static final String kGamePadStickDrive = "Game Pad Stick Drive";
  private String m_DriveSelected;
  private final SendableChooser<String> driveChooser = new SendableChooser<>();

  //SendableChooser for Operate controller
  private static final String kFlightStickOperate = "Flight Stick Operate";
  private static final String kGamePadOperate = "Game Pad Operate";
  private String m_OperateSelected;
  private final SendableChooser<String> operateChooser = new SendableChooser<>();

  //SendableChooser for Side preference
  private static final String kLeftPreference = "Left";
  private static final String kRightPreference = "Right";
  private String m_SidePreference;
  private final SendableChooser<String> SideChooser = new SendableChooser<>();

  private double rotateMultiplier = 0.6;
  private double speedMultiplier = 0.85;

  @Override
  public void robotInit() {
    drivetrain = new DifferentialDrive(leftTrain, rightTrain);
    drivetrain.setDeadband(0.05);

    stick = new Joystick(0);
    gamepad = new LogitechGamepadController(1);

    CameraServer.getInstance().startAutomaticCapture();

    driveChooser.setDefaultOption(kFlightStickDrive, kFlightStickDrive);
    driveChooser.addOption(kGamePadArcadeDrive, kGamePadArcadeDrive);
    driveChooser.addOption(kGamePadTankDrive, kGamePadTankDrive);
    driveChooser.addOption(kGamePadStickDrive, kGamePadStickDrive);
    SmartDashboard.putData("Drive Choice", driveChooser);

    operateChooser.setDefaultOption(kFlightStickOperate, kFlightStickOperate);
    operateChooser.addOption(kGamePadOperate, kGamePadOperate);
    SmartDashboard.putData("Operate Choice", operateChooser);

    SideChooser.addOption(kLeftPreference, kLeftPreference);
    SideChooser.setDefaultOption(kRightPreference, kRightPreference);
    SmartDashboard.putData("Side Choice", SideChooser);

    SmartDashboard.putNumber("speed multiplier", speedMultiplier);
    SmartDashboard.putNumber("rotate multiplier", rotateMultiplier);
  }

  @Override
  public void robotPeriodic() {
    rotateMultiplier = SmartDashboard.getNumber("rotate multiplier", 1);
    speedMultiplier = SmartDashboard.getNumber("speed multiplier", 1);
    SmartDashboard.putNumber("total", rotateMultiplier + speedMultiplier);
  }

  @Override
  public void teleopPeriodic() {
    m_DriveSelected = driveChooser.getSelected();
    m_OperateSelected = operateChooser.getSelected();
    m_SidePreference = SideChooser.getSelected();

    //Operate
    if (m_OperateSelected == kFlightStickOperate){
      if (stick.getRawButton(2)){
        intake.set(-0.9);
      } else {
        intake.set(0.0);
      }

      if (stick.getRawButton(1)){
        launcher.set(-0.85);
      } else if(stick.getRawButton(11)){
        launcher.set(0.8);
      } else {
        launcher.set(0.0);
      }
    } else {
      if (gamepad.getLeftBumper()){
        intake.set(-0.9);
      } else {
        intake.set(0.0);
      }

      if (gamepad.getRightBumper()){
        launcher.set(-0.85);
      } else if(gamepad.getXButton()){
        launcher.set(0.8);
      } else {
        launcher.set(0.0);
      }

    }

    //Drive
    switch (m_DriveSelected) {
      case kFlightStickDrive:
        if (m_SidePreference == kLeftPreference) {
          arcade(stick.getY(), stick.getZ());
        } else if (m_SidePreference == kRightPreference){
          arcade(stick.getY(), stick.getX());
        }
        break;

      case kGamePadArcadeDrive:
      if (m_SidePreference == kLeftPreference) {
        arcade(gamepad.getLeftY(), gamepad.getRightX());
      } else if (m_SidePreference == kRightPreference){
        arcade(gamepad.getRightY(), gamepad.getLeftX());
      }
        break;

      case kGamePadTankDrive:
        tank(gamepad.getLeftY(), gamepad.getRightY());
        break;

      case kGamePadStickDrive:
      if (m_SidePreference == kLeftPreference) {
        arcade(gamepad.getLeftY(), gamepad.getLeftX());
      } else if (m_SidePreference == kRightPreference){
        arcade(gamepad.getRightY(), gamepad.getRightX());
      }
        break;

      default:
        drivetrain.stopMotor();
        break;

    }
  }

  private void arcade(double speed, double rotation) {
    double outSpeed = - speedMultiplier * speed;

    // double outRotation = rotation * speed * rotateMultiplier;
    double outRotation = rotation * rotateMultiplier;

    drivetrain.arcadeDrive(outSpeed, outRotation, true);
  }

  private void tank(double left, double right) {
    double outLeft = - left * speedMultiplier;
    double outRight = - right * speedMultiplier;

    drivetrain.tankDrive(outLeft, outRight, true);
  }

}
