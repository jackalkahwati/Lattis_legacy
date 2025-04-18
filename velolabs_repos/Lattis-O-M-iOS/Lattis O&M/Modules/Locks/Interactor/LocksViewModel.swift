//
//  LocksViewModel.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 8/9/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import LattisSDK
import Oval

class LocksViewModel: NSObject {
    typealias Storage = FleetsStorage & LocksStorage
    var change: (TableRefresh) -> () = {_ in}
    var locks: [Lock] {
        if let section = sections.last, case let .nearby(locks) = section {
            return locks
        }
        return []
    }
    fileprivate(set) var filter: Lock.Filter = .all
    fileprivate let network: EllipseNetwork = Session.shared
    fileprivate let storage: Storage = CoreDataStack.shared
    fileprivate var sections: [Section] = []
    fileprivate(set) var ellipses: [Ellipse] = []
    fileprivate var peripherals: [Peripheral] = []
    fileprivate var notAssigned: [String] = []
    fileprivate var handler: StorageHandler?
    
    enum Error: Swift.Error {
        case noCurrentFleet
    }
}

extension LocksViewModel: LocksTablePresentable {
    func onboard(lock: Lock, callback: @escaping (Bool) -> ()) {
        guard let macId = lock.macId else { return callback(false) }
        network.getFleetId(by: macId) { result in
            switch result {
            case .success(let fleetId):
                callback(fleetId == nil)
            case .failure(let e):
                print(e)
                callback(false)
            }
        }
    }
    
    var sectionsCount: Int {
        return sections.count
    }
    
    func itemsCount(for section: Int) -> Int {
        switch sections[section] {
        case .connected(let locks), .nearby(let locks):
            return locks.count
        }
    }
    
    func heightForHeader(in section: Int) -> CGFloat {
        let sec = sections[section]
        switch sec {
        case .nearby(_):
            return 38
        default:
            return 38
        }
    }
    
    func viewForHeader(for section: Int) -> UIView {
        let sec = sections[section]
        let view: LocksSectionView
        switch sec {
        case .connected(_):
            view = LocksSectionView()
        case .nearby(_):
            let header = LocksFilterSectionView()
            header.subtitleLabel.text = String(format: "locks_filter_header_title".localized(), filter.title)
            view = header
        }
        view.titleLabel.text = sec.title
        return view
    }
    
    func sectionTitle(for section: Int) -> String? {
        return sections[section].title
    }
    
    func lock(for indexPath: IndexPath) -> Lock {
        switch sections[indexPath.section] {
        case .connected(let locks), .nearby(let locks):
            return locks[indexPath.row]
        }
    }
    
    func refreshNetwork(completion: @escaping ([Ellipse], Swift.Error?) -> () = {_,_ in}) {
        guard let fleet = storage.currentFleet else { return completion([], Error.noCurrentFleet) }
        network.getLocks(for: fleet) { [weak self] result in
            switch result {
            case .success(let ellipses):
                // If there is no ellipses in that fleet and filter is off, automatically switch filtet to the 'fresh' Ellipses
                if ellipses.isEmpty, let s = self, case .all = s.filter {
                    s.filter = .notBelongToFleet
                }
                completion(ellipses, nil)
                self?.storage.save(ellipses, update: true, completion: {})
            case .failure(let error):
                completion([], error)
            }
        }
    }
    
    func start() {
        refreshNetwork()
        guard let fleet = storage.currentFleet else { return }
        handler = storage.subscribe(in: fleet) { [weak self] (ellipses) in
            self?.ellipses = ellipses
            self?.calculate()
        }
    }
    
    func lock(by macId: String) -> Lock? {
        var locks: [Lock] = []
        if let section = sections.first, case let .connected(ll) = section {
            locks += ll
        }
        if let section = sections.last, case let .nearby(ll) = section {
            locks += ll
        }
        return locks.filter({$0.macId == macId}).first
    }
    
    func update(filter: Lock.Filter) {
        guard filter != self.filter else { return }
        self.filter = filter
        calculate()
    }
    
    func calculate() {
        notAssigned = peripherals.map({$0.macId}).filter({self.storage.lock(with: $0) == nil})
        let lock: (Peripheral) -> Lock? = { per in
            let ellipse = self.ellipses.filter({$0.macId == per.macId}).first
            if self.notAssigned.contains(where: {$0 == per.macId}) == false && ellipse == nil {
                return nil
            }
            return Lock(peripheral: per, lock: ellipse)
        }
        var refresh = TableRefresh()
        var connected: [Lock] = []
        var nearby: [Lock] = []

        if let section = sections.first, case let .connected(locks) = section {
            connected = locks
        }
        if let section = sections.last, case let .nearby(locks) = section {
            nearby = locks
        }
        let ellipseRefresh: (Lock) -> () = { (lock) in
            if let ell = self.ellipses.filter({$0.macId == lock.peripheral!.macId}).first {
                lock.lock = ell
            } else {
                lock.lock = nil
            }
        }
        nearby.forEach(ellipseRefresh)
        connected.forEach(ellipseRefresh)

        var sectionIdx = connected.isEmpty ? 0 : 1

        let connectedPers = peripherals.filter({$0.isPaired})
        var toRemove = connected.filter { (lock) -> Bool in
            return !connectedPers.contains(where: {$0.macId == lock.macId})
            }.compactMap({$0.macId})
        while !toRemove.isEmpty {
            let macId = toRemove.removeLast()
            if let idx = connected.firstIndex(where: {$0.macId == macId}) {
                refresh.delete.append(IndexPath(row: idx, section: 0))
                connected.remove(at: idx)
            }
        }

        let notConnected = peripherals.filter({!$0.isPaired})
        toRemove = nearby.filter({ lock -> Bool in
            guard notConnected.contains(where: {$0.macId == lock.macId}) else { return true }
            switch self.filter {
            case .all:
                return lock.lock == nil
            case .notBelongToFleet:
                return lock.lock != nil
            case .noBike:
                return lock.lock?.bikeId != nil || lock.lock == nil
            case .bike:
                return lock.lock?.bikeId == nil || lock.lock == nil
            }
        }).compactMap({$0.macId})
        while !toRemove.isEmpty {
            let macId = toRemove.removeLast()
            if let idx = nearby.firstIndex(where: {$0.macId == macId}) {
                refresh.delete.append(IndexPath(row: idx, section: sectionIdx))
                nearby.remove(at: idx)
            }
        }

        sectionIdx = peripherals.contains(where: {$0.isPaired}) ? 1 : 0
        for peripheral in peripherals {
            guard let lock = lock(peripheral) else { continue }
            if peripheral.isPaired {
                if !connected.contains(where: {$0.peripheral?.macId == peripheral.macId})  {
                    refresh.insert.append(IndexPath(row: connected.count, section: 0))
                    connected.append(lock)
                }
            } else {
                if !nearby.contains(where: {$0.peripheral?.macId == peripheral.macId}) {
                    switch filter {
                    case .all:
                        guard lock.lock != nil else { continue }
                    case .bike:
                        guard lock.lock?.bikeId != nil else { continue }
                    case .noBike:
                        guard let l = lock.lock, l.bikeId == nil else { continue }
                    case .notBelongToFleet:
                        guard notAssigned.contains(where: {$0 == peripheral.macId}) else { continue }
                    }
                    let idx = nearby.count
                    refresh.insert.append(IndexPath(row: idx, section: sectionIdx))
                    nearby.insert(lock, at: idx)
                }
            }
        }

//        if nearby.isEmpty && filter != .all {
//            filter = .all
//            return calculate()
//        }

        if sections.count == 0 {
            if connected.isEmpty == false && nearby.isEmpty == false {
                refresh.insertSections = [0, 1]
            } else if !connected.isEmpty || !nearby.isEmpty {
                refresh.insertSections = IndexSet(integer: 0)
            }
        } else if sections.count == 1 {
            if connected.isEmpty == false && nearby.isEmpty == false {
                guard let section = sections.first else { return }
                switch section {
                case .connected:
                    refresh.insertSections = IndexSet(integer: 1)
                case .nearby:
                    refresh.insertSections = IndexSet(integer: 0)
                }
            } else if connected.isEmpty && nearby.isEmpty {
                refresh.deleteSections = IndexSet(integer: 0)
            }
        } else if sections.count == 2 {
            if connected.isEmpty && nearby.isEmpty {
                refresh.deleteSections = [0, 1]
            } else if !connected.isEmpty && nearby.isEmpty {
                refresh.deleteSections = IndexSet(integer: 1)
            } else if connected.isEmpty && !nearby.isEmpty {
                refresh.deleteSections = IndexSet(integer: 0)
                refresh.moveSections = (1, 0)
            }
        }

        sections.removeAll()
        if !connected.isEmpty {
            sections.append(.connected(connected))
        }
        if !nearby.isEmpty {
            sections.append(.nearby(nearby))
        }
        refresh.isHidden = sections.isEmpty
        DispatchQueue.main.async {
//            refresh.needsReload = true
            self.change(refresh)
        }
    }
}

extension LocksViewModel: EllipseManagerDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Peripheral], delete: [Peripheral]) {
        if peripherals.isEmpty {
            peripherals = lockManager.locks
        } else {
            if !insert.isEmpty {
                peripherals += insert
            }
            if !delete.isEmpty {
                var toRemove = delete
                while toRemove.isEmpty == false {
                    let per = toRemove.removeLast()
                    if let idx = self.peripherals.firstIndex(where: {$0.macId == per.macId}) {
                        self.peripherals.remove(at: idx)
                    }
                }
            }
        }
        
        calculate()
    }
}

private extension LocksViewModel {    
    enum Section {
        case connected([Lock])
        case nearby([Lock])
        
        var title: String {
            switch self {
            case .connected(_): return "locks_section_connected".localized()
            case .nearby(_): return "locks_section_nearby".localized()
            }
        }
    }
}
