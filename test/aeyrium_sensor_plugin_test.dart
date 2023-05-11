import 'package:flutter_test/flutter_test.dart';
import 'package:aeyrium_sensor_plugin/aeyrium_sensor_plugin.dart';
import 'package:aeyrium_sensor_plugin/aeyrium_sensor_plugin_platform_interface.dart';
import 'package:aeyrium_sensor_plugin/aeyrium_sensor_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockAeyriumSensorPluginPlatform
    with MockPlatformInterfaceMixin
    implements AeyriumSensorPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final AeyriumSensorPluginPlatform initialPlatform = AeyriumSensorPluginPlatform.instance;

  test('$MethodChannelAeyriumSensorPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelAeyriumSensorPlugin>());
  });

  test('getPlatformVersion', () async {
    AeyriumSensorPlugin aeyriumSensorPlugin = AeyriumSensorPlugin();
    MockAeyriumSensorPluginPlatform fakePlatform = MockAeyriumSensorPluginPlatform();
    AeyriumSensorPluginPlatform.instance = fakePlatform;

    expect(await aeyriumSensorPlugin.getPlatformVersion(), '42');
  });
}
