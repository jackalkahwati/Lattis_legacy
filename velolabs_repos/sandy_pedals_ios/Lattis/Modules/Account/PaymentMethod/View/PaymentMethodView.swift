//
//  PaymentMethodView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 6/30/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Cartography

class PaymentMethodView: UIView {
    let tableView: UITableView = {
        let table = UITableView()
        table.tableFooterView = UIView()
        table.register(CreditCardCell.self, forCellReuseIdentifier: "card")
        table.register(AddCardCell.self, forCellReuseIdentifier: "add")
        table.register(EmptyCardCell.self, forCellReuseIdentifier: "empty")
        table.separatorInset = UIEdgeInsets(top: 0, left: 15, bottom: 0, right: 15)
        table.contentInset = UIEdgeInsets(top: 25, left: 0, bottom: 0, right: 0)
        table.allowsSelectionDuringEditing = false
        return table
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        addSubview(tableView)
        constrain(tableView) { (view) in
            view.edges == view.superview!.edges
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
