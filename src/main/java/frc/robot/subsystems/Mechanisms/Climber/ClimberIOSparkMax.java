package frc.robot.subsystems.mechanisms.climber;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkBaseConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.subsystems.mechanisms.MechanismConstants;

public class ClimberIOSparkMax implements ClimberIO{
    private SparkMax m_climber;
    private SparkMax m_funnel;
    private SparkBaseConfig climberConfig;
    private RelativeEncoder climbEncoder;
    private RelativeEncoder funnelEncoder;
    private SparkClosedLoopController climbController;
    private SparkClosedLoopController funnelController;

    public ClimberIOSparkMax(){
        climberConfig.idleMode(IdleMode.kBrake);
        climberConfig.smartCurrentLimit(0);

        m_climber = new SparkMax(MechanismConstants.climberId, MotorType.kBrushless);
        m_climber.configure(climberConfig, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
        m_funnel = new SparkMax(MechanismConstants.climberId, MotorType.kBrushless);
        m_funnel.configure(climberConfig, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);

        climbEncoder = m_climber.getEncoder();
        funnelEncoder = m_funnel.getEncoder();

        climbController = m_climber.getClosedLoopController();
        funnelController = m_funnel.getClosedLoopController();
    }

    @Override
    public void updateInputs(ClimberIOInputs inputs) {
        inputs.climberPOS = climbEncoder.getPosition();
        inputs.funnelPOS = funnelEncoder.getPosition();
    }

    public void setClimberPosition(double pos){
        climbController.setReference(pos, SparkBase.ControlType.kMAXMotionPositionControl);
    }

    public void setFunnelPosition(double pos){
        funnelController.setReference(pos, SparkBase.ControlType.kVoltage);
    }

    public void stop(){
        m_climber.stopMotor();
    }
}
