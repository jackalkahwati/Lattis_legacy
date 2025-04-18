//
//  SettingsSettingsInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 18/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol SettingsInteractorInput: SettingsCellDelegate {
    func viewLoaded()
    func dispatch()
    func select(group: Group)
    func delete()
}

protocol SettingsInteractorOutput: LoaderPresentable, ErrorPresentable {
    func show(lock: Lock)
    func update(progress: Double)
    func showLabelDialog(completion: @escaping () -> ())
    func show(groups: [Group], bike: QRCodeBike)
}

