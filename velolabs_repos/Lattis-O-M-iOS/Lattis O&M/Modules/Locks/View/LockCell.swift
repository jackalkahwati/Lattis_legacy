//
//  LockCell.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 17/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class LockCell: UITableViewCell {
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var macLabel: UILabel!
    
    var lock: Lock! {
        didSet {
            titleLabel.text = lock.displayTitle
            if let date = lock.lastConnected {
                let formatter = DateFormatter()
                formatter.dateStyle = .medium
                subtitleLabel.text = String(format: "locks_last_connected".localized(), formatter.string(from: date))
                subtitleLabel.textColor = .lsGreyish
            } else if lock.isConnected {
                subtitleLabel.text = "locks_connected".localized()
                subtitleLabel.textColor = .lsRobinsEgg
            } else {
                subtitleLabel.text = nil
            }
            if lock.lock != nil {
                macLabel.text = lock.macId
            } else {
                macLabel.text = nil
            }
        }
    }
}


class LocksSectionView: UIView {
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 10)
        label.textColor = .lsSteelTwo
        label.textAlignment = .center
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .lsWhite
        addSubview(titleLabel)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = bounds.insetBy(dx: 16, dy: 0)
    }
}

protocol LocksFilterSectionDelegate: class {
    func openSearch()
    func openFilter()
    func didSelect(filter: Lock.Filter, vendor: Lock.Vendor)
}

class LocksFilterSectionView: LocksSectionView {
    weak var delegate: LocksFilterSectionDelegate?
    let subtitleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.boldSystemFont(ofSize: 9)
        label.textColor = .lsTurquoiseBlue
        label.textAlignment = .center
        return label
    }()
    
    fileprivate let button = UIButton(type: .custom)
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(subtitleLabel)
        addSubview(button)
        
        let tap = UITapGestureRecognizer(target: self, action: #selector(tapHandler))
        tap.numberOfTouchesRequired = 1
        addGestureRecognizer(tap)
        
        let longPress = UILongPressGestureRecognizer(target: self, action: #selector(longPressHandler))
        addGestureRecognizer(longPress)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func tapHandler() {
        delegate?.openFilter()
    }
    
    @objc func longPressHandler() {
        delegate?.openSearch()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = {
            var frame = titleLabel.frame
            frame.origin.y = bounds.midY - (titleLabel.font.lineHeight + subtitleLabel.font.lineHeight + 3)*0.5
            frame.size.height = titleLabel.font.lineHeight
            return frame
        }()
        
        subtitleLabel.frame = {
            var frame = titleLabel.frame
            frame.origin.y = titleLabel.frame.maxY + 3
            frame.size.height = subtitleLabel.font.lineHeight
            return frame
        }()
    }
}
