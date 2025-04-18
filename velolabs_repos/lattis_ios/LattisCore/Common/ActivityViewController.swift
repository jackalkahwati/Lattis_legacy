//
//  ActivityViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 19/09/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

final class ProgressIindicator: UIView {
    
    fileprivate let indicatorView = UIView()
    fileprivate(set) var isLoading = false
    
    init(_ height: CGFloat = 10, color: UIColor = .accent) {
        super.init(frame: .zero)
        
        layer.cornerRadius = height/2
        layer.borderWidth = 2
        layer.borderColor = color.cgColor
        clipsToBounds = true
        
        addSubview(indicatorView)
        indicatorView.backgroundColor = color
        
        constrain(self) { view in
            view.height == height
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func startLoading() {
        isLoading = true
        updateFrame()
    }
    
    func stopLoading() {
        isLoading = false
        indicatorView.layer.removeAllAnimations()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        if isLoading {
            updateFrame()
        }
    }
    
    func updateFrame() {
        indicatorView.frame = {
            var f = self.bounds
            f.origin.x = -f.width*1.3
            return f
        }()
        UIView.animate(withDuration: 2, delay: 0.1, options: [.repeat, .curveEaseInOut], animations: {
            self.indicatorView.frame = {
                var f = self.bounds
                f.size.width *= 0.8
                f.origin.x = self.bounds.width
                return f
            }()
        }, completion:nil)
    }
}

public final class ActivityViewController: UIViewController {
    
    fileprivate let loaderView = LoaderView()
    
    init(_ title: String) {
        super.init(nibName: nil, bundle: nil)
        loaderView.titleLabel.text = title
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .background
        
        view.addSubview(loaderView)
        constrain(loaderView, view) { loader, view in
            loader.top == view.top
            loader.bottom == view.bottom
            loader.left == view.left
            loader.right == view.right
        }
    }
    
    func loading(_ text: String) {
        loaderView.titleLabel.text = text
    }
    
    func show(warning: String, completion: (() -> ())? = nil) {

    }
    
    func hide(completion: (() -> ())? = nil) {
        dismiss(animated: true, completion: completion)
    }
    
    @objc fileprivate func action(button: ActionButton) {
        hide(completion: button.action.handler)
    }
}
