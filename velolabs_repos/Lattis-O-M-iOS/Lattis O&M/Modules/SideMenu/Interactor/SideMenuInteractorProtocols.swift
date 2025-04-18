//
//  SideMenuSideMenuInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 05/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

protocol SideMenuInteractorInput {
    func viewLoaded()
    func select(fleet: Fleet)
}

protocol SideMenuInteractorOutput: LoaderPresentable, ErrorPresentable {
    func show(fleets: [Fleet])
}
