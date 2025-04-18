//
//  SLWarningViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/23/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

protocol SLWarningViewControllerDelegate:class {
    func warningVCTakeActionButtonPressed(wvc: SLWarningViewController)
    func warningVCCancelActionButtonPressed(wvc: SLWarningViewController)
}

enum SLWarningViewControllerTextProperty {
    case Header
    case Info
    case CancelButton
    case ActionButton
}

class SLWarningViewController: UIViewController {
    var actionClosure: () -> () = {}
    private let padding:CGFloat = 10.0
    
    private let buttonSpacer:CGFloat = 2.0
    
    private var texts:[SLWarningViewControllerTextProperty:String?] = [SLWarningViewControllerTextProperty:String?]()
    
    weak var delegate:SLWarningViewControllerDelegate?
    
    private lazy var headerLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.padding
        let height:CGFloat = 24.0
        let frame = CGRect(x: self.padding, y: 25.0, width: labelWidth, height: height)
        
        let label:UILabel = UILabel(frame: frame)
        label.text = self.getText(property: .Header)
        label.textColor = SLUtilities().color(colorCode: .Color76_79_97)
        label.font = UIFont.systemFont(ofSize: 20)
        label.textAlignment = .center
        label.adjustsFontSizeToFitWidth = true
        label.minimumScaleFactor = 0.5
        
        return label
    }()
    
    private lazy var infoLabel:UILabel = {
        let labelWidth = self.view.bounds.size.width - 2*self.padding
        let utility = SLUtilities()
        let font = UIFont.systemFont(ofSize: 10.0)
        let text = self.getText(property: .Info) == nil ? "" : self.getText(property: .Info)!
        let labelSize:CGSize = utility.sizeForLabel(
            font: font,
            text: text,
            maxWidth: labelWidth,
            maxHeight: CGFloat.greatestFiniteMagnitude,
            numberOfLines: 0
        )
        
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - labelSize.width),
            y: self.headerLabel.frame.maxY + 15.0,
            width: labelSize.width,
            height: labelSize.height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.textColor = utility.color(colorCode: .Color76_79_97)
        label.text = text
        label.textAlignment = .center
        label.font = font
        label.numberOfLines = 0
        
        return label
    }()
    
    lazy var cancelButton:UIButton = {
        let actionButtonTitle = self.getText(property: .ActionButton)
        let width = actionButtonTitle == nil ? self.view.bounds.size.width - 2.0*self.padding
            : 0.5*(self.view.bounds.size.width - self.buttonSpacer) - self.padding
        let height:CGFloat = 45.0
        let util = SLUtilities()
        let frame = CGRect(
            x: self.padding,
            y: self.view.bounds.size.height - self.padding - height,
            width: width,
            height: height
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.setTitle(self.getText(property: .CancelButton), for: .normal)
        button.setTitleColor(util.color(colorCode: .Color155_155_155), for: .normal)
        button.backgroundColor = util.color(colorCode: .Color239_239_239)
        button.addTarget(self, action: #selector(cancelButtonPressed), for: .touchUpInside)
        
        return button
    }()
    
    private lazy var actionButton:UIButton = {
        let width = self.cancelButton.bounds.size.width
        let height:CGFloat = 45.0
        let util = SLUtilities()
        let frame = CGRect(
            x: self.view.bounds.size.width - self.padding - width,
            y: self.view.bounds.size.height - self.padding - height,
            width: width,
            height: height
        )
        
        let button:UIButton = UIButton(type: .system)
        button.frame = frame
        button.setTitle(self.getText(property: .ActionButton), for: .normal)
        button.setTitleColor(util.color(colorCode: .Color255_255_255), for: .normal)
        button.backgroundColor = util.color(colorCode: .Color102_177_227)
        button.addTarget(self, action: #selector(actionButtonPressed), for: .touchUpInside)
        
        return button
    }()
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        self.view.backgroundColor = UIColor.white
        
        if !self.view.subviews.contains(self.headerLabel) {
            self.view.addSubview(self.headerLabel)
        }
        
        if !self.view.subviews.contains(self.infoLabel) {
            self.view.addSubview(self.infoLabel)
        }
        
        if !self.view.subviews.contains(self.cancelButton) {
            self.view.addSubview(self.cancelButton)
        }
        
        if !self.view.subviews.contains(self.actionButton) && self.getText(property: .ActionButton) != nil {
            self.view.addSubview(self.actionButton)
        }
    }
    
    private func getText(property: SLWarningViewControllerTextProperty) -> String? {
        if let text = texts[property] {
            return text
        }
        
        return nil
    }
    
    func setTextProperties(texts: [SLWarningViewControllerTextProperty:String?]) {
        self.texts = texts
    }
    
    @objc private func cancelButtonPressed() {
        self.delegate?.warningVCCancelActionButtonPressed(wvc: self)
    }
    
    @objc private func actionButtonPressed() {
        self.delegate?.warningVCTakeActionButtonPressed(wvc: self)
        actionClosure()
    }
}
