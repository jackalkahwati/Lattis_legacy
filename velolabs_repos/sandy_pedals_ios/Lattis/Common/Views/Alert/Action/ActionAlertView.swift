//
//  ActionAlertView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 26/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit

class ActionAlertView: UIView, AlertView {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var actionButton: UIButton!
    @IBOutlet weak var cancelButton: UIButton!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        actionButton.titleLabel?.numberOfLines = 0
        actionButton.titleLabel?.lineBreakMode = .byWordWrapping
        actionButton.titleLabel?.textAlignment = .center
    }
    

    var action: AlertAction? {
        didSet {
            actionButton.add(action: action)
            actionButton.addTarget(self, action: #selector(performAction), for: .touchUpInside)
        }
    }
    var cancel: AlertAction? {
        didSet {
            cancelButton.add(action: cancel)
            cancelButton.addTarget(self, action: #selector(performCancel), for: .touchUpInside)
        }
    }
    
    static func alert(title: String, subtitle: String? = nil) -> ActionAlertView {
        let alertView = ActionAlertView.nib() as! ActionAlertView
        alertView.titleLabel.text = title
        alertView.subtitleLabel.text = subtitle
        alertView.action = nil
        alertView.cancel = nil
        return alertView
    }
    
    @objc private func performAction() {
        hide() { self.action?.action() }
    }
    
    @objc private func performCancel() {
        hide() { self.cancel?.action() }
    }
}
