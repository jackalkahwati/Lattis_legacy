//
//  LocksLocksInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 26/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

struct Locks {
    struct Section {
        let items: [Ellipse.Lock]
        let style: Style
        
        enum Style: String {
            case current = "currently_connected", previous = "previous_connections", unreachable = "out_of_range"
        }
    }
    
    enum Screen: String {
        case details
    }
}

protocol LocksInteractorInput {
    var numberOfsections: Int {get}
    func numberOfRows(in section: Int) -> Int
    func lock(for indexPath: IndexPath) -> Ellipse.Lock
    func style(for section: Int) -> Locks.Section.Style
    func start()
    func delete(lock: Ellipse.Lock)
    func connect(lock: Ellipse.Lock)
    func open(lock: Ellipse.Lock)
    func addNew()
}

protocol LocksInteractorOutput: InteractorOutput {
    func refresh()
    func setEpty(hidden: Bool)
}
