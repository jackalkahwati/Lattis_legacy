//
//  AlertContentController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 22.07.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class AlertContentController: UIViewController {
    
    fileprivate let contentConstructor: () -> UIView
    fileprivate let containerView = UIView()
    fileprivate let stackView = UIStackView()
    var closeTitle: String = "close".localized()
    let closeButton = ActionButton()
    
    init(title: String? = nil, content: @escaping () -> UIView) {
        self.contentConstructor = content
        super.init(nibName: nil, bundle: nil)
        self.title = title
        closeButton.isHidden = true
        
        modalPresentationStyle = .overCurrentContext
        modalTransitionStyle = .crossDissolve
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        let titleLabel = UILabel.label(text: title, font: .theme(weight: .medium, size: .title), allignment: .center, lines: 0)
        
        view.backgroundColor = UIColor(white: 0.3, alpha: 0.8)
        view.addSubview(containerView)
        containerView.backgroundColor = .white
        containerView.layer.cornerRadius = .containerCornerRadius
        containerView.addShadow()
        
        containerView.addSubview(stackView)
        
        stackView.axis = .vertical
        stackView.spacing = .margin
        
        constrain(containerView, stackView, view) { container, stack, view in
            container.centerY == view.centerY
            container.left == view.left + .margin
            container.right == view.right - .margin
            
            stack.edges == container.edges.inseted(by: .margin)
        }
        stackView.addArrangedSubview(titleLabel)
        stackView.addArrangedSubview(contentConstructor())
        stackView.addArrangedSubview(closeButton)
        
        closeButton.action = .plain(title: closeTitle) { [unowned self] in 
            self.dismiss(animated: true, completion: nil)
        }
    }

}
