//
//  DashboardPageController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 12/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Tabman

class DashboardPageController: TabmanViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()

        bar.appearance = TabmanBar.Appearance({ (appearance) in
            appearance.indicator.color = .lsTurquoiseBlue
            appearance.state.color = .lsCoolGrey
            appearance.state.selectedColor = .lsTurquoiseBlue
            appearance.interaction.isScrollEnabled = false
        })
        bar.style = .buttonBar
//        isScrollEnabled = false
    }
}
