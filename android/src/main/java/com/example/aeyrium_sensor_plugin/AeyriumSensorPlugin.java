package com.example.aeyrium_sensor_plugin;

import android.hardware.display.DisplayManager;
import android.view.Display;
import androidx.annotation.NonNull;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.WindowManager;
import android.app.Activity;
import android.content.Context;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;

/** AeyriumSensorPlugin */
public class AeyriumSensorPlugin implements FlutterPlugin,  EventChannel.StreamHandler {

  // The rate sensor events will be delivered at. As the Android documentation
  // states, this is only a hint to the system and the events might actually be
  // received faster or slower than this specified rate. Since the minimum
  // Android API levels about 9, we are able to set this value ourselves rather
  // than using one of the provided constants which deliver updates too quickly
  // for our use case. The default is set to 100ms
  private static final int SENSOR_DELAY_MICROS = 30 * 1000;

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private static final String SENSOR_CHANNEL_NAME = "aeyrium_sensor_plugin";
  private Display display;
  private SensorEventListener sensorEventListener;
  private SensorManager sensorManager;
  private Sensor sensor;
  private int mLastAccuracy;

  public AeyriumSensorPlugin() {
    // no-op
  }

  private AeyriumSensorPlugin(Context context) {
    display = ((DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE))
        .getDisplay(Display.DEFAULT_DISPLAY);
    sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    EventChannel channel = new EventChannel(binding.getBinaryMessenger(), SENSOR_CHANNEL_NAME);
    channel.setStreamHandler(new AeyriumSensorPlugin(binding.getApplicationContext()));
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }

  public void onListen(Object arguments, EventSink events) {
    sensorEventListener = createSensorEventListener(events);
    // 注册listener，第三个参数是检测的精确度
    // SENSOR_DELAY_FASTEST 最灵敏 因为太快了没必要使用
    // SENSOR_DELAY_GAME    游戏开发中使用
    // SENSOR_DELAY_NORMAL  正常速度
    // SENSOR_DELAY_UI    最慢的速度
    sensorManager.registerListener(sensorEventListener, sensor, sensorManager.SENSOR_DELAY_UI);
//    sensorManager.registerListener(sensorEventListener, sensor, SENSOR_DELAY_MICROS);
  }

  public void onCancel(Object arguments) {
    if (sensorManager != null && sensorEventListener != null){
      sensorManager.unregisterListener(sensorEventListener);
    }
  }

  SensorEventListener createSensorEventListener(final EventSink events) {
    return new SensorEventListener() {
      @Override
      public void onSensorChanged(SensorEvent event) {
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
          return;
        }

        updateOrientation(event.values, events);
      }

      @Override
      public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
          mLastAccuracy = accuracy;
        }
      }
    };
  }

  private void updateOrientation(float[] rotationVector, EventSink events) {
    float[] rotationMatrix = new float[9];
    SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

    final int worldAxisForDeviceAxisX;
    final int worldAxisForDeviceAxisY;

    // Remap the axes as if the device screen was the instrument panel,
    // and adjust the rotation matrix for the device orientation.
    switch (display.getRotation()) {
      case Surface.ROTATION_0:
      default:
        worldAxisForDeviceAxisX = SensorManager.AXIS_X;
        worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
        break;
      case Surface.ROTATION_90:
        worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
        worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
        break;
      case Surface.ROTATION_180:
        worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
        worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
        break;
      case Surface.ROTATION_270:
        worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
        worldAxisForDeviceAxisY = SensorManager.AXIS_X;
        break;
    }


    float[] adjustedRotationMatrix = new float[9];
    SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
        worldAxisForDeviceAxisY, adjustedRotationMatrix);

    // Transform rotation matrix into azimuth/pitch/roll
    float[] orientation = new float[3];
    SensorManager.getOrientation(adjustedRotationMatrix, orientation);

    double pitch = - orientation[1];
    double roll = - orientation[2];
    double[] sensorValues = new double[2];
    sensorValues[0] = pitch;
    sensorValues[1] = roll;
    events.success(sensorValues);
  }
}
