//
//  SearchControl.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 01/05/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import JTMaterialSpinner

class SearchControl: UIControl {
    enum SearchState {
        case search, close, spin
    }
    
    var searchState: SearchState = .search {
        didSet {
            switch searchState {
            case .search:
                spinner.endRefreshing()
                searchIcon.isHidden = false
                closeIcon.isHidden = true
            case .close:
                spinner.endRefreshing()
                searchIcon.isHidden = true
                closeIcon.isHidden = false
            case .spin:
                searchIcon.isHidden = true
                closeIcon.isHidden = true
                spinner.beginRefreshing()
            }
        }
    }
    
    private let spinner = JTMaterialSpinner()
    private let searchIcon = UIImageView(image: #imageLiteral(resourceName: "icon_search"))
    private let closeIcon = UIImageView(image: #imageLiteral(resourceName: "close_Icon_gray"))
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(spinner)
        addSubview(searchIcon)
        addSubview(closeIcon)
        
        closeIcon.contentMode = .center
        searchIcon.contentMode = .center
        closeIcon.isHidden = true
        
        spinner.circleLayer.lineWidth = 2
        spinner.circleLayer.strokeColor = UIColor.lsSteel.cgColor
        spinner.animationDuration = 1.5
        spinner.isUserInteractionEnabled = false
    }
    
    override func layoutSubviews() {
        searchIcon.frame = bounds
        closeIcon.frame = bounds
        spinner.frame = bounds.insetBy(dx: 1, dy: 1)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
