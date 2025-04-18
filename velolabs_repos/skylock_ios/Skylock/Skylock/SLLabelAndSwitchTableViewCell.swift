//
//  SLLabelAndSwitchTableViewCell.swift
//  Skylock
//
//  Created by Andre Green on 6/9/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

enum SLLabelAndSwitchTableViewCellAccessoryType {
    case ToggleSwitch
    case Arrow
}

protocol SLLabelAndSwitchCellDelegate:class {
    func switchFlippedForCell(cell: SLLabelAndSwitchTableViewCell, isNowOn:Bool)
}

class SLLabelAndSwitchTableViewCell: UITableViewCell {
    let xPadding:CGFloat = 5.0
    
    let labelHeight:CGFloat = 14.0
    
    var leftText:String?
    
    weak var delegate:SLLabelAndSwitchCellDelegate?
    
    var leftAccessoryType:SLLabelAndSwitchTableViewCellAccessoryType
    
    lazy var toggleSwitch:UISwitch = {
        let toggle:UISwitch = UISwitch(frame: CGRect.zero)
        toggle.addTarget(self, action: #selector(switchFlipped(toggleSwitch:)), for: .valueChanged)
        toggle.onTintColor = UIColor(red: 102, green: 177, blue: 227)
        
        return toggle
    }()
    
    lazy var arrowView:UIImageView = {
        let image:UIImage = UIImage(named: "lock_settings_right_arrow")!
        let imageView = UIImageView(image: image)
        
        return imageView
    }()
    
    init(accessoryType: SLLabelAndSwitchTableViewCellAccessoryType, reuseId: String) {
        self.leftAccessoryType = accessoryType
        super.init(style: .default, reuseIdentifier: reuseId)
    }
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        self.leftAccessoryType = .ToggleSwitch
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.textLabel?.font = UIFont(name: SLFont.OpenSansSemiBold.rawValue, size: 14)
        self.textLabel?.textColor = UIColor(red: 157, green: 161, blue: 167)
        
        self.setAccessoryView()
    }
    
    func turnSwitchOn(shouldTurnOn: Bool) {
        if self.leftAccessoryType != .ToggleSwitch {
            return
        }
        
        self.toggleSwitch.setOn(shouldTurnOn, animated: false)
    }
    
    func switchFlipped(toggleSwitch: UISwitch) {
        print("switch flipped to \(toggleSwitch.isOn)")
        self.delegate?.switchFlippedForCell(cell: self, isNowOn: toggleSwitch.isOn)
    }
    
    func setAccessoryView() {
        switch self.leftAccessoryType {
        case .Arrow:
            self.accessoryView = self.arrowView
        case .ToggleSwitch:
            self.accessoryView = self.toggleSwitch
        }
    }
}
