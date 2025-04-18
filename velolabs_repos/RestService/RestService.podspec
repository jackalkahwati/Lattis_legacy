Pod::Spec.new do |s|
  s.name     = 'RestService'
  s.version  = '1.0.1'
  s.license  = 'MIT'
  s.summary  = 'REST API classes for Ellipse lock'
  s.homepage = 'https://github.com/velolabs/RestService'
  s.authors  = { 'Ravil Khusainov' => 'ravil@lattis.io' }
  s.source   = { :git => 'https://github.com/velolabs/RestService.git' }
  s.requires_arc = true
  s.dependency 'SwiftyJSON'

  s.source_files = 'RestService/Source/RestService/*.swift'

  s.ios.deployment_target = '10.0'

  s.subspec 'Oval' do |oval|
    oval.source_files = 'RestService/Source/Oval/*.swift'
    oval.dependency	  'SwiftyJSON'
    oval.dependency	  'KeychainSwift'
  end
end
