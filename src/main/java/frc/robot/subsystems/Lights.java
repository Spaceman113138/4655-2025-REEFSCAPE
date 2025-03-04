// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.led.Animation;
import com.ctre.phoenix.led.CANdle;
import com.ctre.phoenix.led.CANdle.LEDStripType;
import com.ctre.phoenix.led.CANdleConfiguration;
import com.ctre.phoenix.led.SingleFadeAnimation;
import com.ctre.phoenix.led.StrobeAnimation;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.mechanisms.MechanismConstants;

public class Lights extends SubsystemBase {
  private static final CANdle candle = new CANdle(MechanismConstants.CANdleID);
  private static final CANdleConfiguration config = new CANdleConfiguration();
  private static final int numLEDS = 33 + 8;

  /** Creates a new Lights. */
  public Lights() {
    config.brightnessScalar = .5;
    config.disableWhenLOS = false;
    config.statusLedOffWhenActive = false;
    config.stripType = LEDStripType.GRBW;
    candle.configAllSettings(config);
    singleColorAnimation(new Color(80, 7, 120));
    candle.animate(getFadeAnimation(255, 209, 0), 1);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  private double minValue(Color color) {
    return Math.min(color.blue, Math.min(color.red, color.blue));
  }

  public void singleFadeAnimation(Color color) {
    SingleFadeAnimation animation =
        getFadeAnimation(
            (int) (color.red * 255), (int) (color.green * 255), (int) (color.blue * 255));
    setLEDstrip(animation);
  }

  public void singleStrobeAnimation(Color color) {
    StrobeAnimation animation =
        getStrobeAnimation(
            (int) (color.red * 255), (int) (color.green * 255), (int) (color.blue * 255));
    setLEDstrip(animation);
  }

  public void singleColorAnimation(Color color) {
    setSolidColor((int) (color.red * 255), (int) (color.green * 255), (int) (color.blue * 255));
  }

  private StrobeAnimation getStrobeAnimation(int r, int g, int b) {
    return new StrobeAnimation(r, g, b, Math.min(r, Math.min(g, b)), 0.5, numLEDS);
  }

  private SingleFadeAnimation getFadeAnimation(int r, int g, int b) {
    return new SingleFadeAnimation(r, g, b, Math.min(r, Math.min(g, b)), 0.5, numLEDS);
  }

  // private void setLEDcolor(int r, int g, int b, int index) {
  //   candle.setLEDs(r, g, b, Math.min(r, Math.min(g, b)), index, 1);
  // }

  public void setLEDstrip(Animation animation) {
    animation.setLedOffset(0);
    animation.setNumLed(numLEDS);
    candle.clearAnimation(0);
    candle.clearAnimation(1);
    candle.animate(animation, 0);
  }

  public void setSolidColor(int r, int g, int b) {
    candle.clearAnimation(0);
    candle.setLEDs(r, g, b, Math.min(r, Math.min(g, b)), 0, numLEDS);
  }
}
