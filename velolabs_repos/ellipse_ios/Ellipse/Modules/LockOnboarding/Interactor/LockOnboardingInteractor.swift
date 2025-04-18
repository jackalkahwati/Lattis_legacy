//
//  LockOnboardingLockOnboardingInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 16/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import LattisSDK
import Oval

class LockOnboardingInteractor {
    weak var view: LockOnboardingInteractorOutput! {
        didSet {
            errorHandler = OnboardinErrorHandler(view)
        }
    }
    var router: LockOnboardingRouter!
    var delegate: LockOnboardingDelegate?
    
    fileprivate var device: Ellipse.Device?
    fileprivate var pageTypes: [PageType] = [.choose]
    fileprivate let ownTypes: [PageType] = [.choose, .touch, .list, .rename, .pin]
    fileprivate let sharedTypes: [PageType] = [.choose, .share, .rename, .pin]
    fileprivate let ble = EllipseManager.shared
    fileprivate let storage: EllipseStorage = CoreDataStack.shared
    fileprivate var sharedHelper: SharedLockOnboardingHelper?
    fileprivate var errorHandler: ErrorHandler!
    fileprivate var network: LocksNetwork = Session.shared
    
    init() {
        ble.scan()
    }
    
    deinit {
        ble.stopScan()
    }
}

extension LockOnboardingInteractor: LockOnboardingInteractorInput {
    func didShowPage(at index: Int) {
        view.show(title: pageTypes[index].title)
    }
    
    func push() {
        view.pushPage()
    }
    
    func page(for index: Int) -> LockOnboardingPage {
        let type = pageTypes[index]
        return type.controller(with: self)
    }
    
    var numberOfPages: Int {
        return pageTypes.count
    }
}

extension LockOnboardingInteractor: OnboardingChoosePageDelegate {
    func onboardOwn() {
        pageTypes = ownTypes
        view.reload()
        push()
    }
    
    func onboardShared() {
        pageTypes = sharedTypes
        view.reload()
        push()
    }
}

extension LockOnboardingInteractor: OnboardingLocksPageDelegate {
    func blink(device: Ellipse.Device) {
        view.startLoading(text: "blinking_led".localized())
        device.peripheral.flashLED() { [weak self] error in
            if let e = error {
                self?.view.show(error: e)
            } else {
                self?.view.stopLoading(completion: nil)
            }
        }
    }
    
    func connect(device: Ellipse.Device) {
        let connect: (Ellipse) -> () = { [weak self] ellipse in
            guard let `self` = self else { return }
            self.device = device
            self.device?.ellipse = ellipse
            self.device?.connect(self)
            self.storage.save(ellipse)
        }
        view.startLoading(text: String(format: "connecting".localized(), device.name))
        network.registration(with: device.macId) { [weak self] result in
            switch result {
            case .success(let ellipse):
                connect(ellipse)
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
}

extension LockOnboardingInteractor: OnboardingPinPageDelegate {
    func save(pin: [Ellipse.Pin]) {
        guard let macId = device?.peripheral.macId else { return } // TODO:
        view.startLoading(text: "saving_new_pin_code".localized())
        network.save(pinCode: pin, forLock: macId) { [weak self] result in
            switch result {
            case .success:
                do {
                    try self?.device?.peripheral.set(pinCode: pin.map(Peripheral.Pin.init))
                    self?.delegate?.didFinishLockOnboarding()
                    self?.router.dismiss(true)
                    self?.view.stopLoading(completion: nil)
                    if var ellipse = self?.device?.ellipse {
                        ellipse.pin = pin
                        self?.storage.save(ellipse)
                    }
                    log(.custom(.onboarding), attributes: [.succeded(true)])
                } catch {
                    self?.errorHandler.handle(error: error)
                    log(.custom(.onboarding), error: error, attributes: [.succeded(false)])
                }
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
}

extension LockOnboardingInteractor: OnboardingRenamePageDelegate {
    func save(name: String) {
        guard var ellipse = device?.ellipse else { return } // TODO:
        view.startLoading(text: "saving_lock_name".localized())
        ellipse.name = name
        network.update(lock: ellipse) { [weak self] result in
            switch result {
            case .success(let ell):
                self?.view.stopLoading(completion: nil)
                ellipse = ell
                self?.device?.ellipse = ell
                self?.push()
                self?.storage.save(ellipse)
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func hideCloseButton() {
        view.hideCloseButton()
    }
    
    func getPlaceholder(completion: (String) -> ()) {
        if let name = CoreDataStack.shared.getLockName {
            completion(name)
        } else if let name = device?.name {
            completion(name)
        }
    }
}

extension LockOnboardingInteractor: OnboardingTouchPageDelegate {
    func next() {
        push()
    }
}

extension LockOnboardingInteractor: OnboardingSharePageDelegate {
    func connect(code: String) {
        view.startLoading(text: nil)
        network.acceptSharing(confirmationCode: code) { [weak self] result in
            switch result {
            case .success(let ellipse):
                self?.processShared(ellipse: ellipse)
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func processShared(ellipse: Ellipse) {
        storage.save(ellipse)
        self.sharedHelper = SharedLockOnboardingHelper(ellipse) { [unowned self] error in
            if let error = error {
                self.sharedHelper = nil
                self.errorHandler.handle(error: error)
                log(.share(.error), error: error)
                self.router.dismiss()
            } else {
                self.view.stopLoading(completion: nil)
                self.router.dismiss(true)
                log(.share(.sharingAccepted))
            }
        }
    }
}

extension LockOnboardingInteractor: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        switch connection {
        case .paired:
            view.stopLoading(completion: nil)
            device?.ellipse?.isCurrent = true
            push()
        case .failed(let error):
            errorHandler.handle(error: error)
        default:
            break
        }
    }
}

class OnboardinErrorHandler: ErrorHandler {
    override func handleBLE(error: EllipseError) {
        guard case .timeout = error else { return super.handleBLE(error: error) }
        view.show(warning: "warning_no_shared_lock_around".localized(), title: nil)
    }
    
    override func handleOval(error: SessionError) {
        guard case .conflict = error.code else { return super.handleOval(error: error) }
        view.show(warning: "ellipse_belongs_to_another_user_warning".localized(), title: nil)
    }
}
