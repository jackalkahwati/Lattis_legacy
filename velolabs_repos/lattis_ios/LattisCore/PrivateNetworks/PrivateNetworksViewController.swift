//
//  PrivateNetworksViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 08/07/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import CallKit

class PrivateNetworksViewController: UIViewController {
    
    public var shouldAddOne: Bool = false
    
    struct Section {
        let email: String
        var fleets: [Fleet]
    }
    
    fileprivate let tableView = UITableView()
    fileprivate let addButton = ActionButton()
    fileprivate let storage = FleetStorage()
    fileprivate var sections: [Section] = []
    fileprivate weak var edit: EditInfoViewController?

    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }

        view.backgroundColor = .white
        title = "private_networks".localized()
        addCloseButton()
        
        view.addSubview(tableView)
        tableView.register(FleetCell.self, forCellReuseIdentifier: "cell")
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.rowHeight = 64
        tableView.sectionHeaderHeight = 44
        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()
        
        addButton.action = .plain(title: "add_private_network".localized(), handler: { [unowned self] in
            self.addNew()
        })
        view.addSubview(addButton)
        
        constrain(tableView, addButton, view) { table, add, view in
            table.top == view.safeAreaLayoutGuide.top
            table.left == view.left
            table.right == view.right
            
            add.bottom == view.safeAreaLayoutGuide.bottom - .margin
            add.left == view.left + .margin
            add.right == view.right - .margin
            
            table.bottom == add.top - .margin
        }
        
        storage.fetch { [unowned self] (fleets) in
            self.calculate(fleets: fleets)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if shouldAddOne {
            shouldAddOne = false
            addNew()
        }
    }
    
    @objc fileprivate func addNew() {
        addNetwork()
    }
    
    fileprivate func addNetwork(email: String? = nil) {
        if let e = email {
            let alert = AlertController(title: email, message: .plain("private_fleet_add_email_label".localized()))
            alert.actions = [
                .plain(title: "Continue".localized(), handler: { [unowned self] in
                    self.addPrivateNetwork(email: e)
                }),
                .cancel
            ]
            present(alert, animated: true, completion: nil)
        } else {
            let edit = EditInfoViewController(.email(description: "private_fleet_add_email_label".localized()) { [unowned self] (value) in
                self.addPrivateNetwork(email: value)
                }, textView: .email)
            present(edit, animated: true, completion: nil)
            self.edit = edit
        }
    }
    
    fileprivate func calculate(fleets: [Fleet]) {
        sections.removeAll()
        for fleet in fleets {
            if let idx = sections.firstIndex(where: {$0.email == fleet.email}) {
                sections[idx].fleets.append(fleet)
            } else if let email = fleet.email {
                sections.append(.init(email: email, fleets: [fleet]))
            }
        }
        tableView.reloadData()
    }
    
    fileprivate func addPrivateNetwork(email: String) {
        storage.addFleet(email: email) { [weak self] (error) in
            if let err = error {
                if let e = err as? UserError, e == .noFleetsToAdd {
                    self?.noFleetsToAdd(for: email)
                } else {
                    if self?.edit == nil {
                        self?.handle(err)
                    } else {
                        self?.dismiss(animated: true) {
                            self?.handle(err)
                        }
                    }
                }
                Analytics.report(err)
            } else if let s = self {
                self?.stopLoading {
                    let info = s.confirm(email: email)
                    if let e = s.edit {
                        e.update(info: info, textView: .code)
                    } else {
                        let edit = EditInfoViewController(info, textView: .code)
                        s.present(edit, animated: true, completion: nil)
                        self?.edit = edit
                    }
                }
            }
        }
    }
    
    fileprivate func confirm(email: String) -> EditInfo {
        return .conrimationCode(description: "email_confirmation_note".localizedFormat(email), handler: { [unowned self] (code) in
            self.storage.addFleet(email: email, conrimationCode: code, compleion: { [weak self] (error) in
                self?.dismiss(animated: true) {
                    if let err = error {
                        if err.isHTTP(code: 401) {
                            self?.edit?.showDefaultWarning()
                        }
                        self?.handle(err)
                    }
                }
            })
        })
    }
    
    fileprivate func noFleetsToAdd(for email: String) {
        let completion: () -> () = {
            let alert = AlertController(title: email, message: .plain("no_private_networks_for_email".localized()))
            alert.actions = [
                .plain(title: "use_another_email".localized()) {
                    self.addNetwork(email: nil)
                },
                .ok
            ]
            self.present(alert, animated: true, completion: nil)
        }
        if edit == nil {
            stopLoading(completion: completion)
        } else {
            dismiss(animated: true, completion: completion)
        }
    }
}

extension PrivateNetworksViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        sections[section].fleets.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! FleetCell
        cell.fleet = sections[indexPath.section].fleets[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let sec = sections[section]
        return FleetEmailView(sec.email)
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }
}

class FleetEmailView: UIView {
    
    fileprivate let titleLabel = UILabel()
    
    init(_ email: String) {
        super.init(frame: .zero)
        
        backgroundColor = .white
        addSubview(titleLabel)
        titleLabel.font = .theme(weight: .medium, size: .body)
        titleLabel.textColor = .black
        titleLabel.text = email
        
        constrain(titleLabel, self) { title, view in
            
            title.left == view.left + .margin
            title.right == view.right - .margin
            title.centerY == view.centerY
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
