//
//  GetDirectionsView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 13/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import JTMaterialSpinner

class GetDirectionsView: UIView {
    @IBOutlet weak var currentHeight: NSLayoutConstraint!
    @IBOutlet weak var emptyView: UIView!
    @IBOutlet weak var searchField: UITextField!
    @IBOutlet weak var tableView: UITableView!

    let searchControl = SearchControl(frame: CGRect(x: 0, y: 0, width: 36, height: 20))
    override func awakeFromNib() {
        super.awakeFromNib()
        
        tableView.register(UINib(nibName: "GetDirectionsCell", bundle: nil), forCellReuseIdentifier: String(describing: GetDirectionsCell.self))
        tableView.separatorInset = UIEdgeInsets(top: 0, left: 16, bottom: 0, right: 16)
        tableView.tableFooterView = UIView()
        
        searchField.rightViewMode = .always
        searchField.rightView = searchControl
        
        searchField.leftViewMode = .always
        searchField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 8, height: 20))
    }
    
    func showEmpty() {
        UIView.animate(withDuration: .defaultAnimation) { 
            self.tableView.alpha = 0
            self.emptyView.alpha = 1
        }
    }
    
    func hideEmpty() {
        UIView.animate(withDuration: .defaultAnimation) {
            self.tableView.alpha = 1
            self.emptyView.alpha = 0
        }
    }
}

extension GetDirectionsView {
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
}
