//
//  FindRideNavigationView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 24/12/2016.
//  Copyright © 2016 Velo Labs. All rights reserved.
//

import UIKit

class FindRideNavigationView: UIView {
    @IBOutlet weak var qrButton: UIButton!
    @IBOutlet weak var searchButton: UIButton!
    @IBOutlet weak var findClosedLayout: NSLayoutConstraint!
    @IBOutlet weak var findOpenLayout: NSLayoutConstraint!
    @IBOutlet weak var locationLabel: UILabel!
    @IBOutlet weak var menuButton: UIButton!
    @IBOutlet weak var findButton: UIButton!
    @IBOutlet weak var findContainer: UIView!
    @IBOutlet weak var findLabel: UILabel!
    weak var whiteView: UIView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        layer.shadowColor = UIColor.black.cgColor
        layer.shadowOpacity = 0.06
        layer.shadowRadius = 3
        layer.shadowOffset = CGSize(width: 0, height: 2)
    }
    
    var state: State = .choose {
        didSet {
            if state == .find {
                findContainer.isHidden = false
            }
            whiteView.isUserInteractionEnabled = state == .find
            findClosedLayout.priority = UILayoutPriority(rawValue: UILayoutPriority.RawValue(state == .find ? 800 : 900))
            findOpenLayout.priority = UILayoutPriority(rawValue: UILayoutPriority.RawValue(state == .find ? 900 : 800))
            
            let image = state == .find ? #imageLiteral(resourceName: "icon_search_back") : #imageLiteral(resourceName: "icon_search")
            searchButton.setImage(image, for: .normal)
            UIView.animate(withDuration: .defaultAnimation, animations: {
                self.whiteView.alpha = self.state == .find ? 1 : 0
                self.superview?.layoutIfNeeded()
            }) { (_) in
                self.findContainer.isHidden = self.state == .choose
            }
        }
    }

    var location: String {
        return locationLabel.text ?? ""
    }
}

extension FindRideNavigationView {
    enum State {
        case find, choose
        static func invert(_ state: State) -> State {
            return state == .find ? .choose : .find
        }
    }
}
