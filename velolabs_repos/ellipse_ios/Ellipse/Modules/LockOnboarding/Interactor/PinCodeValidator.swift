//
//  PinCodeValidator.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/27/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

class PinCodeValidator {
    var onChange: (Ellipse.Pin, Action, Bool) -> () = {_,_,_ in}
    var initial: [Ellipse.Pin] = [] {
        didSet {
            code = initial
        }
    }
    
    fileprivate let min = 4
    fileprivate let max = 8
    fileprivate(set) var code: [Ellipse.Pin] = []
    
    func insert(_ touch: Ellipse.Pin) {
        guard code.count < max else { return }
        code.append(touch)
        onChange(touch, .insert, isValid)
    }
    
    func pop() {
        if let touch = code.popLast() {
            onChange(touch, .delete, isValid)
        }
    }
    
    fileprivate var isValid: Bool {
        return code.count >= min && code != initial
    }
}

extension PinCodeValidator {
    enum Action {
        case insert, delete
    }
}
