//
//  Collections.swift
//  Lattis
//
//  Created by Ravil Khusainov on 7/4/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

protocol CellPresentable {
    var identifire: String {get}
}

protocol TableCellPresentable: CellPresentable {
    var rowHeight: CGFloat {get}
}
