//
//  ProfileInfoCell.swift
//  Lattis
//
//  Created by Ravil Khusainov on 03/04/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

enum ProfileInfoType {
    case name, lastName, email, phone, password, delete, privateNetworks, verification
    var display: String? {
        switch self {
        case .name:
            return "general_first_name".localized().uppercased()
        case .lastName:
            return "general_last_name".localized().uppercased()
        case .email:
            return "general_email".localized().uppercased()
        case .phone:
            return "profile_phone_number".localized().uppercased()
        default:
            return nil
        }
    }
}

struct ProfileInfoModel {
    let type: ProfileInfoType
    var keyboard: UIKeyboardType = .default
    var value: String?
    var accessoryType: UITableViewCell.AccessoryType
    let action: (String, ProfileInfoType) -> ()
    
    init(type: ProfileInfoType, keyboard: UIKeyboardType = .default, value: String?, accessoryType: UITableViewCell.AccessoryType = .none, action: @escaping (String, ProfileInfoType) -> () = {_,_  in}) {
        self.type = type
        self.keyboard = keyboard
        self.value = value
        self.action = action
        self.accessoryType = accessoryType
    }
}

class ProfileCell: UITableViewCell {
    enum RowModel: TableCellPresentable {
        case empty(String)
        case info(ProfileInfoModel)
        case network(PrivateNetwork)
        
        var rowHeight: CGFloat { return 44 }
        var identifire: String {
            switch self {
            case .empty(_): return "empty"
            case .info(_): return "info"
            case .network(_): return "network"
            }
        }
    }
    var model: RowModel?
}

class ProfileInfoCell: ProfileCell {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var valueLabel: UILabel!
    
    override var model: ProfileCell.RowModel? {
        didSet {
            if let model = model, case let .info(info) = model {
                titleLabel.text = info.type.display
                valueLabel.text = info.value
                accessoryType = info.accessoryType
            } else {
                titleLabel.text = nil
                valueLabel.text = nil
                accessoryType = .none
            }
        }
    }
}
