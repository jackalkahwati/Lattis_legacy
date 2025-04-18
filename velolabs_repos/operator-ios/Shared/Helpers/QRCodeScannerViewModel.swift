//
//  QRCodeScannerViewModel.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.03.2021.
//

import Foundation
import QRCodeView
import Combine
import AVFoundation

extension Vehicle {
    struct LattisQRCode: Codable {
        let qr_id: Int
        let bike_name: String
    }
}

final class QRCodeScannerViewModel: ObservableObject {
    
    let fleetId: Int
    @Published var scanning: Bool = true
    @Published var viewState: ViewState = .screen
    @Published var popUpVehicle: Vehicle?
    @Published var selectedVehicle: Vehicle?
    @Published fileprivate(set) var vehicles: [Vehicle] = []
    @Published fileprivate(set) var isTorchOn: Bool = false
    
    fileprivate var ignore: [String] = []
    fileprivate let urlDecoder: QRCodeView.Decoder<URL> = .url()
    fileprivate let lattisDecoder: QRCodeView.Decoder<Vehicle.LattisQRCode> = .json()
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init(_ fleetId: Int) {
        self.fleetId = fleetId
        if let device = AVCaptureDevice.default(for: .video),
            device.hasTorch {
            isTorchOn = device.torchMode == .on
        }
    }
    
    func found(code: String) -> Bool {
        guard !ignore.contains(code) else { return false }
        ignore.append(code)
        if let url = urlDecoder.decode(code) {
            fetchVehicle(thing: url.lastPathComponent)
        } else if let lattis = lattisDecoder.decode(code) {
            fetchVehicle(lattis: lattis)
        } else {
            fetchVehicle(thing: code)
        }
        return true
    }
    
    func delete(vehicle: Vehicle) {
        if let idx = vehicles.firstIndex(of: vehicle) {
            vehicles.remove(at: idx)
        }
    }
    
    func canUseBatchActions() -> Bool {
        popUpVehicle = vehicleOnTrip()
        return popUpVehicle == nil
    }
    
    fileprivate func vehicleOnTrip() -> Vehicle? {
        vehicles.first(where: { $0.metadata.usage == .on_trip })
    }
    
    fileprivate func fetchVehicle(lattis: Vehicle.LattisQRCode? = nil, thing: String? = nil) {
        viewState = .loading
        CircleAPI.findVehicle(fleetId, qrId: lattis?.qr_id, thingQrCode: thing)
            .map(Vehicle.init)
            .sink { [weak self] (com) in
                switch com {
                case .failure(let error):
                    self?.viewState = .error("Warning", error.localizedDescription)
                case .finished:
                    self?.viewState = .screen
                }
            } receiveValue: { [weak self] (vehicle) in
                self?.vehicles.insert(vehicle, at: 0)
                self?.scanning = false
            }
            .store(in: &cancellables)
    }
    
    func toggleTorch() {
        guard let device = AVCaptureDevice.default(for: .video),
            device.hasTorch else {
            Analytics.report(.custom("Torch", "It's not avaliable for scanning"))
                return
        }
        do {
            try device.lockForConfiguration()
            
            if device.torchMode != .on {
                device.torchMode = .on
                isTorchOn = true
            } else {
                device.torchMode = .off
                isTorchOn = false
            }
            
            device.unlockForConfiguration()
        } catch {
            Analytics.report(.error(error))
        }
    }
}

extension QRCodeScannerViewModel: VehiclePatcher {
    var currentVehicle: Vehicle? { nil }
    
    func updateVehicle(patch: Vehicle.Patch) {
        viewState = .loading
        CircleAPI.update(vehicles: vehicles.map(\.id), json: patch)
            .sink { [weak self] result in
                switch result {
                case .failure(let error):
                    Analytics.report(.error(error))
                    self?.viewState = .error("Warning", error.localizedDescription)
                case .finished:
                    self?.viewState = .screen
                }
            } receiveValue: {
                self.vehicles.update{$0.update(patch: patch)}
            }
            .store(in: &cancellables)
    }
}

extension Array {
    mutating func update(_ transfomr: (inout Element) -> Void) {
        var buffer: [Element] = []
        forEach { element in
            var mutable = element
            transfomr(&mutable)
            buffer.append(mutable)
        }
        self = buffer
    }
}
