//
//  PricingOptionsViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 03.08.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Model
import Cartography

final class PricingOptionsView: UIView {
    
    let controller: PricingOptionsController
    
    let confirmButton = ActionButton()
    fileprivate let tableView = UITableView()
    
    init(_ controller: PricingOptionsController) {
        self.controller = controller
        super.init(frame: .zero)
        
        addSubview(tableView)
        addSubview(confirmButton)
        
        constrain(tableView, confirmButton, self) { table, button, view in
            table.top == view.top
            table.leading == view.leading
            table.trailing == view.trailing
            table.height == 250
            
            table.bottom == button.top - .margin
            button.leading == view.leading
            button.trailing == view.trailing
            button.bottom == view.bottom
        }
        
        tableView.register(PricingOptionCell.self, forCellReuseIdentifier: "cell")
        tableView.delegate = controller
        tableView.dataSource = controller
        tableView.tableFooterView = UIView()
        tableView.separatorStyle = .none
        tableView.sectionFooterHeight = 12
        tableView.rowHeight = 44
//        tableView.contentInset = .init(top: 0, left: -15, bottom: 0, right: 0)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class PricingOptionCell: UITableViewCell {
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        textLabel?.font = .theme(weight: .bold, size: .body)
        tintColor = .black
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override var isSelected: Bool {
        didSet {
            accessoryType = isSelected ? .checkmark : .none
        }
    }
}
