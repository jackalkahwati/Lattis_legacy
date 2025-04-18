//
//  SLUnderlinedCharacterView.swift
//  Skylock
//
//  Created by Andre Green on 4/10/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit

class SLUnderlinedCharacterView: UIView {
    private var letter: String
    
    lazy private var letterLabel:UILabel = {
        let frame:CGRect = CGRect(
            x: 0.0,
            y: 0.0,
            width: self.bounds.size.width,
            height: 0.9*self.bounds.size.height
        )
        let label:UILabel = UILabel(frame: frame)
        label.text = self.letter
        label.font = UIFont(
            name: "HelveticaNeue-Light",
            size: frame.size.height > frame.size.width ? frame.size.width : frame.size.height
        )
        label.textColor = UIColor(red: 110, green: 223, blue: 158)
        label.textAlignment = NSTextAlignment.center
        
        return label
    }()
    
    lazy private var underlineView:UIView = {
        let height:CGFloat = 1.0
        let frame = CGRect(
            x: 0.0,
            y: self.bounds.size.height - height,
            width: self.bounds.size.width,
            height: height
        )
        
        let view:UIView = UIView(frame: frame)
        view.backgroundColor = UIColor(red: 97, green: 100, blue: 100)
        
        return view
    }()
    
    init(frame: CGRect, letter: String) {
        self.letter = letter
        super.init(frame: frame)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        self.addSubview(self.letterLabel)
        self.addSubview(self.underlineView)
    }
    
    func updateLetterLabel(letter: String) {
        self.letter = letter
        self.letterLabel.text = self.letter
    }
    
}
