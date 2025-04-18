//
//  FindRideInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 07/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation
import CoreLocation
import Oval
import Mapbox
import Localize_Swift
import LattisSDK
import QRCodeReader
import AVFoundation

public final class FindRideInteractor: NSObject {
    typealias Storage = UserStorage & CreditCardStorage
    weak var view: FindRideInteractorOutput!
    var router: FindRideRouter!
    
    fileprivate var userCoordinate = kCLLocationCoordinate2DInvalid {
        didSet {
            asyncSearch?()
            asyncSearch = nil
        }
    }
    
    fileprivate let locationManager = CLLocationManager()
    fileprivate var selectedBike: Bike?
    fileprivate let network: BikeNetwork
    fileprivate let storage: Storage
    fileprivate var isBluetoothEnabled: Bool {
        return ble.isOn
    }
    fileprivate var asyncSearch: (() -> ())?
    fileprivate var qrBike: Bike?
    fileprivate var tripService: TripService?
    fileprivate let ble = EllipseManager.shared
    fileprivate var connectionTimer: Timer?
    fileprivate weak var qrViwe: FindQRView?
    fileprivate var scannerTimer: Timer?
    fileprivate var qrParser: (bike: QRCodeBike, getInfo: (QRResult) -> ())?
    fileprivate var pickUpLocation: Direction? {
        didSet {
            guard let location = pickUpLocation else { return view.show(userLocationTitle: "pick_up_location".localized()) }
            view.show(userLocationTitle: location.address ?? "find_ride_address_not_recognized".localized())
        }
    }
    fileprivate var isActive = true
    
    init(network: BikeNetwork = Session.shared, storage: Storage = CoreDataStack.shared) {
        self.network = network
        self.storage = storage
        super.init()
        ble.subscribe(handler: self)
    }
    
    fileprivate func handle(_ error: Error, file: String = #file, line: Int = #line) {
        if error is Err {
            return view.warning(with: "find_ride_qr_fail_title".localized(), subtitle: "qr_error_ellipse_not_around".localized())
        } else if let err = error as? EllipseError {
            if case .accessDenided = err {
                return view.warning(with: "ellipse_access_denided_title".localized(), subtitle: "ellipse_access_denided_text".localized())
            } else {
                return view.warning(with: "find_ride_qr_fail_title".localized(), subtitle: "qr_error_ellipse_not_connected".localized())
            }
        } else if let error = error as? SessionError, case .resourceNotFound = error.code {
            self.view.show(result: .noService, userLocation: nil)
        } else {
            view.show(error: error, file: file, line: line)
        }
        tripService = nil
        qrBike = nil
        connectionTimer?.invalidate()
        connectionTimer = nil
//        ble.unsubsribe(self)
        
        Analytics.report(error)
    }
    
    fileprivate func canBook(bike: Bike) -> Bool {
        if !isBluetoothEnabled {
            view.warning(with: "find_ride_no_bluettoth_title".localized(), subtitle: "find_ride_no_bluettoth_text".localized())
            return false
        }
        guard storage.shouldAddPhoneNumber(for: bike) else { return true }
        let alert = ActionAlertView.alert(title: "mandatory_phone_title".localized(), subtitle: "mandatory_phone_text".localized())
        alert.action = .init(title: "mandatory_phone_action".localized()) {
            AppRouter.shared.addPhoneNumber()
        }
        alert.cancel = .init(title: "cancel".localized()) {}
        alert.show()
        return false
    }
}

// MARK: - FindRideInteractorInput methods
extension FindRideInteractor: FindRideInteractorInput  {    
    func viewLoaded() {
        AppRouter.shared.onStart = { [unowned self] shouldSearch in
            if shouldSearch {
                self.isActive = true
                self.search()
            } else {
                self.view.stopLoading(completion: nil)
            }
        }
        locationManager.delegate = self
    }
    
    func search() {
        guard AppRouter.shared.checkConnection() else { return }
        guard isBluetoothEnabled else { return view.warning(with: "find_ride_no_bluettoth_title".localized(), subtitle: "find_ride_no_bluettoth_text".localized()) }
        guard CLLocationManager.authorizationStatus() != .denied else { return view.warning(with: "privacy_location_alert_title".localized(), subtitle: "privacy_location_alert_text".localized())}
        let coordinate: CLLocationCoordinate2D
        if let pickUp = pickUpLocation {
            coordinate = pickUp.coordinate
        } else if CLLocationCoordinate2DIsValid(userCoordinate) == false {
            asyncSearch = { [unowned self] in self.search() }
            return
        } else {
            coordinate = userCoordinate
        }
//        let coordinate = userCoordinate
        view.startLoading(with: "find_ride_search".localized())
        network.find(in: coordinate) { [weak self] (result) in
            switch result {
            case .success(let search):
                self?.view.show(result: search, userLocation: self?.pickUpLocation)
            case .failure(let error):
                self?.handle(error)
            }
        }
    }
    
    func update(userCoordinate: CLLocationCoordinate2D) {
        guard pickUpLocation == nil, self.userCoordinate.isWithin(3, of: userCoordinate) == false else { return }
        if !CLLocationCoordinate2DIsValid(self.userCoordinate) {
            search()
        }
        self.userCoordinate = userCoordinate
    }
    
    func bookSelectedBike() {
        guard let bike = selectedBike, canBook(bike: bike) else { return }
        if (bike.fleetType == .publicPay || bike.fleetType == .privatePay) && storage.currentCard == nil {
            router.openPayments()
            return
        }
        view.closeSelection()
        isActive = false
        router.openRoute(to: bike)
    }
    
    func selectedBikeInfo() {
        guard let bike = selectedBike else { return }
        router.openInfo(for: bike) { $0.delegate = self }
    }
    
    func selectBike(with annotation: MapAnnotation) {
        selectedBike = annotation.model as? Bike
    }
    
    func unselectBike() {
        selectedBike = nil
    }
    
    func openMenu() {
        router.openMenu() { $0.bike = nil }
    }
    
    func openTerms(with link: URL) {
        router.openTems(with: link)
    }
    
    func addPrivateNetwork() {
        AppRouter.shared.addPrivateNetwork()
    }
    
    func scanQRCode() {
        qrBike = nil
        if router.openQRScanner(with: self) == false {
            view.warning(with: "general_error_title".localized(), subtitle: "qr_error_not_avaliable".localized())
        }
    }
    
    func choosePickUp() {
        router.openDirectons(with: self, title: "directions_pick_up_location_title".localized())
    }
}

extension FindRideInteractor: QRCodeReaderViewControllerDelegate {
    public func reader(_ reader: QRCodeReaderViewController, didScanResult result: QRCodeReaderResult) {
        
    }
    
    public func reader(_ reader: QRCodeReaderViewController, didSwitchCamera newCaptureDevice: AVCaptureDeviceInput) {}
    
    public func readerDidCancel(_ reader: QRCodeReaderViewController) {
        reader.dismiss(animated: true, completion: nil)
    }
}

extension FindRideInteractor: FindQRViewDelegate {
    func addCreditCard(qrViwe: FindQRView) {
        self.qrViwe = qrViwe
        router.openPayments() { [unowned self] in
            if let view = self.qrViwe {
                view.checkCreditCard()
            }
        }
    }
    
    func qrView(view: FindQRView, unlock bike: Bike) {
        qrBike = bike
        router.dismiss {
            guard self.canBook(bike: bike) else { return }
            self.view.startLoading(with: "route_to_bike_start".localized())
            self.ble.scan(with: self)
            self.connectionTimer = Timer.after(30, { [weak self] in
                self?.handle(Err.ellipseNotFound)
            })
        }
    }
    
    func qrView(viwe: FindQRView, show bike: Bike) {
        router.openInfo(for: bike, configure: { $0.delegate = self })
    }
    
    func qrView(view: FindQRView, bike: QRCodeBike, getInfo: @escaping (QRResult) -> ()) -> Bool {
        qrParser = (bike, getInfo)
        guard scannerTimer == nil else {
            resetTimer()
            getInfo(.multiple)
            return false
        }
        qrParser = nil
        resetTimer()
        parse(bike: bike, getInfo: getInfo)
        return true
    }
    
    
}

extension FindRideInteractor: CLLocationManagerDelegate {
    public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        switch status {
        case .denied:
            view.stopLoading(completion: nil)
        default:
            break
        }
    }
}

extension FindRideInteractor: EllipseManagerDelegate {
    public func manager(_ lockManager: EllipseManager, didUpdateConnectionState connected: Bool) {
        guard isActive else { return }
        if connected == false && isBluetoothEnabled && view != nil {
            view.warning(with: "find_ride_no_bluettoth_title".localized(), subtitle: "find_ride_no_bluettoth_text".localized())
        } else {
            search()
        }
    }
    
    public func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [LattisSDK.Ellipse], delete: [LattisSDK.Ellipse]) {
        guard let bike = qrBike, let per = lockManager.locks.filter({ $0.macId == bike.macId }).first, tripService == nil else { return }
        network.book(bike: bike, coordinate: userCoordinate) { [weak self] (result) in
            switch result {
            case .success(_, _):
                per.connect(handler: self, bike: bike)
            case .failure(let error):
                self?.handle(error)
            }
        }
        tripService = TripService(bike)
        tripService?.onFail = { [weak self] error in
            self?.handle(error)
        }
        tripService?.onStart = { [unowned self] in
            per.unlock()
            self.isActive = false
            self.router.openRide(tripService: self.tripService!, lock: Lock(peripheral: per))
            self.tripService = nil
            self.qrBike = nil
            self.connectionTimer?.invalidate()
            self.connectionTimer = nil
            self.view?.stopLoading(completion: nil)
        }
    }
}

extension FindRideInteractor: EllipseDelegate {    
    public func ellipse(_ ellipse: LattisSDK.Ellipse, didUpdate security: LattisSDK.Ellipse.Security) {
        
    }
    
    public func ellipse(_ ellipse: LattisSDK.Ellipse, didUpdate connection: LattisSDK.Ellipse.Connection) {
        switch connection {
        case .paired:
            tripService?.isQrBike = true
            tripService?.start(with: userCoordinate)
        case .failed(let error):
            handle(error)
        default:
            break
        }
    }
}

extension FindRideInteractor: BikeInfoInteractorDelegate {
    func bikeInfoBook() {
        bookSelectedBike()
    }
}

extension FindRideInteractor: DirectionsInteractorDelegate {
    func didSelect(direction: Direction) -> Bool {
        pickUpLocation = direction
        search()
        return true
    }
    
    func didSelectCurrentLocation() -> Bool {
        pickUpLocation = nil
        search()
        return true
    }
}

// MARK: - Private methods
private extension FindRideInteractor {
    func resetTimer() {
        scannerTimer?.invalidate()
        scannerTimer = Timer.scheduledTimer(withTimeInterval: 3, repeats: false, block: { [weak self] _ in
            if let parser = self?.qrParser {
                self?.parse(bike: parser.bike, getInfo: parser.getInfo)
                self?.qrParser = nil
            }
            self?.scannerTimer?.invalidate()
            self?.scannerTimer = nil
        })
    }
    
    func parse(bike: QRCodeBike,getInfo:  @escaping (QRResult) -> ()) {
        self.network.getBike(by: nil, qrCode: bike.id) { [weak self] (result) in
            switch result {
            case .success(let bike):
                getInfo(.success(bike))
                self?.qrBike = bike
            case .failure(let error):
                var string = "qr_error_server"
                if let err = error as? SessionError {
                    switch err.code {
                    case .unauthorized:
                        string = "qr_error_fleet_access_denied"
                    case .resourceNotFound:
                        string = "qr_error_bike_not_found"
                    case .conflict:
                        string = "qr_error_bike_is_rent"
                    default: break
                    }
                }
                getInfo(.fail(string.localized()))
            }
        }
    }
    
    enum Err: Error {
        case ellipseNotFound
    }
}

