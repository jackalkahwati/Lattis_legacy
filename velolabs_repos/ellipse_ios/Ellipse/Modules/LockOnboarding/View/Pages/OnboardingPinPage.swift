//
//  OnboardingPinPage.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/16/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import EasyTipView
import Atributika

fileprivate extension Style {
    static let all = Style.font(.elTitleLight).foregroundColor(.black)
    static let u = Style("u").underlineStyle(.single)
}

protocol OnboardingPinPageDelegate: class {
    func save(pin: [Ellipse.Pin])
}

class OnboardingPinPage: ViewController, LockOnboardingPage {
    @IBOutlet weak var buttonsContainer: UIView!
    @IBOutlet weak var lockImageView: UIImageView!
    @IBOutlet weak var hintLabel: Label!
    @IBOutlet weak var upButton: PinButton!
    @IBOutlet weak var leftButton: PinButton!
    @IBOutlet weak var rightButton: PinButton!
    @IBOutlet weak var downButton: PinButton!
    @IBOutlet weak var saveButton: ValidationButton!
    @IBOutlet weak var codeView: PinCodeView!
    @IBOutlet weak var descriptionLabel: Label!
    
    var style: Style = .onboard
    fileprivate let validator = PinCodeValidator()
    fileprivate weak var hintView: EasyTipView?
    
    weak var delegate: OnboardingPinPageDelegate?
    func set(delegate: Any?) {
        self.delegate = delegate as? OnboardingPinPageDelegate
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "pin_code".localized()
        
        leftButton.direction = .left
        rightButton.direction = .right
        downButton.direction = .down
        
        validator.onChange = { [unowned self] touch, action, isValid in
            self.hintLabel.isHidden = self.validator.code.isEmpty == false
            self.saveButton.isValid = isValid
            switch action {
            case .insert:
                self.codeView.insert(touch)
            case .delete:
                self.codeView.pop()
            }
        }
        
        descriptionLabel.attributedText = "pin_edit_description".localized().style(tags: .u).styleAll(.all).attributedString
        
        switch style {
        case .edit(let code):
            validator.initial = code
            codeView.update(code: code)
            addBackButton()
            hintLabel.isHidden = code.isEmpty == false
        default:
            break
        }
    }
    
//    override func viewDidAppear(_ animated: Bool) {
//        super.viewDidAppear(animated)
//        if case .onboard = style {
//            isHintHidden = false
//        }
//    }
    
    fileprivate var isHintHidden: Bool = true
//    {
//        didSet {
//            guard isHintHidden != oldValue else { return }
//            if isHintHidden {
//                hintView?.dismiss()
//            } else {
//                var pref = EasyTipView.globalPreferences
//                pref.positioning.bubbleVInset = 1
//                pref.drawing.arrowPosition = .top
//                let view = EasyTipView(text: "pin_hint".localized(), preferences: pref, delegate: nil)
//                view.show(animated: true, forView: buttonsContainer, withinSuperview: self.view)
//                hintView = view
//            }
//        }
//    }
    
    @IBAction func pinAction(_ sender: PinButton) {
        validator.insert(sender.direction)
        UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseIn, animations: {
            sender.backgroundColor = .elDarkSkyBlue
        }, completion: { _ in
            UIView.animate(withDuration: 0.5, delay: 0, options: .curveEaseIn, animations: {
                sender.backgroundColor = .clear
            }, completion: nil)
        })
        isHintHidden = validator.code.isEmpty == false
    }
    
    @IBAction func backward(_ sender: Any) {
        validator.pop()
        isHintHidden = validator.code.isEmpty == false
    }
    
    @IBAction func save(_ sender: Any) {
        delegate?.save(pin: validator.code)
    }
}

extension OnboardingPinPage {
    enum Style {
        case onboard
        case edit([Ellipse.Pin])
    }
}

class PinButton: UIButton {
    var direction: Ellipse.Pin = .up
}
