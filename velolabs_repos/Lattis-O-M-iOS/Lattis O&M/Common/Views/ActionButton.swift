//
//  ActionButton.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 27/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit

class ActionButton: UIButton {
        
    init(_ action: Action = .none) {
        self.action = action
        super.init(frame: .zero)
        updateTitle()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var action: Action {
        didSet {
            updateTitle()
        }
    }
    
    fileprivate func updateTitle() {
        setTitle(action.title, for: .normal)
        setTitleColor(action.style.titleColor, for: .normal)
        backgroundColor = action.style.backgroundColor
    }
    
    struct Action {
        
        typealias Handler = () -> ()
        
        let title: String?
        let handler: Handler?
        let style: Style
        
        static let none = Action(title: nil, handler: nil, style: .inactive)
        
        static let ok = Action.plain(title: "Ok")
        
        static let cancel = Action.plain(title: "Cancel")
        
        static func plain(title: String?, style: Style? = nil, handler: Handler? = nil) -> Action {
            let s: Style
            if let st = style {
                s = st
            } else if handler == nil {
                s = .inactive
            } else {
                s = .active
            }
            return .init(title: title, handler: handler, style: s)
        }
        
        
        struct Style {
            let titleColor: UIColor
            let backgroundColor: UIColor
            
            static let active = Style(titleColor: .white, backgroundColor: .lsTurquoiseBlue)
            static let inactive = Style(titleColor: .gray, backgroundColor: .disabledActiveButtonBackgground)
            static let delete = Style(titleColor: .red, backgroundColor: .disabledActiveButtonBackgground)
            static let light = Style(titleColor: .gray, backgroundColor: .white)
        }
    }
}
