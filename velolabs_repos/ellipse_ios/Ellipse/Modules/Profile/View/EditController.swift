//
//  EditController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 24/01/2019.
//  Copyright © 2019 Lattis. All rights reserved.
//

import Cartography

class EditController: ViewController {
    
    init(content: EditableContent, headerView: UIView? = nil, saveText: String = "profile_save".localized()) {
        self.content = content
        self.headerView = headerView
        super.init(nibName: nil, bundle: nil)
        saveButton.setTitle(saveText, for: .normal)
        modalPresentationStyle = .overCurrentContext
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }
    
    var save: (String) -> () = {_ in}
    var validate: (String) -> Bool = {_ in return true}
    var format: (String) -> String? = {_ in return nil}
    fileprivate(set) var content: EditableContent
    fileprivate var headerView: UIView?
    fileprivate let inputControl = TitledInputControl(title: "")
    fileprivate let cancelButton = UIButton(type: .custom)
    fileprivate let saveButton = UIButton(type: .custom)
    fileprivate let headerContainer = UIView()
    fileprivate var bottomLayout: NSLayoutConstraint?
    fileprivate let hideShowButton = UIButton(type: .custom)
    var canSaveValue: Bool = false {
        didSet {
            saveButton.alpha = canSaveValue ? 1 : 0.5
            saveButton.isEnabled = canSaveValue
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        addCloseButton()
        
        view.backgroundColor = .clear
        let blurView = UIVisualEffectView(effect: UIBlurEffect(style: .dark))
        view.addSubview(blurView)
        let buttonsContainer = UIView()
        let contentContainer = UIView()
        let bgButton = UIButton(type: .custom)
        contentContainer.backgroundColor = .white
        contentContainer.cornerRadius = 8
        view.addSubview(bgButton)
        view.addSubview(contentContainer)
        view.addSubview(buttonsContainer)
        buttonsContainer.addSubview(cancelButton)
        buttonsContainer.addSubview(saveButton)
        contentContainer.addSubview(inputControl)
        contentContainer.addSubview(headerContainer)
        smallNegativeStyle(cancelButton)
        smallPositiveStyle(saveButton)
        cancelButton.setTitle("cancel".localized().lowercased().capitalized, for: .normal)
        canSaveValue = false
        
        hideShowStyle(hideShowButton)
        hideShowButton.addTarget(self, action: #selector(switchSecret(_:)), for: .touchUpInside)
        
        buttonsContainer.backgroundColor = .white
        constrain(blurView, buttonsContainer, contentContainer, cancelButton, saveButton, view, inputControl, headerContainer, bgButton) { blur, container, content, cancel, save, view, input, header, close in
            container.left == view.left
            container.right == view.right
            self.bottomLayout = container.bottom == view.bottom
            
            cancel.left == container.left + .margin
            cancel.bottom == container.bottom - .margin
            cancel.top == container.top + .margin
            
            save.centerY == cancel.centerY
            save.left == cancel.right + .margin
            save.right == container.right - .margin
            save.width == cancel.width
            
            content.bottom == container.bottom
            
            content.left == view.left
            content.right == view.right
            
            blur.edges == view.edges
            
            input.left == cancel.left
            input.right == save.right
            input.bottom == cancel.top - .margin
            
            header.left == input.left
            header.right == input.right
            header.bottom == input.top - .margin
            header.height >= -.margin ~ .defaultLow
            header.top == content.top + .margin
            
            close.edges == view.edges
        }
        
        inputControl.textField.delegate = self
        inputControl.textField.addTarget(self, action: #selector(editingChanged(_:)), for: .editingChanged)
        configureContent()
        
        saveButton.addTarget(self, action: #selector(saveAction), for: .touchUpInside)
        cancelButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        bgButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        
        NotificationCenter.default.addObserver(self, selector: #selector(willShowKeyboard(notification:)), name: UIResponder.keyboardWillShowNotification, object: nil)
    }
    
    func edit(content: EditableContent, headerView: UIView? = nil, saveText: String = "profile_save".localized()) {
        self.content = content
        self.headerView = headerView
        saveButton.setTitle(saveText, for: .normal)
        configureContent()
    }
    
    func checkValidation() {
        guard let text = inputControl.textField.text else { return }
        canSaveValue = validate(text)
    }
    
    func focus() {
        _ = inputControl.becomeFirstResponder()
    }
    
    @objc fileprivate func willShowKeyboard(notification: NSNotification) {
        if let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue {
            bottomLayout?.constant = -keyboardSize.height
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func saveAction() {
        view.endEditing(true)
        guard let text = inputControl.text else { return }
        save(text.trimmingCharacters(in: .whitespacesAndNewlines))
    }
    
    fileprivate func configureContent() {
        headerContainer.subviews.forEach{$0.removeFromSuperview()}
        if let header = headerView {
            headerContainer.addSubview(header)
            constrain(headerContainer, header) { content, view in
                view.edges == content.edges
            }
        }
        inputControl.text = nil
        inputControl.title = content.title
        inputControl.placeholder = content.placeholder
        inputControl.textField.keyboardType = content.keyboardType
        inputControl.textField.textContentType = content.contentType
        inputControl.textField.autocapitalizationType = content.capitalizationType
        inputControl.textField.isSecureTextEntry = content.contentType == .password
        inputControl.rightButton = content.contentType == .password ? hideShowButton : nil
        _ = inputControl.becomeFirstResponder()
    }
    
    @objc fileprivate func switchSecret(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        inputControl.textField.isSecureTextEntry = !inputControl.textField.isSecureTextEntry
    }
    
    @objc fileprivate func editingChanged(_ sender: UITextField) {
        guard let text = sender.text else { return }
        canSaveValue = validate(text)
        if let value = format(text) {
            sender.text = value
        }
    }
}

extension EditController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) else { return false }
        return newText.count <= content.charLimit || content.charLimit == 0
    }
}

protocol EditableContent {
    var title: String? {get}
    var placeholder: String? {get}
    var charLimit: Int {get}
    var keyboardType: UIKeyboardType {get}
    var contentType: UITextContentType {get}
    var capitalizationType: UITextAutocapitalizationType {get}
}


extension Profile.Item: EditableContent {
    var placeholder: String? {
        return value
    }
    
    var charLimit: Int {
        switch self {
        case .password:
            return 20
        default:
            return 32
        }
    }
}

struct EditValue: EditableContent {
    
    var title: String?
    
    var placeholder: String?
    
    var charLimit: Int
    
    var keyboardType: UIKeyboardType
    
    var contentType: UITextContentType
    
    var capitalizationType: UITextAutocapitalizationType
    
    static let code = EditValue(title: "hint_enter_code".localized(), placeholder: "code_hint".localized(), charLimit: 6, keyboardType: .numberPad, contentType: .smsCode, capitalizationType: .none)
}

fileprivate extension UITextContentType {
    static var smsCode: UITextContentType! {
        if #available(iOS 12.0, *) {
            return .oneTimeCode
        } else {
            return nil
        }
    }
}
