//
//  EmergencyEmergencyViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 14/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Cartography

class EmergencyViewController: ViewController {

    var interactor: EmergencyInteractorInput!
    weak var delegate: EmergencyDelegate?
    fileprivate let tableView = UITableView()
    fileprivate let emptyView = UIView()
    
    override var preferredStatusBarStyle: UIStatusBarStyle {
        return .default
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        if delegate != nil {
            addCloseButton()
        } else {
            addMenuButton()
        }
        title = "action_emergency_contacts".localized()
        
        view.addSubview(tableView)
        constrain(tableView, view) { table, view in
            table.edges == view.edges
        }
        
        let headerView = UIView(frame: .init(x: 0, y: 0, width: 0, height: 200))
        headerView.backgroundColor = .white
        let titleLabel = UILabel()
        headerView.addSubview(titleLabel)
        titleLabel.textColor = .black
        titleLabel.font = .elRegular
        titleLabel.numberOfLines = 0
        titleLabel.textAlignment = .center
        titleLabel.text = "ec_contact_description".localized()
        
        constrain(headerView, titleLabel) { header, title in
            title.edges == inset(header.edges, .margin)
        }
        
        let addButton = UIButton(type: .custom)
        let footerView = UIView(frame: .init(x: 0, y: 0, width: 100, height: 100))
        footerView.addSubview(addButton)
        footerView.backgroundColor = .white
        addButton.setTitle("select_contacts".localized(), for: .normal)
        smallPositiveStyle(addButton)
        addButton.addTarget(self, action: #selector(contacts(_:)), for: .touchUpInside)
        
        constrain(footerView, addButton) { footer, button in
            button.center == footer.center
            button.left >= footer.left + .margin ~ .defaultLow
            button.right >= footer.right - .margin ~ .defaultLow
        }
        
        tableView.rowHeight = 65
        tableView.separatorInset = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        tableView.tableHeaderView = headerView
        tableView.tableFooterView = footerView
        tableView.register(EmergencyCell.self, forCellReuseIdentifier: "cell")
        tableView.dataSource = self
        tableView.delegate = self
        
        interactor.start()
    }
    
    @objc fileprivate func contacts(_ sender: Any) {
        interactor.selectContacts()
    }
    
    override func close() {
        super.close()
        delegate?.didCloseEmergency()
    }
    
    @IBAction func setUp(_ sender: Any) {
        interactor.requestAccess()
    }
}

extension EmergencyViewController: EmergencyInteractorOutput {
    func refresh() {
        tableView.beginUpdates()
        tableView.reloadSections(IndexSet(integer: 0), with: .automatic)
        tableView.endUpdates()
    }
    
    func showHint() {
        emptyView.backgroundColor = .white
        title = "title_information".localized()
        let titleLabel = UILabel()
        titleLabel.font = .elTitle
        titleLabel.textColor = .black
        titleLabel.numberOfLines = 0
        titleLabel.textAlignment = .center
        titleLabel.text = "ec_contact_title".localized()
        emptyView.addSubview(titleLabel)
        
        let bodyLabel = UILabel()
        bodyLabel.font = .elRegular
        bodyLabel.textColor = .elBrownGreyThree
        bodyLabel.numberOfLines = 0
        bodyLabel.textAlignment = .center
        bodyLabel.text = "ec_contact_description".localized()
        emptyView.addSubview(bodyLabel)
        
        let okButton = UIButton(type: .custom)
        emptyView.addSubview(okButton)
        okButton.setTitle("ok".localized().lowercased().capitalized, for: .normal)
        smallPositiveStyle(okButton)
        okButton.addTarget(self, action: #selector(setUp(_:)), for: .touchUpInside)
        
        view.addSubview(emptyView)
        
        constrain(emptyView, titleLabel, bodyLabel, okButton, view) { empty, title, body, ok, view in
            empty.edges == view.edges
            
            title.top == empty.top + .margin
            title.left == empty.left + .margin
            title.right == empty.right - .margin
            
            body.left == title.left
            body.right == title.right
            body.top == title.bottom
            
            ok.left == empty.left + .margin*3
            ok.right == empty.right - .margin*3
            ok.bottom == empty.safeAreaLayoutGuide.bottom - .margin
        }
    }
    
    func hideHint() {
        title = "action_emergency_contacts".localized()
        UIView.animate(withDuration: 0.3, animations: {
            self.emptyView.alpha = 0
        }, completion: { _ in
            self.emptyView.removeFromSuperview()
        })
    }
}

extension EmergencyViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return interactor.numberOfRows(in: section)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! EmergencyCell
        cell.delegate = interactor
        cell.contact = interactor.item(for: indexPath)
        return cell
    }
}
