Pod::Spec.new do |s|
  s.name     = 'BLEService'
  s.version  = '1.0.1'
  s.license  = 'MIT'
  s.summary  = 'BLE classes for Ellipse lock'
  s.homepage = 'https://github.com/velolabs/BLEService'
  s.authors  = { 'Ravil Khusainov' => 'ravil@lattis.io' }
  s.source   = { :git => 'https://github.com/velolabs/BLEService.git' }
  s.requires_arc = true

  s.source_files = 'BLEService/Source/*.swift'
  s.preserve_paths = 'BLEService/Source/*.framework'
  s.ios.vendored_frameworks = 'BLEService/Source/CommonCrypto.framework'
  s.xcconfig = { 'FRAMEWORK_SEARCH_PATHS' => '$(PODS_ROOT)/BLEService/BLEService/Source' }
  s.ios.deployment_target = '10.0'
  s.ios.frameworks = 'CoreBluetooth'
end
