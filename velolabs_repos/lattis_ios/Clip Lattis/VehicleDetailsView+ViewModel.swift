//
//  VehicleDetailsView+ViewModel.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 03.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Combine
import SwiftUI

extension VehicleDetailsView {
    final class ViewModel: ObservableObject {
        
        init(_ qrCode: ScanView.QRType) {
            fetchVehicle(with: qrCode)
        }
        
        @Published fileprivate(set) var vehicle: Vehicle?
        
        @AppStorage("vehicle-id")
        fileprivate var vehicleId: Int?
        
        private func fetchVehicle(with qrCode: ScanView.QRType) {
            Task {
                do {
                    self.vehicle = try await CircleAPI.scan(qrCode)
                    self.vehicleId = self.vehicle?.id
                } catch {
                    print(error)
                }
                
            }
        }
    }
}
