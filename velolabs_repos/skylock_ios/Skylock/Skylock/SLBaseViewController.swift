//
//  SLBaseViewController.swift
//  Ellipse
//
//  Created by Andre Green on 8/24/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLBaseViewController: UIViewController, SLWarningViewControllerDelegate {
    var warningBackgroundView:UIView?
    
    var warningViewController:SLWarningViewController?
    
    var cancelClosure:(() -> ())?
    
    var loadingView:SLLoadingView?

    func makeLoadingView() -> SLLoadingView {
        let height:CGFloat = 200.0
        let frame = CGRect(
            x: 0.0,
            y: 0.5*(self.view.bounds.size.height - height),
            width: self.view.bounds.size.width,
            height: height
        )
        
        let view:SLLoadingView = SLLoadingView(frame: frame)
        view.backgroundColor = UIColor.clear
        
        return view
    }
    
    func presentWarningViewControllerWithTexts(
        texts:[SLWarningViewControllerTextProperty:String?],
        cancelClosure: (() -> ())?,
        actionClosure: (() -> ())? = nil
        )
    {
        guard warningViewController == nil else { return }
        DispatchQueue.main.async {
            self.addWarningBackgroundView()
            
            let width:CGFloat = 268.0
            let height:CGFloat = 211.0
            
            self.warningViewController = SLWarningViewController()
            if let action = actionClosure {
                self.warningViewController?.actionClosure = action
            }
            self.warningViewController!.setTextProperties(texts: texts)
            self.warningViewController!.view.frame = CGRect(
                x: 0.5*(self.view.bounds.size.width - width),
                y: 100.0,
                width: width,
                height: height
            )
            self.warningViewController!.delegate = self
            self.cancelClosure = cancelClosure
            
            self.addChildViewController(self.warningViewController!)
            self.view.addSubview(self.warningViewController!.view)
            self.view.bringSubview(toFront: self.warningViewController!.view)
            self.warningViewController!.didMove(toParentViewController: self.warningViewController!)
        }
    }
    
    func presentLoadingViewWithMessage(message: String) {
        self.addWarningBackgroundView()
        self.loadingView = self.makeLoadingView()
        self.loadingView!.setMessage(message: message)
        self.view.addSubview(self.loadingView!)
        self.loadingView!.rotate()
    }
    
    func dismissLoadingViewWithCompletion(completion: (() -> ())?) {
        DispatchQueue.main.async {
            UIView.animate(withDuration: 0.2, animations: {
                self.warningBackgroundView?.alpha = 0.0
                self.loadingView?.alpha = 0.0
            }) { (finished) in
                self.warningBackgroundView?.removeFromSuperview()
                self.loadingView?.removeFromSuperview()
                self.loadingView = nil
                self.warningBackgroundView = nil
                self.warningViewController?.removeFromParentViewController()
                completion?()
            }
        }
    }
    
    private func addWarningBackgroundView() {
        if self.warningBackgroundView != nil {
            return
        }
        
        self.warningBackgroundView = UIView(frame: self.view.bounds)
        self.warningBackgroundView?.backgroundColor = UIColor(white: 0.2, alpha: 0.75)
        self.view.addSubview(self.warningBackgroundView!)
    }
    
    // MARK: SLWarningViewControllerDelegate Methods
    func warningVCTakeActionButtonPressed(wvc: SLWarningViewController) {
        // This method should be overriden by child class with calling super
        closeWarning()
    }
    
    internal func closeWarning(completion:(() -> ())? = nil) {
        if let background = self.warningBackgroundView {
            UIView.animate(withDuration: 0.2, animations: {
                self.warningViewController?.view.alpha = 0.0
                background.alpha = 0.0
            }) { (finished) in
                self.warningViewController?.view.removeFromSuperview()
                self.warningViewController?.removeFromParentViewController()
                self.warningViewController?.view.removeFromSuperview()
                background.removeFromSuperview()
                self.warningBackgroundView = nil
                self.warningViewController = nil
                completion?()
            }
        } else {
            print("Error: could not find background view while removing warning view controller")
            self.cancelClosure?()
        }
    }
    
    func warningVCCancelActionButtonPressed(wvc: SLWarningViewController) {
        closeWarning { 
            self.cancelClosure?()
        }
    }
    
    func addMenuButton(action: Selector? = nil) {
        navigationItem.leftBarButtonItem = UIBarButtonItem(image: UIImage(named: "lock_screen_hamburger_menu"), style: .plain, target: self, action: action ?? #selector(backAction))
    }
    
    func addBackButton() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(image: UIImage(named: "back_icon_white"), style: .plain, target: self, action: #selector(backAction))
    }
    
    func backAction() {
        if isModal {
            presentingViewController?.dismiss(animated: true, completion: nil)
        } else {
            _ = navigationController?.popViewController(animated: true)
        }
    }
    
    var isModal: Bool {
        if let nav = navigationController, nav.viewControllers.count > 1 {
            return false
        } else if presentationController != nil {
            return true
        }
        
        return false
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}
