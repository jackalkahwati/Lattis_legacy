//
//  LocksLocksInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

protocol LocksInteractorDelegate: class {
    var vendor: Lock.Vendor { get }
    var controller: UIViewController { get }
    func dispatch(lock: Lock)
    func settings(lock: Lock)
    func change(vendor: Lock.Vendor, filter: Lock.Filter)
}

struct TableRefresh {
    var delete: [IndexPath]
    var update: [IndexPath]
    var insert: [IndexPath]
    var insertSections: IndexSet?
    var deleteSections: IndexSet?
    var moveSections: (from: Int, to: Int)?
    var reloadSetions: IndexSet?
    var isHidden: Bool
    var needsReload: Bool
    
    init() {
        insertSections = nil
        deleteSections = nil
        moveSections = nil
        reloadSetions = nil
        self.delete = []
        self.update = []
        self.insert = []
        self.isHidden = false
        self.needsReload = false
    }
    
    var isEmpty: Bool {
        insertSections == nil &&
            deleteSections == nil &&
            moveSections == nil &&
            reloadSetions == nil &&
            delete.isEmpty &&
            update.isEmpty &&
            insert.isEmpty
    }
}

protocol LocksTablePresentable: class {
    var sectionsCount: Int {get}
    var change: (TableRefresh) -> () {get set}
    var filter: Lock.Filter {get}
    func onboard(lock: Lock, callback: @escaping (Bool) -> ())
    func update(filter: Lock.Filter)
    func itemsCount(for section: Int) -> Int
    func heightForHeader(in section: Int) -> CGFloat
    func sectionTitle(for section: Int) -> String?
    func viewForHeader(for section: Int) -> UIView
    func lock(for indexPath: IndexPath) -> Lock
    func start()
    func refreshNetwork(completion: @escaping ([Ellipse], Error?) -> ())
    func calculate()
    func lock(by macId: String) -> Lock?
}

protocol LocksInteractorInput {
    var delegate: LocksInteractorDelegate? {get set}
    var viewModel: LocksTablePresentable {get}
    func viewLoaded()
    func connect(lock: Lock)
    func disconnect(lock: Lock)
    func flashLED(for lock: Lock)
    func refresh()
    func addLock()
    func scanQRCode()
    func viewForHeader(for section: Int) -> UIView
    func changeFilter()
}

protocol LocksInteractorOutput: LoaderPresentable, ErrorPresentable {
    func connectonFailed()
    func update(bluetoothState: Bool)
}
