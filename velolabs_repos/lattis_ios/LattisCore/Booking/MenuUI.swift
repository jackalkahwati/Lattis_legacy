//
//  MenuUI.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 14.03.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SideMenu

struct MenuUI {
    static var shared = MenuUI()
    var conroller: SideMenuController? = nil
    
    func showMenu() {
        conroller?.revealMenu()
    }
}
