//
//  BottomBarViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 03/02/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit

protocol BottomBarPresentable {
    var ellipleBarController: BottomBarPresenting? { get set }
}

protocol BottomBarPresenting: class {
    var canShowBottomBar: Bool { get set }
    func showBottomBar()
    func hideBottomBar()
}

class BottomBarViewController: UIViewController {
    var canShowBottomBar: Bool = true
    private let child: UIViewController
    private(set) var container: BottomBarView!
    private weak var barController: SLLockBarViewController? {
        didSet {
            guard let controller = barController else { return }
            controller.willMove(toParentViewController: self)
            addChildViewController(controller)
            container.barView = controller.view
            controller.didMove(toParentViewController: self)
            setupBarController(controller)
        }
    }
    
    var setupBarController: (SLLockBarViewController) -> () = { _ in }
    
    init(_ child: UIViewController) {
        self.child = child
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        child.willMove(toParentViewController: self)
        addChildViewController(child)
        container.contentView = child.view
        child.didMove(toParentViewController: self)
        
        if let nc = child as? UINavigationController {
            nc.delegate = self
        }
        
        barController = SLLockBarViewController()
        
        NotificationCenter.default.addObserver(self, selector: #selector(hideBottomBar), name: NSNotification.Name(rawValue: kSLNotificationLockManagerDisconnectedLock), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(showBottomBar), name: NSNotification.Name(rawValue: kSLNotificationLockPaired), object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        barController?.setUpViews()
        showBottomBar()
    }
    
    
    override func loadView() {
        container = BottomBarView()
        view = container
    }
    
    func showBottomBar() {
        guard let macId = SLDatabaseManager.shared().getCurrentLockForCurrentUser()?.macId,
            SLLockManager.sharedManager.isConnecedLock(with: macId) && container.isBarShown == false,
            canShowBottomBar && container != nil else { return }
        container.showBar()
    }
    
    func hideBottomBar() {
        guard container != nil else { return }
        container.hideBar()
    }
}

extension BottomBarViewController: BottomBarPresenting {}

extension BottomBarViewController: UINavigationControllerDelegate {
    func navigationController(_ navigationController: UINavigationController, willShow viewController: UIViewController, animated: Bool) {
        guard var controller = viewController as? BottomBarPresentable else { return }
        controller.ellipleBarController = self
    }
}

final class BottomBarView: UIView {
    private(set) var isBarShown = false
    var contentView = UIView() {
        didSet {
            contentView.removeFromSuperview()
            addSubview(contentView)
            setNeedsLayout()
            sendSubview(toBack: contentView)
        }
    }
    
    var barView: UIView! {
        didSet {
            barView.removeFromSuperview()
            guard let view = barView else { return }
            addSubview(view)
            updateFrames(animated: true)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        updateFrames()
    }
    
    private func updateFrames(animated: Bool = false) {
        func update() {
            barView.frame = {
                var frame = bounds
                frame.size.height = 66
                frame.origin.y = bounds.height - (isBarShown ? frame.height : 0)
                return frame
            }()
            
            contentView.frame = {
                var frame = bounds
//                if isBarShown {
//                    frame.size.height -= barView.frame.height
//                }
                return frame
            }()
            
            barView.alpha = isBarShown ? 1 : 0
        }
        if animated {
            barView.frame = {
                var frame = bounds
                frame.size.height = 66
                frame.origin.y = bounds.height - (isBarShown ? 0 : frame.size.height)
                return frame
            }()
            contentView.frame = bounds
            UIView.animate(withDuration: 0.35, delay: 0, options: .curveEaseIn, animations: { 
                update()
            }, completion: nil)
        } else {
            update()
        }
    }
    
    fileprivate func showBar() {
        isBarShown = true
        updateFrames(animated: true)
    }
    
    fileprivate func hideBar() {
        isBarShown = false
        updateFrames(animated: true)
    }
}
