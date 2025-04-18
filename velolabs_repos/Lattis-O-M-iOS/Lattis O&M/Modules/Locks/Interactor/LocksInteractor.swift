//
//  LocksLocksInteractor.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreLocation
import Oval
import LattisSDK
import QRCodeReader
import AVFoundation

class LocksInteractor: NSObject {
    weak var view: LocksInteractorOutput!
    weak var dashboard: UIViewController!
    var router: LocksRouter!
    let viewModel: LocksTablePresentable = LocksViewModel()
    weak var delegate: LocksInteractorDelegate?
    typealias Network = EllipseNetwork & BikeNetwork & LattisSDK.Network
    fileprivate let network: Network
    fileprivate let fleets: FleetsStorage = CoreDataStack.shared
    fileprivate let ble = EllipseManager.shared
    fileprivate let locationManager = CLLocationManager()
    fileprivate var coordinate = kCLLocationCoordinate2DInvalid
    fileprivate var qrBike: QRCodeBike?
    fileprivate var connectionTimer: Timer?
    fileprivate var metaHandler: LockMetadataHandler?
    fileprivate var connectCalback: () -> Ellipse? = { return nil}
    fileprivate var locationTimer: Timer?
    
    init(network: Network = Session.shared) {
        self.network = network
        super.init()
        locationManager.requestWhenInUseAuthorization()
        locationManager.delegate =  self
        updateLocation()
    }
    
    deinit {
        connectionTimer?.invalidate()
        locationManager.delegate = nil
    }
}

extension LocksInteractor: LocksInteractorInput {
    func viewLoaded() {
        viewModel.start()
        if let model = viewModel as? LocksViewModel {
            ble.subscribe(handler: model)
        }
        ble.scan(with: self)
    }
    
    func viewForHeader(for section: Int) -> UIView {
        let view = viewModel.viewForHeader(for: section)
        if let vw = view as? LocksFilterSectionView {
            vw.delegate = self
        }
        return view
    }
    
    func connect(lock: Lock) {
        updateLocation()
        connectionTimer?.invalidate()
        func connect(ellipse: Ellipse, with per: Peripheral, completion: @escaping () -> () = {}) {
            per.connect(lock: ellipse, handler: self)
            connectCalback = {
                EBikeHandler.shared.update(ellipse: ellipse)
                return ellipse
            }
        }
        guard let per = lock.peripheral else { return }
        var lockName = per.name
        if let ellipse = lock.lock {
            if let name = ellipse.name {
                lockName = name
            }
//            if let pin = ellipse.emptyPin, pin {
//                per.needSetPinCode = true
//            }
            per.needSetPinCode = true
            connect(ellipse: ellipse, with: per)
        } else if let fleet = fleets.currentFleet {
            network.assign(lock: per.macId, to: fleet.fleetId) { [weak self] result in
                switch result {
                case .success:
                    per.cleanCache()
                    per.needSetPinCode = true
                    self?.viewModel.refreshNetwork() { ellipses, error in
                        if let error = error {
                            self?.view.show(error: error)
                            return
                        }
                        guard let ellipse = ellipses.filter({$0.macId == per.macId}).first else {
                            self?.view.show(error: Lock.Error.notFound)
                            return
                        }
                        connect(ellipse: ellipse, with: per)
                    }
                case .failure(let error):
                    self?.view.show(error: error)
                }
            }
        }
        view.startLoading(title: String(format: "locks_connecting_to_lock_loading".localized().localized(), lockName))
        metaHandler = LockMetadataHandler(lock: lock)
    }
    
    func disconnect(lock: Lock) {
        guard let per = lock.peripheral else { return }
        per.disconnect()
        ble.scan()
    }
    
    func flashLED(for lock: Lock) {
        guard let per = lock.peripheral else { return }
        view.startLoading(title: "locks_blinking_led".localized())
        per.flashLED() { [weak self] error in
            if let e = error {
                self?.view.show(error: e)
            } else {
                self?.view.stopLoading {}
            }
        }
    }
    
    func refresh() {
        ble.scan()
        viewModel.refreshNetwork(completion:{_,_ in})
    }
    
    func scanQRCode() {
        router.openQRScanner(with: self, in: dashboard, completion: stopScan)
    }
    
    func addLock() {
        router.openOnboarding(locks: (viewModel as! LocksViewModel).ellipses) { [weak self] peripheral in
            self?.viewModel.update(filter: .all)
            self?.connect(lock: .init(peripheral: peripheral, lock: nil))
        }
    }
    
    func changeFilter() {
        router.openFilter(with: self, and: viewModel.filter)
    }
}

extension LocksInteractor: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            coordinate = location.coordinate
        }
    }
}

extension LocksInteractor: QRCodeReaderViewControllerDelegate {
    func reader(_ reader: QRCodeReaderViewController, didScanResult result: QRCodeReaderResult) {
        qrBike = result.bike
    }
    
    func reader(_ reader: QRCodeReaderViewController, didSwitchCamera newCaptureDevice: AVCaptureDeviceInput) {}
    
    func readerDidCancel(_ reader: QRCodeReaderViewController) {
        reader.dismiss(animated: true, completion: nil)
    }
}

extension LocksInteractor: LocksFilterSectionDelegate {
    func openSearch() {
        guard let vm = viewModel as? LocksViewModel else { return }
        router.openSearch(locks: vm.locks, delegate: self)
    }
    
    func openFilter() {
        router.openFilter(with: self, and: viewModel.filter)
    }
    
    func didSelect(filter: Lock.Filter, vendor: Lock.Vendor) {
        if vendor != .ellipse {
            delegate?.change(vendor: vendor, filter: filter)
        } else {
            viewModel.update(filter: filter)
        }
    }
}

extension LocksInteractor: LocksSearchDelegate {
    func locksSearch(controller: LocksSearchViewController, didSelect lock: Lock) {
        controller.dismiss(animated: true) {
            self.connect(lock: lock)
        }
    }
}

extension LocksInteractor: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        view.update(bluetoothState: connected)
    }
}

extension LocksInteractor: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {}
    
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        switch connection {
        case .paired:
            let ell = connectCalback()
            self.viewModel.calculate()
            self.view.showAlert(title: "locks_connection_warning_title".localized(), subtitle: "locks_connection_warning_text".localized())
            if let bikeId = ell?.bikeId {
                self.network.updae(state: .outOfService, with: self.coordinate, for: bikeId) {_ in}
            }
            
            if let macId = ell?.macId, let lock = viewModel.lock(by: macId) {
                delegate?.settings(lock: lock)
            }
        case .ready:
            let ell = connectCalback()
            if let ell = ell, ellipse.needSetPinCode {
                #if DEBUG
                let pin: [Peripheral.Pin] = [.up, .right, .down, .left]
                #else
                let pin = Peripheral.randomPin(to: 10)
                #endif
                do {
                    try ellipse.set(pinCode: pin)
                    self.network.save(pinCode: pin.map({$0.stringValue}), for: ell) { result in
                        switch result {
                        case .success:
                            ellipse.needSetPinCode = false
                        case .failure(let error):
                            report(error: error)
                        }
                    }
                } catch {
                    report(error: error)
                }
            } else {
            }
        case .failed(let error):
            self.view.show(error: error)
        default:
            viewModel.calculate()
            break
        }
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate value: Peripheral.Value) {
        switch value {
        case .firmwareVersion(_):
            metaHandler?.isFWVersionReseived = true
        case .metadata(_):
            metaHandler?.isMetadataReseived = true
        default:
            break
        }
    }
}

private extension LocksInteractor {
    func stopScan() {
        guard let qrCode = qrBike else { return }
        view.startLoading(title: "locks_connecting".localized())
        network.getMacId(by: qrCode.id) { [weak self] result in
            switch result {
            case .success(let macId):
                guard let lock = self?.viewModel.lock(by: macId) else {
                    self?.view.showAlert(title: nil, subtitle: String(format: "qr_connect_error_message".localized(), macId))
                    return
                }
                self?.startTimer()
                self?.connect(lock: lock)
            case .failure(let error):
                self?.view.show(error: error)
            }
        }
    }
    
    func startTimer() {
        connectionTimer?.invalidate()
        connectionTimer = Timer.scheduledTimer(timeInterval: 20, target: self, selector: #selector(connectionFailed), userInfo: nil, repeats: false)
    }
    
    @objc func connectionFailed() {
        connectionTimer?.invalidate()
        view.connectonFailed()
    }
    
    func updateLocation() {
        locationManager.startUpdatingLocation()
        locationTimer = Timer.scheduledTimer(withTimeInterval: 5, repeats: false, block: { [weak self] (_) in
            self?.locationManager.stopUpdatingLocation()
            self?.locationTimer?.invalidate()
            self?.locationTimer = nil
        })
    }
}


extension Peripheral {
    var needSetPinCode: Bool {
        get {
            return UserDefaults.standard.bool(forKey: "needSetPinCode-\(macId)")
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "needSetPinCode-\(macId)")
            UserDefaults.standard.synchronize()
        }
    }
    
    public class func randomPin(to digits: Int) -> [Pin] {
        var code: [UInt32] = []
        let convert: (UInt32) -> Pin = { val in
            switch val {
            case 0:
                return .up
            case 1:
                return .right
            case 2:
                return .down
            default:
                return .left
            }
        }
        while digits > code.count {
            let rand = arc4random()%4
            code.append(rand)
        }
        return code.map(convert)
    }
}
