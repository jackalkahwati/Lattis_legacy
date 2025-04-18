//
//  BikeInfoCell.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 05/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Kingfisher

protocol BikeInfoRepresenting: UITableViewCell {
    var info: BikeInfo! { get set }
}

final class BikeDetailsCell: UITableViewCell, BikeInfoRepresenting {
    
    fileprivate let titleLabel = UILabel.label(text: "description".localized(), font: .theme(weight: .medium, size: .text))
    fileprivate let infoLabel = UILabel.label(font: .theme(weight: .book, size: .text), lines: 0)
    
    var info: BikeInfo! {
        didSet {
            guard case let .details(details) = info else { return }
            infoLabel.text = details
        }
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .none
        tintColor = .black
        
        let stackView = UIStackView(arrangedSubviews: [titleLabel, infoLabel])
        stackView.axis = .vertical
        stackView.spacing = .margin/4
        contentView.addSubview(stackView)
        
        constrain(stackView, contentView) { $0.edges == $1.edges.inseted(horizontally: .margin, vertically: .margin/2) }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

final class BikeInfoBikeCell: UITableViewCell, BikeInfoRepresenting {
    
    fileprivate let bikeTypeLabel = UILabel.label(font: .theme(weight: .bold, size: .small))
    fileprivate let bikeNameLabel = UILabel.label(font: .theme(weight: .bold, size: .giant))
    fileprivate let fleetNameLabel = UILabel.label(font: .theme(weight: .book, size: .text))
    fileprivate let stackView = UIStackView()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .none
        tintColor = .black
        
        stackView.addArrangedSubview(bikeTypeLabel)
        stackView.addArrangedSubview(bikeNameLabel)
        stackView.addArrangedSubview(fleetNameLabel)
        
        stackView.axis = .vertical
        stackView.spacing = .margin/4
        contentView.addSubview(stackView)
        
        constrain(stackView, contentView) { $0.edges == $1.edges.inseted(horizontally: .margin, vertically: .margin/2) }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var info: BikeInfo! {
        didSet {
            guard case let .bike(bike) = info else { return }
            bikeTypeLabel.text = bike.kind.rawValue.capitalized
            bikeNameLabel.text = bike.name
            fleetNameLabel.text = bike.fleetName
            
            if stackView.arrangedSubviews.count == 4,
                let last = stackView.arrangedSubviews.last {
                stackView.removeArrangedSubview(last)
                last.removeFromSuperview()
            }
            if let battery = bike.bikeBatteryLevel {
                stackView.addArrangedSubview(BatteryLevelView(battery))
            }
        }
    }
}

final class BikeInfoBikeCell_v2: UITableViewCell, BikeInfoRepresenting {
    
    fileprivate let bikeTypeLabel = UILabel.label(font: .theme(weight: .bold, size: .small))
    fileprivate let bikeNameLabel = UILabel.label(font: .theme(weight: .bold, size: .giant))
    fileprivate let fleetNameLabel = UILabel.label(font: .theme(weight: .book, size: .text))
    fileprivate let stackView = UIStackView()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .none
        tintColor = .black
        
        stackView.addArrangedSubview(bikeTypeLabel)
        stackView.addArrangedSubview(bikeNameLabel)
        stackView.addArrangedSubview(fleetNameLabel)
        
        stackView.axis = .vertical
        stackView.spacing = .margin/4
        
        contentView.addSubview(stackView)
        
        constrain(stackView, contentView) { $0.edges == $1.edges.inseted(horizontally: .margin, vertically: .margin/2) }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var info: BikeInfo! {
        didSet {
            guard case let .bike_v2(bike) = info else { return }
            bikeTypeLabel.text = bike.bikeGroup.type.localizedTitle
            bikeNameLabel.text = bike.bikeName
            fleetNameLabel.text = bike.fleet.name
            
            if stackView.arrangedSubviews.count == 4,
                let last = stackView.arrangedSubviews.last {
                stackView.removeArrangedSubview(last)
                last.removeFromSuperview()
            }
            if let battery = bike.bikeBatteryLevel {
                stackView.addArrangedSubview(BatteryLevelView(battery))
            }
        }
    }
}

final class BikeInfoDisclosureCell: UITableViewCell, BikeInfoRepresenting {
    
    fileprivate let valueLabel = UILabel.label(font: .theme(weight: .book, size: .body), color: .black)
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(valueLabel)
        accessoryType = .disclosureIndicator
        accessoryView = UIImageView(image: .named("icon_accessory_arrow"))
        
        constrain(valueLabel, contentView) { $0.edges == $1.edges.inseted(horizontally: .margin, vertically: .margin/2) }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var info: BikeInfo! {
        didSet {
            switch info {
            case .fleet(let name):
                valueLabel.text = name
            case .termsOfUse:
                valueLabel.text = "bike_detail_label_terms_condition".localized()
            case .parkingSpots:
                valueLabel.text = "bike_detail_label_parking_zones_spots".localized()
            default:
                valueLabel.text = nil
            }
        }
    }
}

final class BikeInfoTupleCell: UITableViewCell, BikeInfoRepresenting {
    
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .text), color: .black)
    fileprivate let valueLabel = UILabel.label(font: .theme(weight: .bold, size: .text), color: .black, allignment: .right)
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .none
        contentView.backgroundColor = .white
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(valueLabel)
        
        valueLabel.setContentHuggingPriority(.defaultHigh, for: .vertical)
        
        constrain(titleLabel, valueLabel, contentView) { title, value, content in
            title.left == content.left + .margin
            title.centerY == content.centerY
            
            value.right == content.right - .margin
            value.top == content.top + .margin/2
            value.bottom == content.bottom - .margin/2
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var info: BikeInfo! {
        didSet {
            guard case let .tuple(title, value) = info else { return }
            valueLabel.text = value
            titleLabel.text = title
        }
    }
}

class BikeInfoCell: UITableViewCell, BikeInfoRepresenting {
    
    fileprivate let valueLabel = UILabel.label(font: .theme(weight: .book, size: .small), color: .black, lines: 0)
        
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .none
        contentView.backgroundColor = .white
        
        contentView.addSubview(valueLabel)
        
        constrain(valueLabel, contentView) { value, content in
            value.edges == content.edges.inseted(horizontally: .margin, vertically: .margin/2)
        }
    }
    
    var info: BikeInfo! {
        didSet {
            switch info {
            case .info(let text):
                valueLabel.text = text
            default:
                valueLabel.text = nil
            }
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class BikeInfoSectionView: UITableViewHeaderFooterView {
    
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .medium, size: .body), color: .gray)
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        
        contentView.backgroundColor = .white
        contentView.addSubview(titleLabel)
        
        constrain(titleLabel, contentView) { title, view in
            title.edges == view.edges.inseted(horizontally: .margin)
        }
    }
    
    var title: String? {
        set {
            titleLabel.text = newValue
        }
        get {
            return titleLabel.text
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
