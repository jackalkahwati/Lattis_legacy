//
//  EmergencyEmergencyInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 14/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol EmergencyDelegate: class {
    func didCloseEmergency()
}

protocol EmergencyInteractorInput: TableViewPresentable, EmergencyCellDelegate {
    func start()
    func selectContacts()
    func item(for indexPath: IndexPath) -> Contact
    func requestAccess()
}

protocol EmergencyInteractorOutput: InteractorOutput {
    func refresh()
    func showHint()
    func hideHint()
}
