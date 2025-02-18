package frc.robot.subsystems.mechanisms.elevator;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.function.DoubleSupplier;

import org.littletonrobotics.junction.AutoLogOutput;
import org.littletonrobotics.junction.Logger;

import frc.robot.Constants;
import frc.robot.subsystems.mechanisms.MechanismConstants.ElevatorConstants;

public class Elevator extends SubsystemBase {
  private final ElevatorIO io;
  private final ElevatorIOInputsAutoLogged inputs = new ElevatorIOInputsAutoLogged();

  public Elevator(ElevatorIO io) {
    //  System.out.println("[Init] Creating Elevator");
    this.io = io;
    if (Constants.usePIDtuning) {
      SmartDashboard.putData("Elevator/lowerTestCommand", testLowerPosition());
      SmartDashboard.putData("Elevator/upperTestCommand", testUpperPosition());
    }
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("TestArmAngle", 22.5);
    io.updateInputs(inputs);
    Logger.processInputs("Elevator", inputs);

    if (DriverStation.isDisabled()) {
      stop();
    } else {

    }
  }

  public void setElevatorPosition(double ElevatorPosition) {
    io.setElevatorPosition(ElevatorPosition);
  }

  private void stop() {
    io.stop();
  }

  private void voltageControl(double voltage) {
    io.voltageControl(voltage);
  }

  private void positionControl(double targetPostion) {
    inputs.setPoint = targetPostion;
    io.positionControl(targetPostion);
  }

  public Command testPositionControl() {
    return this.runOnce(
      () -> {
        positionControl(39);
      }
    );
  }

  public Command homeCommand() {
    return this.run(
      () -> {
        positionControl(0.0);
      }
    );
  }

  public Command manualRunCommand(DoubleSupplier controllerInput) {
    return this.run(
      () -> {
        voltageControl(controllerInput.getAsDouble() * -6.0);
      }
    ).withName("Maual Run Command");
  }

  public Command stopCommand() {
    return this.runOnce(this::stop);
  }

  public Command holdPosition() {
    return this.run(
      () -> {
        voltageControl(0.0);
      });
  }

  public Command testLowerPosition() {
    return this.defer(
      () -> Commands.runOnce(
        () -> positionControl(
          SmartDashboard.getNumber("Elevator/lowerSetpoint", 
          0.0))));
  }

  public Command testUpperPosition() {
    return this.defer(
      () -> Commands.runOnce(
        () -> positionControl(
          SmartDashboard.getNumber("Elevator/upperSetpoint", 
          0.0))));
  }

  public boolean isHomed(){
    return inputs.zeroed;
  }

  @AutoLogOutput
  public double getCarrageHeight() {
    return inputs.elevatorPos * ElevatorConstants.conversion_Rot_M * 3.0;
  }

  @AutoLogOutput
  public double get2ndStageHeight() {
    return inputs.elevatorPos * ElevatorConstants.conversion_Rot_M * 2.0 - Units.inchesToMeters(1.0);
  }

  @AutoLogOutput
  public double get1stStageHeight() {
    return inputs.elevatorPos * ElevatorConstants.conversion_Rot_M - Units.inchesToMeters(2.0);
  }

  @AutoLogOutput
  public double getCarrageVelocity() {
    return inputs.elevatorVelo * ElevatorConstants.conversion_RPM_MS * 4.0;
  }
    
}
