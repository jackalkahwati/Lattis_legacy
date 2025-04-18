//
//  RideRideInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 22/02/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Mapbox
import Oval
import SwiftyTimer
import LattisSDK
import MapKit
import AXALock

final class RideInteractor: NSObject {
    
    var lock: Lock!
    weak var view: RideInteractorOutput!
    var router: RideRouter!
    var isQr: Bool = false
    var isLockLocked : Bool {
        if let status = lock.axaLock?.status { return status == .strongClosed }
        if let security = lock.peripheral?.security { return security == .locked }
        return false
    }
    var tripService: TripService! {
        didSet {
            tripService.onStart = {}
            tripService.onTripUpdate = { [unowned self] update in
                self.widget.currentTrip = SharedTrip(duration: update.duration.descriptiveTime, fare: update.price?.priceValue(update.currency), bikeName: self.tripService.bike.name!, isLocked: self.isLockLocked)
                self.view.show(update: update)
                if let trip = update.trip {
                    self.stopCount()
                    self.router.openRideSummary(trip: trip, delegate: self)
                }
            }
            network.getZones(fleet: tripService.bike.fleetId) { [weak self] (result) in
                switch result {
                case .success(let zones):
                    self?.parkingZones = zones
                case .failure:
                    break
                }
            }
            network.getSpots(fleet: tripService.bike.fleetId) { [weak self] (result) in
                switch result {
                case .success(let spots):
                    self?.parkingSpots = spots
                case .failure:
                    break
                }
            }
        }
    }
    var location: CLLocation = .init(latitude: -180, longitude: -180) {
        didSet {
            tripService.location = location
        }
    }
    var canForceEndRide: Bool = false
    fileprivate let widget = WidgetConnection()
    fileprivate var canEndRide: Bool = false
    fileprivate var endingRide: Bool = false
    fileprivate var selectedAnnotation: MapAnnotation?
    fileprivate let network: Network
    fileprivate var time = -1.seconds
    fileprivate var timer: Timer?
    fileprivate let ble = EllipseManager.shared
    fileprivate var parkingZones: [ParkingZone] = []
    fileprivate var parkingSpots: [Parking] = []
    fileprivate var metadataSent = false
    fileprivate var scanTime: Int?
    fileprivate var isParkingAllowed = false
    fileprivate let axaHandler = AxaBLE.Handler()
    typealias Network = ParkingNetwork & BikeNetwork
    
    init(network: Network = Session.shared) {
        self.network = network
        super.init()
        widget.receive = { [weak self] message in
            switch message {
            case .lock(let shouldLock):
                if shouldLock {
                    self?.lock.peripheral?.lock()
                } else {
                    self?.lock.peripheral?.unlock()
                }
            }
        }
        AppRouter.shared.endDelegate = self
        NotificationCenter.default.addObserver(self, selector: #selector(appDidEnterForeground), name: UIApplication.didBecomeActiveNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(appDidEnterBackground), name: UIApplication.willResignActiveNotification, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
        timer?.invalidate()
    }
    
    fileprivate var isWalkthroughShown: Bool {
        set {
            UserDefaults.standard.set(newValue, forKey: "isWalkthroughShown")
            UserDefaults.standard.synchronize()
        }
        get {
            return UserDefaults.standard.bool(forKey: "isWalkthroughShown")
        }
    }
    
    fileprivate var tipsShowCount: Int {
        set {
            UserDefaults.standard.set(newValue, forKey: "tipsShowCount")
            UserDefaults.standard.synchronize()
        }
        get {
            return UserDefaults.standard.integer(forKey: "tipsShowCount")
        }
    }
}

extension RideInteractor: RideInteractorInput {
    var shouldFollowUser: Bool {
        return tripService.bike.doNotTrackTrip == false
    }
    
    var isBluetoothEnabled: Bool {
        return ble.isOn
    }
    
    var needShowHint: Bool {
//        if isLockLocked == false || isQr {
//            return false
//        }
        #if RELEASE
        if tipsShowCount < 2 {
            tipsShowCount += 1
            return true
        }
        return false
        #endif
        return true
    }
    
    func viewDidLoad() {
        if let update = Trip.Update(tripService.trip) {
            view.show(update: update)
        }
        ble.subscribe(handler: self)
        view.show(bike: tripService.bike)
        if !isBluetoothEnabled {
            view.show(lockState: .disconnected)
            view.showHint(text: "blutooth_pop_subtitle2".localized())
        }
        if let axa = lock.axaLock {
            if axa.connection == .paired, let slider = axa.status.slider {
                view.show(lockState: slider)
            }
            canEndRide = axa.status == .strongClosed
        }
        axaHandler.connectionChanged = { [unowned self] lock in
            var canSaveSteps = false
            switch lock.connection {
            case .paired:
                canSaveSteps = true
                self.view.show(lockState: lock.status.slider ?? .unlocked)
            case .disconnected:
                self.view.show(lockState: .disconnected)
                self.view.showHint(text: "tool_tip_reconnection".localized())
            case .connected:
                self.view.show(lockState: .connecting)
            default:
                break
            }
            self.tripService.canSaveSteps = canSaveSteps
        }
        axaHandler.statusChanged = { [unowned self] lock in
            guard let sl = lock.status.slider else { return }
            self.canEndRide = lock.status == .strongClosed
            self.view.show(lockState: sl)
        }
        
        if let module = lock.axaModule, let axa = AxaBLE.Lock.all.first(where: {$0.id == module.key}) {
            self.lock.axaLock = axa
            axa.connect(with: axaHandler)
            if axa.connection == .paired, let slider = axa.status.slider {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    self.view.show(lockState: slider)
                }
            }
        }
    }
    
    func searchParkings() {
        if isParkingAllowed == false {
            tripService.isParkingsCheck = true
        }
        network.getSpots(fleet: tripService.bike.fleetId) { [weak self] (result) in
            switch result {
            case .success(let parkings):
                self?.view.show(parkings.map(MapAnnotation.init))
            case .failure(let error):
                self?.handle(error: error)
            }
        }
        
        network.getZones(fleet: tripService.bike.fleetId) { [weak self] (result) in
            switch result {
            case .success(let zones):
                self?.view.show(zones: zones)
            case .failure(let error):
                self?.handle(error: error)
            }
        }
    }
    
    func stopSearchParkings() {
        tripService.isParkingsCheck = false
    }
    
    func selectParking(with annotation: MapAnnotation) {
        selectedAnnotation = annotation
    }
    
    func routeToSelectedParking() {
        guard let annotation = selectedAnnotation else { return }
        view.buildRoute(to: annotation)
    }
    
    func unselectParking() {
        selectedAnnotation = nil
    }
    
    func openMenu() {
        router.openMenu() {  $0.bike = self.tripService.bike }
    }
    
    func startCount() {
        AppRouter.shared.save(trip: tripService.trip, bike: tripService.bike)
        AppRouter.shared.endTrip = { [unowned self] short in
            if short {
                self.tripService.bike.shortEndRide = true
            }
            self.endRide(forced: true)
        }
        if let per = lock.peripheral {
            per.subscribe(self)
        } else {
            connectLock()
        }
        time = tripService.trip.startedAt?.timeIntervalSinceNow ?? 0
        time = abs(time)
        timer?.invalidate()
        timer = Timer.every(1.second) { [weak self] in
            self?.calculate()
        }
    }
    
    func stopCount() {
        time = -1.second
        timer?.invalidate()
        timer = nil
    }
    
    func endRide(forced: Bool) {
//        let next = EndRideRouter.instantiate(type: .action, configure: {$0.trip = tripService.trip})
//        let navigation = UINavigationController(rootViewController: next, style: .white)
//        (view as! UIViewController).present(navigation, animated: true, completion: nil)
//        return
        guard CLLocationManager.locationServicesEnabled(),
            CLLocationManager.authorizationStatus() != .denied else {
                view.warning(with: "privacy_location_alert_title".localized(), subtitle: "privacy_location_alert_text".localized())
                return
        }
        canForceEndRide = false
        let shortEnd: () -> () = { [unowned self] in
            self.view.startLoading(with: "active_ride_ending_trip".localized())
            self.endTrip(with: nil) { [unowned self] (error, trip) in
                self.endingRide = false
                if let error = error {
                    self.view.show(error: error, file: #file, line: #line)
                } else if let trip = trip {
                    self.view.stopLoading(completion: { [unowned self] in
                        self.router.openRideSummary(trip: trip, delegate: self)
                    })
                }
            }
        }
        
        if forced {
            if tripService.bike.shortEndRide {
                shortEnd()
            } else {
                router.openEndRide { $0.delegate = self }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    self.endingRide = false
                }
            }
            return
        }
        if let axa = lock.axaLock, axa.connection != .paired || axa.status != .strongClosed || !canEndRide {
            return view.warning(with: "end_ride_lock_warning_title".localized(), subtitle: "end_ride_lock_warning_text".localized())
        } else if lock.axaLock == nil {
            guard let c = lock.peripheral?.connection, case .paired = c, canEndRide  else {
                return view.warning(with: "end_ride_lock_warning_title".localized(), subtitle: "end_ride_lock_warning_text".localized())
            }
        }
        endingRide = true
        tripService.onParkingCheck = { [unowned self] check in
            self.isParkingAllowed = false
            self.endingRide = false
            switch check {
            case .allowed:
                self.isParkingAllowed = true
                if self.tripService.bike.shortEndRide {
                    shortEnd()
                } else {
                    self.view.stopLoading() {}
                    self.router.openEndRide { $0.delegate = self }
                }
            default:
                self.view.stopLoading() {}
//                guard self.tripService.isParkingsCheck == false else { return }
                self.view.show(parkingCheck: check)
            }
        }
        tripService.onFail = { [unowned self] error in
            self.endingRide = false
            self.view.show(error: error, file: #file, line: #line)
        }
        view.startLoading(with: "active_ride_check_parking_loading".localized())
        tripService.checkParking()
    }
    
    func openDamage() {
        router.openDamage() { $0.bike = self.tripService.bike }
    }
    
    func openDirections() {
        router.openDirections(with: self)
    }
    
    func suspend() {
        timer?.invalidate()
        timer = nil
    }
    
    func set(lockState: LockButton.LockState) -> Bool {
        guard endingRide == false else { return false }
        canEndRide = false
        if lockState.isLocked {
            lock.peripheral?.lock()
            lock.axaLock?.lock()
        } else {
            lock.peripheral?.unlock()
            lock.axaLock?.unlock()
        }
        return true
    }
    
    func openTheft() {
        router.openTheft(for: tripService.bike)
    }
    
    func connectLock() {
        guard ble.isOn else { return }
        var lockState: LockButton.LockState = .connecting
        if let per = lock.peripheral {
            per.connect(handler: self, bike: tripService.bike)
        } else if lock.needEllipse {
            scanTime = 20
            ble.scan(with: self)
        } else if let lock = lock.axaLock {
            lock.connect(with: axaHandler)
            if lock.connection == .paired, let s = lock.status.slider {
                lockState = s
            }
        } else if lock.needAxa {
            axaHandler.discovered = { [unowned self] lock in
                guard lock.id == self.lock.axaModule?.key else { return }
                self.lock.axaLock = lock
                lock.connect(with: self.axaHandler)
                self.scanTime = nil
            }
            AxaBLE.Lock.scan(with: axaHandler)
        }
        view.show(lockState: lockState)
    }
    
    func openBikeDetails() {
        tripService.bike.status = .onRide
        router.openInfo(bike: tripService.bike)
    }
    
    func openPayments() {
        router.openPayments()
    }
}

extension RideInteractor: EndRideInteractorDelegate {
    func endTrip(with image: UIImage?,completion: @escaping (Error?, Trip?) -> ()) {
        tripService.onEnd = { [unowned self] in
            completion(nil, self.tripService.trip)
            self.widget.currentTrip = nil
            self.stopCount()
        }
        tripService.onFail = { completion($0, nil) }
        tripService.end(with: image)
    }
    
    func didEndTrip(with rating: Int?) {
        tripService.rateTrip(with: rating)
        lock.peripheral?.disconnect()
        lock.peripheral?.cleanCache()
        AppRouter.shared.isTripStarted = false
        router.openFind()
    }
}

private extension RideInteractor {
    func calculate() {
        guard time >= 0, view != nil  else { return }
        view.update(time.time)
        time += 1
        if var scan = scanTime {
            scan -= 1
            scanTime = scan
            if scan <= 0 {
                ble.stopScan()
                scanTime = nil
                view.show(lockState: .disconnected)
                view.showHint(text: "tool_tip_reconnection".localized())
            }
        }
    }
    
    func handle(error: Error, file: String = #file, line: Int = #line) {
        view.show(error: error, file: file, line: line)
    }
    
    @objc func appDidEnterForeground() {
        startCount()
    }
    
    @objc func appDidEnterBackground() {
        stopCount()
    }
}

extension RideInteractor: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [LattisSDK.Ellipse], delete: [LattisSDK.Ellipse]) {
        guard lock.peripheral == nil else { return }
        if let per = lockManager.locks.filter({$0.macId == self.lock.ellipse?.macId}).first {
            lock.peripheral = per
            per.connect(handler: self, bike: tripService.bike)
            scanTime = nil
        }
    }
    
    func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        if connected {
            view.show(lockState: .connecting)
            connectLock()
        } else {
            view.show(lockState: .disconnected)
            view.showHint(text: "blutooth_pop_subtitle2".localized())
        }
    }
}

extension RideInteractor: EllipseDelegate {
    func ellipse(_ ellipse: LattisSDK.Ellipse, didUpdate connection: LattisSDK.Ellipse.Connection) {
        var canSaveSteps = false
        switch connection {
        case .paired:
            canSaveSteps = true
            view.show(lockState: ellipse.security.slider)
            ble.stopScan()
        case .connecting,
             .reconnecting where ble.isOn:
            view.show(lockState: .connecting)
            ble.scan()
        case .unpaired:
            view.show(lockState: .disconnected)
            view.showHint(text: "tool_tip_reconnection".localized())
        case .failed(let error):
            if error.isEllipseTimeout {
                view.show(lockState: .disconnected)
                view.showHint(text: "tool_tip_reconnection".localized())
            } else {
                view.show(error: error, file: #file, line: #line)
            }
        case .ready where tripService.isQrBike:
            tripService.isQrBike = false
            ellipse.unlock()
        default:
            break
        }
        tripService.canSaveSteps = canSaveSteps
    }
    
    func ellipse(_ ellipse: LattisSDK.Ellipse, didUpdate security: LattisSDK.Ellipse.Security) {
        tripService.isLocked = isLockLocked
        canEndRide = isLockLocked
        guard view != nil else { return }
        view.show(lockState: security.slider)
        switch security {
        case .locked:
            widget.swithcLockState(to: true)
        case .unlocked:
            widget.swithcLockState(to: false)
        case .invalid, .middle:
            view.handleJamming()
            network.send(metadata: .jamming(tripService.bike), completion: {_ in})
        default:
            break
        }
    }
    
    func ellipse(_ ellipse: LattisSDK.Ellipse, didUpdate value: LattisSDK.Ellipse.Value) {
        switch value {
        case .metadata(let metadata):
            guard metadataSent == false else { return }
            metadataSent = true
            network.send(metadata: .lockBattery(metadata.batteryLevel, tripService.bike), completion: {_ in})
        case .firmwareVersion(let version):
            network.send(metadata: .firmware(version, tripService.bike), completion: {_ in})
        default:
            break
        }
    }
}

extension RideInteractor: DirectionsInteractorDelegate {
    func didSelect(direction: Direction) -> Bool {
        let annotation = MapAnnotation(model: direction)
        view.buildRoute(to: annotation)
        return true
    }
    
    func didSelectCurrentLocation() -> Bool {
        return true
    }
}

extension LattisSDK.Ellipse.Security {
    var slider: LockButton.LockState {
        switch self {
        case .locked:
            return .locked
        default:
            return .unlocked
        }
    }
}

extension Parking {
    func check(coordinate: CLLocationCoordinate2D) -> Bool {
        let center = CLLocation(latitude: self.coordinate.latitude, longitude: self.coordinate.longitude)
        let location = CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
        return center.distance(from: location) < 3
    }
}

extension ParkingZone {
    func check(coordinate: CLLocationCoordinate2D) -> Bool {
        switch geometry {
        case .circle(let circle):
            let center = CLLocation(latitude: circle.center.latitude, longitude: circle.center.longitude)
            let location = CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
            return center.distance(from: location) < circle.radius
        case .polygon(let points), .rectangle(let points):
            var bounds = points
            let polygon = MKPolygon(coordinates: &bounds, count: bounds.count)
            return polygon.contains(coordinate)
        default:
            return true
        }
    }
}

extension AxaBLE.Lock.Status {
    var slider: LockButton.LockState? {
        switch self {
        case .strongClosed:
            return .locked
        case .open:
            return .unlocked
        default:
            return nil
        }
    }
}

