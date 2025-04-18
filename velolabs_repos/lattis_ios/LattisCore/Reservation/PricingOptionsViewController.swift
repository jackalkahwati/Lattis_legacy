//
//  PricingOptionsViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 17.08.2021.
//  Copyright © 2021 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Model

class PricingOptionsViewController: UIViewController {
    
    let controller: PricingOptionsController
    let pricingView: PricingOptionsView
    fileprivate let select: (Int?) -> ()
    
    init(_ controller: PricingOptionsController, select: @escaping (Int?) -> ()) {
        self.controller = controller
        self.pricingView = .init(controller)
        self.select = select
        super.init(nibName: nil, bundle: nil)
        pricingView.confirmButton.action = .plain(title: "confirm".localized()) { [unowned self] in
            self.select(controller.selected)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        title = "select_pricing".localized()
        
        view.addSubview(pricingView)
        view.backgroundColor = .white
        
        constrain(pricingView, view) { pricing, view in
            pricing.edges == view.edges.inseted(by: .margin)
        }
    }
}
