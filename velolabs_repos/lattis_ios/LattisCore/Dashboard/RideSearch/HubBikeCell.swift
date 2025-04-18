//
//  HubBikeCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 19.01.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import Foundation
import Cartography

final class HubBikeCell: UITableViewCell {
    
    fileprivate let stackView = UIStackView()
    fileprivate var bikeView: BikeControl?
    fileprivate let selectButton = ActionButton()
    fileprivate let infoButton = ActionButton()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(stackView)
        stackView.axis = .vertical
        stackView.spacing = .margin/2
        selectionStyle = .none
        constrain(stackView, contentView) { name, view in
            name.edges == view.edges.inseted(by: .margin)
        }
        
        selectButton.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        let actionStack = UIStackView(arrangedSubviews: [infoButton, selectButton])
        actionStack.axis = .horizontal
        actionStack.spacing = .margin
        actionStack.distribution = .fillProportionally
        stackView.addArrangedSubview(actionStack)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func update(bike: Bike, info: @escaping () -> Void, select: @escaping () -> Void) {
        if let current = bikeView {
            stackView.removeArrangedSubview(current)
            current.removeFromSuperview()
        }
        let current = BikeControl(bike: bike)
        stackView.insertArrangedSubview(current, at: 0)
        bikeView = current
        
        selectButton.action = .plain(title: "select".localized(), style: .inactiveSecondary, handler: select)
        infoButton.action = .plain(title: nil, icon: .named("icon_info"), style: .plain, handler: info)
    }
    
    
}

