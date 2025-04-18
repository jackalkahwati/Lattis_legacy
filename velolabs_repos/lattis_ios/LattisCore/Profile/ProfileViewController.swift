//
//  ProfileViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 08/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography

class ProfileViewController: UIViewController {
    
    fileprivate let tableView = UITableView()
    fileprivate var user: User!
    fileprivate var sections: [Section] = []
    fileprivate let storage = UserStorage()
    fileprivate weak var edit: EditInfoViewController?
    fileprivate weak var editPhone: EditPhoneViewController?
    fileprivate var phoneNumbeRequired: Bool
    
    init(_ phoneNumbeRequired: Bool = false) {
        self.phoneNumbeRequired = phoneNumbeRequired
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }

        title = "profile".localized()
        addCloseButton()
        view.backgroundColor = .white
        
        view.addSubview(tableView)
        
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.register(ProfileCell.self, forCellReuseIdentifier: "cell")
        tableView.sectionHeaderHeight = 44
        tableView.sectionFooterHeight = 22
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 64
        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()
        
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        
        storage.current { [weak self] (user) in
            self?.didUpdate(user: user)
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        if phoneNumbeRequired {
            phoneNumbeRequired = false
            let edit = EditPhoneViewController(phone: user.phoneNumber) { [unowned self] (newPhone) in
                self.update(phone: newPhone)
            }
            self.present(edit, animated: true, completion: nil)
            self.editPhone = edit
        }
    }
    
    fileprivate func updateFirst(name: String) {
        self.edit?.startSaving()
        storage.update(user: user.updated(key: \.firstName, value: name)) { (error) in
            self.edit?.handle(error)
            self.edit?.stopSaving()
        }
    }
    
    fileprivate func updateLast(name: String) {
        self.edit?.startSaving()
        storage.update(user: user.updated(key: \.lastName, value: name)) { (error) in
            self.edit?.handle(error)
            self.edit?.stopSaving()
        }
    }
    
    fileprivate func update(email: String) {
        self.edit?.startSaving()
        storage.update(email: email) { [weak self] (error) in
            self?.edit?.stopSaving()
            if let err = error {
                self?.edit?.handle(err)
            } else {
                self?.confirm(email: email)
            }
        }
    }
    
    fileprivate func confirm(email: String) {
        edit?.update(info: .conrimationCode(description: "email_confirmation_note".localizedFormat(email), handler: { [unowned self] (code) in
            self.edit?.startSaving()
            self.storage.update(email: email, code: code, completion: { [weak self] (error) in
                if let err = error {
                    self?.edit?.handle(err)
                }
                self?.edit?.stopSaving()
            })
        }), textView: .code)
    }
    
    fileprivate func update(phone: String) {
        self.editPhone?.startSaving()
        storage.update(phone: phone) { [weak self] error in
            self?.editPhone?.stopSaving()
            if let err = error {
                self?.editPhone?.handle(err)
            } else {
                self?.confirm(phone: phone)
            }
        }
    }
    
    fileprivate func confirm(phone: String) {
        editPhone?.dismiss(animated: true, completion: {
            let edit = EditInfoViewController(.conrimationCode(description: "email_confirmation_note".localizedFormat(phone), handler: { [unowned self] (code) in
                self.edit?.startSaving()
                self.storage.update(phone: phone, code: code, completion: { [weak self] (error) in
                    self?.edit?.stopSaving()
                    if let err = error {
                        self?.edit?.handle(err)
                    }
                })
            }), textView: .code)
            self.edit = edit
            self.present(edit, animated: true, completion: nil)
        })
    }
    
    fileprivate func update(password: String, newPassword: String, controller: PasswordUpdateViewController) {
        controller.startSaving()
        storage.update(password: password, newPassword: newPassword) { (error) in
            if let e = error {
                controller.handle(e)
                controller.stopSaving()
            } else {
                controller.dismiss(animated: true, completion: nil)
            }
        }
    }
    
    fileprivate func deleteAccount() {
        startLoading("Deleting..")
        storage.deleteAccount { error in
            self.stopLoading {
                self.dismiss(animated: true) {
                    AppRouter.shared.logOut()
                }
            }
        }
    }
    
    func didUpdate(user: User) {
        self.user = user
        sections = [
            .init(title: "personal_info".localized(), items: [
                .init(title: "first_name".localized(), value: user.firstName, icon: .named("icon_person")) { [unowned self] in
                    let edit = EditInfoViewController(.first(name: user.firstName, handler: self.updateFirst(name:)), textView: .firstName)
                    self.present(edit, animated: true, completion: nil)
                    self.edit = edit
                },
                .init(title: "last_name".localized(), value: user.lastName, icon: .named("icon_person")) { [unowned self] in
                    let edit = EditInfoViewController(.last(name: user.lastName, handler: self.updateLast(name:)), textView: .larstName)
                    self.present(edit, animated: true, completion: nil)
                    self.edit = edit
                },
                .init(title: "email".localized(), value: user.email, icon: .named("icon_email")) { [unowned self] in
                    let edit = EditInfoViewController(.email(address: user.email, description: "email_update_note".localized(), handler: self.update(email:)), textView: .email)
                    self.present(edit, animated: true, completion: nil)
                    self.edit = edit
                },
                .init(title: "phone_number".localized(), value: user.phoneNumber ?? "mandatory_phone_action".localized(), icon: .named("icon_phone")) { [unowned self] in
                    let edit = EditPhoneViewController(phone: user.phoneNumber) { [unowned self] (newPhone) in
                        self.update(phone: newPhone)
                    }
                    self.present(edit, animated: true, completion: nil)
                    self.editPhone = edit
                }
            ]),
            .init(title: "security".localized(), items: [
                .init(title: nil, value: "change_password".localized(), icon: .named("icon_password")) { [unowned self] in
                    let edit = PasswordUpdateViewController(self.user.email, handler: self.update(password:newPassword:controller:))
                    self.present(edit, animated: true, completion: nil)
                }
            ]),
            .init(title: "Account", items: [
                .init(title: nil, value: "delete_account".localized(), icon: UIImage(systemName: "trash"))
                { [unowned self] in
                    
                    let alert = AlertController(title: "delete_account".localized(), message: .plain("delete_account_message".localized()))
                    alert.actions = [
                        .plain(title: "confirm".localized()) { [unowned self] in
                            self.deleteAccount()
                        },
                        .cancel
                    ]
                    present(alert, animated: true, completion: nil)
                }
            ])
        ]
        tableView.reloadData()
        edit?.dismiss(animated: true, completion: nil)
    }
}

extension ProfileViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        sections.count
    }
    
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        sections[section].items.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! ProfileCell
        cell.model = sections[indexPath.section].items[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        sections[indexPath.section].items[indexPath.row].handler()
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        return PlainTextSectionHeader(sections[section].title)
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        return UIView()
    }
}

extension ProfileViewController {
    struct Section {
        let title: String
        let items: [ProfileCell.Model]
    }
}
