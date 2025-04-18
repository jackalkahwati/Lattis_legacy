//
//  ActionButton.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 27/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class LoadingButton: UIButton {
    fileprivate let loadingView = UIView()
    fileprivate var isAnimating = false
    
    func beginAnimation(title: String? = nil, color: UIColor = .accent) {
        guard !isAnimating else { return }
        isAnimating = true
        clipsToBounds = true
        insertSubview(loadingView, at: 0)
        loadingView.layer.cornerRadius = layer.cornerRadius
        loadingView.backgroundColor = color
        loadingView.frame = {
            var f = self.bounds
            f.origin.x = -f.width*1.3
            return f
        }()
        UIView.animate(withDuration: 2, delay: 0.1, options: [.repeat, .curveEaseInOut], animations: {
            self.loadingView.frame = {
                var f = self.bounds
                f.size.width *= 0.8
                f.origin.x = self.bounds.width
                return f
            }()
        }, completion:nil)
    }
    
    func endAnimation() {
        isAnimating = false
        layer.removeAllAnimations()
        loadingView.removeFromSuperview()
    }
}

class ActionButton: LoadingButton {
    
    var htmlParser = SimpleHTMLParser()
    var activeStyle: Action.Style = .active
    var inactiveStyle: Action.Style = .inactive
    
    var isActive: Bool = true {
        didSet {
//            isEnabled = isActive
            let style = isActive ? activeStyle : inactiveStyle
            updateTitle(style)
        }
    }
    
    init(_ action: Action = .none, height: CGFloat = 48, cornerRadius: CGFloat = 24) {
        self.action = action
        super.init(frame: .zero)
        constrain(self) { $0.height == height }
        self.layer.cornerRadius = cornerRadius
        updateTitle()
        addTarget(self, action: #selector(handleAciton), for: .touchUpInside)
    }
    
    @objc
    fileprivate func handleAciton() {
        action.handler?()
    }
    
    convenience init(action: Action) {
        self.init(action)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var action: Action {
        didSet {
            updateTitle()
        }
    }
    
    fileprivate func updateTitle(_ style: Action.Style? = nil) {
        let s = style ?? action.style
        titleLabel?.font = s.font
        setTitle(action.title, for: .normal)
        setImage(action.icon?.withRenderingMode(.alwaysTemplate), for: .normal)
        let attributed: NSAttributedString?
        if let html = action.htmlTitle {
            htmlParser.color = s.titleColor
            attributed = htmlParser.parse(html)
        } else {
            attributed = nil
        }
        setAttributedTitle(attributed, for: .normal)
        setTitleColor(s.titleColor, for: .normal)
        tintColor = s.titleColor
        backgroundColor = s.backgroundColor
        if s.border {
            layer.borderColor = s.titleColor.cgColor
            layer.borderWidth = 2
        } else {
            layer.borderWidth = 0
        }
        var margin = CGFloat.margin
        if action.title == nil && attributed == nil {
            margin = 0
        }
        imageEdgeInsets = .init(top: 0, left: 0, bottom: 0, right: margin)
    }
    
    struct Action {
        
        typealias Handler = () -> ()
        
        let title: String?
        let icon: UIImage?
        let htmlTitle: String?
        let handler: Handler?
        let style: Style
        
        static let none = Action(title: nil, icon: nil, htmlTitle: nil, handler: nil, style: .inactive)
        
        static let ok = Action.plain(title: "ok".localized())
        static func ok(handler: Handler? = nil) -> Action {
            .plain(title: "ok".localized(), handler: handler)
        }
        
        static let cancel = Action.plain(title: "cancel".localized())
        
        static func plain(title: String?, icon: UIImage? = nil, style: Style? = nil, handler: Handler? = nil) -> Action {
            let s: Style
            if let st = style {
                s = st
            } else if handler == nil {
                s = .inactive
            } else {
                s = .active
            }
            return .init(title: title, icon: icon, htmlTitle: nil, handler: handler, style: s)
        }
        
        static func html(title: String?, icon: UIImage? = nil, style: Style? = nil, handler: Handler? = nil) -> Action {
            let s: Style
            if let st = style {
                s = st
            } else if handler == nil {
                s = .inactive
            } else {
                s = .active
            }
            return .init(title: nil, icon: icon, htmlTitle: title, handler: handler, style: s)
        }
        
        struct Style {
            let titleColor: UIColor
            let backgroundColor: UIColor
            let font: UIFont
            let border: Bool
            
            init(titleColor: UIColor, backgroundColor: UIColor, font: UIFont = .theme(weight: .medium, size: .body), border: Bool = false) {
                self.titleColor = titleColor
                self.backgroundColor = backgroundColor
                self.font = font
                self.border = border
            }
            
            static let active = Style(titleColor: .accentTint, backgroundColor: .accent)
            static let activeSecondary = Style(titleColor: .accent, backgroundColor: .secondaryBackground)
            static let inactiveSecondary = Style(titleColor: .gray, backgroundColor: .secondaryBackground)
            static let inactive = Style(titleColor: .tint, backgroundColor: .background, border: true)
            static let delete = Style(titleColor: .red, backgroundColor: .background)
            static let light = Style(titleColor: .gray, backgroundColor: .white)
            static let plain = Style(titleColor: .gray, backgroundColor: .clear, font: .theme(weight: .medium, size: .text))
        }
    }
}
