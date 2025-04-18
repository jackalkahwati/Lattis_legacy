//
//  DashboardDashboardInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol DashboardInteractorInput: PermissionsDelegate {
    func viewDidLoad()
    func unlock()
    func lock()
    func onboard()
    func setAutoLock(enabled: Bool)
    func setAutoUnlock(enabled: Bool)
    func setTheftAlert(enabled: Bool)
    func setCrashAlert(enabled: Bool)
    func checkContacts() -> Bool
    func updateFW()
    func openSettigns()
}

protocol DashboardInteractorOutput: InteractorOutput {
    func show(state: LockControl.LockState)
    func show(device: Ellipse.Device)
    func show(batteryLevel: Double, and rssiStreight: Double)
    func setAutoLock(enabled: Bool)
    func setAutoUnlock(enabled: Bool)
    func setTheftAlert(enabled: Bool)
    func setCrashAlert(enabled: Bool)
    func showUpdateDialog(changelog: String?)
    func beginFWUpdate()
    func updateFW(progress: Float)
    func finishFWUpdate()
}
