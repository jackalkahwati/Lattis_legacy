//
//  MapViewModel.swift
//  LeMond
//
//  Created by Ravil Khusainov on 01.01.2022.
//

import Combine
import MapKit

final class MapViewModel: ObservableObject {
    
    let settings: AppSettings
    @Published var region: MKCoordinateRegion
    fileprivate(set) var bike: Bike
    
    init(_ settings: AppSettings) {
        self.settings = settings
        self.region = MKCoordinateRegion(
            center: settings.coordinate,
            latitudinalMeters: 750,
            longitudinalMeters: 750
        )
        self.bike = .init(id: UUID(), coordinate: settings.coordinate)
    }
}

struct Bike: Identifiable {
    let id: UUID
    let coordinate: CLLocationCoordinate2D
}
