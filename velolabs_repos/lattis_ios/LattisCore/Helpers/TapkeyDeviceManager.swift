//
//  TapkeyDeviceManager.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 22.06.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import TapkeyMobileLib
import Model
import OvalAPI
import CoreBluetooth

final class TapkeyDeviceManager: NSObject, DeviceRepresenting {
    
    let thing: Thing
    var kind: Device.Kind = .tapkey
    var security: Device.Security = .locked { didSet { sendState() }}
    var connection: Device.Connection = .search { didSet { sendState() }}
    var consent: String? = nil
    var qrCode: String? = nil
    var bleRestricted: Bool = false
    
    fileprivate var central: CBCentralManager!
    fileprivate var serviceFactory: TKMServiceFactory!
    fileprivate var scanner: TKMBleLockScanner!
    fileprivate var locksRegistration: TKMObserverRegistration?
    fileprivate var scannerRegistration: TKMObserverRegistration?
    fileprivate var facade: TKMCommandExecutionFacade!
    fileprivate var communicator: TKMBleLockCommunicator!
    fileprivate let timeoutToken = TKMCancellationTokens.None
    fileprivate var credentials: Credentials?
    fileprivate let api = Session.shared
    fileprivate let searchTimeout: TimeInterval = 30
    fileprivate var searchTimer: Timer?
    
    
    init(_ thing: Thing) {
        self.thing = thing
        super.init()
        self.central = CBCentralManager.init(delegate: self, queue: .main)
        self.serviceFactory = TKMServiceFactoryBuilder().setTokenRefreshHandler(TapkeyRefreshHandler(thing: thing, onRefresh: {
            self.credentials = $0
        })).build()
        scanner = serviceFactory.bleLockScanner
        facade = serviceFactory.commandExecutionFacade
        communicator = serviceFactory.bleLockCommunicator
        sendState()
    }
    
    func lock() {
        guard security == .unlocked else { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2, execute: { [weak self] in
            self?.security = .locked
        })
    }
    
    func unlock() {
//        guard let cred = credentials else { return login(unlock: true) }
//        security = .progress
//        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
//            self.security = .unlocked
//            self.lock()
//        }
//        return
        guard let cred = credentials else { return login(unlock: true) }
        security = .progress
        search { [unowned self] lock in
            self.communicator.executeCommandAsync(bluetoothAddress: lock.bluetoothAddress, physicalLockId: cred.physicalLockId, commandFunc: { [unowned self] connection in
                return self.facade.triggerLockAsync(connection, cancellationToken: self.timeoutToken)
            }, cancellationToken: self.timeoutToken)
            .continueOnUi { [unowned self] result -> Bool in
                self.security = .unlocked
                self.lock()
                let code = result?.code ?? TKMCommandResult.TKMCommandResultCode.technicalError
                switch code {
                case .ok:
                    return true
                default:
                    return false
                }
            }
            .catchOnUi { [unowned self] error in
                print(error.syncSrcError, error.asNSError)
                Analytics.report(error.syncSrcError)
                self.send(.failure(error))
                return false
            }
            .conclude()
        }
    }
    
    func connect() {
        guard connection != .connected else { return sendState() }
        connection = .connecting
        if serviceFactory.userManager.users.isEmpty || credentials == nil {
            login()
        }
    }
    
    func disconnect() {
        scannerRegistration?.close()
        if let user = serviceFactory.userManager.users.first {
            serviceFactory.userManager.logOutAsync(userId: user, cancellationToken: timeoutToken)
                .conclude()
        }
    }
    
    func refreshStatus() {
        sendState()
    }
}

fileprivate extension TapkeyDeviceManager {
    func login(unlock: Bool = false) {
        guard serviceFactory.userManager.users.isEmpty else {
            self.connect()
            return
        }
        api.getTapkeyCredentials(lockId: thing.key, fleetId: thing.fleetId)
            .continueAsyncOnUi { [unowned self] credentials -> TKMPromise<String> in
                self.credentials = credentials
                return self.serviceFactory.userManager.logInAsync(accessToken: credentials!.token, cancellationToken: self.timeoutToken)
            }
            .continueOnUi { [unowned self] userId -> Void in
                self.security = .locked
                self.connection = .connected
                if unlock {
                    self.unlock()
                }
//                self.search { [weak self] in
//                    self?.security = .locked
//                    self?.connection = .connected
//                    if unlock {
//                        self?.unlock()
//                    }
//                }
            }
            .catchOnUi { [unowned self] e -> Void in
                self.send(.failure(e))
            }
            .conclude()
    }
    
    func search(completion: @escaping (TKMBleLock) -> Void) {
        if let cred = credentials, let lock = scanner.getLock(physicalLockId: cred.physicalLockId) {
            return completion(lock)
        }
        locksRegistration = scanner.observable
            .addObserver { [unowned self] locks in
                if let id = credentials?.physicalLockId, let lock = self.scanner.getLock(physicalLockId: id) {
                    self.locksRegistration?.close()
                    self.locksRegistration = nil
                    self.searchTimer?.invalidate()
                    completion(lock)
                }
            }
        scannerRegistration = scanner.startForegroundScan()
        searchTimer?.invalidate()
        searchTimer = Timer.scheduledTimer(withTimeInterval: searchTimeout, repeats: false, block: { [weak self] timer in
            timer.invalidate()
            self?.locksRegistration?.close()
            self?.scannerRegistration?.close()
            self?.send(.failure(Failure.searchTimeout))
        })
    }
}

extension TapkeyDeviceManager: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        let enabled = central.state == .poweredOn
        bleRestricted = !enabled
        send(.bleEnabled(enabled))
        if enabled && security == .undefined {
            security = .locked
            connection = .connected
        } else {
            security = .undefined
        }
    }
}


fileprivate final class TapkeyRefreshHandler: TKMTokenRefreshHandler {
    let fleetId: Int
    let lockId: String
    let onRefresh: (TapkeyDeviceManager.Credentials?) -> Void
    
    init(thing: Thing, onRefresh: @escaping (TapkeyDeviceManager.Credentials?) -> Void) {
        self.lockId = thing.key
        self.fleetId = thing.fleetId
        self.onRefresh = onRefresh
    }
    
    func refreshAuthenticationAsync(userId: String, cancellationToken: TKMCancellationToken) -> TKMPromise<String> {
        Session.shared.getTapkeyCredentials(lockId: lockId, fleetId: fleetId)
            .continueAsyncOnUi { cred in
                let source = TKMPromiseSource<String>()
                if let token = cred?.token {
                    source.setResult(token)
                    self.onRefresh(cred)
                } else {
                    source.setError(TapkeyDeviceManager.Failure.refreshFailed(self.lockId))
                }
                return source.promise
            }
    }
    
    func onRefreshFailed(userId: String) {
        Analytics.report(TapkeyDeviceManager.Failure.refreshFailed(userId))
    }
}

fileprivate extension Session {
    func getTapkeyCredentials(lockId: String, fleetId: Int) -> TKMPromise<TapkeyDeviceManager.Credentials> {
        let source = TKMPromiseSource<TapkeyDeviceManager.Credentials>()
        send(.get(.init(path: "tapkey/credentials/\(lockId.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed)!)?fleetId=\(fleetId)"))) { (result: Result<TapkeyDeviceManager.Credentials, Error>) in
            switch result {
            case .failure(let error):
                source.setError(error)
            case .success(let token):
                source.setResult(token)
            }
        }
        return source.promise
    }
}

fileprivate extension TapkeyDeviceManager {
    struct Credentials: Codable {
        let token: String
        let physicalLockId: String
    }
    
    enum Failure: Error {
        case refreshFailed(String)
        case lockNotFound
        case searchTimeout
    }
}
