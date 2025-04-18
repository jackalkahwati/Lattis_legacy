//
//  MenuMenuInteractorProtocols.swift
//  Lattis
//
//  Created by Ravil Khusainov on 27/02/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

protocol MenuInteractorInput {
    func home()
    func profile()
    func damage()
    func theft()
    func billing()
    func history()
    func viewLoaded()
    func help()
}

protocol MenuInteractorOutput: BaseInteractorOutput {
    func updateMenu(withDamage enabled: Bool)
    func reload(selected: Int)
}
