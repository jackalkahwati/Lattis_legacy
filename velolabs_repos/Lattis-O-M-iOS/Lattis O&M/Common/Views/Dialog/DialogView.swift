//
//  DialogView.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 03/05/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class DialogView: UIView, AlertView {
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var confirmButton: UIButton!
    @IBOutlet weak var cancelButton: UIButton!
    
    var confirm: () -> () = {}
    var cancel: () -> () = {}
    
    class func create(title: String, subtitle: String) -> DialogView {
        let dialog = DialogView.nib() as! DialogView
        dialog.titleLabel.text = title
        dialog.subtitleLabel.text = subtitle
        return dialog
    }
    
    var confirmTitle: String? {
        get {
            return confirmButton.title(for: .normal)
        }
        set {
            confirmButton.setTitle(newValue, for: .normal)
        }
    }
    
    var cancelTitle: String? {
        get {
            return cancelButton.title(for: .normal)
        }
        set {
            cancelButton.setTitle(newValue, for: .normal)
        }
    }
    
    @IBAction func cancelAction(_ sender: Any) {
        hide(completion: cancel)
    }
    
    @IBAction func confirmAction(_ sender: Any) {
        hide(completion: confirm)
    }
}
