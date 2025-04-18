//
//  BikeInfoNetworkCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 27/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

class BikeInfoNetworkCell: BikeInfoCell {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var termsButton: UIButton!
    
    override var model: TableCellPresentable? {
        didSet {
            if let model = model as? RowModel, case let .network(network, bike, showZones: _, openTerms: _) = model {
                titleLabel.text = network.name
                termsButton.isEnabled = bike.termsLink != nil
                termsButton.alpha = termsButton.isEnabled ? 1 : 0.5
            } else {
                titleLabel.text = nil
            }
        }
    }
    
    @IBAction func showZones(_ sender: Any) {
        if let model = model as? RowModel, case let .network(network, _, showZones, _) = model {
            showZones(network)
        }
    }
    
    @IBAction func openTerms(_ sender: Any) {
        if let model = model as? RowModel, case let .network(_, bike, _, openTerms) = model {
            openTerms(bike)
        }
    }
}
