//
//  AxaLockCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 17.03.2020.
//  Copyright © 2020 Lattis. All rights reserved.
//

import UIKit
import Cartography
import AXALock

class AxaLockCell: UITableViewCell {
    var device: AxaDevice! {
        didSet {
            nameLabel.text = device.name
            bikeName = device.bike?.name
            isPaired = device.isPaired
        }
    }
    fileprivate let bikeLabel = UILabel()
    fileprivate let nameLabel = UILabel()
    fileprivate let container = UIStackView()
    fileprivate let connectionLabel = UILabel()
    
    fileprivate var isPaired = false {
        didSet {
            guard isPaired != oldValue else { return }
            if isPaired {
                container.addArrangedSubview(connectionLabel)
            } else {
                container.removeArrangedSubview(connectionLabel)
                connectionLabel.removeFromSuperview()
            }
        }
    }
    
    fileprivate var bikeName: String? = nil {
        didSet {
            guard bikeName != oldValue else { return }
            bikeLabel.text = bikeName
            if container.arrangedSubviews.contains(bikeLabel) && bikeName == nil {
                container.removeArrangedSubview(bikeLabel)
                bikeLabel.removeFromSuperview()
            } else {
                container.insertArrangedSubview(bikeLabel, at: 0)
            }
        }
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(container)
        container.axis = .vertical
        constrain(container, contentView) { con, view in
            con.edges == view.edges.inseted(by: .margin)
        }
        container.addArrangedSubview(nameLabel)
        
        connectionLabel.text = "connnected".localized()
        connectionLabel.font = .systemFont(ofSize: 12)
        connectionLabel.textColor = .lsTurquoiseBlue
        
        nameLabel.font = .boldSystemFont(ofSize: 14)
        nameLabel.textColor = .gray
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
