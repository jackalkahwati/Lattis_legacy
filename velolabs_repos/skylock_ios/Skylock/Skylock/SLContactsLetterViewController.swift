//
//  SLContactsLetterViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/4/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

protocol SLContactsLetterViewControllerDelegate:class {
    func contactsLetterViewController(letterViewController: SLContactsLetterViewController, letter:String)
}

class SLContactsLetterViewController: UIViewController {
    weak var delegate:SLContactsLetterViewControllerDelegate?
    
    let letters:[String] = [
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z"
    ]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.white
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.fillButtons()
    }
    
    func fillButtons() {
        for view in self.view.subviews {
            view.removeFromSuperview()
        }
        
        let height = self.view.bounds.size.height/CGFloat(self.letters.count)
        
        for (index, letter) in self.letters.enumerated() {
            let frame = CGRect(x: 0.0, y: CGFloat(index)*height, width: self.view.bounds.size.width, height: height)
            let button:UIButton = UIButton(frame: frame)
            button.tag = index
            button.addTarget(self, action: #selector(buttonPressed(button:)), for: .touchUpInside)
            button.addTarget(self, action: #selector(buttonPressed(button:)), for: .touchUpOutside)
            button.setTitle(letter, for: .normal)
            button.setTitleColor(UIColor(red: 102, green: 177, blue: 227), for: .normal)
            button.titleLabel?.font = UIFont.systemFont(ofSize: 13)
            self.view.addSubview(button)
        }
    }
    
    func buttonPressed(button:UIButton) {
        if button.tag >= self.letters.count {
            return
        }
        
        self.delegate?.contactsLetterViewController(letterViewController: self, letter: self.letters[button.tag])
    }
}
