//
//  ShareShareInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 07/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol ShareInteractorInput: TableViewPresentable, SharingLockCellDelegate {
    func start()
    func item(for indexPath: IndexPath) -> Ellipse.Shared
    func addNew()
}

protocol ShareInteractorOutput: InteractorOutput {
    func refresh()
    func setEmpty(hidden: Bool)
    func showHint()
}
