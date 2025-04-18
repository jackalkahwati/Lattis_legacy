//
//  EditInfoViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 09/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class EditViewController: UIViewController {
    
    let contentView = UIStackView()
    let infoLabel = UILabel.label(font: .theme(weight: .medium, size: .body), allignment: .center, lines: 0)
    let iconView = UIImageView()
    var centerLayout: NSLayoutConstraint!
    var bottomLayout: NSLayoutConstraint!
    let actionContainer = ActionContainer(left: .ok)
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        
        modalTransitionStyle = .crossDissolve
        modalPresentationStyle = .overCurrentContext
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        let titleStack = UIStackView(arrangedSubviews: [iconView, infoLabel])
        titleStack.axis = .horizontal
        titleStack.spacing = .margin/2
        titleStack.distribution = .fillProportionally
        
        let containerView = UIView()
        
        containerView.layer.cornerRadius = .containerCornerRadius
        containerView.backgroundColor = .white
        containerView.addShadow(offcet: .init(width: 0, height: -2))
        contentView.spacing = .margin/2
        contentView.axis = .vertical
        contentView.addArrangedSubview(titleStack)
        contentView.addArrangedSubview(actionContainer)
        containerView.addSubview(contentView)
        view.addSubview(containerView)
        
        constrain(containerView, contentView, view) { container, content, view in
            container.left == view.left
            container.right == view.right
            self.centerLayout = container.centerY == view.centerY ~ .defaultHigh
            self.bottomLayout = container.bottom == view.bottom ~ .defaultLow
            
            content.edges == container.edges.inseted(by: .margin)
        }
        
        view.backgroundColor = UIColor(white: 0.3, alpha: 0.8)
        
        
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name: UIResponder.keyboardDidShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide(notification:)), name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    
    @objc func keyboardWillShow(notification: Notification) {
        guard let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue,
            let duration = (notification.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? NSNumber)?.doubleValue else { return }
        bottomLayout.constant = -keyboardSize.height
        bottomLayout.priority = .defaultHigh
        centerLayout.priority = .defaultLow
        contentView.superview?.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        UIView.animate(withDuration: duration, animations: view.layoutIfNeeded)
    }
    
    @objc func keyboardWillHide(notification: Notification) {
        guard let duration = (notification.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? NSNumber)?.doubleValue else { return }
        bottomLayout.priority = .defaultLow
        centerLayout.priority = .defaultHigh
        contentView.superview?.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner, .layerMinXMaxYCorner, .layerMaxXMaxYCorner]
        UIView.animate(withDuration: duration, animations: view.layoutIfNeeded)
    }
    
    func startSaving() {
        actionContainer.right.backgroundColor = .secondaryBackground
        actionContainer.right.setTitleColor(.gray, for: .normal)
        actionContainer.right.beginAnimation()
        contentView.isUserInteractionEnabled = false
    }
    
    func stopSaving() {
        actionContainer.right.endAnimation()
        actionContainer.right.backgroundColor = .accent
        actionContainer.right.setTitleColor(.white, for: .normal)
        contentView.isUserInteractionEnabled = true
    }
}

extension EditViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        dismiss(animated: true, completion: nil)
        return false
    }
}

class EditInfoViewController: EditViewController {
    
    fileprivate var info: EditInfo
    fileprivate var textView: TextFieldView
    fileprivate let warningLabel = UILabel.label(font: .theme(weight: .medium, size: .small), color: .warning, allignment: .center, lines: 0)
    
    init(_ info: EditInfo, textView: TextFieldView) {
        self.info = info
        self.textView = textView
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        update(info: info, textView: textView)
        contentView.insertArrangedSubview(warningLabel, at: 2)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        _ = textView.field.becomeFirstResponder()
    }
    
    func update(info: EditInfo, textView: TextFieldView) {
        stopSaving()
        self.info = info
        self.textView = textView
        textView.field.delegate = self
        if let old = contentView.arrangedSubviews[1] as? TextFieldView {
            contentView.removeArrangedSubview(old)
            old.removeFromSuperview()
        }
        contentView.insertArrangedSubview(textView, at: 1)
        textView.field.placeholder = info.placeholder
        infoLabel.text = info.description
        textView.text = nil
        actionContainer.update(left: .plain(title: "cancel".localized(), style: .plain, handler: close), right: .plain(title: info.actionTitle, handler: { [unowned self] in
            self.handleAction()
        }), priority: .right)
        textView.field.becomeFirstResponder()
    }
    
    func showDefaultWarning() {
        guard let text = info.warning else { return }
        show(warning: text)
    }
    
    fileprivate func show(warning: String) {
        textView.isFailed = true
        warningLabel.text = warning
    }
    
    fileprivate func hideWarning() {
        textView.isFailed = false
        warningLabel.text = nil
    }
    
    @objc fileprivate func handleAction() {
        guard let text = textView.text else { return }
        if !info.validate(text).1, let warning = info.warning {
            return show(warning: warning)
        }
        startSaving()
        info.handler(text)
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if info.placeholder == "hint_enter_code".localized(), error.isInvalidConfirmationCode {
            return showDefaultWarning()
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
    
    override func startSaving() {
        super.startSaving()
        textView.field.resignFirstResponder()
        textView.field.isUserInteractionEnabled = false
    }
    
    override func stopSaving() {
        super.stopSaving()
        textView.field.becomeFirstResponder()
        textView.field.isUserInteractionEnabled = true
    }
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let oldString = (textField.text as NSString?) else { return true }
        hideWarning()
        let newString = oldString.replacingCharacters(in: range, with: string)
        let (replace, _) = info.validate(newString)
        if let r = replace {
            textField.text = r
            return false
        }
        return true
    }
}
