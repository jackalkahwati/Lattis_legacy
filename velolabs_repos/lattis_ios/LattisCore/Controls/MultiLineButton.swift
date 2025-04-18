//
//  MultiLineButton.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 01.11.2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Cartography

final class MultiLineButton: UIControl {
    let titleLabel = UILabel()
    var htmlParser = SimpleHTMLParser()
    
    init() {
        super.init(frame: .zero)
        addSubview(titleLabel)
        titleLabel.textAlignment = .center
        titleLabel.numberOfLines = 0
        constrain(titleLabel, self) { title, view in
            title.edges == view.edges.inseted(by: 5)
        }
    }
    
    var title: AlertController.Text = .plain("") {
        didSet {
            switch title {
            case .plain(let string):
                titleLabel.text = string
            case .html(let string):
                titleLabel.attributedText = htmlParser.parse(string)
            }
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
