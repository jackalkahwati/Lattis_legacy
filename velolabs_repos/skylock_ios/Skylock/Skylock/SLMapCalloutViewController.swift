//
//  SLMapCalloutViewController.swift
//  Skylock
//
//  Created by Andre Green on 1/25/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

@objc public enum SLMapCalloutVCPane: Int {
    case Left
    case Right
}

@objc public protocol SLMapCalloutViewControllerDelegate:class {
    @objc func leftCalloutViewTapped(calloutController: SLMapCalloutViewController)
    @objc func rightCalloutViewTapped(calloutController: SLMapCalloutViewController)
}

@objc public class SLMapCalloutViewController: UIViewController, SLMapCalloutViewDelegate {
    var rightText: String?
    var distanceAway: Float?
    @objc public var lock: SLLock?
    weak var delegate: SLMapCalloutViewControllerDelegate?
    
    @objc public func setProperties(rightText: String, lock: SLLock) {
        self.rightText = rightText
        self.lock = lock
    }
    
    lazy var leftCalloutView: SLMapCalloutView = {
        let image = UIImage(named: "icon_mylock_off")
        let frame = CGRect(
            x: 0,
            y: 0,
            width: 0.5*self.view.bounds.size.width,
            height: self.view.bounds.size.height
        )
        
        let lockName:String = self.lock == nil ? "" : self.lock!.displayName
        
        let calloutView:SLMapCalloutView = SLMapCalloutView(
            frame: frame,
            upperText: lockName,
            lowerText: self.distanceAwayText(),
            selectedImageName: "icon_mylock_on",
            deselectedImageName: "icon_mylock_off"
        )
        calloutView.delegate = self
        
        return calloutView
    }()
    
    lazy var rightCalloutView: SLMapCalloutView = {
        let frame = CGRect(
            x: self.leftCalloutView.bounds.size.width,
            y: 0,
            width: 0.5*self.view.bounds.size.width,
            height: self.view.bounds.size.height
        )
        
        let text: String = self.rightText == nil ? "" : self.rightText!
        
        let calloutView:SLMapCalloutView = SLMapCalloutView(
            frame: frame,
            upperText: text,
            lowerText: "",
            selectedImageName: "icon_navigate_on",
            deselectedImageName: "icon_navigate_off"
        )
        calloutView.delegate = self
        
        return calloutView
    }()
    
    lazy var triangleView: UIView = {
        let frame = CGRect(
            x: -5,
            y: self.view.bounds.size.height - 20,
            width: 10,
            height: 10
        )
        
        let view = UIView(frame:frame)
        view.backgroundColor = UIColor.white
        view.transform = CGAffineTransform(rotationAngle: 0.25*CGFloat(M_PI))
        
        return view
    }()
    
    override public func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.white
        self.view.layer.cornerRadius = 5.0
    }
    
    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        self.view.addSubview(self.triangleView)
        self.view.addSubview(self.leftCalloutView)
        self.view.addSubview(self.rightCalloutView)
    }
    
    func distanceAwayText() -> String {
        let distance:Int = self.lock?.distanceAway ?? 0
        return "\(distance as Int)ft away"
    }
    
    @objc public func setCalloutViewUnselected() {
        self.leftCalloutView.setSelected(isSelected: false)
        self.rightCalloutView.setSelected(isSelected: false)
    }
    
    // MARK: callout view delegate methods
    func calloutViewTapped(calloutView: SLMapCalloutView) {
        if calloutView == self.leftCalloutView, let delegate = self.delegate {
            delegate.leftCalloutViewTapped(calloutController: self)
        } else if calloutView == self.rightCalloutView, let delegate = self.delegate {
            delegate.rightCalloutViewTapped(calloutController: self)
        }
    }
}
