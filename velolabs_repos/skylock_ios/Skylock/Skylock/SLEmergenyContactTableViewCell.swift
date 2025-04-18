//
//  SLEmergenyContactTableViewCell.swift
//  Skylock
//
//  Created by Andre Green on 7/10/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

enum SLEmergencyContactTableViewCellProperty {
    case Name
    case Pic
    case ContactId
}

protocol SLEmergenyContactTableViewCellDelegate:class {
    func removeButtonPressedOnCell(cell: SLEmergenyContactTableViewCell)
}

class SLEmergenyContactTableViewCell: UITableViewCell {
    weak var delegate:SLEmergenyContactTableViewCellDelegate?
    
    var contactId:String?
    
    lazy var removeContactButton:UIButton = {
        let image = UIImage(named: "button_remove_Emergencycontacts")!
        let frame = CGRect(
            x: self.textLabel!.frame.minX,
            y: self.textLabel!.frame.maxY + 7.0,
            width: image.size.width,
            height: image.size.height
        )
        
        let button:UIButton = UIButton(frame: frame)
        button.addTarget(self, action: #selector(removeContactButtonPressed), for: .touchDown)
        button.setImage(image, for: .normal)
        
        return button
    }()
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let labelFrame = CGRect(
            x: self.textLabel!.frame.origin.x,
            y: self.imageView!.frame.minY,
            width: self.textLabel!.bounds.size.width,
            height: 0.5*self.imageView!.bounds.size.height
        )
        
        self.textLabel?.frame = labelFrame
        self.textLabel?.textColor = UIColor(red: 130, green: 156, blue: 178)
        self.textLabel?.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 15.0)
        
        if !self.subviews.contains(self.removeContactButton) {
            let frame = CGRect(
                x: self.textLabel!.frame.minX,
                y: self.textLabel!.frame.maxY + 7.0,
                width: self.removeContactButton.bounds.size.width,
                height: self.removeContactButton.bounds.size.height
            )
            
            self.removeContactButton.frame = frame
            self.addSubview(self.removeContactButton)
        }
    }
    
    func setProperties(properties: [SLEmergencyContactTableViewCellProperty: AnyObject]) {
        if let name = properties[.Name] as? String, let textLabel = self.textLabel {
            textLabel.text = name
            self.removeContactButton.isEnabled = true
        } else {
            self.removeContactButton.isEnabled = false
        }
        
        self.imageView?.image = UIImage(named: "sharing_default_picture")!
        
        if let contactIdentifier = properties[.ContactId] as? String {
            self.contactId = contactIdentifier
        } else {
            self.contactId = nil
        }
    }
    
    func updateImage(image: UIImage?) {
        if let pic = image {
            self.imageView?.image = pic
        } else {
            let pic = UIImage(named: "sharing_default_picture")!
            self.imageView?.image = pic
        }
        
        self.imageView?.setNeedsDisplay()
    }
    
    func removeContactButtonPressed() {
        self.delegate?.removeButtonPressedOnCell(cell: self)
    }
}
