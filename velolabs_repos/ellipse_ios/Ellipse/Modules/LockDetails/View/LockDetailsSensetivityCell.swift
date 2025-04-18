//
//  LockDetailsSensetivityCell.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 29/01/2019.
//  Copyright © 2019 Lattis. All rights reserved.
//

import UIKit
import Cartography

protocol LockDetailsSensetivityCellDelegate: class {
    func sensetivityChanged(value: Ellipse.Sensetivity)
}

class LockDetailsSensetivityCell: LockDetailsBaseCell {
    
    weak var delegate: LockDetailsSensetivityCellDelegate?
    fileprivate let segment = UISegmentedControl(items: ["low".localized(), "medium".localized(), "high".localized()])
    fileprivate let subtitleLabel = UILabel()
    fileprivate let noteLabel = UILabel()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        contentView.addSubview(segment)
        contentView.addSubview(subtitleLabel)
        contentView.addSubview(noteLabel)
        
        segment.tintColor = .elDarkSkyBlue
        segment.cornerRadius = 15
        segment.clipsToBounds = true
        segment.borderWidth = 1
        segment.borderColor = .elDarkSkyBlue
        segment.setTitleTextAttributes([.font: UIFont.elRegular], for: .normal)
        subtitleLabel.font = .elRegular
        subtitleLabel.textColor = .elSlateGrey
        subtitleLabel.numberOfLines = 0
        subtitleLabel.text = "lock_settings_sensetivity_body".localized()
        noteLabel.font = .elRegular
        noteLabel.textColor = .elSlateGrey
        noteLabel.numberOfLines = 0
        
        constrain(segment, subtitleLabel, titleLabel, noteLabel, contentView) { slider, subtitle, title, note, view in
            title.left == view.left + .margin
            title.right == view.right - .margin
            title.top == view.top + .margin
            
            subtitle.left == title.left
            subtitle.right == title.right
            subtitle.top == title.bottom + .margin/2
            subtitle.bottom == slider.top - .margin/2
            
            slider.right == title.right
            slider.left == title.left
            slider.bottom == note.top - .margin/2
            
            note.right == title.right
            note.left == title.left
            note.bottom == view.bottom - .margin
        }
        
        subtitleLabel.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        noteLabel.setContentCompressionResistancePriority(.defaultHigh, for: .vertical)
        segment.addTarget(self, action: #selector(segmentChanged(_:)), for: .valueChanged)
    }
    
    fileprivate func smallStyle(label: UILabel, text: String) {
        label.text = text
        label.font = .elImageButtonTitle
        label.textColor = .elSlateGrey
    }
    
    override var info: LockDetails.Info! {
        didSet {
            guard case let .sensetivity(value) = info! else { return }
            segment.selectedSegmentIndex = value.rawValue
            noteLabel.text = value.note
        }
    }
    
    @objc fileprivate func segmentChanged(_ sender: UISegmentedControl) {
        let value: Ellipse.Sensetivity = .init(Int32(sender.selectedSegmentIndex))
        delegate?.sensetivityChanged(value: value)
        noteLabel.text = value.note
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
