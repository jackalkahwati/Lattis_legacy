//
//  LockSettingsLockSettingsInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 23/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

class LockSettingsInteractor: LockSettingsInteractorInput {
    weak var view: LockSettingsInteractorOutput!
    var router: LockSettingsRouter!
    var lock: Lock!
}
