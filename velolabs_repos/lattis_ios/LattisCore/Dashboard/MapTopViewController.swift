//
//  MapTopViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 27/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import CoreLocation

extension Notification.Name {
    static let ble = Notification.Name(rawValue: "io.lattis.notification.ble")
}

final class ActionContainer: UIStackView {
    let left = ActionButton()
    let right = ActionButton()
    fileprivate var priority: Priority = .equal
    
    init(left: ActionButton.Action? = nil, right: ActionButton.Action? = nil, priority: Priority = .equal) {
        guard left != nil || right != nil else {
            fatalError("At least one action is required")
        }
        self.left.isHidden = left == nil
        if let l = left {
            self.left.action = l
        }
        self.right.isHidden = right == nil
        if let r = right {
            self.right.action = r
        }
        super.init(frame: .zero)
        addArrangedSubview(self.left)
        addArrangedSubview(self.right)
        translatesAutoresizingMaskIntoConstraints = false
        axis = .horizontal
        spacing = .margin/2
        
        switch priority {
        case .equal:
            self.left.setContentHuggingPriority(.defaultLow, for: .horizontal)
            self.right.setContentHuggingPriority(.defaultLow, for: .horizontal)
            distribution = .fillEqually
        case .left:
            self.left.setContentHuggingPriority(.defaultLow, for: .horizontal)
            self.right.setContentHuggingPriority(.defaultHigh, for: .horizontal)
            distribution = .fill
        case .right:
            self.left.setContentHuggingPriority(.defaultHigh, for: .horizontal)
            self.right.setContentHuggingPriority(.defaultLow, for: .horizontal)
            distribution = .fill
        }
    }
    
    required init(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func update(left: ActionButton.Action? = nil, right: ActionButton.Action? = nil, priority: Priority = .equal) {
        if let l = left {
            self.left.action = l
        }
        if let r = right {
            self.right.action = r
        }
        switch priority {
        case .equal:
            self.left.setContentHuggingPriority(.defaultLow, for: .horizontal)
            self.right.setContentHuggingPriority(.defaultLow, for: .horizontal)
            distribution = .fillEqually
        case .left:
            self.left.setContentHuggingPriority(.defaultLow, for: .horizontal)
            self.right.setContentHuggingPriority(.defaultHigh, for: .horizontal)
            distribution = .fill
        case .right:
            self.left.setContentHuggingPriority(.defaultHigh, for: .horizontal)
            self.right.setContentHuggingPriority(.defaultLow, for: .horizontal)
            distribution = .fill
        }
        self.left.isHidden = left == nil
        self.right.isHidden = right == nil
    }
}

extension ActionContainer {
    enum Priority {
        case left
        case right
        case equal
    }
}

public class MapTopViewController: UIViewController, OverMap {
    
    weak public var mapController: MapRepresentable? {
        didSet {
            (view as? PassthroughView)?.targetView = mapController?.mapView
        }
    }
    
    let footerView = UIView()
    let actionContainer = ActionContainer(left: .ok, right: nil)
    let cardView = UIView()
    let contentView = UIView()
    let dragView = UIView()
    let cardBackground = UIView()
    let mapHomeButton = UIButton.rounded()
    var menuButton: UIView?
    
    let hintView = UIView()
    let hintLabel = UILabel.label(font: .theme(weight: .medium, size: .text), color: .white, lines: 0)
    fileprivate let hintImageView = UIImageView()
    fileprivate var hintLeftLayout: NSLayoutConstraint!
    
    fileprivate var expandedView: UIView?
    fileprivate var regularContentView: UIView?
    
    fileprivate var fullSizeCardLayout: NSLayoutConstraint!
    fileprivate var hintTimer: Timer?
    
    public override func loadView() {
        view = PassthroughView()
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
                
        if let menu = addMenuButton(navigation: false) {
            menuButton = menu
            view.insertSubview(menu, belowSubview: cardBackground)
        }
        
        view.addSubview(mapHomeButton)
        view.addSubview(hintView)
        view.addSubview(footerView)
        footerView.backgroundColor = .background
        footerView.addShadow(color: .shadow, offcet: .init(width: 0, height: 2), radius: 9, opacity: 1)
        footerView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        footerView.layer.cornerRadius = .containerCornerRadius
        
        cardBackground.backgroundColor = UIColor(white: 0, alpha: 0.5)
        cardBackground.alpha = 0
        cardBackground.isHidden = true
        view.addSubview(cardBackground)
        
        
        hintView.addSubview(hintImageView)
        hintView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        hintView.layer.cornerRadius = .containerCornerRadius
        hintView.addSubview(hintLabel)
        hintView.alpha = 0
        hintView.isHidden = true
        
        view.addSubview(cardView)
        cardView.backgroundColor = .background
        cardView.addShadow(color: .shadow, offcet: .init(width: 0, height: 2), radius: 9, opacity: 1)
        cardView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        cardView.layer.cornerRadius = .containerCornerRadius
        cardView.alpha = 0
        cardView.isHidden = true
        
        dragView.backgroundColor = .thirdBackground
        dragView.layer.cornerRadius = 3
        cardView.addSubview(dragView)
        
        cardView.addSubview(contentView)
        
        view.addSubview(actionContainer)
        
        mapHomeButton.setImage(.named("icon_location_home"), for: .normal)
        mapHomeButton.tintColor = .accent
        mapHomeButton.addTarget(self, action: #selector(centerMap), for: .touchUpInside)
        view.clipsToBounds = true
        
        constrain(footerView, actionContainer, cardView, dragView, contentView, cardBackground, mapHomeButton, view) { footer, action, card, drag, content, background, home, view in
            footer.bottom == view.bottom + .containerCornerRadius
            footer.left == view.left
            footer.right == view.right
            
            action.top >= footer.top + .containerCornerRadius
            action.left == footer.left + .margin
            action.right == footer.right - .margin
            action.bottom == view.safeAreaLayoutGuide.bottom - .margin/2
            
            card.bottom == footer.bottom
            card.left == view.left
            card.right == view.right
            card.top == footer.top ~ .defaultLow
            self.fullSizeCardLayout = card.top == view.safeAreaLayoutGuide.top + .margin/2 ~ .fittingSizeLevel
            
            content.bottom == action.top - .margin/2
            content.left == card.left
            content.right == card.right
            content.top == card.top + .containerCornerRadius
            
            drag.height == 6
            drag.top == card.top + 6
            drag.width == 46
            drag.centerX == card.centerX
            
            background.top == view.top
            background.left == view.left
            background.right == view.right
            background.bottom == view.bottom
            
            home.right == view.right - .margin/2
        }
        
        let hintClose = UIButton.rounded(height: 24, width: 24)
        hintClose.backgroundColor = UIColor(white: 0.0, alpha: 0.2)
        hintClose.setImage(.named("icon_close_small"), for: .normal)
        hintClose.addTarget(self, action: #selector(hideHint), for: .touchUpInside)
        hintView.addSubview(hintClose)
        
        constrain(cardView, hintView, hintLabel, hintClose, hintImageView) { card, container, hint, close, logo in
            container.left == card.left
            container.right == card.right
            container.bottom == card.top + .containerCornerRadius
            
            close.right == container.right - .margin/2
            close.top == container.top + .margin/2
            
            hint.top == container.top + .margin/2
            self.hintLeftLayout = hint.left == container.left + .margin/2
            hint.right == close.left - .margin/2
            hint.bottom <= card.top - .margin/2
            
            logo.right == container.right
            logo.bottom == container.bottom
        }
        
        (view as? PassthroughView)?.targetView = mapController?.mapView
        
        NotificationCenter.default.addObserver(self, selector: #selector(handleBle(notification:)), name: .ble, object: nil)
    }
    
    @objc
    func handleBle(notification: Notification) {
        guard let isEnabled = notification.object as? Bool else { return }
        if isEnabled && hintLabel.text == "bluetooth_access_alert_message".localized() {
            hideHint()
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    public override func startLoading(_ text: String) {
        mapController?.startLoading(text)
    }
    
    public override func stopLoading(completion: (() -> ())? = nil) {
        mapController?.stopLoading(completion: completion)
    }
    
    func show(content: UIView, expanded: UIView? = nil) {
        let old = contentView.subviews.first
        contentView.addSubview(content)
        if cardView.isHidden {
            cardView.isHidden = false
        }
        dragView.isHidden = expanded == nil
        if let ex = expanded {
            expandedView = ex
            ex.alpha = 0
        } else {
            cardBackground.isHidden = false
        }
        
        content.alpha = 0
        constrain(content, contentView) { c, v in
            c.edges == v.edges
        }
        view.layoutIfNeeded()
        fullSizeCardLayout.priority = .fittingSizeLevel
        UIView.animate(withDuration: 0.3, animations: {
            self.cardBackground.alpha = expanded == nil ? 1 : 0
            old?.alpha = 0
            content.alpha = 1
            self.cardView.alpha = 1
            self.view.layoutIfNeeded()
        }, completion: { _ in
            old?.removeFromSuperview()
            self.cardBackground.isHidden = expanded != nil
            self.removeChild(by: old)
        })
    }
    
    func hideContent() {
        let old = contentView.subviews.first
        UIView.animate(withDuration: 0.3, animations: {
            self.cardView.alpha = 0
            self.cardBackground.alpha = 0
            self.view.layoutIfNeeded()
        }, completion: { _ in
            old?.removeFromSuperview()
            self.cardView.isHidden = false
            self.cardBackground.isHidden = true
            self.removeChild(by: old)
        })
    }
    
    fileprivate func removeChild(by view: UIView?) {
        guard let old = view, let child = children.first(where: { $0.view == old }) else { return }
        child.removeFromParent()
        child.didMove(toParent: nil)
    }
    
    func showExtended() {
        regularContentView = contentView.subviews.first
        fullSizeCardLayout.priority = .defaultHigh
        dragView.isHidden = false
        cardBackground.isHidden = false
        if expandedView?.superview == nil, let view = expandedView {
            contentView.addSubview(view)
            constrain(view, contentView) { expanded, container in
                expanded.edges == container.edges
            }
        }
        UIView.animate(withDuration: 0.3, animations: {
            self.regularContentView?.alpha = 0
            self.expandedView?.alpha = 1
            self.cardBackground.alpha = 1
            self.view.layoutIfNeeded()
        }, completion: { _ in
            self.regularContentView?.removeFromSuperview()
        })
    }
    
    func render(hint: String, color: UIColor = .accentBlue, logo: UIImage? = nil, autoHide: TimeInterval? = nil, extraSpace: CGFloat = 0) {
        guard hintView.isHidden || hintLabel.text != hint else { return }
        if cardBackground.isHidden {
            view.insertSubview(hintView, belowSubview: footerView)
        } else {
            view.insertSubview(hintView, aboveSubview: cardBackground)
        }
        hintLabel.text = hint
        hintView.isHidden = false
        hintView.alpha = 0
        hintView.backgroundColor = color
        hintImageView.image = logo
        hintLeftLayout.constant = .margin + extraSpace
        UIView.animate(withDuration: 0.3) {
            self.view.layoutIfNeeded()
            self.hintView.alpha = 1
        }
        if var auto = autoHide  {
            auto = auto > 3 ? auto : 3
            hintTimer?.invalidate()
            hintTimer = Timer.scheduledTimer(withTimeInterval: auto, repeats: false, block: { [weak self] _ in
                self?.hideHint()
            })
        }
    }
    
    @objc
    func hideHint() {
        guard !hintView.isHidden else { return }
        hintLabel.text = nil
        UIView.animate(withDuration: 0.3, animations: {
            self.view.layoutIfNeeded()
            self.hintView.alpha = 0
        }, completion: { _ in
            self.hintView.isHidden = true
        })
    }
    
    @objc
    fileprivate func centerMap() {
        mapController?.centerOnUserLocation()
    }
    
    public func mapDidSelect(point: MapPoint) {}
    public func mapWillMove(byGesture: Bool) {}
    public func mapDidMove(byGesture: Bool) {}
    public func didTapOnMap() {}
    public func didUpdateUseer(location: CLLocation) {}
    public func canSelectPoint() -> Bool {return true}
    public func didLayoutSubviews() {}
}

final class PassthroughView: UIView {
    var touchTransparentView = UIView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(touchTransparentView)
        constrain(touchTransparentView, self) { $0.edges == $1.edges }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    weak var targetView: UIView?
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        var view = super.hitTest(point, with: event)
        if view == touchTransparentView {
            view = targetView
        }
        return view
    }
}

