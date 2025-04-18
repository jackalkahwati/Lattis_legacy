//
//  ScrollableStackView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 20.08.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

extension UIStackView {
    func addScroll(insets: UIEdgeInsets = .zero, to parrent: UIView) -> UIScrollView {
        let scrollView = UIScrollView()
        scrollView.addSubview(self)
        parrent.addSubview(scrollView)
        
        constrain(self, scrollView, parrent) { stack, scroll, view in
            stack.edges == scroll.edges.inseted(by: insets)            
            stack.width == view.width - insets.left - insets.right
        }
        
        return scrollView
    }
}
