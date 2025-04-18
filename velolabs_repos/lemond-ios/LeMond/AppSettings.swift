//
//  AppSettings.swift
//  LeMond
//
//  Created by Ravil Khusainov on 01.01.2022.
//

import Combine
import SwiftUI
import CoreLocation

final class AppSettings: ObservableObject {
    
    @AppStorage("lock-security", store: .standard)
    var secure: Bool = true
    
    @AppStorage("user-name", store: .standard)
    var name: String = "Greg LeMond"
    
    @AppStorage("user-email", store: .standard)
    var email: String = "greg@lemond.com"
    
    @AppStorage("user-phone", store: .standard)
    var phone: String = "+33 898 787347"
    
    let coordinate: CLLocationCoordinate2D = .init(latitude: 36.13539346956965, longitude: -115.16011544419746)
}
