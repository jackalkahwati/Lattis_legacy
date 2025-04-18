Pod::Spec.new do |s|
  s.name     = 'LattisSDK'
  s.version  = '2.2.2'
  s.license  = 'MIT'
  s.summary  = 'BLE classes for Ellipse lock'
  s.homepage = 'https://github.com/velolabs/lattis_sdk_ios'
  s.authors  = { 'Ravil Khusainov' => 'ravil@lattis.io' }
  s.source   = { :git => 'https://github.com/velolabs/lattis_sdk_ios.git', :tag => "v#{s.version}" }
  s.requires_arc = true
  s.swift_version = '5.1'
#  s.platform = :ios
  s.source_files = 'LattisSDK/Source/**/*.swift'
  s.ios.deployment_target = '10.0'
  s.watchos.deployment_target = '4.0'
  s.frameworks = 'CoreBluetooth'
  s.dependency 'CryptoSwift'
  s.dependency 'Oval'
  s.dependency 'KeychainSwift'
end
