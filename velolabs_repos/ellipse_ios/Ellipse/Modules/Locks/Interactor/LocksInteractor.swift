//
//  LocksLocksInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 26/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation
import LattisSDK
import Oval

class LocksInteractor {
    weak var view: LocksInteractorOutput! {
        didSet {
            errorHandler = ErrorHandler(view)
        }
    }
    var router: LocksRouter!
    
    fileprivate let ble = EllipseManager.shared
    fileprivate let storage: EllipseStorage = CoreDataStack.shared
    fileprivate let network: LocksNetwork = Session.shared
    
    fileprivate var locks: [Ellipse.Lock] = []
    fileprivate var peripherals: [Peripheral] = []
    fileprivate var source: [Locks.Section] = []
    fileprivate var handler: StorageHandler?
    fileprivate var errorHandler: ErrorHandler!
    
    init() {
        ble.scan(with: self)
    }
    
    deinit {
        ble.stopScan()
    }
}

extension LocksInteractor: LocksInteractorInput {
    func start() {
        self.handler = storage.ellipses { [unowned self] ellipses in
            self.locks = ellipses.map{ Ellipse.Lock($0) }
            self.view.setEpty(hidden: ellipses.isEmpty == false)
            self.calculate()
        }
    }
    
    var numberOfsections: Int {
        return source.count
    }
    
    func numberOfRows(in section: Int) -> Int {
        return source[section].items.count
    }
    
    func lock(for indexPath: IndexPath) -> Ellipse.Lock {
        return source[indexPath.section].items[indexPath.row]
    }
    
    func style(for section: Int) -> Locks.Section.Style {
        return source[section].style
    }
    
    func delete(lock: Ellipse.Lock) {
        view.startLoading(text: "deleting".localized())
        network.delete(lock: lock.ellipse) { [weak self] result in
            switch result {
            case .success:
                self?.view.stopLoading(completion: nil)
                self?.storage.delete(ellipse: lock.ellipse)
                lock.delete()
                log(.custom(.deleteLock), attributes: [.screen("Locks")])
            case .failure(let error):
                self?.errorHandler.handle(error: error)
            }
        }
    }
    
    func connect(lock: Ellipse.Lock) {
        lock.connect(self)
        view.startLoading(text: "connecting".localized())
    }
    
    func open(lock: Ellipse.Lock) {
        router.open(lock: lock)
    }
    
    func addNew() {
        router.openOnboarding()
    }
}

extension LocksInteractor: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Peripheral], delete: [Peripheral]) {
        self.peripherals = lockManager.locks
        calculate()
    }
}

extension LocksInteractor: EllipseDelegate {
    func ellipse(_ ellipse: Peripheral, didUpdate security: Peripheral.Security) {
        
    }
    
    func ellipse(_ ellipse: Peripheral, didUpdate connection: Peripheral.Connection) {
        switch connection {
        case .paired:
            if let idx = locks.index(where: { $0.macId == ellipse.macId }) {
                var ellipse = locks[idx].ellipse
                ellipse.connectedAt = Date()
                ellipse.isCurrent = true
                storage.save(ellipse)
            }
            view.stopLoading(completion: nil)
        case .failed(let error):
            view.show(error: error)
        default:
            break
        }
        calculate()
    }
}

private extension LocksInteractor {
    func calculate() {
        var connected: [Ellipse.Lock] = []
        var disconnected: [Ellipse.Lock] = []
        var unreachable: [Ellipse.Lock] = []
        let notDisconnected: (Peripheral) -> Bool = { e in
            switch e.connection {
            case .reconnecting:
                return false
            default:
                return true
            }
        }
        for lock in locks {
            if let idx = peripherals.filter(notDisconnected).index(where: { $0.macId == lock.macId }) {
                lock.peripheral = peripherals[idx]
                lock.peripheral?.subscribe(self)
                if lock.isConnected {
                    connected.append(lock)
                } else {
                    disconnected.append(lock)
                }
            } else {
                unreachable.append(lock)
            }
        }
        source.removeAll()
        if connected.isEmpty == false {
            source.append(Locks.Section(items: connected, style: .current))
        }
        if disconnected.isEmpty == false {
            source.append(Locks.Section(items: disconnected, style: .previous))
        }
        if unreachable.isEmpty == false {
            source.append(Locks.Section(items: unreachable, style: .unreachable))
        }
        if let view = view {
            view.refresh()
        }
    }
}

