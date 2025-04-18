//
//  BikeInfoCostCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 27/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class BikeInfoCell: UITableViewCell {
    enum RowModel: TableCellPresentable {
        case cost(Bike)
        case deposit(Bike)
        case about(Bike)
        case network(PrivateNetwork, Bike, showZones: (PrivateNetwork) -> (), openTerms: (Bike) -> ())
        case location(title: String, address: String)
        
        var rowHeight: CGFloat { return 44 }
        var identifire: String {
            switch self {
            case .cost, .deposit: return "text"
            case .about: return "main"
            case .network: return "network"
            case .location: return "location"
            }
        }
    }
    var model: TableCellPresentable?
}
