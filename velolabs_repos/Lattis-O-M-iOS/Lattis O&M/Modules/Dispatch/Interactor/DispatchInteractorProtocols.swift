//
//  DispatchDispatchInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol DispatchInteractorInput {
    var isLockLocked: Bool {get}
    func viewLoaded()
    func isCurrent(state: BikeState) -> Bool
    func select(state: BikeState)
    func set(lockState: LockSlider.LockState)
    func finish()
}

protocol DispatchInteractorOutput: LoaderPresentable, ErrorPresentable {
    func show(lock: Lock)
    func didSelect(state: BikeState, with error: Error?)
    func update(state: LockSlider.LockState)
    func showBack()
}

