Pod::Spec.new do |s|
  s.name     = 'Oval'
  s.version  = '2.1.6'
  s.license  = 'MIT'
  s.summary  = 'REST API classes for Ellipse lock'
  s.homepage = 'https://github.com/velolabs/oval_ios'
  s.authors  = { 'Ravil Khusainov' => 'ravil@lattis.io' }
  s.source   = { :git => 'https://github.com/velolabs/oval_ios.git' }
  s.requires_arc = true
  s.swift_version = '5.3'

  s.source_files = 'Source/*.swift'

  s.ios.deployment_target = '10.0'
  s.watchos.deployment_target = '4.0'
end
