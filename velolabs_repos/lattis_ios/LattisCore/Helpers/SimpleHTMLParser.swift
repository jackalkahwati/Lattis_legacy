//
//  SimpleHTMLParser.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Atributika

class SimpleHTMLParser {
    var regular: UIFont
    var bold: UIFont
    var italyc: UIFont
    var color: UIColor
        
    init(regular: UIFont = .theme(weight: .medium, size: .body), bold: UIFont = .theme(weight: .bold, size: .body), italyc: UIFont = .theme(weight: .bookItalic, size: .body), color: UIColor = .black) {
        self.regular = regular
        self.bold = bold
        self.italyc = italyc
        self.color = color
    }
    
    func parse(_ html: String) -> NSAttributedString {
        let all = Style.font(regular).foregroundColor(color)
        let b = Style("b").font(bold)
        let i = Style("i").font(italyc)
        return html.style(tags: b, i).styleAll(all).attributedString
    }
}
