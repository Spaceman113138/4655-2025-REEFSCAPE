package frc.robot.subsystems.mechanisms.elevator;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController.ArbFFUnits;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLimitSwitch;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.ClosedLoopConfig;
import com.revrobotics.spark.config.EncoderConfig;
import com.revrobotics.spark.config.MAXMotionConfig;
import com.revrobotics.spark.config.SoftLimitConfig;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.ClosedLoopConfig.FeedbackSensor;
import com.revrobotics.spark.config.MAXMotionConfig.MAXMotionPositionMode;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.math.controller.ElevatorFeedforward;
import frc.robot.subsystems.mechanisms.MechanismConstants;

import frc.robot.subsystems.mechanisms.MechanismConstants.ElevatorConstants;

public class ElevatorIOSparkMax implements ElevatorIO {

  private SparkMax m_funnel;
  private SparkMax m_belt;

  private SparkClosedLoopController funnelController;
  private SparkClosedLoopController beltController;

  private RelativeEncoder funnelEncoder;
  private SparkLimitSwitch limitSwitch;
  
  private SparkMax m_leftElevator = new SparkMax(MechanismConstants.leftElevatorId, MotorType.kBrushless);
  private SparkMax m_rightElevator = new SparkMax(MechanismConstants.rightElevatorId, MotorType.kBrushless);
  private SparkClosedLoopController leftElevatorController = m_leftElevator.getClosedLoopController();
  private RelativeEncoder leftEncoder = m_leftElevator.getEncoder();
  private SparkLimitSwitch bottomLimitSwitch = m_leftElevator.getReverseLimitSwitch();
  private static boolean zeroed;
  private SparkMaxConfig mLeftConfig = new SparkMaxConfig();
  private SparkMaxConfig mRightConfig = new SparkMaxConfig();

  private ElevatorFeedforward feedforward = new ElevatorFeedforward(
                                            ElevatorConstants.ks,
                                            ElevatorConstants.kg,
                                            0.0);


  public ElevatorIOSparkMax() {
    //base config for all motors
    mLeftConfig.idleMode(IdleMode.kBrake)
                .inverted(false)
                .smartCurrentLimit(60);
    
    //Create spesific right motor config from base config
    mRightConfig.apply(mLeftConfig);
    mRightConfig.follow(MechanismConstants.leftElevatorId);

    
    m_leftElevator = new SparkMax(MechanismConstants.leftElevatorId, MotorType.kBrushless);
    m_rightElevator = new SparkMax(MechanismConstants.rightElevatorId, MotorType.kBrushless);
    m_funnel = new SparkMax(MechanismConstants.funnelId, MotorType.kBrushless);
    m_belt = new SparkMax(MechanismConstants.funnelId, MotorType.kBrushed);


    //Adjust left motor encoder config
    EncoderConfig encoderConfig = mLeftConfig.encoder;
    encoderConfig.inverted(false);

    //Adjust left motor closed loop (pid controller) config
    ClosedLoopConfig closedLoopConfig = mLeftConfig.closedLoop;
    closedLoopConfig.pid(ElevatorConstants.kp, 
                          ElevatorConstants.ki, 
                          ElevatorConstants.kd)
                    .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
                    .positionWrappingEnabled(false);

    //Adjust left motor max motion position specific config
    MAXMotionConfig maxMotionConfig = mLeftConfig.closedLoop.maxMotion;
    maxMotionConfig.positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal)
                    .allowedClosedLoopError(1)
                    .maxAcceleration(100)
                    .maxVelocity(50);
    
    SoftLimitConfig softLimitConfig = mLeftConfig.softLimit;
    softLimitConfig.forwardSoftLimit(ElevatorConstants.maxHeight)
                    .forwardSoftLimitEnabled(true);
                    
    //Configure both motors
    m_leftElevator.configure(
        mLeftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    m_rightElevator.configure(
        mRightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    leftEncoder = m_leftElevator.getEncoder();
    funnelEncoder = m_funnel.getEncoder();

    limitSwitch = m_leftElevator.getReverseLimitSwitch();

    leftElevatorController = m_leftElevator.getClosedLoopController();
    funnelController = m_funnel.getClosedLoopController();
    beltController = m_belt.getClosedLoopController();
  }

  @Override
  public void updateInputs(ElevatorIOInputs inputs) {
    inputs.dutyCycle = m_leftElevator.getAppliedOutput();
    inputs.appliedCurrent = m_leftElevator.getOutputCurrent();
    inputs.appliedVolts = m_leftElevator.getBusVoltage() * m_leftElevator.getAppliedOutput();
    inputs.elevatorPos = leftEncoder.getPosition();
    inputs.zeroed = zeroed;
    if (bottomLimitSwitch.isPressed()) {
      leftEncoder.setPosition(0);
      zeroed = true;
    }
  }


  /**
   * Drive motors in maxMotion position mode
   *
   * @param targetPosition The target position for the elevator to go to in motor rotations
   * 
   */
  public void positionControl(double targetPostion) {
    if(zeroed){
      zeroed = false;
      leftElevatorController.setReference(
        targetPostion, 
        ControlType.kMAXMotionPositionControl, 
        ClosedLoopSlot.kSlot0, 
        feedforward.calculate(leftEncoder.getVelocity()),
        ArbFFUnits.kVoltage);
    } else {
      // rightElevatorController.setReference(-100, SparkBase.ControlType.kMAXMotionPositionControl);
      leftElevatorController.setReference(0, ControlType.kVoltage);
    }
  }

  public void requestFunnelPOS(double POS){
    funnelController.setReference(POS, ControlType.kPosition);
  }

  public void requestBeltRPM(double RPM){
    funnelController.setReference(RPM, ControlType.kVelocity);
  }
  /**
   * Drive motors in voltage control mode
   * 
   * @param voltage Voltage to drive motors at
   * 
   */
  public void voltageControl(double voltage) {
    //clamp to -12, 12 volts
    voltage = Math.max(-12.0, Math.min(voltage, 12.0));
    leftElevatorController.setReference(voltage, ControlType.kVoltage);
  }


  /**
   * Stop both motors
   */
  public void stop() {
    m_leftElevator.stopMotor();
    m_rightElevator.stopMotor();
  }
}
