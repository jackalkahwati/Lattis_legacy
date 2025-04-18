//
//  FleetStorage.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 31/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Foundation

class FleetStorage {
    fileprivate let coreData = CoreDataStack.shared
    fileprivate var callback: (([Fleet]) -> ())?
    fileprivate var cached: [Fleet]?
    fileprivate let network: UserAPI = AppRouter.shared.api()
    fileprivate var subscryber: CoreDataStack.Subscriber<CDFleet>?
    
    init() {
        subscryber = coreData.subscribe { [unowned self] fleets in
            self.handle(fleets: fleets)
        }
    }
    
    func fetch(completion: @escaping ([Fleet]) -> ()) {
        callback = completion
        if let c = cached {
            completion(c)
        }
    }
    
    func refresh() {
        network.refresh { [weak self] (result) in
            switch result {
            case .success(let res):
                self?.cached = res.fleets
                self?.coreData.save(fleets: res.fleets)
                self?.coreData.save(user: res.user) {_ in}
            case .failure(let error):
                Analytics.report(error)
            }
        }
    }
    
    func addFleet(email: String, conrimationCode: String? = nil, compleion: @escaping (Error?) -> ()) {
        network.addPrivateNetwork(email: email, code: conrimationCode) { [weak self] (result) in
            switch result {
            case .success(let fleets):
                if let f = fleets {
                    self?.coreData.save(fleets: f)
                }
                compleion(nil)
            case .failure(let error):
                compleion(error)
            }
        }
    }
    
    fileprivate func handle(fleets: [CDFleet]) {
        let result = fleets.map(Fleet.init)
        cached = result
        callback?(result)
    }
}

extension CoreDataStack {
    func save(fleets: [Fleet]) {
        write(completion: { (context) in
            do {
                let current = try CDFleet.all(in: context)
                for fl in current {
                    if let update = fleets.first(where: {$0.fleetId == Int(fl.fleetId)}) {
                        fl.fill(update)
                    } else {
                        context.delete(fl)
                    }
                }
                let idS = current.map({Int($0.fleetId)})
                let new = fleets.filter({!idS.contains($0.fleetId)})
                new.forEach({ (fleet) in
                    let fl = CDFleet.create(in: context)
                    fl.fill(fleet)
                })
            } catch {
                Analytics.report(error)
            }
        }, fail: { error in
            Analytics.report(error)
        })
    }
}
