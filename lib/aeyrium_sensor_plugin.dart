
import 'package:flutter/services.dart';

class SensorEvent {
  /// Android模拟器Virtual sensors中绕X轴旋转弧度 （竖屏时屏幕正面向自己，以手机中心点为原点，左右方向为X轴，平行于地面，轴固定，不随手机旋转改变）
  final double pitch;

  /// Android模拟器Virtual sensors中绕Z轴旋转弧度 （竖屏时屏幕正面向自己，以手机中心点为原点，前后方向为Z轴，平行于地面，轴固定，不随手机旋转改变）
  final double roll;

  SensorEvent(this.pitch, this.roll);

  @override
  String toString() => '[Event: (pitch: $pitch, roll: $roll)]';
}

class AeyriumSensor {
  static const EventChannel _sensorEventChannel =
  EventChannel('aeyrium_sensor_plugin');

  static final AeyriumSensor _instance = AeyriumSensor._();

  factory AeyriumSensor() {
    return _instance;
  }

  AeyriumSensor._();

  static Stream<SensorEvent>? _sensorEvents;

  /// A broadcast stream of events from the device rotation sensor.
  static Stream<SensorEvent>? get sensorEvents {
    _sensorEvents ??= _sensorEventChannel
        .receiveBroadcastStream()
        .map((dynamic event) => _listToSensorEvent(event.cast<double>()));
    return _sensorEvents;
  }

  static SensorEvent _listToSensorEvent(List<double> list) {
    // Android是从aeyrium_sensor_plugin/android/src/main/java/com/aeyrium/sensor/aeyrium_sensor_plugin/AeyriumSensorPlugin.java
    // double[] sensorValues = new double[2];传入的
    return SensorEvent(list[0], list[1]);
  }
}
