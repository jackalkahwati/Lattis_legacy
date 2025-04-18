//
//  ProfileProfileViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 30/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography
import Atributika

class ProfileViewController: ViewController {
    
    var interactor: ProfileInteractorInput!
    fileprivate let tableView = UITableView()
    fileprivate let codeInput = TitledInputControl(title: "hint_enter_code".localized(), hint: "code_hint".localized())
    fileprivate weak var editController: EditController?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "myprofile".localized()
        addCloseButton()
        
        view.backgroundColor = .white
        view.addSubview(tableView)
        
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.tableFooterView = UIView()
        
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        
        tableView.register(ProfileCell.self, forCellReuseIdentifier: "edit")
        tableView.estimatedRowHeight = .rowHeightBig
        tableView.rowHeight = UITableView.automaticDimension
        tableView.sectionHeaderHeight = .rowHeight
        tableView.delegate = self
        tableView.dataSource = self
        
        codeInput.textField.delegate = self
        codeInput.textField.addTarget(self, action: #selector(editingChanged(_:)), for: .editingChanged)
        codeInput.placeholder = "code_hint".localized()
        codeInput.textField.keyboardType = .numberPad
        if #available(iOS 12.0, *) {
            codeInput.textField.textContentType = .oneTimeCode
        } else {
        }
        
        interactor.viewDidLoad()
    }
    
    @objc fileprivate func editingChanged(_ sender: UITextField) {
        editController?.checkValidation()
    }
    
    @objc fileprivate func facebookPopUp() {
        show(warning: "warning_facebook_text".localized(), title: "warning_facebook_title".localized())
    }
    
    override func didHideWarning() {
        _ = editController?.focus()
    }
}

extension ProfileViewController: ProfileInteractorOutput {
    func enterPasswordCode(phoneNumber: String) {
        stopLoading(completion: nil)
        let label = UILabel()
        label.numberOfLines = 0
        label.textAlignment = .center
        label.attributedText = "restore_pass_hint_sms_html".localizedFormat(phoneNumber).style(tags: .b).styleAll(.all).attributedString
        let view = UIView()
        view.addSubview(label)
        view.addSubview(codeInput)
        constrain(view, label, codeInput) { view, title, code in
            title.top == view.top
            title.left == view.left
            title.right == view.right
            
            code.top == title.bottom + .margin
            code.left == view.left
            code.right == view.right
            code.bottom == view.bottom
        }
        let edit = EditController(content: Profile.Item.password, headerView: view)
        edit.save = { [unowned self] password in
            self.interactor.update(password: password, code: self.codeInput.text)
        }
        edit.validate = { [unowned self] value in
            guard let code = self.codeInput.text, code.count == .codeLimit else { return false }
            return value.count >= Int.passwordLimit.min
        }
        self.editController = edit
        present(edit, animated: true, completion: nil)
    }
    
    func enterCode(phoneNumber: String) {
        stopLoading(completion: nil)
        let label = UILabel()
        label.numberOfLines = 0
        label.textAlignment = .center
        label.attributedText = "welcome_confirmation_hint_sms_html".localizedFormat(phoneNumber).style(tags: .b).styleAll(.all).attributedString
        editController?.edit(content: EditValue.code, headerView: label, saveText: "share_submit_code".localized().lowercased().capitalized)
        editController?.save = { [unowned self] code in
            self.interactor.update(phoneNumber: phoneNumber, code: code)
        }
        editController?.format = {_ in return nil}
        editController?.validate = { code in return code.count == .codeLimit }
    }
    
    func enterCode(email: String) {
        stopLoading(completion: nil)
        let label = UILabel()
        label.numberOfLines = 0
        label.textAlignment = .center
        label.attributedText = "welcome_confirmation_hint_sms_html".localizedFormat(email).style(tags: .b).styleAll(.all).attributedString
        editController?.edit(content: EditValue.code, headerView: label, saveText: "share_submit_code".localized().lowercased().capitalized)
        editController?.save = { [unowned self] code in
            self.interactor.update(email: email, code: code)
        }
        editController?.format = {_ in return nil}
        editController?.validate = { code in return code.count == .codeLimit }
    }
    
    func refresh() {
        if interactor.isFBUser() {
            navigationItem.rightBarButtonItem = UIBarButtonItem(image: UIImage(named: "icon_facebook"), style: .plain, target: self, action: #selector(facebookPopUp))
            navigationItem.rightBarButtonItem?.tintColor = .fbBlue
        } else {
            navigationItem.rightBarButtonItem = nil
        }
        tableView.reloadData()
    }
    
    func saved() {
        stopLoading(completion: nil)
        dismiss(animated: true, completion: nil)
    }
}

extension ProfileViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.numberOfRows(in: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = interactor.item(for: indexPath)
        let cell = tableView.dequeueReusableCell(withIdentifier: "edit", for: indexPath) as! ProfileCell
        cell.item = item
        cell.isFacebookUser = interactor.isFBUser()
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        guard let cell = tableView.cellForRow(at: indexPath) as? ProfileCell, cell.accessoryView != nil else { return }
        let item = interactor.item(for: indexPath)
        let edit = EditController(item)
        if case .password = item {
            return self.interactor.update(password: nil, code: nil)
        }
        edit.save = { [unowned self] value in
            switch item {
            case .firstName:
                self.interactor.save(firstName: value)
            case .lastName:
                self.interactor.save(lastName: value)
            case .phoneNmber:
                self.interactor.update(phoneNumber: value, code: nil)
            case .email:
                self.interactor.update(email: value, code: nil)
            default:
                break
            }
        }
        edit.format = { value in
            switch item {
            case .phoneNmber:
                return value.phoneNumberFormat
            default:
                return nil
            }
        }
        edit.validate = { [unowned self] value in
            switch item {
            case .email:
                return value.isValidEmail && self.interactor.isDifferent(email: value)
            case .firstName, .lastName:
                return value.count > 1
            case .phoneNmber:
                return value.isValidPhoneNumber && self.interactor.isDifferent(phoneNumber: value)
            default:
                return true
            }
        }
        present(edit, animated: true, completion: nil)
        self.editController = edit
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        return TableSectionView("personal_details".localized())
    }
}

extension ProfileViewController: UITextFieldDelegate {
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        guard let newText = (textField.text as NSString?)?.replacingCharacters(in: range, with: string) else { return false }
        return newText.count <= .codeLimit
    }
}

fileprivate extension Style {
    static let b = Style("b").font(.elTitle).foregroundColor(.black)
    static let all = Style.font(.elRegular).foregroundColor(.black)
}

extension EditController {
    convenience init(_ item: Profile.Item) {
        let label = UILabel()
        label.textAlignment = .center
        label.textColor = .black
        label.numberOfLines = 0
        label.font = .elRegular
        switch item {
        case .phoneNmber:
            label.text = "we_will_send_sms".localized()
            self.init(content: item, headerView: label, saveText: "action_send_code".localized())
        case .email:
            label.text = "we_will_send_email".localized()
            self.init(content: item, headerView: label, saveText: "action_send_code".localized())
        default:
            self.init(content: item)
        }
    }
}

