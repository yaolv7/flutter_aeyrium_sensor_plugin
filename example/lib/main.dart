import 'dart:async';
import 'dart:io';

import 'package:aeyrium_sensor_plugin/aeyrium_sensor_plugin.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:math' as math;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _data = "";

  StreamSubscription<dynamic>? _streamSubscriptions;

  @override
  void initState() {
    _streamSubscriptions = AeyriumSensor.sensorEvents?.listen((event) {
      // (180 * roll / Math.PI)  (180 * pitch / Math.PI)
      setState(() {
        var roll = (180 * event.roll ~/ math.pi);
        if (Platform.isIOS && roll < -180) {
          // iOS 是返回 0 ~ -360度，需要转一下
          roll += 360;
        }

        var pitch = 180 * event.pitch ~/ math.pi;

        _data = "roll Z-Rot: $roll \n pitch X-Rot: $pitch";
      });
    });
    super.initState();
  }

  @override
  void dispose() {
    if (_streamSubscriptions != null) {
      _streamSubscriptions?.cancel();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(15.0),
          child: Center(
            child: Text(' ---- $_data'),
          ),
        ),
      ),
    );
  }
}
