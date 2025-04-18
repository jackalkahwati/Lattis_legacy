//
//  LockDetailsLockDetailsInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 27/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

struct LockDetails {
    class Section {
        let title: String
        var items: [Info]
        
        init(title: String, items: [Info]) {
            self.title = title
            self.items = items
        }
    }
    
    enum Info {
        case name(String?, Bool)
        case owner(String?)
        case serial(String)
        case firmware(String, Bool)
        case sensetivity(Ellipse.Sensetivity)
        case pin([Ellipse.Pin])
        
        var identifire: String {
            switch self {
            case .pin(_):
                return "pin"
            case .firmware(_, let available) where available:
                return "fw"
            case .sensetivity:
                return "sensetivity"
            default:
                return "info"
            }
        }
    }
}

protocol LockDetailsInteractorInput: TableViewPresentable, OnboardingPinPageDelegate, LockDetailsSensetivityCellDelegate {
    var changelog: String? {get}
    func update()
    func start()
    func item(for indexPath: IndexPath) -> LockDetails.Info
    func title(for section: Int) -> String
    func deleteLock()
    func select(itemAt indexPath: IndexPath)
    func isLockShared() -> Bool
}

protocol LockDetailsInteractorOutput: InteractorOutput {
    func refresh()
    func reloadRows(at indexPaths: [IndexPath])
    func beginFWUpdate()
    func updateFW(progress: Float)
    func finishFWUpdate()
}

protocol TableViewPresentable {
    var numberOfSections: Int {get}
    func numberOfRows(in section: Int) -> Int
}
