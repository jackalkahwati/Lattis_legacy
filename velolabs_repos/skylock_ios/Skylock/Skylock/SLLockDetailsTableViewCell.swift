//
//  SLLockDetailsTableViewCell.swift
//  Skylock
//
//  Created by Andre Green on 6/7/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLLockDetailsTableViewCell: UITableViewCell {
    let xPadding:CGFloat = 10.0

    var isConnected:Bool = false
    
    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: .subtitle, reuseIdentifier: reuseIdentifier)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let utilities:SLUtilities = SLUtilities()
        let yPadding:CGFloat = 20.0
        let xPadding:CGFloat = 10.0
        if let textLabel = self.textLabel {
            textLabel.textColor = utilities.color(colorCode: .Color155_155_155)
            textLabel.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 18.0)
            
            let textLabelFrame = CGRect(
                x: textLabel.frame.origin.x,
                y: yPadding,
                width: self.contentView.bounds.size.width - 2.0*xPadding,
                height: textLabel.bounds.size.height
            )
            textLabel.frame = textLabelFrame
        }
        
        if let detailTextLabel = self.detailTextLabel {
            let colorCode:SLColor = self.isConnected ? .Color160_200_224  : .Color188_187_187
            detailTextLabel.font = UIFont(name: SLFont.OpenSansRegular.rawValue, size: 14.0)
            detailTextLabel.textColor = utilities.color(colorCode: colorCode)
            
            let detailTextLabelFrame = CGRect(
                x: detailTextLabel.frame.origin.x,
                y: self.contentView.bounds.size.height - detailTextLabel.bounds.size.height - yPadding,
                width: self.contentView.bounds.size.width - 2.0*xPadding,
                height: detailTextLabel.bounds.size.height
            )
            
            detailTextLabel.frame = detailTextLabelFrame
        }
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
    func setProperties(isConnected: Bool, mainText: String?, detailText: String?) {
        self.isConnected = isConnected
        self.textLabel?.text = mainText
        self.detailTextLabel?.text = detailText
    }
}
