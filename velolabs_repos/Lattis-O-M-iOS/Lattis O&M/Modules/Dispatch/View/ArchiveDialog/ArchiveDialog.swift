//
//  ArchiveDialog.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 03/05/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class ArchiveDialog: DialogView {
    @IBOutlet weak var picker: UIPickerView!
    override class func create(title: String, subtitle: String) -> ArchiveDialog {
        let dialog = ArchiveDialog.nib() as! ArchiveDialog
        dialog.titleLabel.text = title
        dialog.subtitleLabel.text = subtitle
        return dialog
    }
}
